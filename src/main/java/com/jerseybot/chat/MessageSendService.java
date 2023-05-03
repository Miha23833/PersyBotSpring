package com.jerseybot.chat;

import com.jerseybot.chat.cleaner.MessageType;
import com.jerseybot.chat.cleaner.SelfMessagesCleaner;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.command.button.enums.PLAYER_BUTTON;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageSendService {
    private final SelfMessagesCleaner selfMessagesCleaner;

    @Autowired
    public MessageSendService(SelfMessagesCleaner selfMessagesCleaner) {
        this.selfMessagesCleaner = selfMessagesCleaner;
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

    public void sendNowPlaying(TextChannel textChannel, String title, boolean isPlayerPaused) {
        if (textChannel.canTalk()) {
            textChannel
                    .sendMessage(new InfoMessage("Now playing:", title).template())
                    .setActionRow(
                            PLAYER_BUTTON.STOP.button(),
                            isPlayerPaused ? PLAYER_BUTTON.RESUME.button() : PLAYER_BUTTON.PAUSE.button(),
                            PLAYER_BUTTON.SKIP.button())
                    .queue(msg -> selfMessagesCleaner.addMessage(MessageType.PLAYER_NOW_PLAYING, textChannel.getIdLong(), msg.getIdLong()));
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
