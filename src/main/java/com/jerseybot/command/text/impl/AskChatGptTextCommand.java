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
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
            rsp.setMessage("Please specify message (should be 10 or more symbols).");
            return false;
        }
        return true;
    }

    @SneakyThrows
    @Override
    protected boolean runCommand(TextCommandExecutionContext context, CommandExecutionRsp rsp) {
        String question = String.join(" ", context.getArgs()).trim();
        String title = getTitleOfQuestion(question);

        ThreadChannel threadChannel;
        List<ChatMessage> history;
        if (context.getMessageChannel() instanceof ThreadChannel) {
            threadChannel = (ThreadChannel) context.getMessageChannel();
            history = threadChannel.getIterableHistory().takeAsync(10)
                    .thenApply((msgList) -> msgList.stream().map(msg -> new ChatMessage(ChatMessageRole.USER.value(), msg.getContentRaw())).collect(Collectors.toList())).get();
        } else {
            threadChannel = null;
            messageSendService.sendInfoMessage(context.getMessageChannel(),
                    "Question is generating. I'll create new thread for it as answer will be done.");
            history = new ArrayList<>();
        }
        history.add(new ChatMessage(ChatMessageRole.USER.value(), question));

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(history)
                .model("gpt-3.5-turbo")
                .build();
        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(completionRequest).getChoices();
        if (choices.isEmpty()) {
            rsp.setMessage("Cannot get answer to your question.");
            return false;
        }
        String answer = choices.get(0).getMessage().getContent();
        if (threadChannel == null) {
            threadChannel = createThreadChannel((TextChannel) context.getMessageChannel(), title);
        }
        messageSendService.sendInfoMessage(threadChannel, title, answer);
        return true;
    }

    private ThreadChannel createThreadChannel(TextChannel textChannel, String title) {
        return textChannel.createThreadChannel(title).complete();
    }

    private String getTitleOfQuestion(String question) {
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(Collections.singletonList(new ChatMessage(ChatMessageRole.USER.value(), "Create title for this question. Not more than 100 symbols and use language of the question:\n" + question)))
                .model("gpt-3.5-turbo")
                .build();
        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(completionRequest).getChoices();
        return choices.get(0).getMessage().getContent();
    }

    @Override
    public String getDescription() {
        return null;
    }
}
