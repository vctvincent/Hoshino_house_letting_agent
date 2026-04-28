package com.reams.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {

    private Long id;
    private Long senderId;
    private Integer senderType;
    private Long receiverId;
    private Integer receiverType;
    private Integer messageType;
    private String contentType;
    private String title;
    private String content;
    private String messageScene;
    private Long houseId;
    private Long viewingId;
    private Integer isRead;
    private Date readTime;
    private Date createTime;
    private Date updateTime;
}
