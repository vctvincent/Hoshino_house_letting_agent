package com.reams.service.impl;

import com.reams.common.result.Result;
import com.reams.util.CaptchaUtil;
import com.reams.util.JwtUtil;
import com.reams.entity.SysAdmin;
import com.reams.entity.SysAgent;
import com.reams.entity.SysCustomer;
import com.reams.mapper.SysAdminMapper;
import com.reams.mapper.SysAgentMapper;
import com.reams.mapper.SysCustomerMapper;
import com.reams.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String CAPTCHA_PREFIX = "auth:captcha:";

    @Autowired
    private SysAdminMapper adminMapper;

    @Autowired
    private SysAgentMapper agentMapper;

    @Autowired
    private SysCustomerMapper customerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Result<?> loginAdmin(String name, String password, String captchaKey, String captchaCode) {
        Result<?> captchaResult = validateCaptcha(captchaKey, captchaCode);
        if (captchaResult != null) {
            return captchaResult;
        }

        SysAdmin admin = adminMapper.selectByName(name);
        if (admin == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(admin.getId(), admin.getName(), "ROLE_ADMIN");
        redisTemplate.opsForValue().set("admin:token:" + admin.getId(), token, 7, TimeUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", admin.getId());
        data.put("username", admin.getName());
        data.put("name", admin.getName());
        data.put("realName", admin.getName());
        data.put("phone", admin.getPhone());
        data.put("email", admin.getEmail());
        data.put("avatar", admin.getAvatar());
        data.put("role", "ROLE_ADMIN");
        return Result.success("登录成功", data);
    }

    @Override
    public Result<?> loginAgent(String phone, String password, String captchaKey, String captchaCode) {
        Result<?> captchaResult = validateCaptcha(captchaKey, captchaCode);
        if (captchaResult != null) {
            return captchaResult;
        }

        SysAgent agent = agentMapper.selectByPhone(phone);
        if (agent == null) {
            return Result.error("用户不存在");
        }
        if (agent.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        if (agent.getStatus() == 2) {
            return Result.error("账号待审核中");
        }
        if (!passwordEncoder.matches(password, agent.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(agent.getId(), agent.getPhone(), "ROLE_AGENT");
        redisTemplate.opsForValue().set("agent:token:" + agent.getId(), token, 7, TimeUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", agent.getId());
        data.put("name", agent.getName());
        data.put("phone", agent.getPhone());
        data.put("email", agent.getEmail());
        data.put("avatar", agent.getAvatar());
        data.put("role", "ROLE_AGENT");
        return Result.success("登录成功", data);
    }

    @Override
    public Result<?> loginCustomer(String phone, String password, String captchaKey, String captchaCode) {
        Result<?> captchaResult = validateCaptcha(captchaKey, captchaCode);
        if (captchaResult != null) {
            return captchaResult;
        }

        SysCustomer customer = customerMapper.selectByPhone(phone);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        if (customer.getStatus() == 0) {
            return Result.error("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, customer.getPassword())) {
            return Result.error("密码错误");
        }

        String token = jwtUtil.generateToken(customer.getId(), customer.getPhone(), "ROLE_CUSTOMER");
        redisTemplate.opsForValue().set("customer:token:" + customer.getId(), token, 7, TimeUnit.DAYS);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", customer.getId());
        data.put("nickname", customer.getNickname());
        data.put("phone", customer.getPhone());
        data.put("email", customer.getEmail());
        data.put("avatar", customer.getAvatar());
        data.put("role", "ROLE_CUSTOMER");
        return Result.success("登录成功", data);
    }

    @Override
    public Result<Map<String, String>> generateCaptcha() {
        String captchaKey = UUID.randomUUID().toString().replace("-", "");
        String captchaCode = CaptchaUtil.randomCode(4);
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, captchaCode.toLowerCase(), 5, TimeUnit.MINUTES);

        Map<String, String> data = new HashMap<>();
        data.put("captchaKey", captchaKey);
        data.put("captchaImage", CaptchaUtil.generateBase64Image(captchaCode));
        return Result.success(data);
    }

    @Override
    public Result<?> registerCustomer(SysCustomer customer) {
        if (customerMapper.selectByPhone(customer.getPhone()) != null) {
            return Result.error("手机号已注册");
        }

        if (customer.getNickname() == null || customer.getNickname().trim().isEmpty()) {
            customer.setNickname(buildDefaultCustomerNickname(customer.getPhone()));
        }

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setStatus(1);
        customerMapper.insert(customer);
        return Result.success("注册成功");
    }

    @Override
    public Result<?> registerAgent(SysAgent agent) {
        if (agentMapper.selectByPhone(agent.getPhone()) != null) {
            return Result.error("手机号已注册");
        }

        if (agent.getName() == null || agent.getName().trim().isEmpty()) {
            agent.setName(buildDefaultAgentName(agent.getPhone()));
        }

        agent.setPassword(passwordEncoder.encode(agent.getPassword()));
        agent.setStatus(2);
        agentMapper.insert(agent);
        return Result.success("注册成功，请等待管理员审核");
    }

    @Override
    public Object getUserInfo(Long userId, String role) {
        Map<String, Object> data = new HashMap<>();
        if ("ROLE_ADMIN".equals(role)) {
            SysAdmin admin = adminMapper.selectById(userId);
            data.put("userId", admin.getId());
            data.put("username", admin.getName());
            data.put("name", admin.getName());
            data.put("realName", admin.getName());
            data.put("email", admin.getEmail());
            data.put("phone", admin.getPhone());
            data.put("avatar", admin.getAvatar());
            data.put("role", role);
        } else if ("ROLE_AGENT".equals(role)) {
            SysAgent agent = agentMapper.selectById(userId);
            data.put("userId", agent.getId());
            data.put("name", agent.getName());
            data.put("email", agent.getEmail());
            data.put("phone", agent.getPhone());
            data.put("avatar", agent.getAvatar());
            data.put("rating", agent.getRating());
            data.put("dealCount", agent.getDealCount());
            data.put("introduction", agent.getIntroduction());
            data.put("role", role);
        } else if ("ROLE_CUSTOMER".equals(role)) {
            SysCustomer customer = customerMapper.selectById(userId);
            data.put("userId", customer.getId());
            data.put("nickname", customer.getNickname());
            data.put("email", customer.getEmail());
            data.put("phone", customer.getPhone());
            data.put("avatar", customer.getAvatar());
            data.put("role", role);
        }
        return data;
    }

    @Override
    public Result<?> changePassword(Long userId, String role, String oldPassword, String newPassword) {
        if ("ROLE_ADMIN".equals(role)) {
            SysAdmin admin = adminMapper.selectById(userId);
            if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
                return Result.error("原密码错误");
            }
            admin.setPassword(passwordEncoder.encode(newPassword));
            adminMapper.update(admin);
        } else if ("ROLE_AGENT".equals(role)) {
            SysAgent agent = agentMapper.selectById(userId);
            if (!passwordEncoder.matches(oldPassword, agent.getPassword())) {
                return Result.error("原密码错误");
            }
            agent.setPassword(passwordEncoder.encode(newPassword));
            agentMapper.update(agent);
        } else if ("ROLE_CUSTOMER".equals(role)) {
            SysCustomer customer = customerMapper.selectById(userId);
            if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
                return Result.error("原密码错误");
            }
            customer.setPassword(passwordEncoder.encode(newPassword));
            customerMapper.update(customer);
        }
        return Result.success("密码修改成功");
    }

    @Override
    @Transactional
    public Result<?> updateAdminProfile(Long userId, String name, String phone, String email) {
        SysAdmin admin = adminMapper.selectById(userId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }

        if (phone != null && !phone.equals(admin.getPhone())) {
            SysAdmin phoneExists = adminMapper.selectByPhone(phone);
            if (phoneExists != null && !phoneExists.getId().equals(userId)) {
                return Result.error("手机号已被使用");
            }
        }

        SysAdmin updateAdmin = new SysAdmin();
        updateAdmin.setId(userId);
        updateAdmin.setName(name);
        updateAdmin.setPhone(phone);
        updateAdmin.setEmail(email);
        adminMapper.update(updateAdmin);
        return Result.success("个人资料更新成功");
    }

    @Override
    public SysAdmin getAdminById(Long id) {
        return adminMapper.selectById(id);
    }

    private Result<?> validateCaptcha(String captchaKey, String captchaCode) {
        if (captchaKey == null || captchaKey.trim().isEmpty() || captchaCode == null || captchaCode.trim().isEmpty()) {
            return Result.error("请输入验证码");
        }

        String redisKey = CAPTCHA_PREFIX + captchaKey.trim();
        Object cached = redisTemplate.opsForValue().get(redisKey);
        if (cached == null) {
            return Result.error("验证码已过期，请刷新后重试");
        }

        redisTemplate.delete(redisKey);
        String expected = String.valueOf(cached).trim().toLowerCase();
        String provided = captchaCode.trim().toLowerCase();
        if (!expected.equals(provided)) {
            return Result.error("验证码错误");
        }
        return null;
    }

    private String buildDefaultCustomerNickname(String phone) {
        if (phone != null && phone.length() >= 4) {
            return "用户" + phone.substring(phone.length() - 4);
        }
        return "用户";
    }

    private String buildDefaultAgentName(String phone) {
        if (phone != null && phone.length() >= 4) {
            return "中介" + phone.substring(phone.length() - 4);
        }
        return "中介";
    }
}
