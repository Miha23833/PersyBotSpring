package com.jerseybot;

import net.dv8tion.jda.api.JDA;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class JDAStorage {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private JDA jda;

    public void setJda(JDA jda) {
        try {
            rwLock.writeLock().lock();
            this.jda = jda;
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public JDA getJda() {
        try {
            rwLock.readLock().lock();
            return this.jda;
        } finally {
            rwLock.readLock().unlock();
        }
    }
}
