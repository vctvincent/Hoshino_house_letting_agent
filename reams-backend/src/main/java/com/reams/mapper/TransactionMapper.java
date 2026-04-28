package com.reams.mapper;

import com.reams.entity.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface TransactionMapper {

    List<Transaction> selectPage(Map<String, Object> params);

    long count(Map<String, Object> params);

    Transaction selectById(@Param("id") Long id);

    List<Transaction> selectByCustomerId(@Param("customerId") Long customerId);

    List<Transaction> selectByAgentId(@Param("agentId") Long agentId);

    Transaction selectByTransactionNo(@Param("transactionNo") String transactionNo);

    int insert(Transaction transaction);

    int update(Transaction transaction);

    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("statusHistory") String statusHistory,
                     @Param("remark") String remark);

    int updatePriceAndHistory(@Param("id") Long id,
                              @Param("newPrice") BigDecimal newPrice,
                              @Param("statusHistory") String statusHistory);

    BigDecimal sumFinalPrice(Map<String, Object> params);

    Map<String, Object> selectTransactionOverview();

    List<Map<String, Object>> selectMonthlySales();

    List<Map<String, Object>> selectDistrictSalesRanking(@Param("limit") Integer limit,
                                                         @Param("offsetMonths") Integer offsetMonths);

    List<Map<String, Object>> selectTopAgentSales(@Param("limit") Integer limit,
                                                  @Param("offsetMonths") Integer offsetMonths);

    List<Map<String, Object>> selectStatusDistribution();

    List<Map<String, Object>> selectMonthlySalesByAgentId(@Param("agentId") Long agentId);

    List<Map<String, Object>> selectDistrictSalesRankingByAgentId(@Param("agentId") Long agentId,
                                                                  @Param("limit") Integer limit,
                                                                  @Param("offsetMonths") Integer offsetMonths);

    List<Map<String, Object>> selectCompanySalesRanking(@Param("limit") Integer limit,
                                                        @Param("offsetMonths") Integer offsetMonths);

    int deleteById(@Param("id") Long id);
}
