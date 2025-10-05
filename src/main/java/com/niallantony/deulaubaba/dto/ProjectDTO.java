package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String objective;
    private LocalDate startedOn;
    private Set<CommunicationCategory> categories = new HashSet<>();
    private String description;
    private String imgsrc;
    private boolean completed;
    private LocalDate completedOn;
    private ProjectType type;
    private Set<ProjectUserStatus> userStatuses = new HashSet<>();
    private boolean isOwnProject = false;

    public void isOwnProject(boolean isOwnProject) {
        this.isOwnProject = isOwnProject;
    }
}
