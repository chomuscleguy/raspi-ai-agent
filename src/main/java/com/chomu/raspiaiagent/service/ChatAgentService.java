package com.chomu.raspiaiagent.service;

import com.chomu.raspiaiagent.repository.ActivityLogRepository;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatAgentService {

    private final ChatClient chatClient;
    private final InterestExtractionService interestExtractionService;
    private final ActivityLogRepository activityLogRepository;

    public ChatAgentService(
            ChatModel chatModel,
            SystemStatusTool systemStatusTool,
            WeatherTool weatherTool,
            NewsSearchTool newsSearchTool,
            InterestExtractionService interestExtractionService,
            ActivityLogRepository activityLogRepository) {

        this.interestExtractionService = interestExtractionService;
        this.activityLogRepository = activityLogRepository;

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
        String recentActivity = getRecentActivitySummary();

        String contextualMessage = recentActivity.isBlank()
                ? userMessage
                : "(참고: 최근 혼자 있는 동안의 내 활동 기록: %s)\n\n사용자: %s"
                .formatted(recentActivity, userMessage);

        String reply = chatClient.prompt()
                .user(contextualMessage)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        interestExtractionService.extractAndSaveInterest(userMessage);

        return reply;
    }

    /**
     * 최근 24시간 이내의 활동 로그를 요약해서 반환.
     * 대화 시작 시 캐릭터가 "그동안 뭐 했는지" 자연스럽게 언급할 수 있게 함.
     */
    private String getRecentActivitySummary() {
        OffsetDateTime since = OffsetDateTime.now().minusHours(24);
        List<String> logs = activityLogRepository
                .findByCreatedAtAfterOrderByCreatedAtAsc(since)
                .stream()
                .map(log -> log.getContent())
                .collect(Collectors.toList());

        if (logs.isEmpty()) {
            return "";
        }
        // 너무 길어지지 않게 최근 3개만
        int start = Math.max(0, logs.size() - 3);
        return String.join(" / ", logs.subList(start, logs.size()));
    }
}