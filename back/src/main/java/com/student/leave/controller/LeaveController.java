package com.student.leave.controller;

import com.student.leave.common.Result;
import com.student.leave.entity.LeaveApplication;
import com.student.leave.entity.LeaveReturnApplication;
import com.student.leave.service.LeaveApplicationService;
import com.student.leave.service.LeaveReturnApplicationService;
import com.student.leave.vo.LeaveReturnRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

/**
 * 请假申请控制器
 */
@RestController
@RequestMapping("/leave")
public class LeaveController {

    @Autowired
    private LeaveApplicationService leaveApplicationService;

    @Autowired
    private LeaveReturnApplicationService returnApplicationService;

    /**
     * 创建请假申请
     */
    @PostMapping("/apply")
    public Result<Long> applyLeave(@RequestBody LeaveApplication leaveApplication, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        leaveApplication.setUserId(userId);
        Long leaveId = leaveApplicationService.createLeaveApplication(leaveApplication);
        return Result.success("申请成功", leaveId);
    }

    /**
     * 查询请假列表
     */
    @GetMapping("/list")
    public Result<List<LeaveApplication>> getLeaveList(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endTime,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        System.out.println("[CONTROLLER DEBUG] 请假列表请求 - userId: " + userId + ", role: " + role);
        System.out.println("[CONTROLLER DEBUG] 请求参数 - status: " + status + ", type: " + type + ", startTime: " + startTime + ", endTime: " + endTime);
        
        List<LeaveApplication> list = leaveApplicationService.getLeaveApplicationsByRole(userId, role, status, type, startTime, endTime);
        return Result.success(list);
    }

    /**
     * 查询请假详情
     */
    @GetMapping("/detail/{id}")
    public Result<LeaveApplication> getLeaveDetail(@PathVariable Long id) {
        LeaveApplication leaveApplication = leaveApplicationService.getLeaveApplicationById(id);
        return Result.success(leaveApplication);
    }

    /**
     * 撤销请假申请
     */
    @PutMapping("/cancel/{id}")
    public Result<Void> cancelLeave(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        leaveApplicationService.cancelLeaveApplication(id, userId);
        return Result.success("撤销成功", null);
    }

    /**
     * 销假（上级直接销假）
     */
    @PostMapping("/return/{leaveId}")
    public Result<Void> returnFromLeave(@PathVariable Long leaveId, 
                                        @RequestParam(required = false) String remark,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有非学生角色可以直接销假
        if ("STUDENT".equals(role)) {
            return Result.error("学生不能直接销假，请通过销假申请流程");
        }
        
        leaveApplicationService.returnFromLeave(leaveId, userId, role, remark);
        return Result.success("销假成功", null);
    }

    /**
     * 查询销假提醒
     */
    @GetMapping("/return-reminder")
    public Result<?> getReturnReminder(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        if ("STUDENT".equals(role)) {
            // 学生：返回自己需要销假的记录（已结束但未销假的请假）
            List<LeaveApplication> expiringSoon = leaveApplicationService.getExpiringSoonLeaves(24);
            return Result.success(expiringSoon);
        } else {
            // 上级：返回待审批的销假申请列表
            List<LeaveReturnApplication> pendingApplications = returnApplicationService.getPendingApplications(userId);
            return Result.success(pendingApplications);
        }
    }

    /**
     * 学生发起销假申请
     */
    @PostMapping("/return-application/apply/{leaveId}")
    public Result<Long> applyReturn(@PathVariable Long leaveId,
                                    @RequestBody(required = false) java.util.Map<String, Object> requestBody,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有学生可以发起销假申请
        if (!"STUDENT".equals(role)) {
            return Result.error("只有学生可以发起销假申请");
        }
        
        // 从请求体中获取remark参数
        String remark = null;
        if (requestBody != null && requestBody.containsKey("remark")) {
            remark = (String) requestBody.get("remark");
        }
        
        Long applicationId = returnApplicationService.applyReturn(leaveId, userId, remark);
        return Result.success("销假申请提交成功", applicationId);
    }

    /**
     * 查询学生的销假申请列表
     */
    @GetMapping("/return-application/my-list")
    public Result<List<LeaveReturnApplication>> getMyReturnApplications(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<LeaveReturnApplication> list = returnApplicationService.getApplicationsByStudentId(userId, status);
        return Result.success(list);
    }

    /**
     * 查询待审批的销假申请列表（上级视角）
     */
    @GetMapping("/return-application/pending")
    public Result<List<LeaveReturnApplication>> getPendingReturnApplications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有非学生角色可以查看
        if ("STUDENT".equals(role)) {
            return Result.error("无权访问");
        }
        
        List<LeaveReturnApplication> list = returnApplicationService.getPendingApplications(userId);
        return Result.success(list);
    }

    /**
     * 查询已审批的销假申请列表（上级视角）
     */
    @GetMapping("/return-application/approved")
    public Result<List<LeaveReturnApplication>> getApprovedReturnApplications(
            @RequestParam(required = false) String status,
            HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有非学生角色可以查看
        if ("STUDENT".equals(role)) {
            return Result.error("无权访问");
        }
        
        List<LeaveReturnApplication> list = returnApplicationService.getApprovedApplications(status);
        return Result.success(list);
    }

    /**
     * 审批销假申请
     */
    @PostMapping("/return-application/approve/{applicationId}")
    public Result<Void> approveReturnApplication(@PathVariable Long applicationId,
                                           @RequestBody java.util.Map<String, Object> params,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有非学生角色可以审批
        if ("STUDENT".equals(role)) {
            return Result.error("学生无权审批销假申请");
        }
        
        Boolean approved = (Boolean) params.get("approved");
        String opinion = (String) params.get("opinion");
        
        if (approved == null) {
            return Result.error("请指定审批结果");
        }
        
        returnApplicationService.approveReturnApplication(applicationId, userId, approved, opinion);
        
        String message = approved ? "审批通过" : "审批驳回";
        return Result.success(message, null);
    }

    /**
     * 查询销假申请详情
     */
    @GetMapping("/return-application/detail/{id}")
    public Result<LeaveReturnApplication> getReturnApplicationDetail(@PathVariable Long id) {
        LeaveReturnApplication application = returnApplicationService.getApplicationById(id);
        return Result.success(application);
    }

    /**
     * 查询学生提交过销假申请的请假记录列表（去重）
     */
    @GetMapping("/return-records")
    public Result<List<LeaveReturnRecordVO>> getMyReturnRecords(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 验证权限：只有学生可以查看
        if (!"STUDENT".equals(role)) {
            return Result.error("无权访问");
        }
        
        List<LeaveReturnRecordVO> records = returnApplicationService.getMyReturnRecords(userId);
        return Result.success(records);
    }
}
