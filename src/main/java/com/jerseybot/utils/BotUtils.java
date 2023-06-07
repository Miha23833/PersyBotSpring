package com.jerseybot.utils;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.TextCommandExecutionContext;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface BotUtils {
    static boolean isMemberInVoiceChannel(Member member) {
        return Objects.requireNonNull(member.getVoiceState()).inAudioChannel();
    }

    static boolean isMemberInSameVoiceChannelAsBot(Member member, Member selfMember) {
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = selfMember.getVoiceState();

        if (memberVoiceState == null || selfVoiceState == null) {
            return false;
        }
        return memberVoiceState.getChannel() != null && memberVoiceState.getChannel().equals(selfVoiceState.getChannel());
    }

    static boolean canSpeak(Member member) {
        return member.hasPermission(Permission.VOICE_SPEAK);
    }

    static boolean canJoin(Member member, VoiceChannel targetChannel) {
        return (targetChannel.getUserLimit() == 0 || targetChannel.getUserLimit() - targetChannel.getMembers().size() > 0)
                && member.hasPermission(Permission.VOICE_CONNECT);
    }

    static boolean isMemberInVoiceChannel(Member selfMember, VoiceChannel targetChannel) {
        return selfMember.getVoiceState() != null && selfMember.getVoiceState().getChannel() != null
                && selfMember.getVoiceState().getChannel().getIdLong() == targetChannel.getIdLong();
    }

    static boolean canWrite(Member selfMember, TextChannel targetChannel) {
        return selfMember.hasPermission(Permission.MESSAGE_SEND) && targetChannel.canTalk();
    }

    static boolean canBotJoinAndSpeak(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        Member requestingMember = context.getEvent().getMember();
        if (requestingMember == null) {
            rsp.setMessage("Please join to a voice channel first");
            return false;
        }
        GuildVoiceState guildVoiceState = requestingMember.getVoiceState();
        if (guildVoiceState == null) {
            rsp.setMessage("Please join to a voice channel first");
            return false;
        }
        AudioChannelUnion voiceChannel = guildVoiceState.getChannel();
        if (voiceChannel == null) {
            rsp.setMessage("Please join to a voice channel first");
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

    static String toHypertext(String text, String link) {
        return "[" + text + "]" + "(" + link + ")";
    }

    static String bold(@NotNull String text) {
        return String.join("", "**", text, "**");
    }

    static String extractTextData(Message message) {
        if (message.getEmbeds().size() == 0) {
            return message.getContentRaw();
        }
        return message.getEmbeds().get(0).getDescription();
    }
}
