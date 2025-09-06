package com.niallantony.deulaubaba.dto;

import lombok.Data;

@Data
public class StudentRequest {
    private String uid;
    private String name;
    private String school;
    private Integer age;
    private Integer grade;
    private String setting;
    private String disability;
    private String imgsrc;
}
