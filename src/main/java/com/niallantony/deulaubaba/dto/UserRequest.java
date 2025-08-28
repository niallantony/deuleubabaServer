package com.niallantony.deulaubaba.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String name;
    private String userType;
    private String email;
}
