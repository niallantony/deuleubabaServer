package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.ProjectUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectUserRepository extends CrudRepository<ProjectUser, Long> {


    @Query(
            "SELECT p FROM ProjectUser p JOIN " +
                    "p.user u JOIN p.project d" +
                    " WHERE u.userId = :user_id AND d.id = :project_id"
    )
    Optional<ProjectUser> findProjectUserById(String user_id, Long project_id);

    @EntityGraph(attributePaths = {"project"})
    @Query(
            "SELECT p FROM ProjectUser p JOIN " +
                    "p.project d WHERE d.id = :project_id ORDER BY p.completedOn ASC "
    )
    List<ProjectUser> findAllProjectUsersByProjectId(long project_id);
}
