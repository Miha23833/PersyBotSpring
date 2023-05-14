package com.jerseybot.utils;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ActionHelper {
    private final MessageSendService messageSendService;
    private final PlayerRepository playerRepository;

    @Autowired
    public ActionHelper(MessageSendService messageSendService, PlayerRepository playerRepository) {
        this.messageSendService = messageSendService;
        this.playerRepository = playerRepository;
    }

    public void joinAndPlay(TextCommandExecutionContext context, String requestingTrack) {
        Member executorMember = Objects.requireNonNull(context.getEvent().getMember());
        VoiceChannel voiceChannel = Objects.requireNonNull(Objects.requireNonNull(executorMember.getVoiceState()).getChannel()).asVoiceChannel();

        if (!BotUtils.isMemberInSameVoiceChannelAsBot(context.getEvent().getMember(), context.getGuild().getSelfMember())) {
            context.getGuild().getAudioManager().openAudioConnection(voiceChannel);
            messageSendService.sendInfoMessage(context.getEvent().getChannel().asTextChannel(), "Connected to " + voiceChannel.getName());
        }

        Player player = playerRepository.get(context.getGuildId());

        player.scheduleTrack(requestingTrack, context.getTextChannel());
        if (context.getGuild().getAudioManager().getSendingHandler() == null) {
            context.getGuild().getAudioManager().setSendingHandler(player.getSendHandler());
        }
    }

}
