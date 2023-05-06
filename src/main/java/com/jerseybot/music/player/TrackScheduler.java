package com.jerseybot.music.player;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Component
public class TrackScheduler {
    private final Queue<AudioTrack> tracks = new LinkedBlockingQueue<>();

    public void addTrack(AudioTrack track){
        this.tracks.add(track);
    }

    public AudioTrack nextTrack(){
        return tracks.remove();
    }

    public void clear(){

    }

    public boolean isEmpty() {
        return tracks.isEmpty();
    }

    public List<AudioTrack> getTracks() {
        return tracks.stream().map(AudioTrack::makeClone).collect(Collectors.toList());
    }

}
