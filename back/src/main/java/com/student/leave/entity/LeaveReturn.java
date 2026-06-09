package com.student.leave.entity;

import lombok.Data;
import java.util.Date;

/**
 * 销假记录实体类
 */
@Data
public class LeaveReturn {
    private Long id;
    private Long leaveId;          // 请假申请ID
    private Date returnTime;       // 销假时间
    private Integer isOverdue;     // 是否逾期：0-否，1-是
    private String remark;         // 备注说明
    private Date createTime;
}
