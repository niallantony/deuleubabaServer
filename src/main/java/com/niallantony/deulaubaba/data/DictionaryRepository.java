package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DictionaryRepository extends JpaRepository<DictionaryEntry, Long> {
}
