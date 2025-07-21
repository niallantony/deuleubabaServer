package com.niallantony.deulaubaba.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.niallantony.deulaubaba.Student;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
public class StudentServices {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Autowired
    public StudentServices(StudentRepository studentRepository, UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public StudentDTO getStudentById(String id) throws ResourceNotFoundException {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found" + id));
        return new StudentDTO(
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
        );
    }

    public List<UserAvatar> getStudentTeam(String id) throws ResourceNotFoundException {
       Student student = studentRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("Student not found" + id));
       Set<User> users = student.getUsers();
        return users.stream()
               .map(user -> new UserAvatar(
                       user.getUsername(),
                       user.getImagesrc(),
                       user.getUserType()
               )).toList();
    }

    public List<StudentIdAvatar> getStudents(String id) throws ResourceNotFoundException {
        List<StudentIdAvatar> students = studentRepository.findAllOfUserId(id);
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("Students not found" + id);
        }
        return students;
    }

    public StudentDTO createStudent(StudentRequest request) throws UserNotAuthorizedException {
        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();
        User user = userRepository.findById(request.getUid()).orElseThrow(() -> new UserNotAuthorizedException("Unauthorized Access"));

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
        return new StudentDTO(
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
        );
    }

    public StudentDTO updateStudent(String studentId, StudentEditRequest request) throws UserNotAuthorizedException , ResourceNotFoundException{
       Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found" + studentId));
       User user = userRepository.findById(request.getUid()).orElseThrow(() -> new UserNotAuthorizedException("User not found" + request.getUid()));
       if (!student.getUsers().contains(user)) {
           throw new UserNotAuthorizedException("Unauthorized Access");
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

       return new StudentDTO(
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
       );
    }

}
