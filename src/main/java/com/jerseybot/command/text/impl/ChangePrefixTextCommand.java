package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.db.entities.DiscordServer;
import com.jerseybot.db.entities.DiscordServerSettings;
import com.jerseybot.db.repositories.DiscordServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChangePrefixTextCommand extends AbstractTextCommand {
    private final DiscordServerRepository discordServerRepository;

    @Autowired
    public ChangePrefixTextCommand(DiscordServerRepository discordServerRepository) {
        this.discordServerRepository = discordServerRepository;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getArgs().isEmpty()) {
            rsp.setMessage("Please specify the prefix.");
        } else if (context.getArgs().get(0).length() > DiscordServerSettings.PREFIX_MAX_LEN) {
            rsp.setMessage("Prefix is too long. Max length is " + DiscordServerSettings.PREFIX_MAX_LEN + ".");
        } else if (context.getArgs().get(0).equals("/")) {
            return false;
        }
        return rsp.isOk();
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        DiscordServer discordServer = this.discordServerRepository.getOrCreateDefault(context.getGuildId());
        discordServer.getSettings().setPrefix(context.getArgs().get(0));
        this.discordServerRepository.save(discordServer);

        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
