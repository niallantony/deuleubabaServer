package com.niallantony.deulaubaba.dev;

import com.niallantony.deulaubaba.Role;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.RoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("dev")
public class MockFirebaseAuthFilter extends OncePerRequestFilter {
    private final RoleRepository roleRepository;

    public MockFirebaseAuthFilter(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            Long fakeUid = 12345L;
            String fakeEmail = "mock@example.com";
            String fakeUsername = "mock-user";
            String fakeUserType = "교사";

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("User Role not found"));

            User user = new User();
            user.setId(fakeUid);
            user.setEmail(fakeEmail);
            user.setUsername(fakeUsername);
            user.setUserType(fakeUserType);
            user.getRoles().add(userRole);

            FirebaseAuthenticationToken authenticationToken = new FirebaseAuthenticationToken(user, null, user.authorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

}
