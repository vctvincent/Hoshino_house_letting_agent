package com.reams.service.impl;

import com.reams.common.result.Result;
import com.reams.entity.Favorite;
import com.reams.entity.House;
import com.reams.mapper.FavoriteMapper;
import com.reams.mapper.HouseMapper;
import com.reams.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 收藏服务实现类
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Override
    public Result<List<Favorite>> getFavoritesByCustomerId(Long customerId) {
        if (customerId == null) {
            return Result.error("客户 ID 不能为空");
        }
        List<Favorite> favorites = favoriteMapper.selectByCustomerId(customerId);
        return Result.success(favorites);
    }

    @Override
    @Transactional
    public Result<?> addFavorite(Long customerId, Long houseId) {
        if (customerId == null || houseId == null) {
            return Result.error("参数错误");
        }

        // 检查房源是否存在
        House house = houseMapper.selectById(houseId);
        if (house == null) {
            return Result.error("房源不存在");
        }

        // 检查是否已收藏
        int count = favoriteMapper.checkFavorite(customerId, houseId);
        if (count > 0) {
            return Result.error("已收藏过该房源");
        }

        // 创建收藏记录
        Favorite favorite = new Favorite();
        favorite.setCustomerId(customerId);
        favorite.setHouseId(houseId);
        favorite.setCreateTime(new Date());

        favoriteMapper.insert(favorite);
        return Result.success("收藏成功");
    }

    @Override
    @Transactional
    public Result<?> removeFavorite(Long id) {
        if (id == null) {
            return Result.error("收藏 ID 不能为空");
        }
        favoriteMapper.deleteById(id);
        return Result.success("取消收藏成功");
    }

    @Override
    @Transactional
    public Result<?> removeFavoriteByHouse(Long customerId, Long houseId) {
        if (customerId == null || houseId == null) {
            return Result.error("参数错误");
        }
        favoriteMapper.delete(customerId, houseId);
        return Result.success("取消收藏成功");
    }

    @Override
    public Result<Boolean> checkFavorite(Long customerId, Long houseId) {
        if (customerId == null || houseId == null) {
            return Result.error("参数错误");
        }
        int count = favoriteMapper.checkFavorite(customerId, houseId);
        return Result.success(count > 0);
    }

    @Override
    public Result<Long> getCount(Long customerId) {
        if (customerId == null) {
            return Result.error("客户 ID 不能为空");
        }
        long count = favoriteMapper.count(customerId);
        return Result.success(count);
    }
}
