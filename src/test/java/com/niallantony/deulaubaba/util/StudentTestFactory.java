package com.niallantony.deulaubaba.util;

import com.niallantony.deulaubaba.domain.Student;

public class StudentTestFactory {
    public static Student createStudent() {
        Student student = new Student();
        student.setStudentId("ABC");
        student.setName("John");
        student.setSchool("School");
        student.setAge(12);
        student.setGrade(3);
        student.setSetting("Setting");
        student.setDisability("Disability");
        return student;
    }
}
