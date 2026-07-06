package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.StatisticSnapshot;
import com.qst.onlinenewsbackend.entity.User;
import com.qst.onlinenewsbackend.enums.NewsStatus;
import com.qst.onlinenewsbackend.service.CommentService;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.StatisticSnapshotService;
import com.qst.onlinenewsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * 数据统计控制器
 * 模块4：新闻数据统计看板模块
 */
@RestController
@RequestMapping("/api/statistics")
@Tag(name = "数据统计", description = "新闻数据统计看板相关接口")
public class StatisticsController {

    @Autowired
    private NewsService newsService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private StatisticSnapshotService statisticSnapshotService;

    @GetMapping("/overview")
    @Operation(summary = "看板概览", description = "关键指标卡片：总新闻数、总用户数、今日阅读量、总评论数")
    public Result<?> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        overview.put("totalNews", newsService.count());
        overview.put("totalUsers", userService.count());
        overview.put("totalComments", commentService.count());
        // 今日阅读量：所有新闻的view_count之和
        long totalViews = newsService.lambdaQuery().list().stream()
                .mapToLong(n -> n.getViewCount() != null ? n.getViewCount() : 0)
                .sum();
        overview.put("todayViews", totalViews);
        return Result.success(overview);
    }

    @GetMapping("/category-distribution")
    @Operation(summary = "分类新闻数量占比", description = "各分类新闻数量占比（饼图数据）")
    public Result<?> getCategoryDistribution() {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.select("category_id", "COUNT(*) as count")
                .groupBy("category_id");
        List<Map<String, Object>> list = newsService.listMaps(wrapper);
        return Result.success(list);
    }

    @GetMapping("/publish-trend")
    @Operation(summary = "每日发布趋势", description = "每日新闻发布数量趋势（折线图数据）")
    public Result<?> getPublishTrend(@RequestParam(defaultValue = "30") Integer days) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.select("DATE(publish_time) as date", "COUNT(*) as count")
                .ge("publish_time", LocalDate.now().minusDays(days))
                .groupBy("DATE(publish_time)")
                .orderByAsc("date");
        List<Map<String, Object>> list = newsService.listMaps(wrapper);
        return Result.success(list);
    }

    @GetMapping("/top-news")
    @Operation(summary = "热门新闻TOP10", description = "按阅读量/评论数排序的热门新闻（条形图数据）")
    public Result<?> getTopNews(@RequestParam(defaultValue = "view_count") String sortBy,
                                 @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .orderByDesc(sortBy)
                .last("LIMIT " + limit);
        List<News> list = newsService.list(wrapper);
        return Result.success(list);
    }

    @GetMapping("/author-stats")
    @Operation(summary = "作者发稿量对比", description = "各作者发稿量对比（柱状图数据）")
    public Result<?> getAuthorStats() {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.select("author_id", "COUNT(*) as count")
                .eq("status", "PUBLISHED")
                .groupBy("author_id")
                .orderByDesc("count");
        List<Map<String, Object>> list = newsService.listMaps(wrapper);
        return Result.success(list);
    }

    @GetMapping("/snapshot")
    @Operation(summary = "获取统计快照", description = "获取历史统计快照数据")
    public Result<?> getSnapshots(@RequestParam(defaultValue = "30") Integer days) {
        List<StatisticSnapshot> list = statisticSnapshotService.lambdaQuery()
                .ge(StatisticSnapshot::getStatDate, LocalDate.now().minusDays(days))
                .orderByAsc(StatisticSnapshot::getStatDate)
                .list();
        return Result.success(list);
    }
}
