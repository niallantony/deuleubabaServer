package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.StudentFeedRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Student;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeedService {

    private final StudentService studentService;
    private final StudentFeedRepository studentFeedRepository;
    private final StudentFeedMapper studentFeedMapper;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public FeedService(StudentService studentService, StudentFeedRepository studentFeedRepository,
                       StudentFeedMapper studentFeedMapper,
                       StudentRepository studentRepository,
                       UserRepository userRepository,
                       FileStorageService fileStorageService
    ) {
        this.studentService = studentService;
        this.studentFeedRepository = studentFeedRepository;
        this.studentFeedMapper = studentFeedMapper;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public FeedDTO getFeed(String user_id, String student_id, int page, int size) {
        Student student = studentService.getAuthorisedStudent(student_id, user_id);
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<StudentFeedItem> feedItems = studentFeedRepository.findAllByStudentOrderByCreatedAtDesc(student, pageable);
            FeedDTO feedDTO = new FeedDTO();
            feedDTO.setFeed(feedItems.stream().map(item -> {
                FeedItemDTO dto = studentFeedMapper.entityToDto(item);
                dto.getUser().setImagesrc(fileStorageService.generateSignedURL(item.getUser()));
                return dto;
            }).toList());
            return feedDTO;
        } catch (IllegalArgumentException e) {
            throw new BadPageRequestException(e.getMessage());
        }
    }

    public void postComment(String user_id, String student_id, FeedPostDTO request) {
        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new InvalidCommentPostException("Empty body");
        }
        Student student = studentRepository.findById(student_id).orElseThrow(
                () -> new ResourceNotFoundException("Student not found " + student_id)
        );
        User user = userRepository.findByUserId(user_id).orElseThrow(
                () -> new ResourceNotFoundException("User not found " + user_id)
        );
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        StudentFeedItem feedItem = new StudentFeedItem();
        feedItem.setStudent(student);
        feedItem.setUser(user);
        feedItem.setCreatedAt(LocalDateTime.now());
        feedItem.setEmotions(request.getEmotions());
        feedItem.setBody(request.getBody());

        studentFeedRepository.save(feedItem);
    }

    public void deleteComment(String user_id, Long comment_id) {
        StudentFeedItem comment = studentFeedRepository.findById(comment_id).orElseThrow(
                () -> new ResourceNotFoundException("Comment not found")
        );
        if (comment.getUser() == null || !comment.getUser().getUserId().equals(user_id)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        studentFeedRepository.delete(comment);
    }
}
