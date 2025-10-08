package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.*;
import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.project.*;
import com.niallantony.deulaubaba.exceptions.*;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import com.niallantony.deulaubaba.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final JsonUtils jsonUtils;
    private final FileStorageService fileStorageService;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final ProjectUserRepository projectUserRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            UserRepository userRepository,
            StudentRepository studentRepository,
            JsonUtils jsonUtils,
            FileStorageService fileStorageService,
            CommunicationCategoryRepository communicationCategoryRepository,
            ProjectUserRepository projectUserRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.jsonUtils = jsonUtils;
        this.fileStorageService = fileStorageService;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.projectUserRepository = projectUserRepository;
    }

    public ProjectDTO getProject(String project_id, String user_id) {
        Long longProjectId = Long.parseLong(project_id);
        Project project = projectRepository.findById(longProjectId).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        if (!userAssignedToProject(user_id, project)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        ProjectDTO projectDTO = projectMapper.entityToDto(project);
        if (project.getCreatedBy().getUserId().equals(user_id)) {
           projectDTO.isOwnProject(true);
        }
        return projectDTO;
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

    public ProjectDTO createProject(ProjectPostDTO request, MultipartFile image, String user_id) {
        User user = getUserOrThrow(user_id);
        Student student = getStudentOrThrow(request.getStudentId());
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        validatePost(request);
        Project project = projectMapper.requestToEntity(request);
        project.setCategories(getCategories(request));
        Set<ProjectUser> projectUsers = projectUsersFromPost(request);
        project.setUsers(projectUsers);
        project.setCreatedBy(user);
        project.setStudent(student);
        if (image != null) {
            try {
                String filename = fileStorageService.storeImage(image);
                project.setImgsrc(filename);
            } catch (FileStorageException e) {
                log.warn("File not saved: ", e);
            }
        }
        projectRepository.save(project);
        ProjectDTO projectDTO = projectMapper.entityToDto(project);
        projectDTO.isOwnProject(true);
        return projectDTO;
    }

    public void changeCompletedStatus(String user_id, String project_id, boolean completed)  {
        long longProjectId;
        try {
            longProjectId = Long.parseLong(project_id);
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project ID");
        }
        ProjectUser relation = projectUserRepository.findProjectUserById(user_id, longProjectId).orElseThrow(
                () -> new ResourceNotFoundException("Project user not found")
        );
        if (relation.getIsCompleted() == completed) {
            return;
        }
        relation.setIsCompleted(completed);
        if (completed) {
            relation.setCompletedOn(LocalDate.now());
        } else {
            relation.setCompletedOn(null);
        }
        projectUserRepository.save(relation);
    }

    public void checkProjectStatus(Long project_id) {
        List<ProjectUser> projectUsers = projectUserRepository.findAllProjectUsersByProjectId(project_id);
        if (projectUsers.isEmpty()) {
            throw new NoProjectsFoundException("No projects found");
        }
        if (projectUsers.stream().anyMatch(user -> user.getProject() == null || !Objects.equals(user.getProject().getId(), project_id))) {
            throw new InvalidProjectUsersException("Project users not returned correctly - try again later");
        }
        Project project = projectUsers.getFirst().getProject();
        if (projectUsers.stream().allMatch(ProjectUser::getIsCompleted)) {
            project.setCompleted(true);
            project.setCompletedOn(projectUsers.getFirst().getCompletedOn());
        } else {
            project.setCompleted(false);
            project.setCompletedOn(null);
        }
    }

    private Set<ProjectUser> projectUsersFromPost(ProjectPostDTO post) {
        return post.getUsernames().stream().map(userRepository::findByUsername)
                .map(userOptional -> {
                    User user = userOptional.orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    ProjectUser projectUser = new ProjectUser();
                    projectUser.setUser(user);
                    return projectUser;
                })
                .collect(Collectors.toSet());
    }

    private void validatePost(ProjectPostDTO post) {
        if (
                post.getStudentId() == null
                        || post.getStartedOn() == null
                        || post.getType() == null
                        || post.getUsernames().isEmpty()
        ) {
            throw new InvalidProjectDataException("Invalid request");
        }
    }

    private Set<CommunicationCategory> getCategories(ProjectPostDTO post) {
        return post.getCategories().stream().map(label ->
               communicationCategoryRepository.findByLabel(label).orElseThrow()
        ).collect(Collectors.toSet());
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
