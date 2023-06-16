package com.jerseybot.adapters;

import com.jerseybot.JDAStorage;
import com.jerseybot.config.BotConfig;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JDAStateListenerAdapter extends ListenerAdapter {
    private final JDAStorage jdaStorage;
    private final BotConfig botConfig;

    @Autowired
    public JDAStateListenerAdapter(JDAStorage jdaStorage, BotConfig botConfig) {
        this.jdaStorage = jdaStorage;
        this.botConfig = botConfig;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.jdaStorage.setJda(event.getJDA());
        event.getJDA().getPresence().setActivity(Activity.competing(botConfig.getDefaultPrefix() + "help"));
    }
}
