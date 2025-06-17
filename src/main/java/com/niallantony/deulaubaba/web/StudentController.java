package com.niallantony.deulaubaba.web;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public Student getStudent(String id) {
        return studentRepository.findById(id).orElse(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createStudent(@RequestBody StudentRequest request) {
        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();
        User user = userRepository.findById(request.getUid()).orElse(null);
        if (user == null) {
           return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Student student = new Student();
        student.setName(request.getName());
        student.setSchool(request.getSchool());
        student.setAge(request.getAge());
        student.setGrade(request.getGrade());
        student.setDisability(request.getDisability());
        student.setSetting(request.getSetting());
        student.setStudentId(NanoIdUtils.randomNanoId(random, alphabet, 6));
        student.getUsers().add(user);

        studentRepository.save(student);
        return ResponseEntity.ok(student);
    }

}
