package com.niallantony.deulaubaba.mapper;


import com.niallantony.deulaubaba.domain.ProjectFeedItem;
import com.niallantony.deulaubaba.domain.ProjectFeedItemType;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.project.ProjectFeedItemDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectFeedMapperTests {
    @Test
    public void testProjectFeedMapper() {
        ProjectFeedMapper mapper = new ProjectFeedMapperImpl();
        LocalDateTime time = LocalDateTime.now();
        User mockUser = new User();
        mockUser.setUsername("username");
        mockUser.setImagesrc("example.jpg");
        mockUser.setUserType("user type");
           ProjectFeedItem feedItem = new ProjectFeedItem();
        feedItem.setBody("body");
        feedItem.setType(ProjectFeedItemType.COMMENT);
        feedItem.setCreatedAt(time);
        feedItem.setUser(mockUser);

        ProjectFeedItemDTO dto = mapper.entityToDto(feedItem);

        assertEquals("body", dto.getBody());
        assertEquals(ProjectFeedItemType.COMMENT, dto.getType());
        assertEquals(time, dto.getCreatedAt());
        assertEquals("username", dto.getUser().getUsername());
        assertEquals("example.jpg", dto.getUser().getImagesrc());
        assertEquals("user type", dto.getUser().getUserType());
    }
}
