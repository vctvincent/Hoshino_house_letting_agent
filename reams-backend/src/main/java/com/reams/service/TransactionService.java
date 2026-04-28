package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Transaction;

import java.util.Map;

/**
 * 交易服务接口
 */
public interface TransactionService {

    /**
     * 分页查询交易
     */
    PageResult<Transaction> getTransactionPage(Map<String, Object> params);

    /**
     * 获取交易详情
     */
    Result<?> getTransactionDetail(Long id);

    /**
     * 创建交易
     */
    Result<?> createTransaction(Transaction transaction);

    /**
     * 更新交易
     */
    Result<?> updateTransaction(Transaction transaction);

    /**
     * 更新交易状态
     * @param id 交易 ID
     * @param status 新状态
     * @param remark 变更备注
     * @return 操作结果
     */
    Result<?> updateTransactionStatus(Long id, Integer status, String remark);

    /**
     * 协商价格（仅在谈判中状态可用）
     * @param id 交易 ID
     * @param newPrice 新价格
     * @param remark 协商备注
     * @return 操作结果
     */
    Result<?> negotiatePrice(Long id, java.math.BigDecimal newPrice, String remark);

    /**
     * 获取客户的交易列表
     */
    Result<?> getCustomerTransactions(Long customerId);

    /**
     * 获取中介的交易列表
     */
    Result<?> getAgentTransactions(Long agentId);

    /**
     * 统计中介成交额
     */
    Result<?> getAgentTotalSales(Long agentId);

    /**
     * 删除交易记录
     * @param id 交易 ID
     * @return 操作结果
     */
    Result<?> deleteTransaction(Long id);
}
