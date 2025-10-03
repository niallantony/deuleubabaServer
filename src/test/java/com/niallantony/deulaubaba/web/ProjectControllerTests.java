package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
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


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    @Transactional
    public void getProject_withValidId_returnsProject(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/project")
                .with(jwt())
                .param("id", String.valueOf(1)))
                .andExpect(status().isOk())
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

    }

}
