package com.student.leave.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 销假申请实体类
 */
@Data
public class LeaveReturnApplication {
    private Long id;
    private Long leaveId;          // 请假申请ID
    private Long studentId;        // 学生ID
    private Date applyTime;        // 申请时间
    private String status;         // 状态：PENDING-待审批, APPROVED-已通过, REJECTED-已驳回
    private String remark;         // 申请备注
    private Long approverId;       // 审批人ID
    private Date approveTime;      // 审批时间
    private String approveOpinion; // 审批意见
    private Date createTime;
    private Date updateTime;
    
    // 关联查询字段
    private String studentName;    // 学生姓名
    private String studentUsername; // 学生学号
    private String leaveType;      // 请假类型
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveStartTime;   // 请假开始时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveEndTime;     // 请假结束时间
    
    private Integer leaveDays;     // 请假天数
    private String leaveReason;    // 请假事由
    private String approverName;   // 审批人姓名
    
    // 审批流程列表
    private List<ApprovalProcess> approvalProcesses; // 审批流程列表
}
