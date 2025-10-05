package com.niallantony.deulaubaba.dto.student;

import lombok.Data;

@Data
public class StudentIdAvatar {
    private String studentId;
    private String name;
    private String imagesrc;

    public StudentIdAvatar(String studentId, String name, String imagesrc) {
        this.studentId = studentId;
        this.name = name;
        this.imagesrc = imagesrc;
    }
}
