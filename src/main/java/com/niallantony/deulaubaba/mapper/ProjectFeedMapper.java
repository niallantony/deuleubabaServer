package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.ProjectFeedItem;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.project.ProjectFeedItemDTO;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectFeedMapper {
    ProjectFeedItemDTO entityToDto(ProjectFeedItem item);

    default UserAvatar entityToAvatar(User user) {
        return new UserAvatar(
                user.getUsername(),
                user.getImagesrc(),
                user.getUserType()
        );
    }
}
