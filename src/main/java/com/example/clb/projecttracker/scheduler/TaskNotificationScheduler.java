package com.example.clb.projecttracker.scheduler;

import com.example.clb.projecttracker.model.Developer;
import com.example.clb.projecttracker.model.Task;
import com.example.clb.projecttracker.service.EmailService;
import com.example.clb.projecttracker.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskNotificationScheduler {

    private final TaskService taskService;
    private final EmailService emailService;

    // Schedule to run daily at 8:00 AM. Cron format: second, minute, hour, day of month, month, day(s) of week
    // For testing, you might want a more frequent schedule, e.g., every 5 minutes: "0 */5 * * * ?"
    // Or every minute: "0 * * * * ?"
    @Scheduled(cron = "0 0 8 * * ?") // Daily at 8 AM
    // @Scheduled(cron = "0 * * * * ?") // For testing: runs every minute
    @Transactional(readOnly = true)
    public void sendOverdueTaskNotifications() {
        log.info("Running scheduled job: Send Overdue Task Notifications");
        List<Task> overdueTasks = taskService.findOverdueTasksForNotification();

        if (overdueTasks.isEmpty()) {
            log.info("No overdue tasks found.");
            return;
        }

        log.info("Found {} overdue tasks. Preparing notifications...", overdueTasks.size());

        for (Task task : overdueTasks) {
            Developer developer = task.getDeveloper(); // Assumes Developer is eagerly fetched or accessible
            if (developer != null && developer.getEmail() != null && !developer.getEmail().isBlank()) {
                String subject = "Overdue Task Reminder: " + task.getTitle();
                String body = String.format(
                        "Dear %s,\n\n" +
                                "This is a reminder that the following task assigned to you is overdue:\n\n" +
                                "Task ID: %d\n" +
                                "Title: %s\n" +
                                "Project: %s\n" + // Assumes Project is eagerly fetched or accessible
                                "Due Date: %s\n\n" +
                                "Please update its status or complete it as soon as possible.\n\n" +
                                "Thank you,\nProject Tracker System",
                        developer.getName(),
                        task.getId(),
                        task.getTitle(),
                        task.getProject() != null ? task.getProject().getName() : "N/A",
                        task.getDueDate() != null ? task.getDueDate().toString() : "N/A"
                );

                try {
                    // Ensure your EmailService is in com.example.clb.projecttracker.service.email
                    // If it's in com.example.clb.projecttracker.service, adjust the import above.
                    emailService.sendSimpleMessage(developer.getEmail(), subject, body);
                    log.info("Sent overdue task notification for task ID {} to {}", task.getId(), developer.getEmail());
                } catch (Exception e) {
                    log.error("Failed to send notification for task ID {} to {}: {}", task.getId(), developer.getEmail(), e.getMessage(), e);
                }
            } else {
                log.warn("Skipping notification for task ID {}: No developer assigned or developer has no email.", task.getId());
            }
        }
        log.info("Finished processing overdue task notifications. {} tasks processed.", overdueTasks.size());
    }
}