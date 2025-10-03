package com.niallantony.deulaubaba.util;


import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.ProjectUser;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.ProjectDTO;

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
}
