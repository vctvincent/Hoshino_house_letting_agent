package com.reams.service.impl;

import com.reams.common.constant.MessageScenes;
import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.House;
import com.reams.entity.HouseAudit;
import com.reams.entity.Message;
import com.reams.mapper.HouseAuditMapper;
import com.reams.mapper.HouseMapper;
import com.reams.mapper.SysAgentMapper;
import com.reams.service.HouseService;
import com.reams.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 房源状态常量
 * 对应数据库 house_status: 0-未发布 1-已发布 2-已成交 3-已下架
 */
class HouseStatus {
    static final int UNPUBLISHED = 0;  // 未发布
    static final int PUBLISHED   = 1;  // 已发布
    static final int SOLD        = 2;  // 已成交
    static final int DELISTED    = 3;  // 已下架
}

/**
 * 审核状态常量
 */
class AuditStatus {
    static final int PENDING  = 0;  // 待审核
    static final int REVIEWING = 1; // 审核中
    static final int APPROVED = 2;  // 已通过
    static final int REJECTED = 3;  // 已拒绝
}

/**
 * 房源服务实现类
 */
@Slf4j
@Service
public class HouseServiceImpl implements HouseService {

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private HouseAuditMapper houseAuditMapper;

    @Autowired
    private SysAgentMapper sysAgentMapper;

    @Autowired
    private MessageService messageService;

    /**
     * 根据房源信息自动生成固定格式的描述（4-5行）
     */
    private String generateHouseDescription(House house) {
        StringBuilder desc = new StringBuilder();

        // 第1行：基本信息 - 户型、面积、楼层
        if (house.getHouseType() != null || house.getArea() != null || house.getFloor() != null) {
            desc.append("【房源概况】");
            if (house.getHouseType() != null) {
                desc.append(house.getHouseType());
            }
            if (house.getArea() != null) {
                desc.append("，建筑面积").append(house.getArea()).append("㎡");
            }
            if (house.getFloor() != null && house.getTotalFloor() != null) {
                desc.append("，位于").append(house.getFloor()).append("/").append(house.getTotalFloor()).append("层");
            } else if (house.getFloor() != null) {
                desc.append("，").append(house.getFloor());
            }
            desc.append("\n");
        }

        // 第2行：位置和朝向
        if (house.getCommunity() != null || house.getOrientation() != null) {
            desc.append("【位置朝向】");
            if (house.getCommunity() != null) {
                desc.append(house.getCommunity());
            }
            if (house.getAddress() != null) {
                desc.append("，").append(house.getAddress());
            }
            if (house.getOrientation() != null) {
                desc.append("，").append(house.getOrientation());
            }
            desc.append("\n");
        }

        // 第3行：装修和配套
        if (house.getDecoration() != null || house.getElevator() != null || house.getHeating() != null) {
            desc.append("【装修配套】");
            if (house.getDecoration() != null) {
                desc.append(house.getDecoration());
            }
            if (house.getElevator() != null && house.getElevator() == 1) {
                desc.append("，有电梯");
            }
            if (house.getHeating() != null && house.getHeating() == 1) {
                desc.append("，集中供暖");
            }
            if (house.getPropertyFee() != null) {
                desc.append("，物业费").append(house.getPropertyFee()).append("元/㎡/月");
            }
            desc.append("\n");
        }

        // 第4行：建筑年代和房屋类型
        if (house.getBuildingYear() != null || house.getPropertyType() != null) {
            desc.append("【房屋属性】");
            if (house.getPropertyType() != null) {
                desc.append(house.getPropertyType());
            }
            if (house.getBuildingYear() != null) {
                desc.append("，建于").append(house.getBuildingYear()).append("年");
            }
            if (house.getLayout() != null) {
                desc.append("，").append(house.getLayout());
            }
            desc.append("\n");
        }

        // 第5行：价格和标签（如果有）
        if (house.getPrice() != null || house.getTags() != null) {
            desc.append("【房源亮点】");
            if (house.getPrice() != null) {
                desc.append("售价").append(house.getPrice()).append("万");
                if (house.getUnitPrice() != null) {
                    desc.append("，单价").append(house.getUnitPrice()).append("元/㎡");
                }
            }
            if (house.getTags() != null && !house.getTags().isEmpty()) {
                desc.append("，标签：").append(house.getTags());
            }
            desc.append("\n");
        }

        return desc.toString().trim();
    }

