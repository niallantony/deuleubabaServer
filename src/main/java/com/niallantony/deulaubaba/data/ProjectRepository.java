package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
