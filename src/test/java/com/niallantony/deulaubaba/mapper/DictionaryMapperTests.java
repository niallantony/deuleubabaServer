package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.DictionaryEntry;
import com.niallantony.deulaubaba.domain.ExpressionType;
import com.niallantony.deulaubaba.dto.dictionary.DictionaryEntryDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DictionaryMapperTests {

    @Test
    void entityToDTO_whenGivenDictionaryEntry_thenReturnDTO() {
        DictionaryMapper dictionaryMapper = new DictionaryMapperImpl();
        CommunicationCategory mockCategory = new CommunicationCategory();
        mockCategory.setLabel(CommunicationCategoryLabel.ATTENTION);

        DictionaryEntry mockEntry = new DictionaryEntry();
        mockEntry.setId(123L);
        mockEntry.setType(ExpressionType.BODY);
        mockEntry.setTitle("Movement");
        mockEntry.getCategory().add(mockCategory);
        mockEntry.setImgsrc("./example.png");
        mockEntry.setDescription("Description");

        DictionaryEntryDTO result = dictionaryMapper.entityToDto(mockEntry);

        assertEquals(result.getId(), mockEntry.getId());
        assertEquals(result.getType(), mockEntry.getType());
        assertEquals(result.getTitle(), mockEntry.getTitle());
        assertEquals(result.getDescription(), mockEntry.getDescription());
        assertEquals(result.getImgsrc(), mockEntry.getImgsrc());
        assertTrue(result.getCategory().contains(mockCategory.getLabel()));
    }

    @Test
    void entityToDTO_whenGivenDictionaryEntryWithMultipleCategories_thenReturnDTO() {
        DictionaryMapper dictionaryMapper = new DictionaryMapperImpl();
        CommunicationCategory mockCategory1 = new CommunicationCategory();
        mockCategory1.setLabel(CommunicationCategoryLabel.ATTENTION);
        CommunicationCategory mockCategory2 = new CommunicationCategory();
        mockCategory1.setLabel(CommunicationCategoryLabel.PAIN);

        DictionaryEntry mockEntry = new DictionaryEntry();
        mockEntry.setId(123L);
        mockEntry.setType(ExpressionType.BODY);
        mockEntry.setTitle("Movement");
        mockEntry.getCategory().add(mockCategory1);
        mockEntry.getCategory().add(mockCategory2);
        mockEntry.setImgsrc("./example.png");
        mockEntry.setDescription("Description");

        DictionaryEntryDTO result = dictionaryMapper.entityToDto(mockEntry);

        assertEquals(result.getId(), mockEntry.getId());
        assertEquals(result.getType(), mockEntry.getType());
        assertEquals(result.getTitle(), mockEntry.getTitle());
        assertEquals(result.getDescription(), mockEntry.getDescription());
        assertEquals(result.getImgsrc(), mockEntry.getImgsrc());
        assertTrue(result.getCategory().contains(mockCategory1.getLabel()));
        assertTrue(result.getCategory().contains(mockCategory2.getLabel()));
    }
}
