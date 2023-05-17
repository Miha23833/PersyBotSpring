package com.jerseybot.adapters;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.ButtonCommandRouter;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.TextCommandRouter;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.repositories.DiscordServerRepository;
import com.jerseybot.exception.CommandExecutionException;
import com.jerseybot.exception.TextCommandExecutionException;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserInteractionEventListener extends ListenerAdapter {
    private final TextCommandRouter textCommandRouter;
    private final ButtonCommandRouter buttonCommandRouter;
    private final MessageSendService messageSendService;
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public UserInteractionEventListener(TextCommandRouter textCommandRouter,
                                        ButtonCommandRouter buttonCommandRouter,
                                        MessageSendService messageSendService, DiscordServerRepository discordServerRepository) {
        this.textCommandRouter = textCommandRouter;
        this.buttonCommandRouter = buttonCommandRouter;
        this.messageSendService = messageSendService;
        this.discordServerRepository = discordServerRepository;
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
            log.error("Something broke when processing command", new TextCommandExecutionException(rsp.getException(), guildId, event.getChannel().getIdLong(), event.getMessage().getContentRaw()));
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getMessage().getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {
            CommandExecutionRsp rsp = new CommandExecutionRsp();
            buttonCommandRouter.route(new ButtonCommandContext(event), rsp);

            if (!rsp.isOk()) {
                event.getMessage()
                        .editMessage(MessageEditBuilder.fromMessage(event.getMessage()).setActionRow().build())
                        .queue();
                if (rsp.getMessage() != null) {
                    messageSendService.sendErrorMessage(event.getChannel().asTextChannel(), rsp.getMessage());
                }
                if (rsp.getException() != null) {
                    log.error("Something broke when processing command",
                            new CommandExecutionException(rsp.getException(), event.getGuild() == null ? -1 : event.getGuild().getIdLong(), event.getChannel().getIdLong()));
                }
            } else if (!event.getInteraction().isAcknowledged()) {
                event.getInteraction().deferEdit().queue();
            }
        }
    }
}
