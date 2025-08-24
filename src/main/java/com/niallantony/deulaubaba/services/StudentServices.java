package com.niallantony.deulaubaba.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
public class StudentServices {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper jacksonObjectMapper;

    @Autowired
    public StudentServices(StudentRepository studentRepository, UserRepository userRepository, ObjectMapper jacksonObjectMapper, FileStorageService fileStorageService) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
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

    public Student extractStudent(StudentRequest studentRequest) {

        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();
        User user = userRepository.findById(studentRequest.getUid()).orElseThrow(() -> new UserNotAuthorizedException("Unauthorized Access"));

        Student student = new Student();
        student.setName(studentRequest.getName());
        student.setSchool(studentRequest.getSchool());
        student.setAge(studentRequest.getAge());
        student.setGrade(studentRequest.getGrade());
        student.setDisability(studentRequest.getDisability());
        student.setSetting(studentRequest.getSetting());
        student.setStudentId(NanoIdUtils.randomNanoId(random, alphabet, 6));
        student.getUsers().add(user);

        return student;
    }

    public StudentDTO createStudent(String request) throws UserNotAuthorizedException, JsonProcessingException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        Student student = extractStudent(studentRequest);


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

    public StudentDTO createStudent(String request, MultipartFile image) throws UserNotAuthorizedException, IOException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        String filename = fileStorageService.storeImage(image);

        Student student = extractStudent(studentRequest);
        student.setImagesrc(filename);

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

    @Transactional
    public StudentDTO updateStudent(String studentId, String request) throws UserNotAuthorizedException, ResourceNotFoundException, JsonProcessingException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
       Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found" + studentId));
       User user = userRepository.findById(studentRequest.getUid()).orElseThrow(() -> new UserNotAuthorizedException("User not found" + studentRequest.getUid()));
       if (!student.getUsers().contains(user)) {
           throw new UserNotAuthorizedException("Unauthorized Access");
       }

       student.setName(studentRequest.getName());
       student.setSchool(studentRequest.getSchool());
       student.setAge(studentRequest.getAge());
       student.setGrade(studentRequest.getGrade());
       student.setDisability(studentRequest.getDisability());
       student.setSetting(studentRequest.getSetting());
       student.setCommunicationDetails(studentRequest.getCommunicationDetails());
       student.setChallengesDetails(studentRequest.getChallengesDetails());

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

    @Transactional
    public StudentDTO updateStudent(String studentId, String request, MultipartFile image) throws UserNotAuthorizedException, ResourceNotFoundException, IOException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        Student student = studentRepository.findById(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found" + studentId));
        User user = userRepository.findById(studentRequest.getUid()).orElseThrow(() -> new UserNotAuthorizedException("User not found" + studentRequest.getUid()));
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized Access");
        }
        fileStorageService.deleteImage(student);

        String filename = fileStorageService.storeImage(image);

        student.setImagesrc(filename);
        student.setName(studentRequest.getName());
        student.setSchool(studentRequest.getSchool());
        student.setAge(studentRequest.getAge());
        student.setGrade(studentRequest.getGrade());
        student.setDisability(studentRequest.getDisability());
        student.setSetting(studentRequest.getSetting());
        student.setCommunicationDetails(studentRequest.getCommunicationDetails());
        student.setChallengesDetails(studentRequest.getChallengesDetails());

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
