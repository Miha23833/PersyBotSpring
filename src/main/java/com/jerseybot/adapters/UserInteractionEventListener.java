package com.jerseybot.adapters;

import com.jerseybot.JDAService;
import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.ButtonCommandRouter;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.TextCommandRouter;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.text.TextCommandExecutionContext;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserInteractionEventListener extends ListenerAdapter {
    private final TextCommandRouter textCommandRouter;
    private final ButtonCommandRouter buttonCommandRouter;
    private final MessageSendService messageSendService;

    // TODO: move to db and keep dynamically
    private final String prefix = "$";

    @Autowired
    public UserInteractionEventListener(TextCommandRouter textCommandRouter,
                                        ButtonCommandRouter buttonCommandRouter,
                                        MessageSendService messageSendService,
                                        JDAService jdaService) {
        this.textCommandRouter = textCommandRouter;
        this.buttonCommandRouter = buttonCommandRouter;
        this.messageSendService = messageSendService;

        jdaService.getJda().addEventListener(this);
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
            messageSendService.sendErrorMessage(event.getMessage().getChannel().asTextChannel(), rsp.getMessage());
        }
        if (rsp.getException() != null) {
            // TODO: log it
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getMessage().getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
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
