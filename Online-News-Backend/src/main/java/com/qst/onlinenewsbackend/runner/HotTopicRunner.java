package com.qst.onlinenewsbackend.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.onlinenewsbackend.entity.Category;
import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;
import com.qst.onlinenewsbackend.entity.User;
import com.qst.onlinenewsbackend.enums.NewsStatus;
import com.qst.onlinenewsbackend.enums.UserRole;
import com.qst.onlinenewsbackend.mapper.TagMapper;
import com.qst.onlinenewsbackend.service.CategoryService;
import com.qst.onlinenewsbackend.service.ImageGenerationService;
import com.qst.onlinenewsbackend.service.NewsService;
import com.qst.onlinenewsbackend.service.TagService;
import com.qst.onlinenewsbackend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 热点话题自动获取启动器
 * <p>
 * 项目每次启动时，自动执行以下流程：
 * 1. 调用 DeepSeek 获取当前热点新闻话题
 * 2. 自动分析内容并打上标签（体育/财经/科技/娱乐）
 * 3. 调用百度千帆为每条新闻生成配图
 * 4. 将完整新闻（标题+摘要+内容+标签+配图）存入数据库
 * 5. 详细记录每个步骤的日志
 * </p>
 */
@Slf4j
@Component
public class HotTopicRunner implements ApplicationRunner {

    @Autowired
    private ChatModel chatModel;
    @Autowired
    private NewsService newsService;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private UserService userService;
    @Autowired
    private TagService tagService;
    @Autowired
    private ImageGenerationService imageGenerationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 预定义的标签类别
    private static final List<String> PREDEFINED_TAGS = List.of("体育", "财经", "科技", "娱乐", "健康", "教育");

    // 预定义的新闻分类（名称, 图标, 排序）
    private static final Object[][] CATEGORIES = {
        {"国内", "🇨🇳", 1},
        {"国际", "🌍", 2},
        {"财经", "💰", 3},
        {"科技", "💻", 4},
        {"体育", "⚽", 5},
        {"娱乐", "🎬", 6},
        {"健康", "🩺", 7},
        {"教育", "📚", 8},
    };

