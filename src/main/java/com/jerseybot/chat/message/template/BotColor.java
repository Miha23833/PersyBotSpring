package com.jerseybot.chat.message.template;

import java.awt.*;

public enum BotColor {
    EMBED(new Color(30, 120, 42));

    private final Color color;

    BotColor(Color color) {
        this.color = color;
    }

    public Color color() {
        return color;
    }
}
