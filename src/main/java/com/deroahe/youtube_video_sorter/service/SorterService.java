package com.deroahe.youtube_video_sorter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SorterService {

    @Value("${youtube.api-key}")
    private String apiKey;

    public SorterService() {
    }
}
