package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.entities.PlaylistId;
import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.db.repositories.PlayListRepository;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

@Component
public class AddPlaylistTextCommand extends AbstractTextCommand {
    private final DiscordServerRepository discordServerRepository;
    private final PlayListRepository playListRepository;
    private final MessageSendService messageSendService;

    public AddPlaylistTextCommand(DiscordServerRepository discordServerRepository, PlayListRepository playListRepository, MessageSendService messageSendService) {
        this.discordServerRepository = discordServerRepository;
        this.playListRepository = playListRepository;
        this.messageSendService = messageSendService;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getArgs().size() < 2) {
            rsp.setMessage("Need arguments: {playlist name} {url}");
        }
        String firstArg = context.getArgs().get(0);
        String secondArg = context.getArgs().get(1);

        if (firstArg.length() > PlaylistId.MAX_PLAYLIST_NAME_LENGTH) {
            rsp.setMessage("Max playlist name length is " + PlaylistId.MAX_PLAYLIST_NAME_LENGTH);
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
        messageSendService.sendInfoMessage(context.getMessageChannel(), "Playlist " + context.getArgs().get(0) + " saved.");
        return true;
    }

    @Override
    public String getDescription() {
        return "Add playlist by name. Use: {command} {playlist name} {url}.";
    }
}
