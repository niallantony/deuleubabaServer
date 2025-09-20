package com.niallantony.deulaubaba.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDTO {
    private String studentId;
    private String name;
    private String school;
    private Integer age;
    private Integer grade;
    private String setting;
    private String disability;
    private String imagesrc;
    private String communicationDetails;
    private String challengesDetails;
}
