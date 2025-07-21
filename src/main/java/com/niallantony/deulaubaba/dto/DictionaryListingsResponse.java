package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.DictionaryEntry;
import com.niallantony.deulaubaba.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryListingsResponse {
    private Iterable<DictionaryEntryResponse> listings;
    private Set<ExpressionType> expressiontypes;
    private String message;
}
