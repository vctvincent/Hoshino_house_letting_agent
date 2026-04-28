package com.reams.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long transactionId;
    private Long viewingId;
    private Long houseId;
    private Long agentId;
    private Long customerId;
    private Integer targetType;
    private Integer rating;
    private String content;
    private String images;
    private String replyContent;
    private Date replyTime;
    private Integer isShow;
    private Date createTime;
    private Date updateTime;

    private String customerName;
    private String customerNickname;
    private String customerAvatar;
    private String agentName;
    private String agentAvatar;
    private String houseTitle;
}
