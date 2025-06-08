package com.niallantony.deulaubaba;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
public class Student {
    @Id
    @Column(name="student_id")
    private String studentId;

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

    private String chanllengesDetails;




}
