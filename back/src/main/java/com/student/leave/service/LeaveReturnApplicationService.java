package com.student.leave.service;

import com.student.leave.entity.LeaveReturnApplication;
import com.student.leave.vo.LeaveReturnRecordVO;

import java.util.List;

/**
 * 销假申请服务接口
 */
public interface LeaveReturnApplicationService {
    
    /**
     * 学生发起销假申请
     */
    Long applyReturn(Long leaveId, Long studentId, String remark);
    
    /**
     * 审批销假申请
     */
    void approveReturnApplication(Long applicationId, Long approverId, Boolean approved, String opinion);
    
    /**
     * 查询学生的销假申请列表
     */
    List<LeaveReturnApplication> getApplicationsByStudentId(Long studentId, String status);
    
    /**
     * 查询待审批的销假申请列表（上级视角）
     */
    List<LeaveReturnApplication> getPendingApplications(Long approverId);
    
    /**
     * 查询已审批的销假申请列表（上级视角）
     */
    List<LeaveReturnApplication> getApprovedApplications(String status);
    
    /**
     * 根据ID查询销假申请详情
     */
    LeaveReturnApplication getApplicationById(Long id);
    
    /**
     * 查询学生提交过销假申请的请假记录列表（去重）
     */
    List<LeaveReturnRecordVO> getMyReturnRecords(Long studentId);
}
