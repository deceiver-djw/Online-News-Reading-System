package com.qst.onlinenewsbackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qst.onlinenewsbackend.common.Result;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.enums.NewsStatus;
import com.qst.onlinenewsbackend.helper.NewsRelationHelper;
import com.qst.onlinenewsbackend.mapper.TagMapper;
import com.qst.onlinenewsbackend.service.ImageGenerationService;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/editor/news")
@Tag(name = "新闻编辑", description = "新闻发布、编辑、删除、审核相关接口")
public class NewsEditorController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private NewsRelationHelper newsRelationHelper;

    @Autowired
    private TagService tagService;

    @Autowired
    private TagMapper tagMapper;

    @Value("${qianfan.image-save-path}")
    private String imageSavePath;

    @PostMapping
    @Operation(summary = "发布新闻", description = "提交新闻稿件，状态为待审核，可自动生成配图")
    public Result<?> publishNews(@RequestBody Map<String, Object> body) {
        News news = new News();
        news.setTitle((String) body.get("title"));
        news.setSummary((String) body.get("summary"));
        news.setContent((String) body.get("content"));
        news.setSource((String) body.get("source"));
        news.setStatus(NewsStatus.PENDING);
        news.setPublishTime(LocalDateTime.now());
        news.setViewCount(0);
        news.setLikeCount(0);
        news.setCollectCount(0);
        news.setCommentCount(0);

        // categoryId
        Object catId = body.get("categoryId");
        if (catId instanceof Integer) {
            news.setCategoryId((Integer) catId);
        } else if (catId instanceof Number) {
            news.setCategoryId(((Number) catId).intValue());
        }

        // authorId
        Object authId = body.get("authorId");
        if (authId instanceof Integer) {
            news.setAuthorId((Integer) authId);
        } else if (authId instanceof Number) {
            news.setAuthorId(((Number) authId).intValue());
        }

        // coverImage
        String coverImage = (String) body.get("coverImage");
        if (coverImage != null && !coverImage.isEmpty()) {
            news.setCoverImage(coverImage);
        } else {
            // 自动生成新闻配图
            try {
                String imagePath = imageGenerationService.generateImageForNews(
                        news.getTitle(), news.getSummary());
                if (imagePath != null) {
                    news.setCoverImage(imagePath);
                }
            } catch (Exception e) {
                System.err.println("新闻配图生成失败: " + e.getMessage());
            }
        }

        newsService.save(news);

        // 处理标签
        @SuppressWarnings("unchecked")
        List<String> tagNames = (List<String>) body.get("tags");
        if (tagNames != null && !tagNames.isEmpty()) {
            for (String tagName : tagNames) {
                if (tagName == null || tagName.trim().isEmpty()) continue;
                com.qst.onlinenewsbackend.entity.Tag existingTag = tagService.lambdaQuery()
                        .eq(com.qst.onlinenewsbackend.entity.Tag::getTagName, tagName.trim())
                        .one();
                if (existingTag == null) {
                    existingTag = new com.qst.onlinenewsbackend.entity.Tag();
                    existingTag.setTagName(tagName.trim());
                    tagService.save(existingTag);
                }
                tagMapper.insertNewsTag(news.getId(), existingTag.getId());
            }
        }

        newsRelationHelper.populateAll(news);
        return Result.success("发布成功，等待审核", news);
    }

    @PostMapping("/upload-image")
    @Operation(summary = "上传新闻图片", description = "上传图片文件到服务器，返回图片路径")
    public Result<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择图片文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        try {
            File dir = new File(imageSavePath);
            if (!dir.exists()) dir.mkdirs();
            String originalName = file.getOriginalFilename();
            String ext = originalName != null && originalName.contains(".")
                    ? originalName.substring(originalName.lastIndexOf(".")) : ".png";
            String fileName = "upload_" + System.currentTimeMillis() + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + ext;
            File dest = new File(dir, fileName);
            file.transferTo(dest);
            return Result.success("上传成功", imageSavePath + File.separator + fileName);
        } catch (IOException e) {
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑新闻", description = "编辑修改新闻稿件，修改后状态重置为待审核")
    public Result<?> updateNews(@PathVariable Integer id, @RequestBody Map<String, Object> body) {
        News news = new News();
        news.setId(id);
        news.setTitle((String) body.get("title"));
        news.setSummary((String) body.get("summary"));
        news.setContent((String) body.get("content"));
        news.setSource((String) body.get("source"));
        news.setStatus(NewsStatus.PENDING);

        Object catId = body.get("categoryId");
        if (catId instanceof Integer) {
            news.setCategoryId((Integer) catId);
        } else if (catId instanceof Number) {
            news.setCategoryId(((Number) catId).intValue());
        }

        String coverImage = (String) body.get("coverImage");
        if (coverImage != null && !coverImage.isEmpty()) {
            news.setCoverImage(coverImage);
        }

        newsService.updateById(news);
        newsRelationHelper.populateAll(news);
        return Result.success("编辑成功，已重新提交审核", news);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除新闻", description = "删除新闻稿件")
    public Result<?> deleteNews(@PathVariable Integer id) {
        newsService.removeById(id);
        return Result.success("删除成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取新闻详情", description = "获取新闻详情用于编辑")
    public Result<?> getNewsById(@PathVariable Integer id) {
        News news = newsService.getById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        newsRelationHelper.populateAll(news);
        return Result.success(news);
    }

    @GetMapping("/my")
    @Operation(summary = "我的新闻列表", description = "获取当前作者发布的新闻列表")
    public Result<?> getMyNews(@RequestParam Integer authorId) {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.eq("author_id", authorId).orderByDesc("created_at");
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @GetMapping("/all")
    @Operation(summary = "全部新闻列表", description = "管理员获取所有用户的新闻列表")
    public Result<?> getAllNews() {
        QueryWrapper<News> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        List<News> list = newsService.list(wrapper);
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @PutMapping("/{id}/audit")
    @Operation(summary = "审核新闻", description = "管理员审核新闻（通过/拒绝）")
    public Result<?> auditNews(@PathVariable Integer id, @RequestParam String action) {
        News news = newsService.getById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        if ("approve".equals(action)) {
            news.setStatus(NewsStatus.PUBLISHED);
            news.setPublishTime(LocalDateTime.now());
        } else if ("reject".equals(action)) {
            news.setStatus(NewsStatus.REJECTED);
        } else {
            return Result.error("无效的操作");
        }
        newsService.updateById(news);
        newsRelationHelper.populateAll(news);
        return Result.success("审核完成", news);
    }

    @GetMapping("/pending")
    @Operation(summary = "待审核新闻列表", description = "管理员获取待审核的新闻列表")
    public Result<?> getPendingNews() {
        List<News> list = newsService.lambdaQuery()
                .eq(News::getStatus, NewsStatus.PENDING)
                .orderByAsc(News::getCreatedAt)
                .list();
        newsRelationHelper.populateAll(list);
        return Result.success(list);
    }

    @PostMapping("/{id}/generate-image")
    @Operation(summary = "为新闻生成配图", description = "手动触发为指定新闻生成配图")
    public Result<?> generateNewsImage(@PathVariable Integer id) {
        News news = newsService.getById(id);
        if (news == null) {
            return Result.error("新闻不存在");
        }
        try {
            String imagePath = imageGenerationService.generateImageForNews(
                    news.getTitle(), news.getSummary());
            if (imagePath != null) {
                news.setCoverImage(imagePath);
                newsService.updateById(news);
                return Result.success("图片生成成功", imagePath);
            } else {
                return Result.error("图片生成失败");
            }
        } catch (Exception e) {
            return Result.error("图片生成异常: " + e.getMessage());
        }
    }
}
