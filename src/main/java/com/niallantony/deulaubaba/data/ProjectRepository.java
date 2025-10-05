package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.ProjectPreviewDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(
            "SELECT new com.niallantony.deulaubaba.dto.ProjectPreviewDTO(" +
                    "p.id, p.completed, p.description, p.objective, p.imgsrc, p.startedOn) " +
                    "FROM Project p JOIN p.users u WHERE u.user = :user"
    )
    Set<ProjectPreviewDTO> findAllProjectsByUserId(@Param("user") User user) ;

    @Query(
            "SELECT new com.niallantony.deulaubaba.dto.ProjectPreviewDTO(" +
                    "p.id, p.completed, p.description, p.objective, p.imgsrc, p.startedOn) " +
                    "FROM Project p JOIN p.users u WHERE u.user = :user AND p.student = :student"
    )
    Set<ProjectPreviewDTO> findAllProjectsByStudentId(@Param("user") User user, @Param("student") Student student); ;
}
