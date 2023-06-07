package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.utils.BotUtils;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import io.reactivex.subscribers.DefaultSubscriber;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
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
        if (context.getArgs().isEmpty() || (message.length() < 8 && !(context.getMessageChannel() instanceof ThreadChannel))) {
            rsp.setMessage("Please specify message (should be 8 or more symbols).");
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
                    .thenApply((msgList) -> msgList.stream().map(msg -> new ChatMessage(ChatMessageRole.USER.value(), BotUtils.extractTextData(msg))).collect(Collectors.toList())).get();
        } else {
            messageSendService.sendInfoMessage(context.getMessageChannel(),
                    "Question is generating. I'll create new thread for it as answer will be done.");
            history = new ArrayList<>();
            threadChannel = createThreadChannel((TextChannel) context.getMessageChannel(), title);
        }
        history.add(new ChatMessage(ChatMessageRole.USER.value(), question));

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().messages(history).model("gpt-3.5-turbo").stream(true).build();
        Message message = threadChannel.sendMessage(new InfoMessage(title, "...").template()).complete();

        openAiService.streamChatCompletion(completionRequest).subscribeWith(new MessageStreamSubscriber(title, message));
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

    private static class MessageStreamSubscriber extends DefaultSubscriber<ChatCompletionChunk> {
        private final StringBuilder builder;
        private final String title;
        private final Message message;
        private long lastUpdatedMessageTimeMillis;

        private MessageStreamSubscriber(String title, Message message) {
            this.builder = new StringBuilder();
            this.title = title;
            this.message = message;
            lastUpdatedMessageTimeMillis = System.currentTimeMillis();
        }

        @Override
        public void onNext(ChatCompletionChunk chatCompletionChunk) {
            String text = chatCompletionChunk.getChoices().get(0).getMessage().getContent();
            if (text != null) {
                builder.append(text);
            }
            int minUpdateTimeMillis = 1000;
            if (System.currentTimeMillis() - lastUpdatedMessageTimeMillis >= minUpdateTimeMillis) {
                updateMessage();
                lastUpdatedMessageTimeMillis = System.currentTimeMillis();
            }
        }

        @Override
        public void onError(Throwable t) {
            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromCreateData(new InfoMessage(title, "Error generating answer.").template());
            message.editMessage(messageEditBuilder.build()).queue();
        }

        @Override
        public void onComplete() {
            updateMessage();
        }

        private void updateMessage() {
            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromCreateData(new InfoMessage(title, builder.toString()).template());
            message.editMessage(messageEditBuilder.build()).queue();
        }
    }
}
