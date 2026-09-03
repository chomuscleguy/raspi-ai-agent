package com.chomu.raspiaiagent.service;

import com.chomu.raspiaiagent.entity.ActivityLog;
import com.chomu.raspiaiagent.repository.ActivityLogRepository;
import com.chomu.raspiaiagent.repository.UserInterestRepository;
import com.chomu.raspiaiagent.tool.SystemStatusTool;
import com.chomu.raspiaiagent.tool.WeatherTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class IdleActivityScheduler {

    private final ChatClient chatClient;
    private final WeatherTool weatherTool;
    private final SystemStatusTool systemStatusTool;
    private final UserInterestRepository userInterestRepository;
    private final ActivityLogRepository activityLogRepository;

    public IdleActivityScheduler(
            ChatModel chatModel,
            WeatherTool weatherTool,
            SystemStatusTool systemStatusTool,
            UserInterestRepository userInterestRepository,
            ActivityLogRepository activityLogRepository) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.weatherTool = weatherTool;
        this.systemStatusTool = systemStatusTool;
        this.userInterestRepository = userInterestRepository;
        this.activityLogRepository = activityLogRepository;
    }

    /**
     * 1시간마다 실행. 현재 날씨/시스템 상태/관심사를 바탕으로
     * 캐릭터가 "혼자 있는 동안 무엇을 하고 있는지"를 짧게 생성해 저장한다.
     */
    @Scheduled(fixedRate = 3600000) // 1시간 = 3,600,000ms
    public void generateIdleActivity() {
        try {
            String weatherInfo = safeGetWeather();
            String systemInfo = safeGetSystemStatus();
            String interestInfo = safeGetInterests();

            String prompt = """
                    너는 라즈베리파이에서 24시간 살아있는 AI 캐릭터야.
                    지금은 사용자가 없는 혼자만의 시간이야.
                    아래 정보를 참고해서, 지금 이 순간 네가 무엇을 하고 있거나
                    무슨 생각을 하고 있는지 1~2문장의 짧은 혼잣말로 표현해줘.
                    너무 거창하지 않게, 소소하고 자연스럽게 써줘.

                    [현재 날씨]
                    %s

                    [내 상태(서버)]
                    %s

                    [사용자가 관심 가졌던 것]
                    %s
                    """.formatted(weatherInfo, systemInfo, interestInfo);

            String activity = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            activityLogRepository.save(new ActivityLog(activity));

        } catch (Exception e) {
            // 스케줄러는 실패해도 앱 전체에 영향 주면 안 되므로 여기서 흡수
            activityLogRepository.save(new ActivityLog(
                    "(활동 기록 생성 중 오류가 발생했습니다: " + e.getMessage() + ")"));
        }
    }

    private String safeGetWeather() {
        try {
            var weather = weatherTool.getCurrentWeather();
            return "%s, 기온 %.1f℃, %s".formatted(
                    weather.location(), weather.temperature(), weather.description());
        } catch (Exception e) {
            return "날씨 정보 없음";
        }
    }

    private String safeGetSystemStatus() {
        try {
            var status = systemStatusTool.getSystemStatus();
            return "힙 메모리 사용량 %dMB, 시스템 부하 %.2f".formatted(
                    status.usedHeapMb(), status.systemLoadAverage());
        } catch (Exception e) {
            return "시스템 상태 정보 없음";
        }
    }

    private String safeGetInterests() {
        var interests = userInterestRepository.findByActiveTrue();
        if (interests.isEmpty()) {
            return "아직 특별히 아는 관심사 없음";
        }
        return interests.stream()
                .map(i -> i.getKeyword())
                .reduce((a, b) -> a + ", " + b)
                .orElse("없음");
    }
}