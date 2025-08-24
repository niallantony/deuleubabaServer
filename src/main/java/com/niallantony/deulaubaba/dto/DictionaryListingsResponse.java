package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.domain.ExpressionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryListingsResponse {
    private List<DictionaryEntryResponse> listings;
    private Set<ExpressionType> expressiontypes;
    private String message;
}
