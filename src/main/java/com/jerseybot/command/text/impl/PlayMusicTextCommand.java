package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.utils.BotUtils;
import com.jerseybot.utils.ActionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PlayMusicTextCommand extends AbstractTextCommand {
    private final ActionHelper actionHelper;

    @Autowired
    public PlayMusicTextCommand(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return rsp.isOk();
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        return BotUtils.canBotJoinAndSpeak(context, rsp);
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        actionHelper.joinAndPlay(context, String.join(" ", context.getArgs()));
        return rsp.isOk();
    }

    @Override
    public String getDescription() {
        return "Command to play music. To use write '''<prefix>play <link or name of sound>'''";
    }
}
