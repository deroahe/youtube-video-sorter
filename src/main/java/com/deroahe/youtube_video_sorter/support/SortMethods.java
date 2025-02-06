package com.deroahe.youtube_video_sorter.support;

import com.google.api.services.youtube.model.PlaylistItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class SortMethods {

    private static final Pattern VIDEO_HASHTAG_NUMBER_PATTERN = Pattern.compile("#(\\d+)");
    private static final Pattern VIDEO_HASHTAG_NUMBER_PATTERN_BACKUP = Pattern.compile("(\\d+)");

    private SortMethods() {
    }

    public static List<PlaylistItem> getSortedAlphabetical(final List<PlaylistItem> videos, final boolean ascending) {
        final Comparator<PlaylistItem> titleComparator = Comparator.comparing(video -> video.getSnippet().getTitle().toLowerCase());
        final var videosCopy = new ArrayList<>(videos);

        if (ascending) {
            videosCopy.sort(titleComparator);
        } else {
            videosCopy.sort(titleComparator.reversed());
        }
        return videosCopy;
    }

    public static List<PlaylistItem> getSortedNumerical(final List<PlaylistItem> videos, final boolean ascending) {
        final Comparator<PlaylistItem> hashtagNumberComparator = Comparator.comparing(video -> extractVideoNumber(video.getSnippet().getTitle().toLowerCase()));
        final var videosCopy = new ArrayList<>(videos);

        if (ascending) {
            videosCopy.sort(hashtagNumberComparator);
        } else {
            videosCopy.sort(hashtagNumberComparator.reversed());
        }
        return videosCopy;
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
