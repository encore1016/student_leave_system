package com.student.leave.entity;

import lombok.Data;
import java.util.Date;

/**
 * 用户实体类
 */
@Data
public class User {
    private Long id;
    private String username;  // 学号/工号
    private String password;
    private String name;      // 姓名
    private String phone;     // 手机号
    private String email;     // 邮箱
    private String role;      // 角色：STUDENT, COUNSELOR, SECRETARY, ADMIN
    private Integer status;   // 状态：0-禁用，1-启用
    private String avatar;    // 头像
    private String classId;   // 班级ID（学生）
    private String collegeId; // 学院ID
    private Date createTime;
    private Date updateTime;
}
