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
@TableName("subscriptions")
public class Subscription {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Boolean isPushEnabled = true;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime subscribeTime;

    private Integer userId;

    private Integer categoryId;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Category category;
}
