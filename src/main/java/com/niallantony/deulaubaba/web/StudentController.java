package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.dto.student.*;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import com.niallantony.deulaubaba.exceptions.InvalidStudentDataException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.StudentService;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/student", produces = "application/json")
public class StudentController {
    private final StudentService studentService;
    private final UserRepository userRepository;
    private final JsonUtils jsonUtils;

    @Autowired
    public StudentController(StudentService studentService, UserRepository userRepository, JsonUtils jsonUtils) {
        this.studentService = studentService;
        this.userRepository = userRepository;
        this.jsonUtils = jsonUtils;
    }

    @GetMapping(path= "/preview")
    public ResponseEntity<StudentIdAvatar> getStudentPreview(
            @RequestParam String id,
            @CurrentUser String userId
    ) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotAuthorizedException("Unauthorized access");
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
        throw new UserNotAuthorizedException("Unauthorized access");
    }

    @GetMapping(path = "/team")
    public ResponseEntity<List<UserAvatar>> getStudentTeam(
            @RequestParam String id,
            @CurrentUser String userId
    ) {
        if (studentService.studentBelongsToUser(id, userId)) {
            return ResponseEntity.ok(studentService.getStudentTeam(id));
        }
        throw new UserNotAuthorizedException("Unauthorized access");
    }

    @GetMapping(path = "/all")
    public ResponseEntity<List<StudentIdAvatar>> getStudents(@CurrentUser String userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotAuthorizedException("Unauthorized access");
        }
        return ResponseEntity.ok(studentService.getStudents(userId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentDTO> createStudent(
            @RequestPart("data") String request,
            @CurrentUser String userId,
            @RequestPart(value = "image", required = false) MultipartFile image)  {
        StudentRequest studentRequest = jsonUtils.parse(
                request,
                StudentRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        return ResponseEntity.ok(studentService.createStudent(studentRequest, image, userId));
    }

    @PutMapping(path = "/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<StudentDTO> updateStudent(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image)  {
        StudentRequest studentRequest = jsonUtils.parse(
                request,
                StudentRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        studentService.updateStudentDetails(studentId, studentRequest, image, userId);
        return ResponseEntity.noContent()
                             .location(
                                     URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/student").toUriString() + "?id=" + studentId)
                             )
                             .build();
    }

    @PutMapping(path = "/{studentId}/communication")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudentCommunication(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestBody String request
            )  {
        StudentCommunicationRequest studentCommunicationRequest = jsonUtils.parse(
                request,
                StudentCommunicationRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        studentService.updateStudentCommunication(studentId, studentCommunicationRequest, userId);
        return ResponseEntity.noContent()
                             .location(
                                     URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/student").toUriString() + "?id=" + studentId)
                             )
                             .build();
    }

    @PutMapping(path = "/{studentId}/challenge")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> updateStudentChallenges(
            @PathVariable String studentId,
            @CurrentUser String userId,
            @RequestBody String request
    )  {
        StudentChallengeRequest studentChallengeRequest = jsonUtils.parse(
                request,
                StudentChallengeRequest.class,
                () -> new InvalidStudentDataException("Invalid request")
        );
        studentService.updateStudentChallenge(studentId, studentChallengeRequest, userId);
        return ResponseEntity.noContent()
                             .location(
                                     URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/student").toUriString() + "?id=" + studentId)
                             )
                             .build();
    }
}
