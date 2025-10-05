package com.niallantony.deulaubaba.dto.project;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class ProjectCollectionsDTO {
    private Set<ProjectPreviewDTO> completed = new HashSet<>();
    private Set<ProjectPreviewDTO> pending = new HashSet<>();
    private Set<ProjectPreviewDTO> current = new HashSet<>();
}
