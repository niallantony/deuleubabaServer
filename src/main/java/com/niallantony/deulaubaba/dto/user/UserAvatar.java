package com.niallantony.deulaubaba.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAvatar {
    private String username;
    private String imagesrc;
    private String userType;
}
