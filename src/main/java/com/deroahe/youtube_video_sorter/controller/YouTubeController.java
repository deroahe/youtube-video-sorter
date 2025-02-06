package com.deroahe.youtube_video_sorter.controller;

import com.deroahe.youtube_video_sorter.model.SortMethod;
import com.deroahe.youtube_video_sorter.service.YouTubePlaylistService;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/youtube")
public class YouTubeController {

    private final YouTubePlaylistService youTubePlaylistService;

    public YouTubeController(final YouTubePlaylistService youTubePlaylistService) {
        this.youTubePlaylistService = youTubePlaylistService;
    }

    @GetMapping
    public List<Playlist> getAllPlaylists() throws IOException, GeneralSecurityException {
        return youTubePlaylistService.getAllPlaylists();
    }

    @GetMapping("/{playlistId}")
    public List<PlaylistItem> getAllVideoNumbersInPlaylist(@PathVariable(name = "playlistId") final String playlistId) throws GeneralSecurityException, IOException {
        return youTubePlaylistService.getVideosInPlaylist(playlistId);
    }

    @PostMapping("/{playlistId}")
    public void sortPlaylist(@PathVariable(name = "playlistId") final String playlistId,
                             @RequestParam(name = "sortMethod", required = false, defaultValue = "WHOLE_TITLE_ALPHABETICAL") final SortMethod sortMethod,
                             @RequestParam(name = "ascending", required = false, defaultValue = "true") boolean ascending)
            throws GeneralSecurityException, IOException {
        youTubePlaylistService.updatePlaylistOrder(playlistId, sortMethod, ascending);
    }
}
