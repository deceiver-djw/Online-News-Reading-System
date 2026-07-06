package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.Collection;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.service.CollectionService;
import com.qst.onlinenewsbackend.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collection")
@Tag(name = "新闻收藏", description = "收藏、取消收藏、收藏列表相关接口")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;
    @Autowired
    private NewsService newsService;
    @Autowired
    private NewsRelationHelper newsRelationHelper;

    @PostMapping
    @Operation(summary = "收藏新闻", description = "用户收藏指定新闻")
    public Result<?> addCollection(@RequestParam Integer userId, @RequestParam Integer newsId) {
        // 检查是否已收藏
        long count = collectionService.lambdaQuery()
                .eq(Collection::getUserId, userId)
                .eq(Collection::getNewsId, newsId)
                .count();
        if (count > 0) {
            return Result.error("已收藏该新闻");
        }
        Collection collection = new Collection();
        collection.setUserId(userId);
        collection.setNewsId(newsId);
        collectionService.save(collection);
        // 更新新闻收藏数
        News news = newsService.getById(newsId);
        if (news != null) {
            news.setCollectCount(news.getCollectCount() + 1);
            newsService.updateById(news);
        }
        return Result.success("收藏成功", collection);
    }

    @DeleteMapping
    @Operation(summary = "取消收藏", description = "用户取消收藏指定新闻")
    public Result<?> removeCollection(@RequestParam Integer userId, @RequestParam Integer newsId) {
        collectionService.remove(new QueryWrapper<Collection>()
                .eq("user_id", userId)
                .eq("news_id", newsId));
        // 更新新闻收藏数
        News news = newsService.getById(newsId);
        if (news != null && news.getCollectCount() > 0) {
            news.setCollectCount(news.getCollectCount() - 1);
            newsService.updateById(news);
        }
        return Result.success("取消收藏成功");
    }

    @GetMapping("/my")
    @Operation(summary = "我的收藏列表", description = "获取用户收藏的新闻列表（按收藏时间倒序）")
    public Result<?> getMyCollections(@RequestParam Integer userId) {
        QueryWrapper<Collection> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("create_time");
        List<Collection> list = collectionService.list(wrapper);

        // 填充每条收藏的新闻详情（包括分类和标签）
        populateNewsInCollection(list);

        return Result.success(list);
    }

    @GetMapping("/check")
    @Operation(summary = "检查收藏状态", description = "检查用户是否已收藏指定新闻")
    public Result<?> checkCollection(@RequestParam Integer userId, @RequestParam Integer newsId) {
        long count = collectionService.lambdaQuery()
                .eq(Collection::getUserId, userId)
                .eq(Collection::getNewsId, newsId)
                .count();
        return Result.success(count > 0);
    }

    /**
     * 为收藏列表填充新闻详情
     */
    private void populateNewsInCollection(List<Collection> list) {
        if (list.isEmpty()) return;

        List<Integer> newsIds = list.stream()
                .map(Collection::getNewsId)
                .distinct()
                .collect(Collectors.toList());

        List<News> newsList = newsService.listByIds(newsIds);
        newsRelationHelper.populateAll(newsList);

        Map<Integer, News> newsMap = newsList.stream()
                .collect(Collectors.toMap(News::getId, n -> n));

        for (Collection collection : list) {
            collection.setNews(newsMap.get(collection.getNewsId()));
        }
    }
}
