package com.reams.service;

import com.reams.common.result.Result;
import com.reams.entity.Favorite;

import java.util.List;

/**
 * 收藏服务接口
 */
public interface FavoriteService {

    /**
     * 查询客户的收藏列表
     */
    Result<List<Favorite>> getFavoritesByCustomerId(Long customerId);

    /**
     * 添加收藏
     */
    Result<?> addFavorite(Long customerId, Long houseId);

    /**
     * 取消收藏
     */
    Result<?> removeFavorite(Long id);

    /**
     * 取消收藏 (根据客户 ID 和房源 ID)
     */
    Result<?> removeFavoriteByHouse(Long customerId, Long houseId);

    /**
     * 检查是否已收藏
     */
    Result<Boolean> checkFavorite(Long customerId, Long houseId);

    /**
     * 统计收藏总数
     */
    Result<Long> getCount(Long customerId);
}
