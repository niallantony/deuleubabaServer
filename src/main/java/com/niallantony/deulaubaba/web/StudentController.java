package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.services.StudentServices;
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
    private final StudentServices studentServices;

    @Autowired
    public StudentController(StudentServices studentServices) {
        this.studentServices = studentServices;
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
    public ResponseEntity<List<StudentIdAvatar>> getStudents(@RequestParam String id) {
        return ResponseEntity.ok(studentServices.getStudents(id));
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
