package com.jerseybot.command;

import com.google.common.collect.Lists;
import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.chat.pagination.PAGEABLE_MESSAGE_TYPE;
import com.jerseybot.chat.pagination.PageableMessage;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.command.text.impl.AddPlaylistTextCommand;
import com.jerseybot.command.text.impl.AskChatGptTextCommand;
import com.jerseybot.command.text.impl.ChangePrefixTextCommand;
import com.jerseybot.command.text.impl.ChangeVolumeTextCommand;
import com.jerseybot.command.text.impl.JoinToVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.LeaveVoiceChannelTextCommand;
import com.jerseybot.command.text.impl.MixTrackQueueTextCommand;
import com.jerseybot.command.text.impl.PauseMusicTextCommand;
import com.jerseybot.command.text.impl.PlayMusicTextCommand;
import com.jerseybot.command.text.impl.PlayPlaylistTextCommand;
import com.jerseybot.command.text.impl.RepeatMusicTextCommand;
import com.jerseybot.command.text.impl.ResumeMusicTextCommand;
import com.jerseybot.command.text.impl.ShowPlaylistsTextCommand;
import com.jerseybot.command.text.impl.ShowQueueTextCommand;
import com.jerseybot.command.text.impl.SkipMusicTextCommand;
import com.jerseybot.command.text.impl.StopMusicTextCommand;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Component
public class TextCommandRouter {
    private final Map<String, AbstractTextCommand> textCommandRoutes;
    private final MessageSendService messageSendService;
    @Autowired
    public TextCommandRouter(MessageSendService messageSendService,
                             //
                             PlayMusicTextCommand playMusicTextCommand,
                             StopMusicTextCommand stopMusicTextCommand,
                             ResumeMusicTextCommand resumeMusicTextCommand,
                             PauseMusicTextCommand pauseMusicTextCommand,
                             JoinToVoiceChannelTextCommand joinToVoiceChannelTextCommand,
                             LeaveVoiceChannelTextCommand leaveVoiceChannelTextCommand,
                             SkipMusicTextCommand skipMusicTextCommand,
                             ShowQueueTextCommand showQueueTextCommand,
                             ChangePrefixTextCommand changePrefixTextCommand,
                             MixTrackQueueTextCommand mixTrackQueueTextCommand,
                             AddPlaylistTextCommand addPlaylistTextCommand,
                             PlayPlaylistTextCommand playPlaylistTextCommand,
                             ChangeVolumeTextCommand changeVolumeTextCommand,
                             RepeatMusicTextCommand repeatMusicTextCommand,
                             ShowPlaylistsTextCommand showPlaylistsTextCommand,
                             AskChatGptTextCommand askChatGptTextCommand) {
        this.messageSendService = messageSendService;

        Map<String, AbstractTextCommand> routes = new HashMap<>();

        registerRoutes(routes, playMusicTextCommand, "play", "p");
        registerRoutes(routes, stopMusicTextCommand, "stop", "s");
        registerRoutes(routes, resumeMusicTextCommand, "resume", "r");
        registerRoutes(routes, pauseMusicTextCommand, "pause", "pa");
        registerRoutes(routes, joinToVoiceChannelTextCommand, "join", "j");
        registerRoutes(routes, leaveVoiceChannelTextCommand, "leave", "l");
        registerRoutes(routes, skipMusicTextCommand, "skip");
        registerRoutes(routes, showQueueTextCommand, "queue");
        registerRoutes(routes, changePrefixTextCommand, "prefix");
        registerRoutes(routes, mixTrackQueueTextCommand, "mix");
        registerRoutes(routes, addPlaylistTextCommand, "addplaylist", "padd");
        registerRoutes(routes, playPlaylistTextCommand, "playlist", "pplay");
        registerRoutes(routes, showPlaylistsTextCommand, "playlists", "pshow", "plist");
        registerRoutes(routes, changeVolumeTextCommand, "volume", "v");
        registerRoutes(routes, repeatMusicTextCommand, "repeat", "roll");
        registerRoutes(routes, askChatGptTextCommand, "gpt", "chatgpt", "ask");

        this.textCommandRoutes = Collections.unmodifiableMap(routes);
    }

    public void route(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        try {
            String command = context.getCommand();
            if ("help".equals(command) || "h".equals(command)) {
                showHelp(context.getMessageChannel());
                return;
            }
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

    private void showHelp(GuildMessageChannel textChannel) {
        Map<AbstractTextCommand, List<String>> commands = new HashMap<>();
        for (Map.Entry<String, AbstractTextCommand> command : textCommandRoutes.entrySet()) {
            commands.computeIfAbsent(command.getValue(), k -> new LinkedList<>()).add(command.getKey());
        }
        PageableMessage.Builder pageableMessage = PageableMessage.builder();
        List<String> commandsHelp = new LinkedList<>();
        for (Map.Entry<AbstractTextCommand, List<String>> abstractTextCommandListEntry : commands.entrySet()) {
            commandsHelp.add(
                    String.join(", ", ("**" + abstractTextCommandListEntry.getValue() + "**") + " - " + abstractTextCommandListEntry.getKey().getDescription()) + "\n"
            );
        }

        Lists.partition(commandsHelp, 8)
                .stream()
                .map(part -> new InfoMessage("Available commands:", String.join("\n ", part)).template())
                .forEach(pageableMessage::addMessage);

        messageSendService.sendPageableMessage(pageableMessage, textChannel, PAGEABLE_MESSAGE_TYPE.HELP);
    }
}
