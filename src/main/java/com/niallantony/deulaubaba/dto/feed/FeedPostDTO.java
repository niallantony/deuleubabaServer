package com.niallantony.deulaubaba.dto.feed;

import com.niallantony.deulaubaba.domain.StudentFeedEmotion;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class FeedPostDTO {
    private String body;
    private Set<StudentFeedEmotion> emotions = new HashSet<>();
}
