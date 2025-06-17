package com.niallantony.deulaubaba.web;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.dto.StudentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public Student getStudent(String id) {
        return studentRepository.findById(id).orElse(null);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student createStudent(@RequestBody StudentRequest request) {
        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();

        Student student = new Student();
        student.setName(request.getName());
        student.setSchool(request.getSchool());
        student.setAge(request.getAge());
        student.setGrade(request.getGrade());
        student.setDisability(request.getDisability());
        student.setSetting(request.getSetting());
        student.setStudentId(NanoIdUtils.randomNanoId(random, alphabet, 6));

        return studentRepository.save(student);
    }

}
