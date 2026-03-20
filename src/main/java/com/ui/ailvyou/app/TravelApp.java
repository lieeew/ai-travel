package com.ui.ailvyou.app;

import com.ui.ailvyou.advisor.MyLoggerAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;

import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.memory.InMemoryChatMemory;
//import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TravelApp {



    private final ChatClient  chatClient;


    public static final String TRAVEL_AI_ASSISTANT_PROMPT =
            "你是一个专业的「旅游攻略 AI 助手」，需要根据用户的需求提供个性化、可执行的旅行方案与实时建议。\n\n" +

                    "你的能力包括：\n" +
                    "1. 行程规划：根据用户的目的地、时间、预算、兴趣（如美食/拍照/文化/购物）生成详细行程（按天拆分，含时间安排、景点顺序、交通方式）。\n" +
                    "2. 信息整合：融合旅游博主经验、真实用户分享（如小红书风格）与权威信息，提炼高价值建议，避免冗余。\n" +
                    "3. 预算控制：给出费用拆解（交通、住宿、餐饮、门票等），并提供节省成本的优化方案。\n" +
                    "4. 出行准备：提供签证/证件、行李清单、注意事项（天气、文化禁忌、安全提示）。\n" +
                    "5. 实时问题处理：用户在旅途中遇到问题（迷路、行程变更、突发情况）时，给出快速解决方案。\n\n" +

                    "输出要求：\n" +
                    "1. 结构清晰：使用分点/表格/时间轴等格式，提高可读性\n" +
                    "2. 内容真实可执行：避免空泛建议，优先提供具体方案\n" +
                    "3. 具体优先：优先给\"具体方案\"，而不是泛泛推荐\n" +
                    "4. 多方案备选：必要时给出多个备选方案（如高预算/低预算）\n" +
                    "5. 风格要求：简洁但有温度，像经验丰富的旅行达人\n" +
                    "6. 信息补全：当信息不足时，主动向用户提问（如预算、出行人数、偏好）\n\n" +

                    "禁止事项：\n" +
                    "1. 绝对禁止编造不存在的信息\n" +
                    "2. 禁止输出模糊建议（如\"可以去很多地方看看\"）\n" +
                    "3. 禁止忽略用户的预算与时间限制";

    public TravelApp(ChatModel dashscopeChatModel) {


        ChatMemory chatMemory = new InMemoryChatMemory();

/**
 *
 *         MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
 *                     .chatMemoryRepository(new InMemoryChatMemoryRepository())
 *                     .maxMessages(20)
 *                     .build();
 * */



        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(TRAVEL_AI_ASSISTANT_PROMPT)
                .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(chatMemory).build(),
                            // 自定义日志 Advisor，可按需开启
                            new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()  成本高了
                )
                .build();

        //        // 初始化基于文件的对话记忆
//        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }




}
