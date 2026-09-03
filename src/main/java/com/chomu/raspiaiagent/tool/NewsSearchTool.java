package com.chomu.raspiaiagent.tool;

import com.chomu.raspiaiagent.repository.UserInterestRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class NewsSearchTool {

    private final UserInterestRepository userInterestRepository;
    private final RestClient restClient;
    private final String apiKey;

    public NewsSearchTool(
            UserInterestRepository userInterestRepository,
            @Value("${news.api-key:}") String apiKey) {
        this.userInterestRepository = userInterestRepository;
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://newsapi.org");
    }

    public record NewsItem(String title, String description, String url) {}

    @Tool(description = "사용자가 이전 대화에서 언급했던 관심사(키워드)와 관련된 최신 뉴스를 검색한다. " +
            "사용자가 '요즘 뉴스 뭐 있어', '내가 관심있어했던 거 관련 소식 있어?' 등을 물어볼 때 사용한다.")
    public List<NewsItem> searchNewsForInterests() {
        var interests = userInterestRepository.findByActiveTrue();
        if (interests.isEmpty()) {
            return List.of();
        }

        // 가장 최근 관심사 하나로 검색 (여러 개면 API 호출량이 늘어나므로 우선 1개만)
        String keyword = interests.get(interests.size() - 1).getKeyword();

        NewsApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/everything")
                        .queryParam("q", keyword)
                        .queryParam("language", "ko")
                        .queryParam("sortBy", "publishedAt")
                        .queryParam("pageSize", 5)
                        .queryParam("apiKey", apiKey)
                        .build())
                .retrieve()
                .body(NewsApiResponse.class);

        var interest = interests.get(interests.size() - 1);
        interest.setLastCheckedAt(OffsetDateTime.now());
        userInterestRepository.save(interest);

        return response.articles().stream()
                .map(a -> new NewsItem(a.title(), a.description(), a.url()))
                .toList();
    }

    private record NewsApiResponse(List<Article> articles) {}
    private record Article(String title, String description, String url) {}
}