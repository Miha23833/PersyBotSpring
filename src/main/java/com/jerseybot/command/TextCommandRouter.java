package com.jerseybot.command;

import com.jerseybot.chat.TextChannelsToInteractStore;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.command.text.impl.JoinToVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.LeaveVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.PauseMusicTextCommand;
import com.jerseybot.command.text.impl.PlayMusicTextCommand;
import com.jerseybot.command.text.impl.ResumeMusicTextCommand;
import com.jerseybot.command.text.impl.SetDefaultInteractionTextChannelTextCommand;
import com.jerseybot.command.text.impl.SkipMusicTextCommand;
import com.jerseybot.command.text.impl.StopMusicTextCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class TextCommandRouter {
    private final Map<String, AbstractTextCommand> textCommandRoutes;
    private final TextChannelsToInteractStore textChannelsToInteractStore;

    @Autowired
    public TextCommandRouter(PlayMusicTextCommand playMusicTextCommand,
                             StopMusicTextCommand stopMusicTextCommand,
                             ResumeMusicTextCommand resumeMusicTextCommand,
                             PauseMusicTextCommand pauseMusicTextCommand,
                             JoinToVoiceChannelTextCommand joinToVoiceChannelTextCommand,
                             LeaveVoiceChannelTextCommand leaveVoiceChannelTextCommand,
                             SkipMusicTextCommand skipMusicTextCommand,
                             SetDefaultInteractionTextChannelTextCommand setDefaultInteractionTextChannelTextCommand,

                            TextChannelsToInteractStore textChannelsToInteractStore) {
        this.textChannelsToInteractStore = textChannelsToInteractStore;

        Map<String, AbstractTextCommand> routes = new HashMap<>();

        registerRoutes(routes, playMusicTextCommand, "play", "p");
        registerRoutes(routes, stopMusicTextCommand, "stop", "s");
        registerRoutes(routes, resumeMusicTextCommand, "resume", "r");
        registerRoutes(routes, pauseMusicTextCommand, "pause", "pa");
        registerRoutes(routes, joinToVoiceChannelTextCommand, "join", "j");
        registerRoutes(routes, leaveVoiceChannelTextCommand, "leave", "l");
        registerRoutes(routes, skipMusicTextCommand, "skip");
        registerRoutes(routes, setDefaultInteractionTextChannelTextCommand, "texthere", "text-here", "behere");

        this.textCommandRoutes = Collections.unmodifiableMap(routes);
    }

    public void route(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        try {
            String command = context.getCommand().toLowerCase();
            Long guildId = context.getGuildId();
            Long textChannelId = context.getTextChannel().getIdLong();
            populateDefaultTextChannelToStoreIfNeededAndNotifyUser(context);
            if (textCommandRoutes.containsKey(command)) {
                AbstractTextCommand textCommand = textCommandRoutes.get(command);
                if (Objects.equals(textChannelsToInteractStore.get(guildId), textChannelId)
                        || textCommand instanceof SetDefaultInteractionTextChannelTextCommand) {
                    textCommand.execute(context, rsp);
                }

            } else {
                rsp.setMessage(context.getCommand() + " is not my command.");
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

    private void populateDefaultTextChannelToStoreIfNeededAndNotifyUser(TextCommandExecutionContext context) {
        Long guildId = context.getGuildId();
        Long textChannelId = context.getTextChannel().getIdLong();
        if (!textChannelsToInteractStore.contains(guildId)) {
            textChannelsToInteractStore.update(guildId, textChannelId);
            context.getTextChannel()
                    .sendMessage("I will listen and write to this channel by default. To move me to another text channel, use command \"behere\" in this channel.")
                    .queue();
        }
    }
}
