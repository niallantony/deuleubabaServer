package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dev.MockFirebaseUser;
import com.niallantony.deulaubaba.dto.StudentCodeRequest;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServices {
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;


    @Autowired
    public UserServices(StudentRepository studentRepository, RoleRepository roleRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public User getUser(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
    }

    public User createUser(UserRequest userRequest) throws ResourceNotFoundException{
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("User Role Not Found"));

        User user = new User();
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        user.setRole(userRole);
        user.setEmail(userRequest.getEmail());
        user.setUsername(userRequest.getUsername());
        user.setUserType(userRequest.getUserType());
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
}
