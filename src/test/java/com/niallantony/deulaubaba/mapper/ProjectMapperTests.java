package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.*;
import com.niallantony.deulaubaba.dto.project.ProjectDTO;
import com.niallantony.deulaubaba.dto.project.ProjectPostDTO;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProjectMapperTests {

    @Test
    void entityEntityToDto() {
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


        ProjectDTO dto = projectMapper.entityToDto(project);

        assertEquals(project.getId(), dto.getId());
        assertEquals(project.getCategories(), dto.getCategories());
        assertEquals(project.getStartedOn(), dto.getStartedOn());
        assertEquals(project.getDescription(), dto.getDescription());
        assertEquals(project.getImgsrc(), dto.getImgsrc());
        assertEquals(project.getType(), dto.getType());
        assertEquals(project.getCompletedOn(), dto.getCompletedOn());
        assertEquals(1, dto.getUserStatuses().size());
    }

    @Test
    void postDtoToEntity() {
        ProjectMapper mapper = new ProjectMapperImpl();
        ProjectPostDTO p = new ProjectPostDTO();
        p.setObjective("Objective");
        p.setDescription("Description");
        p.setStartedOn(LocalDate.of(2020, 1, 1));
        p.getCategories().add(CommunicationCategoryLabel.PAIN);
        p.setType(ProjectType.COMMUNICATION);
        p.getUsernames().add("user");
        p.setStudentId("studentId");

        Project project = mapper.requestToEntity(p);

        assertEquals("Objective", project.getObjective());
        assertEquals("Description", project.getDescription());
        assertEquals(LocalDate.of(2020, 1, 1), project.getStartedOn());
        assertEquals(0, project.getCategories().size());
        assertEquals(ProjectType.COMMUNICATION, project.getType());
        assertNull(project.getStudent());
        assertEquals(0, project.getUsers().size());
    }
}
