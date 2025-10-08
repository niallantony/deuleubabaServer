package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.ProjectUser;
import com.niallantony.deulaubaba.dto.project.ProjectDTO;
import com.niallantony.deulaubaba.dto.project.ProjectPostDTO;
import com.niallantony.deulaubaba.dto.project.ProjectUserStatusDTO;
import com.niallantony.deulaubaba.dto.user.UserAvatar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

// TODO: Implement projectUser to DTO - then refactor the set method to use this
@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(source = "users", target = "userStatuses")
    ProjectDTO entityToDto(Project project);

    @Mapping(target = "categories", ignore = true)
    Project requestToEntity(ProjectPostDTO project);

    ProjectUserStatusDTO projectUserToProjectUserStatus(ProjectUser projectUser);

    default Set<ProjectUserStatusDTO> projectUserToProjectUserStatus(Set<ProjectUser> projectUsers) {
        if (projectUsers.isEmpty()) return null;
        return projectUsers.stream().map((user) -> new ProjectUserStatusDTO(
                new UserAvatar(
                        user.getUser().getUsername(),
                        user.getUser().getImagesrc(),
                        user.getUser().getUserType()
                ),
                user.getIsCompleted(),
                user.getCompletedOn()
        )).collect(Collectors.toSet());
    }
}

