package com.jerseybot.chat.message.template;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

public class InfoMessage implements MessageTemplate {
    private final String title;
    private final String content;

    public InfoMessage(String title, String content) {
        this.title = title;
        this.content = content;
    }

    @Override
    public MessageCreateData template() {
        MessageEmbed embedMessage = new EmbedBuilder().setColor(BotColor.EMBED.color()).setTitle(title).setDescription(content) .build();
        return new MessageCreateBuilder().setEmbeds(embedMessage).build();
    }
}
