package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import com.example.clb.projecttracker.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<TaskDto> createTask(@Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskDto createdTask = taskService.createTask(taskRequestDto);
        return ResponseEntity.created(URI.create("/api/v1/tasks/" + createdTask.getId()))
                .body(createdTask);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable Long taskId) {
        TaskDto taskDto = taskService.getTaskById(taskId);
        return ResponseEntity.ok(taskDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<TaskDto>> getAllTasks(
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<Page<TaskDto>> getTasksByProjectId(
            @PathVariable Long projectId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByProjectId(projectId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<Page<TaskDto>> getTasksByDeveloperId(
            @PathVariable Long developerId,
            @PageableDefault(size = 10, sort = "dueDate") Pageable pageable) {
        Page<TaskDto> tasks = taskService.getTasksByDeveloperId(developerId, pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/overdue")
    public ResponseEntity<Page<TaskDto>> getOverdueTasks(
            @PageableDefault(size = 20, sort = "dueDate,asc") Pageable pageable) {
        Page<TaskDto> overdueTasks = taskService.getOverdueTasks(pageable);
        return ResponseEntity.ok(overdueTasks);
    }

    @GetMapping("/overdue/list")
    @Operation(summary = "Get all overdue tasks (not paginated)",
               description = "Retrieves a list of all tasks that are past their due date and are not yet completed or cancelled.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of overdue tasks")
    })
    public ResponseEntity<List<TaskDto>> getOverdueTasksList() {
        return ResponseEntity.ok(taskService.findOverdueTasks());
    }

    @GetMapping("/projects/{projectId}/status-counts")
    @Operation(summary = "Get task counts by status for a specific project")
    public ResponseEntity<List<TaskStatusCountDto>> getTaskCountsByStatusForProject(@PathVariable Long projectId) {
        List<TaskStatusCountDto> counts = taskService.getTaskCountsByStatusForProject(projectId);
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/status-counts")
    public ResponseEntity<List<TaskStatusCountDto>> getTaskCountsByStatusOverall() {
        List<TaskStatusCountDto> counts = taskService.getTaskCountsByStatusOverall();
        return ResponseEntity.ok(counts);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable Long taskId, @Valid @RequestBody TaskRequestDto taskRequestDto) {
        TaskDto updatedTask = taskService.updateTask(taskId, taskRequestDto);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/assign/{developerId}")
    public ResponseEntity<TaskDto> assignTaskToDeveloper(@PathVariable Long taskId, @PathVariable Long developerId) {
        TaskDto updatedTask = taskService.assignTaskToDeveloper(taskId, developerId);
        return ResponseEntity.ok(updatedTask);
    }

    @PatchMapping("/{taskId}/unassign")
    public ResponseEntity<TaskDto> unassignTaskFromDeveloper(@PathVariable Long taskId) {
        TaskDto updatedTask = taskService.unassignTaskFromDeveloper(taskId);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
