package com.niallantony.deulaubaba.dev;


import com.niallantony.deulaubaba.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class FirebaseAuthenticationToken extends AbstractAuthenticationToken {

    private final User principal;

    public FirebaseAuthenticationToken(User principal, String credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true); // Mark as authenticated since this is mock
    }

    @Override
    public Object getCredentials() {
        return null; // No password/JWT stored here
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
