package com.reams.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

// 房源实体类
@Data
public class House implements Serializable {
    private static final long serialVersionUID = 1L;

    // 房源 ID
    private Long id;

    // 中介 ID
    private Long agentId;

    // 房源标题
    private String title;

    // 详细地址
    private String address;

    // 省份
    private String province;

    // 城市
    private String city;

    // 区县
    private String district;

    // 小区名称
    private String community;

    // 面积 (平方米)
    private BigDecimal area;

    // 售价 (万元)
    private BigDecimal price;

    // 单价 (元/平米)
    private BigDecimal unitPrice;

    // 户型 (如：3 室 2 厅)
    private String houseType;

    // 户型结构
    private String layout;

    // 楼层
    private String floor;

    // 总楼层
    private Integer totalFloor;

    // 建筑年代
    private Integer buildingYear;

    // 朝向
    private String orientation;

    // 装修情况：毛坯/简装/精装/豪华
    private String decoration;

    // 房屋类型：住宅/公寓/别墅/商铺
    private String propertyType;

    // 物业费
    private BigDecimal propertyFee;

    // 是否有电梯：0-无 1-有
    private Integer elevator;

    // 供暖：0-无 1-有
    private Integer heating;

    // 房源描述
    private String description;

    // 房源图片 (JSON 数组)
    private String images;

    // 房源视频 URL
    private String videoUrl;

    // 配套设施 (JSON: 地铁/学校/医院等)
    private String facilities;

    // 标签 (逗号分隔)
    private String tags;

    // 房源状态：0-未发布 1-已发布 2-已成交 3-已下架
    private Integer houseStatus;

    // 浏览次数
    private Integer viewCount;

    // 收藏次数
    private Integer favoriteCount;

    // 审核状态：0-待审核 1-审核中 2-审核通过 3-审核拒绝
    private Integer auditStatus;

    // 拒绝原因
    private String rejectReason;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    // 关联字段
    private String agentName;
    private String agentPhone;
}
