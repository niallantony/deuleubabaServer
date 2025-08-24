package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.StudentCodeRequest;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(path="/user", produces = "application/json")
public class UserController {
    private final UserServices userServices;

    @Autowired
    public UserController(UserServices userServices) {
        this.userServices = userServices;
    }

    @GetMapping
    public ResponseEntity<User> getUser(String id) {
        return ResponseEntity.ok(userServices.getUser(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserRequest request) {
        return ResponseEntity.ok(userServices.createUser(request));
    }

    @PostMapping("/link-student")
    public ResponseEntity<?> linkStudent(@RequestBody StudentCodeRequest request) {
        return ResponseEntity.ok(userServices.linkStudent(request));
    }
}
