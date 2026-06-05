package com.student.leave.service.impl;

import com.student.leave.common.BusinessException;
import com.student.leave.entity.ApprovalProcess;
import com.student.leave.entity.LeaveApplication;
import com.student.leave.entity.LeaveReturn;
import com.student.leave.entity.LeaveReturnApplication;
import com.student.leave.entity.User;
import com.student.leave.mapper.ApprovalProcessMapper;
import com.student.leave.mapper.LeaveApplicationMapper;
import com.student.leave.mapper.LeaveReturnMapper;
import com.student.leave.mapper.UserMapper;
import com.student.leave.service.LeaveApplicationService;
import com.student.leave.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 请假申请服务实现类
 */
@Service
public class LeaveApplicationServiceImpl implements LeaveApplicationService {

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;

    @Autowired
    private ApprovalProcessMapper approvalProcessMapper;

    @Autowired
    private LeaveReturnMapper leaveReturnMapper;

    @Autowired
    private com.student.leave.mapper.LeaveReturnApplicationMapper returnApplicationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Override
    @Transactional
    public Long createLeaveApplication(LeaveApplication leaveApplication) {
        // 验证时间合法性
        if (leaveApplication.getStartTime().after(leaveApplication.getEndTime())) {
            throw new BusinessException("开始时间不能晚于结束时间");
        }

        // 检查时间冲突
        int conflictCount = leaveApplicationMapper.checkTimeConflict(
                leaveApplication.getUserId(),
                leaveApplication.getStartTime(),
                leaveApplication.getEndTime(),
                null
        );
        if (conflictCount > 0) {
            throw new BusinessException("该时间段已有请假记录，请检查");
        }

        // 设置初始状态为待审批
        leaveApplication.setStatus("PENDING");

        // 创建请假记录
        leaveApplicationMapper.insert(leaveApplication);

        // 根据请假天数确定审批流程
        Integer days = leaveApplication.getDays();
        User user = userMapper.selectById(leaveApplication.getUserId());

        // 查找辅导员（这里简化处理，实际应该根据班级查找）
        List<User> counselors = userMapper.selectList("COUNSELOR", 1);
        if (counselors.isEmpty()) {
            throw new BusinessException("未找到可用的辅导员");
        }
        User counselor = counselors.get(0);

        // 创建第一级审批流程（辅导员）
        ApprovalProcess firstApproval = new ApprovalProcess();
        firstApproval.setLeaveId(leaveApplication.getId());
        firstApproval.setProcessType("LEAVE");  // 设置为请假审批
        firstApproval.setApproverId(counselor.getId());
        firstApproval.setLevel(1);
        firstApproval.setStatus("PENDING");
        approvalProcessMapper.insert(firstApproval);

        // 如果请假天数大于3天，需要副书记审批
        if (days > 3) {
            List<User> secretaries = userMapper.selectList("SECRETARY", 1);
            if (!secretaries.isEmpty()) {
                User secretary = secretaries.get(0);
                ApprovalProcess secondApproval = new ApprovalProcess();
                secondApproval.setLeaveId(leaveApplication.getId());
                secondApproval.setProcessType("LEAVE");  // 设置为请假审批
                secondApproval.setApproverId(secretary.getId());
                secondApproval.setLevel(2);
                secondApproval.setStatus("PENDING");
                approvalProcessMapper.insert(secondApproval);
            }
        }

        // 发送通知给第一级审批人
        messageService.sendMessage(counselor.getId(), "APPROVAL", "新的请假申请待审批",
                "学生" + user.getName() + "提交了请假申请，请及时审批", leaveApplication.getId());

        return leaveApplication.getId();
    }

