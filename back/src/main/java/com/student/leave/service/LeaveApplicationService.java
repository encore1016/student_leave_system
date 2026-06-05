package com.student.leave.service;

import com.student.leave.entity.LeaveApplication;

import java.util.Date;
import java.util.List;

/**
 * 请假申请服务接口
 */
public interface LeaveApplicationService {
    
    /**
     * 创建请假申请
     */
    Long createLeaveApplication(LeaveApplication leaveApplication);
    
    /**
     * 查询请假详情
     */
    LeaveApplication getLeaveApplicationById(Long id);
    
    /**
     * 查询用户的请假列表
     */
    List<LeaveApplication> getLeaveApplicationsByUserId(Long userId, String status, String type, Date startTime, Date endTime);
    
    /**
     * 根据角色查询请假列表
     */
    List<LeaveApplication> getLeaveApplicationsByRole(Long userId, String role, String status, String type, Date startTime, Date endTime);
    
    /**
     * 撤销请假申请
     */
    void cancelLeaveApplication(Long leaveId, Long userId);
    
    /**
     * 销假
     */
    void returnFromLeave(Long leaveId, Long userId, String role, String remark);
    
    /**
     * 查询即将到期的请假记录
     */
    List<LeaveApplication> getExpiringSoonLeaves(Integer hours);
    
    /**
     * 查询已逾期未销假的记录
     */
    List<LeaveApplication> getOverdueLeaves();
}
