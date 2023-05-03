package com.jerseybot.chat.message.template;

import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public interface MessageTemplate {
    MessageCreateData template();
}
