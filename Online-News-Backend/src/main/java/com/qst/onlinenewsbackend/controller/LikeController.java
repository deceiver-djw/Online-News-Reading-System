package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Comment;
import com.qst.onlinenewsbackend.entity.LikeRecord;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.enums.LikeTargetType;
import com.qst.onlinenewsbackend.service.CommentService;
import com.qst.onlinenewsbackend.service.LikeRecordService;
import com.qst.onlinenewsbackend.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 * 模块2：新闻详情与内容展示模块
 * 模块7：新闻收藏与评论互动模块
 */
@RestController
@RequestMapping("/api/like")
@Tag(name = "点赞管理", description = "新闻和评论的点赞、取消点赞相关接口")
public class LikeController {

    @Autowired
    private LikeRecordService likeRecordService;
    @Autowired
    private NewsService newsService;
    @Autowired
    private CommentService commentService;

    @PostMapping("/news")
    @Operation(summary = "点赞新闻", description = "用户对新闻进行点赞")
    public Result<?> likeNews(@RequestParam Integer userId, @RequestParam Integer newsId) {
        // 检查是否已点赞
        long count = likeRecordService.lambdaQuery()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, LikeTargetType.NEWS)
                .eq(LikeRecord::getTargetId, newsId)
                .count();
        if (count > 0) {
            return Result.error("已点赞");
        }
        // 记录点赞
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setTargetType(LikeTargetType.NEWS);
        record.setTargetId(newsId);
        likeRecordService.save(record);
        // 更新新闻点赞数
        News news = newsService.getById(newsId);
        if (news != null) {
            news.setLikeCount(news.getLikeCount() + 1);
            newsService.updateById(news);
        }
        return Result.success("点赞成功", record);
    }

    @DeleteMapping("/news")
    @Operation(summary = "取消点赞新闻", description = "用户取消对新闻的点赞")
    public Result<?> unlikeNews(@RequestParam Integer userId, @RequestParam Integer newsId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("target_type", "NEWS")
                .eq("target_id", newsId);
        likeRecordService.remove(wrapper);
        // 更新新闻点赞数
        News news = newsService.getById(newsId);
        if (news != null && news.getLikeCount() > 0) {
            news.setLikeCount(news.getLikeCount() - 1);
            newsService.updateById(news);
        }
        return Result.success("取消点赞成功");
    }

    @PostMapping("/comment")
    @Operation(summary = "点赞评论", description = "用户对评论进行点赞")
    public Result<?> likeComment(@RequestParam Integer userId, @RequestParam Integer commentId) {
        // 检查是否已点赞
        long count = likeRecordService.lambdaQuery()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, LikeTargetType.COMMENT)
                .eq(LikeRecord::getTargetId, commentId)
                .count();
        if (count > 0) {
            return Result.error("已点赞");
        }
        // 记录点赞
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setTargetType(LikeTargetType.COMMENT);
        record.setTargetId(commentId);
        likeRecordService.save(record);
        // 更新评论点赞数
        Comment comment = commentService.getById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentService.updateById(comment);
        }
        return Result.success("点赞成功", record);
    }

    @DeleteMapping("/comment")
    @Operation(summary = "取消点赞评论", description = "用户取消对评论的点赞")
    public Result<?> unlikeComment(@RequestParam Integer userId, @RequestParam Integer commentId) {
        QueryWrapper<LikeRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .eq("target_type", "COMMENT")
                .eq("target_id", commentId);
        likeRecordService.remove(wrapper);
        // 更新评论点赞数
        Comment comment = commentService.getById(commentId);
        if (comment != null && comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            commentService.updateById(comment);
        }
        return Result.success("取消点赞成功");
    }

    @GetMapping("/check")
    @Operation(summary = "检查点赞状态", description = "检查用户是否已点赞指定目标")
    public Result<?> checkLike(@RequestParam Integer userId,
                                @RequestParam String targetType,
                                @RequestParam Integer targetId) {
        long count = likeRecordService.lambdaQuery()
                .eq(LikeRecord::getUserId, userId)
                .eq(LikeRecord::getTargetType, LikeTargetType.fromValue(targetType))
                .eq(LikeRecord::getTargetId, targetId)
                .count();
        return Result.success(count > 0);
    }
}
