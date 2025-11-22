package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.ProjectUser;
import com.niallantony.deulaubaba.dto.project.ProjectDTO;
import com.niallantony.deulaubaba.dto.project.ProjectPostDTO;
import com.niallantony.deulaubaba.dto.project.ProjectPreviewDTO;
import com.niallantony.deulaubaba.dto.project.ProjectUserStatusDTO;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import com.niallantony.deulaubaba.services.FileStorageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class ProjectMapper {
    @Autowired
    FileStorageService fileStorageService;

    @Mapping(source = "users", target = "userStatuses")
    public abstract ProjectDTO entityToDto(Project project);

    public abstract ProjectPreviewDTO entityToPreviewDTO(Project project);

    @Mapping(target = "categories", ignore = true)
    public abstract Project requestToEntity(ProjectPostDTO project);

    public abstract ProjectUserStatusDTO projectUserToProjectUserStatus(ProjectUser projectUser);

    protected Set<ProjectUserStatusDTO> projectUserToProjectUserStatus(Set<ProjectUser> projectUsers) {
        if (projectUsers.isEmpty()) return null;
        return projectUsers.stream().map((user) -> new ProjectUserStatusDTO(
                new UserAvatar(
                        user.getUser().getUsername(),
                        fileStorageService.generateSignedURL(user.getUser()),
                        user.getUser().getUserType()
                ),
                user.getIsCompleted(),
                user.getCompletedOn()
        )).collect(Collectors.toSet());
    }
}

