package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.dto.user.UserRequest;

public class UserTestFactory {


    public static UserRequest createUserRequest() {
        UserRequest u = new UserRequest();
        u.setUsername("user1");
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
