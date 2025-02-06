package com.deroahe.youtube_video_sorter.service;

import com.deroahe.youtube_video_sorter.support.model.SortType;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static com.deroahe.youtube_video_sorter.support.SortMethods.getSortedAlphabetical;
import static com.deroahe.youtube_video_sorter.support.SortMethods.getSortedNumerical;
import static org.slf4j.LoggerFactory.getLogger;

@Service
public class YouTubePlaylistService {

    private static final Logger LOGGER = getLogger(YouTubePlaylistService.class);

    private final YouTubeService youTubeService;

    public YouTubePlaylistService(final YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    public List<Playlist> getAllPlaylists() throws GeneralSecurityException, IOException {
        LOGGER.info("Fetching all playlists");

        final var youtubeService = youTubeService.getYouTubeService();
        int requestsMade = 0;

        List<Playlist> playlists = new ArrayList<>();
        String nextPageToken = null;

        do {
            final var request = youtubeService.playlists()
                    .list("snippet,contentDetails")
                    .setMine(true) // Fetch only the playlists from the authenticated user
                    .setMaxResults(50L)
                    .setPageToken(nextPageToken);

            final var response = request.execute();
            requestsMade++;

            for (final var playList : response.getItems()) {
                final var title = playList.getSnippet().getTitle();
                LOGGER.info("Fetched playlist {}", title);
            }

            playlists.addAll(response.getItems());
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        LOGGER.info("Playlists found: {}. Requests made: {} ({} queries)", playlists.size(), requestsMade, requestsMade);

        return playlists;
    }

    public void updatePlaylistOrder(final String playlistId, final SortType sortType, final boolean ascending)
            throws GeneralSecurityException, IOException {
        LOGGER.info("Updating video order for playlist {}", playlistId);

        final var youtubeService = youTubeService.getYouTubeService();
        final var sortedVideos = getSortedVideos(playlistId, sortType, ascending);

        int updateRequestsMade = 0;

        for (int i = 0; i < sortedVideos.size(); i++) {
            final var video = sortedVideos.get(i);
            final var videoTitle = video.getSnippet().getTitle();
            final var videoPosition = video.getSnippet().getPosition();

            if (videoPosition == i) {
                continue;
            }

            final var updateRequest = youtubeService.playlistItems()
                    .update("snippet", new PlaylistItem()
                            .setId(video.getId())
                            .setSnippet(video.getSnippet().setPosition((long) i)));
            updateRequest.execute();
            updateRequestsMade++;

            LOGGER.info("Request sent for video {}", videoTitle);
        }

        LOGGER.info("Updated playlist {}. Requests made: {} ({} queries)", playlistId, updateRequestsMade, updateRequestsMade * 50);
    }

    public List<PlaylistItem> getSortedVideos(final String playlistId, final SortType sortType, final boolean ascending)
            throws GeneralSecurityException, IOException {
        LOGGER.info("Sorting videos in playlist {}", playlistId);

        final var videos = getVideosInPlaylist(playlistId);

        return switch (sortType) {
            case WHOLE_TITLE_ALPHABETICAL -> getSortedAlphabetical(videos, ascending);
            case HASHTAG_NUMBER_NUMERICAL -> getSortedNumerical(videos, ascending);
        };
    }

    public List<PlaylistItem> getVideosInPlaylist(final String playlistId) throws GeneralSecurityException, IOException {
        LOGGER.info("Fetching all videos in playlist {}", playlistId);

        final var youtubeService = youTubeService.getYouTubeService();
        int requestsMade = 0;

        List<PlaylistItem> videos = new ArrayList<>();
        String nextPageToken = null;

        do {
            final var request = youtubeService.playlistItems()
                    .list("snippet")
                    .setPlaylistId(playlistId)
                    .setMaxResults(50L) // Max allowed per request
                    .setPageToken(nextPageToken);

            final var response = request.execute();
            requestsMade++;

            for (final var item : response.getItems()) {
                final var title = item.getSnippet().getTitle();
                LOGGER.info("Fetched video {}", title);
            }

            videos.addAll(response.getItems());
            nextPageToken = response.getNextPageToken();
        } while (nextPageToken != null);

        LOGGER.info("Videos found in playlist {}: {}. Requests made: {} ({} queries)", playlistId, videos.size(),
                requestsMade, requestsMade);

        return videos;
    }
}

