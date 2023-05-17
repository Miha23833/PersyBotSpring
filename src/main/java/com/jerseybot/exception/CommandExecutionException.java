package com.jerseybot.exception;

public class CommandExecutionException extends Exception{
    private final long guildId;
    private final long textChannelId;

    public CommandExecutionException(Throwable throwable, long guildId, long textChannelId) {
        super(throwable);
        this.guildId = guildId;
        this.textChannelId = textChannelId;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "; GuildId: " + guildId + "; TextChannelId: " + textChannelId;
    }
}
