package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Collection;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.ReadHistory;
import com.qst.onlinenewsbackend.enums.NewsStatus;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.service.CollectionService;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.ReadHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐控制器
 * 模块6：用户登录与个性化推荐模块
 */
@RestController
@RequestMapping("/api/recommend")
@Tag(name = "个性化推荐", description = "猜你喜欢等个性化推荐相关接口")
public class RecommendController {

    @Autowired
    private NewsService newsService;
    @Autowired
    private ReadHistoryService readHistoryService;
    @Autowired
    private CollectionService collectionService;
    @Autowired
    private NewsRelationHelper newsRelationHelper;

    @GetMapping("/guess-you-like")
    @Operation(summary = "猜你喜欢", description = "根据用户阅读历史和收藏偏好推荐个性化新闻")
    public Result<?> guessYouLike(@RequestParam Integer userId,
                                   @RequestParam(defaultValue = "10") Integer limit) {
        // 获取用户阅读历史中的新闻ID
        List<ReadHistory> histories = readHistoryService.lambdaQuery()
                .eq(ReadHistory::getUserId, userId)
                .list();
        Set<Integer> readNewsIds = histories.stream()
                .map(ReadHistory::getNewsId)
                .collect(Collectors.toSet());

        // 获取用户收藏偏好（分类）
        List<Collection> collections = collectionService.lambdaQuery()
                .eq(Collection::getUserId, userId)
                .list();
        Set<Integer> collectNewsIds = collections.stream()
                .map(Collection::getNewsId)
                .collect(Collectors.toSet());

        // 获取已读和已收藏的新闻，分析分类偏好
        Set<Integer> allInteractedIds = new HashSet<>();
        allInteractedIds.addAll(readNewsIds);
        allInteractedIds.addAll(collectNewsIds);

        // 推荐：排除已读的新闻，按热度排序
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED");
        if (!allInteractedIds.isEmpty()) {
            wrapper.notIn("id", allInteractedIds);
        }
        wrapper.orderByDesc("view_count")
                .last("LIMIT " + limit);
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @GetMapping("/related")
    @Operation(summary = "相关推荐", description = "根据当前新闻推荐同类新闻")
    public Result<?> getRelatedNews(@RequestParam Integer newsId,
                                     @RequestParam(defaultValue = "5") Integer limit) {
        News currentNews = newsService.getById(newsId);
        if (currentNews == null) {
            return Result.error("新闻不存在");
        }
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .eq("category_id", currentNews.getCategoryId())
                .ne("id", newsId)
                .orderByDesc("view_count")
                .last("LIMIT " + limit);
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }
}
