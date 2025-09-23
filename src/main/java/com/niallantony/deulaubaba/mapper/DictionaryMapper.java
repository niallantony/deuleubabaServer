package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.dto.DictionaryEntryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DictionaryMapper {
    DictionaryEntryDTO entityToDto(DictionaryEntry dictionaryEntry);

    default CommunicationCategoryLabel communicationCategoryToCommunicationCategoryLabel(CommunicationCategory communicationCategory) {
        if (communicationCategory == null) {
            return null;
        }
        return communicationCategory.getLabel();
    };
}
