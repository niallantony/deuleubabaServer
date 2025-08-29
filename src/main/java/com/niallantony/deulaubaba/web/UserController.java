package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.StudentCodeRequest;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.services.UserServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    public ResponseEntity<UserDTO> getProfile(@AuthenticationPrincipal Jwt principal) {

        String userId = (String) principal.getClaims().get("sub");
        return ResponseEntity.ok(userServices.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<User> createUser(
            @RequestPart("data") String request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal Jwt principal
    ) throws IOException {
        String userId = (String) principal.getClaims().get("sub");
        if (image != null) {
            return ResponseEntity.ok(userServices.createUser(userId, request, image));

        }
        return ResponseEntity.ok(userServices.createUser(userId, request));
    }

    @PostMapping("/link-student")
    public ResponseEntity<?> linkStudent(@RequestBody StudentCodeRequest request) {
        return ResponseEntity.ok(userServices.linkStudent(request));
    }
}
