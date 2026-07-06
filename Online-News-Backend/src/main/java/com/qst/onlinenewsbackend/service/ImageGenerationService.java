package com.qst.onlinenewsbackend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 图片生成服务
 * 使用百度千帆大模型为新闻生成配图
 * 通过HTTP REST API调用百度千帆图片生成接口
 *
 * @author QST
 * @since 1.0
 */
@Slf4j
@Service
public class ImageGenerationService {

    @Value("${qianfan.api-key}")
    private String apiKey;

    @Value("${qianfan.image-save-path}")
    private String imageSavePath;

    @Value("${qianfan.image-model:qwen-image}")
    private String imageModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 百度千帆图片生成API端点
    private static final String QIANFAN_IMAGE_API = "https://qianfan.baidubce.com/v2/images/generations";

    /**
     * 为新闻生成图片
     *
     * @param newsTitle   新闻标题
     * @param newsSummary 新闻摘要
     * @return 生成的图片文件路径
     */
    public String generateImageForNews(String newsTitle, String newsSummary) {
        try {
            // 构建图片生成的提示词
            String prompt = buildImagePrompt(newsTitle, newsSummary);
            log.info("开始为新闻生成图片，提示词: {}", prompt);

            // 调用百度千帆生成图片
            String imageUrl = generateImage(prompt);

            // 下载并保存图片到本地
            String savedImagePath = downloadAndSaveImage(imageUrl, newsTitle);

            log.info("新闻图片生成成功，保存路径: {}", savedImagePath);
            return savedImagePath;

        } catch (Exception e) {
            log.error("新闻图片生成失败", e);
            return null;
        }
    }

    /**
     * 构建图片生成的提示词
     */
    private String buildImagePrompt(String title, String summary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("新闻配图：");
        prompt.append(title);

        if (summary != null && !summary.isEmpty()) {
            String briefSummary = summary.length() > 100 ? summary.substring(0, 100) : summary;
            prompt.append("，").append(briefSummary);
        }

        prompt.append("，专业新闻摄影风格，高质量，写实风格");

        return prompt.toString();
    }

    /**
     * 调用百度千帆生成图片
     *
     * @param prompt 图片描述提示词
     * @return 图片URL
     */
    private String generateImage(String prompt) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", imageModel);
            requestBody.put("prompt", prompt);
            requestBody.put("n", 1);
            requestBody.put("size", "1024x1024");
            requestBody.put("watermark", false);

            // 设置请求头 - 直接使用API Key作为Bearer Token
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            log.info("调用千帆API，模型: {}, 提示词: {}", imageModel, prompt);

            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                    QIANFAN_IMAGE_API,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.info("千帆API响应状态: {}", response.getStatusCode());

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            
            if (jsonNode.has("data")) {
                JsonNode dataArray = jsonNode.get("data");
                if (dataArray.isArray() && dataArray.size() > 0) {
                    JsonNode firstImage = dataArray.get(0);
                    if (firstImage.has("url")) {
                        String imageUrl = firstImage.get("url").asText();
                        log.info("成功获取图片URL");
                        return imageUrl;
                    }
                }
            }

            // 检查是否有错误信息
            if (jsonNode.has("error_code") || jsonNode.has("code")) {
                String errorCode = jsonNode.has("error_code") ? 
                    jsonNode.get("error_code").asText() : jsonNode.get("code").asText();
                String errorMsg = jsonNode.has("error_msg") ? 
                    jsonNode.get("error_msg").asText() : 
                    (jsonNode.has("message") ? jsonNode.get("message").asText() : "未知错误");
                throw new RuntimeException("千帆API返回错误 [" + errorCode + "]: " + errorMsg);
            }

            throw new RuntimeException("图片生成失败，未返回图片URL: " + response.getBody());

        } catch (Exception e) {
            log.error("调用百度千帆图片生成API失败", e);
            throw new RuntimeException("图片生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 下载并保存图片到本地
     *
     * @param imageUrl  图片URL
     * @param newsTitle 新闻标题（用于文件名）
     * @return 保存后的文件路径
     */
    private String downloadAndSaveImage(String imageUrl, String newsTitle) {
        try {
            // 确保保存目录存在
            Path saveDir = Paths.get(imageSavePath);
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
                log.info("创建图片保存目录: {}", imageSavePath);
            }

            // 生成文件名
            String fileName = generateFileName(newsTitle);
            Path filePath = saveDir.resolve(fileName);

            log.info("开始下载图片: {}", imageUrl);

            // 使用Java原生URL连接下载（绕过Spring RestTemplate的认证问题）
            java.net.URL url = new java.net.URL(imageUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            int responseCode = connection.getResponseCode();
            log.info("图片下载响应码: {}", responseCode);
            
            if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                try (InputStream inputStream = connection.getInputStream();
                     FileOutputStream outputStream = new FileOutputStream(filePath.toFile())) {
                    
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    long totalBytes = 0;
                    
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                        totalBytes += bytesRead;
                    }
                    
                    log.info("图片下载成功，大小: {} bytes, 保存到: {}", totalBytes, filePath);
                    return filePath.toString();
                }
            } else {
                // 如果直接下载失败，尝试使用带认证的请求
                log.warn("直接下载失败 ({}), 尝试使用认证方式下载", responseCode);
                return downloadWithAuth(imageUrl, filePath);
            }

        } catch (Exception e) {
            log.error("保存图片失败", e);
            throw new RuntimeException("保存图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用认证方式下载图片
     */
    private String downloadWithAuth(String imageUrl, Path filePath) {
        try {
            // 构建带认证的请求
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    imageUrl,
                    HttpMethod.GET,
                    entity,
                    byte[].class
            );
            
            byte[] imageBytes = response.getBody();
            if (imageBytes != null && imageBytes.length > 0) {
                Files.write(filePath, imageBytes);
                log.info("图片已保存到(认证方式): {}", filePath);
                return filePath.toString();
            } else {
                throw new RuntimeException("下载的图片数据为空");
            }
        } catch (Exception e) {
            log.error("使用认证方式下载图片失败", e);
            throw new RuntimeException("下载图片失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成文件名
     */
    private String generateFileName(String newsTitle) {
        // 使用UUID和时间戳确保文件名唯一
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        // 清理标题中的非法文件名字符
        String cleanTitle = newsTitle.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_");
        if (cleanTitle.length() > 50) {
            cleanTitle = cleanTitle.substring(0, 50);
        }

        return String.format("news_%s_%s_%s.png", timestamp, uuid, cleanTitle);
    }

    /**
     * 批量为新闻生成图片
     *
     * @param newsList 新闻列表，每个新闻包含标题和摘要
     * @return 生成的图片路径列表
     */
    public java.util.List<String> generateImagesForNewsList(
            java.util.List<NewsTitleSummary> newsList) {
        java.util.List<String> imagePaths = new java.util.ArrayList<>();
        
        for (NewsTitleSummary news : newsList) {
            String imagePath = generateImageForNews(news.getTitle(), news.getSummary());
            if (imagePath != null) {
                imagePaths.add(imagePath);
            }
        }
        
        return imagePaths;
    }

    /**
     * 新闻标题和摘要的简单封装类
     */
    public static class NewsTitleSummary {
        private String title;
        private String summary;

        public NewsTitleSummary(String title, String summary) {
            this.title = title;
            this.summary = summary;
        }

        public String getTitle() {
            return title;
        }

        public String getSummary() {
            return summary;
        }
    }
}
