package com.niallantony.deulaubaba.web;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private static final Logger log = LoggerFactory.getLogger(StudentController.class);
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudentController(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<StudentDTO> getStudent(@RequestParam String id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(new StudentDTO(
                student.getStudentId(),
                student.getName(),
                student.getSchool(),
                student.getAge(),
                student.getGrade(),
                student.getSetting(),
                student.getDisability(),
                student.getImagesrc(),
                student.getCommunicationDetails(),
                student.getChallengesDetails()
        ));
    }

    @GetMapping(path = "/team")
    public ResponseEntity<List<UserAvatar>> getStudentTeam(@RequestParam String id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }
        Set<User> users = student.getUsers();
        List<UserAvatar> userAvatars = users.stream()
                .map(user -> new UserAvatar(
                        user.getUsername(),
                        user.getImagesrc(),
                        user.getUserType()
                )).toList();
        return ResponseEntity.ok(userAvatars);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<StudentIdAvatar>> getStudents(@RequestParam String id) {
        log.info("getStudents called");
        List<StudentIdAvatar> students = studentRepository.findAllOfUserId(id);
        if (students.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(students);
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

    @PutMapping(path = "/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudent(@PathVariable String studentId, @RequestBody StudentEditRequest request) {
        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return ResponseEntity.notFound().build();
        }

        User user = userRepository.findById(request.getUid()).orElse(null);
        if (user == null || !student.getUsers().contains(user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        student.setName(request.getName());
        student.setSchool(request.getSchool());
        student.setAge(request.getAge());
        student.setGrade(request.getGrade());
        student.setDisability(request.getDisability());
        student.setSetting(request.getSetting());
        student.setCommunicationDetails(request.getCommunicationDetails());
        student.setChallengesDetails(request.getChallengesDetails());

        studentRepository.save(student);
        return ResponseEntity.ok(student);
    }
}
