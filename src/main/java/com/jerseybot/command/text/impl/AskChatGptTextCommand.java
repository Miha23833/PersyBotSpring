package com.jerseybot.command.text.impl;

import com.jerseybot.chat.MessageSendService;
import com.jerseybot.chat.message.template.InfoMessage;
import com.jerseybot.command.CommandExecutionRsp;
import com.jerseybot.command.text.AbstractTextCommand;
import com.jerseybot.command.text.TextCommandExecutionContext;
import com.jerseybot.config.BotConfig;
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
    private final int maxHistoryLength;
    private final int streamMessageUpdateMillis;

    @Autowired
    public AskChatGptTextCommand(OpenAiService openAiService, MessageSendService messageSendService, BotConfig botConfig) {
        this.openAiService = openAiService;
        this.messageSendService = messageSendService;
        this.maxHistoryLength = botConfig.getChatgptMaxHistoryLength();
        this.streamMessageUpdateMillis = botConfig.getChatgptStreamMessageUpdateMillis();
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

        String title = question.substring(0, Math.min(question.length(), 100));
        ThreadChannel threadChannel;
        List<ChatMessage> history;
        if (context.getMessageChannel() instanceof ThreadChannel) {
            threadChannel = (ThreadChannel) context.getMessageChannel();
            history = threadChannel.getIterableHistory()
                    .takeAsync(this.maxHistoryLength)
                    .thenApply((msgList) -> msgList.stream().map(this::buildChatMessage).collect(Collectors.toList()))
                    .get();
        } else {
            threadChannel = createThreadChannel((TextChannel) context.getMessageChannel(), title);
            messageSendService.sendMessage(threadChannel, "Question", question);
            history = new ArrayList<>();
        }
        history.add(new ChatMessage(ChatMessageRole.USER.value(), question));

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder().messages(history).model("gpt-3.5-turbo").stream(true).build();
        String initialTitle = "Processing answer...";
        Message message = threadChannel.sendMessage(new InfoMessage(initialTitle, "...").template()).complete();

        openAiService.streamChatCompletion(completionRequest).subscribeWith(new MessageStreamSubscriber(initialTitle, message));
        return true;
    }

    @Override
    public String getDescription() {
        return "Ask Chat-GPT something! I will create new thread for conversation and send the answer to it. You can continue conversation with this command in thread.";
    }

    private ThreadChannel createThreadChannel(TextChannel textChannel, String title) {
        return textChannel.createThreadChannel(title).complete();
    }

    private ChatMessage buildChatMessage(Message message) {
        return new ChatMessage(ChatMessageRole.USER.value(), BotUtils.extractTextData(message));
    }

    private String getTitleOfQuestion(String question) {
        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(Collections.singletonList(new ChatMessage(ChatMessageRole.USER.value(), "Create title for this question. Not more than 100 symbols and use language of the question:\n" + question)))
                .model("gpt-3.5-turbo")
                .build();
        List<ChatCompletionChoice> choices = openAiService.createChatCompletion(completionRequest).getChoices();
        return choices.get(0).getMessage().getContent();
    }

    private class MessageStreamSubscriber extends DefaultSubscriber<ChatCompletionChunk> {
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
            if (System.currentTimeMillis() - lastUpdatedMessageTimeMillis >= AskChatGptTextCommand.this.streamMessageUpdateMillis) {
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
            String content = builder.toString();
            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromCreateData(new InfoMessage(AskChatGptTextCommand.this.getTitleOfQuestion(content), content).template());
            message.editMessage(messageEditBuilder.build()).queue();
        }

        private void updateMessage() {
            MessageEditBuilder messageEditBuilder = MessageEditBuilder.fromCreateData(new InfoMessage(title, builder.toString()).template());
            message.editMessage(messageEditBuilder.build()).queue();
        }
    }
}
