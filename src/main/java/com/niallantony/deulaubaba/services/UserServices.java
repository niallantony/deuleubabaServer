package com.niallantony.deulaubaba.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dev.MockFirebaseUser;
import com.niallantony.deulaubaba.dto.StudentCodeRequest;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UserServices {
    private static final Logger log = LoggerFactory.getLogger(UserServices.class);
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ObjectMapper jacksonObjectMapper;
    private final FileStorageService fileStorageService;


    @Autowired
    public UserServices(StudentRepository studentRepository, RoleRepository roleRepository, UserRepository userRepository, ObjectMapper jacksonObjectMapper, FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
    }

    public UserDTO getUser(String id) {
        User user = userRepository.findByUserId(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        return new UserDTO(
                user.getName(),
                user.getEmail(),
                user.getUserType(),
                user.getImagesrc(),
                user.getRole()
        );

    }

    public User createUser(String userId, String data) throws ResourceNotFoundException, JsonProcessingException {
        log.info(data);
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

    public String linkStudent(StudentCodeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MockFirebaseUser firebaseUser = (MockFirebaseUser) authentication.getPrincipal();

        // Dev Only - Remove Later
        User user = userRepository.findById(firebaseUser.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        Student student = studentRepository.findById(request.getStudentCode())
                .orElseThrow(() -> new ResourceNotFoundException("Student Not Found"));
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
        return "Student Linked" + student.getStudentId();
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
