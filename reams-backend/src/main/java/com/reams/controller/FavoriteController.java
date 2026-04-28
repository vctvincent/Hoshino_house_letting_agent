package com.reams.controller;

import com.reams.common.result.Result;
import com.reams.entity.Favorite;
import com.reams.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 查询客户的收藏列表
     */
    @GetMapping("/list")
    @Secured("ROLE_CUSTOMER")
    public Result<List<Favorite>> getFavorites(@RequestParam Long customerId) {
        return favoriteService.getFavoritesByCustomerId(customerId);
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    @Secured("ROLE_CUSTOMER")
    public Result<?> addFavorite(@RequestParam Long customerId, @RequestParam Long houseId) {
        return favoriteService.addFavorite(customerId, houseId);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/remove/{id}")
    @Secured("ROLE_CUSTOMER")
    public Result<?> removeFavorite(@PathVariable Long id) {
        return favoriteService.removeFavorite(id);
    }

    /**
     * 取消收藏 (根据房源 ID)
     */
    @DeleteMapping("/remove")
    @Secured("ROLE_CUSTOMER")
    public Result<?> removeFavoriteByHouse(@RequestParam Long customerId, @RequestParam Long houseId) {
        return favoriteService.removeFavoriteByHouse(customerId, houseId);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Long customerId, @RequestParam Long houseId) {
        return favoriteService.checkFavorite(customerId, houseId);
    }

    /**
     * 统计收藏总数
     */
    @GetMapping("/count")
    @Secured("ROLE_CUSTOMER")
    public Result<Long> getCount(@RequestParam Long customerId) {
        return favoriteService.getCount(customerId);
    }
}
