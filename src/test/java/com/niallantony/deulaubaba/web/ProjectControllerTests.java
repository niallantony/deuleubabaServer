package com.niallantony.deulaubaba.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.data.*;
import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.ProjectUser;
import com.niallantony.deulaubaba.dto.project.ProjectAddUserRequestDTO;
import com.niallantony.deulaubaba.dto.project.ProjectDetailsPatchDTO;
import com.niallantony.deulaubaba.dto.project.ProjectPostDTO;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.services.FileStorageService;
import com.niallantony.deulaubaba.util.ProjectTestFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Testcontainers
@Sql(scripts = "/fixtures/categories.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTests {

    @Autowired
    ProjectRepository projectRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    CommunicationCategoryRepository communicationCategoryRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FileStorageService fileStorageService;

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("integration-test-db")
            .withUsername("sa")
            .withPassword("sa");
    @Autowired
    private ProjectUserRepository projectUserRepository;

    @BeforeAll
    public static void setUp() { postgreSQLContainer.start(); }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @AfterAll
    public static void cleanUp() {postgreSQLContainer.stop();}

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void getProject_withValidId_returnsProject(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                   .with(jwt())
                   .param("id", String.valueOf(1)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.categories[0].label").value("REJECTION"))
           .andExpect(jsonPath("$.userStatuses[0].user.username").value("user1"))
           .andExpect(jsonPath("$.userStatuses[0].completed").value(false))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void getProject_ofCreatedProjectWithValidId_returnsProjectWithTrueCreated(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                   .with(jwt())
                   .param("id", String.valueOf(1)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ownProject").value(true))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unowned.sql"
    })
    public void getProject_ofUnownedProjectWithValidId_returnsProjectWithFalseCreated(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                   .with(jwt())
                   .param("id", String.valueOf(1)))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ownProject").value(false))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    public void getProject_withInvalidId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                   .with(jwt())
                   .param("id", String.valueOf(1)))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Project not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unrelated.sql"
    })
    public void getProject_withUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                   .with(jwt())
                   .param("id", String.valueOf(1)))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/one_of_each_project.sql"
    })
    public void getAllProjectsOfUser_withValidId_returnsProject(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all")
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.completed.length()").value(1))
           .andExpect(jsonPath("$.pending.length()").value(1))
           .andExpect(jsonPath("$.current.length()").value(1))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    public void getAllProjectsOfUser_forUserWithNoProjects_returns204(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all")
                   .with(jwt()))
           .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("No projects found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/one_of_each_project.sql"
    })
    public void getAllProjectsOfStudent_withValidId_returnsCollectedProjects(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all/abc")
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.completed.length()").value(1))
           .andExpect(jsonPath("$.pending.length()").value(1))
           .andExpect(jsonPath("$.current.length()").value(1))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/one_of_each_project.sql"
    })
    public void getAllProjectsOfStudent_withInvalidId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all/def")
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql",
            "/fixtures/one_of_each_project.sql"
    })
    public void getAllProjectsOfStudent_asUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all/abc")
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    public void getAllProjectsOfStudent_forStudentWithNoProjects_returns204(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/all/abc")
                   .with(jwt()))
           .andExpect(status().isNoContent())
           .andExpect(jsonPath("$.message").value("No projects found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({"/fixtures/student_and_user.sql"})
    public void createProject_withValidRequest_returnsCorrectResponse(@Autowired MockMvc mvc) throws Exception {
        ProjectPostDTO postDTO = ProjectTestFactory.getProjectPostDTOWithUser("user1");
        String json = objectMapper.writeValueAsString(postDTO);
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        mvc.perform(multipart("/project")
                .file("data", json.getBytes())
                .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.userStatuses[0].user.username").value("user1"))
                .andExpect(jsonPath("$.ownProject").value(true))
                .andExpect(redirectedUrl(redirect))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<Project> projects = projectRepository.findAll().stream().toList();
        assertEquals(1, projects.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({"/fixtures/student_and_user.sql", "/fixtures/user_another.sql"})
    public void createProject_withValidRequestAndOtherUsernames_returnsCorrectResponse(@Autowired MockMvc mvc) throws Exception {
        ProjectPostDTO postDTO = ProjectTestFactory.getProjectPostDTOWithUser("user1");
        postDTO.getUsernames().add("user2");
        String json = objectMapper.writeValueAsString(postDTO);
        mvc.perform(multipart("/project")
                   .file("data", json.getBytes())
                   .with(jwt()))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.userStatuses.length()").value(2))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({"/fixtures/student_and_user.sql"})
    public void createProject_withImage_returnsCorrectResponse(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenReturn("new_image.jpg");
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());
        ProjectPostDTO postDTO = ProjectTestFactory.getProjectPostDTOWithUser("user1");
        String json = objectMapper.writeValueAsString(postDTO);
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "image/jpg", "image".getBytes());
        mvc.perform(multipart("/project")
                   .file("data", json.getBytes())
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.imgsrc").value("new_image.jpg"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<Project> projects = projectRepository.findAll().stream().toList();
        assertEquals(1, projects.size());
    }


    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({"/fixtures/student_and_user.sql"})
    public void createProject_withInvalidRequest_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "json-placeholder";
        mvc.perform(multipart("/project")
                   .file("data", json.getBytes())
                   .with(jwt()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid request"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({"/fixtures/user.sql", "/fixtures/student.sql"})
    public void createProject_withUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        ProjectPostDTO postDTO = ProjectTestFactory.getProjectPostDTOWithUser("user1");
        String json = objectMapper.writeValueAsString(postDTO);
        mvc.perform(multipart("/project")
                   .file("data", json.getBytes())
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/user.sql")
    public void createProject_forMissingStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        ProjectPostDTO postDTO = ProjectTestFactory.getProjectPostDTOWithUser("user1");
        String json = objectMapper.writeValueAsString(postDTO);
        mvc.perform(multipart("/project")
                   .file("data", json.getBytes())
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void changeCompleteStatus_withValidRequestAndOneUser_changesIncompleteProjectToComplete(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        mvc.perform(patch("/project/status/1")
                .with(jwt())
                .contentType("application/json")
                .content("{\"isCompleted\":\"true\"}"))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl(redirect))
                .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
        Project project = projectRepository.findById(1L).orElseThrow();
        ProjectUser projectUser = projectUserRepository.findProjectUserById("user", 1L).orElseThrow();
        assertEquals(true, projectUser.getIsCompleted());
        assertNotNull(projectUser.getCompletedOn());
        assertEquals(true, project.getCompleted());
        assertNotNull(project.getCompletedOn());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unrelated.sql"
    })
    public void changeCompleteStatus_asUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(patch("/project/status/1")
                   .with(jwt())
                   .contentType("application/json")
                   .content("{\"isCompleted\":\"true\"}"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Project user not found"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unrelated.sql"
    })
    public void changeCompleteStatus_ofNonExistantProject_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(patch("/project/status/2")
                   .with(jwt())
                   .contentType("application/json")
                   .content("{\"isCompleted\":\"true\"}"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Project user not found"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unowned.sql"
    })
    public void changeCompleteStatus_whenOtherUserNotComplete_doesNotChangeProjectStatus(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        mvc.perform(patch("/project/status/1")
                   .with(jwt())
                   .contentType("application/json")
                   .content("{\"isCompleted\":\"true\"}"))
           .andExpect(status().isNoContent())
           .andExpect(redirectedUrl(redirect))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
        Project project = projectRepository.findById(1L).orElseThrow();
        ProjectUser projectUser = projectUserRepository.findProjectUserById("user", 1L).orElseThrow();
        assertEquals(true, projectUser.getIsCompleted());
        assertNotNull(projectUser.getCompletedOn());
        assertFalse(project.getCompleted());
        assertNull(project.getCompletedOn());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_completed.sql"
    })
    public void changeCompleteStatus_toIncomplete_changesStatus(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        mvc.perform(patch("/project/status/1")
                   .with(jwt())
                   .contentType("application/json")
                   .content("{\"isCompleted\":\"false\"}"))
           .andExpect(status().isNoContent())
           .andExpect(redirectedUrl(redirect))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
        Project project = projectRepository.findById(1L).orElseThrow();
        ProjectUser projectUser = projectUserRepository.findProjectUserById("user", 1L).orElseThrow();
        assertFalse(projectUser.getIsCompleted());
        assertNull(projectUser.getCompletedOn());
        assertFalse(project.getCompleted());
        assertNull(project.getCompletedOn());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_halfdone.sql"
    })
    public void changeCompleteStatus_ofHalfDoneProject_changesStatusAndAppliesLatestData(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        mvc.perform(patch("/project/status/1")
                   .with(jwt())
                   .contentType("application/json")
                   .content("{\"isCompleted\":\"true\"}"))
           .andExpect(status().isNoContent())
           .andExpect(redirectedUrl(redirect))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
        Project project = projectRepository.findById(1L).orElseThrow();
        ProjectUser projectUser = projectUserRepository.findProjectUserById("user", 1L).orElseThrow();
        assertTrue(projectUser.getIsCompleted());
        assertNotNull(projectUser.getCompletedOn());
        assertTrue(project.getCompleted());
        assertTrue(LocalDate.of(2000, 1, 1).isBefore(project.getCompletedOn()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void patchProjectDetails_withGoodRequest_changesProjectDetails(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        projectDetailsPatchDTO.setCategories(Set.of(CommunicationCategoryLabel.PAIN));
        projectDetailsPatchDTO.setDescription("new description");
        projectDetailsPatchDTO.setObjective("new objective");
        projectDetailsPatchDTO.setStartedOn(LocalDate.of(2020,1,1));
        String json = objectMapper.writeValueAsString(projectDetailsPatchDTO);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "application/json", "image".getBytes());
        when(fileStorageService.storeImage(image)).thenReturn("new_image.jpg");
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());

        mvc.perform(multipart(HttpMethod.PATCH, "/project/1")
                .file(data)
                .file(image)
                .with(jwt()))
                .andExpect(status().isNoContent())
                .andExpect(redirectedUrl(redirect))
                .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));

        Project project = projectRepository.findById(1L).orElseThrow();
        assertAll(
                () -> assertEquals("new_image.jpg", project.getImgsrc()),
                () -> assertEquals(projectDetailsPatchDTO.getStartedOn(), project.getStartedOn()),
                () -> assertEquals(projectDetailsPatchDTO.getDescription(), project.getDescription()),
                () -> assertEquals(projectDetailsPatchDTO.getObjective(), project.getObjective()),
                () -> assertEquals(1, project.getCategories().size()),
                () -> assertTrue(project.getCategories().stream().allMatch(cat -> cat.getLabel().equals(CommunicationCategoryLabel.PAIN)))
        );
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void patchProjectDetails_withBadRequest_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "json_placeholder";
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());
        mvc.perform(multipart(HttpMethod.PATCH, "/project/1")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Invalid request"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unowned.sql"
    })
    public void patchProjectDetails_ofUnownedProject_returns401(@Autowired MockMvc mvc) throws Exception {
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        projectDetailsPatchDTO.setCategories(Set.of(CommunicationCategoryLabel.PAIN));
        projectDetailsPatchDTO.setDescription("new description");
        projectDetailsPatchDTO.setObjective("new objective");
        projectDetailsPatchDTO.setStartedOn(LocalDate.of(2020,1,1));
        String json = objectMapper.writeValueAsString(projectDetailsPatchDTO);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());

        mvc.perform(multipart(HttpMethod.PATCH, "/project/1")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    public void patchProjectDetails_ofMissingProject_returns404(@Autowired MockMvc mvc) throws Exception {
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        projectDetailsPatchDTO.setCategories(Set.of(CommunicationCategoryLabel.PAIN));
        projectDetailsPatchDTO.setDescription("new description");
        projectDetailsPatchDTO.setObjective("new objective");
        projectDetailsPatchDTO.setStartedOn(LocalDate.of(2020,1,1));
        String json = objectMapper.writeValueAsString(projectDetailsPatchDTO);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());

        mvc.perform(multipart(HttpMethod.PATCH, "/project/2")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Project not found"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void patchProjectDetails_whenImageFailsToSave_stillSavesProject(@Autowired MockMvc mvc) throws Exception {
        String redirect = ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/1";
        ProjectDetailsPatchDTO projectDetailsPatchDTO = new ProjectDetailsPatchDTO();
        projectDetailsPatchDTO.setCategories(Set.of(CommunicationCategoryLabel.PAIN));
        projectDetailsPatchDTO.setDescription("new description");
        projectDetailsPatchDTO.setObjective("new objective");
        projectDetailsPatchDTO.setStartedOn(LocalDate.of(2020,1,1));
        String json = objectMapper.writeValueAsString(projectDetailsPatchDTO);
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "application/json", "image".getBytes());
        when(fileStorageService.storeImage(image)).thenThrow(FileStorageException.class);
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());

        mvc.perform(multipart(HttpMethod.PATCH, "/project/1")
                   .file(data)
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isNoContent())
           .andExpect(redirectedUrl(redirect))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));

        verify(fileStorageService, never()).deleteImage(any());
        Project project = projectRepository.findById(1L).orElseThrow();
        assertAll(
                () -> assertEquals("example.jpg", project.getImgsrc()),
                () -> assertEquals(projectDetailsPatchDTO.getStartedOn(), project.getStartedOn()),
                () -> assertEquals(projectDetailsPatchDTO.getDescription(), project.getDescription()),
                () -> assertEquals(projectDetailsPatchDTO.getObjective(), project.getObjective()),
                () -> assertEquals(1, project.getCategories().size()),
                () -> assertTrue(project.getCategories().stream().allMatch(cat -> cat.getLabel().equals(CommunicationCategoryLabel.PAIN)))
        );
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    public void patchProjectDetails_withInvalidId_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "json_placeholder";
        MockMultipartFile data = new MockMultipartFile("data", "data.json", "application/json", json.getBytes());

        mvc.perform(multipart(HttpMethod.PATCH, "/project/a")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid project_id: a"))
           .andDo((r) -> System.out.println(r.getResponse().getRedirectedUrl()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_completed.sql"
    })
    public void deleteProject_withValidId_deletesProjectAndUsers(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/project/1")
                .with(jwt()))
                .andExpect(status().isNoContent());

        List<Project> projects = projectRepository.findAll();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(0, projects.size());
        assertEquals(0, users.size());

    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void deleteProject_withInvalidId_throws404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/project/2")
                   .with(jwt()))
           .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Project not found"));

        List<Project> projects = projectRepository.findAll();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(1, projects.size());
        assertEquals(1, users.size());
        verifyNoInteractions(fileStorageService);

    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unowned.sql"
    })
    public void deleteProject_asUnauthorizedUser_throws401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/project/1")
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"));

        List<Project> projects = projectRepository.findAll();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(1, projects.size());
        assertEquals(2, users.size());
        verifyNoInteractions(fileStorageService);
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql"
    })
    public void deleteProject_withMisformedId_throws400(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/project/a")
                   .with(jwt()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid project_id: a"));

        List<Project> projects = projectRepository.findAll();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(1, projects.size());
        assertEquals(1, users.size());
        verifyNoInteractions(fileStorageService);

    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql",
            "/fixtures/user_another.sql"
    })
    public void addUserToProject_withValidId_addsANewProjectUser(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/1/add-user")
                .with(jwt())
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notFound.length()").value(0))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Project project = projectRepository.findById(1L).orElseThrow();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(2, users.size());
        assertEquals(2, project.getUsers().size());
        assertTrue(users.stream().anyMatch(user -> user.getUser().getUserId().equals("user2")));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql",
    })
    public void addUserToProject_whenUserDoesNotExist_returnsNotFound(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/1/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.notFound.length()").value(1))
                .andExpect(jsonPath("$.notFound[0]").value("user2"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Project project = projectRepository.findById(1L).orElseThrow();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(1, users.size());
        assertEquals(1, project.getUsers().size());
        assertTrue(users.stream().noneMatch(user -> user.getUser().getUserId().equals("user2")));
    }


    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project.sql",
            "/fixtures/user_another.sql"
    })
    public void addUserToProject_whenSomeUsersDoNotExist_returnsOkWithNotFoundUsers(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2", "user3"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/1/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.notFound.length()").value(1))
           .andExpect(jsonPath("$.notFound[0]").value("user3"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Project project = projectRepository.findById(1L).orElseThrow();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertEquals(2, users.size());
        assertEquals(2, project.getUsers().size());
        assertTrue(users.stream().anyMatch(user -> user.getUser().getUserId().equals("user2")));
        assertTrue(users.stream().noneMatch(user -> user.getUser().getUserId().equals("user3")));
    }

    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_two_users.sql",
            "/fixtures/user_another.sql"
    })
    @Test
    public void addUserToProject_whenGivenAUserAlreadyAdded_doesNotAddTheUserTwice(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2", "user3"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/1/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.notFound.length()").value(0))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Project project = projectRepository.findById(1L).orElseThrow();
        List<ProjectUser> users = projectUserRepository.findAllProjectUsersByProjectId(1L);
        assertAll(
                () -> assertEquals(3, users.size()),
                () -> assertEquals(3, project.getUsers().size()),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUser().getUserId().equals("user"))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUser().getUserId().equals("user2"))),
                () -> assertTrue(users.stream().anyMatch(user -> user.getUser().getUserId().equals("user3")))
        );
    }

    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/project_unowned.sql",
    })
    @Test
    public void addUserToProject_whenProjectIsNotOwned_returns401(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/1/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

    }

    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    @Test
    public void addUserToProject_whenProjectDoesNotExist_throws404(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/2/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Project not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    @Test
    public void addUserToProject_whenProjecIdIsNotNumber_throws400(@Autowired MockMvc mvc) throws Exception {
        ProjectAddUserRequestDTO request = new ProjectAddUserRequestDTO(List.of("user2"));
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(patch("/project/a/add-user")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid project_id: a"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
        "/fixtures/student_and_user.sql",
        "/fixtures/project.sql",
        "/fixtures/project_feed_items.sql"
    })
    @Test
    public void getProjectFeed_withValidRequest_returnsProjectFeed(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project/1/feed")
                .with(jwt()))
                .andExpect(status().isOk())
                .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

}
