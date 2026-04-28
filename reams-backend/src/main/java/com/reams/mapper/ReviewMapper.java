package com.reams.mapper;

import com.reams.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 评价Mapper接口
 */
@Mapper
public interface ReviewMapper {

    /**
     * 分页查询评价
     */
    List<Review> selectPage(Map<String, Object> params);

    /**
     * 根据ID查询评价
     */
    Review selectById(@Param("id") Long id);

    /**
     * 根据交易ID查询评价
     */
    Review selectByTransactionId(@Param("transactionId") Long transactionId);

    /**
     * 根据房源ID查询评价
     */
    List<Review> selectByHouseId(@Param("houseId") Long houseId);

    /**
     * 根据中介ID查询评价
     */
    List<Review> selectByAgentId(@Param("agentId") Long agentId);

    /**
     * 根据客户ID查询评价
     */
    List<Review> selectByCustomerId(@Param("customerId") Long customerId);

    /**
     * 新增评价
     */
    int insert(Review review);

    /**
     * 更新评价
     */
    int update(Review review);

    /**
     * 删除评价
     */
    int deleteById(@Param("id") Long id);

    /**
     * 统计评价总数
     */
    long count(Map<String, Object> params);

    /**
     * 统计平均评分
     */
    Double selectAvgRatingByAgentId(@Param("agentId") Long agentId);

    /**
     * 查询最新评价
     */
    List<Review> selectLatest(@Param("limit") Integer limit);
}
