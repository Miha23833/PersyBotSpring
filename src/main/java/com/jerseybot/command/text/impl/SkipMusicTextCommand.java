package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkipMusicTextCommand extends AbstractTextCommand {
    private final PlayerRepository playerRepository;

    @Autowired
    public SkipMusicTextCommand(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return rsp.isOk();
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            return false;
        }
        Player player = playerRepository.get(context.getGuildId());
        if (!player.isPlaying()) {
            rsp.setMessage("Player is not playing.");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        Player player = playerRepository.get(context.getGuildId());
        player.skip();
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
