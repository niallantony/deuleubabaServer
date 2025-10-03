package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.dto.StudentRequest;

public class StudentTestFactory {
    public static StudentRequest createStudentRequest(String uid) {
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setUid(uid);
        studentRequest.setName("John");
        studentRequest.setSchool("School");
        studentRequest.setAge(12);
        studentRequest.setGrade(3);
        studentRequest.setSetting("Setting");
        studentRequest.setDisability("Disability");
        studentRequest.setImgsrc("./example.png");
        return studentRequest;
    }

    public static StudentRequest createBadRequest(String uid) {
        StudentRequest studentRequest = new StudentRequest();
        studentRequest.setUid(uid);
        return studentRequest;
    }
}
