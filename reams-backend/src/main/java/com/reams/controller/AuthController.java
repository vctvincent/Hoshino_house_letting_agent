package com.reams.controller;

import com.reams.common.result.Result;
import com.reams.entity.SysCustomer;
import com.reams.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// 认证控制器
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // 管理员登录
    @PostMapping("/login/admin")
    public Result<?> loginAdmin(@RequestBody Map<String, String> params) {
        String name = params.get("name");
        if (name == null || name.trim().isEmpty()) {
            name = params.get("username");
        }
        String password = params.get("password");
        return authService.loginAdmin(name, password, params.get("captchaKey"), params.get("captchaCode"));
    }

    // 中介登录
    @PostMapping("/login/agent")
    public Result<?> loginAgent(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            phone = params.get("username");
        }
        String password = params.get("password");
        return authService.loginAgent(phone, password, params.get("captchaKey"), params.get("captchaCode"));
    }

    // 客户登录
    @PostMapping("/login/customer")
    public Result<?> loginCustomer(@RequestBody Map<String, String> params) {
        String phone = params.get("phone");
        if (phone == null || phone.trim().isEmpty()) {
            phone = params.get("username");
        }
        String password = params.get("password");
        return authService.loginCustomer(phone, password, params.get("captchaKey"), params.get("captchaCode"));
    }

    @GetMapping("/captcha")
    public Result<?> captcha() {
        return authService.generateCaptcha();
    }

    // 客户注册
    @PostMapping("/register/customer")
    public Result<?> registerCustomer(@RequestBody SysCustomer customer) {
        return authService.registerCustomer(customer);
    }

    // 中介注册
    @PostMapping("/register/agent")
    public Result<?> registerAgent(@RequestBody com.reams.entity.SysAgent agent) {
        return authService.registerAgent(agent);
    }

    // 获取用户信息
    @GetMapping("/info")
    public Result<?> getUserInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return Result.success(authService.getUserInfo(userId, role));
    }

    // 修改密码
    @PostMapping("/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> changePassword(Authentication authentication,
                                    @RequestBody Map<String, String> params) {
        Long userId = (Long) authentication.getPrincipal();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        return authService.changePassword(userId, role, oldPassword, newPassword);
    }
    
    // 更新管理员个人资料
    @PutMapping("/admin/profile")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateAdminProfile(Authentication authentication,
                                        @RequestBody Map<String, Object> params) {
        Long userId = (Long) authentication.getPrincipal();
        String name = (String) params.get("name");
        if (name == null || name.trim().isEmpty()) {
            name = (String) params.get("realName");
        }
        String phone = (String) params.get("phone");
        String email = (String) params.get("email");
        return authService.updateAdminProfile(userId, name, phone, email);
    }
    
    // 获取管理员详情
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getAdminById(@PathVariable Long id) {
        return Result.success(authService.getAdminById(id));
    }

    // 登出
    @PostMapping("/logout")
    public Result<?> logout() {
        return Result.success("登出成功");
    }
}
