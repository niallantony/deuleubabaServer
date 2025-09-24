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
import com.niallantony.deulaubaba.mapper.StudentMapper;
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
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper jacksonObjectMapper;
    private final StudentMapper studentMapper;

    @Autowired
    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            ObjectMapper jacksonObjectMapper,
            FileStorageService fileStorageService,
            StudentMapper studentMapper
    ) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.jacksonObjectMapper = jacksonObjectMapper;
        this.fileStorageService = fileStorageService;
        this.studentMapper = studentMapper;
    }

    public StudentIdAvatar getStudentPreviewById(String id) throws ResourceNotFoundException {
        Student student = studentRepository.findById(id.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found" + id));
        return getStudentIdAvatar(student);
    }

    public StudentDTO getStudentById(String id) throws ResourceNotFoundException {
        Student student = studentRepository.findById(id.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found" + id));
        return studentMapper.toDTO(student);
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

    public Student extractStudent(StudentRequest studentRequest, String userId) {

        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserNotAuthorizedException("Unauthorized Access"));

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

    @Transactional
    public StudentDTO createStudent(String request, String userId) throws UserNotAuthorizedException, JsonProcessingException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        Student student = extractStudent(studentRequest, userId);
        studentRepository.save(student);
        linkStudentToUser(student, userId);
        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO createStudent(String request, MultipartFile image, String userId) throws UserNotAuthorizedException, IOException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        String filename = fileStorageService.storeImage(image);

        Student student = extractStudent(studentRequest, userId);
        student.setImagesrc(filename);
        studentRepository.save(student);
        linkStudentToUser(student, userId);

        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentDetails(String studentId, String request, String userId) throws UserNotAuthorizedException, ResourceNotFoundException, JsonProcessingException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        Student student = getAuthorisedStudent(studentId, userId);

       applyDetailUpdates(student, studentRequest);

       studentRepository.save(student);
       return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentDetails(String studentId, String request, MultipartFile image, String userId) throws UserNotAuthorizedException, ResourceNotFoundException, IOException {
        StudentRequest studentRequest = jacksonObjectMapper.readValue(request, StudentRequest.class);
        Student student = getAuthorisedStudent(studentId, userId);
        String oldImg = student.getImagesrc();

        String filename = fileStorageService.storeImage(image);

        student.setImagesrc(filename);
        applyDetailUpdates(student, studentRequest);

        studentRepository.save(student);
        fileStorageService.deleteImage(oldImg);

        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentCommunication(String studentId, String request, String userId) throws UserNotAuthorizedException, ResourceNotFoundException, IOException {
        StudentCommunicationRequest studentCommunicationRequest = jacksonObjectMapper.readValue(request, StudentCommunicationRequest.class);
        Student student = getAuthorisedStudent(studentId, userId);
        student.setCommunicationDetails(studentCommunicationRequest.getCommunicationDetails());
        studentRepository.save(student);
        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentChallenge(String studentId, String request, String userId) throws UserNotAuthorizedException, ResourceNotFoundException, IOException {
        StudentChallengeRequest studentChallengeRequest = jacksonObjectMapper.readValue(request, StudentChallengeRequest.class);
        Student student = getAuthorisedStudent(studentId, userId);
        student.setChallengesDetails(studentChallengeRequest.getChallengesDetails());
        studentRepository.save(student);
        return studentMapper.toDTO(student);
    }

    public Student getAuthorisedStudent(String studentId, String userId)
            throws UserNotAuthorizedException, ResourceNotFoundException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + studentId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotAuthorizedException("User not found " + userId));
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized Access");
        }
        return student;
    }
    
    private void applyDetailUpdates(Student student, StudentRequest studentRequest) {
        student.setName(studentRequest.getName());
        student.setSchool(studentRequest.getSchool());
        student.setAge(studentRequest.getAge());
        student.setGrade(studentRequest.getGrade());
        student.setDisability(studentRequest.getDisability());
        student.setSetting(studentRequest.getSetting());
    }
    
    private StudentIdAvatar getStudentIdAvatar(Student student) {
        return new StudentIdAvatar(
                student.getStudentId(),
                student.getName(),
                student.getImagesrc()
        );
    }

    public boolean studentBelongsToUser(String studentId, String userId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + studentId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found " + userId));
        return student.getUsers().contains(user);
    }

    private void linkStudentToUser(Student student, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
    }

}
