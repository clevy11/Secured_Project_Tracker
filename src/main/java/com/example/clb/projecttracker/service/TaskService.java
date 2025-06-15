package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.TaskDto;
import com.example.clb.projecttracker.dto.TaskRequestDto;
import com.example.clb.projecttracker.dto.TaskStatusCountDto;
import com.example.clb.projecttracker.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for managing tasks.
 */
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

    Page<TaskDto> findTasksByDeveloper(Long developerId, Pageable pageable);

    List<TaskDto> findTasksByProjectId(Long projectId);

    List<TaskDto> findOverdueTasks();

    List<Task> findOverdueTasksForNotification();


}
