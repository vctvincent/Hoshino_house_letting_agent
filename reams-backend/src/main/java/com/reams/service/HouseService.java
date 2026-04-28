package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.House;

import java.util.Map;

/**
 * 房源服务接口
 */
public interface HouseService {

    /**
     * 分页查询房源
     */
    PageResult<House> getHousePage(Map<String, Object> params);

    /**
     * 获取已发布房源列表
     */
    Result<?> getPublishedHouses();

    /**
     * 获取热门房源（支持城市筛选）
     */
    Result<?> getHotHouses(Integer limit, String city);

    /**
     * 获取房源详情
     */
    Result<?> getHouseDetail(Long id);

    /**
     * 中介新增房源
     */
    Result<?> addHouse(House house);

    /**
     * 中介更新房源
     */
    Result<?> updateHouse(House house);

    /**
     * 中介删除房源
     */
    Result<?> deleteHouse(Long id);

    /**
     * 审核房源
     */
    Result<?> auditHouse(Long houseId, Integer auditStatus, String reason);

    /**
     * 获取待审核房源列表
     */
    Result<?> getPendingAuditHouses();

    /**
     * 更新房源状态
     */
    Result<?> updateHouseStatus(Long id, Integer status);

    /**
     * 获取城市列表
     */
    Result<?> getCityList();

    /**
     * 获取区级列表
     */
    Result<?> getDistrictList(String city);
}
