package com.jerseybot.chat.cleaner;

public enum MessageType {
    SIMPLE_INFORMATION(1),

    PLAYER_NOW_PLAYING(1),
    PLAYER_STATE(2),
    PLAYER_INFO(40),
    PLAYER_QUEUE(1),

    BUTTON_ERROR(3),

    ERROR(5);


    public int getMaxMessagesCount() {
        return maxMessagesCount;
    }

    private final int maxMessagesCount;

    MessageType(int maxMessagesCount) {
        this.maxMessagesCount = maxMessagesCount;
    }
}
