package com.jerseybot.config.chatgpt;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:chatgpt.properties")
public class ChatGptConfiguration {
    @Value("${token}")
    private String token;

    @Bean
    public OpenAiService getOpenAiService() {
        return new OpenAiService(token);
    }
}
