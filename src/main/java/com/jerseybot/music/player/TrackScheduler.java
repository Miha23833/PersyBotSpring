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
    private final Queue<AudioTrack> tracks = new LinkedBlockingQueue<>();

    public void addTrack(AudioTrack track) {
        this.tracks.add(track);
    }

    public AudioTrack nextTrack() {
        return tracks.remove();
    }

    public void clear() {
        this.tracks.clear();
    }

    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    public List<AudioTrack> getTracks() {
        return tracks.stream().map(AudioTrack::makeClone).collect(Collectors.toList());
    }

    public void shuffle() {
        List<AudioTrack> trackQueue = new ArrayList<>(this.tracks);
        this.tracks.clear();
        Collections.shuffle(trackQueue);
        this.tracks.addAll(trackQueue);
    }

}
