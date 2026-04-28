package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Transaction;
import com.reams.entity.Viewing;
import com.reams.mapper.ViewingMapper;
import com.reams.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// 交易控制器
@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private ViewingMapper viewingMapper;

    // 分页查询交易
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<PageResult<Transaction>> getTransactionList(
            Authentication authentication,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long houseId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        
        // 根据角色自动过滤数据
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        Long userId = (Long) authentication.getPrincipal();
        
        if ("ROLE_CUSTOMER".equals(role)) {
            params.put("customerId", userId);
        } else if ("ROLE_AGENT".equals(role)) {
            params.put("agentId", userId);
        }
        // ADMIN 可以查看所有
        
        params.put("houseId", houseId);
        params.put("status", status);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);

        return Result.success(transactionService.getTransactionPage(params));
    }

    // 获取交易详情
    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> getTransactionDetail(@PathVariable Long id) {
        return transactionService.getTransactionDetail(id);
    }

    // 创建交易
    @PostMapping("/create")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> createTransaction(Authentication authentication, @RequestBody Transaction transaction) {
        Long userId = (Long) authentication.getPrincipal();
        transaction.setAgentId(userId);

        if (transaction.getViewingId() == null) {
            return Result.error("请选择已完成带看的房源");
        }
        if (transaction.getCustomerId() == null || transaction.getHouseId() == null) {
            return Result.error("请选择客户和房源");
        }

        Viewing viewing = viewingMapper.selectById(transaction.getViewingId());
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        if (!Integer.valueOf(2).equals(viewing.getStatus())) {
            return Result.error("只能基于已完成的带看创建交易");
        }
        if (!userId.equals(viewing.getAgentId())
                || !transaction.getCustomerId().equals(viewing.getCustomerId())
                || !transaction.getHouseId().equals(viewing.getHouseId())) {
            return Result.error("所选房源不是该客户与当前中介共同完成带看的房源");
        }

        return transactionService.createTransaction(transaction);
    }

    // 更新交易
    @PutMapping("/update")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> updateTransaction(Authentication authentication, @RequestBody Transaction transaction) {
        Long userId = (Long) authentication.getPrincipal();
        transaction.setAgentId(userId);
        return transactionService.updateTransaction(transaction);
    }

    // 更新交易状态
    @PostMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public Result<?> updateTransactionStatus(
            @PathVariable Long id, 
            @RequestParam Integer status,
            @RequestParam(required = false) String remark) {
        
        return transactionService.updateTransactionStatus(id, status, remark);
    }

    // 协商价格（仅在谈判中状态可用）
    @PostMapping("/negotiate/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> negotiatePrice(
            @PathVariable Long id,
            @RequestParam BigDecimal newPrice,
            @RequestParam(required = false) String remark) {
        
        return transactionService.negotiatePrice(id, newPrice, remark);
    }

    // 获取我的交易 (客户)
    @GetMapping("/my/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> getMyTransactionsAsCustomer(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return transactionService.getCustomerTransactions(userId);
    }

    // 获取我的交易 (中介)
    @GetMapping("/my/agent")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> getMyTransactionsAsAgent(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return transactionService.getAgentTransactions(userId);
    }

    // 统计我的成交额 (中介)
    @GetMapping("/my/sales")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> getMyTotalSales(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return transactionService.getAgentTotalSales(userId);
    }

    // 删除交易记录
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteTransaction(@PathVariable Long id) {
        return transactionService.deleteTransaction(id);
    }
}
