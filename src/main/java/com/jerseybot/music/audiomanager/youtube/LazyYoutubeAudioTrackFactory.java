package com.jerseybot.music.audiomanager.youtube;

import com.google.api.services.youtube.model.Video;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

@Component
public class LazyYoutubeAudioTrackFactory {

    private final YoutubeSearchProvider ytSearchProvider;
    private final YoutubeAudioSourceManager ytAudioSourceManager;

    public LazyYoutubeAudioTrackFactory(YoutubeSearchProvider ytSearchProvider,
                                        YoutubeAudioSourceManager ytAudioSourceManager) {
        this.ytSearchProvider = ytSearchProvider;
        this.ytAudioSourceManager = ytAudioSourceManager;
    }

    public LazyYoutubeAudioTrack getAudioTrack(Video ytVideo) {
        String author = ytVideo.getSnippet().getChannelTitle();
        String title = ytVideo.getSnippet().getTitle();
        if (title.chars().filter(ch -> ch == '-').count() == 1 && !title.startsWith("-") && !title.endsWith("-")) {
            author = title.substring(0, title.indexOf("-")).trim();
            title = title.substring(title.indexOf('-') + 1).trim();
        } else if (title.chars().filter(ch -> ch == '\u2014').count() == 1 && !title.startsWith("\u2014") && !title.endsWith("\u2014")) {
            author = title.substring(0, title.indexOf('\u2014')).trim();
            title = title.substring(title.indexOf('\u2014') + 1).trim();
        }
        long durationMillis = Duration.parse(ytVideo.getContentDetails().getDuration()).toMillis();
        AudioTrackInfo info = new AudioTrackInfo(title, author, durationMillis, ytVideo.getId(), false, "https://www.youtube.com/watch?v=" + ytVideo.getId(), new HashMap<>());
        return new LazyYoutubeAudioTrack(info, ytAudioSourceManager, ytSearchProvider);
    }

    public LazyYoutubeAudioTrack getAudioTrack(Track track) {
        String author = Arrays.stream(track.getArtists()).map(ArtistSimplified::getName).collect(Collectors.joining(", "));
        String title = track.getName();
        long durationMillis = track.getDurationMs();
        AudioTrackInfo info = new AudioTrackInfo(title, author, durationMillis, track.getId(), false, "https://open.spotify.com/track/" + track.getId(), new HashMap<>());
        return new LazyYoutubeAudioTrack(info, ytAudioSourceManager, ytSearchProvider);
    }

    public LazyYoutubeAudioTrack getAudioTrack(AudioTrackInfo ati) {
        return new LazyYoutubeAudioTrack(ati, ytAudioSourceManager, ytSearchProvider);
    }
}