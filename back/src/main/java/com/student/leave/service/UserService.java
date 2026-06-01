package com.student.leave.service;

import com.student.leave.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    void register(User user);
    
    /**
     * 用户登录
     */
    String login(String username, String password);
    
    /**
     * 根据ID查询用户
     */
    User getUserById(Long id);
    
    /**
     * 根据用户名查询用户
     */
    User getUserByUsername(String username);
    
    /**
     * 更新用户信息
     */
    void updateUser(User user);
    
    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 更新用户状态
     */
    void updateStatus(Long userId, Integer status);
    
    /**
     * 获取审批人列表（仅副书记）
     */
    List<User> getApprovers();
}
