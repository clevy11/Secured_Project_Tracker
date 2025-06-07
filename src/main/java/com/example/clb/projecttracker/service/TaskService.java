package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface TaskService {

    TaskDto createTask(TaskRequestDto taskRequestDto);

    TaskDto getTaskById(Long taskId);

    Page<TaskDto> getAllTasks(Pageable pageable);

    Page<TaskDto> getTasksByProjectId(Long projectId, Pageable pageable);

    Page<TaskDto> getTasksByDeveloperId(Long developerId, Pageable pageable);

    Page<TaskDto> getOverdueTasks(Pageable pageable);

    List<TaskStatusCountDto> getTaskCountsByStatusForProject(Long projectId);

    List<TaskStatusCountDto> getTaskCountsByStatusOverall();

    TaskDto updateTask(Long taskId, TaskRequestDto taskRequestDto);

    TaskDto assignTaskToDeveloper(Long taskId, Long developerId);

    TaskDto unassignTaskFromDeveloper(Long taskId);

    void deleteTask(Long taskId);

    // Methods for advanced queries will be added later, e.g.:
    // List<TaskDto> findOverdueTasks();
    // List<TaskDto> findTasksByStatus(TaskStatus status);
}
