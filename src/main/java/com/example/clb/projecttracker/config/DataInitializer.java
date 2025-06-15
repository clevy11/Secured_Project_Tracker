package com.example.clb.projecttracker.config;

import com.example.clb.projecttracker.model.ERole;
import com.example.clb.projecttracker.model.Role;
import com.example.clb.projecttracker.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public DataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
    }

    private void initializeRoles() {
        for (ERole roleName : ERole.values()) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(null, roleName));
            }
        }
    }
}
