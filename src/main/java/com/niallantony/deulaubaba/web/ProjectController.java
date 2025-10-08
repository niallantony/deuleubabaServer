package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.project.*;
import com.niallantony.deulaubaba.exceptions.InvalidProjectDataException;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.ProjectService;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping(path = "/project", produces = "application/json")
public class ProjectController {

    private final ProjectService projectService;
    private final JsonUtils jsonUtils;

    public ProjectController(ProjectService projectService, JsonUtils jsonUtils) {
        this.projectService = projectService;
        this.jsonUtils = jsonUtils;
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

    @PostMapping()
    public ResponseEntity<ProjectDTO> createProject(
            @CurrentUser String user_id,
            @RequestPart(name = "data") String data,
            @RequestPart(name = "image", required = false)MultipartFile image
            ) {
        ProjectPostDTO request = jsonUtils.parse(
                data,
                ProjectPostDTO.class,
                () -> new InvalidProjectDataException("Invalid request")
        );
        ProjectDTO project = projectService.createProject(request, image, user_id);
        return ResponseEntity.created(
                URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/" + project.getId()
        )).body(project);
    }

    @PatchMapping(path = "/status/{project_id}")
    public ResponseEntity<ProjectUserStatusDTO> updateProjectStatus(
            @CurrentUser String user_id,
            @PathVariable String project_id,
            @RequestBody String body
    ) {
        ProjectStatusDTO request = jsonUtils.parse(body, ProjectStatusDTO.class,
                () -> new InvalidProjectDataException("Invalid request"));
        projectService.changeCompletedStatus(user_id, project_id, request.getIsCompleted());
        return ResponseEntity.noContent().location(
                URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/" + project_id)
        ).build();
    }
}
