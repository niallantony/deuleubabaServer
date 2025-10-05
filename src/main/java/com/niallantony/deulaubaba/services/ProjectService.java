package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.ProjectCollectionsDTO;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.dto.ProjectPreviewDTO;
import com.niallantony.deulaubaba.exceptions.NoProjectsFoundException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            UserRepository userRepository,
            StudentRepository studentRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
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

    public ProjectCollectionsDTO getProjectsOfUser(String user_id) {
        User user = getUserOrThrow(user_id);
        Set<ProjectPreviewDTO> projects = projectRepository.findAllProjectsByUserId(user);
        if (projects.isEmpty()) {
            throw new NoProjectsFoundException("No projects found");
        }
        return createCollections(projects);
    }

    public ProjectCollectionsDTO getProjectsOfStudent(String user_id, String student_id) {
        User user = getUserOrThrow(user_id);
        Student student = getStudentOrThrow(student_id);
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        Set<ProjectPreviewDTO> project = projectRepository.findAllProjectsByStudentId(user, student);
        if (project.isEmpty()) {
            throw new NoProjectsFoundException("No projects found");
        }
        return createCollections(project);
    }

    private Student getStudentOrThrow(String student_id) {
        return studentRepository.findById(student_id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    private User getUserOrThrow(String user_id) {
        return userRepository.findByUserId(user_id)
                             .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    }

    private ProjectCollectionsDTO createCollections(Set<ProjectPreviewDTO> projects) {
        ProjectCollectionsDTO projectCollectionsDTO = new ProjectCollectionsDTO();
        for (ProjectPreviewDTO project : projects) {
            if (project.isCompleted()) {
                projectCollectionsDTO.getCompleted().add(project);
            } else if (project.getStartedOn().isAfter(LocalDate.now())) {
                projectCollectionsDTO.getPending().add(project);
            } else if (project.getStartedOn().isBefore(LocalDate.now()) ||
                    project.getStartedOn().isEqual(LocalDate.now())) {
                projectCollectionsDTO.getCurrent().add(project);
            }
        }
        return projectCollectionsDTO;
    }



    private boolean userAssignedToProject(String user_id, Project project) {
        return project.getUsers().stream()
                .filter(projectUser -> projectUser.getUser() != null)
                .anyMatch(projectUser -> projectUser.getUser().getUserId().equals(user_id));
    }
}
