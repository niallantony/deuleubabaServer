package com.niallantony.deulaubaba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserAvatar {
    private String username;
    private String imagesrc;
    private String userType;
}
