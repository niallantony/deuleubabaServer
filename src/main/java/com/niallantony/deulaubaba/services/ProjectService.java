package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.*;
import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.project.*;
import com.niallantony.deulaubaba.exceptions.*;
import com.niallantony.deulaubaba.mapper.ProjectFeedMapper;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectFeedMapper projectFeedMapper;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final FileStorageService fileStorageService;
    private final CommunicationCategoryRepository communicationCategoryRepository;
    private final ProjectUserRepository projectUserRepository;
    private final ProjectFeedRepository projectFeedRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ProjectMapper projectMapper,
            UserRepository userRepository,
            StudentRepository studentRepository,
            FileStorageService fileStorageService,
            CommunicationCategoryRepository communicationCategoryRepository,
            ProjectUserRepository projectUserRepository,
            ProjectFeedMapper projectFeedMapper,
            ProjectFeedRepository projectFeedRepository
    ) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.fileStorageService = fileStorageService;
        this.communicationCategoryRepository = communicationCategoryRepository;
        this.projectUserRepository = projectUserRepository;
        this.projectFeedRepository = projectFeedRepository;
        this.projectFeedMapper = projectFeedMapper;
    }

    public ProjectDTO getProject(Long project_id, String user_id) {
        Project project = getAuthorizedProject(project_id, user_id);
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

    public ProjectFeedDTO getProjectFeed(Long project_id, String user_id) {
        getAuthorizedProject(project_id, user_id);
        List<ProjectFeedItemDTO> feedItems = projectFeedRepository
                .findByProjectIdOrderByCreatedAtDesc(project_id)
                .stream()
                .map(projectFeedMapper::entityToDto)
                .toList();
        ProjectFeedDTO response = new ProjectFeedDTO();
        response.setFeed(feedItems);
        return response;
    }

    public void addUserComment(Long project_id, String user_id, ProjectFeedCommentDTO request) {
        ProjectUser projectUser = projectUserRepository.findProjectUserById(user_id, project_id)
                .orElseThrow(() -> new InvalidProjectUsersException("Invalid request"));
        ProjectFeedItem comment = new ProjectFeedItem();
        if (request.getBody() == null || request.getBody().trim().isEmpty()) {
            throw new InvalidProjectDataException("Comment is empty");
        }
        comment.setProject(projectUser.getProject());
        comment.setUser(projectUser.getUser());
        comment.setBody(request.getBody());
        comment.setType(ProjectFeedItemType.COMMENT);
        comment.setCreatedAt(LocalDateTime.now());
        projectFeedRepository.save(comment);
    }

    public void deleteUserComment(Long comment_id, String user_id) {
        ProjectFeedItem comment = projectFeedRepository.findById(comment_id).orElseThrow(
                () -> new ResourceNotFoundException("Comment not found")
        );

        if (!comment.getUser().getUserId().equals(user_id)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }

        projectFeedRepository.delete(comment);
    }

    public ProjectDTO createProject(ProjectPostDTO request, MultipartFile image, String user_id) {
        User user = getUserOrThrow(user_id);
        Student student = getStudentOrThrow(request.getStudentId());
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        validatePost(request);
        Project project = projectMapper.requestToEntity(request);
        project.setCategories(getCategories(request.getCategories()));
        Set<ProjectUser> projectUsers = projectUsersFromPost(request);
        project.setUsers(projectUsers);
        project.setCreatedBy(user);
        project.setStudent(student);
        fileStorageService.swapImage(image, project);
        projectRepository.save(project);
        ProjectDTO projectDTO = projectMapper.entityToDto(project);
        projectDTO.isOwnProject(true);
        addSystemEventMessage(project, "Project created");
        projectUsers.forEach(pu ->
                addNewUserFeedMessage(project, pu.getUser())
        );
        return projectDTO;
    }

    public void patchProjectDetails(String user_id, Long project_id, ProjectDetailsPatchDTO request, MultipartFile image) {
        Project project = projectRepository.findById(project_id).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        if (!project.getCreatedBy().getUserId().equals(user_id)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        fileStorageService.swapImage(image, project);
        validatePatch(request);
        applyDetailChanges(project, request);
        projectRepository.save(project);
        addSystemEventMessage(project, "Project updated");
    }

    private void validatePatch(ProjectDetailsPatchDTO request) {
        if (
                request.getCategories() == null
                || request.getCategories().isEmpty()
                || request.getObjective() == null
                || request.getStartedOn() == null
        ) throw new InvalidProjectDataException("Invalid patch request");
    }

    private void applyDetailChanges(Project project, ProjectDetailsPatchDTO request) {
        project.setObjective(request.getObjective());
        project.setDescription(request.getDescription());
        project.setStartedOn(request.getStartedOn());
        project.setCategories(getCategories(request.getCategories()));
    }

    public void changeCompletedStatus(String user_id, Long project_id, boolean completed)  {
        ProjectUser relation = projectUserRepository.findProjectUserById(user_id, project_id).orElseThrow(
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
        checkProjectStatus(project_id);
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
        if (projectUsers.stream().allMatch(ProjectUser::getIsCompleted) && !project.getCompleted()) {
            project.setCompleted(true);
            project.setCompletedOn(projectUsers.getFirst().getCompletedOn());
            projectRepository.save(project);
        } else if (projectUsers.stream().anyMatch(user -> !user.getIsCompleted()) && project.getCompleted()){
            project.setCompleted(false);
            project.setCompletedOn(null);
            projectRepository.save(project);
        }
    }

    public void deleteProject(String user_id, Long project_id) {
        Project project = getCreatedProject(user_id, project_id);
        if (project.getImgsrc() != null) {
            try {
                fileStorageService.deleteImage(project.getImgsrc());
            } catch (FileStorageException e) {
                log.warn("Image not deleted: {}", project.getImgsrc(), e);
            }
        }
        projectRepository.delete(project);
    }

    public ProjectAddUserResponseDTO addUsersToProject(String user_id, Long project_id, ProjectAddUserRequestDTO request) {
        Project project = getCreatedProject(user_id, project_id);
        List<User> users = userRepository.findByIds(request.getToAdd());

        List<String> notFound = request.getToAdd().stream().filter( name ->
                users.stream()
                     .filter(user -> user.getUserId() != null)
                     .noneMatch(user -> user.getUserId().equals(name))
        ).toList();

        users.stream().filter(user ->
                     project.getUsers().stream().noneMatch(current -> current.getUser().equals(user)) )
             .forEach(user -> {
                 ProjectUser projectUser = new ProjectUser();
                 projectUser.setProject(project);
                 projectUser.setUser(user);
                 project.getUsers().add(projectUser);
             });

        projectRepository.save(project);
        project.getUsers().forEach(user -> addNewUserFeedMessage(project, user.getUser()));
        return new ProjectAddUserResponseDTO(notFound);
    }

    private @NotNull Project getCreatedProject(String user_id, Long project_id) {
        Project project = projectRepository.findById(project_id).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        if (project.getCreatedBy() == null || !project.getCreatedBy().getUserId().equals(user_id)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        return project;
    }

    private Set<ProjectUser> projectUsersFromPost(ProjectPostDTO post) {
        Set<User> users = userRepository.findAllByUsernames(post.getUsernames());
        return users.stream()
                .map(user -> {
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

    private Set<CommunicationCategory> getCategories(Set<CommunicationCategoryLabel> label) {
        Set<CommunicationCategory> categories = new HashSet<>(communicationCategoryRepository.findAll());
        return categories.stream().filter(category ->
                label.contains(category.getLabel())
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

    private void addNewUserFeedMessage(Project project, User user) {
        ProjectFeedItem comment = new ProjectFeedItem();
        comment.setProject(project);
        comment.setUser(user);
        comment.setBody("New user added: " + user.getUserType());
        comment.setType(ProjectFeedItemType.NEW_USER);
        comment.setCreatedAt(LocalDateTime.now());
        projectFeedRepository.save(comment);
    }

    private void addSystemEventMessage(Project project, String eventBody) {
        ProjectFeedItem comment = new ProjectFeedItem();
        comment.setProject(project);
        comment.setBody(eventBody);
        comment.setType(ProjectFeedItemType.EVENT);
        comment.setCreatedAt(LocalDateTime.now());
        projectFeedRepository.save(comment);
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

    private Project getAuthorizedProject(Long project_id, String user_id) {
        Project project = projectRepository.findById(project_id).orElseThrow(
                () -> new ResourceNotFoundException("Project not found")
        );
        if (!userAssignedToProject(user_id, project)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        return project;
    }
}
