package com.jerseybot.command.text;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class AbstractTextCommand {
    private final List<Function<TextCommandExecutionContext, Boolean>> executingSequence;

    protected abstract void validateArgs(TextCommandExecutionContext context);
    protected abstract boolean runCommand(TextCommandExecutionContext context);

    protected AbstractTextCommand() {
        this.executingSequence = Arrays.asList(
                this::runBefore,
                this::runCommand,
                this::runAfter);
    }

    protected boolean runBefore(TextCommandExecutionContext context){return true;}
    protected boolean runAfter(TextCommandExecutionContext context){return true;}


    protected abstract String getDescription();

    public final void execute(TextCommandExecutionContext context) {
        try {
            boolean canContinue = true;
            for (Function<TextCommandExecutionContext, Boolean> step: executingSequence) {
                if (!canContinue) {
                    return;
                }
                canContinue = step.apply(context);
            }
        } catch (Throwable e) {
            onException(e, context);
        }
    }

    protected void onException(Throwable e, TextCommandExecutionContext context) {
//        if (context.getTextChannel().)
        // TODO: log it
    }


}
