package com.jerseybot.music.player;

import com.jerseybot.music.AudioContentCache;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Player {
    private final DefaultAudioPlayerManager audioSourceManager;
    private final AudioPlayer audioPlayer;

    @Getter
    private final AudioPlayerSendHandler sendHandler;

    private final TrackScheduler scheduler = new TrackScheduler();

    private final AudioContentCache audioContentCache;

    @Autowired
    public Player(DefaultAudioPlayerManager manager, AudioContentCache audioContentCache) {
        this.audioSourceManager = manager;
        this.audioPlayer = manager.createPlayer();
        this.audioPlayer.addListener(new AudioEventAdapter());
        this.audioContentCache = audioContentCache;
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void scheduleTrack(String source) {
        if (audioContentCache.contains(source)) {
            this.scheduler.addTrack(audioContentCache.get(source).makeClone());
        } else {
            this.audioSourceManager.loadItemOrdered(this, source, new AudioLoadResultHandler());
        }
    }

    public void skip() {
        if (scheduler.isEmpty()) {
            stop();
        } else {
            this.audioPlayer.playTrack(scheduler.nextTrack());
        }
    }

    public void stop() {
        this.scheduler.clear();
        this.audioPlayer.stopTrack();
    }

    private class AudioLoadResultHandler implements com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler {
        @Override
        public void trackLoaded(AudioTrack track) {
            if (Player.this.scheduler.isEmpty()) {
                Player.this.audioPlayer.playTrack(track);
            } else {
                Player.this.audioContentCache.put(track.getIdentifier(), track);
                Player.this.scheduler.addTrack(track);
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            if (playlist.isSearchResult()) {
                trackLoaded(playlist.getTracks().get(0));
            } else {
                for (AudioTrack track : playlist.getTracks()) {
                    Player.this.scheduler.addTrack(track);
                }
            }
        }

        @Override
        public void noMatches() {
            System.out.println("no matches");
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            throw exception;
        }
    }

    private class AudioEventAdapter extends com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter {
        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            if (Player.this.scheduler.isEmpty()) {
                player.stopTrack();
            } else {
                player.playTrack(Player.this.scheduler.nextTrack());
            }
        }
    }
}
