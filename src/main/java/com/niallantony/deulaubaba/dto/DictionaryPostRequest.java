package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryPostRequest implements DictionaryRequest {
    private String studentId;
    private ExpressionType type;
    private String title;
    private Set<CommunicationCategoryLabel> category = new HashSet<>();
    private String description;
}
