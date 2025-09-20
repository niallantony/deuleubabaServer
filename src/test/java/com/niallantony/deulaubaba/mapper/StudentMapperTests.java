package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.StudentDTO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class StudentMapperTests {

    @Test
    void mapsToDTO() {
        StudentMapper studentMapper = new StudentMapperImpl();
        Student student = new Student();
        student.setStudentId("abc");
        student.setName("John");
        student.setAge(23);
        student.setGrade(3);
        student.setSetting("General");
        student.setDisability("None");
        student.setImagesrc("./example.png");
        student.setChallengesDetails("Example Challenges Details");
        student.setCommunicationDetails("Example Communication Details");

        StudentDTO dto = studentMapper.toDTO(student);

        assertEquals(dto.getStudentId(), "abc");
        assertEquals(dto.getName(), "John");
        assertEquals(dto.getAge() , 23);
        assertEquals(dto.getGrade(),  3);
        assertEquals(dto.getSetting(),"General");
        assertEquals(dto.getDisability(),"None");
        assertEquals(dto.getImagesrc(),"./example.png");
        assertEquals(dto.getChallengesDetails(),"Example Challenges Details");
        assertEquals(dto.getCommunicationDetails(),"Example Communication Details");
    }

}
