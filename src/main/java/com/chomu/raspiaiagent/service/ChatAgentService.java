package com.chomu.raspiaiagent.service;

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

    public ChatAgentService(ChatModel chatModel, SystemStatusTool systemStatusTool) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                        너는 라즈베리파이에서 동작하는 AI 에이전트야.
                        사용자의 질문에 친절하고 간결하게 답변해.
                        서버 상태나 성능에 대한 질문이 오면 반드시 도구를 호출해서
                        실제 데이터를 확인한 뒤 답변해.
                        """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(systemStatusTool)
                .build();
    }

    public String chat(String conversationId, String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public ChatAgentService(ChatModel chatModel, SystemStatusTool systemStatusTool, WeatherTool weatherTool) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build();

        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem("""
                    너는 라즈베리파이에서 동작하는 AI 에이전트야.
                    사용자의 질문에 친절하고 간결하게 답변해.
                    서버 상태나 날씨에 대한 질문이 오면 반드시 도구를 호출해서
                    실제 데이터를 확인한 뒤 답변해.
                    """)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(systemStatusTool, weatherTool)
                .build();
    }
}