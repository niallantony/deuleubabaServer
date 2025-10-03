package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/project", produces = "application/json")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public ResponseEntity<ProjectDTO> getProject(
            @RequestParam String id,
            @CurrentUser String userID
    ) {
        return ResponseEntity.ok(projectService.getProject(id, userID));
    }
}
