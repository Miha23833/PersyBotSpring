package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MixTrackQueueTextCommand extends AbstractTextCommand {
    private final PlayerRepository playerRepository;
    private final MessageSendService messageSendService;

    @Autowired
    public MixTrackQueueTextCommand(PlayerRepository playerRepository, MessageSendService messageSendService) {
        this.playerRepository = playerRepository;
        this.messageSendService = messageSendService;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return true;
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId()) || playerRepository.get(context.getGuildId()).isTracksQueueEmpty()) {
            rsp.setMessage("You don't have tracks in the queue.");
        }
        return rsp.isOk();
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        Player player = playerRepository.get(context.getGuildId());
        player.mixQueue();

        messageSendService.sendInfoMessage(context.getTextChannel(), "Queue was mixed");
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
