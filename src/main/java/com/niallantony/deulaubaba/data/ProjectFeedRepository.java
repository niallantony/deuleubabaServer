package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.ProjectFeedItem;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectFeedRepository extends JpaRepository<ProjectFeedItem, Long> {
    List<ProjectFeedItem> findByProjectIdOrderByCreatedAtDesc(Long projectId);


    @NotNull
    Optional<ProjectFeedItem> findById(@NotNull Long id);
}
