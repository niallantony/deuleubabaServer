package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.UserDTO;
import com.niallantony.deulaubaba.dto.UserRequest;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected RoleRepository roleRepository;

    @Mapping(target="user.userId", ignore = true)
    @Mapping(target="user.students", ignore = true)
    public abstract UserDTO toDTO(User user);

    @Mapping(target = "students", ignore = true)
    @Mapping(target = "imagesrc", ignore = true)
    @Mapping(target = "role", ignore = true)
    public abstract User toUser(UserRequest userRequest, String userId);

    @AfterMapping
    protected void setUserRole(@MappingTarget User user) {
        Role role = roleRepository.findByName("ROLE_USER").orElseThrow(
                () -> new ResourceNotFoundException("Role does not exist")
        );
        user.setRole(role);
    }

}
