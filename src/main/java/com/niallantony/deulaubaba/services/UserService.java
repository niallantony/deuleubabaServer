package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.RoleRepository;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserService {
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final FileStorageService fileStorageService;
    private final StudentService studentService;
    private final UserMapper userMapper;
    private final StudentMapper studentMapper;


    @Autowired
    public UserService(
            StudentRepository studentRepository,
            RoleRepository roleRepository,
            UserRepository userRepository,
            ObjectMapper jacksonObjectMapper,
            FileStorageService fileStorageService,
            StudentService studentService,
            UserMapper userMapper,
            StudentMapper studentMapper
    ) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
        this.studentService = studentService;
        this.userMapper = userMapper;
        this.studentMapper = studentMapper;
    }

    public UserDTO getUser(String id) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return userMapper.toDTO(user);

    }

    public User createUser(String userId, String data) throws ResourceNotFoundException, JsonProcessingException {
        UserRequest userRequest = jacksonObjectMapper.readValue(data, UserRequest.class);
        User user = newUser(userId, userRequest);
        userRepository.save(user);
        return user;
    }

    public User createUser(String userId,  String data, MultipartFile image) throws ResourceNotFoundException, IOException {
        UserRequest userRequest = jacksonObjectMapper.readValue(data, UserRequest.class);
        String filename = fileStorageService.storeImage(image);

        User user = newUser(userId, userRequest);
        user.setImagesrc(filename);
        userRepository.save(user);

        return user;
    }

    public StudentDTO linkStudent(String userId, String studentCode) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        Student student = studentRepository.findById(studentCode)
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
        return studentMapper.toDTO(student);
    }

    private User newUser (String userId, UserRequest userRequest) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("User Role Not Found"));
        User user = new User();
        user.setName(userRequest.getName());
        user.setUsername(userRequest.getUsername());
        user.setUserType(userRequest.getUserType());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRole);
        user.setUserId(userId);
        return user;
    }
}
