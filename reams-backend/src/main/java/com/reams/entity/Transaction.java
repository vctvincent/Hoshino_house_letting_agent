package com.reams.entity;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 交易实体类 - 核心业务实体 (简化版)
 * 对应数据库表：h_transaction
 */
@Data
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    // 交易 ID
    private Long id;

    // 房源 ID
    private Long houseId;

    // 客户 ID
    private Long customerId;

    // 中介 ID
    private Long agentId;

    // 带看 ID
    private Long viewingId;

    // 交易单号
    private String transactionNo;

    // 成交价格 (万元)
    private BigDecimal finalPrice;

    // 定金 (万元)
    private BigDecimal deposit;

    // 付款方式：全款/分期/贷款
    private String paymentMethod;

    // 合同文件 URL
    private String contractUrl;

    // 成交日期
    private Date dealDate;

    // 状态：0-待确认 1-谈判中 2-已签约 3-已完成 4-已取消
    private Integer status;

    // 状态变更历史 (JSON 格式)
    private String statusHistory;

    // 备注/操作日志
    private String remark;

    // 创建时间
    private Date createTime;

    // 更新时间
    private Date updateTime;

    // ========== 关联字段 (用于前端展示) ==========
    private String customerName;
    private String customerPhone;
    private String agentName;
    private String houseTitle;
    private String houseAddress;
}