    @Override
    public LeaveApplication getLeaveApplicationById(Long id) {
        LeaveApplication leaveApplication = leaveApplicationMapper.selectById(id);
        if (leaveApplication == null) {
            throw new BusinessException("请假申请不存在");
        }
        
        // 查询关联的请假审批流程（只查询 LEAVE 类型）
        List<ApprovalProcess> leaveApprovalProcesses = approvalProcessMapper.selectByLeaveId(id, "LEAVE");
        leaveApplication.setApprovalProcesses(leaveApprovalProcesses);
        
        // 查询关联的销假记录
        LeaveReturn leaveReturn = leaveReturnMapper.selectByLeaveId(id);
        leaveApplication.setLeaveReturn(leaveReturn);
        
        // 查询关联的销假申请及其审批流程（只查询 RETURN 类型）
        List<LeaveReturnApplication> returnApplications = returnApplicationMapper.selectByLeaveId(id);
        if (returnApplications != null && !returnApplications.isEmpty()) {
            // 为每个销假申请查询其审批流程
            for (LeaveReturnApplication returnApp : returnApplications) {
                // 注意：这里返回的是该请假下所有RETURN类型的审批流程
                // 前端需要根据审批流程的create_time和销假申请的apply_time来匹配
                List<ApprovalProcess> allReturnProcesses = approvalProcessMapper.selectByLeaveId(returnApp.getLeaveId(), "RETURN");
                returnApp.setApprovalProcesses(allReturnProcesses);
            }
            leaveApplication.setReturnApplications(returnApplications);
        }
        
        return leaveApplication;
    }

    @Override
    public List<LeaveApplication> getLeaveApplicationsByUserId(Long userId, String status, String type, Date startTime, Date endTime) {
        List<LeaveApplication> list = leaveApplicationMapper.selectByUserId(userId, status, type, startTime, endTime);
        // 为每个请假记录填充销假信息
        fillLeaveReturnInfo(list);
        return list;
    }

    @Override
    public List<LeaveApplication> getLeaveApplicationsByRole(Long userId, String role, String status, String type, Date startTime, Date endTime) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        System.out.println("[DEBUG] 查询请假列表 - userId: " + userId + ", role: " + role + ", username: " + user.getUsername());

        List<LeaveApplication> list;
        switch (role) {
            case "STUDENT":
                System.out.println("[DEBUG] 学生查询 - 只查询自己的请假记录");
                list = leaveApplicationMapper.selectByUserId(userId, status, type, startTime, endTime);
                break;
            case "COUNSELOR":
                System.out.println("[DEBUG] 辅导员查询 - 查询班级: " + user.getClassId());
                // 辅导员查看所管理班级的请假记录
                list = leaveApplicationMapper.selectByClassId(user.getClassId(), status, type, startTime, endTime);
                break;
            case "SECRETARY":
                System.out.println("[DEBUG] 副书记查询 - 查询学院: " + user.getCollegeId());
                // 副书记查看本学院的请假记录
                list = leaveApplicationMapper.selectByCollegeId(user.getCollegeId(), status, type, startTime, endTime);
                break;
            case "ADMIN":
                System.out.println("[DEBUG] 管理员查询 - 查询所有记录");
                // 管理员查看所有请假记录
                list = leaveApplicationMapper.selectAll(status, type, startTime, endTime);
                break;
            default:
                System.out.println("[DEBUG] 未知角色: " + role + "，默认查询自己的记录");
                // 未知角色，默认查询自己的记录
                list = leaveApplicationMapper.selectByUserId(userId, status, type, startTime, endTime);
                break;
        }
        
