package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.*;
import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.project.*;
import com.niallantony.deulaubaba.exceptions.*;
import com.niallantony.deulaubaba.mapper.ProjectFeedMapper;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import com.niallantony.deulaubaba.util.ProjectTestFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private ProjectFeedMapper projectFeedMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ProjectUserRepository projectUserRepository;

    @Mock
    private CommunicationCategoryRepository communicationCategoryRepository;

    @Mock
    private ProjectFeedRepository projectFeedRepository;

    @InjectMocks
    private ProjectService projectService;


    @Test
    public void getProject_whenGivenValidId_returnsProject() {
        Project persistedProject = ProjectTestFactory.getEmptyProjectWithUser("abc");
        ProjectDTO expectedProject = new ProjectDTO();
        User user = new User();
        user.setUserId("abc");
        persistedProject.setCreatedBy(user);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(persistedProject));
        when(projectMapper.entityToDto(persistedProject)).thenReturn(expectedProject);

        ProjectDTO response = projectService.getProject(1L, "abc");
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).entityToDto(persistedProject);
        assertEquals(expectedProject, response);
    }

    @Test
    public void getProject_whenGivenInvalidId_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProject(1L, "abc"));
    }

    @Test
    public void getProject_whenUserNotAssignedToProject_returns401() {
        Project persistedProject = new Project();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(persistedProject));
        assertThrows(UserNotAuthorizedException.class, () -> projectService.getProject(1L, "abc"));
    }

    @Test
    public void getAllProjectsOfUser_whenGivenValidId_returnsAllProjects() {
        Set<ProjectPreviewDTO> projects = getProjectPreviewDTOS();
        User user = new User();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(projectRepository.findAllProjectsByUserId(user)).thenReturn(projects);

        ProjectCollectionsDTO response = projectService.getProjectsOfUser("abc");
        verify(projectRepository, times(1)).findAllProjectsByUserId(user);
        assertEquals(1, response.getCompleted().size());
        assertEquals(1, response.getPending().size());
        assertEquals(1, response.getCurrent().size());
    }

    @Test
    public void getAllProjectsOfUser_whenProjectsEmpty_throwsNoProjectFoundException() {
        User user = new User();
        Set<ProjectPreviewDTO> projects = new HashSet<>();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(projectRepository.findAllProjectsByUserId(user)).thenReturn(projects);
        assertThrows(NoProjectsFoundException.class, () -> projectService.getProjectsOfUser("abc"));
    }

    @Test
    public void getAllProjectsOfUser_whenUserDoesNotExist_throwsResourceNotFoundException() {
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectsOfUser("abc"));
    }

    @Test
    public void getAllProjectsOfStudent_whenGivenValidId_returnsAllProjects() {
        Set<ProjectPreviewDTO> projects = getProjectPreviewDTOS();
        User user = new User();
        Student student = new Student();
        student.getUsers().add(user);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(projectRepository.findAllProjectsByStudentId(user, student)).thenReturn(projects);

        ProjectCollectionsDTO response = projectService.getProjectsOfStudent("abc", "123");
        verify(projectRepository, times(1)).findAllProjectsByStudentId(user,student);
        assertEquals(1, response.getCompleted().size());
        assertEquals(1, response.getPending().size());
        assertEquals(1, response.getCurrent().size());
    }

    @Test
    public void getAllProjectsOfStudent_whenStudentDoesNotExist_throwsResourceNotFoundException() {
        User user = new User();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectsOfStudent("abc", "123"));
    }

    @Test void getAllProjectsOfStudent_whenStudentNotAssignedToUser_throwsUserNotAuthorizedException() {
        User user = new User();
        Student student = new Student();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        assertThrows(UserNotAuthorizedException.class, () -> projectService.getProjectsOfStudent("abc", "123"));
    }

    @Test void getAllProjectsOfStudent_whenStudentHasNoProjects_throwsNoProjectFoundException() {
        Set<ProjectPreviewDTO> projects = new HashSet<>();
        User user = new User();
        Student student = new Student();
        student.getUsers().add(user);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(projectRepository.findAllProjectsByStudentId(user, student)).thenReturn(projects);
        assertThrows(NoProjectsFoundException.class, () -> projectService.getProjectsOfStudent("abc", "123"));
        verify(projectRepository, times(1)).findAllProjectsByStudentId(user,student);
    }

    @Test void createProject_whenRequestIsValid_createsProjectAndReturnsDTO() {
        User user = new User();
        user.setUsername("user1");
        Student student = new Student();
        student.getUsers().add(user);
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        ProjectDTO projectDTO = new ProjectDTO();
        Project createdproject = new Project();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(projectMapper.entityToDto(any(Project.class))).thenReturn(projectDTO);
        when(projectMapper.requestToEntity(postDTO)).thenReturn(createdproject);
        when(communicationCategoryRepository.findAll()).thenReturn(new ArrayList<>());

        ProjectDTO result = projectService.createProject(postDTO, null, "abc");
        assertEquals(projectDTO, result);
        verify(projectRepository, times(1)).save(any(Project.class));
        assertEquals(1, createdproject.getUsers().size());
        assertEquals(student, createdproject.getStudent());
        assertEquals(user, createdproject.getCreatedBy());
    }

    @Test void createProject_whenRequestWithImageIsValid_createsProjectAndReturnsDTO() {
        User user = new User();
        user.setUsername("user1");
        Student student = new Student();
        student.getUsers().add(user);
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        ProjectDTO projectDTO = new ProjectDTO();
        Project createdproject = new Project();
        MockMultipartFile image = new MockMultipartFile("image", "image".getBytes());
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(projectMapper.entityToDto(any(Project.class))).thenReturn(projectDTO);
        when(projectMapper.requestToEntity(postDTO)).thenReturn(createdproject);
        when(fileStorageService.storeImage(image)).thenReturn("new_url");
        when(communicationCategoryRepository.findAll()).thenReturn(new ArrayList<>());

        ProjectDTO result = projectService.createProject(postDTO, image, "abc");
        assertEquals(projectDTO, result);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(fileStorageService, times(1)).storeImage(image);
        assertEquals("new_url", createdproject.getImgsrc());
    }

    @Test void createProject_whenStudentIsMissing_throwsResourceNotFoundException() {
        User user = new User();
        user.setUsername("user1");
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.createProject(postDTO, null, "abc"));
    }

    @Test void createProject_whenUserNotAuthorised_throwsUserNotAuthorizedException() {
        User user = new User();
        user.setUsername("user1");
        Student student = new Student();
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        assertThrows(UserNotAuthorizedException.class, () -> projectService.createProject(postDTO, null, "abc"));
    }

    @Test void createProject_whenRequestIsNotValid_throwsInvalidProjectRequestException() {
        User user = new User();
        Student student = new Student();
        student.getUsers().add(user);
        ProjectPostDTO postDTO = new ProjectPostDTO();
        postDTO.setStudentId("123");
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        assertThrows(InvalidProjectDataException.class, () -> projectService.createProject(postDTO, null, "abc"));
    }

    @Test void createProject_whenImageSaveFails_stillSavesProject() {
        User user = new User();
        user.setUsername("user1");
        Student student = new Student();
        student.getUsers().add(user);
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        ProjectDTO projectDTO = new ProjectDTO();
        Project createdproject = new Project();
        MockMultipartFile image = new MockMultipartFile("image", "image".getBytes());
        doCallRealMethod().when(fileStorageService).swapImage(any(),any());
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(projectMapper.entityToDto(any(Project.class))).thenReturn(projectDTO);
        when(projectMapper.requestToEntity(postDTO)).thenReturn(createdproject);
        when(fileStorageService.storeImage(image)).thenThrow(FileStorageException.class);

        ProjectDTO result = projectService.createProject(postDTO, image, "abc");
        assertEquals(projectDTO, result);
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(fileStorageService, times(1)).storeImage(image);
        assertNull(createdproject.getImgsrc());
    }

    @Test void createProject_whenGivenListOfAdditionalUsernames_populatesProjectUsers() {
        User user = new User();
        user.setUsername("user1");
        user.setUserId("abc");
        User user2 = new User();
        user2.setUsername("user2");
        user2.setUserId("def");
        Student student = new Student();
        student.getUsers().add(user);
        ProjectPostDTO postDTO = getProjectPostDTO(user);
        postDTO.getUsernames().add(user2.getUsername());
        ProjectDTO projectDTO = new ProjectDTO();
        Project createdproject = new Project();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(user2));
        when(projectMapper.entityToDto(any(Project.class))).thenReturn(projectDTO);
        when(projectMapper.requestToEntity(postDTO)).thenReturn(createdproject);
        when(communicationCategoryRepository.findAll()).thenReturn(new ArrayList<>());

        ProjectDTO result = projectService.createProject(postDTO, null, "abc");
        assertEquals(projectDTO, result);
        verify(projectRepository, times(1)).save(any(Project.class));
        assertEquals(user, createdproject.getCreatedBy());
        assertEquals(2, createdproject.getUsers().size());
        assertTrue(createdproject.getUsers().stream().anyMatch(
                projectUser -> projectUser.getUser().equals(user2)
        ));
        assertTrue(createdproject.getUsers().stream().anyMatch(
                projectUser -> projectUser.getUser().equals(user)
        ));
    }

    @Test void updateProjectStatus_whenGivenValidRequest_changesProjectStatus() {
        Project project = new Project();
        project.setId(1L);
        Project completedProject = new Project();
        completedProject.setId(2L);
        ProjectUser completedProjectUser = new ProjectUser();
        completedProjectUser.setProject(completedProject);
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProject(project);
        completedProjectUser.setIsCompleted(true);
        when(projectUserRepository.findProjectUserById("abc", 1L)).thenReturn(Optional.of(projectUser));
        when(projectUserRepository.findProjectUserById("abc", 2L)).thenReturn(Optional.of(completedProjectUser));
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(List.of(projectUser));
        when(projectUserRepository.findAllProjectUsersByProjectId(2L)).thenReturn(List.of(completedProjectUser));
        projectService.changeCompletedStatus("abc", 1L, true);
        projectService.changeCompletedStatus("abc", 2L, false);
        assertTrue(projectUser.getIsCompleted());
        assertNotNull(projectUser.getCompletedOn());
        assertNull(completedProjectUser.getCompletedOn());
        assertFalse(completedProjectUser.getIsCompleted());
        verify(projectUserRepository, times(2)).save(any());
    }

    @Test void updateProjectStatus_whenStatusIsIdentical_doesNotChangeStatus() {
        LocalDate date = LocalDate.now();
        ProjectUser completedProjectUser = new ProjectUser();
        ProjectUser projectUser = new ProjectUser();
        completedProjectUser.setIsCompleted(true);
        completedProjectUser.setCompletedOn(date);
        when(projectUserRepository.findProjectUserById("abc", 1L)).thenReturn(Optional.of(projectUser));
        when(projectUserRepository.findProjectUserById("abc", 2L)).thenReturn(Optional.of(completedProjectUser));
        projectService.changeCompletedStatus("abc", 1L, false);
        projectService.changeCompletedStatus("abc", 2L, true);
        assertTrue(completedProjectUser.getIsCompleted());
        assertFalse(projectUser.getIsCompleted());
        assertEquals(date, completedProjectUser.getCompletedOn());
        verify(projectUserRepository, times(0)).save(any());
    }

    @Test void updateProjectStatus_withIncorrectInformation_throws404() {
        when(projectUserRepository.findProjectUserById("abc", 1L)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.changeCompletedStatus("abc", 1L, true)
        );
        assertEquals("Project user not found", exception.getMessage());
    }

    @Test void checkProjectStatus_whenAllStudentsDone_changesProjectStatus() {
        Project project = new Project();
        project.setId(1L);
        ProjectUser earlierUser = new ProjectUser();
        ProjectUser laterUser = new ProjectUser();
        earlierUser.setIsCompleted(true);
        laterUser.setIsCompleted(true);
        earlierUser.setCompletedOn(LocalDate.of(1991, 5, 30));
        laterUser.setCompletedOn(LocalDate.of(2000, 1, 1));
        earlierUser.setProject(project);
        laterUser.setProject(project);
        project.getUsers().add(earlierUser);
        project.getUsers().add(laterUser);
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(Arrays.asList(laterUser, earlierUser));
        projectService.checkProjectStatus(1L);
        verify(projectUserRepository, times(1)).findAllProjectUsersByProjectId(1L);
        assertEquals(true, project.getCompleted());
        assertEquals(LocalDate.of(2000, 1, 1), project.getCompletedOn());
        verify(projectRepository, times(1)).save(any());
    }

    @Test void checkProjectStatus_whenNotAllStudentsDone_doesNotChangeStatus() {
        Project project = new Project();
        project.setId(1L);
        ProjectUser earlierUser = new ProjectUser();
        ProjectUser laterUser = new ProjectUser();
        earlierUser.setIsCompleted(false);
        laterUser.setIsCompleted(true);
        laterUser.setCompletedOn(LocalDate.of(2000, 1, 1));
        earlierUser.setProject(project);
        laterUser.setProject(project);
        project.getUsers().add(earlierUser);
        project.getUsers().add(laterUser);
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(Arrays.asList(laterUser, earlierUser));
        projectService.checkProjectStatus(1L);
        verify(projectUserRepository, times(1)).findAllProjectUsersByProjectId(1L);
        assertFalse(project.getCompleted());
        assertNull(project.getCompletedOn());
        verify(projectRepository, times(0)).save(any());
    }

    @Test void checkProjectStatus_whenNotAllStudentsDoneOnAPreviouslyCompletedProject_changesStatus() {
        Project project = new Project();
        project.setId(1L);
        project.setCompleted(true);
        project.setCompletedOn(LocalDate.now());
        ProjectUser earlierUser = new ProjectUser();
        ProjectUser laterUser = new ProjectUser();
        earlierUser.setIsCompleted(false);
        laterUser.setIsCompleted(true);
        laterUser.setCompletedOn(LocalDate.of(2000, 1, 1));
        earlierUser.setProject(project);
        laterUser.setProject(project);
        project.getUsers().add(earlierUser);
        project.getUsers().add(laterUser);
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(Arrays.asList(laterUser, earlierUser));
        projectService.checkProjectStatus(1L);
        verify(projectUserRepository, times(1)).findAllProjectUsersByProjectId(1L);
        assertFalse(project.getCompleted());
        assertNull(project.getCompletedOn());
        verify(projectRepository, times(1)).save(any());
    }

    @Test void checkProjectStatus_OnAMissingProject_throwsNoProjectsFoundException() {
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(new ArrayList<>());
        assertThrows(
                NoProjectsFoundException.class,
                () ->projectService.checkProjectStatus(1L));
        verify(projectUserRepository, times(1)).findAllProjectUsersByProjectId(1L);
    }

    @Test void patchProjectDetails_whenGivenGoodRequest_updatesProject() {
        Project mockProject = new Project();
        CommunicationCategory pain = new CommunicationCategory();
        pain.setLabel(CommunicationCategoryLabel.PAIN);
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        projectDetailsPatchDTO.setCategories(Set.of(CommunicationCategoryLabel.PAIN));
        projectDetailsPatchDTO.setDescription("description");
        projectDetailsPatchDTO.setObjective("objective");
        projectDetailsPatchDTO.setStartedOn(LocalDate.of(2000,1,1));
        User mockUser = new User();
        MultipartFile image = new MockMultipartFile("image", "image", "image/jpg", "image".getBytes());
        mockUser.setUserId("abc");
        mockProject.setCreatedBy(mockUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(fileStorageService.storeImage(image)).thenReturn("new_url");
        when(communicationCategoryRepository.findAll()).thenReturn(List.of(pain));
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());

        projectService.patchProjectDetails("abc", 1L, projectDetailsPatchDTO, image );

        assertEquals("new_url", mockProject.getImgsrc());
        assertEquals("description", mockProject.getDescription());
        assertEquals("objective", mockProject.getObjective());
        assertEquals(LocalDate.of(2000,1,1), mockProject.getStartedOn());
        assertTrue(mockProject.getCategories().contains(pain));
    }

    @Test void patchProjectDetails_whenGivenBadRequest_throwsBadRequestException() {
        Project mockProject = new Project();
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        User mockUser = new User();
        mockUser.setUserId("abc");
        mockProject.setCreatedBy(mockUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());

        assertThrows(
                InvalidProjectDataException.class,
                () ->projectService.patchProjectDetails("abc", 1L, projectDetailsPatchDTO, null )
        );
    }

    @Test void patchProjectDetails_whenUserNotCreator_throwsUserNotAuthorizedException() {
        Project mockProject = new Project();
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        User mockUser = new User();
        mockUser.setUserId("def");
        mockProject.setCreatedBy(mockUser);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));

        assertThrows(
                UserNotAuthorizedException.class,
                () ->projectService.patchProjectDetails("abc", 1L, projectDetailsPatchDTO, null )
        );
    }

    @Test void patchProjectDetails_whenProjectDoesNotExist_throwsResourceNotFoundException() {
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () ->projectService.patchProjectDetails("abc", 1L, projectDetailsPatchDTO, null )
        );
    }

    @Test void checkProjectStatus_whenRepoReturnsInvalidUsers_returnsServerError() {
        Project project = new Project();
        project.setId(1L);
        project.setCompleted(true);
        project.setCompletedOn(LocalDate.now());
        ProjectUser earlierUser = new ProjectUser();
        ProjectUser laterUser = new ProjectUser();
        earlierUser.setIsCompleted(false);
        laterUser.setIsCompleted(true);
        laterUser.setCompletedOn(LocalDate.of(2000, 1, 1));
        laterUser.setProject(project);
        project.getUsers().add(laterUser);
        when(projectUserRepository.findAllProjectUsersByProjectId(1L)).thenReturn(Arrays.asList(laterUser, earlierUser));
        assertThrows(InvalidProjectUsersException.class,  () -> projectService.checkProjectStatus(1L));
        verify(projectUserRepository, times(1)).findAllProjectUsersByProjectId(1L);
    }

    @Test void deleteProject_withValidDetails_deletesProject() {
        Project mockProject = new Project();
        mockProject.setImgsrc("example.png");
        User mockUser = new User();
        mockUser.setUserId("abc");
        mockProject.setCreatedBy(mockUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));

        projectService.deleteProject("abc", 1L);

        verify(fileStorageService, times(1)).deleteImage("example.png");
        verify(projectRepository, times(1)).delete(mockProject);
    }

    @Test void deleteProject_withIncorrectId_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.deleteProject("abc", 1L)
        );
    }

    @Test void deleteProject_calledByUnauthorizedUser_throwsUserNotAuthorizedException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(new Project()));
        assertThrows(
                UserNotAuthorizedException.class,
                () -> projectService.deleteProject("abc", 1L)
        );
    }

    @Test void deleteProject_whenDeleteFileFails_stillDeletesProject() {
        Project mockProject = new Project();
        mockProject.setImgsrc("example.png");
        User mockUser = new User();
        mockUser.setUserId("abc");
        mockProject.setCreatedBy(mockUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        doThrow(FileStorageException.class).when(fileStorageService).deleteImage(any());

        projectService.deleteProject("abc", 1L);

        verify(fileStorageService, times(1)).deleteImage("example.png");
        verify(projectRepository, times(1)).delete(mockProject);
    }

    @Test void addUser_whenValidRequest_addsUsersToProject() {
        User mockCreator = new User();
        mockCreator.setUserId("abc");
        Project mockProject = new Project();
        mockProject.setCreatedBy(mockCreator);
        User mockNewUser = new User();
        mockNewUser.setUserId("def");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(userRepository.findByIds(List.of("def"))).thenReturn(List.of(mockNewUser));

        ProjectAddUserResponseDTO response = projectService.addUsersToProject("abc", 1L, new ProjectAddUserRequestDTO(List.of("def")));

        assertTrue(mockProject.getUsers().stream().allMatch(pu -> pu.getUser().equals(mockNewUser)));
        assertTrue(response.getNotFound().isEmpty());
    }

    @Test void addUser_whenUsersToProjectNotFound_returnsNotFound() {
        User mockCreator = new User();
        mockCreator.setUserId("abc");
        Project mockProject = new Project();
        mockProject.setCreatedBy(mockCreator);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(userRepository.findByIds(List.of("def"))).thenReturn(new ArrayList<>());

        ProjectAddUserResponseDTO response = projectService.addUsersToProject("abc", 1L, new ProjectAddUserRequestDTO(List.of("def")));

        assertTrue(mockProject.getUsers().isEmpty());
        assertTrue(response.getNotFound().contains("def"));

    }

    @Test void addUser_whenNotCreator_throwsUsersToProjectNotAuthorizedException() {
        Project mockProject = new Project();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));

        assertThrows(
                UserNotAuthorizedException.class,
                () -> projectService.addUsersToProject("abc", 1L, new ProjectAddUserRequestDTO(List.of("def")))
        );
    }

    @Test void addUsersToProject_whenProjectNotFound_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.addUsersToProject("abc", 1L, new ProjectAddUserRequestDTO(List.of("def")))
        );
    }

    @Test void getProjectFeed_whenValidRequest_returnsProjectFeed() {
        Project mockProject = new Project();
        User mockUser = new User();
        mockUser.setUserId("abc");
        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(mockUser);
        projectUser.setProject(mockProject);
        mockProject.getUsers().add(projectUser);
        List<ProjectFeedItem> repoReturn = List.of(new ProjectFeedItem());
        ProjectFeedItemDTO itemDTO = new ProjectFeedItemDTO();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(projectFeedRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(repoReturn);
        when(projectFeedMapper.entityToDto(repoReturn.getFirst())).thenReturn(itemDTO);

        ProjectFeedDTO response = projectService.getProjectFeed(1L, "abc");

        assertEquals(itemDTO, response.getFeed().getFirst());
    }

    @Test void getProjectFeed_whenNoFeedItems_returnsEmptyList() {
        Project mockProject = new Project();
        User mockUser = new User();
        mockUser.setUserId("abc");
        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(mockUser);
        projectUser.setProject(mockProject);
        mockProject.getUsers().add(projectUser);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        when(projectFeedRepository.findByProjectIdOrderByCreatedAtDesc(1L)).thenReturn(List.of());
        ProjectFeedDTO response = projectService.getProjectFeed(1L, "abc");
        assertTrue(response.getFeed().isEmpty());
    }

    @Test void getProjectFeed_whenProjectNotFound_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> projectService.getProjectFeed(1L, "abc")
        );
    }

    @Test void getProjectFeed_whenUnauthorizedUser_throwsUnauthorizedUserException() {
        Project mockProject = new Project();
        when(projectRepository.findById(1L)).thenReturn(Optional.of(mockProject));
        assertThrows(
                UserNotAuthorizedException.class,
                () -> projectService.getProjectFeed(1L, "abc")
        );
    }

    private static ProjectPostDTO getProjectPostDTO(User user) {
        ProjectPostDTO postDTO = new ProjectPostDTO();
        postDTO.setStudentId("123");
        postDTO.setStartedOn(LocalDate.of(2000,1,1));
        postDTO.setType(ProjectType.COMMUNICATION);
        postDTO.setUsernames(new HashSet<>());
        postDTO.getUsernames().add(user.getUsername());
        return postDTO;
    }

    private static @NotNull Set<ProjectPreviewDTO> getProjectPreviewDTOS() {
        LocalDate today = LocalDate.now();
        ProjectPreviewDTO persistedProject = new ProjectPreviewDTO();
        persistedProject.setId(1L);
        persistedProject.setCompleted(true);
        ProjectPreviewDTO persistedProject2 = new ProjectPreviewDTO();
        persistedProject2.setId(2L);
        persistedProject2.setStartedOn(today.plusDays(30));
        ProjectPreviewDTO persistedProject3 = new ProjectPreviewDTO();
        persistedProject3.setId(3L);
        persistedProject3.setStartedOn(today.minusDays(30));
        Set<ProjectPreviewDTO> projects = new HashSet<>();
        projects.add(persistedProject);
        projects.add(persistedProject2);
        projects.add(persistedProject3);
        return projects;
    }
}
