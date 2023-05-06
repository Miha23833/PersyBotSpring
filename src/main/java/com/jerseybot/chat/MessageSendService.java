package com.jerseybot.chat;

import com.jerseybot.chat.cleaner.MessageType;
import com.jerseybot.chat.cleaner.SelfMessagesCleaner;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.chat.message.template.PagingMessage;
import com.jerseybot.chat.pagination.PAGEABLE_MESSAGE_TYPE;
import com.jerseybot.chat.pagination.PageableMessage;
import com.jerseybot.chat.pagination.PaginationService;
import com.jerseybot.command.button.enums.PLAYER_BUTTON;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSendService {
    private final SelfMessagesCleaner selfMessagesCleaner;
    private final PaginationService paginationService;

    @Autowired
    public MessageSendService(SelfMessagesCleaner selfMessagesCleaner, PaginationService paginationService) {
        this.selfMessagesCleaner = selfMessagesCleaner;
        this.paginationService = paginationService;
    }

    public void sendInfoMessage(TextChannel textChannel, String content) {
        sendInfoMessage(textChannel, "Info", content);
    }

    public void sendInfoMessage(TextChannel textChannel, String title, String content) {
        sendMessage(textChannel, title, content, MessageType.SIMPLE_INFORMATION);
    }

    public void sendErrorMessage(TextChannel textChannel, String content) {
        sendMessage(textChannel, "Error", content, MessageType.ERROR);
    }

    public void sendNowPlaying(TextChannel textChannel, String title, boolean isPlayerPaused, boolean isNextTrackAbsent) {
        if (textChannel.canTalk()) {
            textChannel
                    .sendMessage(new InfoMessage("Now playing:", title).template())
                    .setActionRow(
                            PLAYER_BUTTON.STOP.button(),
                            isPlayerPaused ? PLAYER_BUTTON.RESUME.button() : PLAYER_BUTTON.PAUSE.button(),
                            PLAYER_BUTTON.SKIP.button(isNextTrackAbsent))
                    .queue(msg -> selfMessagesCleaner.addMessage(MessageType.PLAYER_NOW_PLAYING, textChannel.getIdLong(), msg.getIdLong()));
        }
    }

    public void sendPageableMessage(PageableMessage.Builder message, TextChannel channel, PAGEABLE_MESSAGE_TYPE type) {
        if (message.size() == 1) {
            channel.sendMessage(new PagingMessage(message.get(0), false, false).template()).queue();
        } else if (message.size() > 1) {
            channel.sendMessage(new PagingMessage(message.get(0), false, true).template())
                    .queue(success -> paginationService.registerPagination(success.getChannel().getIdLong(), type, message.build(success.getIdLong())));
        }
    }

    public void sendMessage(TextChannel textChannel, String title, String content, @Nullable MessageType messageType) {
        if (textChannel.canTalk()) {
            textChannel.sendMessage(new InfoMessage(title, content).template())
                    .queue((msg) -> {
                        if (messageType != null) {
                            selfMessagesCleaner.addMessage(messageType, textChannel.getIdLong(), msg.getIdLong());
                        }
                    });
        }
    }

}
