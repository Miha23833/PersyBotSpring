package com.jerseybot.chat.cleaner;

import com.jerseybot.JDAService;
import com.jerseybot.utils.collections.FreshLimitedQueue;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
public class SelfMessagesCleaner {
    private final JDA jda;

    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Map<Long, Map<MessageType, FreshLimitedQueue<Long>>> messages = new HashMap<>();

    @Autowired
    public SelfMessagesCleaner(JDAService jdaService) {
        this.jda = jdaService.getJda();
    }

    public void addMessage(MessageType messageType, Long textChannelId, Long msgId) {
        this.rwLock.writeLock().lock();
        try {
            FreshLimitedQueue<Long> messageQueue = getQueue(textChannelId, messageType);
            messageQueue.add(msgId);
            if (!messageQueue.isOldEmpty()) {
                List<Long> messagesToRemove = messageQueue.clearOld();
                TextChannel channel = this.jda.getTextChannelById(textChannelId);
                if (channel == null) {
                    this.messages.remove(textChannelId);
                    return;
                }
                if (messagesToRemove.size() == 1) {
                    channel.deleteMessageById(messagesToRemove.get(0)).queue();
                } else if (messagesToRemove.size() > 1) {
                    channel.deleteMessagesByIds(messagesToRemove.stream().map(Object::toString).collect(Collectors.toList())).queue();
                }
            }
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }

    private FreshLimitedQueue<Long> getQueue(Long guildId, MessageType messageType) {
        return getMapByMessageType(guildId)
                .computeIfAbsent(messageType, x -> new FreshLimitedQueue<>(messageType.getMaxMessagesCount()));
    }

    private Map<MessageType, FreshLimitedQueue<Long>> getMapByMessageType(Long guildId) {
        return this.messages.computeIfAbsent(guildId, x -> new HashMap<>());
    }
}
