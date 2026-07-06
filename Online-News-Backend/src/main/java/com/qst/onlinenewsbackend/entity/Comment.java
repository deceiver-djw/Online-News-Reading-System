package com.qst.onlinenewsbackend.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.qst.onlinenewsbackend.enums.CommentStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("comments")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String content;

    private Integer likeCount = 0;

    private Boolean isPinned = false;

    private CommentStatus status = CommentStatus.NORMAL;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    private Integer newsId;

    private Integer userId;

    private Integer parentCommentId;

    @TableField(exist = false)
    private News news;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private Comment parentComment;

    @TableField(exist = false)
    private List<Comment> replyList;
}
