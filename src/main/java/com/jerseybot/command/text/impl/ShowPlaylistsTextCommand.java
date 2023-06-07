package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.Playlist;
import com.jerseybot.db.repositories.PlayListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ShowPlaylistsTextCommand extends AbstractTextCommand {
    private final PlayListRepository playListRepository;
    private final MessageSendService messageSendService;

    @Autowired
    public ShowPlaylistsTextCommand(PlayListRepository playListRepository, MessageSendService messageSendService) {
        this.playListRepository = playListRepository;
        this.messageSendService = messageSendService;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        List<Playlist> playlists = playListRepository.findAllByDiscordServerId(context.getGuildId());
        if (playlists.size() == 0) {
            rsp.setMessage("No playlist found.");
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (Playlist playlist: playlists) {
            sb.append("[").append(playlist.getName()).append("]").append("(").append(playlist.getUrl()).append(")\n");
        }
        messageSendService.sendInfoMessage(context.getMessageChannel(), "Available playlists:", sb.toString());
        return true;
    }

    @Override
    public String getDescription() {
        return "Show all saved playlists.";
    }
}
