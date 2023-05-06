package com.jerseybot.command.button.impl;

import com.jerseybot.chat.message.template.PagingMessage;
import com.jerseybot.chat.pagination.PageableMessage;
import com.jerseybot.chat.pagination.PaginationService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.button.ButtonCommand;
import com.jerseybot.command.button.ButtonCommandContext;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.springframework.stereotype.Component;

@Component
public class PrevPageCommand implements ButtonCommand {
    private final PaginationService paginationService;

    public PrevPageCommand(PaginationService paginationService) {
        this.paginationService = paginationService;
    }

    @Override
    public void execute(ButtonCommandContext context, CommandExecutionRsp rsp) {
        long textChannelId = context.getEvent().getMessage().getChannel().getIdLong();
        Message currentMessage = context.getEvent().getMessage();
        PageableMessage pageableMessage = paginationService.getPagination(textChannelId, context.getEvent().getMessageIdLong());

        if (pageableMessage == null || !pageableMessage.hasPrev()) {
            return;
        }

        MessageEditData nextMessage = MessageEditData.fromCreateData(new PagingMessage(
                pageableMessage.prev(),
                pageableMessage.hasPrev(),
                pageableMessage.hasNext()).template());

        currentMessage.editMessage(nextMessage).queue();
    }
}
