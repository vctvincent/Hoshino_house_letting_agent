package com.reams.mapper;

import com.reams.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
//收藏Mapper接口
@Mapper
public interface FavoriteMapper {

    //根据客户ID查询收藏列表
    List<Favorite> selectByCustomerId(@Param("customerId") Long customerId);

    //检查是否已收藏
    int checkFavorite(@Param("customerId") Long customerId, @Param("houseId") Long houseId);

    //根据ID查询收藏
    Favorite selectById(@Param("id") Long id);

    //新增收藏
    int insert(Favorite favorite);

    //删除收藏
    int deleteById(@Param("id") Long id);

    //删除收藏(根据客户ID和房源ID)
    int delete(@Param("customerId") Long customerId, @Param("houseId") Long houseId);

    //统计收藏总数
    long count(@Param("customerId") Long customerId);
}
