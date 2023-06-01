package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.entities.PlaylistId;
import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.db.repositories.PlayListRepository;
import com.jerseybot.utils.ActionHelper;
import com.jerseybot.utils.BotUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PlayPlaylistTextCommand extends AbstractTextCommand {
    private final DiscordServerRepository discordServerRepository;
    private final PlayListRepository playListRepository;
    private final ActionHelper actionHelper;

    public PlayPlaylistTextCommand(DiscordServerRepository discordServerRepository, PlayListRepository playListRepository, ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
        this.discordServerRepository = discordServerRepository;
        this.playListRepository = playListRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getArgs().isEmpty()) {
            rsp.setMessage("Please specify playlist name");
        }
        return rsp.isOk();
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return BotUtils.canBotJoinAndSpeak(context, rsp);
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        DiscordServer discordServer = discordServerRepository.findById(context.getGuildId()).orElseThrow();
        Optional<Playlist> playlist = playListRepository.findById(new PlaylistId(context.getArgs().get(0), discordServer));
        if (playlist.isEmpty()) {
            rsp.setMessage("Playlist with name '" +context.getArgs().get(0) + "' does not exist.");
            return false;
        }
        actionHelper.joinAndPlay(context, playlist.get().getUrl());
        return rsp.isOk();
    }

    @Override
    public String getDescription() {
        return "Play saved playlist. Link may be outdated.";
    }
}
