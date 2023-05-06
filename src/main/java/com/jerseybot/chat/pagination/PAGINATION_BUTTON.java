package com.jerseybot.chat.pagination;

import com.jerseybot.command.button.enums.BUTTON_ID;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

public enum PAGINATION_BUTTON {
    NEXT(ButtonStyle.PRIMARY, BUTTON_ID.NEXT_PAGE.getId(), Emoji.fromUnicode("U+25B6")),
    PREVIOUS(ButtonStyle.PRIMARY, BUTTON_ID.PREV_PAGE.getId(), Emoji.fromUnicode("U+25C0"));


    private final ButtonStyle style;
    private final String id;
    private final Emoji playerImg;


    PAGINATION_BUTTON(ButtonStyle style, String id, Emoji playerImg) {
        this.style = style;
        this.id = id;
        this.playerImg = playerImg;
    }

    public Button button(boolean isDisabled) {
        return Button.of(style, id, playerImg).withDisabled(isDisabled);
    }
}
