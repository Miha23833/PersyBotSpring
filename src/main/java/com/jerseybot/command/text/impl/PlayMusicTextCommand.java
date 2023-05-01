package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.music.player.Player;
import com.jerseybot.utils.BotUtils;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static org.springframework.core.io.support.ResourcePatternUtils.isUrl;

@Component
public class PlayMusicTextCommand extends AbstractTextCommand {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayMusicTextCommand(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        validateArgs(context);

        Member requestingMember = context.getEvent().getMember();
        if (requestingMember == null) {
            return false;
        }
        GuildVoiceState guildVoiceState = requestingMember.getVoiceState();
        if (guildVoiceState == null) {
            return false;
        }
        AudioChannelUnion voiceChannel = guildVoiceState.getChannel();
        if (voiceChannel == null) {
            return false;
        }

        if (!BotUtils.isMemberInVoiceChannel(requestingMember)) {
            rsp.setMessage("Please join to a voice channel first");
            return false;
        }

        if (!BotUtils.isMemberInVoiceChannel(context.getGuild().getSelfMember(), voiceChannel.asVoiceChannel())
                && !BotUtils.canJoin(requestingMember, voiceChannel.asVoiceChannel())) {
            rsp.setMessage("I cannot connect to your voice channel");
            return false;
        }

        if (!BotUtils.canSpeak(context.getGuild().getSelfMember())) {
            rsp.setMessage("I cannot speak in your voice channel");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        Member executorMember = Objects.requireNonNull(context.getEvent().getMember());
        VoiceChannel voiceChannel = Objects.requireNonNull(Objects.requireNonNull(executorMember.getVoiceState()).getChannel()).asVoiceChannel();

        if (!BotUtils.isMemberInSameVoiceChannelAsBot(context.getEvent().getMember(), context.getGuild().getSelfMember())) {
            context.getGuild().getAudioManager().openAudioConnection(voiceChannel);
            BotUtils.sendMessage("Connected to " + voiceChannel.getName(), context.getEvent().getChannel().asTextChannel());
        }

        Player player = playerRepository.get(context.getGuildId());

        String requestingTrack = String.join(" ", context.getArgs());
        if (!isUrl(requestingTrack)) {
            requestingTrack = "ytsearch:" + requestingTrack;
        }
        player.scheduleTrack(requestingTrack);
        context.getGuild().getAudioManager().setSendingHandler(player.getSendHandler());

        return true;
    }

    @Override
    public String getDescription() {
        return "Command to play music. To use write '''<prefix>play <link or name of sound>'''";
    }

    @Override
    protected void validateArgs(TextCommandExecutionContext context) {
//        if (!hasMinimumArgs(context.getArgs())){
//            BotUtils.sendMessage(getDescription(), context.getTextChannel());
//        }

//        String link = String.join(" ", args);
//        if (isUrl(link) && !isPlayableLink(link)) {
//            validationResult.setInvalid(TEXT_COMMAND_REJECT_REASON.WRONG_VALUE, "I cannot play this url");
//        }
    }
}
