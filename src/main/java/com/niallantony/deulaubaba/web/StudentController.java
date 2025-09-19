package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private final StudentService studentService;
    private final UserRepository userRepository;

    @Autowired
    public StudentController(StudentService studentService, UserRepository userRepository) {
        this.studentService = studentService;
        this.userRepository = userRepository;
    }

    @GetMapping(path= "/preview")
    public ResponseEntity<StudentIdAvatar> getStudentPreview(
            @RequestParam String id,
            @CurrentUser String userId
    ) {
        userRepository.findByUserId(userId);
        if (userId == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(studentService.getStudentPreviewById(id));
    }

    @GetMapping
    public ResponseEntity<StudentDTO> getStudent(
            @RequestParam String id,
            @CurrentUser String userId
    ) {
        if (studentService.studentBelongsToUser(id, userId)) {
            return ResponseEntity.ok(studentService.getStudentById(id));
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping(path = "/team")
    public ResponseEntity<List<UserAvatar>> getStudentTeam(
            @RequestParam String id,
            @CurrentUser String userId
    ) {
        if (studentService.studentBelongsToUser(id, userId)) {
            return ResponseEntity.ok(studentService.getStudentTeam(id));
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<StudentIdAvatar>> getStudents(@CurrentUser String userId) {
        return ResponseEntity.ok(studentService.getStudents(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentDTO> createStudent(
            @RequestPart("data") String request,
            @CurrentUser String userId,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(studentService.createStudent(request, image, userId));
        }
        return ResponseEntity.ok(studentService.createStudent(request, userId));
    }

    @PutMapping(path = "/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudent(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(studentService.updateStudentDetails(studentId, request, image, userId));
        }
        return ResponseEntity.ok(studentService.updateStudentDetails(studentId, request, userId));
    }

    @PutMapping(path = "/{studentId}/communication")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudentCommunication(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestBody String request
            ) throws IOException {
        return ResponseEntity.ok(studentService.updateStudentCommunication(studentId, request, userId));
    }

    @PutMapping(path = "/{studentId}/challenge")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudentChallenges(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestBody String request
    ) throws IOException {
        return ResponseEntity.ok(studentService.updateStudentChallenge(studentId, request, userId));
    }
}
