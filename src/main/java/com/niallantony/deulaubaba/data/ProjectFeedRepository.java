package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.ProjectFeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectFeedRepository extends JpaRepository<ProjectFeedItem, Long> {
    List<ProjectFeedItem> findByProjectIdOrderByCreatedAtDesc(Long projectId);
}
