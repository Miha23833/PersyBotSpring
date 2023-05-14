package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.db.repositories.PlayListRepository;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

@Component
public class AddPlaylistTextCommand extends AbstractTextCommand {
    private final DiscordServerRepository discordServerRepository;
    private final PlayListRepository playListRepository;

    public AddPlaylistTextCommand(DiscordServerRepository discordServerRepository, PlayListRepository playListRepository) {
        this.discordServerRepository = discordServerRepository;
        this.playListRepository = playListRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getArgs().size() < 2) {
            rsp.setMessage("Need arguments: {playlist name} {url}");
        }
        String firstArg = context.getArgs().get(0);
        String secondArg = context.getArgs().get(1);

        if (firstArg.length() > Playlist.MAX_PLAYLIST_NAME_LENGTH) {
            rsp.setMessage("Max playlist name length is " + Playlist.MAX_PLAYLIST_NAME_LENGTH);
        }
        if (!UrlValidator.getInstance().isValid(secondArg)) {
            rsp.setMessage(secondArg + " must be a url");
        }
        return rsp.isOk();
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        DiscordServer discordServer = discordServerRepository.findById(context.getGuildId()).orElseThrow();
        playListRepository.save(new Playlist(discordServer, context.getArgs().get(0), context.getArgs().get(1)));
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
