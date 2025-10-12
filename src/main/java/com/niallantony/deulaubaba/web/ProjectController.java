package com.niallantony.deulaubaba.web;

import com.niallantony.deulaubaba.dto.project.*;
import com.niallantony.deulaubaba.exceptions.InvalidProjectDataException;
import com.niallantony.deulaubaba.security.CurrentUser;
import com.niallantony.deulaubaba.services.ProjectService;
import com.niallantony.deulaubaba.utils.JsonUtils;
import org.springframework.http.HttpStatus;
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
        try {
            Long projectId = Long.parseLong(id);
            return ResponseEntity.ok(projectService.getProject(projectId, userID));
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project id");
        }
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
    public ResponseEntity<?> updateProjectStatus(
            @CurrentUser String user_id,
            @PathVariable String project_id,
            @RequestBody String body
    ) {
        try {
            Long id = Long.parseLong(project_id);
            ProjectStatusDTO request = jsonUtils.parse(body, ProjectStatusDTO.class,
                    () -> new InvalidProjectDataException("Invalid request"));
            projectService.changeCompletedStatus(user_id, id, request.getIsCompleted());
            return ResponseEntity.noContent().location(
                    URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/" + project_id)
            ).build();
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project ID");
        }
    }

    @PatchMapping(path = "/{project_id}")
    public ResponseEntity<?> updateProject(
            @CurrentUser String user_id,
            @PathVariable String project_id,
            @RequestPart(name = "data") String data,
            @RequestPart(name = "image", required = false) MultipartFile image
    ) {
        try {
            Long longProjectId = Long.parseLong(project_id);
            ProjectDetailsPatchDTO request = jsonUtils.parse(
                    data,
                    ProjectDetailsPatchDTO.class,
                    () -> new InvalidProjectDataException("Invalid request")
            );
            projectService.patchProjectDetails(user_id, longProjectId, request, image);
            return ResponseEntity.noContent()
                                 .location(
                                         URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/project").toUriString() + "/" + project_id)
                                 )
                                 .build();
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project_id: " + project_id);
        }
    }

    @DeleteMapping(path = "/{project_id}")
    public ResponseEntity<?> deleteProject(
            @CurrentUser String user_id,
            @PathVariable String project_id
    ) {
        try {
            Long longProjectId = Long.parseLong(project_id);
            projectService.deleteProject(user_id, longProjectId);
            return ResponseEntity.noContent().build();
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project_id: " + project_id);
        }
    }

    @PatchMapping(path = "/{project_id}/add-user")
    public ResponseEntity<ProjectAddUserResponseDTO> addUserToProject(
            @CurrentUser String user_id,
            @PathVariable String project_id,
            @RequestBody String data
    ) {
        try {
            Long longProjectId = Long.parseLong(project_id);
            ProjectAddUserRequestDTO request = jsonUtils.parse(
                    data,
                    ProjectAddUserRequestDTO.class,
                    () -> new InvalidProjectDataException("Invalid request")
            );
            ProjectAddUserResponseDTO response = projectService.addUsersToProject(
                    user_id,
                    longProjectId,
                    request
            );
            if (response.getNotFound().size() == request.getToAdd().size()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

            }
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            throw new InvalidProjectDataException("Invalid project_id: " + project_id);
        }
    }
}
