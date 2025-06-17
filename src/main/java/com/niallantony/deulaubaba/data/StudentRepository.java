package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.Student;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface StudentRepository extends CrudRepository<Student, String> {
    public Optional<Student> findById(String id);
}
