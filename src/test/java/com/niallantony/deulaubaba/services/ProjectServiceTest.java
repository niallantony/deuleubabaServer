package com.niallantony.deulaubaba.services;

import com.niallantony.deulaubaba.data.ProjectRepository;
import com.niallantony.deulaubaba.data.UserRepository;
import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.User;
import com.niallantony.deulaubaba.dto.ProjectCollectionsDTO;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.dto.ProjectPreviewDTO;
import com.niallantony.deulaubaba.exceptions.ResourceNotFoundException;
import com.niallantony.deulaubaba.exceptions.UserNotAuthorizedException;
import com.niallantony.deulaubaba.mapper.ProjectMapper;
import com.niallantony.deulaubaba.util.ProjectTestFactory;
import net.bytebuddy.asm.Advice;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {
    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ProjectService projectService;

    @Test
    public void getProject_whenGivenValidId_returnsProject() {
        Project persistedProject = ProjectTestFactory.getEmptyProjectWithUser("abc");
        ProjectDTO expectedProject = new ProjectDTO();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(persistedProject));
        when(projectMapper.toDTO(persistedProject)).thenReturn(expectedProject);

        ProjectDTO response = projectService.getProject("1", "abc");
        verify(projectRepository, times(1)).findById(1L);
        verify(projectMapper, times(1)).toDTO(persistedProject);
        assertEquals(expectedProject, response);
    }

    @Test
    public void getProject_whenGivenInvalidId_throwsResourceNotFoundException() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProject("1", "abc"));
    }

    @Test
    public void getProject_whenUserNotAssignedToProject_returns401() {
        Project persistedProject = new Project();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(persistedProject));
        assertThrows(UserNotAuthorizedException.class, () -> projectService.getProject("1", "abc"));
    }

    @Test
    public void getAllProjectsOfUser_whenGivenValidId_returnsAllProjects() {
        Set<ProjectPreviewDTO> projects = getProjectPreviewDTOS();
        User user = new User();
        when(userRepository.findByUserId("abc")).thenReturn(Optional.of(user));
        when(projectRepository.findAllProjectsByUserId(user)).thenReturn(projects);

        ProjectCollectionsDTO response = projectService.getProjectsOfUser("abc");
        verify(projectRepository, times(1)).findAllProjectsByUserId(user);
        assertEquals(1, response.getCompleted().size());
        assertEquals(1, response.getPending().size());
        assertEquals(1, response.getCurrent().size());
    }

    private static @NotNull Set<ProjectPreviewDTO> getProjectPreviewDTOS() {
        LocalDate today = LocalDate.now();
        ProjectPreviewDTO persistedProject = new ProjectPreviewDTO();
        persistedProject.setId(1L);
        persistedProject.setCompleted(true);
        ProjectPreviewDTO persistedProject2 = new ProjectPreviewDTO();
        persistedProject2.setId(2L);
        persistedProject2.setStartedOn(today.plusDays(30));
        ProjectPreviewDTO persistedProject3 = new ProjectPreviewDTO();
        persistedProject3.setId(3L);
        persistedProject3.setStartedOn(today.minusDays(30));
        Set<ProjectPreviewDTO> projects = new HashSet<>();
        projects.add(persistedProject);
        projects.add(persistedProject2);
        projects.add(persistedProject3);
        return projects;
    }
}
