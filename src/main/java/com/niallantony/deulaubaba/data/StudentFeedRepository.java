package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface StudentFeedRepository extends JpaRepository<StudentFeedItem, Long> {
    List<StudentFeedItem> findAllByStudentOrderByCreatedAtDesc(Student student, Pageable pageable);

    @Query(
            "SELECT f FROM StudentFeedItem f JOIN f.student s WHERE s.studentId = :student_id"
    )
    List<StudentFeedItem> findAllByStudentIdOrderByCreatedAtDesc(@Param("student_id") String student_id);
}
