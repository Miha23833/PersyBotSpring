package com.jerseybot.chat.message.template;

import com.jerseybot.chat.pagination.PAGINATION_BUTTON;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class PagingMessage implements MessageTemplate {
    private final MessageCreateData message;
    boolean hasPrev, hasNext;

    public PagingMessage(MessageCreateData message, boolean hasPrev, boolean hasNext) {
        this.message = message;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
    }

    @Override
    public MessageCreateData template() {
        return MessageCreateBuilder.from(message).setActionRow(
                PAGINATION_BUTTON.PREVIOUS.button(!hasPrev),
                PAGINATION_BUTTON.NEXT.button(!hasNext)).build();
    }
}
