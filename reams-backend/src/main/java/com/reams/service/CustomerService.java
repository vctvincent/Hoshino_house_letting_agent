package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysCustomer;

import java.util.List;
import java.util.Map;

/**
 * 客户服务接口
 */
public interface CustomerService {

    /**
     * 分页查询客户
     */
    PageResult<SysCustomer> getCustomerPage(Map<String, Object> params);

    /**
     * 获取所有客户
     */
    List<SysCustomer> getAllCustomers();

    /**
     * 根据 ID 查询客户
     */
    SysCustomer getCustomerById(Long id);

    /**
     * 新增客户
     */
    Result<?> addCustomer(SysCustomer customer);

    /**
     * 更新客户
     */
    Result<?> updateCustomer(SysCustomer customer);

    /**
     * 删除客户
     */
    Result<?> deleteCustomer(Long id);

    /**
     * 更新客户状态
     */
    Result<?> updateCustomerStatus(Long id, Integer status);
    
    /**
     * 更新客户个人资料
     */
    Result<?> updateCustomerProfile(SysCustomer customer);
    
    /**
     * 修改密码
     */
    Result<?> changePassword(Long id, String oldPassword, String newPassword);
}
