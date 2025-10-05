package com.niallantony.deulaubaba.dto.project;

import com.niallantony.deulaubaba.dto.user.UserAvatar;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUserStatus {
    UserAvatar user;
    Boolean completed;
    LocalDate completedOn;

}
