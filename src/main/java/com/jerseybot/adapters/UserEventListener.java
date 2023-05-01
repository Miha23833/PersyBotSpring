package com.jerseybot.adapters;

import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.command.text.impl.PlayMusicTextCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener extends ListenerAdapter {
    private final PlayMusicTextCommand playMusicTextCommand;

    @Autowired
    public UserEventListener(PlayMusicTextCommand playMusicTextCommand) {
        this.playMusicTextCommand = playMusicTextCommand;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().isBot()) {
            playMusicTextCommand.execute(new TextCommandExecutionContext(event, "$"));
        }
    }
}
