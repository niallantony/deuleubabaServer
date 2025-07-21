package com.niallantony.deulaubaba.dto;

import lombok.Data;

@Data
public class StudentIdAvatar {
    private String id;
    private String name;
    private String imagesrc;

    public StudentIdAvatar(String studentId, String name, String imagesrc) {
        this.id = studentId;
        this.name = name;
        this.imagesrc = imagesrc;
    }
}
