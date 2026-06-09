package com.student.leave.controller;

import com.student.leave.common.Result;
import com.student.leave.entity.ApprovalProcess;
import com.student.leave.service.ApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * 审批控制器
 */
@RestController
@RequestMapping("/approval")
public class ApprovalController {

    @Autowired
    private ApprovalService approvalService;

    /**
     * 查询待审批列表
     * @param processType 审批类型：LEAVE-请假审批, RETURN-销假审批（可选，不传则返回所有类型）
     */
    @GetMapping("/pending")
    public Result<List<ApprovalProcess>> getPendingApprovals(
            @RequestParam(required = false) String processType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ApprovalProcess> list = approvalService.getPendingApprovals(userId, processType);
        return Result.success(list);
    }

    /**
     * 审批操作
     */
    @PostMapping("/process")
    public Result<Void> processApproval(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long approvalId = Long.valueOf(params.get("approvalId").toString());
        String result = params.get("result").toString();
        String opinion = params.get("opinion") != null ? params.get("opinion").toString() : "";
        
        // 获取下一级审批人ID（可选）
        Long nextApproverId = null;
        if (params.containsKey("nextApproverId") && params.get("nextApproverId") != null) {
            nextApproverId = Long.valueOf(params.get("nextApproverId").toString());
        }
        
        approvalService.processApproval(approvalId, userId, result, opinion, nextApproverId);
        return Result.success("审批成功", null);
    }

    /**
     * 查询审批历史
     * @param processType 审批类型：LEAVE-请假审批, RETURN-销假审批（可选，不传则返回所有类型）
     */
    @GetMapping("/history")
    public Result<List<ApprovalProcess>> getApprovalHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String processType,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ApprovalProcess> list = approvalService.getApprovalHistory(userId, status, processType);
        return Result.success(list);
    }
}
