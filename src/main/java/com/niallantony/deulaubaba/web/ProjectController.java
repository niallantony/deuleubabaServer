package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.ProjectCollectionsDTO;
import com.niallantony.deulaubaba.dto.ProjectDTO;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping(path = "/all")
    public ResponseEntity<ProjectCollectionsDTO> getProjectsForUser(
            @CurrentUser String id
    ) {
        return ResponseEntity.ok(projectService.getProjectsOfUser(id));
    }

    @GetMapping(path = "/all/{student_id}")
    public ResponseEntity<ProjectCollectionsDTO> getStudentsProjects(
            @CurrentUser String user_id,
            @PathVariable String student_id
    ) {
        return ResponseEntity.ok(projectService.getProjectsOfStudent(user_id, student_id));
    }
}
