package com.jerseybot.command;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.button.ButtonCommand;
import com.jerseybot.command.button.ButtonCommandContext;
import com.jerseybot.command.button.enums.BUTTON_ID;
import com.jerseybot.command.button.impl.PauseAudioPlayerButtonCommand;
import com.jerseybot.command.button.impl.ResumeAudioPlayerButtonCommand;
import com.jerseybot.command.button.impl.SkipAudioPlayerButtonCommand;
import com.jerseybot.command.button.impl.StopAudioPlayerButtonCommand;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import org.apache.commons.collections4.map.UnmodifiableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ButtonCommandRouter {
    private final Map<String, ButtonCommand> buttonCommandRoutes;

    @Autowired
    public ButtonCommandRouter(PauseAudioPlayerButtonCommand pauseAudioPlayerButtonCommand,
                               ResumeAudioPlayerButtonCommand resumeAudioPlayerButtonCommand,
                               SkipAudioPlayerButtonCommand skipAudioPlayerButtonCommand,
                               StopAudioPlayerButtonCommand stopAudioPlayerButtonCommand) {
        Map<String, ButtonCommand> routes = new HashMap<>();
        registerRoutes(routes, BUTTON_ID.PLAYER_PAUSE, pauseAudioPlayerButtonCommand);
        registerRoutes(routes, BUTTON_ID.PLAYER_RESUME, resumeAudioPlayerButtonCommand);
        registerRoutes(routes, BUTTON_ID.PLAYER_SKIP, skipAudioPlayerButtonCommand);
        registerRoutes(routes, BUTTON_ID.PLAYER_STOP, stopAudioPlayerButtonCommand);
        this.buttonCommandRoutes = Collections.unmodifiableMap(routes);
    }

    public void route(ButtonCommandContext context, CommandExecutionRsp rsp) {
        try {
            String command = context.getButtonId().toLowerCase();
            if (buttonCommandRoutes.containsKey(command)) {
                buttonCommandRoutes.get(command).execute(context, rsp);
            }
        } catch (Throwable e) {
            rsp.setException(e);
            rsp.setMessage("Something went wrong");
        }
    }

    private void registerRoutes(Map<String, ButtonCommand> routes, BUTTON_ID buttonId, ButtonCommand command) {
        routes.put(buttonId.getId(), command);
    }

}
