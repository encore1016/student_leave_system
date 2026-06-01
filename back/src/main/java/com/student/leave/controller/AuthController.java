package com.student.leave.controller;

import com.student.leave.common.Result;
import com.student.leave.dto.LoginRequest;
import com.student.leave.dto.LoginResponse;
import com.student.leave.entity.User;
import com.student.leave.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        userService.register(user);
        return Result.success("注册成功", null);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getUsername(), request.getPassword());
        User user = userService.getUserByUsername(request.getUsername());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setRole(user.getRole());
        
        return Result.success(response);
    }
}
