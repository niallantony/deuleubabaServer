package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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
    ) throws IOException {
            return ResponseEntity.ok(userService.createUser(userId, request, image));
    }

    @PostMapping("/link-student")
    public ResponseEntity<StudentDTO> linkStudent(
            @CurrentUser String userId,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(userService.linkStudent(userId, code));
    }
}
