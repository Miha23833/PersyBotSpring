package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChangeVolumeTextCommand extends AbstractTextCommand {
    private final MessageSendService messageSendService;
    private final PlayerRepository playerRepository;
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public ChangeVolumeTextCommand(MessageSendService messageSendService, PlayerRepository playerRepository, DiscordServerRepository discordServerRepository) {
        this.messageSendService = messageSendService;
        this.playerRepository = playerRepository;
        this.discordServerRepository = discordServerRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            return false;
        }
        if (context.getArgs().isEmpty()) {
            rsp.setMessage("Please add volume parameter (0-1000).");
            return false;
        }
        int volume;
        try {
            volume = Integer.parseInt(context.getArgs().get(0));
        } catch (NumberFormatException e) {
            rsp.setMessage("Volume must be number.");
            return false;
        }
        if (volume < 0 || volume > 1000) {
            rsp.setMessage("Volume must be 0-1000");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        int volume = Integer.parseInt(context.getArgs().get(0));
        Player player = playerRepository.get(context.getGuildId());
        player.setVolume(volume);

        DiscordServer discordServer = discordServerRepository.findById(context.getGuildId()).orElseThrow();
        discordServer.getSettings().setVolume(volume);
        discordServerRepository.save(discordServer);

        messageSendService.sendInfoMessage(context.getTextChannel(), "Volume set to " + volume);

        return true;
    }

    @Override
    public String getDescription() {
        return "Change volume of player. 0 is silence, 100 is default volume, 1000 is 10x volume boost.";
    }
}
