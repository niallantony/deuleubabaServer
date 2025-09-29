package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.UserRequest;

public class UserTestFactory {
    public static User createBasicUser(Role role) {
        User u = new User();
        u.setUserId("user");
        u.setImagesrc("./example.png");
        u.setUsername("username");
        u.setName("name");
        u.setEmail("email@gmail.com");
        u.setUserType("user type");
        u.setRole(role);
        return u;
    }

    public static UserRequest createUserRequest() {
        UserRequest u = new UserRequest();
        u.setUsername("username");
        u.setName("name");
        u.setEmail("email@gmail.com");
        u.setUserType("user type");
        return u;
    }

    public static UserRequest createBadUserRequest() {
        UserRequest u = new UserRequest();
        u.setUsername("username");
        return u;
    }
}
