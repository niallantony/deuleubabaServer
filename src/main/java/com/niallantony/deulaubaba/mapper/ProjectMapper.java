package com.niallantony.deulaubaba.mapper;

import com.niallantony.deulaubaba.domain.Project;
import com.niallantony.deulaubaba.domain.ProjectUser;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.dto.ProjectUserStatus;
import com.niallantony.deulaubaba.dto.UserAvatar;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    @Mapping(source = "users", target = "userStatuses")
    ProjectDTO toDTO(Project project);

    Project fromDTO(ProjectDTO projectDTO);


    default Set<ProjectUserStatus> projectUserToProjectUserStatus(Set<ProjectUser> projectUsers) {
        if (projectUsers.isEmpty()) return null;
        return projectUsers.stream().map((user) -> new ProjectUserStatus(
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

