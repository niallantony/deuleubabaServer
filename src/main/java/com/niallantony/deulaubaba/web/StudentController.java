package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.services.StudentServices;
import jakarta.persistence.JoinTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final StudentServices studentServices;
    private final UserRepository userRepository;

    @Autowired
    public StudentController(StudentServices studentServices, UserRepository userRepository) {
        this.studentServices = studentServices;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<StudentDTO> getStudent(@RequestParam String id) {
        return ResponseEntity.ok(studentServices.getStudentById(id));
    }

    @GetMapping(path = "/team")
    public ResponseEntity<List<UserAvatar>> getStudentTeam(@RequestParam String id) {
        return ResponseEntity.ok(studentServices.getStudentTeam(id));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<StudentIdAvatar>> getStudents(@AuthenticationPrincipal Jwt principal) {
        String userId = (String) principal.getClaims().get("sub");
        log.info("Get all students from user id {}", userId);
        User user = userRepository.findByUserId(userId).orElseThrow();
        return ResponseEntity.ok(studentServices.getStudents(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentDTO> createStudent(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(studentServices.createStudent(request, image));
        }
        return ResponseEntity.ok(studentServices.createStudent(request));
    }

    @PutMapping(path = "/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudent(
            @PathVariable String studentId,
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(studentServices.updateStudent(studentId, request, image));
        }
        return ResponseEntity.ok(studentServices.updateStudent(studentId, request));
    }
}
