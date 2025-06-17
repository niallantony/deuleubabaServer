package com.niallantony.deulaubaba.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String id;
    private String username;
    private String email;
    private String name;
    private String userType;
}
