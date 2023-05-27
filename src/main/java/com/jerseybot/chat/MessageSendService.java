package com.jerseybot.chat;

import com.jerseybot.chat.cleaner.MessageType;
import com.jerseybot.chat.cleaner.SelfMessageStore;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.chat.message.template.PagingMessage;
import com.jerseybot.chat.pagination.PAGEABLE_MESSAGE_TYPE;
import com.jerseybot.chat.pagination.PageableMessage;
import com.jerseybot.chat.pagination.PaginationService;
import com.jerseybot.command.button.enums.BUTTON_ID;
import com.jerseybot.command.button.enums.PLAYER_BUTTON;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import jakarta.annotation.Nullable;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jerseybot.utils.DateTimeUtils.toTimeDuration;

@Service
public class MessageSendService {
    private final SelfMessageStore selfMessageStore;
    private final PaginationService paginationService;

    private final Set<String> playerButtonIds;


    @Autowired
    public MessageSendService(SelfMessageStore selfMessageStore, PaginationService paginationService) {
        this.selfMessageStore = selfMessageStore;
        this.paginationService = paginationService;
        this.playerButtonIds = Stream.of(BUTTON_ID.PLAYER_PAUSE, BUTTON_ID.PLAYER_RESUME, BUTTON_ID.PLAYER_SKIP, BUTTON_ID.PLAYER_STOP).map(BUTTON_ID::getId).collect(Collectors.toSet());
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

    public void sendNowPlaying(TextChannel textChannel, AudioTrack track, boolean isPlayerPaused, boolean isNextTrackAbsent) {
        if (textChannel.canTalk()) {
            textChannel
                    .sendMessage(new InfoMessage("Now playing:", getTrackTitle(track)).template())
                    .setActionRow(
                            PLAYER_BUTTON.STOP.button(),
                            isPlayerPaused ? PLAYER_BUTTON.RESUME.button() : PLAYER_BUTTON.PAUSE.button(),
                            PLAYER_BUTTON.SKIP.button(isNextTrackAbsent))
                    .queue(msg -> selfMessageStore.addMessage(MessageType.PLAYER_NOW_PLAYING, textChannel.getIdLong(), msg.getIdLong()));
        }
    }

    public void sendQueuedTrack(TextChannel textChannel, AudioTrack track, boolean isPlayerPaused) {
        if (textChannel.canTalk()) {
            textChannel.sendMessage(new InfoMessage("Queued track: ", getTrackTitle(track)).template())
                    .queue(msg -> updatePlayerButtons(textChannel, isPlayerPaused, false));
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
    public void sendMessage(TextChannel textChannel, String title, String content) {
        sendMessage(textChannel, title, content, null);
    }

    public void sendMessage(TextChannel textChannel, String title, String content, @Nullable MessageType messageType) {
        if (textChannel.canTalk()) {
            textChannel.sendMessage(new InfoMessage(title, content).template())
                    .queue((msg) -> {
                        if (messageType != null) {
                            selfMessageStore.addMessage(messageType, textChannel.getIdLong(), msg.getIdLong());
                        }
                    });
        }
    }

    private void updatePlayerButtons(TextChannel textChannel, boolean isPlayerPaused, boolean isNextTrackAbsent) {
        textChannel.getHistory().retrievePast(10).queue(
                lastMessagesInTextChannel -> lastMessagesInTextChannel.stream().filter(message -> {
                    if (message.getActionRows().isEmpty()) {
                        return false;
                    }
                    List<ItemComponent> components = message.getActionRows().get(0).getComponents();
                    if (components.isEmpty()) {
                        return false;
                    }
                    if (components.stream().noneMatch((ic -> ic instanceof Button))) {
                        return false;
                    }
                    return containsExactlyButtons(message, playerButtonIds);
                }).findFirst().ifPresent(message -> message.editMessage(MessageEditData.fromMessage(message))
                        .setActionRow(
                                PLAYER_BUTTON.STOP.button(),
                                isPlayerPaused ? PLAYER_BUTTON.RESUME.button() : PLAYER_BUTTON.PAUSE.button(),
                                PLAYER_BUTTON.SKIP.button(isNextTrackAbsent))
                        .queue())
        );
    }

    public void sendQueuedTracks(TextChannel textChannel, List<AudioTrack> tracks) {
        sendMessage(textChannel, "Queued tracks:", getQueuedTrackMessage(tracks));
    }

    public void sendRepeatingTrack(TextChannel textChannel, AudioTrack track) {
        sendInfoMessage(textChannel, "Repeating: " + getTrackTitle(track));
    }

    private String getQueuedTrackMessage(List<AudioTrack> tracks) {
        StringBuilder queuedTracksRsp = new StringBuilder();

        int trackInfoRspLineLimit = Math.min(tracks.size(), 10);

        for (int i = 0; i < trackInfoRspLineLimit; i++) {
            queuedTracksRsp.append(getTrackTitle(tracks.get(i))).append("\n");
        }

        if (tracks.size() > trackInfoRspLineLimit) {
            queuedTracksRsp.append('\n');
            queuedTracksRsp.append("And ")
                    .append(tracks.size() - trackInfoRspLineLimit).append(" more");
        }
        return queuedTracksRsp.toString();
    }

    private String getTrackTitle(AudioTrack track) {
        return track.getInfo().author + " - " + track.getInfo().title + " (" + toTimeDuration(track.getInfo().length) + ")";
    }

    private boolean containsExactlyButtons(Message message, Set<String> buttonIds) {
        return message.getButtons().stream().map(Button::getId).filter(Objects::nonNull).allMatch(buttonIds::contains);
    }
}
