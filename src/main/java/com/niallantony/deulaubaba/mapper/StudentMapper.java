package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Student;
import com.niallantony.deulaubaba.dto.StudentDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentMapper {

    @Mapping(target = "student.users", ignore = true)
    @Mapping(target = "student.dictionaries", ignore = true)
    public StudentDTO toDTO(Student student);
}
