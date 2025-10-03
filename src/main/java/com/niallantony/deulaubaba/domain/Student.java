package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Student  {
    @EqualsAndHashCode.Include
    @Id
    @Column(name="student_id")
    private String studentId;

    @EqualsAndHashCode.Include
    @NotNull
    @Column(nullable = false)
    private String name;

    @NotNull
    private String school;

    @NotNull
    private Integer age;

    @NotNull
    private Integer grade;

    @NotNull
    private String setting;

    @NotNull
    private String disability;

    private String imagesrc;

    private String communicationDetails;

    private String challengesDetails;

    @ManyToMany(mappedBy = "students")
    @JsonBackReference
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "student")
    @JsonBackReference
    private Set<DictionaryEntry> dictionaries = new HashSet<>();

    @OneToMany(mappedBy = "student")
    @JsonBackReference
    private Set<Project> projects = new HashSet<>();
}
