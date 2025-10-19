package com.niallantony.deulaubaba.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.data.StudentFeedRepository;
import com.niallantony.deulaubaba.domain.StudentFeedEmotion;
import com.niallantony.deulaubaba.domain.StudentFeedItem;
import com.niallantony.deulaubaba.dto.feed.FeedPostDTO;
import com.niallantony.deulaubaba.services.FeedService;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@Sql(scripts = "/fixtures/teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SpringBootTest
@AutoConfigureMockMvc
public class FeedControllerTests {

    @Autowired
    FeedService feedService;

    @Autowired
    StudentFeedRepository studentFeedRepository;

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:13.2")
            .withDatabaseName("integration-test-db")
            .withUsername("sa")
            .withPassword("sa");
    @Autowired
    private ObjectMapper jacksonObjectMapper;

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
            "/fixtures/student_feed.sql"
    })
    public void getFeed_ofValidStudent_returnsFeed(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.feed.length()").value(1))
           .andExpect(jsonPath("$.feed[0].body").value("Comment Body"))
           .andExpect(jsonPath("$.feed[0].emotions.length()").value(2))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/student_feed.sql"
    })
    public void getFeed_ofNonExistantStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/def")
                   .with(jwt()))
           .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Student not found def"))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void getFeed_ofNoneAffiliatedStudent_returns401(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt()))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message").value("Unauthorized access"))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/student_feed_many.sql"
    })
    public void getFeed_whenMoreThanPageSize_paginatesResponse(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt()))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.feed.length()").value(10))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/student_feed_many.sql"
    })
    public void getFeed_withCustomPageSettings_paginatesAppropriately(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt())
                   .param("page", "1"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.feed.length()").value(1))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/student_feed_many.sql"
    })
    public void getFeed_withCustomSizeSettings_paginatesAppropriately(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt())
                   .param("size", "5"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.feed.length()").value(5))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student_and_user.sql",
            "/fixtures/student_feed_many.sql"
    })
    public void getFeed_withMalformedPageSettings_throws403(@Autowired MockMvc mvc) throws Exception {
        mvc.perform(get("/feed/abc")
                   .with(jwt())
                   .param("page", "-1"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.message").value("Page index must not be less than zero"))
           .andDo(r -> System.out.println(r.getResponse().getContentAsString()));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    public void postComment_withValidRequest_returns204(@Autowired MockMvc mvc) throws Exception {
        String redirect = String.valueOf(URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/feed").toUriString() + "/abc"));
        FeedPostDTO postDTO = new FeedPostDTO();
        postDTO.setBody("Comment");
        postDTO.getEmotions().add(StudentFeedEmotion.HAPPY);
        String json = jacksonObjectMapper.writeValueAsString(postDTO);
        mvc.perform(post("/feed/abc")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isNoContent())
           .andExpect(redirectedUrl(redirect));
        List<StudentFeedItem> feed = studentFeedRepository.findAllByStudentIdOrderByCreatedAtDesc("abc");
        assertEquals(1, feed.size());
        assertTrue(feed.stream().anyMatch(f -> f.getBody().equals("Comment")));
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    public void postComment_withInvalidRequest_returns403(@Autowired MockMvc mvc) throws Exception {
        String json = "json";
        mvc.perform(post("/feed/abc")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid comment post"));
        List<StudentFeedItem> feed = studentFeedRepository.findAllByStudentIdOrderByCreatedAtDesc("abc");
        assertEquals(0, feed.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    public void postComment_withEmptyBody_returns403(@Autowired MockMvc mvc) throws Exception {
        FeedPostDTO postDTO = new FeedPostDTO();
        postDTO.getEmotions().add(StudentFeedEmotion.HAPPY);
        String json = jacksonObjectMapper.writeValueAsString(postDTO);
        mvc.perform(post("/feed/abc")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Empty body"));
        List<StudentFeedItem> feed = studentFeedRepository.findAllByStudentIdOrderByCreatedAtDesc("abc");
        assertEquals(0, feed.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql({
            "/fixtures/student.sql",
            "/fixtures/user.sql"
    })
    public void postComment_asUnauthorizedUser_returns401(@Autowired MockMvc mvc) throws Exception {
        FeedPostDTO postDTO = new FeedPostDTO();
        postDTO.setBody("Comment");
        postDTO.getEmotions().add(StudentFeedEmotion.HAPPY);
        String json = jacksonObjectMapper.writeValueAsString(postDTO);
        mvc.perform(post("/feed/abc")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Unauthorized access"));
        List<StudentFeedItem> feed = studentFeedRepository.findAllByStudentIdOrderByCreatedAtDesc("abc");
        assertEquals(0, feed.size());
    }

    @Test
    @SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
    @Sql("/fixtures/student_and_user.sql")
    public void postComment_forNonExistingStudent_returns404(@Autowired MockMvc mvc) throws Exception {
        FeedPostDTO postDTO = new FeedPostDTO();
        postDTO.setBody("Comment");
        postDTO.getEmotions().add(StudentFeedEmotion.HAPPY);
        String json = jacksonObjectMapper.writeValueAsString(postDTO);
        mvc.perform(post("/feed/def")
                   .with(jwt())
                   .content(json))
           .andExpect(status().isNotFound())
           .andExpect(jsonPath("$.message").value("Student not found def"));
        List<StudentFeedItem> feed = studentFeedRepository.findAllByStudentIdOrderByCreatedAtDesc("abc");
        assertEquals(0, feed.size());

    }
}
