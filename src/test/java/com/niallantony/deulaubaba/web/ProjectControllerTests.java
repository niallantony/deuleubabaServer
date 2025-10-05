package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;



import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("integration-test-db")
            .withUsername("sa")
            .withPassword("sa");

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
            "/fixtures/unrelatedproject.sql"
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

}
