package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.StudentDTO;
import com.niallantony.deulaubaba.dto.StudentRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    StudentDTO toDTO(Student student);

    Student toStudent(StudentRequest request);
}
