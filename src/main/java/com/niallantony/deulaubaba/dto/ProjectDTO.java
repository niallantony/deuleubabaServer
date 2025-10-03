package com.niallantony.deulaubaba.dto;

import com.niallantony.deulaubaba.domain.CommunicationCategory;
import com.niallantony.deulaubaba.domain.ProjectType;
import com.niallantony.deulaubaba.domain.ProjectUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String objective;
    private Date startDate;
    private Set<CommunicationCategory> categories = new HashSet<>();
    private String description;
    private String imgsrc;
    private Boolean completed;
    private Date completedOn;
    private ProjectType type;
    private Set<ProjectUser> users = new HashSet<>();
}
