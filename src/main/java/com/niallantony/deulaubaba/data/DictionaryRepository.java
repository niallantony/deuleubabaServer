package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<DictionaryEntry, Long> {
    List<DictionaryEntry> findAllByStudent(Student student);
}
