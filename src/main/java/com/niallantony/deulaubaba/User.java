package com.niallantony.deulaubaba;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="app_user", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "email"}))
public class User {

    @EqualsAndHashCode.Include
    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(unique = true, nullable = false)
    private String username;

    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String userType;

    private String imagesrc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", referencedColumnName = "id")
    private Role role;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name="user_students",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonManagedReference
    private Set<Student> students = new HashSet<>();

    public Collection<? extends GrantedAuthority> authorities() {
        return Collections.singleton(new SimpleGrantedAuthority(role.getName()));
    }

}
