package com.deroahe.youtube_video_sorter.service;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
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
        YouTube youtubeService = youTubeService.getYouTubeService();
        List<PlaylistItem> sortedVideos = getSortedVideos(playlistId);

        // Create a batch request
        BatchRequest batch = youtubeService.batch();

        // Callback to handle batch responses
        JsonBatchCallback<PlaylistItem> callback = new JsonBatchCallback<>() {
            @Override
            public void onSuccess(PlaylistItem playlistItem, HttpHeaders httpHeaders) {
                LOGGER.info("Updated video position: {}", playlistItem.getId());
            }

            @Override
            public void onFailure(GoogleJsonError googleJsonError, HttpHeaders httpHeaders) {
                LOGGER.error("Failed to update video position: {}", googleJsonError.getMessage());
            }
        };

        // Add all update requests to the batch
        for (int i = 0; i < sortedVideos.size(); i++) {
            PlaylistItem video = sortedVideos.get(i);
            PlaylistItemSnippet snippet = video.getSnippet();
            snippet.setPosition((long) i); // Set new position

            // Create the update request
            YouTube.PlaylistItems.Update updateRequest = youtubeService.playlistItems()
                    .update("snippet", video);

            updateRequest.queue(batch, callback); // Queue request in batch
        }

        // Execute batch update
        batch.execute();
        LOGGER.info("Batch update completed.");
    }

    public List<PlaylistItem> getSortedVideos(String playlistId) throws GeneralSecurityException, IOException {
        List<PlaylistItem> videos = getVideosInPlaylist(playlistId);

        videos.sort(Comparator.comparing(item -> {
            final var title = item.getSnippet().getTitle().toLowerCase();
            Matcher matcher = VIDEO_NUMBER_PATTERN.matcher(title);

            if (matcher.find()) {
                final var videoNumber = matcher.group(1);
                LOGGER.info("Found video number {}", videoNumber);

                return videoNumber;
            } else {
                LOGGER.error("Couldn't extract number for video {}", title);
                return "";
            }
        }));

        return videos;
    }

    public List<PlaylistItem> getVideosInPlaylist(String playlistId) throws GeneralSecurityException, IOException {
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

            videos.addAll(response.getItems());
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        LOGGER.info("Videos found in playlist {}: {}\nRequests made: {}", playlistId, videos.size(), requestsMade);
        return videos;
    }
}

