package com.jerseybot.command.button.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.button.ButtonCommand;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StopAudioPlayerButtonCommand implements ButtonCommand {
    private final PlayerRepository playerRepository;
    private final MessageSendService messageSendService;

    @Autowired
    public StopAudioPlayerButtonCommand(PlayerRepository playerRepository, MessageSendService messageSendService) {
        this.playerRepository = playerRepository;
        this.messageSendService = messageSendService;
    }

    @Override
    public void execute(ButtonCommandContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            return;
        }
        Player player = playerRepository.get(context.getGuildId());
        if (!player.isPlaying()) {
            return;
        }

        player.stop();
        Message message = context.getEvent().getMessage();
        if (message.getEmbeds().size() > 0) {
            MessageEmbed embed = message.getEmbeds().get(0);
            MessageEditData messageUpdated = MessageEditData.fromCreateData(new InfoMessage(embed.getTitle(), embed.getDescription()).template());
            message.editMessage(messageUpdated).queue();
        }

        messageSendService.sendInfoMessage(message.getChannel().asGuildMessageChannel(), "Player stopped.");
    }
}