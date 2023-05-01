package com.jerseybot.command.text.impl;

import com.jerseybot.chat.TextChannelsToInteractStore;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SetDefaultInteractionTextChannelTextCommand extends AbstractTextCommand {
    public final TextChannelsToInteractStore textChannelsToInteractStore;

    @Autowired
    public SetDefaultInteractionTextChannelTextCommand(TextChannelsToInteractStore textChannelsToInteractStore) {
        this.textChannelsToInteractStore = textChannelsToInteractStore;
    }

    @Override
    protected void validateArgs(TextCommandExecutionContext context) {}

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getEvent().getMessage().getChannel().asTextChannel().canTalk()) {
            textChannelsToInteractStore.update(context.getGuildId(), context.getTextChannel().getIdLong());
        }
        context.getTextChannel().sendMessage("I will listen and send messages to this channel.").queue();
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
