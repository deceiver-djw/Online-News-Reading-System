package com.qst.onlinenewsbackend.controller;

import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.User;
import com.qst.onlinenewsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 模块6：用户登录与个性化推荐模块
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "用户认证", description = "登录、注册、登出相关接口")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册账号")
    public Result<?> register(@RequestBody User user) {
        // 设置默认头像
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            user.setAvatar("C:\\images\\avator.png");
        }
        userService.save(user);
        return Result.success("注册成功", user);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户名密码登录")
    public Result<?> login(@RequestParam String username, @RequestParam String password) {
        User user = userService.lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)
                .one();
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userService.updateById(user);
        return Result.success("登录成功", user);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "退出登录状态")
    public Result<?> logout() {
        return Result.success("登出成功");
    }

    @GetMapping("/user/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    public Result<?> getUserInfo(@PathVariable Integer id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/user/{id}")
    @Operation(summary = "更新用户信息", description = "更新用户个人资料")
    public Result<?> updateUserInfo(@PathVariable Integer id, @RequestBody User user) {
        user.setId(id);
        userService.updateById(user);
        return Result.success("更新成功", user);
    }
}
