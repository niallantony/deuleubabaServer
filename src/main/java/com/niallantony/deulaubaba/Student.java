package com.niallantony.deulaubaba;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Student {
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
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<DictionaryEntry> dictionaries = new HashSet<>();
}
