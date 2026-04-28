package com.reams.service.impl;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysCustomer;
import com.reams.mapper.SysCustomerMapper;
import com.reams.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private SysCustomerMapper customerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResult<SysCustomer> getCustomerPage(Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        String keyword = (String) params.get("keyword");
        Integer status = (Integer) params.get("status");
        Integer offset = (pageNum - 1) * pageSize;

        List<SysCustomer> list;
        long total;
        if ((keyword != null && !keyword.trim().isEmpty()) || status != null) {
            list = customerMapper.selectPageWithSearch(offset, pageSize, keyword, status);
            total = customerMapper.countWithSearch(keyword, status);
        } else {
            list = customerMapper.selectPage(offset, pageSize);
            total = customerMapper.count();
        }

        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    public List<SysCustomer> getAllCustomers() {
        return customerMapper.selectAll();
    }

    @Override
    public SysCustomer getCustomerById(Long id) {
        return customerMapper.selectById(id);
    }

    @Override
    @Transactional
    public Result<?> addCustomer(SysCustomer customer) {
        if (customerMapper.selectByPhone(customer.getPhone()) != null) {
            return Result.error("手机号已注册");
        }

        if (customer.getNickname() == null || customer.getNickname().trim().isEmpty()) {
            customer.setNickname(buildDefaultCustomerNickname(customer.getPhone()));
        }

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        }
        customer.setStatus(1);
        customerMapper.insert(customer);
        return Result.success("客户添加成功");
    }

    @Override
    @Transactional
    public Result<?> updateCustomer(SysCustomer customer) {
        SysCustomer existingCustomer = customerMapper.selectById(customer.getId());
        if (existingCustomer == null) {
            return Result.error("客户不存在");
        }

        if (customer.getPhone() != null && !customer.getPhone().equals(existingCustomer.getPhone())) {
            SysCustomer phoneExists = customerMapper.selectByPhone(customer.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(customer.getId())) {
                return Result.error("手机号已被使用");
            }
        }

        if (customer.getPassword() != null && !customer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        } else {
            customer.setPassword(null);
        }

        customerMapper.update(customer);
        return Result.success("客户信息更新成功");
    }

    @Override
    @Transactional
    public Result<?> updateCustomerProfile(SysCustomer customer) {
        SysCustomer existingCustomer = customerMapper.selectById(customer.getId());
        if (existingCustomer == null) {
            return Result.error("客户不存在");
        }

        if (customer.getPhone() != null && !customer.getPhone().equals(existingCustomer.getPhone())) {
            SysCustomer phoneExists = customerMapper.selectByPhone(customer.getPhone());
            if (phoneExists != null && !phoneExists.getId().equals(customer.getId())) {
                return Result.error("手机号已被使用");
            }
        }

        customerMapper.update(customer);
        return Result.success("个人资料更新成功");
    }

    @Override
    @Transactional
    public Result<?> deleteCustomer(Long id) {
        SysCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        customerMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<?> updateCustomerStatus(Long id, Integer status) {
        SysCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }

        SysCustomer updateCustomer = new SysCustomer();
        updateCustomer.setId(id);
        updateCustomer.setStatus(status);
        customerMapper.update(updateCustomer);
        return Result.success("状态更新成功");
    }

    @Override
    @Transactional
    public Result<?> changePassword(Long id, String oldPassword, String newPassword) {
        SysCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            return Result.error("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, customer.getPassword())) {
            return Result.error("原密码错误");
        }

        SysCustomer updateCustomer = new SysCustomer();
        updateCustomer.setId(id);
        updateCustomer.setPassword(passwordEncoder.encode(newPassword));
        customerMapper.update(updateCustomer);
        return Result.success("密码修改成功");
    }

    private String buildDefaultCustomerNickname(String phone) {
        if (phone != null && phone.length() >= 4) {
            return "用户" + phone.substring(phone.length() - 4);
        }
        return "用户";
    }
}
