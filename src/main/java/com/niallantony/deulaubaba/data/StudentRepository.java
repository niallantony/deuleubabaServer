package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.StudentIdAvatar;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends CrudRepository<Student, String> {
    Optional<Student> findById(String id);

    @Query("SELECT s.studentId AS studentId, s.name AS name, s.imagesrc AS imagesrc " +
            "FROM Student s JOIN s.users u WHERE u.id = :user_id")
    List<StudentIdAvatar> findAllOfUserId(@Param("user_id") String user_id);
}
