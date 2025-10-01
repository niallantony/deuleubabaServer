package com.niallantony.deulaubaba.services;


import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.InvalidUserDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.mapper.StudentMapper;
import com.niallantony.deulaubaba.mapper.UserMapper;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private StudentMapper studentMapper;
    @Mock
    private JsonUtils jsonUtils;


    @InjectMocks
    UserService userService;

    private final UserRequest mockUserRequest = new UserRequest(
            "Username",
            "Name",
            "User Type",
            "Email"
    );
    @Test
    public void getUser_whenGivenValidId_thenReturnUserDTO() {
        User mockUser = new User();
        UserDTO expected = new UserDTO(
                "name",
                "email",
                "teacher",
                "image",
                "username",
                new Role("ROLE_USER")
        );

        when(userRepository.findByUserId("ABC")).thenReturn(Optional.of(mockUser));
        when(userMapper.toDTO(mockUser)).thenReturn(expected);
        UserDTO userDTO = userService.getUser("ABC");
        assertEquals(expected, userDTO);
        verify(userMapper).toDTO(mockUser);
        verify(userRepository).findByUserId("ABC");
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    public void getUser_whenGivenInvalidId_thenThrowsResourceNotFoundException() {
        when(userRepository.findByUserId("ABC")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUser("ABC"));
        verify(userRepository).findByUserId("ABC");
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    public void createUser_whenGivenValidDataWithNoImage_thenReturnUserDTO() {
        User mockUser = new User();
        UserDTO mockDTO = new UserDTO();
        String data = "data";
        when(jsonUtils.parse(eq(data), eq(UserRequest.class), any())).thenReturn(mockUserRequest);

        when(userMapper.toNewUser(argThat(req ->
                        req.getName().equals("Name") &&
                        req.getUserType().equals("User Type") &&
                        req.getEmail().equals("Email") &&
                        req.getUsername().equals("Username")
                ), eq("ABC"))).thenReturn(mockUser);
        when(userMapper.toDTO(mockUser)).thenReturn(mockDTO);

        UserDTO result = userService.createUser("ABC", data, null);

        assertSame(mockDTO, result);
        verify(userMapper).toDTO(mockUser);
        verify(userMapper).toNewUser(any(UserRequest.class), any(String.class));
        verify(userRepository).existsByUsername(mockUserRequest.getUsername());
        verify(userRepository).existsByEmail(mockUserRequest.getEmail());
        verify(userRepository).save(mockUser);
        verifyNoMoreInteractions(userRepository, userMapper);
    }

    @Test
    public void createUser_whenGivenValidDataWithImage_thenReturnUserDTO() {
        MultipartFile mockFile = mock(MultipartFile.class);
        User mockUser = new User();
        UserDTO mockDTO = new UserDTO();
        String data = "data";
        when(jsonUtils.parse(eq(data), eq(UserRequest.class), any())).thenReturn(mockUserRequest);
        when(userMapper.toNewUser(mockUserRequest, "ABC")).thenReturn(mockUser);
        when(userMapper.toDTO(mockUser)).thenReturn(mockDTO);
        when(fileStorageService.storeImage(mockFile)).thenReturn("./mock-file.png");

        UserDTO result = userService.createUser("ABC", data, mockFile);

        assertSame(mockDTO, result);
        assertEquals("./mock-file.png", mockUser.getImagesrc());
        verify(userMapper).toDTO(mockUser);
        verify(userMapper).toNewUser(argThat(req ->
                req.getName().equals("Name") &&
                        req.getUserType().equals("User Type") &&
                        req.getEmail().equals("Email") &&
                        req.getUsername().equals("Username")
        ), any(String.class));
        verify(userRepository).existsByUsername(mockUserRequest.getUsername());
        verify(userRepository).existsByEmail(mockUserRequest.getEmail());
        verify(userRepository).save(mockUser);
        verify(fileStorageService).storeImage(mockFile);
        verifyNoMoreInteractions(userRepository, userMapper, fileStorageService);
    }

    @Test
    public void createUser_whenGivenInvalidData_thenThrowsInvalidUserDataException() {
        String data = "data";
        when(jsonUtils.parse(eq(data), any(),any())).thenThrow(InvalidUserDataException.class);
        assertThrows(
                InvalidUserDataException.class,
                () -> userService.createUser("ABC", data, null)
        );
    }


    @Test
    public void createUser_whenFileStorageFailure_ThenSavesUserWithNoImage() throws InvalidUserDataException {
        MultipartFile mockFile = mock(MultipartFile.class);
        User mockUser = new User();
        UserDTO mockDTO = new UserDTO();
        String data = "data";
        when(jsonUtils.parse(eq(data), eq(UserRequest.class), any())).thenReturn(mockUserRequest);
        when(userMapper.toNewUser(mockUserRequest, "ABC")).thenReturn(mockUser);
        when(userMapper.toDTO(mockUser)).thenReturn(mockDTO);
        when(fileStorageService.storeImage(mockFile)).thenThrow(FileStorageException.class);
        UserDTO result = userService.createUser("ABC", data, mockFile);

        assertSame(mockDTO, result);
        assertNull(mockUser.getImagesrc());
        verify(userMapper).toDTO(mockUser);
        verify(userMapper).toNewUser(argThat(req ->
                req.getName().equals("Name") &&
                        req.getUserType().equals("User Type") &&
                        req.getEmail().equals("Email") &&
                        req.getUsername().equals("Username")
        ), any(String.class));
        verify(userRepository).existsByUsername(mockUserRequest.getUsername());
        verify(userRepository).existsByEmail(mockUserRequest.getEmail());
        verify(userRepository).save(mockUser);
        verify(fileStorageService).storeImage(mockFile);
        verifyNoMoreInteractions(userRepository, userMapper, fileStorageService);
    }

    @Test
    public void linkStudent_whenGivenValidCodes_returnsStudentDTO() {
        User mockUser = new User();
        Student mockStudent = new Student();
        StudentDTO mockDTO = new StudentDTO();
        when(userRepository.findByUserId("ABC")).thenReturn(Optional.of(mockUser));
        when(studentRepository.findById("123")).thenReturn(Optional.of(mockStudent));
        when(studentMapper.toDTO(mockStudent)).thenReturn(mockDTO);

        StudentDTO result = userService.linkStudent("ABC", "123");
        assertTrue(mockUser.getStudents().contains(mockStudent));
        assertTrue(mockStudent.getUsers().contains(mockUser));
        verify(userRepository).findByUserId("ABC");
        verify(userRepository).save(mockUser);
        verify(studentRepository).findById("123");
        verify(studentMapper).toDTO(mockStudent);
        verifyNoMoreInteractions(userRepository, studentMapper, studentRepository);
        assertSame(mockDTO, result);
    }

    @Test
    public void linkStudent_whenUserNotFound_thenThrowsResourceNotFoundException() {
        when(userRepository.findByUserId("ABC")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.linkStudent("ABC", "123")
        );
        assertEquals(exception.getMessage(), "User Not Found");
    }
    @Test
    public void linkStudent_whenStudentNotFound_thenThrowsResourceNotFoundException() {
        when(userRepository.findByUserId("ABC")).thenReturn(Optional.of(new User()));
        when(studentRepository.findById("123")).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> userService.linkStudent("ABC", "123")
        );
        assertEquals(exception.getMessage(), "Student Not Found");
    }
}
