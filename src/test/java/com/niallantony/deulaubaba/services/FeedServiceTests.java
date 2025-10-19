package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.StudentFeedRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.dto.feed.FeedDTO;
import com.niallantony.deulaubaba.dto.feed.FeedItemDTO;
import com.niallantony.deulaubaba.exceptions.BadPageRequestException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.StudentFeedMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTests {
    @Mock
    StudentService studentService;

    @Mock
    StudentFeedRepository studentFeedRepository;

    @Mock
    StudentFeedMapper studentFeedMapper;

    @InjectMocks
    FeedService feedService;

    @Test
    public void getFeed_withValidData_returnsFeed() {
        Student student = new Student();
        StudentFeedItem studentFeedItem = new StudentFeedItem();
        FeedItemDTO dto = new FeedItemDTO();

        when(studentService.getAuthorisedStudent("123", "abc")).thenReturn(student);
        when(studentFeedRepository.findAllByStudentOrderByCreatedAtDesc(student, PageRequest.of(0, 10)))
                .thenReturn(List.of(studentFeedItem));
        when(studentFeedMapper.entityToDto(studentFeedItem)).thenReturn(dto);

        FeedDTO feed = feedService.getFeed("abc", "123", 0, 10);

        assertAll(() -> {
            assertEquals(1, feed.getFeed().size());
            assertTrue(feed.getFeed().contains(dto));
        });
    }

    @Test
    public void getFeed_withUnauthorisedUser_throwsUserNotAuthorisedException() {
        when(studentService.getAuthorisedStudent("123", "abc")).thenThrow(new UserNotAuthorizedException("Unauthorized"));
        assertThrows(UserNotAuthorizedException.class, () -> feedService.getFeed("abc", "123", 0, 10));
    }

    @Test
    public void getFeed_withNoItems_returnsEmptyList() {
        Student student = new Student();

        when(studentService.getAuthorisedStudent("123", "abc")).thenReturn(student);
        when(studentFeedRepository.findAllByStudentOrderByCreatedAtDesc(student, PageRequest.of(0, 10)))
                .thenReturn(List.of());

        FeedDTO feed = feedService.getFeed("abc", "123", 0, 10);

        assertEquals(0, feed.getFeed().size());
    }

    @Test
    public void getFeed_withIncorrectPageParams_throwsException() {
        Student student = new Student();

        when(studentService.getAuthorisedStudent("123", "abc")).thenReturn(student);

        assertThrows(
                BadPageRequestException.class,
                () -> feedService.getFeed("abc", "123", -1, 10)
        );
        assertThrows(
                BadPageRequestException.class,
                () -> feedService.getFeed("abc", "123", 1, 0)
        );

    }

}
