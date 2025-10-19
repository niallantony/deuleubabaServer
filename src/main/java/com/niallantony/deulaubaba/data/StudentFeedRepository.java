package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface StudentFeedRepository extends JpaRepository<StudentFeedItem, Long> {
    List<StudentFeedItem> findAllByStudentOrderByCreatedAtDesc(Student student, Pageable pageable);
}
