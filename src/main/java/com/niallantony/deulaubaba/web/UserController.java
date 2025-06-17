package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.Role;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dev.MockFirebaseUser;
import com.niallantony.deulaubaba.dto.StudentCodeRequest;
import com.niallantony.deulaubaba.dto.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path="/user", produces = "application/json")
public class UserController {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public UserController(UserRepository userRepository, RoleRepository roleRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public User getUser(String id) {
        return userRepository.findById(id).orElse(null);
    }

    @PostMapping
    public User createUser(@RequestBody UserRequest request) {
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("User Role Not Found"));
        User user = new User();
        user.setId(request.getId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setUserType(request.getUserType());
        user.setRole(userRole);
        return userRepository.save(user);
    }

    @PostMapping("/link-student")
    public ResponseEntity<?> linkStudent(@RequestBody StudentCodeRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        MockFirebaseUser firebaseUser = (MockFirebaseUser) authentication.getPrincipal();

        User user = userRepository.findById(firebaseUser.getUid()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Student student = studentRepository.findById(request.getStudentCode()).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
        return ResponseEntity.ok("Student linked");
    }
}
