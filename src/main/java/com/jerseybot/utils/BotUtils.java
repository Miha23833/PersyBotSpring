package com.jerseybot.utils;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
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

    static void sendMessage(@NotNull String text, @NotNull TextChannel channel) {
        channel.sendMessage(text).queue();
    }

    static void sendMessage(@NotNull MessageCreateData message, @NotNull TextChannel channel) {
        channel.sendMessage(message).queue();
    }

    static void sendPersonalMessage(@NotNull String text, @NotNull User user) {
        user.openPrivateChannel().queue((channel) -> channel.sendMessage(text).queue());
    }

//    static void sendPageableMessage(PageableMessage.Builder message, TextChannel channel, PAGEABLE_MESSAGE_TYPE type) {
//        sendPageableMessage(
//                message,
//                channel,
//                type,
//                ServiceAggregator.getInstance().get(CacheService.class).get(PageableMessageCache.class));
//    }
//
//    static void sendPageableMessage(PageableMessage.Builder message, TextChannel channel, PAGEABLE_MESSAGE_TYPE type, PageableMessageCache cache) {
//        if (message.size() == 1) {
//            sendMessage(new PagingMessage(message.get(0), false, false).template(), channel);
//        } else {
//            channel.sendMessage(new PagingMessage(message.get(0), false, true).template())
//                    .queue(success -> cache.add(success.getChannel().getIdLong(), PAGEABLE_MESSAGE_TYPE.PLAYLISTS, message.build(success.getIdLong())));
//        }
//    }

    static String toHypertext(String text, String link) {
        return "[" + text + "]" + "(" + link + ")";
    }

    static String bold(@NotNull String text) {
        return String.join("", "**", text, "**");
    }
}
