package com.reams.entity;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

// 房源审核实体类
@Data
public class HouseAudit implements Serializable {
    private static final long serialVersionUID = 1L;

// 审核 ID
    private Long id;

// 房源 ID
    private Long houseId;

// 审核人 ID(管理员)
    private Long auditorId;

// 审核类型：1-发布审核 2-修改审核
    private Integer auditType;

// 审核结果：1-通过 2-拒绝
    private Integer auditResult;

// 审核意见/拒绝原因
    private String auditReason;

// 审核时间
    private Date auditTime;

// 创建时间
    private Date createTime;

    // 关联字段
    private String auditorName;
    private String houseTitle;
}
