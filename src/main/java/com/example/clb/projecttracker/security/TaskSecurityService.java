package com.example.clb.projecttracker.security;

import com.example.clb.projecttracker.model.Task;
import com.example.clb.projecttracker.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service("taskSecurity")
@RequiredArgsConstructor
public class TaskSecurityService {

    private final TaskRepository taskRepository;

    public boolean isAssigneeOrAdminOrManager(Long taskId) {
        return SecurityUtil.getCurrentUserPrincipal().map(principal -> {
            if (principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"))) {
                return true;
            }
            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null || task.getDeveloper() == null) {
                return false;
            }
            return task.getDeveloper().getId().equals(principal.getId());
        }).orElse(false);
    }
}
