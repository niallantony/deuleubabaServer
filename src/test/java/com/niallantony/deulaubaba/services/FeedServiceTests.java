package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.StudentFeedRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedEmotion;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.feed.FeedDTO;
import com.niallantony.deulaubaba.dto.feed.FeedItemDTO;
import com.niallantony.deulaubaba.dto.feed.FeedPostDTO;
import com.niallantony.deulaubaba.exceptions.BadPageRequestException;
import com.niallantony.deulaubaba.exceptions.InvalidCommentPostException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.StudentFeedMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedServiceTests {
    @Mock
    StudentService studentService;

    @Mock
    StudentFeedRepository studentFeedRepository;

    @Mock
    StudentRepository studentRepository;

    @Mock
    UserRepository userRepository;

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

    @Test
    public void postComment_withValidComment_savesAComment() {
        FeedPostDTO request = new FeedPostDTO();
        request.setBody("Comment");
        request.getEmotions().add(StudentFeedEmotion.QUIET);
        Student student = new Student();
        User author = new User();
        student.getUsers().add(author);

        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(author));

        feedService.postComment("abc", "123", request);

        verify(studentFeedRepository).save(argThat(req ->
            req.getEmotions().contains(StudentFeedEmotion.QUIET) &&
                    req.getUser().equals(author) &&
                    req.getStudent().equals(student) &&
                    req.getBody().equals("Comment") &&
                    req.getCreatedAt() != null
        ));
    }

    @Test
    public void postComment_withEmptyBody_doesNotSaveComment() {
        FeedPostDTO request = new FeedPostDTO();
        request.getEmotions().add(StudentFeedEmotion.QUIET);
        Student student = new Student();
        User author = new User();
        student.getUsers().add(author);

        assertThrows(
                InvalidCommentPostException.class,
                () -> feedService.postComment("abc", "123", request)
        );
        verify(studentFeedRepository, never()).save(any());
    }

    @Test
    public void postComment_withWhitespaceBody_doesNotSaveComment() {
        FeedPostDTO request = new FeedPostDTO();
        request.setBody("  ");
        request.getEmotions().add(StudentFeedEmotion.QUIET);
        Student student = new Student();
        User author = new User();
        student.getUsers().add(author);

        assertThrows(
                InvalidCommentPostException.class,
                () -> feedService.postComment("abc", "123", request)
        );
        verify(studentFeedRepository, never()).save(any());
    }

    @Test
    public void postComment_asUnauthorizedUser_throwsException() {
        FeedPostDTO request = new FeedPostDTO();
        request.setBody("Comment");
        Student student = new Student();
        User author = new User();

        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(author));

        assertThrows(
                UserNotAuthorizedException.class,
                () -> feedService.postComment("abc", "123", request)
        );
    }

    @Test
    public void postComment_forNonExistentUser_throwsException() {
        FeedPostDTO request = new FeedPostDTO();
        request.setBody("Comment");
        Student student = new Student();

        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> feedService.postComment("abc", "123", request)
        );
    }

    @Test
    public void postComment_forNonExistentStudent_throwsException() {
        FeedPostDTO request = new FeedPostDTO();
        request.setBody("Comment");

        when(studentRepository.findById("123")).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> feedService.postComment("abc", "123", request)
        );
    }

    @Test
    public void deleteComment_withValidRequest_deletesComment() {
        User user = new User();
        user.setUserId("abc");
        StudentFeedItem item = new StudentFeedItem();
        item.setUser(user);
        when(studentFeedRepository.findById(1L)).thenReturn(Optional.of(item));

        feedService.deleteComment("abc", 1L);

        verify(studentFeedRepository).delete(item);
    }

    @Test
    public void deleteComment_whenCommentDoesNotExist_throwsException() {
        when(studentFeedRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
               ResourceNotFoundException.class,
                () -> feedService.deleteComment("abc", 1L)
        );
        verify(studentFeedRepository, never()).delete(any());
    }

    @Test
    public void deleteComment_whenUserNotAuthorized_throwsException() {
        StudentFeedItem item = new StudentFeedItem();
        when(studentFeedRepository.findById(1L)).thenReturn(Optional.of(item));
        assertThrows(
                UserNotAuthorizedException.class,
                () -> feedService.deleteComment("abc", 1L)
        );
        verify(studentFeedRepository, never()).delete(item);
    }


}
