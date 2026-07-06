package com.qst.onlinenewsbackend.controller;

import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.service.AINewsGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI新闻生成控制器
 * 提供一键生成新闻（内容+配图）的接口
 *
 * @author QST
 * @since 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/news")
@Tag(name = "AI新闻生成", description = "使用DeepSeek生成新闻内容，百度千帆生成配图")
public class AINewsGenerationController {

    private static final Logger log = LoggerFactory.getLogger(AINewsGenerationController.class);

    @Autowired
    private AINewsGenerationService aiNewsGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "AI生成单条新闻（含配图）", 
               description = "根据主题，使用DeepSeek生成新闻内容，百度千帆自动生成配图")
    public Result<?> generateNews(@RequestBody NewsGenerateRequest request) {
        try {
            log.info("收到AI新闻生成请求，主题: {}, 分类ID: {}, 作者ID: {}", 
                    request.getTopic(), request.getCategoryId(), request.getAuthorId());

            News news = aiNewsGenerationService.generateNewsWithImage(
                    request.getTopic(),
                    request.getCategoryId(),
                    request.getAuthorId()
            );

            if (news != null) {
                return Result.success("新闻生成成功（含配图）", news);
            } else {
                return Result.error("新闻生成失败");
            }
        } catch (Exception e) {
            log.error("AI新闻生成异常", e);
            return Result.error("AI新闻生成异常: " + e.getMessage());
        }
    }

    @PostMapping("/generate/batch")
    @Operation(summary = "AI批量生成新闻（含配图）", 
               description = "根据多个主题，批量生成新闻，每条新闻自动配图")
    public Result<?> generateNewsBatch(@RequestBody BatchNewsGenerateRequest request) {
        try {
            log.info("收到批量AI新闻生成请求，主题数量: {}", request.getTopics().size());

            List<News> newsList = aiNewsGenerationService.generateNewsBatchWithImages(
                    request.getTopics(),
                    request.getCategoryId(),
                    request.getAuthorId()
            );

            return Result.success("批量生成成功，共生成 " + newsList.size() + " 条新闻", newsList);
        } catch (Exception e) {
            log.error("批量AI新闻生成异常", e);
            return Result.error("批量AI新闻生成异常: " + e.getMessage());
        }
    }

    @PostMapping("/generate/hot-topics")
    @Operation(summary = "基于热点话题生成新闻（含配图）", 
               description = "根据热点话题列表，自动生成新闻和配图")
    public Result<?> generateNewsFromHotTopics(@RequestBody HotTopicsRequest request) {
        try {
            log.info("收到热点话题新闻生成请求，话题数量: {}", request.getTopics().size());

            List<News> newsList = aiNewsGenerationService.generateNewsFromHotTopics(
                    request.getTopics(),
                    request.getCategoryId(),
                    request.getAuthorId()
            );

            return Result.success("热点新闻生成成功，共生成 " + newsList.size() + " 条新闻", newsList);
        } catch (Exception e) {
            log.error("热点话题新闻生成异常", e);
            return Result.error("热点话题新闻生成异常: " + e.getMessage());
        }
    }

    /**
     * 单条新闻生成请求
     */
    @Data
    public static class NewsGenerateRequest {
        private String topic;        // 新闻主题
        private Integer categoryId;  // 分类ID
        private Integer authorId;    // 作者ID
    }

    /**
     * 批量新闻生成请求
     */
    @Data
    public static class BatchNewsGenerateRequest {
        private List<String> topics; // 主题列表
        private Integer categoryId;  // 分类ID
        private Integer authorId;    // 作者ID
    }

    /**
     * 热点话题请求
     */
    @Data
    public static class HotTopicsRequest {
        private List<AINewsGenerationService.HotTopic> topics; // 热点话题列表
        private Integer categoryId;  // 分类ID
        private Integer authorId;    // 作者ID
    }
}
