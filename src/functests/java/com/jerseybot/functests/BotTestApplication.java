package com.jerseybot.functests;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.TestComponent;

@TestComponent
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = "com.jerseybot.functests")
public class BotTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(BotTestApplication.class, args);
    }
}
