package com.jerseybot.command.text.impl;

import com.google.common.collect.Lists;
import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.chat.pagination.PAGEABLE_MESSAGE_TYPE;
import com.jerseybot.chat.pagination.PageableMessage;
import com.jerseybot.chat.pagination.PaginationService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.jerseybot.utils.DateTimeUtils.toTimeDuration;

@Component
public class ShowQueueTextCommand extends AbstractTextCommand {
    private final MessageSendService messageSendService;
    private final PlayerRepository playerRepository;

    @Autowired
    public ShowQueueTextCommand(MessageSendService messageSendService, PaginationService cache, PlayerRepository playerRepository) {
        this.messageSendService = messageSendService;
        this.playerRepository = playerRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return rsp.isOk();
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (!playerRepository.hasInitializedPlayer(context.getGuildId())) {
            return false;
        }
        Player player = playerRepository.get(context.getGuildId());
        List<String> queue = player.getScheduledTracks()
                .stream()
                .map(AudioTrack::getInfo)
                .map(info -> info.author + " - " + info.title + " (" + toTimeDuration(info.length) + ")")
                .collect(Collectors.toList());
        if (queue.isEmpty()) {
            messageSendService.sendErrorMessage(context.getTextChannel(), "The track queue is empty");
            return false;
        }

        PageableMessage.Builder pageableMessage
                = PageableMessage.builder();
        Lists.partition(queue, 8)
                .stream()
                .map(part -> new InfoMessage("Now playing tracks:", String.join("\n ", part)).template())
                .forEach(pageableMessage::addMessage);

        messageSendService.sendPageableMessage(pageableMessage, context.getEvent().getChannel().asTextChannel(), PAGEABLE_MESSAGE_TYPE.PLAYER_QUEUE);
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
