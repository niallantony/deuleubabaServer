package com.niallantony.deulaubaba.dto.project;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class ProjectDetailsPatchDTO {
    private String objective;
    private LocalDate startedOn;
    private String description;
    private Set<CommunicationCategoryLabel> categories;
}
