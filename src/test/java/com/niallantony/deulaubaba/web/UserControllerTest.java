package com.niallantony.deulaubaba.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.services.FileStorageService;
import com.niallantony.deulaubaba.util.UserTestFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    FileStorageService fileStorageService;

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:12-alpine")
            .withDatabaseName("integration-tests-db")
            .withUsername("sa")
            .withPassword("sa");

    @BeforeAll
    public static void beforeAll() {
        postgreSQLContainer.start();
    }

    @AfterEach
    public void afterEach() {
        userRepository.deleteAll();
        studentRepository.deleteAll();
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    }

    @AfterAll
    public static void afterAll() {
        postgreSQLContainer.stop();
    }

    @Test
    @Sql("/fixtures/user.sql")
    public void getProfile_ofAuthorisedUser_returnsUserDTO(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/me").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("name"))
            .andExpect(jsonPath("$.email").value("email@email.com"))
            .andExpect(jsonPath("$.userType").value("teacher"))
            .andExpect(jsonPath("$.username").value("user1"))
            .andExpect(jsonPath("$.role.name").value("ROLE_USER"));
    }

    @Test
    public void getProfile_ofNoneAuthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getProfile_ofNonExistentUser_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/me").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    public void postUser_withValidDataAndNoImage_SavesUserAndReturnsDTO(@Autowired MockMvc mvc) throws Exception {
        UserRequest request = UserTestFactory.createUserRequest();
        String body = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "user.json", "application/json", body.getBytes());
        mvc.perform(multipart("/me").file(data).with(jwt()).contentType("multipart/form-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.userType").value(request.getUserType()))
                .andExpect(jsonPath("$.username").value(request.getUsername()))
                .andExpect(jsonPath("$.role.name").value("ROLE_USER"));

        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals(request.getUsername(), users.getFirst().getUsername());
    }

    @Test
    public void postUser_withValidDataAndImage_SavesUserAndReturnsDTO(@Autowired MockMvc mvc) throws Exception {
        UserRequest request = UserTestFactory.createUserRequest();
        String body = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "user.json", "application/json", body.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "user.jpg", "image/jpg", "fake-image".getBytes(
                StandardCharsets.UTF_8));
        when(fileStorageService.storeImage(image)).thenReturn("user.jpg");
        mvc.perform(multipart("/me")
                   .file(data)
                   .file(image)
                   .with(jwt())
                   .contentType("multipart/form-data"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value(request.getName()))
           .andExpect(jsonPath("$.email").value(request.getEmail()))
           .andExpect(jsonPath("$.userType").value(request.getUserType()))
           .andExpect(jsonPath("$.username").value(request.getUsername()))
           .andExpect(jsonPath("$.role.name").value("ROLE_USER"))
            .andExpect(jsonPath("$.imagesrc").value("user.jpg"));


        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals(request.getUsername(), users.getFirst().getUsername());
        verify(fileStorageService).storeImage(image);
    }

    @Test
    public void postUser_withInvalidData_returns400(@Autowired MockMvc mvc) throws Exception {
        UserRequest request = UserTestFactory.createBadUserRequest();
        String body = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "user.json", "application/json", body.getBytes());
        mvc.perform(multipart("/me")
                .file(data)
                .with(jwt())
                .contentType("multipart/form-data"))
            .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Missing required user fields"));
        List<User> users = userRepository.findAll();
        assertEquals(0, users.size());
    }

    @Test
    @Transactional
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void linkStudent_withValidData_returns200andStudentDTO(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(post("/me/link-student")
                .with(jwt())
                .param("code","ABC"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("John"));
        User user = userRepository.findByUserId("user").orElseThrow();
        Set<Student> students = user.getStudents();
        assertEquals(1, students.size());
        assertEquals("John", students.iterator().next().getName());
        assertEquals("abc", students.iterator().next().getStudentId());
    }

    @Test
    @Transactional
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void linkStudent_withBadStudentCode_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(post("/me/link-student")
                   .with(jwt())
                   .param("code","DEF"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student Not Found"));
        User user = userRepository.findByUserId("user").orElseThrow();
        Set<Student> students = user.getStudents();
        assertEquals(0, students.size());
    }

    @Test
    @Transactional
    @Sql("/fixtures/student.sql")
    public void linkStudent_withBadUserId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(post("/me/link-student")
                   .with(jwt())
                   .param("code","ABC"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("User Not Found"));
        Student db_student = studentRepository.findById("abc").orElseThrow();
        assertEquals(0, db_student.getUsers().size());
    }

}