package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ExpressionType;

import java.util.Set;

public interface DictionaryRequest {
    String getStudentId();
    ExpressionType getType();
    String getTitle();
    Set<CommunicationCategoryLabel> getCategory();
    String getImgsrc();
    String getDescription();
}
