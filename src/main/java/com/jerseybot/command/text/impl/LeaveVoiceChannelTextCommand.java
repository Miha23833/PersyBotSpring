package com.jerseybot.command.text.impl;

import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.music.PlayerRepository;
import com.jerseybot.utils.BotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LeaveVoiceChannelTextCommand extends AbstractTextCommand {
    private final PlayerRepository playerRepository;

    @Autowired
    public LeaveVoiceChannelTextCommand(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Override
    protected void validateArgs(TextCommandExecutionContext context) {
    }

    @Override
    protected boolean runBefore(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        if (context.getEvent().getMember() == null) return false;

        if (!BotUtils.isMemberInVoiceChannel(context.getGuild().getSelfMember())) {
            return false;
        }

        if (!BotUtils.isMemberInSameVoiceChannelAsBot(context.getGuild().getSelfMember(), context.getEvent().getMember())){
            rsp.setMessage("You must be in the same channel as me");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        context.getGuild().getAudioManager().closeAudioConnection();
        if (this.playerRepository.hasInitializedPlayer(context.getGuildId())) {
            this.playerRepository.get(context.getGuildId()).stop();
        }
        return true;
    }

    @Override
    protected String getDescription() {
        return null;
    }
}
