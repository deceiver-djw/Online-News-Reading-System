package com.qst.onlinenewsbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qst.onlinenewsbackend.enums.LikeTargetType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_records")
public class LikeRecord {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private LikeTargetType targetType;

    private Integer targetId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer userId;

    @TableField(exist = false)
    private User user;
}
