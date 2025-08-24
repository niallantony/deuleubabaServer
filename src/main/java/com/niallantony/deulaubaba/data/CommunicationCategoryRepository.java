package com.niallantony.deulaubaba.data;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunicationCategoryRepository extends JpaRepository<CommunicationCategory, Long> {
    Optional<CommunicationCategory> findByLabel(CommunicationCategoryLabel label);
}
