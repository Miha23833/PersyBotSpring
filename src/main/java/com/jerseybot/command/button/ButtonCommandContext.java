package com.jerseybot.command.button;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Objects;

public class ButtonCommandContext {
    private final ButtonInteractionEvent event;

    public ButtonCommandContext(ButtonInteractionEvent event) {
        this.event = event;
    }

    public Guild getGuild() {
        return event.getGuild();
    }

    public ButtonInteractionEvent getEvent() {
        return event;
    }

    public Long getGuildId() {
        return getGuild().getIdLong();
    }

    public String getButtonId() {
        return Objects.requireNonNull(event.getButton()).getId();
    }
}
