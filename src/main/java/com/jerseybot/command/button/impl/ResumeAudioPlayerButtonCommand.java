package com.jerseybot.command.button.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.button.ButtonCommand;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.button.enums.PLAYER_BUTTON;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResumeAudioPlayerButtonCommand implements ButtonCommand {
    private final PlayerRepository playerRepository;

    @Autowired
    public ResumeAudioPlayerButtonCommand(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    public void execute(ButtonCommandContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            return;
        }
        Player player = playerRepository.get(context.getGuildId());
        if (player.isPlaying() && !player.isPaused()) {
            return;
        }

        player.resume();
        context.getEvent().getInteraction().editButton(PLAYER_BUTTON.PAUSE.button()).queue();
    }
}
