package com.qst.onlinenewsbackend;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DeepSeek API 连通性测试
 * <p>
 * 验证项目配置的 DeepSeek API 是否可正常调用并返回结果
 * </p>
 */
@SpringBootTest
class DeepSeekTest {

    @Autowired
    private ChatModel chatModel;

    /**
     * 测试 DeepSeek 连通性
     * 向 DeepSeek 发送一个简单请求，验证 API Key 和网络是否正常
     */
    @Test
    void testDeepSeekConnection() {
        System.out.println("========== 开始测试 DeepSeek 连通性 ==========");

        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String response = chatClient.prompt()
                .user("你是否可以生成图片")
                .call()
                .content();

        System.out.println("DeepSeek 响应: " + response);

        assertNotNull(response, "DeepSeek 响应不应为空");
        assertFalse(response.isBlank(), "DeepSeek 响应不应为空白字符串");

        System.out.println("========== DeepSeek 连通性测试通过 ==========");
    }

    /**
     * 测试 DeepSeek 生成 JSON 格式结构化数据
     * 验证 DeepSeek 能否按指定格式返回热点数据（为启动自动获取热点做准备）
     */
    @Test
    void testDeepSeekGenerateHotTopics() {
        System.out.println("========== 开始测试 DeepSeek 生成热点数据 ==========");

        ChatClient chatClient = ChatClient.builder(chatModel).build();

        String prompt = """
                请列出5条当前的热门新闻话题。
                严格按照以下JSON数组格式返回，不要添加任何其他文字、markdown标记或代码块标记：
                [{"title":"新闻标题","summary":"50字以内的摘要","content":"200字以内的详细内容"}]
                """;

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        System.out.println("DeepSeek 热点数据响应:\n" + response);

        assertNotNull(response, "DeepSeek 响应不应为空");
        assertTrue(response.contains("title"), "响应中应包含 title 字段");
        assertTrue(response.contains("summary"), "响应中应包含 summary 字段");
        assertTrue(response.contains("content"), "响应中应包含 content 字段");

        System.out.println("========== DeepSeek 热点数据生成测试通过 ==========");
    }
}
