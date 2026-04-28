package com.reams.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysCustomer implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String password;
    private String nickname;
    private String phone;
    private String email;
    private String avatar;
    private Integer gender;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