    @Override
    public PageResult<House> getHousePage(Map<String, Object> params) {
        Map<String, Object> queryParams = params == null ? new HashMap<>() : new HashMap<>(params);

        Integer pageNum = (Integer) queryParams.get("pageNum");
        Integer pageSize = (Integer) queryParams.get("pageSize");
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        queryParams.put("offset", (pageNum - 1) * pageSize);
        queryParams.put("limit", pageSize);
        applyCityAliases(queryParams);

        List<House> list = houseMapper.selectPage(queryParams);
        normalizeUnitPrice(list);
        long total = houseMapper.count(queryParams);

        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    @Cacheable(value = "houses", key = "'published'")
    public Result<?> getPublishedHouses() {
        List<House> list = houseMapper.selectPublished();
        normalizeUnitPrice(list);
        return Result.success(list);
    }

    @Override
    @Cacheable(value = "houses", key = "'hot_' + #limit + '_' + (#city != null ? #city : 'all')")
    public Result<?> getHotHouses(Integer limit, String city) {
        if (limit == null) limit = 10;
        List<House> list = houseMapper.selectHot(limit, city);
        normalizeUnitPrice(list);

        if (!list.isEmpty()) {
            log.info("🔍 [DEBUG] 第一条房源 ID: {}", list.get(0).getId());
            log.info("🔍 [DEBUG] images 字段值: {}", list.get(0).getImages());
        }

        return Result.success(list);
    }

    @Override
    public Result<?> getHouseDetail(Long id) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            return Result.error("房源不存在");
        }
        normalizeUnitPrice(house);
        houseMapper.incrementViewCount(id);
        return Result.success(house);
    }

    @Override
    @Transactional
    public Result<?> addHouse(House house) {
        // ★ 改动1：新增房源默认状态为"未发布"(0)，等待审核后变为"已发布"(1)
        house.setHouseStatus(HouseStatus.UNPUBLISHED); // 0-未发布
        house.setAuditStatus(AuditStatus.PENDING);      // 0-待审核
        house.setViewCount(0);
        house.setFavoriteCount(0);

        // 重新计算单价（仅在用户未提供时自动计算）
        if (house.getUnitPrice() == null && house.getPrice() != null && house.getArea() != null && house.getArea().compareTo(java.math.BigDecimal.ZERO) > 0) {
            house.setUnitPrice(house.getPrice().multiply(new java.math.BigDecimal(10000))
                    .divide(house.getArea(), 2, java.math.RoundingMode.HALF_UP));
        }

        // 自动生成房源描述（如果用户没有手动填写）
        if (house.getDescription() == null || house.getDescription().trim().isEmpty()) {
            house.setDescription(generateHouseDescription(house));
        }

        houseMapper.insert(house);
        return Result.success("房源添加成功，等待审核");
    }

    @Override
    @Transactional
    public Result<?> updateHouse(House house) {
        House existingHouse = houseMapper.selectById(house.getId());
        if (existingHouse == null) {
            return Result.error("房源不存在");
        }

        // 重新计算单价
        if (house.getPrice() != null && house.getArea() != null && house.getArea().compareTo(java.math.BigDecimal.ZERO) > 0) {
            house.setUnitPrice(house.getPrice().multiply(new java.math.BigDecimal(10000))
                    .divide(house.getArea(), 2, java.math.RoundingMode.HALF_UP));
        }

        // 自动生成房源描述（如果用户没有手动填写或清空了描述）
        if (house.getDescription() == null || house.getDescription().trim().isEmpty()) {
            house.setDescription(generateHouseDescription(house));
        }

        // ★ 改动2：修改后的状态逻辑
        if (existingHouse.getAuditStatus() == AuditStatus.APPROVED || existingHouse.getAuditStatus() == AuditStatus.REJECTED) {
            // 已通过/已拒绝 → 修改后重新审核，状态回到"未发布"
            house.setAuditStatus(AuditStatus.REVIEWING);    // 1-审核中
            house.setHouseStatus(HouseStatus.UNPUBLISHED);  // 0-未发布
        } else if (existingHouse.getAuditStatus() == AuditStatus.PENDING || existingHouse.getAuditStatus() == AuditStatus.REVIEWING) {
            // 待审核/审核中 → 保持原状态
            house.setAuditStatus(existingHouse.getAuditStatus());
            house.setHouseStatus(existingHouse.getHouseStatus());
        }

        house.setId(existingHouse.getId());

        houseMapper.update(house);
        return Result.success("房源更新成功");
    }

    @Override
    @Transactional
    public Result<?> deleteHouse(Long id) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            return Result.error("房源不存在");
        }
        houseMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    @Transactional
    public Result<?> auditHouse(Long houseId, Integer auditStatus, String reason) {
        try {
            log.info("[DEBUG] 开始审核房源 ID: {}, 状态: {}", houseId, auditStatus);

            House house = houseMapper.selectById(houseId);
            if (house == null) {
                return Result.error("房源不存在");
            }
            if (house.getAuditStatus() == AuditStatus.APPROVED) {
                return Result.error("房源已审核通过，无需重复审核");
            }

            // 更新房源审核状态
            log.info("[DEBUG] 更新审核状态...");
            houseMapper.updateAuditStatus(houseId, auditStatus, reason);

            // ★ 改动3：审核通过后更新房源状态为"已发布"(1)
            if (auditStatus == AuditStatus.APPROVED) {
                log.info("[DEBUG] 审核通过，更新房源状态为已发布(1)...");
                houseMapper.updateStatus(houseId, HouseStatus.PUBLISHED); // 1-已发布
                sendAuditNotification(house, true, reason); // 发送通知
            } else if (auditStatus == AuditStatus.REJECTED) {
                sendAuditNotification(house, false, reason); // 发送通知
            }

            // 记录审核信息
            log.info(" [DEBUG] 创建审核记录...");
            HouseAudit audit = new HouseAudit();
            audit.setHouseId(houseId);
            audit.setAuditorId(1L); // TODO: 获取当前登录管理员ID
            audit.setAuditType(1);
            audit.setAuditResult(auditStatus);
            audit.setAuditReason(reason);

            log.info("[DEBUG] 审核记录对象: {}", audit);

            int result = houseAuditMapper.insert(audit);
            log.info("[DEBUG] 插入结果: {}, 生成的 ID: {}", result, audit.getId());

            return Result.success(auditStatus == AuditStatus.APPROVED ? "审核通过" : "审核拒绝");
        } catch (Exception e) {
            log.error("[ERROR] 审核房源失败: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 发送房源审核通知给中介
     * @param house 房源信息
     * @param approved true=通过，false=拒绝
     * @param reason 拒绝原因（通过时可为null）
     */
    private void sendAuditNotification(House house, boolean approved, String reason) {
        try {
            Message message = new Message();
            message.setSenderId(0L);
            message.setSenderType(1);
            message.setReceiverId(house.getAgentId());
            message.setReceiverType(2);
            message.setMessageType(1);
            message.setMessageScene(approved ? MessageScenes.HOUSE_AUDIT_APPROVED : MessageScenes.HOUSE_AUDIT_REJECTED);
            message.setTitle(approved ? "房源审核通过" : "房源审核未通过");

            if (approved) {
                message.setContent("恭喜！您的房源「" + house.getTitle() + "」已通过审核，现已发布。您可以在房源列表中查看和管理。");
            } else {
                message.setContent("很抱歉，您的房源「" + house.getTitle() + "」审核未通过。" +
                        (reason != null && !reason.isEmpty() ? "拒绝原因：" + reason : "请修改后重新提交。"));
            }

            message.setHouseId(house.getId());
            message.setIsRead(0);
            message.setCreateTime(new Date());
            message.setUpdateTime(new Date());

            messageService.sendMessage(message);
            log.info("✅ [消息] 已发送审核{}通知给中介 ID: {}", approved ? "通过" : "拒绝", house.getAgentId());
        } catch (Exception e) {
            log.warn("⚠️ [消息] 发送审核通知失败: {}", e.getMessage());
        }
    }

    @Override
    public Result<?> getPendingAuditHouses() {
        List<House> list = houseMapper.selectPendingAudit();

        long pendingHouseCount = houseMapper.countPendingHouses();
        long pendingAgentCount = sysAgentMapper.countPendingAgents();
        long totalCount = pendingHouseCount + pendingAgentCount;

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", totalCount);
        return Result.success(result);
    }

    @Override
    public Result<?> updateHouseStatus(Long id, Integer status) {
        House house = houseMapper.selectById(id);
        if (house == null) {
            return Result.error("房源不存在");
        }
        houseMapper.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    @Override
    public Result<?> getCityList() {
        return Result.success(normalizeCities(houseMapper.selectCityList()));
    }

    @Override
    public Result<?> getDistrictList(String city) {
        List<String> cityAliases = buildCityAliases(city);
        if (cityAliases.isEmpty()) {
            return Result.success(new ArrayList<>());
        }

        List<String> districts = houseMapper.selectDistrictListByCities(cityAliases);
        return Result.success(deduplicateValues(districts));
    }

    private void normalizeUnitPrice(List<House> houses) {
        if (houses == null || houses.isEmpty()) {
            return;
        }
        houses.forEach(this::normalizeUnitPrice);
    }

    private void normalizeUnitPrice(House house) {
        if (house == null) {
            return;
        }

        if (house.getUnitPrice() != null && house.getUnitPrice().compareTo(BigDecimal.ZERO) > 0) {
            return;
        }

        if (house.getPrice() == null || house.getArea() == null || house.getArea().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        house.setUnitPrice(
                house.getPrice()
                        .multiply(new BigDecimal("10000"))
                        .divide(house.getArea(), 2, RoundingMode.HALF_UP)
        );
    }

    private void applyCityAliases(Map<String, Object> params) {
        if (params == null) {
            return;
        }

        List<String> cityAliases = buildCityAliases(asText(params.get("city")));
        if (cityAliases.isEmpty()) {
            params.remove("cityAliases");
            return;
        }

        params.put("city", cityAliases.get(0));
        params.put("cityAliases", cityAliases);
    }

    private List<String> normalizeCities(List<String> cities) {
        Set<String> normalizedCities = new LinkedHashSet<>();
        if (cities == null) {
            return new ArrayList<>();
        }

        for (String city : cities) {
            String normalizedCity = normalizeCityName(city);
            if (!normalizedCity.isEmpty()) {
                normalizedCities.add(normalizedCity);
            }
        }

        return new ArrayList<>(normalizedCities);
    }

    private List<String> deduplicateValues(List<String> values) {
        Set<String> uniqueValues = new LinkedHashSet<>();
        if (values == null) {
            return new ArrayList<>();
        }

        for (String value : values) {
            String cleanValue = value == null ? "" : value.trim();
            if (!cleanValue.isEmpty()) {
                uniqueValues.add(cleanValue);
            }
        }

        return new ArrayList<>(uniqueValues);
    }

    private List<String> buildCityAliases(String city) {
        Set<String> aliases = new LinkedHashSet<>();
        String cleanCity = city == null ? "" : city.trim();
        String normalizedCity = normalizeCityName(cleanCity);

        if (normalizedCity.isEmpty()) {
            return new ArrayList<>();
        }

        aliases.add(normalizedCity);

        if (cleanCity.endsWith("市")) {
            aliases.add(cleanCity);
        } else if (canAppendCitySuffix(normalizedCity)) {
            aliases.add(normalizedCity + "市");
        }

        return new ArrayList<>(aliases);
    }

    private String normalizeCityName(String city) {
        String cleanCity = city == null ? "" : city.trim();
        if (cleanCity.endsWith("市") && cleanCity.length() > 1) {
            return cleanCity.substring(0, cleanCity.length() - 1);
        }
        return cleanCity;
    }

    private boolean canAppendCitySuffix(String city) {
        return !city.endsWith("市")
                && !city.endsWith("区")
                && !city.endsWith("县")
                && !city.endsWith("州")
                && !city.endsWith("盟")
                && !city.endsWith("旗")
                && !city.endsWith("地区")
                && !city.endsWith("自治州")
                && !city.endsWith("特别行政区");
    }

    private String asText(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }
}
