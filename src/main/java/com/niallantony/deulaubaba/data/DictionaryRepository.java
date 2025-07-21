package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.DictionaryEntry;
import com.niallantony.deulaubaba.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntry, Long> {
    List<DictionaryEntry> findAllByStudent(Student student);
}
