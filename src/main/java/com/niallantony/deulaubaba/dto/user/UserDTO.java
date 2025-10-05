package com.niallantony.deulaubaba.dto.user;

import com.niallantony.deulaubaba.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String name;
    private String email;
    private String userType;
    private String imagesrc;
    private String username;
    private Role role;
}
