package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target="user.userId", ignore = true)
    @Mapping(target="user.students", ignore = true)
    UserDTO toDTO(User user);
}
