package com.reams.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

// 收藏实体类
@Data
public class Favorite implements Serializable {
    private static final long serialVersionUID = 1L;

// 收藏 ID
    private Long id;

// 客户 ID
    private Long customerId;

// 房源 ID
    private Long houseId;

// 创建时间
    private Date createTime;

    // 关联字段
    private String customerName;
    private String houseTitle;
    private String houseAddress;
    private String houseImage;
    private String housePrice;
    private String houseType;
    private Double houseArea;
}
