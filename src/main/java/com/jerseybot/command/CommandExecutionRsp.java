package com.jerseybot.command;

import lombok.Getter;
import lombok.Setter;

public class CommandExecutionRsp {
    @Getter
    @Setter
    private String message;

    @Getter
    @Setter
    private Throwable exception;

    public boolean isOk() {
        return message == null && exception == null;
    }
}
