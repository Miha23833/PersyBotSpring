package com.jerseybot.music.audiomanager.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.common.collect.Lists;
import com.jerseybot.config.youtube.YoutubeConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.util.ResourceUtils.isUrl;

@Component
public class LazyYoutubeAudioSourceManager implements AudioSourceManager {
    private static final List<String> YOUTUBE_SNIPPET_PART = Collections.singletonList("snippet");
    private static final List<String> YOUTUBE_CONTENT_DETAILS_PART = Collections.singletonList("contentDetails");

    private static final Pattern TRACK_ID_PATTERN = Pattern.compile("(?:https?:)?(?://)?(?:[\\dA-Z-]+\\.)?(?:youtu\\.be/|youtube(?:-nocookie)?\\.com\\S*?[^\\w\\s-])(?<id>[\\w-]{11})(?=[^\\w-]|$)(?![?=&+%\\w.\\-]*(?:['\"][^<>]*>|</a>))");
    private static final Pattern PLAYLIST_ID_PATTERN = Pattern.compile("(?:https?:)?(?://)?(?:[\\dA-Z-]+\\.)?(?:youtu\\.be/|youtube(?:-nocookie)?\\.com\\S*?[^\\w\\s-])((play)?list=)(?<id>[\\w\\d\\-]+)");

    private static final String CONTENT_ID_GROUP = "id";

    private final String countryCodeForSearchEngine;

    private final YouTube ytApi;
    private final LazyYoutubeAudioTrackFactory audioTrackFactory;
    private final YoutubeAudioSourceManager ytSourceManager;

    private final int playlistItemsLimit;

    public LazyYoutubeAudioSourceManager(YouTube ytApi, YoutubeAudioSourceManager youtubeAudioSourceManager, LazyYoutubeAudioTrackFactory lazyYoutubeAudioTrackFactory, YoutubeConfiguration youtubeConfiguration) {
        this.ytApi = ytApi;
        this.ytSourceManager = youtubeAudioSourceManager;
        this.audioTrackFactory = lazyYoutubeAudioTrackFactory;
        this.countryCodeForSearchEngine = youtubeConfiguration.getCountryCodeForSearchEngine();
        this.playlistItemsLimit = youtubeConfiguration.getPlaylistItemsLimit();
    }

    @Override
    public String getSourceName() {
        return "youtube_lazy";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        try {
            if (!isUrl(reference.identifier)) {
                return loadTrackFromYtByTitle(reference.identifier);
            }
            Matcher match = PLAYLIST_ID_PATTERN.matcher(reference.identifier);
            if (match.find()) {
                return loadPlaylistFromYT(match.group(CONTENT_ID_GROUP));
            }
            match = TRACK_ID_PATTERN.matcher(reference.identifier);

            if (match.find()) {
                return loadTrackFromYT(match.group(CONTENT_ID_GROUP));
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return this.ytSourceManager.isTrackEncodable(track);
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) {
        this.ytSourceManager.encodeTrack(track, output);
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) {
        return this.ytSourceManager.decodeTrack(trackInfo, input);
    }

    @Override
    public void shutdown() {
        this.ytSourceManager.shutdown();
    }

    private AudioItem loadPlaylistFromYT(String playlistId) throws IOException {
        PlaylistListResponse playlistRsp = ytApi.playlists()
                .list(YOUTUBE_SNIPPET_PART)
                .setId(Collections.singletonList(playlistId))
                .setMaxResults(1L)
                .execute();
        Playlist playlist = playlistRsp.getItems().get(0);

        List<String> allPlaylistItemsIds = new ArrayList<>();

        String pageToken = null;
        while (allPlaylistItemsIds.size() != playlistItemsLimit) {
            PlaylistItemListResponse playlistItemListRsp = ytApi.playlistItems()
                    .list(YOUTUBE_CONTENT_DETAILS_PART)
                    .setPlaylistId(playlistId)
                    .setMaxResults(50L)
                    .setPageToken(pageToken)
                    .execute();
            pageToken = playlistItemListRsp.getNextPageToken();

            List<String> itemsOnCurrentPage = playlistItemListRsp
                    .getItems()
                    .stream()
                    // video is not available
                    .filter(it -> it.getContentDetails().getVideoPublishedAt() != null)
                    .map(it -> it.getContentDetails().getVideoId())
                    .toList();
            if (itemsOnCurrentPage.isEmpty()) {
                break;
            }
            int cutPart = Math.min(itemsOnCurrentPage.size(), playlistItemsLimit - allPlaylistItemsIds.size());
            allPlaylistItemsIds.addAll(itemsOnCurrentPage.subList(0, cutPart));

            if (pageToken == null) {
                break;
            }
        }

        List<Video> playlistVideos = new ArrayList<>();
        for (List<String> trackIdSubList: Lists.partition(allPlaylistItemsIds, 50)) {
            List<Video> pagePlaylistVideos = ytApi.videos()
                    .list(ListUtils.union(YOUTUBE_SNIPPET_PART, YOUTUBE_CONTENT_DETAILS_PART))
                    .setId(trackIdSubList)
                    .setMaxResults(50L)
                    .execute().getItems();
            playlistVideos.addAll(pagePlaylistVideos);
        }
        List<AudioTrack> result = playlistVideos.stream().map(audioTrackFactory::getAudioTrack).collect(Collectors.toList());

        return new BasicAudioPlaylist(playlist.getSnippet().getTitle(), result, null, false);
    }

    private AudioTrack loadTrackFromYT(String id) throws IOException {
        Video ytVideo = ytApi.videos()
                .list(ListUtils.union(YOUTUBE_SNIPPET_PART, YOUTUBE_CONTENT_DETAILS_PART))
                .setMaxResults(1L)
                .setId(Collections.singletonList(id))
                .execute()
                .getItems()
                .get(0);
        return audioTrackFactory.getAudioTrack(ytVideo);
    }

    private AudioTrack loadTrackFromYtByTitle(String title) throws IOException {
        SearchListResponse searchRsp = ytApi.search()
                .list(YOUTUBE_SNIPPET_PART)
                // TODO: maybe change it depending on country of request user?
                .setRegionCode(countryCodeForSearchEngine)
                .setMaxResults(1L)
                .setQ(title)
                .execute();

        if (searchRsp.getItems().isEmpty()) {
            return null;
        }
        return loadTrackFromYT(searchRsp.getItems().get(0).getId().getVideoId());

    }
}
