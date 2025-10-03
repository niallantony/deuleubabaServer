package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.dto.ProjectPreviewDTO;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
    }

    public ProjectDTO getProject(String project_id, String user_id) {
        Long longProjectId = Long.parseLong(project_id);
        Project project = projectRepository.findById(longProjectId).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        if (!userAssignedToProject(user_id, project)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        return projectMapper.toDTO(project);
    }

    public Set<ProjectPreviewDTO> getProjectsOfUser(String user_id) {
        User user = userRepository.findByUserId(user_id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return projectRepository.findAllProjectsByUserId(user);
    }

    private boolean userAssignedToProject(String user_id, Project project) {
        return project.getUsers().stream()
                .filter(projectUser -> projectUser.getUser() != null)
                .anyMatch(projectUser -> projectUser.getUser().getUserId().equals(user_id));
    }
}
