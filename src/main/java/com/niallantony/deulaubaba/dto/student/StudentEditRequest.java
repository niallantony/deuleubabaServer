package com.niallantony.deulaubaba.dto.student;

import lombok.Data;

@Data
public class StudentEditRequest {
    private String uid;
    private String name;
    private String school;
    private Integer age;
    private Integer grade;
    private String setting;
    private String disability;
    private String communicationDetails;
    private String challengesDetails;
}