    @Override
    public void run(ApplicationArguments args) {
        long runnerStartTime = System.currentTimeMillis();
        
        log.info("========================================");
        log.info("项目启动 - 开始自动获取热点并生成新闻");
        log.info("========================================");
        
        try {
            // 1. 确保有系统用户和全部分类
            log.info("【步骤1/4】准备系统用户和分类...");
            User systemUser = getOrCreateSystemUser();
            Map<String, Category> categoryMap = getOrCreateAllCategories();
            log.info("系统用户: {} (ID: {})", systemUser.getUsername(), systemUser.getId());
            log.info("已就绪 {} 个分类", categoryMap.size());

            // 2. 调用 DeepSeek 获取热点话题
            log.info("【步骤2/4】调用DeepSeek获取热点新闻话题...");
            long deepseekStartTime = System.currentTimeMillis();
            
            List<Map<String, String>> topics = fetchHotTopicsFromDeepSeek();
            
            long deepseekEndTime = System.currentTimeMillis();
            long deepseekDuration = deepseekEndTime - deepseekStartTime;
            
            if (topics == null || topics.isEmpty()) {
                log.warn("【警告】DeepSeek 返回的热点数据为空，跳过后续处理");
                return;
            }

            log.info("【成功】DeepSeek返回 {} 条热点话题", topics.size());
            log.info("  - 耗时: {} ms", deepseekDuration);
            for (int i = 0; i < topics.size(); i++) {
                log.info("  {}. {}", i + 1, topics.get(i).get("title"));
            }

            // 3. 处理每条热点：生成标签、生成配图、保存到数据库
            log.info("【步骤3/4】开始处理热点话题（打标签+生成配图+保存）...");
            long processStartTime = System.currentTimeMillis();
            
            int savedCount = 0;
            int skippedCount = 0;
            int failedCount = 0;
            
            for (int i = 0; i < topics.size(); i++) {
                Map<String, String> topic = topics.get(i);
                String title = topic.get("title");
                String summary = topic.get("summary");
                String content = topic.get("content");

                log.info("");
                log.info("【进度 {}/{}】处理热点: {}", i + 1, topics.size(), title);

                if (title == null || title.isBlank()) {
                    log.warn("  标题为空，跳过");
                    skippedCount++;
                    continue;
                }

                // 检查标题是否已存在（避免重复）
                long existCount = newsService.lambdaQuery()
                        .eq(News::getTitle, title)
                        .count();
                if (existCount > 0) {
                    log.info("  热点已存在，跳过");
                    skippedCount++;
                    continue;
                }

                try {
                    // 3.1 创建新闻对象
                    News news = new News();
                    news.setTitle(title);
                    news.setSummary(summary != null ? summary : "");
                    news.setContent(content != null ? content : title);
                    news.setSource("AI热点");
                    news.setStatus(NewsStatus.PUBLISHED);
                    news.setPublishTime(LocalDateTime.now());

                    // 根据内容自动确定分类
                    String categoryName = determineCategory(title, summary);
                    Category matchedCategory = categoryMap.getOrDefault(categoryName, categoryMap.get("科技"));
                    news.setCategoryId(matchedCategory.getId());
                    log.info("  【分类】自动归类为: {}", matchedCategory.getName());

                    news.setAuthorId(systemUser.getId());
                    news.setViewCount(0);
                    news.setLikeCount(0);
                    news.setCollectCount(0);
                    news.setCommentCount(0);

                    // 3.2 自动打上标签
                    log.info("  【子步骤1】分析内容并自动打标签...");
                    List<String> autoTags = analyzeAndSelectTags(title, summary);
                    List<Tag> tags = getOrCreateTags(autoTags);
                    news.setTags(tags);
                    log.info("  【成功】自动打上标签: {}", 
                            tags.stream().map(Tag::getTagName).collect(Collectors.joining(", ")));

                    // 3.3 生成配图
                    log.info("  【子步骤2】调用百度千帆生成配图...");
                    long imageStartTime = System.currentTimeMillis();
                    
                    try {
                        String imagePath = imageGenerationService.generateImageForNews(title, summary);
                        long imageEndTime = System.currentTimeMillis();
                        long imageDuration = imageEndTime - imageStartTime;
                        
                        if (imagePath != null) {
                            news.setCoverImage(imagePath);
                            log.info("  【成功】配图生成完成");
                            log.info("    - 图片路径: {}", imagePath);
                            log.info("    - 耗时: {} ms", imageDuration);
                        } else {
                            log.warn("  【警告】配图生成失败，新闻将没有配图");
                            log.info("    - 耗时: {} ms", imageDuration);
                        }
                    } catch (Exception e) {
                        long imageEndTime = System.currentTimeMillis();
                        long imageDuration = imageEndTime - imageStartTime;
                        log.error("  【错误】配图生成异常");
                        log.error("    - 错误信息: {}", e.getMessage());
                        log.info("    - 耗时: {} ms", imageDuration);
                    }

                    // 3.4 保存到数据库（MyBatis-Plus 保存新闻，手动维护 news_tags 关联）
                    log.info("  【子步骤3】保存到数据库...");
                    newsService.save(news);
                    // 手动维护多对多标签关联
                    if (news.getTags() != null && !news.getTags().isEmpty()) {
                        for (Tag tag : news.getTags()) {
                            tagMapper.insertNewsTag(news.getId(), tag.getId());
                        }
                    }
                    savedCount++;
                    log.info("  【成功】热点已保存: {}", title);

                } catch (Exception e) {
                    failedCount++;
                    log.error("  【失败】处理热点异常: {}", title, e);
                }
            }
            
            long processEndTime = System.currentTimeMillis();
            long processDuration = processEndTime - processStartTime;

            // 4. 完成统计
            long runnerEndTime = System.currentTimeMillis();
            long totalDuration = runnerEndTime - runnerStartTime;

            log.info("");
            log.info("【步骤4/4】热点获取与处理完成");
            log.info("========================================");
            log.info("执行结果统计:");
            log.info("  - 总热点数: {}", topics.size());
            log.info("  - 成功保存: {} 条", savedCount);
            log.info("  - 跳过重复: {} 条", skippedCount);
            log.info("  - 处理失败: {} 条", failedCount);
            log.info("  - 成功率: {}%", (savedCount * 100.0 / topics.size()));
            log.info("性能统计:");
            log.info("  - DeepSeek获取: {} ms", deepseekDuration);
            log.info("  - 处理与保存: {} ms", processDuration);
            log.info("  - 总耗时: {} ms ({} 秒)", totalDuration, totalDuration / 1000.0);
            log.info("  - 平均每条: {} ms", topics.size() > 0 ? processDuration / topics.size() : 0);
            log.info("========================================");

        } catch (Exception e) {
            long runnerEndTime = System.currentTimeMillis();
            long totalDuration = runnerEndTime - runnerStartTime;
            log.error("【失败】获取和处理热点异常");
            log.error("  - 总耗时: {} ms", totalDuration);
            log.error("  - 错误信息: {}", e.getMessage(), e);
            log.info("========================================");
            log.warn("项目正常启动不受影响，热点获取失败可稍后手动触发");
        }
    }

