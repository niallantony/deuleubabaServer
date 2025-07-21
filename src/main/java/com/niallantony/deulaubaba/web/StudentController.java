package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.services.StudentServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<StudentDTO> createStudent(@RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentServices.createStudent(request));
    }

    @PutMapping(path = "/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudent(@PathVariable String studentId, @RequestBody StudentEditRequest request) {
        return ResponseEntity.ok(studentServices.updateStudent(studentId, request));
    }
}
