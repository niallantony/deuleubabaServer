package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.StudentFeedRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.dto.feed.FeedDTO;
import com.niallantony.deulaubaba.exceptions.BadPageRequestException;
import com.niallantony.deulaubaba.mapper.StudentFeedMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FeedService {

    private final StudentService studentService;
    private final StudentFeedRepository studentFeedRepository;
    private final StudentFeedMapper studentFeedMapper;

    public FeedService(StudentService studentService, StudentFeedRepository studentFeedRepository,
                       StudentFeedMapper studentFeedMapper
    ) {
        this.studentService = studentService;
        this.studentFeedRepository = studentFeedRepository;
        this.studentFeedMapper = studentFeedMapper;
    }

    public FeedDTO getFeed(String user_id, String student_id, int page, int size) {
        Student student = studentService.getAuthorisedStudent(student_id, user_id);
        try {
            Pageable pageable = PageRequest.of(page, size);
            List<StudentFeedItem> feedItems = studentFeedRepository.findAllByStudentOrderByCreatedAtDesc(student, pageable);
            FeedDTO feedDTO = new FeedDTO();
            feedDTO.setFeed(feedItems.stream().map(studentFeedMapper::entityToDto).toList());
            return feedDTO;
        } catch (IllegalArgumentException e) {
            throw new BadPageRequestException(e.getMessage());
        }
    }
}
