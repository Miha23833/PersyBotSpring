package com.jerseybot.adapters;

import com.jerseybot.JDAStorage;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JDAStateListenerAdapter extends ListenerAdapter {
    private final JDAStorage jdaStorage;

    @Autowired
    public JDAStateListenerAdapter(JDAStorage jdaStorage) {
        this.jdaStorage = jdaStorage;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.jdaStorage.setJda(event.getJDA());
    }
}
