package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.mapper.StudentMapper;
import com.niallantony.deulaubaba.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final FileStorageService fileStorageService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;


    @Autowired
    public UserService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            ObjectMapper jacksonObjectMapper,
            FileStorageService fileStorageService,
            UserMapper userMapper,
            StudentMapper studentMapper
    ) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
    }

    public UserDTO getUser(String userId) {
        User user = getUserOrThrow(userId);
        return userMapper.toDTO(user);

    }


    public UserDTO createUser(String userId,  String data, MultipartFile image) throws ResourceNotFoundException, IOException {
        UserRequest userRequest = jacksonObjectMapper.readValue(data, UserRequest.class);
        User user = userMapper.toUser(userRequest, userId);
        if (image != null && !image.isEmpty()) {
            String filename = fileStorageService.storeImage(image);
            user.setImagesrc(filename);
        }
        userRepository.save(user);

        return userMapper.toDTO(user);
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
