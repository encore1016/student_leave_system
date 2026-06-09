package com.student.leave.service;

import com.student.leave.entity.ApprovalProcess;

import java.util.List;

/**
 * 审批服务接口
 */
public interface ApprovalService {
    
    /**
     * 查询待审批列表
     * @param approverId 审批人ID
     * @param processType 审批类型：LEAVE-请假审批, RETURN-销假审批（可选）
     */
    List<ApprovalProcess> getPendingApprovals(Long approverId, String processType);
    
    /**
     * 审批操作
     */
    void processApproval(Long approvalId, Long approverId, String result, String opinion, Long nextApproverId);
    
    /**
     * 查询审批历史
     * @param approverId 审批人ID
     * @param status 状态（可选）
     * @param processType 审批类型：LEAVE-请假审批, RETURN-销假审批（可选）
     */
    List<ApprovalProcess> getApprovalHistory(Long approverId, String status, String processType);
    
    /**
     * 根据请假ID查询审批流程
     */
    List<ApprovalProcess> getApprovalsByLeaveId(Long leaveId);
}
