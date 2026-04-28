package com.reams.service;

import com.reams.common.result.Result;
import com.reams.entity.SysAdmin;
import com.reams.entity.SysAgent;
import com.reams.entity.SysCustomer;

import java.util.Map;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 管理员登录
     */
    Result<?> loginAdmin(String name, String password, String captchaKey, String captchaCode);

    /**
     * 中介登录
     */
    Result<?> loginAgent(String phone, String password, String captchaKey, String captchaCode);

    /**
     * 客户登录
     */
    Result<?> loginCustomer(String phone, String password, String captchaKey, String captchaCode);

    Result<Map<String, String>> generateCaptcha();

    /**
     * 注册客户
     */
    Result<?> registerCustomer(SysCustomer customer);

    /**
     * 注册中介
     */
    Result<?> registerAgent(SysAgent agent);

    /**
     * 根据用户 ID 和角色获取用户信息
     */
    Object getUserInfo(Long userId, String role);

    /**
     * 修改密码
     */
    Result<?> changePassword(Long userId, String role, String oldPassword, String newPassword);
    
    /**
     * 根据 ID 获取管理员
     */
    com.reams.entity.SysAdmin getAdminById(Long id);
    
    /**
     * 更新管理员个人资料
     */
    Result<?> updateAdminProfile(Long userId, String name, String phone, String email);
}
