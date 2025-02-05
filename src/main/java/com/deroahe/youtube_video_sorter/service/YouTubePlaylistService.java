package com.deroahe.youtube_video_sorter.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class YouTubePlaylistService {

    private static final Logger LOGGER = getLogger(YouTubePlaylistService.class);

    private static final Pattern VIDEO_NUMBER_PATTERN = Pattern.compile("#(\\d+)");
    private static final Pattern VIDEO_NUMBER_PATTERN_BACKUP = Pattern.compile("(\\d+)");

    private final YouTubeService youTubeService;

    public YouTubePlaylistService(final YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public List<Playlist> getAllPlaylists() throws GeneralSecurityException, IOException {
        YouTube youtubeService = youTubeService.getYouTubeService();

        YouTube.Playlists.List request = youtubeService.playlists()
                .list("snippet,contentDetails")
                .setMine(true) // Fetch only the playlists from the authenticated user
                .setMaxResults(50L);

        PlaylistListResponse response = request.execute();
        final var playlists = response.getItems();
        LOGGER.info("Playlists found: {}", playlists.size());

        return playlists;
    }

    public void updatePlaylistOrder(String playlistId) throws GeneralSecurityException, IOException {
        LOGGER.info("Updating video order for playlist {}", playlistId);

        YouTube youtubeService = youTubeService.getYouTubeService();
        List<PlaylistItem> sortedVideos = getSortedVideos(playlistId);

        int updateRequestsMade = 0;
        for (int i = 0; i < sortedVideos.size(); i++) {
            PlaylistItem video = sortedVideos.get(i);

            final var playListItemPosition = video.getSnippet().getPosition();

            if (playListItemPosition == i) {
                continue;
            }

            YouTube.PlaylistItems.Update updateRequest = youtubeService.playlistItems()
                    .update("snippet",
                            new PlaylistItem()
                                    .setId(video.getId())
                                    .setSnippet(video.getSnippet().setPosition((long) i)));

            updateRequest.execute();
            LOGGER.info("Request sent for video {}", video.getId());
            updateRequestsMade++;
        }

        LOGGER.info("Made {} update requests", updateRequestsMade);
    }

    public List<PlaylistItem> getSortedVideos(String playlistId) throws GeneralSecurityException, IOException {
        LOGGER.info("Sorting videos in playlist {}", playlistId);

        List<PlaylistItem> videos = getVideosInPlaylist(playlistId);

        videos.sort(Comparator.comparing(item -> {
            final var title = item.getSnippet().getTitle().toLowerCase();
            return extractVideoNumber(title);
        }));

        return videos;
    }

    public List<PlaylistItem> getVideosInPlaylist(String playlistId) throws GeneralSecurityException, IOException {
        LOGGER.info("Fetching videos in playlist {}", playlistId);

        int requestsMade = 0;
        YouTube youtubeService = youTubeService.getYouTubeService();

        List<PlaylistItem> videos = new ArrayList<>();
        String nextPageToken = null;

        do {
            YouTube.PlaylistItems.List request = youtubeService.playlistItems()
                    .list("snippet")
                    .setPlaylistId(playlistId)
                    .setMaxResults(50L) // Max allowed per request
                    .setPageToken(nextPageToken);

            PlaylistItemListResponse response = request.execute();
            requestsMade++;

            for (PlaylistItem item : response.getItems()) {
                final var title = item.getSnippet().getTitle();
                final var videoNumber = extractVideoNumber(title);
                LOGGER.info("Fetched video with number {}", videoNumber);
            }

            videos.addAll(response.getItems());
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        LOGGER.info("Videos found in playlist {}: {}. Requests made: {}", playlistId, videos.size(), requestsMade);
        return videos;
    }

    public static int extractVideoNumber(final String title) {
        Matcher matcher = VIDEO_NUMBER_PATTERN.matcher(title);
        int videoNumber;

        if (matcher.find()) {
            videoNumber = Integer.parseInt(matcher.group(1));
        } else {
            Matcher matcherBackup = VIDEO_NUMBER_PATTERN_BACKUP.matcher(title);
            if (matcherBackup.find()) {
                videoNumber = Integer.parseInt(matcherBackup.group(1));
            } else {
                videoNumber = 0;
            }
        }
        return videoNumber;
    }
}

