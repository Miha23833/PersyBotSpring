package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AskChatGptTextCommand extends AbstractTextCommand {
    private final OpenAiService openAiService;
    private final MessageSendService messageSendService;

    @Autowired
    public AskChatGptTextCommand(OpenAiService openAiService, MessageSendService messageSendService) {
        this.openAiService = openAiService;
        this.messageSendService = messageSendService;
    }

    @Override
    protected boolean validateArgs(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        String message = String.join(" ", context.getArgs()).trim();
        if (context.getArgs().isEmpty() || message.length() < 10) {
            rsp.setMessage("Please specify message (should be 10 more symbols).");
            return false;
        }
        return true;
    }

    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        String message = String.join(" ", context.getArgs()).trim();
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(Collections.singletonList(new ChatMessage(ChatMessageRole.USER.value(), message)))
                .model("gpt-3.5-turbo")
                .build();
        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(completionRequest).getChoices();
        if (choices.isEmpty()) {
            rsp.setMessage("Cannot get answer to your question.");
            return false;
        }
        String answer = choices.get(0).getMessage().getContent();
        choices = openAiService.createChatCompletion(ChatCompletionRequest
                .builder()
                .messages(Collections.singletonList(new ChatMessage(ChatMessageRole.USER.value(), "Figure out the title of this text:\n" + answer)))
                .model("gpt-3.5-turbo")
                .build()).getChoices();
        String title =  choices.get(0).getMessage().getContent();
        messageSendService.sendInfoMessage(context.getMessageChannel(), title, answer);
        return true;
    }

    @Override
    public String getDescription() {
        return null;
    }
}
