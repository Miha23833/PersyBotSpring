package com.jerseybot.command.button.enums;

public enum BUTTON_ID {
    PLAYER_PAUSE("player_pause"),
    PLAYER_RESUME("player_resume"),
    PLAYER_SKIP("player_skip"),
    PLAYER_STOP("player_stop"),
    PLAYER_QUEUE("player_queue"),

    NEXT_PAGE("next_page"),
    PREV_PAGE("prev_page");

    private final String id;
    BUTTON_ID(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
