package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.student.StudentDTO;
import com.niallantony.deulaubaba.dto.student.StudentRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class StudentMapperTests {

    @Test
    void givenStudent_whenMap_thenCorrectDTO() {
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

    @Test
    void studentMapper_whenGivenStudentRequest_returnsStudent() {
        StudentMapper studentMapper = new StudentMapperImpl();
        StudentRequest request = new StudentRequest();

        request.setName("John");
        request.setAge(23);
        request.setGrade(3);
        request.setSetting("General");
        request.setDisability("None");
        request.setImgsrc("./example.png");

        Student student = studentMapper.toStudent(request);
        assertEquals("John", student.getName());
        assertEquals(23, student.getAge());
        assertEquals(3, student.getGrade());
        assertEquals("General", student.getSetting());
        assertEquals("None", student.getDisability());
        assertEquals("./example.png", student.getImagesrc());
    }

}
