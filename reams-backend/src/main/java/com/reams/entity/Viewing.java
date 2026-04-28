package com.reams.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Viewing implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long customerId;
    private Long agentId;
    private Long houseId;
    private Date appointTime;
    private Date actualTime;
    private String viewingAddress;
    private Integer status;
    private String cancelReason;
    private Integer cancelByType;
    private Long cancelById;
    private String remark;
    private String customerPhone;
    private Date createTime;
    private Date updateTime;

    private String customerName;
    private String customerAvatar;
    private String agentName;
    private String agentPhone;
    private String agentAvatar;
    private String houseTitle;
    private String houseAddress;
    private Integer agentReviewSubmitted;
}
