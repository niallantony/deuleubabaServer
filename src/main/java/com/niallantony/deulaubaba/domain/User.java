package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="app_user", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "email"}))
public class User extends HasImage{

    @EqualsAndHashCode.Include
    @Id
    @Column(nullable = false, updatable = false)
    private String userId;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(unique = true, nullable = false)
    private String username;

    @NotNull
    @EqualsAndHashCode.Include
    private String name;

    @EqualsAndHashCode.Include
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
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "userId"),
            inverseJoinColumns = @JoinColumn(name = "student_id")
    )
    @JsonManagedReference
    private Set<Student> students = new HashSet<>();

    @Override
    public String getImage() {
        return imagesrc;
    }

    @Override
    public void setImage(String image) {
        imagesrc = image;
    }

    @Override
    public String getBucketId() {
        return username.toLowerCase();
    }
}