        // 为每个请假记录填充销假信息
        fillLeaveReturnInfo(list);
        return list;
    }
    
    /**
     * 为请假列表填充销假信息
     */
    private void fillLeaveReturnInfo(List<LeaveApplication> list) {
        if (list != null && !list.isEmpty()) {
            for (LeaveApplication leave : list) {
                LeaveReturn leaveReturn = leaveReturnMapper.selectByLeaveId(leave.getId());
                leave.setLeaveReturn(leaveReturn);
            }
        }
    }

    @Override
    @Transactional
    public void cancelLeaveApplication(Long leaveId, Long userId) {
        LeaveApplication leaveApplication = leaveApplicationMapper.selectById(leaveId);
        if (leaveApplication == null) {
            throw new BusinessException("请假申请不存在");
        }

        // 验证是否是本人的申请
        if (!leaveApplication.getUserId().equals(userId)) {
            throw new BusinessException("只能撤销自己的请假申请");
        }

        // 检查请假状态
        if (!"PENDING".equals(leaveApplication.getStatus()) && !"APPROVED".equals(leaveApplication.getStatus())) {
            throw new BusinessException("该请假申请不能撤销");
        }

        // 如果是已通过的请假，检查是否已开始
        if ("APPROVED".equals(leaveApplication.getStatus()) && leaveApplication.getStartTime().before(new Date())) {
            throw new BusinessException("请假已开始，不能撤销");
        }

        // 更新请假状态为已撤销
        leaveApplicationMapper.updateStatus(leaveId, "CANCELLED");

        // 更新所有待审批的流程记录为已取消
        List<ApprovalProcess> approvalProcesses = approvalProcessMapper.selectByLeaveId(leaveId, "LEAVE");
        for (ApprovalProcess process : approvalProcesses) {
            if ("PENDING".equals(process.getStatus())) {
                approvalProcessMapper.updateStatus(process.getId(), "CANCELLED", "申请人撤销");
            }
        }

        // 通知审批人
        for (ApprovalProcess process : approvalProcesses) {
            messageService.sendMessage(process.getApproverId(), "SYSTEM", "请假申请已撤销",
                    "学生已撤销请假申请", leaveId);
        }
    }

    @Override
    @Transactional
    public void returnFromLeave(Long leaveId, Long userId, String role, String remark) {
        LeaveApplication leaveApplication = leaveApplicationMapper.selectById(leaveId);
        if (leaveApplication == null) {
            throw new BusinessException("请假申请不存在");
        }

        // 验证权限：学生不能销假，只有辅导员、副书记、管理员可以
        if ("STUDENT".equals(role)) {
            throw new BusinessException("学生不能进行销假操作");
        }

        // 验证请假状态
        if (!"APPROVED".equals(leaveApplication.getStatus())) {
            throw new BusinessException("该请假申请未通过审批，无法销假");
        }

        // 检查是否已销假
        LeaveReturn existReturn = leaveReturnMapper.selectByLeaveId(leaveId);
        if (existReturn != null) {
            throw new BusinessException("该请假已销假，请勿重复操作");
        }

        // 判断是否逾期
        Date now = new Date();
        Integer isOverdue = now.after(leaveApplication.getEndTime()) ? 1 : 0;

        // 创建销假记录
        LeaveReturn leaveReturn = new LeaveReturn();
        leaveReturn.setLeaveId(leaveId);
        leaveReturn.setReturnTime(now);
        leaveReturn.setIsOverdue(isOverdue);
        leaveReturn.setRemark(remark);
        leaveReturnMapper.insert(leaveReturn);

        // 处理该请假已有的待审批销假申请：将所有待审批的销假申请标记为已通过
        List<LeaveReturnApplication> allApplications = returnApplicationMapper.selectByLeaveId(leaveId);
        boolean hasPendingApplication = false;
        
        for (LeaveReturnApplication application : allApplications) {
            if ("PENDING".equals(application.getStatus())) {
                hasPendingApplication = true;
                // 更新销假申请状态为已通过
                returnApplicationMapper.updateApprovalStatus(application.getId(), "APPROVED", userId, 
                    "上级直接销假，系统自动通过");
                
                // 发送通知给学生
                messageService.sendMessage(application.getStudentId(), "SYSTEM", "销假申请已通过",
                    "您的销假申请已通过审批（上级直接销假）", application.getLeaveId());
            }
        }
        
        // 如果有待审批的销假申请，需要将其对应的审批流程也标记为已通过
        if (hasPendingApplication) {
            List<ApprovalProcess> allReturnProcesses = approvalProcessMapper.selectByLeaveId(leaveId, "RETURN");
            for (ApprovalProcess process : allReturnProcesses) {
                if ("PENDING".equals(process.getStatus())) {
                    approvalProcessMapper.updateStatus(process.getId(), "APPROVED", "上级直接销假，系统自动通过");
                }
            }
        }

        // 发送销假通知给辅导员
        List<ApprovalProcess> approvalProcesses = approvalProcessMapper.selectByLeaveId(leaveId, "LEAVE");
        if (!approvalProcesses.isEmpty()) {
            ApprovalProcess firstApproval = approvalProcesses.get(0);
            String noticeContent = isOverdue == 1 ? "学生逾期销假" : "学生已按时销假";
            messageService.sendMessage(firstApproval.getApproverId(), "RETURN", "销假通知",
                    noticeContent, leaveId);
        }
    }

    @Override
    public List<LeaveApplication> getExpiringSoonLeaves(Integer hours) {
        return leaveApplicationMapper.selectExpiringSoon(hours);
    }

    @Override
    public List<LeaveApplication> getOverdueLeaves() {
        return leaveApplicationMapper.selectOverdue();
    }
}
