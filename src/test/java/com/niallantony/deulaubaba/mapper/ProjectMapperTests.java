package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectMapperTests {

    @Test
    void entityToDto_whenGivenProjectEntry_thenReturnProjectDto() {
        ProjectMapper projectMapper = new ProjectMapperImpl();
        CommunicationCategory mockCategory = new CommunicationCategory();
        mockCategory.setLabel(CommunicationCategoryLabel.ATTENTION);
        Student student = new Student();
        LocalDate start = LocalDate.of(2020, 1, 1);
        LocalDate end = LocalDate.of(2020, 12, 31);
        User user = new User();
        user.setUserType("teacher");
        user.setImagesrc("useravatar.jpg");
        user.setUsername("username");
        ProjectUser projectUser = new ProjectUser();
        projectUser.setUser(user);

        Project project = new Project();
        project.setId(123L);
        project.getCategories().add(mockCategory);
        project.setStudent(student);
        project.setStartedOn(start);
        project.setDescription("Description");
        project.setImgsrc("Image");
        project.setType(ProjectType.COMMUNICATION);
        project.setCompletedOn(end);
        project.getUsers().add(projectUser);


        ProjectDTO dto = projectMapper.toDTO(project);

        assertEquals(project.getId(), dto.getId());
        assertEquals(project.getCategories(), dto.getCategories());
        assertEquals(project.getStartedOn(), dto.getStartedOn());
        assertEquals(project.getDescription(), dto.getDescription());
        assertEquals(project.getImgsrc(), dto.getImgsrc());
        assertEquals(project.getType(), dto.getType());
        assertEquals(project.getCompletedOn(), dto.getCompletedOn());
        assertEquals(1, dto.getUserStatuses().size());
    }
}
