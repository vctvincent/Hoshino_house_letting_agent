package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.SysCustomer;
import com.reams.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public Result<PageResult<SysCustomer>> getCustomerList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("keyword", keyword);
        params.put("status", status);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return Result.success(customerService.getCustomerPage(params));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public Result<List<SysCustomer>> getAllCustomers() {
        return Result.success(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<SysCustomer> getCustomerById(@PathVariable Long id) {
        return Result.success(customerService.getCustomerById(id));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateCustomerStatus(@PathVariable Long id, @RequestParam Integer status) {
        return customerService.updateCustomerStatus(id, status);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> updateCustomerProfile(Authentication authentication, @RequestBody SysCustomer customer) {
        customer.setId((Long) authentication.getPrincipal());
        return customerService.updateCustomerProfile(customer);
    }

    @PutMapping("/password")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> changePassword(
            Authentication authentication,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return customerService.changePassword((Long) authentication.getPrincipal(), oldPassword, newPassword);
    }
}
