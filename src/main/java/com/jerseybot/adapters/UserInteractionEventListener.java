package com.jerseybot.adapters;

import com.jerseybot.JDAService;
import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.ButtonCommandRouter;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.TextCommandRouter;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.repositories.DiscordServerRepository;
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
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public UserInteractionEventListener(TextCommandRouter textCommandRouter,
                                        ButtonCommandRouter buttonCommandRouter,
                                        MessageSendService messageSendService,
                                        JDAService jdaService, DiscordServerRepository discordServerRepository) {
        this.textCommandRouter = textCommandRouter;
        this.buttonCommandRouter = buttonCommandRouter;
        this.messageSendService = messageSendService;
        this.discordServerRepository = discordServerRepository;

        jdaService.getJda().addEventListener(this);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot() || !event.getMessage().getChannel().asTextChannel().canTalk()) {
            return;
        }
        long guildId = event.getGuild().getIdLong();
        DiscordServer discordServer = discordServerRepository.getOrCreateDefault(guildId);

        CommandExecutionRsp rsp = new CommandExecutionRsp();
        if (event.getMessage().getContentRaw().startsWith(discordServer.getSettings().getPrefix())) {
            textCommandRouter.route(new TextCommandExecutionContext(event, discordServer.getSettings().getPrefix()), rsp);
        }

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
