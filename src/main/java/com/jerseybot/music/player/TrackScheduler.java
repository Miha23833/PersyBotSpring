package com.jerseybot.music.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
public class TrackScheduler {
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private final Queue<AudioTrack> tracks = new LinkedBlockingQueue<>();

    public void addTrack(AudioTrack track){
        try {
            rwLock.writeLock().lock();
            this.tracks.add(track);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public AudioTrack nextTrack(){
        try {
            rwLock.writeLock().lock();
        return tracks.remove();
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public void clear(){

    }

    public boolean isEmpty() {
        try {
            rwLock.readLock().lock();
        return tracks.isEmpty();
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public List<AudioTrack> getTracks() {
        try {
            rwLock.readLock().lock();
        return tracks.stream().map(AudioTrack::makeClone).collect(Collectors.toList());
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void shuffle() {
        try {
            rwLock.writeLock().lock();

            List<AudioTrack> trackQueue = new ArrayList<>(this.tracks);
            this.tracks.clear();
            Collections.shuffle(trackQueue);
            this.tracks.addAll(trackQueue);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

}
