package com.jerseybot.music.player;

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

    @Autowired
    public Player(DefaultAudioPlayerManager manager) {
        this.audioSourceManager = manager;
        this.audioPlayer = manager.createPlayer();
        this.audioPlayer.addListener(new AudioEventAdapter());
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }

    public void scheduleTrack(String source) {
            this.audioSourceManager.loadItemOrdered(this, source, new AudioLoadResultHandler());
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

    public void pause() {
        this.audioPlayer.setPaused(true);
    }

    public void resume() {
        this.audioPlayer.setPaused(false);
    }

    public boolean isPlaying() {
        return this.audioPlayer.getPlayingTrack() != null;
    }

    public boolean isPaused() {
        return this.audioPlayer.isPaused();
    }

    public boolean isTracksQueueEmpty() {
        return this.scheduler.isEmpty();
    }

    private class AudioLoadResultHandler implements com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler {
        @Override
        public void trackLoaded(AudioTrack track) {
            if (Player.this.scheduler.isEmpty() && !Player.this.isPlaying()) {
                Player.this.resume();
                Player.this.audioPlayer.playTrack(track);
            } else {
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
            if (!endReason.mayStartNext) {
                return;
            }
            if (Player.this.scheduler.isEmpty()) {
                player.stopTrack();
            } else {
                player.playTrack(Player.this.scheduler.nextTrack());
            }
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
            super.onTrackStart(player, track);
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            super.onTrackException(player, track, exception);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
            super.onTrackStuck(player, track, thresholdMs);
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
            super.onTrackStuck(player, track, thresholdMs, stackTrace);
        }
    }
}
