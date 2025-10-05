package com.niallantony.deulaubaba.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.data.CommunicationCategoryRepository;
import com.niallantony.deulaubaba.data.DictionaryRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPostRequest;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryPutRequest;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.services.FileStorageService;
import com.niallantony.deulaubaba.util.DictionaryTestFactory;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withValidData_returns201(@Autowired MockMvc mvc) throws Exception {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart("/dictionary")
                .file(data)
                .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        assertEquals(1, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withValidDataAndImage_returns201(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenReturn("test.jpg");
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image".getBytes());
        mvc.perform(multipart("/dictionary")
                   .file(data)
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.imgsrc").value("test.jpg"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        verify(fileStorageService).storeImage(any());
        assertEquals(1, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withValidDataAndImageError_stillReturns201(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenThrow(FileStorageException.class);
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "test.jpg", "image/jpeg", "image".getBytes());
        mvc.perform(multipart("/dictionary")
                   .file(data)
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.id").value(1))
           .andExpect(jsonPath("$.imgsrc").isEmpty())
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        verify(fileStorageService).storeImage(any());
        assertEquals(1, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withinValidData_returns400(@Autowired MockMvc mvc) throws Exception {
        DictionaryPostRequest request = DictionaryTestFactory.createBadRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart("/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Entry data not valid"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withUnauthorisedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart("/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void addDictionary_withInvalidStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        DictionaryPostRequest request = DictionaryTestFactory.createDictionaryPostRequest();
        request.setStudentId("def");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart("/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withValidData_updatesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                .file(data)
                .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(request.getId()))
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        DictionaryEntry entry = dictionaryRepository.findById(request.getId()).orElse(null);
        assertNotNull(entry);
        assertEquals(entry.getTitle(), request.getTitle());

    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withInvalidData_returns400(@Autowired MockMvc mvc) throws Exception {
        DictionaryPutRequest request = DictionaryTestFactory.createBadPutReqeust();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Entry data not valid"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        DictionaryEntry entry = dictionaryRepository.findById(1L).orElse(null);
        assertNotNull(entry);
        assertEquals("Title", entry.getTitle());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withMalformedRequest_returns400(@Autowired MockMvc mvc) throws Exception {
        String json = "Bad Request";
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Entry data not valid"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        DictionaryEntry entry = dictionaryRepository.findById(1L).orElse(null);
        assertNotNull(entry);
        assertEquals("Title", entry.getTitle());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withMissingEntry_returns404(@Autowired MockMvc mvc) throws Exception {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Entry not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        List<DictionaryEntry> dictionaries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, dictionaries.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql",
            "/fixtures/dictionary.sql",
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withValidDataAndImage_updatesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenReturn("new_image.jpg");
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "application/json", "image".getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(request.getId()))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        DictionaryEntry entry = dictionaryRepository.findById(request.getId()).orElse(null);
        verify(fileStorageService).deleteImage("example.jpg");
        assertNotNull(entry);
        assertEquals("new_image.jpg", entry.getImgsrc());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateDictionary_withValidDataAndImageError_updatesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        when(fileStorageService.storeImage(any())).thenThrow(FileStorageException.class);
        DictionaryPutRequest request = DictionaryTestFactory.createDictionaryPutRequest();
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile data = new MockMultipartFile("data", "test.json", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "new_image.jpg", "application/json", "image".getBytes());
        mvc.perform(multipart( HttpMethod.PUT, "/dictionary")
                   .file(data)
                   .file(image)
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.id").value(request.getId()))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));

        DictionaryEntry entry = dictionaryRepository.findById(request.getId()).orElse(null);
        verify(fileStorageService, never()).deleteImage("example.jpg");
        assertNotNull(entry);
        assertEquals("example.jpg", entry.getImgsrc());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteDictionary_withValidId_deletesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/dictionary/1")
                .with(jwt()))
                .andExpect(status().isNoContent())
                .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> entries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, entries.size());
        verify(fileStorageService, times(1)).deleteImage("example.jpg");
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteDictionary_withInvalidId_deletesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/dictionary/2")
                   .with(jwt()))
           .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Dictionary not found"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> entries = dictionaryRepository.findAll().stream().toList();
        assertEquals(1, entries.size());
        verify(fileStorageService, never()).deleteImage("example.jpg");
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/user.sql",
            "/fixtures/student.sql",
            "/fixtures/dictionary.sql",
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteDictionary_withUnauthorizedUser_deletesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(delete("/dictionary/1")
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> entries = dictionaryRepository.findAll().stream().toList();
        assertEquals(1, entries.size());
        verify(fileStorageService, never()).deleteImage("example.jpg");
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/dictionary.sql"
    })
    @Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void deleteDictionary_whenDeleteFileThrows_stillDeletesDictionaryEntry(@Autowired MockMvc mvc) throws Exception {
        doThrow(FileStorageException.class).when(fileStorageService).deleteImage("example.jpg");
        mvc.perform(delete("/dictionary/1")
                   .with(jwt()))
           .andExpect(status().isNoContent())
           .andDo((r) -> System.out.println(r.getResponse().getContentAsString()));
        List<DictionaryEntry> entries = dictionaryRepository.findAll().stream().toList();
        assertEquals(0, entries.size());
        verify(fileStorageService, times(1)).deleteImage("example.jpg");
    }
}

