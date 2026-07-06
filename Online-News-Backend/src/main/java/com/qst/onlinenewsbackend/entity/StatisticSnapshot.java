package com.qst.onlinenewsbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("statistic_snapshots")
public class StatisticSnapshot {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private LocalDate statDate;

    private Integer totalNews = 0;

    private Integer totalUsers = 0;

    private Integer totalComments = 0;

    private Integer dailyViews = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
