package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.data.RoleRepository;
import com.niallantony.deulaubaba.domain.Role;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.user.UserDTO;
import com.niallantony.deulaubaba.dto.user.UserRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static  org.mockito.Mockito.*;
import java.lang.reflect.Field;
import java.util.Optional;

public class UserMapperTests {

    UserMapper userMapper = new UserMapperImpl();

    RoleRepository roleRepository = mock(RoleRepository.class);




    @Test
    public void givenUser_whenUserToDTO_thenReturnUserDTO() {
        User user = new User();
        Role role = new Role("ADMIN");
        user.setName("John");
        user.setEmail("john@gmail.com");
        user.setUsername("JohnUser");
        user.setUserType("Teacher");
        user.setRole(role);
        user.setImagesrc("./example.png");

        UserDTO dto = userMapper.toDTO(user);

        assertEquals("John", dto.getName());
        assertEquals("john@gmail.com", dto.getEmail());
        assertEquals("JohnUser", dto.getUsername());
        assertEquals("Teacher", dto.getUserType());
        assertEquals("./example.png", dto.getImagesrc());
        assertEquals(role, dto.getRole());
    }

    @Test
    public void givenUserRequest_whenRequestToNewUser_thenReturnNewUser() throws NoSuchFieldException, IllegalAccessException {

        // Make protected repository visible in order to mock
        Field field = UserMapper.class.getDeclaredField("roleRepository");
        field.setAccessible(true);
        field.set(userMapper, roleRepository);

        Role role = new Role("USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        UserRequest request = new UserRequest();
        request.setName("John");
        request.setEmail("john@gmail.com");
        request.setUsername("JohnUser");
        request.setUserType("Teacher");

        User user = userMapper.toNewUser(request, "ABC");
        assertEquals("John", user.getName());
        assertEquals("john@gmail.com", user.getEmail());
        assertEquals("JohnUser", user.getUsername());
        assertEquals("Teacher", user.getUserType());
        assertEquals("ABC", user.getUserId());
        assertEquals(role, user.getRole());
    }
}
