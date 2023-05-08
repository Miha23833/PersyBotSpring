package com.jerseybot.command;

import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.command.text.impl.JoinToVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.LeaveVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.PauseMusicTextCommand;
import com.jerseybot.command.text.impl.PlayMusicTextCommand;
import com.jerseybot.command.text.impl.ResumeMusicTextCommand;
import com.jerseybot.command.text.impl.ShowQueueTextCommand;
import com.jerseybot.command.text.impl.SkipMusicTextCommand;
import com.jerseybot.command.text.impl.StopMusicTextCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class TextCommandRouter {
    private final Map<String, AbstractTextCommand> textCommandRoutes;

    @Autowired
    public TextCommandRouter(PlayMusicTextCommand playMusicTextCommand,
                             StopMusicTextCommand stopMusicTextCommand,
                             ResumeMusicTextCommand resumeMusicTextCommand,
                             PauseMusicTextCommand pauseMusicTextCommand,
                             JoinToVoiceChannelTextCommand joinToVoiceChannelTextCommand,
                             LeaveVoiceChannelTextCommand leaveVoiceChannelTextCommand,
                             SkipMusicTextCommand skipMusicTextCommand,
                             ShowQueueTextCommand showQueueTextCommand) {
        Map<String, AbstractTextCommand> routes = new HashMap<>();

        registerRoutes(routes, playMusicTextCommand, "play", "p");
        registerRoutes(routes, stopMusicTextCommand, "stop", "s");
        registerRoutes(routes, resumeMusicTextCommand, "resume", "r");
        registerRoutes(routes, pauseMusicTextCommand, "pause", "pa");
        registerRoutes(routes, joinToVoiceChannelTextCommand, "join", "j");
        registerRoutes(routes, leaveVoiceChannelTextCommand, "leave", "l");
        registerRoutes(routes, skipMusicTextCommand, "skip");
        registerRoutes(routes, showQueueTextCommand, "queue");

        this.textCommandRoutes = Collections.unmodifiableMap(routes);
    }

    public void route(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        try {
            String command = context.getCommand();
            if (textCommandRoutes.containsKey(command)) {
                AbstractTextCommand textCommand = textCommandRoutes.get(command);
                textCommand.execute(context, rsp);
            }
        } catch (Throwable e) {
            rsp.setException(e);
            rsp.setMessage("Something went wrong");
        }
    }

    private void registerRoutes(Map<String, AbstractTextCommand> routes, AbstractTextCommand command, String... mappings) {
        for (String mapping: mappings) {
            routes.put(mapping.toLowerCase(), command);
        }
    }
}
