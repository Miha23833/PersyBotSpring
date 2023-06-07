package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.utils.BotUtils;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class JoinToVoiceChannelTextCommand extends AbstractTextCommand {
    private final MessageSendService messageSendService;

    @Autowired
    public JoinToVoiceChannelTextCommand(MessageSendService messageSendService) {
        this.messageSendService = messageSendService;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return rsp.isOk();
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        Member requestingMember = context.getEvent().getMember();
        if (requestingMember == null) {
            return false;
        }
        GuildVoiceState guildVoiceState = requestingMember.getVoiceState();
        if (guildVoiceState == null) {
            return false;
        }

        AudioChannelUnion voiceChannel = guildVoiceState.getChannel();
        if (voiceChannel == null || !BotUtils.isMemberInVoiceChannel(requestingMember)) {
            rsp.setMessage("Please join to a voice channel first");
            return false;
        }

        if (!BotUtils.isMemberInVoiceChannel(context.getGuild().getSelfMember(), voiceChannel.asVoiceChannel())
                && !BotUtils.canJoin(requestingMember, voiceChannel.asVoiceChannel())) {
            rsp.setMessage("I cannot connect to your voice channel");
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
            messageSendService.sendInfoMessage(context.getEvent().getChannel().asGuildMessageChannel(), "Connected to " + voiceChannel.getName());
        }
        return true;
    }

    @Override
    public String getDescription() {
        return "Join to voice channel.";
    }
}
