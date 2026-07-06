package com.qst.onlinenewsbackend.controller;

import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.service.ImageGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 图片生成控制器
 * 提供新闻图片自动生成接口
 *
 * @author QST
 * @since 1.0
 */
@RestController
@RequestMapping("/api/image")
@Tag(name = "图片生成", description = "使用百度千帆为新闻自动生成配图")
public class ImageGenerationController {

    @Autowired
    private ImageGenerationService imageGenerationService;

    @PostMapping("/generate")
    @Operation(summary = "为新闻生成图片", description = "根据新闻标题和摘要，使用百度千帆大模型生成配图")
    public Result<?> generateImage(@RequestBody ImageGenerateRequest request) {
        try {
            String imagePath = imageGenerationService.generateImageForNews(
                    request.getTitle(),
                    request.getSummary()
            );

            if (imagePath != null) {
                return Result.success("图片生成成功", imagePath);
            } else {
                return Result.error("图片生成失败");
            }
        } catch (Exception e) {
            return Result.error("图片生成异常: " + e.getMessage());
        }
    }

    @PostMapping("/generate/batch")
    @Operation(summary = "批量为新闻生成图片", description = "批量为多个新闻生成配图")
    public Result<?> generateImagesBatch(@RequestBody List<BatchImageRequest> requests) {
        try {
            List<ImageGenerationService.NewsTitleSummary> newsList = requests.stream()
                    .map(req -> new ImageGenerationService.NewsTitleSummary(
                            req.getTitle(),
                            req.getSummary()
                    ))
                    .toList();

            List<String> imagePaths = imageGenerationService.generateImagesForNewsList(newsList);

            return Result.success("批量图片生成完成", imagePaths);
        } catch (Exception e) {
            return Result.error("批量图片生成异常: " + e.getMessage());
        }
    }

    /**
     * 图片生成请求
     */
    @Data
    public static class ImageGenerateRequest {
        private String title;
        private String summary;
    }

    /**
     * 批量图片生成请求
     */
    @Data
    public static class BatchImageRequest {
        private String title;
        private String summary;
    }
}
