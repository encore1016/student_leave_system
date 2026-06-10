package com.student.leave.entity;

import lombok.Data;
import java.util.Date;

/**
 * 操作日志实体类
 */
@Data
public class OperationLog {
    private Long id;
    private Long userId;           // 操作用户ID
    private String action;         // 操作动作
    private String module;         // 操作模块
    private String ip;             // IP地址
    private String userAgent;      // 用户代理
    private String params;         // 请求参数
    private String result;         // 操作结果
    private Date createTime;
    
    // 关联查询字段
    private String userName;       // 操作人姓名
}
