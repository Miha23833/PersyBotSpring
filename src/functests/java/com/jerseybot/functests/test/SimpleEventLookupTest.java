package com.jerseybot.functests.test;

import com.jerseybot.functests.base.BotTestBase;
import com.jerseybot.functests.base.adapters.TestEventMatcher;
import com.jerseybot.functests.config.TestServerData;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.function.Predicate;

@SpringBootTest
public class SimpleEventLookupTest {
    @Autowired
    BotTestBase botTestBase;

    @Autowired
    TestServerData testServerData;

    @Test
    public void testMessageEventsMatched() {
        String messageContent = UUID.randomUUID().toString();
        botTestBase.getJda().getTextChannelById(testServerData.getTextChannelId_1()).sendMessage(messageContent).queue();
        botTestBase.getJda().getTextChannelById(testServerData.getTextChannelId_2()).sendMessage(messageContent).queue();

        Predicate<MessageReceivedEvent> testMsg1 = e -> e.getMessage().getChannel().asTextChannel().getIdLong() == testServerData.getTextChannelId_1()
                && messageContent.equals(e.getMessage().getContentRaw());

        Predicate<MessageReceivedEvent> testMsg2 = e -> e.getMessage().getChannel().asTextChannel().getIdLong() == testServerData.getTextChannelId_2()
                && messageContent.equals(e.getMessage().getContentRaw());

        new TestEventMatcher(botTestBase.getJda())
                .addEvent(MessageReceivedEvent.class, testMsg1)
                .addEvent(MessageReceivedEvent.class, testMsg2)
                .await();
    }
}
