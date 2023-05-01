package com.jerseybot.command.router;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.command.text.impl.PlayMusicTextCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class CommandRouter {
    private final Map<String, AbstractTextCommand> textCommandRoutes;

    @Autowired
    public CommandRouter(PlayMusicTextCommand playMusicTextCommand) {
        Map<String, AbstractTextCommand> routes = new HashMap<>();

        registerRoutes(routes, playMusicTextCommand, "play", "p");

        this.textCommandRoutes = Collections.unmodifiableMap(routes);
    }

    public void route(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        try {
            if (textCommandRoutes.containsKey(context.getCommand())) {
                textCommandRoutes.get(context.getCommand()).execute(context, rsp);
            } else {
                rsp.setMessage(context.getCommand() + " is not my command.");
            }
        } catch (Throwable e) {
            rsp.setException(e);
            rsp.setMessage("Something went wrong");
        }
    }

    private void registerRoutes(Map<String, AbstractTextCommand> routes, AbstractTextCommand command, String... mappings) {
        for (String mapping: mappings) {
            routes.put(mapping, command);
        }
    }
}
