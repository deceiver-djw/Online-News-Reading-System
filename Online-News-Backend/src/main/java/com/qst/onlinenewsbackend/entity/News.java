package com.qst.onlinenewsbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qst.onlinenewsbackend.enums.NewsStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("news")
public class News {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private String videoUrl;

    private String source;

    private LocalDateTime publishTime;

    private NewsStatus status = NewsStatus.DRAFT;

    private Integer viewCount = 0;

    private Integer likeCount = 0;

    private Integer collectCount = 0;

    private Integer commentCount = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Integer categoryId;

    private Integer authorId;

    @JsonIgnore
    @TableField(exist = false)
    private Category category;

    @JsonIgnore
    @TableField(exist = false)
    private User author;

    @JsonIgnore
    @TableField(exist = false)
    private List<Tag> tags;

    @JsonIgnore
    @TableField(exist = false)
    private List<Comment> commentList;

    @JsonIgnore
    @TableField(exist = false)
    private List<Collection> collections;
}
