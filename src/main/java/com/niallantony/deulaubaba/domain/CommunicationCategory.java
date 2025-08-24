package com.niallantony.deulaubaba.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class CommunicationCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CommunicationCategoryLabel label;

    @ManyToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonBackReference
    private Set<DictionaryEntry> entries = new HashSet<>();
}
