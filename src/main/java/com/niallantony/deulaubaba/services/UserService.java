package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.InvalidUserDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.mapper.StudentMapper;
import com.niallantony.deulaubaba.mapper.UserMapper;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final JsonUtils jsonUtils;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;


    @Autowired
    public UserService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            UserMapper userMapper,
            StudentMapper studentMapper,
            JsonUtils jsonUtils
    ) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.jsonUtils = jsonUtils;
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
    }

    public UserDTO getUser(String userId) {
        User user = getUserOrThrow(userId);
        return userMapper.toDTO(user);
    }


    public UserDTO createUser(String userId,  String data, MultipartFile image) {
        UserRequest userRequest = jsonUtils.parse(
                data,
                UserRequest.class,
                () -> new InvalidUserDataException("Given User Data not Valid")
        );
        validateUserRequest(userRequest);
        User user = userMapper.toNewUser(userRequest, userId);
        if (image != null && !image.isEmpty()) {
            try {
                String filename = fileStorageService.storeImage(image);
                user.setImagesrc(filename);
            } catch (FileStorageException e) {
                log.warn("Image storage failed", e);
            }
        }
        userRepository.save(user);

        return userMapper.toDTO(user);
    }
    private void validateUserRequest(UserRequest request) {
        if (request.getName() == null || request.getName().isBlank() ||
                request.getUsername() == null || request.getUsername().isBlank() ||
                request.getEmail() == null || request.getEmail().isBlank() ||
                request.getUserType() == null || request.getUserType().isBlank()) {
            throw new InvalidUserDataException( "Missing required user fields" );
        }
    }

    @Transactional
    public StudentDTO linkStudent(String userId, String studentCode) {
        User user = getUserOrThrow(userId);
        Student student = studentRepository.findById(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
        return studentMapper.toDTO(student);
    }

    private User getUserOrThrow(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }
}
