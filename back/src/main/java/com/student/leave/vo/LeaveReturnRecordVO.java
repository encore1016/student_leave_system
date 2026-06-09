package com.student.leave.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * 销假记录VO
 */
@Data
public class LeaveReturnRecordVO {
    private Long leaveId;              // 请假ID
    private String leaveType;          // 请假类型
    private Date startTime;            // 请假开始时间
    private Date endTime;              // 请假结束时间
    private String reason;             // 请假原因
    private String destination;        // 目的地
    private Integer days;              // 天数
    
    // 最新销假申请信息
    private Long returnApplicationId;  // 销假申请ID
    private String returnStatus;       // 销假状态：PENDING/APPROVED/REJECTED
    private Date applyTime;            // 销假申请时间
    private String approvalOpinion;    // 审批意见
    private String remark;             // 备注
    private Boolean hasReturned;       // 是否已完成销假
}
