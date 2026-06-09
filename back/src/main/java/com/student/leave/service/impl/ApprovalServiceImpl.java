package com.student.leave.service.impl;

import com.student.leave.common.BusinessException;
import com.student.leave.entity.ApprovalProcess;
import com.student.leave.entity.LeaveApplication;
import com.student.leave.mapper.ApprovalProcessMapper;
import com.student.leave.mapper.LeaveApplicationMapper;
import com.student.leave.service.ApprovalService;
import com.student.leave.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审批服务实现类
 */
@Service
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private ApprovalProcessMapper approvalProcessMapper;

    @Autowired
    private LeaveApplicationMapper leaveApplicationMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public List<ApprovalProcess> getPendingApprovals(Long approverId, String processType) {
        return approvalProcessMapper.selectPendingByApproverId(approverId, processType);
    }

    @Override
    @Transactional
    public void processApproval(Long approvalId, Long approverId, String result, String opinion, Long nextApproverId) {
        ApprovalProcess approval = approvalProcessMapper.selectById(approvalId);
        if (approval == null) {
            throw new BusinessException("审批记录不存在");
        }

        // 验证审批人权限
        if (!approval.getApproverId().equals(approverId)) {
            throw new BusinessException("无权限审批该申请");
        }

        // 检查审批状态
        if (!"PENDING".equals(approval.getStatus())) {
            throw new BusinessException("该申请已审批，请勿重复操作");
        }

        LeaveApplication leaveApplication = leaveApplicationMapper.selectById(approval.getLeaveId());
        if (leaveApplication == null) {
            throw new BusinessException("请假申请不存在");
        }

        // 更新审批记录
        approvalProcessMapper.updateStatus(approvalId, result, opinion);

        if ("APPROVED".equals(result)) {
            // 审批通过
            if (nextApproverId != null) {
                // 如果指定了下一级审批人，创建新的审批记录
                ApprovalProcess existNextApproval = approvalProcessMapper.selectByLeaveIdAndLevel(
                        approval.getLeaveId(), approval.getLevel() + 1);
                
                if (existNextApproval == null) {
                    // 不存在下一级审批记录，创建新的
                    ApprovalProcess nextApproval = new ApprovalProcess();
                    nextApproval.setLeaveId(approval.getLeaveId());
                    nextApproval.setApproverId(nextApproverId);
                    nextApproval.setLevel(approval.getLevel() + 1);
                    nextApproval.setProcessType(approval.getProcessType());
                    nextApproval.setStatus("PENDING");
                    approvalProcessMapper.insert(nextApproval);
                    
                    // 发送通知给下一级审批人
                    messageService.sendMessage(nextApproverId, "APPROVAL", "新的请假申请待审批",
                            "有新的请假申请需要您审批", approval.getLeaveId());
                } else {
                    // 已存在下一级审批记录，更新审批人
                    // 这里可以根据需求决定是更新还是保持原样
                    messageService.sendMessage(existNextApproval.getApproverId(), "APPROVAL", "新的请假申请待审批",
                            "有新的请假申请需要您审批", approval.getLeaveId());
                }
            } else {
                // 没有指定下一级审批人，检查是否已有下一级审批
                ApprovalProcess nextApproval = approvalProcessMapper.selectByLeaveIdAndLevel(
                        approval.getLeaveId(), approval.getLevel() + 1);

                if (nextApproval != null) {
                    // 有下一级审批，发送通知
                    messageService.sendMessage(nextApproval.getApproverId(), "APPROVAL", "新的请假申请待审批",
                            "有新的请假申请需要您审批", approval.getLeaveId());
                } else {
                    // 没有下一级审批，更新请假状态为已通过
                    leaveApplicationMapper.updateStatus(approval.getLeaveId(), "APPROVED");
                    // 通知申请人
                    messageService.sendMessage(leaveApplication.getUserId(), "APPROVAL", "请假申请已通过",
                            "您的请假申请已通过审批", approval.getLeaveId());
                }
            }
        } else if ("REJECTED".equals(result)) {
            // 审批驳回，更新请假状态为已驳回
            leaveApplicationMapper.updateStatus(approval.getLeaveId(), "REJECTED");
            // 通知申请人
            messageService.sendMessage(leaveApplication.getUserId(), "APPROVAL", "请假申请已驳回",
                    "您的请假申请已被驳回，驳回原因：" + opinion, approval.getLeaveId());
        }
    }

    @Override
    public List<ApprovalProcess> getApprovalHistory(Long approverId, String status, String processType) {
        return approvalProcessMapper.selectHistoryByApproverId(approverId, status, processType);
    }

    @Override
    public List<ApprovalProcess> getApprovalsByLeaveId(Long leaveId) {
        return approvalProcessMapper.selectByLeaveId(leaveId, "LEAVE");
    }
}
