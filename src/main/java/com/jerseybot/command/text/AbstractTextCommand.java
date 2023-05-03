package com.jerseybot.command.text;

import com.jerseybot.command.CommandExecutionRsp;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

public abstract class AbstractTextCommand {
    private final List<BiFunction<TextCommandExecutionContext, CommandExecutionRsp, Boolean>> executingSequence;

    protected abstract void validateArgs(TextCommandExecutionContext context);
    protected abstract boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp);

    protected AbstractTextCommand() {
        this.executingSequence = Arrays.asList(
                this::runBefore,
                this::runCommand,
                this::runAfter);

    }

    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp){return true;}
    public final void execute(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        boolean canContinue = true;
        for (BiFunction<TextCommandExecutionContext, CommandExecutionRsp, Boolean> step: executingSequence) {
            if (!canContinue) {
                return;
            }
            canContinue = step.apply(context, rsp);
        }
    }
    protected boolean runAfter(TextCommandExecutionContext context, CommandExecutionRsp rsp){return true;}

    protected abstract String getDescription();
}