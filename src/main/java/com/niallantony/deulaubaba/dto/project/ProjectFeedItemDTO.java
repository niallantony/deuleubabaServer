package com.niallantony.deulaubaba.dto.project;

import com.niallantony.deulaubaba.domain.ProjectFeedItemType;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectFeedItemDTO {
    private String body;
    private ProjectFeedItemType type;
    private UserAvatar user;
    private LocalDateTime createdAt;
}
