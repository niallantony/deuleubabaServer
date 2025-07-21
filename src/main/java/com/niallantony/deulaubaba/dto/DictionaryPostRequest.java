package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class DictionaryPostRequest {
    private String studentId;
    private ExpressionType type;
    private String title;
    private Set<CommunicationCategoryLabel> category;
    private String imgsrc;
    private String description;
}
