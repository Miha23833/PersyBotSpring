package com.jerseybot.exception;

public class TextCommandExecutionException extends CommandExecutionException {
    private final String triggeringChatMessage;

    public TextCommandExecutionException(Throwable throwable, long guildId, long textChannelId, String triggeringChatMessage) {
        super(throwable, guildId, textChannelId);
        this.triggeringChatMessage = triggeringChatMessage;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "; chatMessage:" + triggeringChatMessage;
    }
}
