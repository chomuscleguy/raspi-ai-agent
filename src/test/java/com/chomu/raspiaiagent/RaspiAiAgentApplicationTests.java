package com.chomu.raspiaiagent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.google.genai.api-key=test-dummy-key"
})
class RaspiAiAgentApplicationTests {

    @Test
    void contextLoads() {
    }
}