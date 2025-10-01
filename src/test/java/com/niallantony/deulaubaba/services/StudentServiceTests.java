package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.InvalidStudentDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.StudentMapper;
import com.niallantony.deulaubaba.util.StudentTestFactory;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StudentServiceTests {

    @Mock
    StudentRepository studentRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    FileStorageService fileStorageService;
    @Mock
    StudentMapper studentMapper;
    @Mock
    JsonUtils jsonUtils;
    @InjectMocks
    StudentService studentService;

    private Student getMockStudent() {
        Student student = new Student();
        student.setName("Kyle");
        student.setSchool("School");
        student.setAge(12);
        student.setGrade(3);
        student.setSetting("Setting");
        student.setDisability("Disability");
        return student;
    }

    @Test
    public void getStudentPreviewById_whenGivenGoodId_returnsStudentPreview() {
        Student student = new Student();
        student.setStudentId("abc");
        student.setName("Kyle");
        student.setImagesrc("./example.jpg");

        when(studentRepository.findById("abc")).thenReturn(Optional.of(student));

        StudentIdAvatar studentIdAvatar = studentService.getStudentPreviewById("ABC");

        assertEquals(student.getStudentId(), studentIdAvatar.getStudentId());
        assertEquals(student.getName(), studentIdAvatar.getName());
        assertEquals(student.getImagesrc(), studentIdAvatar.getImagesrc());
    }

    @Test
    public void getStudentPreviewById_whenGivenBadId_ThrowsException() {
        when(studentRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentPreviewById("ABC"));
    }

    @Test
    public void getStudentById_whenGivenGoodId_returnsStudentDTO() {
        Student student = new Student();
        StudentDTO mockDTO = new StudentDTO();
        when(studentRepository.findById("abc")).thenReturn(Optional.of(student));
        when(studentMapper.toDTO(student)).thenReturn(mockDTO);
        StudentDTO studentDTO = studentService.getStudentById("abc");
        assertEquals(mockDTO.getStudentId(), studentDTO.getStudentId());
    }

    @Test
    public void getStudentById_whenGivenBadId_throwsException() {
        when(studentRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentById("ABC"));
    }

    @Test
    public void getStudentTeam_whenGivenGoodId_returnsUsers() {
        User user = new User();
        user.setUsername("username");
        user.setImagesrc("./example.jpg");
        user.setUserType("user type");
        Student student = new Student();
        student.getUsers().add(user);
        when(studentRepository.findById("abc")).thenReturn(Optional.of(student));
        List<UserAvatar> userAvatars = studentService.getStudentTeam("abc");
        assertEquals(1, userAvatars.size());
        assertEquals("user type", userAvatars.getFirst().getUserType());
        assertEquals("username", userAvatars.getFirst().getUsername());
        assertEquals("./example.jpg", userAvatars.getFirst().getImagesrc());
    }

    @Test
    public void getStudentTeam_whenGivenStudentWithNoTeam_returnsEmptyList() {
        Student student = new Student();
        when(studentRepository.findById("abc")).thenReturn(Optional.of(student));
        List<UserAvatar> userAvatars = studentService.getStudentTeam("abc");
        assertEquals(0, userAvatars.size());
    }

    @Test
    public void getStudentTeam_whenGivenBadStudentId_throwsException() {
        when(studentRepository.findById("abc")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudentTeam("abc"));
    }

    @Test
    public void getStudents_whenGivenGoodId_returnsStudents() {
        StudentIdAvatar student = new StudentIdAvatar(
                "abc",
                "Kyle",
                "example.jpg"
        );
        when(studentRepository.findAllOfUserId("123")).thenReturn(List.of(student));
        List<StudentIdAvatar> students = studentService.getStudents("123");
        assertEquals(1, students.size());
        assertEquals("Kyle", students.getFirst().getName());
        assertEquals("example.jpg", students.getFirst().getImagesrc());
        assertEquals("abc", students.getFirst().getStudentId());
    }

    @Test
    public void getStudents_whenGivenBadId_throwsException() {
        when(studentRepository.findAllOfUserId("123")).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class, () -> studentService.getStudents("123"));
    }

    @Test
    public void createStudent_whenGivenGoodRequest_returnsStudent() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        User user = new User();
        Student student = getMockStudent();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentMapper.toStudent(request)).thenReturn(student);
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());

        StudentDTO result = studentService.createStudent(json,null,  "abc");
        assertTrue(student.getUsers().contains(user));
        assertEquals(6, student.getStudentId().length());
        assertNotNull(result);
    }

    @Test
    public void createStudent_whenGivenBadUserId_throwsException() {
        String json = "json-placeholder";
        StudentRequest request = new StudentRequest();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        assertThrows(UserNotAuthorizedException.class, () -> studentService.createStudent(json, null, "abc"));
    }

    @Test
    public void createStudent_whenRequestNotValid_throwsException() {
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenThrow(InvalidStudentDataException.class);
        assertThrows(InvalidStudentDataException.class, () -> studentService.createStudent(json, null, "abc"));
    }

    @Test
    public void createStudent_whenGivenImage_savesImage(){
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        User user = new User();
        Student student = getMockStudent();
        MultipartFile image = mock(MultipartFile.class);
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentMapper.toStudent(request)).thenReturn(student);
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(fileStorageService.storeImage(image)).thenReturn("new_url");

        StudentDTO result = studentService.createStudent(json, image, "abc");
        assertTrue(student.getUsers().contains(user));
        assertEquals("new_url", student.getImagesrc());
        assertEquals(6, student.getStudentId().length());
        assertNotNull(result);
    }

    @Test
    public void createStudent_whenImageDoesntSave_stillSaves() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        User user = new User();
        Student student = getMockStudent();
        MultipartFile image = mock(MultipartFile.class);
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentMapper.toStudent(request)).thenReturn(student);
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(fileStorageService.storeImage(image)).thenThrow(FileStorageException.class);
        StudentDTO result = studentService.createStudent(json, image, "abc");
        assertTrue(student.getUsers().contains(user));
        assertEquals(6, student.getStudentId().length());
        assertNull(student.getImagesrc());
        assertNotNull(result);
    }

    @Test
    public void updateStudentDetails_whenGivenGoodRequest_updatesStudent() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("John");
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());

        studentService.updateStudentDetails("123", json, null, "abc");
        assertTrue(student.getUsers().contains(user));
        assertEquals("John", student.getName());
    }

    @Test
    public void updateStudentDetails_whenGivenBadStudentId_throwsException() {
        String json = "json-placeholder";
        StudentRequest request = new StudentRequest();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> studentService.updateStudentDetails("123", json, null, "abc")
                );
        assertEquals("Student not found 123", exception.getMessage());
    }

    @Test
    public void updateStudentDetails_whenGivenBadUserId_throwsException() {
        String json = "json-placeholder";
        StudentRequest request = new StudentRequest();
        request.setName("John");
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        UserNotAuthorizedException exception =
                assertThrows(
                        UserNotAuthorizedException.class,
                        () -> studentService.updateStudentDetails("123", json, null, "abc")
                );
        assertEquals("User not found abc", exception.getMessage());
    }

    @Test
    public void updateStudentDetails_whenGivenUnauthorisedUserId_throwsException() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("John");
        User user = new User();
        Student student = getMockStudent();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        UserNotAuthorizedException exception =
                assertThrows(
                        UserNotAuthorizedException.class,
                        () -> studentService.updateStudentDetails("123", json, null, "abc")
                );
        assertEquals("Unauthorized access", exception.getMessage());
    }

    @Test
    public void updateStudentDetails_whenGivenImage_updatesAndDeletesCorrectly() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("John");
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);
        student.setImagesrc("old_url");
        MultipartFile image = mock(MultipartFile.class);

        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(fileStorageService.storeImage(image)).thenReturn("new_url");

        studentService.updateStudentDetails("123", json, image, "abc");
        assertTrue(student.getUsers().contains(user));
        assertEquals("John", student.getName());
        assertEquals("new_url",student.getImagesrc());
        verify(fileStorageService).deleteImage("old_url");
    }

    @Test
    public void updateStudentDetails_whenImageFileDoesntSave_preservesOldImage() {
        String json = "json-placeholder";
        StudentRequest request = StudentTestFactory.createStudentRequest("user");
        request.setName("John");
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);
        student.setImagesrc("old_url");
        MultipartFile image = mock(MultipartFile.class);

        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());
        when(fileStorageService.storeImage(image)).thenThrow(FileStorageException.class);

        studentService.updateStudentDetails("123", json, image, "abc");
        verify(fileStorageService).storeImage(image);
        assertEquals("John", student.getName());
        assertEquals("old_url",student.getImagesrc());
        verifyNoMoreInteractions(fileStorageService);
    }

    @Test
    public void updateStudentDetails_whenGivenBadRequest_throwsException() {
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenThrow(InvalidStudentDataException.class);
        assertThrows(InvalidStudentDataException.class, () -> studentService.updateStudentDetails("123", json, null, "abc"));
    }

    @Test
    public void updateStudentCommunicationDetails_whenGivenGoodRequest_updatesStudent() {
        StudentCommunicationRequest request = new StudentCommunicationRequest("New Request");
        String json = "json-placeholder";
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);

        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());

        studentService.updateStudentCommunication("123", json, "abc");
        verify(studentRepository).save(student);
        assertEquals("New Request", student.getCommunicationDetails());
    }

    @Test
    public void updateStudentCommunicationDetails_whenGivenBadStudentId_throwsException() {
        StudentCommunicationRequest request = new StudentCommunicationRequest("New Request");
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.updateStudentCommunication("123", json, "abc")
        );
        assertEquals("Student not found 123", exception.getMessage());
    }

    @Test
    public void updateStudentCommunicationDetails_whenGivenBadUserId_throwsException() {
        StudentCommunicationRequest request = new StudentCommunicationRequest("New Request");
        String json = "json-placeholder";
        Student student = new Student();

        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());

        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.updateStudentCommunication("123", json, "abc")
        );
        assertEquals("User not found abc", exception.getMessage());
    }

    @Test
    public void updateStudentCommunicationDetails_whenGivenUnauthorisedUserId_throwsException() {
        StudentCommunicationRequest request = new StudentCommunicationRequest("New Request");
        String json = "json-placeholder";
        User user = new User();
        Student student = getMockStudent();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.updateStudentCommunication("123", json, "abc")
        );
        assertEquals("Unauthorized access", exception.getMessage());

    }

    @Test
    public void updateStudentCommunicationDetails_whenGivenBadRequest_throwsException() {
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenThrow(InvalidStudentDataException.class);
        assertThrows(InvalidStudentDataException.class, () -> studentService.updateStudentCommunication("123", json, "abc"));
    }

    @Test
    public void updateStudentChallengeDetails_whenGivenGoodRequest_updatesStudent() {
        StudentChallengeRequest request = new StudentChallengeRequest("New Request");
        String json = "json-placeholder";
        User user = new User();
        Student student = getMockStudent();
        student.getUsers().add(user);
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(studentMapper.toDTO(student)).thenReturn(new StudentDTO());

        studentService.updateStudentChallenge("123", json, "abc");
        verify(studentRepository).save(student);
        assertEquals("New Request", student.getChallengesDetails());

    }

    @Test
    public void updateStudentChallengeDetails_whenGivenBadStudentId_throwsException() {
        StudentChallengeRequest request = new StudentChallengeRequest("New Request");
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.updateStudentChallenge("123", json, "abc")
        );
        assertEquals("Student not found 123", exception.getMessage());
    }

    @Test
    public void updateStudentChallengeDetails_whenGivenBadUserId_throwsException() {
        StudentChallengeRequest request = new StudentChallengeRequest("New Request");
        String json = "json-placeholder";
        Student student = new Student();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.updateStudentChallenge("123", json, "abc")
        );
        assertEquals("User not found abc", exception.getMessage());
    }

    @Test
    public void updateStudentChallengeDetails_whenGivenBadRequest_throwsException() {
        String json = "json-placeholder";
        when(jsonUtils.parse(eq(json), any(), any())).thenThrow(InvalidStudentDataException.class);
        assertThrows(InvalidStudentDataException.class, () -> studentService.updateStudentChallenge("123", json, "abc"));
    }

    @Test
    public void updateStudentChallengeDetails_whenGivenUnauthorisedUserId_throwsException() {
        StudentChallengeRequest request = new StudentChallengeRequest("New Request");
        String json = "json-placeholder";
        User user = new User();
        Student student = getMockStudent();
        when(jsonUtils.parse(eq(json), any(), any())).thenReturn(request);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));

        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.updateStudentChallenge("123", json, "abc")
        );
        assertEquals("Unauthorized access", exception.getMessage());
    }

    @Test
    public void getAuthorisedStudent_whenGivenValidData_returnsStudent() {
        Student student = new Student();
        User user = new User();
        student.getUsers().add(user);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));

        Student result = studentService.getAuthorisedStudent("123", "abc");
        assertEquals(student, result);
    }

    @Test
    public void getAuthorisedStudent_withBadStudentId_throwsException() {
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.getAuthorisedStudent("123", "abc")
        );
        assertEquals("Student not found 123", exception.getMessage());
    }

    @Test
    public void getAuthorisedStudent_withBadUserId_throwsException() {
        Student student = new Student();
        when (studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.getAuthorisedStudent("123", "abc")
        );
        assertEquals("User not found abc", exception.getMessage());
    }

    @Test
    public void getAuthorisedStudent_withUnauthorisedUserId_throwsException() {
        Student student = new Student();
        User user = new User();
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.getAuthorisedStudent("123", "abc")
        );
        assertEquals("Unauthorized access", exception.getMessage());
    }

    @Test
    public void studentBelongsToUser_withValidData_isTrue() {
        Student student = new Student();
        User user = new User();
        student.getUsers().add(user);
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));

        boolean result = studentService.studentBelongsToUser("123", "abc");
        assertTrue(result);
    }

    @Test
    public void studentBelongsToUser_withBadUserId_throwsException() {
        Student student = new Student();
        when (studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.empty());
        UserNotAuthorizedException exception = assertThrows(
                UserNotAuthorizedException.class,
                () -> studentService.studentBelongsToUser("123", "abc")
        );
        assertEquals("User not found abc", exception.getMessage());
    }

    @Test
    public void studentBelongsToUser_withUnauthorisedUserId_isFalse() {
        Student student = new Student();
        User user = new User();
        when(studentRepository.findById("123")).thenReturn(Optional.of(student));
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));

        boolean result = studentService.studentBelongsToUser("123", "abc");
        assertFalse(result);
    }

    @Test
    public void studentBelongsToUser_withBadStudentId_throwsException() {
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> studentService.studentBelongsToUser("123", "abc")
        );
        assertEquals("Student not found 123", exception.getMessage());
    }
}
