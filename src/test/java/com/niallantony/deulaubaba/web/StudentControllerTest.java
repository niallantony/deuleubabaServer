package com.niallantony.deulaubaba.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.student.StudentChallengeRequest;
import com.niallantony.deulaubaba.dto.student.StudentCommunicationRequest;
import com.niallantony.deulaubaba.dto.student.StudentRequest;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.services.FileStorageService;
import com.niallantony.deulaubaba.util.StudentTestFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    StudentRepository studentRepository;

    @Autowired
    RoleRepository roleRepository;

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
    @Sql({
            "/fixtures/user.sql",
            "/fixtures/student.sql"
    })
    public void getStudentPreview_withValidStudentId_ReturnsStudentIdAvatar(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/preview")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value("abc"))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.imagesrc").value("./example.jpg"))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @Sql({ "/fixtures/user.sql", })
    public void getStudentPreview_withInvalidStudentId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/preview")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found ABC"));
    }

    @Test
    @Sql({ "/fixtures/student.sql", })
    public void getStudentPreview_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/preview")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"));
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void getStudent_withValidId_returnsStudentDTO(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student")
                .param("id", "ABC")
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }


    @Test
    @Sql({
            "/fixtures/user.sql",
            "/fixtures/student.sql"
    })
    public void getStudent_withInvalidId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student")
                   .param("id", "DEF")
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found DEF"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @Sql({
            "/fixtures/user.sql",
            "/fixtures/student.sql"
    })
    public void getStudent_notLinkedWithUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student")
                   .param("id", "ABC")
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @Sql("/fixtures/student_with_two_users.sql")
    public void getStudentTeam_withValidId_returnsListOfUserAvatars(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/team")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].username", containsInAnyOrder("user1", "user2")))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("length($)").value(2))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    public void getStudentTeam_withInvalidId_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/team")
                .with(jwt())
                .param("id", "DEF"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found DEF"));
    }

    @Test
    @Sql("/fixtures/student.sql")
    public void getStudentTeam_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/team")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("User not found user"));
    }

    @Test
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void getStudentTeam_withUnlinkedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/team")
                .with(jwt())
                .param("id", "ABC"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"));
    }

    @Test
    @Sql("/fixtures/user_with_two_students.sql")
    public void getStudents_withValidId_returnsListOfStudentIdAvatars(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/all")
                .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("length($)").value(2))
           .andExpect(jsonPath("$.[0].studentId").value("abc"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @Sql("/fixtures/user.sql")
    public void getStudents_withNoStudents_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/all")
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Students not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

    }

    @Test
    public void getStudents_withNoUserId_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/student/all")
                .with(jwt()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"));
    }

    @Test
    @Sql("/fixtures/user.sql")
    public void createStudent_withValidData_returnsStudentDTO(@Autowired MockMvc mvc) throws Exception {
        StudentRequest student = StudentTestFactory.createStudentRequest("user");
        String request = objectMapper.writeValueAsString(student);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", request.getBytes());
        mvc.perform(multipart("/student").file(data)
                .with(jwt())
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        assertEquals(1, studentRepository.count());
    }

    @Test
    @Sql("/fixtures/user.sql")
    public void createStudent_withImage_returnsStudentDTOWithImageURI(@Autowired MockMvc mvc) throws Exception {
        doCallRealMethod().when(fileStorageService).swapImage(any(),any());
        when(fileStorageService.storeImage(any())).thenReturn("new-image.jpg");
        StudentRequest student = StudentTestFactory.createStudentRequest("user");
        String request = objectMapper.writeValueAsString(student);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", request.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new-image.jpg", "image/jpg", request.getBytes());
        MvcResult res = mvc.perform(multipart("/student")
                                   .file(data)
                                   .file(image)
                                   .with(jwt())
                                   .contentType(MediaType.MULTIPART_FORM_DATA))
                           .andExpect(status().isOk())
                           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()))
                           .andReturn();

        String body = res.getResponse().getContentAsString();
        String imageSrc = objectMapper.readTree(body).get("imagesrc").asText();
        verify(fileStorageService, times(1)).swapImage(any(), any());
        assertEquals("new-image.jpg", imageSrc);

    }

    @Test
    public void createStudent_asInvalidUser_returns401(@Autowired MockMvc mvc) throws Exception {
        StudentRequest student = StudentTestFactory.createStudentRequest("user");
        String request = objectMapper.writeValueAsString(student);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", request.getBytes());
        mvc.perform(multipart("/student")
                .file(data)
                .with(jwt()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @Sql("/fixtures/user.sql")
    public void createStudent_withInvalidData_returns400(@Autowired MockMvc mvc) throws Exception {
        StudentRequest student = StudentTestFactory.createBadRequest("user");
        String request = objectMapper.writeValueAsString(student);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", request.getBytes());
        mvc.perform(multipart("/student")
                .file(data)
                .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid student data"))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        assertEquals(0, studentRepository.count());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudent_withValidData_updatesStudent(@Autowired MockMvc mvc) throws Exception {
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("Jane");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.name").value(request.getName()))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("Jane", students.iterator().next().getName());
    }

    @Test
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void updateStudent_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("Jane");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("John", students.iterator().next().getName());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudent_withWrongStudentId_returns404(@Autowired MockMvc mvc) throws Exception {
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("Jane");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/def")
                   .file(data)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("John", students.iterator().next().getName());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudent_withMalformedData_returns400(@Autowired MockMvc mvc) throws Exception {
        StudentRequest request = new StudentRequest();
        request.setName("Jane");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid student data"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("John", students.iterator().next().getName());
    }

    @Test
    @Sql("/fixtures/student_with_image_and_user.sql")
    public void updateStudent_withImage_createsNewFileUri(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenReturn("new_image.jpg");
        doCallRealMethod().when(fileStorageService).swapImage(any(),any());
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "image/jpg", "new_image".getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .file(image)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagesrc").value("new_image.jpg"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("new_image.jpg", students.iterator().next().getImagesrc());
        verify(fileStorageService, times(1)).storeImage(any());
        verify(fileStorageService, times(1)).deleteImage("example.jpg");

    }

    @Test
    @Sql("/fixtures/student_with_image_and_user.sql")
    public void updateStudent_whenImageUploadFails_keepsOriginalFile(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenThrow(FileStorageException.class);
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "image/jpg", "new_image".getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .file(image)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.imagesrc").value("example.jpg"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("example.jpg", students.iterator().next().getImagesrc());
        verify(fileStorageService, times(1)).swapImage(any(), any());
        verifyNoMoreInteractions(fileStorageService);
    }

    // FOR NOW!
    @Test
    @Sql("/fixtures/student_with_image_and_user.sql")
    public void updateStudent_whenFileDeletionFails_updatesFile(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenReturn("new_image.jpg");
        doCallRealMethod().when(fileStorageService).swapImage(any(), any());
        doThrow(FileStorageException.class).when(fileStorageService).deleteImage(any());
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "student.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "image/jpg", "new_image".getBytes());
        mvc.perform(multipart(HttpMethod.PUT, "/student/abc")
                   .file(data)
                   .file(image)
                   .with(jwt())
                   .contentType(MediaType.MULTIPART_FORM_DATA))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.imagesrc").value("new_image.jpg"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Iterable<Student> students = studentRepository.findAll();
        assertEquals(1, studentRepository.count());
        assertEquals("new_image.jpg", students.iterator().next().getImagesrc());
        verify(fileStorageService, times(1)).swapImage(any(), any());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentCommunication_withValidRequest_updatesDetails(@Autowired MockMvc mvc) throws Exception {
        StudentCommunicationRequest request = new StudentCommunicationRequest("Communication");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/abc/communication")
                .with(jwt())
                   .content(json)
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.communicationDetails").value("Communication"))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertEquals("Communication", repo.getCommunicationDetails());
    }

    @Test
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void updateStudentCommunication_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        StudentCommunicationRequest request = new StudentCommunicationRequest("Communication");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/abc/communication")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getCommunicationDetails());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentCommunication_withNoStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        StudentCommunicationRequest request = new StudentCommunicationRequest("Communication");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/def/communication")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getCommunicationDetails());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentCommunication_withBadRequest_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "bad request";
        mvc.perform(put("/student/def/communication")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid request"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getCommunicationDetails());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentChallenge_withValidRequest_updatesDetails(@Autowired MockMvc mvc) throws Exception {
        StudentChallengeRequest request = new StudentChallengeRequest("Challenge");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/abc/challenge")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.challengesDetails").value("Challenge"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertEquals("Challenge", repo.getChallengesDetails());
    }

    @Test
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void updateStudentChallenge_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        StudentChallengeRequest request = new StudentChallengeRequest("Challenge");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/abc/challenge")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getChallengesDetails());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentChallenge_withNoStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        StudentChallengeRequest request = new StudentChallengeRequest("Challenge");
        String json = objectMapper.writeValueAsString(request);
        mvc.perform(put("/student/def/challenge")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getChallengesDetails());
    }

    @Test
    @Sql("/fixtures/student_and_user.sql")
    public void updateStudentChallenge_withBadRequest_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "bad request";
        mvc.perform(put("/student/def/challenge")
                   .with(jwt())
                   .content(json)
                   .contentType("application/json"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Invalid request"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        Student repo = studentRepository.findAll().iterator().next();
        assertNull(repo.getChallengesDetails());
    }
}