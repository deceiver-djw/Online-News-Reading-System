package com.qst.onlinenewsbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notifications")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String body;

    private String jumpUrl;

    private Boolean isRead = false;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer userId;

    @TableField(exist = false)
    private User user;
}
