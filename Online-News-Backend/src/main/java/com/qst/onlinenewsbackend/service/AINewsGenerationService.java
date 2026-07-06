package com.qst.onlinenewsbackend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;
import com.qst.onlinenewsbackend.service.TagService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI新闻生成服务
 * 结合DeepSeek（生成新闻内容）和百度千帆（生成配图）
 * 实现一键生成完整新闻（标题+摘要+内容+配图）
 *
 * @author QST
 * @since 1.0
 */
@Slf4j
@Service
public class AINewsGenerationService {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ImageGenerationService imageGenerationService;

    @Autowired
    private TagService tagService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 预定义的标签类别
    private static final List<String> PREDEFINED_TAGS = List.of("体育", "财经", "科技", "娱乐");

    /**
     * 根据主题生成单条新闻（含配图和标签）
     *
     * @param topic 新闻主题
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 完整的新闻对象（包含配图路径和标签）
     */
    public News generateNewsWithImage(String topic, Integer categoryId, Integer authorId) {
        long startTime = System.currentTimeMillis();
        
        log.info("========================================");
        log.info("开始AI新闻生成流程");
        log.info("主题: {}", topic);
        log.info("分类ID: {}, 作者ID: {}", categoryId, authorId);
        log.info("========================================");

        try {
            // 1. 使用DeepSeek生成新闻内容（含自动标签）
            log.info("【步骤1/3】调用DeepSeek生成新闻内容...");
            long deepseekStartTime = System.currentTimeMillis();
            
            News news = generateNewsContentWithTags(topic, categoryId, authorId);
            
            long deepseekEndTime = System.currentTimeMillis();
            long deepseekDuration = deepseekEndTime - deepseekStartTime;
            
            if (news == null) {
                log.error("【失败】DeepSeek生成新闻内容失败");
                return null;
            }

            log.info("【成功】DeepSeek生成新闻内容完成");
            log.info("  - 标题: {}", news.getTitle());
            log.info("  - 摘要: {}", news.getSummary());
            log.info("  - 内容长度: {} 字", news.getContent() != null ? news.getContent().length() : 0);
            log.info("  - 标签数: {}", news.getTags() != null ? news.getTags().size() : 0);
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                String tagNames = news.getTags().stream()
                        .map(Tag::getTagName)
                        .collect(Collectors.joining(", "));
                log.info("  - 标签列表: {}", tagNames);
            }
            log.info("  - 耗时: {} ms", deepseekDuration);

            // 2. 使用百度千帆生成配图
            log.info("【步骤2/3】调用百度千帆生成配图...");
            long imageStartTime = System.currentTimeMillis();
            
            try {
                String imagePath = imageGenerationService.generateImageForNews(
                        news.getTitle(),
                        news.getSummary()
                );
                
                long imageEndTime = System.currentTimeMillis();
                long imageDuration = imageEndTime - imageStartTime;
                
                if (imagePath != null) {
                    news.setCoverImage(imagePath);
                    log.info("【成功】百度千帆生成配图完成");
                    log.info("  - 图片路径: {}", imagePath);
                    log.info("  - 耗时: {} ms", imageDuration);
                } else {
                    log.warn("【警告】百度千帆配图生成失败，新闻将没有配图");
                    log.info("  - 耗时: {} ms", imageDuration);
                }
            } catch (Exception e) {
                long imageEndTime = System.currentTimeMillis();
                long imageDuration = imageEndTime - imageStartTime;
                log.error("【错误】百度千帆配图生成异常，但不影响新闻发布");
                log.error("  - 错误信息: {}", e.getMessage());
                log.info("  - 耗时: {} ms", imageDuration);
            }

            // 3. 完成
            long endTime = System.currentTimeMillis();
            long totalDuration = endTime - startTime;
            
            log.info("【步骤3/3】新闻生成流程完成");
            log.info("========================================");
            log.info("生成结果摘要:");
            log.info("  - 标题: {}", news.getTitle());
            log.info("  - 标签: {}", 
                    news.getTags() != null ? 
                    news.getTags().stream().map(Tag::getTagName).collect(Collectors.joining(", ")) : 
                    "无");
            log.info("  - 配图: {}", news.getCoverImage() != null ? "已生成" : "未生成");
            log.info("  - 总耗时: {} ms ({} 秒)", totalDuration, totalDuration / 1000.0);
            log.info("========================================");

            return news;

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            long totalDuration = endTime - startTime;
            log.error("【失败】AI新闻生成流程异常");
            log.error("  - 主题: {}", topic);
            log.error("  - 总耗时: {} ms", totalDuration);
            log.error("  - 错误信息: {}", e.getMessage(), e);
            log.info("========================================");
            return null;
        }
    }

    /**
     * 批量生成新闻（含配图）
     *
     * @param topics 新闻主题列表
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 新闻列表
     */
    public List<News> generateNewsBatchWithImages(List<String> topics, Integer categoryId, Integer authorId) {
        long batchStartTime = System.currentTimeMillis();
        
        log.info("========================================");
        log.info("开始批量AI新闻生成");
        log.info("主题数量: {}", topics.size());
        for (int i = 0; i < topics.size(); i++) {
            log.info("  {}. {} ", i + 1, topics.get(i));
        }
        log.info("========================================");
        
        List<News> newsList = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i);
            log.info("");
            log.info("【批量进度 {}/{}】开始生成: {}", i + 1, topics.size(), topic);
            
            try {
                News news = generateNewsWithImage(topic, categoryId, authorId);
                if (news != null) {
                    newsList.add(news);
                    successCount++;
                    log.info("【批量进度 {}/{}】成功: {}", i + 1, topics.size(), news.getTitle());
                } else {
                    failCount++;
                    log.warn("【批量进度 {}/{}】失败: {}", i + 1, topics.size(), topic);
                }
            } catch (Exception e) {
                failCount++;
                log.error("【批量进度 {}/{}】异常: {}", i + 1, topics.size(), topic, e);
            }
        }
        
        long batchEndTime = System.currentTimeMillis();
        long batchDuration = batchEndTime - batchStartTime;
        
        log.info("");
        log.info("========================================");
        log.info("批量生成完成");
        log.info("  - 总主题数: {}", topics.size());
        log.info("  - 成功: {} 条", successCount);
        log.info("  - 失败: {} 条", failCount);
        log.info("  - 成功率: {}%", (successCount * 100.0 / topics.size()));
        log.info("  - 总耗时: {} ms ({} 秒)", batchDuration, batchDuration / 1000.0);
        log.info("  - 平均每条: {} ms", topics.size() > 0 ? batchDuration / topics.size() : 0);
        log.info("========================================");
        
        return newsList;
    }

    /**
     * 根据热点话题生成新闻（含配图）
     *
     * @param hotTopics 热点话题列表
     * @param categoryId 分类ID
     * @param authorId 作者ID
     * @return 新闻列表
     */
    public List<News> generateNewsFromHotTopics(List<HotTopic> hotTopics, Integer categoryId, Integer authorId) {
        List<News> newsList = new ArrayList<>();
        
        for (HotTopic topic : hotTopics) {
            try {
                News news = generateNewsWithImage(
                        topic.getTitle(), 
                        categoryId, 
                        authorId
                );
                
                // 如果DeepSeek生成的标题更好，可以覆盖
                if (news != null && topic.getSummary() != null) {
                    // 保留DeepSeek生成的内容，但可以使用热点的摘要作为补充
                    log.info("基于热点话题生成新闻: {}", news.getTitle());
                }
                
                if (news != null) {
                    newsList.add(news);
                }
            } catch (Exception e) {
                log.error("基于热点生成新闻失败: {}", topic.getTitle(), e);
            }
        }
        
        return newsList;
    }

    /**
     * 使用DeepSeek生成新闻内容（含自动标签）
     */
    private News generateNewsContentWithTags(String topic, Integer categoryId, Integer authorId) {
        try {
            ChatClient chatClient = ChatClient.builder(chatModel).build();

            String prompt = String.format("""
                请根据以下主题生成一条完整的新闻，并自动为其打上合适的标签。
                主题：%s
                
                可选标签类别：[%s]
                
                严格按照以下JSON格式返回，不要添加任何其他文字、markdown标记或代码块标记：
                {
                    "title": "新闻标题（20-50字）",
                    "summary": "新闻摘要（50-100字）",
                    "content": "新闻详细内容（300-500字，分段描述）",
                    "source": "新闻来源（如：AI生成）",
                    "tags": ["标签1", "标签2"]  // 从可选标签中选择1-2个最相关的
                }
                
                要求：
                1. 标题要吸引人，概括性强
                2. 摘要简洁明了，突出核心内容
                3. 内容详实，结构清晰，分段描述
                4. 语言专业、客观、准确
                5. 标签必须从可选标签类别中选择，选择1-2个最相关的
                6. 不要使用markdown格式
                
                标签选择规则：
                - 科技：人工智能、互联网、5G、区块链、量子计算等技术相关
                - 财经：股市、经济、金融、投资、市场等财经相关
                - 体育：奥运会、足球、篮球、运动员、比赛等体育相关
                - 娱乐：电影、音乐、明星、综艺、演艺等娱乐相关
                """, topic, String.join(", ", PREDEFINED_TAGS));

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("DeepSeek响应: {}", response);

            // 解析JSON响应
            NewsDataWithTags newsData = objectMapper.readValue(response, NewsDataWithTags.class);
            
            // 构建News对象
            News news = new News();
            news.setTitle(newsData.getTitle());
            news.setSummary(newsData.getSummary());
            news.setContent(newsData.getContent());
            news.setSource(newsData.getSource() != null ? newsData.getSource() : "AI生成");
            news.setCategoryId(categoryId);
            news.setAuthorId(authorId);
            news.setViewCount(0);
            news.setLikeCount(0);
            news.setCollectCount(0);
            news.setCommentCount(0);

            // 处理标签
            if (newsData.getTags() != null && !newsData.getTags().isEmpty()) {
                List<Tag> tags = getOrCreateTags(newsData.getTags());
                news.setTags(tags);
                log.info("自动打上标签: {}", tags.stream().map(Tag::getTagName).collect(Collectors.joining(", ")));
            }

            return news;

        } catch (Exception e) {
            log.error("DeepSeek生成新闻内容失败", e);
            return null;
        }
    }

    /**
     * 获取或创建标签
     */
    private List<Tag> getOrCreateTags(List<String> tagNames) {
        List<Tag> tags = new ArrayList<>();
        
        for (String tagName : tagNames) {
            // 查找是否已存在
            Tag existingTag = tagService.lambdaQuery()
                    .eq(Tag::getTagName, tagName)
                    .one();
            
            if (existingTag != null) {
                tags.add(existingTag);
                log.debug("标签已存在: {}", tagName);
            } else {
                // 创建新标签
                Tag newTag = new Tag();
                newTag.setTagName(tagName);
                tagService.save(newTag);
                tags.add(newTag);
                log.info("创建新标签: {}", tagName);
            }
        }
        
        return tags;
    }

    /**
     * 新闻数据DTO（含标签）
     */
    @Data
    public static class NewsDataWithTags {
        private String title;
        private String summary;
        private String content;
        private String source;
        private List<String> tags;  // 标签名称列表
    }

    /**
     * 新闻数据DTO（不含标签，用于兼容）
     */
    @Data
    public static class NewsData {
        private String title;
        private String summary;
        private String content;
        private String source;
    }

    /**
     * 热点话题DTO
     */
    @Data
    public static class HotTopic {
        private String title;
        private String summary;
        private String content;
    }
}
