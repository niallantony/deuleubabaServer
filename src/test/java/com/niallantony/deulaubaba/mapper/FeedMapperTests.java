package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedEmotion;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.feed.FeedItemDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FeedMapperTests {
    @Test
    public void entityToDto_givenEntity_returnsDTO() {
        StudentFeedMapper mapper = new StudentFeedMapperImpl();
        StudentFeedItem entity = new StudentFeedItem();
        Student student = new Student();
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUserType("Type");
        user.setUsername("Username");
        user.setImagesrc("ImageSrc");
        entity.setId(1L);
        entity.setStudent(student);
        entity.setUser(user);
        entity.getEmotions().add(StudentFeedEmotion.QUIET);
        entity.setBody("Body");
        entity.setCreatedAt(now);

        FeedItemDTO dto = mapper.entityToDto(entity);
        assertAll(() -> {
            assertEquals(entity.getId(), dto.getId());
            assertEquals(entity.getEmotions().size(), dto.getEmotions().size());
            assertEquals(entity.getCreatedAt(), dto.getCreatedAt());
            assertEquals(entity.getBody(), dto.getBody());
            assertEquals(entity.getUser().getUserType(), dto.getUser().getUserType());
            assertEquals(entity.getUser().getUsername(), dto.getUser().getUsername());
            assertEquals(entity.getUser().getImagesrc(), dto.getUser().getImagesrc());
            assertTrue(dto.getEmotions().contains(StudentFeedEmotion.QUIET));
        });

    }
}
