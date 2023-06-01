package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepeatMusicTextCommand extends AbstractTextCommand {
    private final PlayerRepository playerRepository;

    @Autowired
    public RepeatMusicTextCommand(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            rsp.setMessage("Player is not playing.");
            return false;
        }
        Player player = playerRepository.get(context.getGuildId());
        if (!player.isPlaying()) {
            rsp.setMessage("Player is not playing.");
            return false;
        }
        player.repeat();
        return true;
    }

    @Override
    public String getDescription() {
        return "If current track finished, play it again. To stop repeating, skip the track.";
    }
}
