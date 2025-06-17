package com.niallantony.deulaubaba.dev;

import com.niallantony.deulaubaba.Role;
import com.niallantony.deulaubaba.User;
import com.niallantony.deulaubaba.data.RoleRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Profile("dev")
public class MockFirebaseAuthFilter extends OncePerRequestFilter {

    public MockFirebaseAuthFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
           String fakeUid = "1";

            MockFirebaseUser user = new MockFirebaseUser();
            user.setUid(fakeUid);

            FirebaseAuthenticationToken authenticationToken = new FirebaseAuthenticationToken(user, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

}
