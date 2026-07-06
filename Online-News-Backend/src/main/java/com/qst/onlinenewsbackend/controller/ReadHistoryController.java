package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.ReadHistory;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.ReadHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/read-history")
@Tag(name = "阅读历史", description = "阅读历史记录、清除历史相关接口")
public class ReadHistoryController {

    @Autowired
    private ReadHistoryService readHistoryService;

    @Autowired
    private NewsService newsService;

    @Autowired
    private NewsRelationHelper newsRelationHelper;

    @PostMapping
    @Operation(summary = "记录阅读历史", description = "用户浏览新闻时自动记录阅读历史")
    public Result<?> addReadHistory(@RequestParam Integer userId, @RequestParam Integer newsId) {
        ReadHistory history = new ReadHistory();
        history.setUserId(userId);
        history.setNewsId(newsId);
        readHistoryService.save(history);
        return Result.success("记录成功", history);
    }

    @GetMapping("/my")
    @Operation(summary = "我的阅读历史", description = "获取用户浏览过的新闻列表（按时间倒序）")
    public Result<?> getMyReadHistory(@RequestParam Integer userId) {
        QueryWrapper<ReadHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
                .orderByDesc("read_time");
        List<ReadHistory> list = readHistoryService.list(wrapper);

        // 填充每条记录的新闻详情（包括分类和标签）
        populateNewsInHistory(list);

        return Result.success(list);
    }

    @DeleteMapping("/my")
    @Operation(summary = "清除全部阅读历史", description = "用户清除自己的所有阅读历史记录")
    public Result<?> clearMyReadHistory(@RequestParam Integer userId) {
        QueryWrapper<ReadHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        readHistoryService.remove(wrapper);
        return Result.success("清除成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除单条阅读记录", description = "删除指定的阅读历史记录")
    public Result<?> deleteReadHistory(@PathVariable Integer id) {
        readHistoryService.removeById(id);
        return Result.success("删除成功");
    }

    /**
     * 为阅读历史列表填充新闻详情
     */
    private void populateNewsInHistory(List<ReadHistory> list) {
        if (list.isEmpty()) return;

        List<Integer> newsIds = list.stream()
                .map(ReadHistory::getNewsId)
                .distinct()
                .collect(Collectors.toList());

        // 批量查询新闻
        List<News> newsList = newsService.listByIds(newsIds);
        // 填充分类和标签
        newsRelationHelper.populateAll(newsList);

        Map<Integer, News> newsMap = newsList.stream()
                .collect(Collectors.toMap(News::getId, n -> n));

        for (ReadHistory history : list) {
            history.setNews(newsMap.get(history.getNewsId()));
        }
    }
}
