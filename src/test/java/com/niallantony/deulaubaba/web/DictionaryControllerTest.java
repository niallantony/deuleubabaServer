package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.services.FileStorageService;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@Sql(scripts = "/fixtures/categories.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class DictionaryControllerTest {

    @Autowired
    DictionaryRepository dictionaryRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired CommunicationCategoryRepository communicationCategoryRepository;


    @MockitoBean
    FileStorageService fileStorageService;

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("integration-test-db")
            .withUsername("sa")
            .withPassword("sa");

    @BeforeAll
    public static void setUp() {
        postgreSQLContainer.start();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @AfterAll
    public static void cleanUp() {
        postgreSQLContainer.stop();
    }

    @Test
    @Transactional
    public void categoriesExist() {
        List<CommunicationCategory> categories = communicationCategoryRepository.findAll();
        assertEquals(6, categories.size());
        System.out.println(categories);
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getDictionaryListings_withValidId_returnsDictionaryListingsResponse(@Autowired MockMvc mvc)
            throws Exception {
        mvc.perform(get("/dictionary")
                   .with(jwt())
                   .param("student_id", "abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listings.length()").value(1))
                .andExpect(jsonPath("$.expressiontypes.length()").value(1))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getDictionaryListings_withIncorrectId_returns404(@Autowired MockMvc mvc)
            throws Exception {
        mvc.perform(get("/dictionary")
                   .with(jwt())
                   .param("student_id", "def"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getDictionaryListings_withNoDictionaryListings_returns204(@Autowired MockMvc mvc)
            throws Exception {
        mvc.perform(get("/dictionary")
                   .with(jwt())
                   .param("student_id", "abc"))
           .andExpect(status().isNoContent())
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void getDictionaryListings_asUnauthorisedUser_returns401(@Autowired MockMvc mvc)
            throws Exception {
        mvc.perform(get("/dictionary")
                   .with(jwt())
                   .param("student_id", "abc"))
           .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }
}
