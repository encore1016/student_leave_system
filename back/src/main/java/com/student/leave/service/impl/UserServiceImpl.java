package com.student.leave.service.impl;

import com.student.leave.common.BusinessException;
import com.student.leave.entity.User;
import com.student.leave.mapper.UserMapper;
import com.student.leave.service.UserService;
import com.student.leave.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(User user) {
        // 验证学号/工号格式
        if (user.getUsername() == null || user.getUsername().length() != 10) {
            throw new BusinessException("学号/工号格式错误，必须为10位");
        }

        // 验证手机号格式
        if (user.getPhone() == null || !user.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式错误");
        }

        // 验证密码强度
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new BusinessException("密码长度至少6位");
        }

        // 检查学号/工号是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("该学号/工号已被注册");
        }

        // 检查手机号是否已被注册
        User existPhone = userMapper.selectByPhone(user.getPhone());
        if (existPhone != null) {
            throw new BusinessException("该手机号已被注册");
        }

        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态为启用
        user.setStatus(1);

        // 创建用户记录
        userMapper.insert(user);
    }

    @Override
    public String login(String username, String password) {
        // 查询用户信息
//        User user = userMapper.selectByUsername(username);
//        if (user == null) {
//            throw new BusinessException("用户名或密码错误");
//        }
//
//        // 验证用户状态
//        if (user.getStatus() == 0) {
//            throw new BusinessException("账号已被禁用，请联系管理员");
//        }
//
//        // 验证密码
//        if (!passwordEncoder.matches(password, user.getPassword())) {
//            throw new BusinessException("用户名或密码错误");
//        }
//
//        // 生成JWT Token
//        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
//
//        return token;
        // 查询用户信息
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        // 验证密码 - 添加调试日志
        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        if (!passwordMatch) {
            // 开发环境可打印详细信息用于调试
            System.out.println("[DEBUG] 登录失败 - 用户名: " + username + ", 输入密码: " + password + ", 数据库哈希: " + user.getPassword());
            throw new BusinessException("用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        return token;
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清空密码字段
        user.setPassword(null);
        return user;
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 清空密码字段
        user.setPassword(null);
        return user;
    }

    @Override
    public void updateUser(User user) {
        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证手机号格式
        if (user.getPhone() != null && !user.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new BusinessException("手机号格式错误");
        }

        // 检查手机号是否被其他用户使用
        if (user.getPhone() != null) {
            User phoneUser = userMapper.selectByPhone(user.getPhone());
            if (phoneUser != null && !phoneUser.getId().equals(user.getId())) {
                throw new BusinessException("该手机号已被其他用户使用");
            }
        }

        userMapper.update(user);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 验证新密码强度
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度至少6位");
        }

        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(userId, encodedPassword);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        userMapper.updateStatus(userId, status);
    }

    @Override
    public List<User> getApprovers() {
        // 查询所有副书记角色的用户，且状态为启用
        List<User> approvers = userMapper.selectList("SECRETARY", 1);
        
        // 清空密码字段
        for (User user : approvers) {
            user.setPassword(null);
        }
        
        return approvers;
    }
}
