package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Comment;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.User;
import com.qst.onlinenewsbackend.enums.CommentStatus;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.service.CommentService;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论管理", description = "评论发布、回复、点赞、置顶、删除相关接口")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private NewsService newsService;
    @Autowired
    private UserService userService;
    @Autowired
    private NewsRelationHelper newsRelationHelper;

    @PostMapping
    @Operation(summary = "发表评论", description = "用户在新闻详情页发表评论")
    public Result<?> addComment(@RequestBody Comment comment) {
        comment.setStatus(CommentStatus.NORMAL);
        commentService.save(comment);
        News news = newsService.getById(comment.getNewsId());
        if (news != null) {
            news.setCommentCount(news.getCommentCount() + 1);
            newsService.updateById(news);
        }
        // 填充用户信息
        populateUser(comment);
        return Result.success("评论成功", comment);
    }

    @PostMapping("/reply")
    @Operation(summary = "回复评论", description = "用户回复指定评论")
    public Result<?> replyComment(@RequestBody Comment comment) {
        comment.setStatus(CommentStatus.NORMAL);
        commentService.save(comment);
        populateUser(comment);
        return Result.success("回复成功", comment);
    }

    @GetMapping("/news/{newsId}")
    @Operation(summary = "获取新闻评论列表", description = "获取指定新闻的评论列表（置顶优先，按时间倒序）")
    public Result<?> getCommentsByNewsId(@PathVariable Integer newsId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("news_id", newsId)
                .eq("status", "NORMAL")
                .isNull("parent_comment_id")
                .orderByDesc("is_pinned")
                .orderByDesc("create_time");
        List<Comment> list = commentService.list(wrapper);
        populateUsers(list);
        return Result.success(list);
    }

    @GetMapping("/replies/{commentId}")
    @Operation(summary = "获取评论回复列表", description = "获取指定评论的子回复列表")
    public Result<?> getReplies(@PathVariable Integer commentId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_comment_id", commentId)
                .eq("status", "NORMAL")
                .orderByAsc("create_time");
        List<Comment> list = commentService.list(wrapper);
        populateUsers(list);
        return Result.success(list);
    }

    @PutMapping("/{id}/pin")
    @Operation(summary = "置顶/取消置顶评论", description = "管理员设置评论置顶或取消置顶")
    public Result<?> pinComment(@PathVariable Integer id, @RequestParam Boolean isPinned) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setIsPinned(isPinned);
        commentService.updateById(comment);
        return Result.success(isPinned ? "置顶成功" : "取消置顶成功", comment);
    }

    @PutMapping("/{id}/like")
    @Operation(summary = "评论点赞", description = "对评论进行点赞")
    public Result<?> likeComment(@PathVariable Integer id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setLikeCount(comment.getLikeCount() + 1);
        commentService.updateById(comment);
        return Result.success("点赞成功", comment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除指定评论（软删除）")
    public Result<?> deleteComment(@PathVariable Integer id) {
        Comment comment = commentService.getById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setStatus(CommentStatus.DELETED);
        commentService.updateById(comment);
        return Result.success("删除成功");
    }

    @GetMapping("/my")
    @Operation(summary = "我的评论列表", description = "获取用户发表的所有评论")
    public Result<?> getMyComments(@RequestParam Integer userId) {
        QueryWrapper<Comment> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("status", "NORMAL")
                .orderByDesc("create_time");
        List<Comment> list = commentService.list(wrapper);

        if (!list.isEmpty()) {
            Set<Integer> newsIds = list.stream()
                    .map(Comment::getNewsId)
                    .collect(Collectors.toSet());
            Map<Integer, News> newsMap = newsService.listByIds(newsIds).stream()
                    .collect(Collectors.toMap(News::getId, n -> n));
            for (Comment comment : list) {
                comment.setNews(newsMap.get(comment.getNewsId()));
            }
        }

        populateUsers(list);
        return Result.success(list);
    }

    private void populateUser(Comment comment) {
        if (comment.getUserId() != null) {
            comment.setUser(userService.getById(comment.getUserId()));
        }
    }

    private void populateUsers(List<Comment> list) {
        if (list == null || list.isEmpty()) return;
        Set<Integer> userIds = list.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (userIds.isEmpty()) return;
        Map<Integer, User> userMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        for (Comment comment : list) {
            if (comment.getUserId() != null) {
                comment.setUser(userMap.get(comment.getUserId()));
            }
        }
    }
}
