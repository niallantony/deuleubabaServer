package com.niallantony.deulaubaba.dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProjectAddUserRequestDTO {
    List<String> toAdd;
}
