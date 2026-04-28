package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.House;
import com.reams.security.JwtAuthenticationFilter;
import com.reams.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

// 房源控制器
@Slf4j
@RestController
@RequestMapping("/api/house")
public class HouseController {

    @Autowired
    private HouseService houseService;

    // 分页查询房源
    @GetMapping("/list")
    public Result<PageResult<House>> getHouseList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Integer houseStatus,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minArea,
            @RequestParam(required = false) BigDecimal maxArea,
            @RequestParam(required = false) String houseType,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String propertyType,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        System.out.println("🔍 [HouseController] 接收到的参数 - floor: " + floor + ", houseType: " + houseType);
        
        Map<String, Object> params = new HashMap<>();
        params.put("title", title);
        params.put("keyword", keyword);
        params.put("province", province);
        params.put("city", city);
        params.put("district", district);
        params.put("houseStatus", houseStatus);
        params.put("auditStatus", auditStatus);
        params.put("agentId", agentId);
        params.put("minPrice", minPrice);
        params.put("maxPrice", maxPrice);
        params.put("minArea", minArea);
        params.put("maxArea", maxArea);
        params.put("houseType", houseType);
        params.put("propertyType", propertyType);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        
        // 将楼层字符串转换为数字标识符，避免 MyBatis OGNL 字符串比较问题
        if (floor != null && !floor.isEmpty()) {
            Integer floorLevel = null;
            if ("低".equals(floor)) {
                floorLevel = 1; // 低楼层
            } else if ("中".equals(floor)) {
                floorLevel = 2; // 中楼层
            } else if ("高".equals(floor)) {
                floorLevel = 3; // 高楼层
            }
            params.put("floorLevel", floorLevel);
            System.out.println("📦 [HouseController] 楼层转换 - floor: " + floor + " -> floorLevel: " + floorLevel);
        }

        return Result.success(houseService.getHousePage(params));
    }

    // 获取已发布房源列表 (公开接口)
    @GetMapping("/published")
    public Result<?> getPublishedHouses() {
        return houseService.getPublishedHouses();
    }

    // 获取热门房源（支持城市筛选）
    @GetMapping("/hot")
    public Result<?> getHotHouses(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String city) {
        return houseService.getHotHouses(limit, city);
    }

    // 获取房源详情
    @GetMapping("/detail/{id}")
    public Result<?> getHouseDetail(@PathVariable Long id) {
        return houseService.getHouseDetail(id);
    }

    // 中介新增房源
    @PostMapping("/add")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> addHouse(Authentication authentication, @RequestBody House house) {
        Long userId = (Long) authentication.getPrincipal();
        house.setAgentId(userId);
        return houseService.addHouse(house);
    }

    // 中介更新房源
    @PutMapping("/update")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> updateHouse(@RequestBody House house) {
        log.info("📥 收到房源更新请求 - ID: {}, Title: {}, Images: {}", 
                house.getId(), house.getTitle(), house.getImages());
        
        try {
            return houseService.updateHouse(house);
        } catch (Exception e) {
            log.error("❌ 更新房源失败 - ID: {}, Error: {}", house.getId(), e.getMessage(), e);
            throw e;
        }
    }

    // 中介删除房源
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> deleteHouse(@PathVariable Long id) {
        return houseService.deleteHouse(id);
    }

    // 管理员审核房源
    @PostMapping("/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> auditHouse(@PathVariable Long id,
                                @RequestParam Integer auditStatus,
                                @RequestParam(required = false) String reason) {
        return houseService.auditHouse(id, auditStatus, reason);
    }

    // 获取待审核房源列表
    @GetMapping("/pending/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> getPendingAuditHouses() {
        return houseService.getPendingAuditHouses();
    }

    // 更新房源状态
    @PostMapping("/status/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    public Result<?> updateHouseStatus(@PathVariable Long id, @RequestParam Integer status) {
        return houseService.updateHouseStatus(id, status);
    }

    // 获取城市列表
    @GetMapping("/cities")
    public Result<?> getCityList() {
        return houseService.getCityList();
    }

    // 获取区级列表
    @GetMapping("/districts")
    public Result<?> getDistrictList(@RequestParam String city) {
        return houseService.getDistrictList(city);
    }
}
