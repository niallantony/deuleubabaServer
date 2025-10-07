package com.niallantony.deulaubaba.dto.project;

import com.niallantony.deulaubaba.domain.CommunicationCategoryLabel;
import com.niallantony.deulaubaba.domain.ProjectType;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class ProjectPostDTO {
    private String objective;
    private LocalDate startedOn;
    private Set<CommunicationCategoryLabel> categories = new HashSet<>();
    private String description;
    private ProjectType type;
    private Set<String> usernames = new HashSet<>();
    private String studentId;
}

