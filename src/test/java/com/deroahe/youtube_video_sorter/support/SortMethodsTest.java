package com.deroahe.youtube_video_sorter.support;

import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class SortMethodsTest {

    @Test
    void shouldGetSortedAlphabeticalWithAscendingOrder() {
        final var inputVideos = new ArrayList<>(List.of(
                createPlaylistItem("Boomin"),
                createPlaylistItem("yup!"),
                createPlaylistItem("mathematics"),
                createPlaylistItem("QWOP")
        ));
        final var expectedOutputVideos = List.of(
                createPlaylistItem("Boomin"),
                createPlaylistItem("mathematics"),
                createPlaylistItem("QWOP"),
                createPlaylistItem("yup!")
        );
        final var actualOutputVideos = SortMethods.getSortedAlphabetical(inputVideos, true);
        Assertions.assertEquals(expectedOutputVideos, actualOutputVideos);
    }

    @Test
    void shouldGetSortedAlphabeticalWithDescendingOrder() {
        final var inputVideoTitles = new ArrayList<>(List.of(
                createPlaylistItem("Boomin"),
                createPlaylistItem("yup!"),
                createPlaylistItem("mathematics"),
                createPlaylistItem("QWOP")
        ));
        final var expectedOutputVideoTitles = List.of(
                createPlaylistItem("yup!"),
                createPlaylistItem("QWOP"),
                createPlaylistItem("mathematics"),
                createPlaylistItem("Boomin")
        );
        final var actualOutputVideoTitles = SortMethods.getSortedAlphabetical(inputVideoTitles, false);
        Assertions.assertEquals(expectedOutputVideoTitles, actualOutputVideoTitles);
    }

    @Test
    void shouldGetSortedNumericalAscending() {
        final var inputVideoTitles = new ArrayList<>(List.of(
                createPlaylistItem("KILL TONY #378 - IAN EDWARDS"),
                createPlaylistItem("KT #622 - JIM NORTON"),
                createPlaylistItem("KILL TONY #377 - FT WORTH"),
                createPlaylistItem("KT #623 - BRIAN MOSES + MATTHEW BROUSSARD"),
                createPlaylistItem("KILL TONY #379 - GREG FITZSIMMONS"),
                createPlaylistItem("KILL TONY #414 - SHANE GILLIS + BIG JAY OAKERSON - NYC")
        ));
        final var expectedOutputVideoTitles = List.of(
                createPlaylistItem("KILL TONY #377 - FT WORTH"),
                createPlaylistItem("KILL TONY #378 - IAN EDWARDS"),
                createPlaylistItem("KILL TONY #379 - GREG FITZSIMMONS"),
                createPlaylistItem("KILL TONY #414 - SHANE GILLIS + BIG JAY OAKERSON - NYC"),
                createPlaylistItem("KT #622 - JIM NORTON"),
                createPlaylistItem("KT #623 - BRIAN MOSES + MATTHEW BROUSSARD")
        );
        final var actualOutputVideoTitles = SortMethods.getSortedNumerical(inputVideoTitles, true);
        Assertions.assertEquals(expectedOutputVideoTitles, actualOutputVideoTitles);
    }

    @Test
    void shouldGetSortedNumericalDescending() {
        final var inputVideoTitles = new ArrayList<>(List.of(
                createPlaylistItem("KILL TONY #378 - IAN EDWARDS"),
                createPlaylistItem("KT #622 - JIM NORTON"),
                createPlaylistItem("KILL TONY #377 - FT WORTH"),
                createPlaylistItem("KT #623 - BRIAN MOSES + MATTHEW BROUSSARD"),
                createPlaylistItem("KILL TONY #379 - GREG FITZSIMMONS"),
                createPlaylistItem("KILL TONY #414 - SHANE GILLIS + BIG JAY OAKERSON - NYC")
        ));
        final var expectedOutputVideoTitles = List.of(
                createPlaylistItem("KT #623 - BRIAN MOSES + MATTHEW BROUSSARD"),
                createPlaylistItem("KT #622 - JIM NORTON"),
                createPlaylistItem("KILL TONY #414 - SHANE GILLIS + BIG JAY OAKERSON - NYC"),
                createPlaylistItem("KILL TONY #379 - GREG FITZSIMMONS"),
                createPlaylistItem("KILL TONY #378 - IAN EDWARDS"),
                createPlaylistItem("KILL TONY #377 - FT WORTH")
        );
        final var actualOutputVideoTitles = SortMethods.getSortedNumerical(inputVideoTitles, false);
        Assertions.assertEquals(expectedOutputVideoTitles, actualOutputVideoTitles);
    }

    @Test
    void shouldExtractVideoNumberWithHashtagFollowedByNumber() {
        final var inputVideoTitle = "Kill Tony #94 (Jeff Ross, Moshe Kasher, Willie Hunter)";
        final var expectedVideoNumber = 94;

        final var actualVideoNumber = SortMethods.extractVideoNumber(inputVideoTitle);
        Assertions.assertEquals(expectedVideoNumber, actualVideoNumber);
    }

    @Test
    void shouldExtractVideoNumberWithMultipleHashtagsFollowedByNumbers() {
        final var inputVideoTitle = "KILL TONY #330 – PHILADELPHIA #1";
        final var expectedVideoNumber = 330;

        final var actualVideoNumber = SortMethods.extractVideoNumber(inputVideoTitle);
        Assertions.assertEquals(expectedVideoNumber, actualVideoNumber);
    }

    @Test
    void shouldExtractVideoNumberWithJustNumberWithoutAnyHashtags() {
        final var inputVideoTitle = "Kill Tony 86 (Russell Peters, Jesus Trejo)";
        final var expectedVideoNumber = 86;

        final var actualVideoNumber = SortMethods.extractVideoNumber(inputVideoTitle);
        Assertions.assertEquals(expectedVideoNumber, actualVideoNumber);
    }

    private static PlaylistItem createPlaylistItem(final String videoTitle) {
        final var snippet = new PlaylistItemSnippet();
        snippet.setTitle(videoTitle);

        final var playlistItem = new PlaylistItem();
        playlistItem.setSnippet(snippet);

        return playlistItem;
    }
}
