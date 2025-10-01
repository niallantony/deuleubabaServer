package com.niallantony.deulaubaba.services;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.data.StudentRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.*;
import com.niallantony.deulaubaba.exceptions.FileStorageException;
import com.niallantony.deulaubaba.exceptions.InvalidStudentDataException;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.StudentMapper;
import com.niallantony.deulaubaba.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Random;
import java.util.Set;

@Slf4j
@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final StudentMapper studentMapper;
    private final JsonUtils jsonUtils;

    @Autowired
    public StudentService(
            StudentRepository studentRepository,
            UserRepository userRepository,
            FileStorageService fileStorageService,
            StudentMapper studentMapper,
            JsonUtils jsonUtils
    ) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.studentMapper = studentMapper;
        this.jsonUtils = jsonUtils;
    }

    public StudentIdAvatar getStudentPreviewById(String id)  {
        Student student = studentRepository.findById(id.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + id));
        return getStudentIdAvatar(student);
    }

    public StudentDTO getStudentById(String id)  {
        Student student = studentRepository.findById(id.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + id));
        return studentMapper.toDTO(student);
    }

    public List<UserAvatar> getStudentTeam(String id)  {

       Student student = studentRepository.findById(id.toLowerCase())
               .orElseThrow(() -> new ResourceNotFoundException("Student not found " + id));
       Set<User> users = student.getUsers();
        return users.stream()
               .map(user -> new UserAvatar(
                       user.getUsername(),
                       user.getImagesrc(),
                       user.getUserType()
               )).toList();
    }

    public List<StudentIdAvatar> getStudents(String id)  {
        List<StudentIdAvatar> students = studentRepository.findAllOfUserId(id);
        if (students.isEmpty()) {
            throw new ResourceNotFoundException("Students not found");
        }
        return students;
    }

    private Student extractStudent(StudentRequest studentRequest, User user) {

        char[] alphabet = {'1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        Random random = new Random();
        Student student = studentMapper.toStudent(studentRequest);
        student.setStudentId(NanoIdUtils.randomNanoId(random, alphabet, 6));
        student.getUsers().add(user);

        return student;
    }

    @Transactional
    public StudentDTO createStudent(String request, MultipartFile image, String userId)  {
        StudentRequest studentRequest = jsonUtils.parse(
                request,
                StudentRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        User user = userRepository.findByUserId(userId).orElseThrow(() -> new UserNotAuthorizedException("Unauthorized access"));
        validateStudentRequest(studentRequest);
        Student student = extractStudent(studentRequest, user);
        if (image != null && !image.isEmpty()) {
            try {
                String filename = fileStorageService.storeImage(image);
                student.setImagesrc(filename);
            } catch (FileStorageException e) {
                log.warn("Image not saved to storage", e);
            }
        }
        studentRepository.save(student);
        linkStudentToUser(student, userId);

        return studentMapper.toDTO(student);
    }

    // TODO: Image storage failure should be told to user
    // TODO: Image deletion is not ATOM
    @Transactional
    public StudentDTO updateStudentDetails(String studentId, String request, MultipartFile image, String userId)  {
        StudentRequest studentRequest = jsonUtils.parse(
                request,
                StudentRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        Student student = getAuthorisedStudent(studentId, userId);
        String oldImg = null;
        boolean newImageStored = false;
        if (image != null && !image.isEmpty()) {
            oldImg = student.getImagesrc();
            try {
                String filename = fileStorageService.storeImage(image);
                student.setImagesrc(filename);
                newImageStored = true;
            } catch (FileStorageException e) {
                log.warn("Image not saved to storage", e);
            }
        }
        applyDetailUpdates(student, studentRequest);
        studentRepository.save(student);
        if (newImageStored && oldImg != null) {
            try {
                fileStorageService.deleteImage(oldImg);
            } catch (FileStorageException e) {
                log.warn("Couldn't delete old image from storage", e);
            }
        }
        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentCommunication(String studentId, String request, String userId)  {
        StudentCommunicationRequest studentCommunicationRequest = jsonUtils.parse(
                request,
                StudentCommunicationRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        Student student = getAuthorisedStudent(studentId, userId);
        student.setCommunicationDetails(studentCommunicationRequest.getCommunicationDetails());
        studentRepository.save(student);
        return studentMapper.toDTO(student);
    }

    @Transactional
    public StudentDTO updateStudentChallenge(String studentId, String request, String userId)  {
        StudentChallengeRequest studentChallengeRequest = jsonUtils.parse(
                request,
                StudentChallengeRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        Student student = getAuthorisedStudent(studentId, userId);
        student.setChallengesDetails(studentChallengeRequest.getChallengesDetails());
        studentRepository.save(student);
        return studentMapper.toDTO(student);
    }

    public Student getAuthorisedStudent(String studentId, String userId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + studentId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotAuthorizedException("User not found " + userId));
        if (!student.getUsers().contains(user)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        return student;
    }
    
    private void applyDetailUpdates(Student student, StudentRequest studentRequest) {
        validateStudentRequest(studentRequest);
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
        Student student = studentRepository.findById(studentId.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found " + studentId));
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new UserNotAuthorizedException("User not found " + userId));
        return student.getUsers().contains(user);
    }

    private void linkStudentToUser(Student student, String userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User Not Found"));
        user.getStudents().add(student);
        student.getUsers().add(user);
        userRepository.save(user);
    }

    private void validateStudentRequest(StudentRequest studentRequest) {
        if (
                studentRequest.getName() == null
                || studentRequest.getSchool() == null
                || studentRequest.getAge() == null
                || studentRequest.getGrade() == null
                || studentRequest.getDisability() == null
                || studentRequest.getSetting() == null
        ) {
            throw new InvalidStudentDataException("Invalid student data");
        }

    }

}
