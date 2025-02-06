package com.deroahe.youtube_video_sorter.service;

import com.deroahe.youtube_video_sorter.model.SortMethod;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

@Service
public class YouTubePlaylistService {

    private static final Logger LOGGER = getLogger(YouTubePlaylistService.class);

    private static final Pattern VIDEO_HASHTAG_NUMBER_PATTERN = Pattern.compile("#(\\d+)");
    private static final Pattern VIDEO_HASHTAG_NUMBER_PATTERN_BACKUP = Pattern.compile("(\\d+)");

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

        LOGGER.info("Playlists found: {}. Requests made {} ({} queries)", playlists.size(), requestsMade, requestsMade);

        return playlists;
    }

    public void updatePlaylistOrder(final String playlistId, final SortMethod sortMethod, final boolean ascending)
            throws GeneralSecurityException, IOException {
        LOGGER.info("Updating video order for playlist {}", playlistId);

        final var youtubeService = youTubeService.getYouTubeService();
        final var sortedVideos = getSortedVideos(playlistId, sortMethod, ascending);

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

    public List<PlaylistItem> getSortedVideos(final String playlistId, final SortMethod sortMethod, final boolean ascending)
            throws GeneralSecurityException, IOException {
        LOGGER.info("Sorting videos in playlist {}", playlistId);

        final var videos = getVideosInPlaylist(playlistId);

        final Comparator<PlaylistItem> hashtagNumberComparator = Comparator.comparing(item -> {
            final var title = item.getSnippet().getTitle().toLowerCase();
            return extractVideoNumber(title);
        });

        final Comparator<PlaylistItem> titleComparator = Comparator.comparing(item -> item.getSnippet().getTitle().toLowerCase());

        switch (sortMethod) {
            case HASHTAG_NUMBER_NUMERICAL -> {
                if (ascending) {
                    videos.sort(hashtagNumberComparator);
                } else {
                    videos.sort(hashtagNumberComparator.reversed());
                }
            }
            case WHOLE_TITLE_ALPHABETICAL -> {
                if (ascending) {
                    videos.sort(titleComparator);
                } else {
                    videos.sort(titleComparator.reversed());
                }
            }
            default -> throw new IllegalArgumentException("Unknown sort method: " + sortMethod);
        }

        return videos;
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

    public static int extractVideoNumber(final String title) {
        final var matcher = VIDEO_HASHTAG_NUMBER_PATTERN.matcher(title);
        int videoNumber;

        if (matcher.find()) {
            videoNumber = Integer.parseInt(matcher.group(1));
        } else {
            final var matcherBackup = VIDEO_HASHTAG_NUMBER_PATTERN_BACKUP.matcher(title);
            if (matcherBackup.find()) {
                videoNumber = Integer.parseInt(matcherBackup.group(1));
            } else {
                videoNumber = 0;
            }
        }
        return videoNumber;
    }
}

