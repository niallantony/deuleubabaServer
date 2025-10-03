package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public ProjectDTO getProject(String project_id, String user_id) {
        Long longProjectId = Long.parseLong(project_id);
        Project project = projectRepository.findById(longProjectId).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        return projectMapper.toDTO(project);
    }
}
