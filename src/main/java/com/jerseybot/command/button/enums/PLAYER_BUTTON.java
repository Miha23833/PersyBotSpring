package com.jerseybot.command.button.enums;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public enum PLAYER_BUTTON {
    PAUSE(ButtonStyle.SUCCESS, BUTTON_ID.PLAYER_PAUSE.getId(), Emoji.fromUnicode("U+23F8")),
    RESUME(ButtonStyle.SUCCESS, BUTTON_ID.PLAYER_RESUME.getId(), Emoji.fromUnicode("U+25B6")),
    SKIP(ButtonStyle.PRIMARY, BUTTON_ID.PLAYER_SKIP.getId(), Emoji.fromUnicode("U+23ED")),
    STOP(ButtonStyle.DANGER, BUTTON_ID.PLAYER_STOP.getId(), Emoji.fromUnicode("U+23F9"));

    private final ButtonStyle style;
    private final String id;
    private final Emoji playerImg;


    PLAYER_BUTTON(ButtonStyle style, String id, Emoji playerImg) {
        this.style = style;
        this.id = id;
        this.playerImg = playerImg;
    }

    public Button button() {
        return button(false);
    }

    public Button button(boolean isDisabled) {
        return Button.of(style, id, playerImg).withDisabled(isDisabled);
    }
}
