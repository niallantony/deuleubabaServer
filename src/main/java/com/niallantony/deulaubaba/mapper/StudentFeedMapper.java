package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.feed.FeedItemDTO;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface StudentFeedMapper {
    FeedItemDTO entityToDto(StudentFeedItem feed);

    default UserAvatar userToUserAvatar(User user) {
        if (user == null) return null;
        return new UserAvatar(
                user.getUsername(),
                user.getImagesrc(),
                user.getUserType()
        );
    }
}