    /**
     * 从 DeepSeek 响应中提取 JSON 内容（去除可能的 markdown 代码块包裹）
     */
    private String extractJson(String response) {
        if (response == null) return "[]";
        String trimmed = response.trim();

        // 去除 markdown 代码块标记
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        trimmed = trimmed.trim();

        // 找到 JSON 数组的起始和结束位置
        int start = trimmed.indexOf('[');
        int end = trimmed.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return trimmed;
    }

    /**
     * 调用 DeepSeek 获取热点话题
     */
    private List<Map<String, String>> fetchHotTopicsFromDeepSeek() {
        try {
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            String prompt = """
                    请列出10条当前的热门新闻话题，涵盖国内、国际、科技、财经、体育、娱乐等不同领域。
                    严格按照以下JSON数组格式返回，不要添加任何其他文字、markdown标记或代码块标记：
                    [{"title":"新闻标题","summary":"50字以内的摘要","content":"200字以内的详细内容"}]
                    """;

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("DeepSeek 响应长度: {} 字符", response.length());
            log.debug("DeepSeek 原始响应: {}", response);

            // 解析 JSON（处理可能的 markdown 包裹）
            String jsonStr = extractJson(response);
            List<Map<String, String>> topics = objectMapper.readValue(jsonStr,
                    new TypeReference<List<Map<String, String>>>() {});

            return topics;
        } catch (Exception e) {
            log.error("调用DeepSeek获取热点失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据标题和摘要分析并选择合适的标签
     */
    private List<String> analyzeAndSelectTags(String title, String summary) {
        List<String> selectedTags = new ArrayList<>();
        
        String text = (title + " " + (summary != null ? summary : "")).toLowerCase();
        
        // 科技类关键词
        if (text.contains("ai") || text.contains("人工智能") || text.contains("科技") ||
            text.contains("互联网") || text.contains("5g") || text.contains("区块链") ||
            text.contains("芯片") || text.contains("量子") || text.contains("云计算") ||
            text.contains("大数据") || text.contains("算法")) {
            selectedTags.add("科技");
        }
        
        // 财经类关键词
        if (text.contains("股票") || text.contains("股市") || text.contains("财经") ||
            text.contains("经济") || text.contains("金融") || text.contains("投资") ||
            text.contains("银行") || text.contains("央行") || text.contains("降准") ||
            text.contains("加息") || text.contains("gdp") || text.contains("通胀")) {
            selectedTags.add("财经");
        }
        
        // 体育类关键词
        if (text.contains("奥运") || text.contains("足球") || text.contains("篮球") ||
            text.contains("nba") || text.contains("cba") || text.contains("比赛") ||
            text.contains("冠军") || text.contains("金牌") || text.contains("运动员") ||
            text.contains("世界杯") || text.contains("亚运会")) {
            selectedTags.add("体育");
        }
        
        // 娱乐类关键词
        if (text.contains("电影") || text.contains("电视剧") || text.contains("综艺") ||
            text.contains("明星") || text.contains("歌手") || text.contains("音乐") ||
            text.contains("票房") || text.contains("春晚") || text.contains("颁奖")) {
            selectedTags.add("娱乐");
        }

        // 健康类关键词
        if (text.contains("健康") || text.contains("医疗") || text.contains("疫情") ||
            text.contains("疫苗") || text.contains("病毒") || text.contains("药品") ||
            text.contains("疾病") || text.contains("医院") || text.contains("卫生") ||
            text.contains("养生") || text.contains("中医") || text.contains("心理")) {
            selectedTags.add("健康");
        }

        // 教育类关键词
        if (text.contains("教育") || text.contains("学校") || text.contains("高考") ||
            text.contains("大学") || text.contains("考试") || text.contains("学生") ||
            text.contains("教师") || text.contains("考研") || text.contains("留学") ||
            text.contains("课程") || text.contains("培训") || text.contains("科研")) {
            selectedTags.add("教育");
        }

        // 如果没有匹配到任何标签，默认打上"科技"
        if (selectedTags.isEmpty()) {
            selectedTags.add("科技");

        }
        
        // 限制最多2个标签
        if (selectedTags.size() > 2) {
            selectedTags = selectedTags.subList(0, 2);
        }
        
        return selectedTags;
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
                log.debug("    标签已存在: {}", tagName);
            } else {
                // 创建新标签
                Tag newTag = new Tag();
                newTag.setTagName(tagName);
                tagService.save(newTag);
                tags.add(newTag);
                log.info("    创建新标签: {}", tagName);
            }
        }
        
        return tags;
    }

    /**
     * 获取或创建系统用户（用于 AI 生成新闻的作者）
     */
    private User getOrCreateSystemUser() {
        User systemUser = userService.lambdaQuery()
                .eq(User::getEmail, "ai@system.com")
                .one();

        if (systemUser == null) {
            systemUser = new User();
            systemUser.setUsername("ai_system");
            systemUser.setPassword("system_generated");
            systemUser.setEmail("ai@system.com");
            systemUser.setAvatar("C:\\images\\avator.png");
            systemUser.setRole(UserRole.READER);
            userService.save(systemUser);
            log.info("已创建系统用户: ai_system");
        }

        return systemUser;
    }

    /**
     * 获取或创建全部分类（国内、国际、财经、科技、体育、娱乐、健康、教育）
     */
    private Map<String, Category> getOrCreateAllCategories() {
        Map<String, Category> result = new java.util.LinkedHashMap<>();

        for (Object[] cat : CATEGORIES) {
            String name = (String) cat[0];
            String icon = (String) cat[1];
            int sortOrder = (int) cat[2];

            Category category = categoryService.lambdaQuery()
                    .eq(Category::getName, name)
                    .one();

            if (category == null) {
                category = new Category();
                category.setName(name);
                category.setIcon(icon);
                category.setSortOrder(sortOrder);
                categoryService.save(category);
                log.info("已创建分类: {}", name);
            }
            result.put(name, category);
        }
        return result;
    }

    /**
     * 根据标题和摘要确定新闻分类
     */
    private String determineCategory(String title, String summary) {
        String text = (title + " " + (summary != null ? summary : ""));

        // 国内特征
        if (text.contains("中国") || text.contains("国内") || text.contains("我国")
                || text.contains("北京") || text.contains("上海") || text.contains("深圳")
                || text.contains("政府") || text.contains("政策") || text.contains("国务院")
                || text.contains("两会") || text.contains("政协")) {
            return "国内";
        }
        // 国际特征
        if (text.contains("国际") || text.contains("美国") || text.contains("欧洲")
                || text.contains("日本") || text.contains("韩国") || text.contains("俄罗斯")
                || text.contains("联合国") || text.contains("北约") || text.contains("外交")) {
            return "国际";
        }
        // 财经特征
        if (text.contains("股票") || text.contains("股市") || text.contains("财经")
                || text.contains("经济") || text.contains("金融") || text.contains("投资")
                || text.contains("银行") || text.contains("央行") || text.contains("基金")
                || text.contains("A股") || text.contains("GDP") || text.contains("通胀")
                || text.contains("汇率") || text.contains("贸易战") || text.contains("关税")) {
            return "财经";
        }
        // 体育特征
        if (text.contains("奥运") || text.contains("足球") || text.contains("篮球")
                || text.contains("NBA") || text.contains("CBA") || text.contains("比赛")
                || text.contains("冠军") || text.contains("金牌") || text.contains("运动员")
                || text.contains("世界杯") || text.contains("亚运会") || text.contains("联赛")
                || text.contains("赛季") || text.contains("决赛")) {
            return "体育";
        }
        // 娱乐特征
        if (text.contains("电影") || text.contains("电视剧") || text.contains("综艺")
                || text.contains("明星") || text.contains("歌手") || text.contains("音乐")
                || text.contains("票房") || text.contains("春晚") || text.contains("颁奖")
                || text.contains("演唱会") || text.contains("演员") || text.contains("导演")) {
            return "娱乐";
        }
        // 健康特征
        if (text.contains("健康") || text.contains("医疗") || text.contains("疫情")
                || text.contains("疫苗") || text.contains("病毒") || text.contains("药品")
                || text.contains("疾病") || text.contains("医院") || text.contains("卫生")
                || text.contains("养生") || text.contains("中医药") || text.contains("心理")) {
            return "健康";
        }
        // 教育特征
        if (text.contains("教育") || text.contains("学校") || text.contains("高考")
                || text.contains("大学") || text.contains("考试") || text.contains("学生")
                || text.contains("教师") || text.contains("考研") || text.contains("留学")
                || text.contains("课程") || text.contains("培训") || text.contains("科研")) {
            return "教育";
        }
        // 科技特征（放在最后作为默认，因为科技词汇可能与其他类别重叠）
        if (text.contains("AI") || text.contains("人工智能") || text.contains("科技")
                || text.contains("互联网") || text.contains("5G") || text.contains("区块链")
                || text.contains("芯片") || text.contains("量子") || text.contains("云计算")
                || text.contains("大数据") || text.contains("算法") || text.contains("机器人")
                || text.contains("航天") || text.contains("卫星") || text.contains("新能源")) {
            return "科技";
        }

        // 默认归类为科技
        return "科技";
    }
}
