package com.jerseybot.adapters;

import com.jerseybot.chat.TextChannelsToInteractStore;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.ButtonCommandRouter;
import com.jerseybot.command.TextCommandRouter;
import com.jerseybot.command.text.TextCommandExecutionContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserInteractionEventListener extends ListenerAdapter {
    private final TextCommandRouter textCommandRouter;
    private final ButtonCommandRouter buttonCommandRouter;

    // TODO: move to db and keep dynamically
    private final String prefix = "$";

    @Autowired
    public UserInteractionEventListener(TextCommandRouter textCommandRouter,
                                        ButtonCommandRouter buttonCommandRouter) {
        this.textCommandRouter = textCommandRouter;
        this.buttonCommandRouter = buttonCommandRouter;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot() ||
                !event.getMessage().getContentRaw().startsWith(prefix) ||
                !event.getMessage().getChannel().asTextChannel().canTalk()) {
            return;
        }
        CommandExecutionRsp rsp = new CommandExecutionRsp();
        textCommandRouter.route(new TextCommandExecutionContext(event, prefix), rsp);

        if (rsp.getMessage() != null) {
            event.getMessage().getChannel().asTextChannel().sendMessage(rsp.getMessage()).queue();
        }
        if (rsp.getException() != null) {
            // TODO: log it
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getMessage().getAuthor().isBot()) {
            if (event.getMessage().getContentRaw().startsWith(prefix)) {
                CommandExecutionRsp rsp = new CommandExecutionRsp();
                buttonCommandRouter.route(new ButtonCommandContext(event), rsp);

                if (rsp.getMessage() != null || rsp.getException() != null) {
                    event.getMessage()
                            .editMessage(MessageEditBuilder.fromMessage(event.getMessage()).setActionRow().build())
                            .queue();
                } else if (!event.getInteraction().isAcknowledged()) {
                    event.getInteraction().deferEdit().queue();
                }
            }
        }
    }
}
