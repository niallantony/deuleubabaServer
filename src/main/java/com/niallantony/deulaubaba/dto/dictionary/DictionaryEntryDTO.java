package com.niallantony.deulaubaba.dto.dictionary;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryDTO {
    private Long id;
    private ExpressionType type;
    private String title;
    private Set<CommunicationCategoryLabel> category;
    private String imgsrc;
    private String description;
}
