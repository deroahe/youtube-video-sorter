package com.deroahe.youtube_video_sorter.controller;

import com.deroahe.youtube_video_sorter.service.YouTubePlaylistService;
import com.google.api.services.youtube.model.Playlist;
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
    public List<Playlist> getPlayListIds() throws IOException, GeneralSecurityException {
        return youTubePlaylistService.getAllPlaylists();
    }

    @PostMapping("/{playlistId}")
    public void sortPlaylist(@PathVariable(name = "playlistId") final String playlistId) throws GeneralSecurityException, IOException {
        youTubePlaylistService.updatePlaylistOrder(playlistId);
    }
}
