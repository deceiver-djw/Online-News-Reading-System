package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.service.NewsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 新闻控制器
 * 模块1：新闻列表展示与分类浏览
 * 模块2：新闻详情与内容展示
 * 模块5：新闻搜索与高级筛选
 * 模块8：新闻头条与实时热点
 */
@RestController
@RequestMapping("/api/news")
@Tag(name = "新闻管理", description = "新闻列表、详情、搜索、头条、热点相关接口")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRelationHelper newsRelationHelper;

    // ==================== 模块1：新闻列表展示与分类浏览 ====================

    @GetMapping("/list")
    @Operation(summary = "获取新闻列表", description = "分页获取新闻列表，支持按分类筛选")
    public Result<?> getNewsList(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED");
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        wrapper.orderByDesc("publish_time");
        List<News> list = newsService.page(new Page<>(pageNum, pageSize), wrapper).getRecords();
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    // ==================== 模块2：新闻详情与内容展示 ====================

    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情", description = "根据新闻ID获取完整新闻内容，同时增加浏览量")
    public Result<?> getNewsDetail(@PathVariable Integer id) {
        News news = newsService.getById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        // 浏览量+1
        news.setViewCount(news.getViewCount() + 1);
        newsService.updateById(news);
        // 填充分类和标签
        newsRelationHelper.populateAll(news);
        return Result.success(news);
    }

    // ==================== 模块5：新闻搜索与高级筛选 ====================

    @GetMapping("/search")
    @Operation(summary = "搜索新闻", description = "按关键词模糊搜索新闻标题、内容、来源")
    public Result<?> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .and(w -> w.like("title", keyword)
                        .or().like("content", keyword)
                        .or().like("source", keyword))
                .orderByDesc("publish_time");
        List<News> list = newsService.page(new Page<>(pageNum, pageSize), wrapper).getRecords();
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @GetMapping("/filter")
    @Operation(summary = "高级筛选", description = "多条件组合筛选新闻（分类、时间范围、来源、阅读量区间）")
    public Result<?> filterNews(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer minViews,
            @RequestParam(required = false) Integer maxViews,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED");
        if (categoryId != null) wrapper.eq("category_id", categoryId);
        if (startTime != null) wrapper.ge("publish_time", startTime);
        if (endTime != null) wrapper.le("publish_time", endTime);
        if (source != null) wrapper.like("source", source);
        if (minViews != null) wrapper.ge("view_count", minViews);
        if (maxViews != null) wrapper.le("view_count", maxViews);
        wrapper.orderByDesc("publish_time");
        List<News> list = newsService.page(new Page<>(pageNum, pageSize), wrapper).getRecords();
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    // ==================== 模块8：新闻头条与实时热点 ====================

    @GetMapping("/headline")
    @Operation(summary = "头条新闻", description = "获取首页头条轮播新闻（最新/最重要的5条）")
    public Result<?> getHeadline() {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .orderByDesc("publish_time")
                .last("LIMIT 5");
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @GetMapping("/hot")
    @Operation(summary = "实时热点", description = "按阅读量排序的热点新闻榜单，每分钟自动刷新")
    public Result<?> getHotNews(@RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "PUBLISHED")
                .orderByDesc("view_count")
                .last("LIMIT " + limit);
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }
}
