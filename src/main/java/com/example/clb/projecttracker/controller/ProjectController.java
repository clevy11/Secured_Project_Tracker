package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.ProjectDto;
import com.example.clb.projecttracker.dto.ProjectRequestDto;
import com.example.clb.projecttracker.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/create")
    public ResponseEntity<ProjectDto> createProject(@Valid @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectDto createdProject = projectService.createProject(projectRequestDto);
        // Return 201 Created with Location header
        return ResponseEntity.created(URI.create("/api/v1/projects/" + createdProject.getId()))
                .body(createdProject);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long projectId) {
        ProjectDto projectDto = projectService.getProjectById(projectId);
        return ResponseEntity.ok(projectDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ProjectDto>> getAllProjects(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<ProjectDto> projects = projectService.getAllProjects(pageable);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/no-tasks")
    public ResponseEntity<Page<ProjectDto>> getProjectsWithNoTasks(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<ProjectDto> projects = projectService.getProjectsWithNoTasks(pageable);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDto> updateProject(@PathVariable Long projectId,
                                                  @Valid @RequestBody ProjectRequestDto projectRequestDto) {
        ProjectDto updatedProject = projectService.updateProject(projectId, projectRequestDto);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
