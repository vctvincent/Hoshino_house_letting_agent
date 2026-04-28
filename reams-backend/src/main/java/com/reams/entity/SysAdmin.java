package com.reams.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysAdmin implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String password;
    private String email;
    private String phone;
    private String avatar;
    private Date createTime;
    private Date updateTime;
}
