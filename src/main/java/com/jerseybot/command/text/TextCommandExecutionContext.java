package com.jerseybot.command.text;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class TextCommandExecutionContext {
    @Getter
    private final MessageReceivedEvent event;
    @Getter
    private final List<String> args;
    @Getter
    private final String command;

    public TextCommandExecutionContext(MessageReceivedEvent event, String prefix) {
        this.event = Objects.requireNonNull(event);
        Objects.requireNonNull(prefix);

        String[] split = event.getMessage().getContentRaw()
                .replaceFirst("(?i)" + Pattern.quote(prefix), "")
                .split("\\s+");

        this.command = split[0].toLowerCase();


        this.args = Arrays.asList(split).subList(1, split.length);
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public Long getGuildId() {
        return getGuild().getIdLong();
    }

    public GuildMessageChannel getMessageChannel() {
        return this.event.getChannel().asGuildMessageChannel();
    }
}
