package com.qst.onlinenewsbackend;

import com.qst.onlinenewsbackend.entity.News;
import com.qst.onlinenewsbackend.entity.Tag;
import com.qst.onlinenewsbackend.service.AINewsGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * AI新闻自动生成标签测试类
 * 测试DeepSeek生成新闻时自动打上标签（体育、财经、科技、娱乐）
 *
 * @author QST
 * @since 1.0
 */
@SpringBootTest
public class AINewsAutoTagTest {

    @Autowired
    private AINewsGenerationService aiNewsGenerationService;

    /**
     * 测试科技类新闻自动打标签
     */
    @Test
    public void testAutoTagForTechNews() {
        System.out.println("========== 测试科技类新闻自动打标签 ==========");

        String topic = "人工智能在医疗领域的突破性应用";
        
        System.out.println("生成主题: " + topic);
        System.out.println("期望标签: 科技");
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, 1, 1);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            System.out.println("配图: " + (news.getCoverImage() != null ? "✓ 已生成" : "✗ 未生成"));
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.println("✓ 自动标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.println("  - " + tag.getTagName() + " (ID: " + tag.getId() + ")");
                }
            } else {
                System.out.println(" 未生成标签");
            }
        } else {
            System.out.println("✗ 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试体育类新闻自动打标签
     */
    @Test
    public void testAutoTagForSportsNews() {
        System.out.println("========== 测试体育类新闻自动打标签 ==========");

        String topic = "中国队在奥运会上再创佳绩，单日斩获3金";
        
        System.out.println("生成主题: " + topic);
        System.out.println("期望标签: 体育");
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, 2, 1);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.println("✓ 自动标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.println("  - " + tag.getTagName() + " (ID: " + tag.getId() + ")");
                }
            } else {
                System.out.println("✗ 未生成标签");
            }
        } else {
            System.out.println("✗ 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试财经类新闻自动打标签
     */
    @Test
    public void testAutoTagForFinanceNews() {
        System.out.println("========== 测试财经类新闻自动打标签 ==========");

        String topic = "股市迎来新一轮上涨行情，多个板块表现强劲";
        
        System.out.println("生成主题: " + topic);
        System.out.println("期望标签: 财经");
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, 3, 1);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.println("✓ 自动标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.println("  - " + tag.getTagName() + " (ID: " + tag.getId() + ")");
                }
            } else {
                System.out.println("✗ 未生成标签");
            }
        } else {
            System.out.println(" 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试娱乐类新闻自动打标签
     */
    @Test
    public void testAutoTagForEntertainmentNews() {
        System.out.println("========== 测试娱乐类新闻自动打标签 ==========");

        String topic = "春节档电影票房突破纪录，多部影片口碑爆棚";
        
        System.out.println("生成主题: " + topic);
        System.out.println("期望标签: 娱乐");
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, 4, 1);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.println("✓ 自动标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.println("  - " + tag.getTagName() + " (ID: " + tag.getId() + ")");
                }
            } else {
                System.out.println("✗ 未生成标签");
            }
        } else {
            System.out.println(" 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试跨类别新闻自动打标签
     */
    @Test
    public void testAutoTagForCrossCategoryNews() {
        System.out.println("========== 测试跨类别新闻自动打标签 ==========");

        // 这个主题可能同时涉及科技和财经
        String topic = "科技股引领股市上涨，AI概念板块大涨";
        
        System.out.println("生成主题: " + topic);
        System.out.println("期望标签: 科技、财经（跨类别）");
        System.out.println();

        News news = aiNewsGenerationService.generateNewsWithImage(topic, 1, 1);

        if (news != null) {
            System.out.println("✓ 新闻生成成功！");
            System.out.println("标题: " + news.getTitle());
            System.out.println("摘要: " + news.getSummary());
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.println("✓ 自动标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.println("  - " + tag.getTagName() + " (ID: " + tag.getId() + ")");
                }
            } else {
                System.out.println("✗ 未生成标签");
            }
        } else {
            System.out.println("✗ 新闻生成失败");
        }

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试批量生成新闻的自动标签
     */
    @Test
    public void testBatchAutoTagging() {
        System.out.println("========== 测试批量生成新闻的自动标签 ==========");

        List<String> topics = List.of(
                "5G技术赋能智慧城市发展",           // 期望：科技
                "NBA总决赛精彩对决",               // 期望：体育
                "央行宣布降准释放流动性",            // 期望：财经
                "热门综艺节目收视率创新高"           // 期望：娱乐
        );

        System.out.println("批量生成主题数量: " + topics.size());
        for (int i = 0; i < topics.size(); i++) {
            System.out.println((i + 1) + ". " + topics.get(i));
        }
        System.out.println();

        List<News> newsList = aiNewsGenerationService.generateNewsBatchWithImages(
                topics, 1, 1
        );

        System.out.println("生成结果:");
        System.out.println("成功生成 " + newsList.size() + " 条新闻");
        System.out.println();

        for (int i = 0; i < newsList.size(); i++) {
            News news = newsList.get(i);
            System.out.println((i + 1) + ". " + news.getTitle());
            
            if (news.getTags() != null && !news.getTags().isEmpty()) {
                System.out.print("   标签: ");
                for (Tag tag : news.getTags()) {
                    System.out.print(tag.getTagName() + " ");
                }
                System.out.println();
            } else {
                System.out.println("   标签: 无");
            }
            System.out.println();
        }

        System.out.println("========== 批量测试完成 ==========");
    }
}
