package com.qst.onlinenewsbackend;

import com.qst.onlinenewsbackend.service.ImageGenerationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 图片生成服务测试类
 * 测试百度千帆图片生成功能
 *
 * @author QST
 * @since 1.0
 */
@SpringBootTest
public class ImageGenerationTest {

    @Autowired
    private ImageGenerationService imageGenerationService;

    /**
     * 测试根据新闻标题生成图片
     */
    @Test
    public void testGenerateImageByTitle() {
        System.out.println("========== 开始测试图片生成 ==========");

        // 测试用例1：科技新闻
        String title1 = "人工智能技术突破性进展";
        String summary1 = "最新研究表明，AI技术在自然语言处理领域取得重大突破，能够实现更智能的对话和理解能力。";
        
        System.out.println("测试1 - 科技新闻：");
        System.out.println("标题: " + title1);
        System.out.println("摘要: " + summary1);
        
        String imagePath1 = imageGenerationService.generateImageForNews(title1, summary1);
        System.out.println("生成结果: " + (imagePath1 != null ? "成功 - " + imagePath1 : "失败"));
        System.out.println();

        // 测试用例2：体育新闻
        String title2 = "中国队在奥运会上获得金牌";
        String summary2 = "在今天的比赛中，中国选手以优异成绩夺得金牌，为国家争得荣誉。";
        
        System.out.println("测试2 - 体育新闻：");
        System.out.println("标题: " + title2);
        System.out.println("摘要: " + summary2);
        
        String imagePath2 = imageGenerationService.generateImageForNews(title2, summary2);
        System.out.println("生成结果: " + (imagePath2 != null ? "成功 - " + imagePath2 : "失败"));
        System.out.println();

        // 测试用例3：财经新闻
        String title3 = "股市迎来新一轮上涨行情";
        String summary3 = "受利好政策影响，今日股市大幅上涨，多个板块表现强劲。";
        
        System.out.println("测试3 - 财经新闻：");
        System.out.println("标题: " + title3);
        System.out.println("摘要: " + summary3);
        
        String imagePath3 = imageGenerationService.generateImageForNews(title3, summary3);
        System.out.println("生成结果: " + (imagePath3 != null ? "成功 - " + imagePath3 : "失败"));
        System.out.println();

        // 测试用例4：仅有标题
        String title4 = "科技创新推动经济发展";
        
        System.out.println("测试4 - 仅标题：");
        System.out.println("标题: " + title4);
        
        String imagePath4 = imageGenerationService.generateImageForNews(title4, null);
        System.out.println("生成结果: " + (imagePath4 != null ? "成功 - " + imagePath4 : "失败"));
        System.out.println();

        System.out.println("========== 测试完成 ==========");
    }

    /**
     * 测试单个新闻图片生成
     */
    @Test
    public void testSingleImageGeneration() {
        String title = "春日花海美不胜收";
        String summary = "正值春暖花开的季节，各地花海进入最佳观赏期，吸引大量游客前往拍照打卡。";

        System.out.println("生成新闻配图：");
        System.out.println("标题: " + title);
        System.out.println("摘要: " + summary);

        String imagePath = imageGenerationService.generateImageForNews(title, summary);

        if (imagePath != null) {
            System.out.println("✓ 图片生成成功！");
            System.out.println("保存路径: " + imagePath);
        } else {
            System.out.println("✗ 图片生成失败");
        }
    }

    /**
     * 测试批量生成图片
     */
    @Test
    public void testBatchImageGeneration() {
        System.out.println("========== 批量图片生成测试 ==========");

        java.util.List<ImageGenerationService.NewsTitleSummary> newsList = new java.util.ArrayList<>();
        
        newsList.add(new ImageGenerationService.NewsTitleSummary(
                "城市夜景灯火辉煌",
                "繁华都市的夜景令人陶醉，高楼大厦灯火通明"
        ));
        
        newsList.add(new ImageGenerationService.NewsTitleSummary(
                "科技创新大会召开",
                "全球科技精英齐聚一堂，共话未来科技发展趋势"
        ));
        
        newsList.add(new ImageGenerationService.NewsTitleSummary(
                "美食文化节开幕",
                "各地特色美食齐聚，吸引众多美食爱好者前来品尝"
        ));

        java.util.List<String> imagePaths = imageGenerationService.generateImagesForNewsList(newsList);

        System.out.println("批量生成结果：");
        System.out.println("成功生成: " + imagePaths.size() + " 张图片");
        
        for (int i = 0; i < imagePaths.size(); i++) {
            System.out.println((i + 1) + ". " + imagePaths.get(i));
        }

        System.out.println("========== 批量测试完成 ==========");
    }
}
