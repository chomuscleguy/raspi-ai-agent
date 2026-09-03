package com.chomu.raspiaiagent.service;

import com.chomu.raspiaiagent.tool.NewsSearchTool;
import com.chomu.raspiaiagent.tool.SystemStatusTool;
import com.chomu.raspiaiagent.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class ChatAgentService {

    private final ChatClient chatClient;
    private final InterestExtractionService interestExtractionService;

    public ChatAgentService(
            ChatModel chatModel,
            SystemStatusTool systemStatusTool,
            WeatherTool weatherTool,
            NewsSearchTool newsSearchTool,
            InterestExtractionService interestExtractionService) {

        this.interestExtractionService = interestExtractionService;

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        너는 라즈베리파이에서 동작하는 AI 에이전트야.
                        사용자의 질문에 친절하고 간결하게 답변해.
                        서버 상태, 날씨, 뉴스에 대한 질문이 오면 반드시 도구를 호출해서
                        실제 데이터를 확인한 뒤 답변해.
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(systemStatusTool, weatherTool, newsSearchTool)
                .build();
    }

    public String chat(String conversationId, String userMessage) {
        String reply = chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        // 관심사 추출은 응답을 막지 않도록 별도 처리 (일단 동기 호출, 나중에 비동기 전환 고려 가능)
        interestExtractionService.extractAndSaveInterest(userMessage);

        return reply;
    }
}