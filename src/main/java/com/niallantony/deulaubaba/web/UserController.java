package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.UserServices;
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
    private final UserServices userServices;

    @Autowired
    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping
    public ResponseEntity<UserDTO> getProfile(@CurrentUser String userId) {
        return ResponseEntity.ok(userServices.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @CurrentUser String userId
    ) throws IOException {
        if (image != null) {
            return ResponseEntity.ok(userServices.createUser(userId, request, image));

        }
        return ResponseEntity.ok(userServices.createUser(userId, request));
    }

    @PostMapping("/link-student")
    public ResponseEntity<StudentDTO> linkStudent(
            @CurrentUser String userId,
            @RequestParam String code
    ) {
        return ResponseEntity.ok(userServices.linkStudent(userId, code));
    }
}
