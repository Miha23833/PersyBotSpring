package com.jerseybot.adapters;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.router.CommandRouter;
import com.jerseybot.command.text.TextCommandExecutionContext;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserEventListener extends ListenerAdapter {
    private final CommandRouter commandRouter;
    // TODO: move to db and keep dynamically
    private final String prefix = "$";

    @Autowired
    public UserEventListener(CommandRouter commandRouter) {
        this.commandRouter = commandRouter;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getMessage().getAuthor().isBot()) {
            if (event.getMessage().getContentRaw().startsWith(prefix)) {
                CommandExecutionRsp rsp = new CommandExecutionRsp();
                commandRouter.route(new TextCommandExecutionContext(event, prefix), rsp);

                if (rsp.getMessage() != null) {
                    event.getMessage().getChannel().asTextChannel().sendMessage(rsp.getMessage()).queue();
                }
                if (rsp.getException() != null) {
                    // TODO: log it
                }
            }
        }
    }
}
