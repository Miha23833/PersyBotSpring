package com.jerseybot.config.chatgpt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.OpenAiApi;
import com.theokanning.openai.service.OpenAiService;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import retrofit2.Retrofit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static com.theokanning.openai.service.OpenAiService.defaultClient;
import static com.theokanning.openai.service.OpenAiService.defaultObjectMapper;
import static com.theokanning.openai.service.OpenAiService.defaultRetrofit;

@Configuration
@PropertySource("classpath:chatgpt.properties")
public class ChatGptConfiguration {
    @Value("${token}")
    private String token;

    @Value("${requestTimeoutSeconds}")
    private int timoutInSeconds;

    @Bean
    public OpenAiService getOpenAiService() {
        ObjectMapper mapper = defaultObjectMapper();
        OkHttpClient client = defaultClient(token, Duration.of(timoutInSeconds, ChronoUnit.SECONDS))
                .newBuilder()
                .build();
        Retrofit retrofit = defaultRetrofit(client, mapper);

        OpenAiApi api = retrofit.create(OpenAiApi.class);
        return new OpenAiService(api);
    }
}
