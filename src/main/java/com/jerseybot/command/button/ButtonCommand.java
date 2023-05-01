package com.jerseybot.command.button;

import com.jerseybot.command.CommandExecutionRsp;

public interface ButtonCommand {
    void execute(ButtonCommandContext context, CommandExecutionRsp rsp);
}
