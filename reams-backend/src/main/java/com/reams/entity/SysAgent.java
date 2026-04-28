package com.reams.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SysAgent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String password;
    private String name;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private BigDecimal rating;
    private Integer dealCount;
    private Integer yearsExperience;
    private String introduction;
    private String company;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
