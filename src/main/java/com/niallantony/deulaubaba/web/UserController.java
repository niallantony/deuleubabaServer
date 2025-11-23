package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.student.StudentDTO;
import com.niallantony.deulaubaba.dto.user.UserDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;


@RestController
@Slf4j
@RequestMapping(path="/me", produces = "application/json")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getProfile(@CurrentUser String userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @CurrentUser String userId
    )  {
            userService.createUser(userId, request, image);
            return ResponseEntity.created(
                    URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/me").toUriString())
            ).build();
    }

    @PostMapping("/link-student")
    public ResponseEntity<StudentDTO> linkStudent(
            @CurrentUser String userId,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(userService.linkStudent(userId, code));
    }
}
