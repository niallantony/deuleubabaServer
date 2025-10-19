package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.StudentFeedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentFeedRepository extends JpaRepository<StudentFeedItem, Long> {
}
