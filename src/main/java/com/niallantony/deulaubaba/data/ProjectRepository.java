package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.project.ProjectPreviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(
                    "SELECT p FROM Project p JOIN p.users u WHERE u.user = :user"
    )
    Set<Project> findAllProjectsByUserId(@Param("user") User user) ;

    @Query(
                    "SELECT p FROM Project p JOIN p.users u WHERE u.user = :user AND p.student = :student"
    )
    Set<Project> findAllProjectsByStudentId(@Param("user") User user, @Param("student") Student student);
}
