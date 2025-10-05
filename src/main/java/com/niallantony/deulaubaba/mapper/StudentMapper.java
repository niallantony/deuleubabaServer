package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.student.StudentDTO;
import com.niallantony.deulaubaba.dto.student.StudentRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDTO toDTO(Student student);

    @Mapping(target = "imagesrc", source = "imgsrc")
    Student toStudent(StudentRequest request);
}
