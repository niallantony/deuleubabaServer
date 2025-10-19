package com.niallantony.deulaubaba.dto.feed;

import com.niallantony.deulaubaba.domain.StudentFeedEmotion;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
public class FeedItemDTO {
    private Long id;
    private UserAvatar user;
    private Set<StudentFeedEmotion> emotions = new HashSet<>();
    private String body;
    private LocalDateTime createdAt;
}
