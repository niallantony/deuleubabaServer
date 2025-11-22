package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.student.StudentIdAvatar;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends CrudRepository<Student, String> {
    @NotNull
    Optional<Student> findById(@NotNull String id);

    @Query("SELECT s FROM Student s JOIN s.users u WHERE u.userId = :user_id")
    List<Student> findAllOfUserId(@Param("user_id") String user_id);
}
