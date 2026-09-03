package com.chomu.raspiaiagent;

import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;

@SpringBootTest(properties = {
        "spring.ai.model.chat=google-genai",
        "spring.ai.google.genai.api-key=test-dummy-key",
        "spring.ai.openai.api-key=test-dummy-key",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
})
class RaspiAiAgentApplicationTests {

    @Test
    void contextLoads() {
    }
}