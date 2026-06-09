package com.student.leave.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 审批流程实体类
 */
@Data
public class ApprovalProcess {
    private Long id;
    private Long leaveId;          // 请假申请ID
    private String processType;    // 审批类型：LEAVE-请假审批, RETURN-销假审批
    private Long approverId;       // 审批人ID
    private Integer level;         // 审批级别：1-辅导员, 2-副书记
    private String status;         // 状态：PENDING-待审批, APPROVED-已通过, REJECTED-已驳回
    private String opinion;        // 审批意见
    private Date approveTime;      // 审批时间
    private Date createTime;
    
    // 关联查询字段
    private String approverName;   // 审批人姓名
    
    // 关联的请假申请信息
    private String leaveType;      // 请假类型
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveStartTime;   // 请假开始时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date leaveEndTime;     // 请假结束时间
    
    private Integer leaveDays;     // 请假天数
    private String leaveReason;    // 请假事由
    private String applicantName;  // 申请人姓名
    private String applicantUsername; // 申请人学号
}
