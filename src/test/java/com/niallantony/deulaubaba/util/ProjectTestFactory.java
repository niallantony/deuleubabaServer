package com.niallantony.deulaubaba.util;


import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.project.ProjectPostDTO;

import java.time.LocalDate;

public class ProjectTestFactory {
    public static Project getEmptyProjectWithUser(String id) {
        Project project = new Project();
        ProjectUser projectUser = new ProjectUser();
        User user = new User();
        user.setUserId(id);
        projectUser.setUser(user);
        project.getUsers().add(projectUser);
        return project;
    }

    public static ProjectPostDTO getProjectPostDTOWithUser(String id) {
        ProjectPostDTO p = new ProjectPostDTO();
        p.getUsernames().add(id);
        p.setStudentId("abc");
        p.setStartedOn(LocalDate.of(2000, 1, 1));
        p.setType(ProjectType.COMMUNICATION);
        p.setObjective("Objective");
        p.setDescription("Description");
        p.getCategories().add(CommunicationCategoryLabel.PAIN);
        return p;
    }
}
