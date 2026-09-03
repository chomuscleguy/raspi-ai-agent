package com.chomu.raspiaiagent.tool;

import com.chomu.raspiaiagent.repository.LocationSettingsRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WeatherTool {

    private final LocationSettingsRepository locationRepository;
    private final RestClient restClient;
    private final String apiKey;

    public WeatherTool(
            LocationSettingsRepository locationRepository,
            @Value("${weather.api-key:}") String apiKey) {
        this.locationRepository = locationRepository;
        this.apiKey = apiKey;
        this.restClient = RestClient.create("https://api.openweathermap.org");
    }

    public record WeatherResult(
            String location,
            double temperature,
            double feelsLike,
            String description,
            int humidity
    ) {}

    @Tool(description = "현재 저장된 위치의 실시간 날씨 정보를 조회한다. " +
            "사용자가 날씨, 기온, 오늘 날씨, 옷차림 등에 대해 물어볼 때 반드시 이 도구를 사용한다.")
    public WeatherResult getCurrentWeather() {
        var location = locationRepository.findLatest()
                .orElseThrow(() -> new IllegalStateException(
                        "위치가 설정되지 않았습니다. 먼저 위치를 등록해주세요."));

        OpenWeatherResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", location.getLatitude())
                        .queryParam("lon", location.getLongitude())
                        .queryParam("appid", apiKey)
                        .queryParam("units", "metric")
                        .queryParam("lang", "kr")
                        .build())
                .retrieve()
                .body(OpenWeatherResponse.class);

        String label = location.getCityLabel() != null ? location.getCityLabel() : "설정된 위치";

        return new WeatherResult(
                label,
                response.main().temp(),
                response.main().feels_like(),
                response.weather().get(0).description(),
                response.main().humidity()
        );
    }

    private record OpenWeatherResponse(Main main, java.util.List<Weather> weather) {}
    private record Main(double temp, double feels_like, int humidity) {}
    private record Weather(String description) {}
}