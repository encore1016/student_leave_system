package com.student.leave.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;
import java.util.List;

/**
 * 请假申请实体类
 */
@Data
public class LeaveApplication {
    private Long id;
    private Long userId;           // 申请人ID
    private String type;           // 请假类型：SICK-病假, PERSONAL-事假, OTHER-其他
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;        // 开始时间
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;          // 结束时间
    private Integer days;          // 请假天数
    private String reason;         // 请假事由
    private String destination;    // 目的地
    private Integer leaveSchool;   // 是否离校：0-否，1-是
    private String attachments;    // 附件（JSON格式存储多个文件路径）
    private String status;         // 状态：PENDING-待审批, APPROVED-已通过, REJECTED-已驳回, CANCELLED-已撤销
    private Date createTime;
    private Date updateTime;
    
    // 关联查询字段
    private String userName;       // 申请人姓名
    private String userUsername;   // 申请人学号
    
    // 关联的审批流程信息
    private List<ApprovalProcess> approvalProcesses; // 请假审批流程列表
    
    // 关联的销假信息
    private LeaveReturn leaveReturn; // 销假记录
    private List<LeaveReturnApplication> returnApplications; // 销假申请列表
}
