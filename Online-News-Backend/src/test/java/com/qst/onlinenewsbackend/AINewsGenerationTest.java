package com.qst.onlinenewsbackend;

import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.service.AINewsGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

/**
 * AI新闻生成测试类
 * 测试DeepSeek生成新闻内容 + 百度千帆生成配图的完整流程
 *
 * @author QST
 * @since 1.0
 */
@SpringBootTest
public class AINewsGenerationTest {

    @Autowired
    private AINewsGenerationService aiNewsGenerationService;

    /**
     * 测试单条新闻生成（含配图）
     */
    @Test
    public void testGenerateSingleNewsWithImage() {
        System.out.println("========== 开始测试AI生成单条新闻（含配图） ==========");

        String topic = "人工智能在医疗领域的突破性应用";
        Integer categoryId = 1;  // 科技类
        Integer authorId = 1;    // AI作者

        System.out.println("生成主题: " + topic);
        System.out.println("分类ID: " + categoryId);
        System.out.println("作者ID: " + authorId);
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, categoryId, authorId);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            System.out.println("内容长度: " + (news.getContent() != null ? news.getContent().length() : 0) + " 字");
            System.out.println("来源: " + news.getSource());
            System.out.println("配图: " + (news.getCoverImage() != null ? news.getCoverImage() : "无"));
            System.out.println();
            
            if (news.getCoverImage() != null) {
                System.out.println("✓ 配图生成成功: " + news.getCoverImage());
            } else {
                System.out.println(" 配图生成失败（不影响新闻发布）");
            }
        } else {
            System.out.println("✗ 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试批量新闻生成（含配图）
     */
    @Test
    public void testGenerateBatchNewsWithImages() {
        System.out.println("========== 开始测试AI批量生成新闻（含配图） ==========");

        List<String> topics = Arrays.asList(
                "5G技术在智慧城市中的应用",
                "新能源汽车市场快速增长",
                "区块链技术赋能金融行业",
                "量子计算取得重大突破",
                "元宇宙概念持续升温"
        );

        Integer categoryId = 1;  // 科技类
        Integer authorId = 1;

        System.out.println("生成主题数量: " + topics.size());
        for (int i = 0; i < topics.size(); i++) {
            System.out.println((i + 1) + ". " + topics.get(i));
        }
        System.out.println();

        List<News> newsList = aiNewsGenerationService.generateNewsBatchWithImages(
                topics, categoryId, authorId
        );

        System.out.println("生成结果:");
        System.out.println("成功生成 " + newsList.size() + " 条新闻");
        System.out.println();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);
            System.out.println((i + 1) + ". " + news.getTitle());
            System.out.println("   摘要: " + news.getSummary());
            System.out.println("   配图: " + (news.getCoverImage() != null ? "✓ 已生成" : "✗ 未生成"));
            System.out.println();
        }

        System.out.println("========== 批量测试完成 ==========");
    }

    /**
     * 测试基于热点话题生成新闻
     */
    @Test
    public void testGenerateNewsFromHotTopics() {
        System.out.println("========== 开始测试基于热点话题生成新闻 ==========");

        List<AINewsGenerationService.HotTopic> hotTopics = Arrays.asList(
                createHotTopic("ChatGPT发布最新版本", "OpenAI发布新一代AI模型，性能大幅提升"),
                createHotTopic("SpaceX星舰试飞成功", "SpaceX星舰完成首次轨道级试飞"),
                createHotTopic("苹果发布Vision Pro 2", "苹果第二代混合现实头显正式发布")
        );

        Integer categoryId = 1;
        Integer authorId = 1;

        System.out.println("热点话题数量: " + hotTopics.size());
        for (int i = 0; i < hotTopics.size(); i++) {
            System.out.println((i + 1) + ". " + hotTopics.get(i).getTitle());
        }
        System.out.println();

        List<News> newsList = aiNewsGenerationService.generateNewsFromHotTopics(
                hotTopics, categoryId, authorId
        );

        System.out.println("生成结果:");
        System.out.println("成功生成 " + newsList.size() + " 条新闻");
        System.out.println();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);
            System.out.println((i + 1) + ". " + news.getTitle());
            System.out.println("   配图: " + (news.getCoverImage() != null ? "✓ " + news.getCoverImage() : "✗ 未生成"));
            System.out.println();
        }

        System.out.println("========== 热点话题测试完成 ==========");
    }

    /**
     * 测试不同分类的新闻生成
     */
    @Test
    public void testGenerateNewsForDifferentCategories() {
        System.out.println("========== 开始测试不同分类的新闻生成 ==========");

        // 科技类
        System.out.println("\n【科技类新闻】");
        News techNews = aiNewsGenerationService.generateNewsWithImage(
                "AI大模型技术最新进展", 1, 1
        );
        printNewsInfo(techNews);

        // 体育类
        System.out.println("\n【体育类新闻】");
        News sportsNews = aiNewsGenerationService.generateNewsWithImage(
                "中国队在奥运会上再创佳绩", 2, 1
        );
        printNewsInfo(sportsNews);

        // 财经类
        System.out.println("\n【财经类新闻】");
        News financeNews = aiNewsGenerationService.generateNewsWithImage(
                "股市迎来新一轮上涨行情", 3, 1
        );
        printNewsInfo(financeNews);

        System.out.println("========== 分类测试完成 ==========");
    }

    /**
     * 创建热点话题
     */
    private AINewsGenerationService.HotTopic createHotTopic(String title, String summary) {
        AINewsGenerationService.HotTopic topic = new AINewsGenerationService.HotTopic();
        topic.setTitle(title);
        topic.setSummary(summary);
        return topic;
    }

    /**
     * 打印新闻信息
     */
    private void printNewsInfo(News news) {
        if (news != null) {
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            System.out.println("配图: " + (news.getCoverImage() != null ? "✓ " + news.getCoverImage() : "✗ 未生成"));
        } else {
            System.out.println("✗ 新闻生成失败");
        }
    }
}
