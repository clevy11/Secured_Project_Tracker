package com.example.clb.projecttracker.service;

import com.example.clb.projecttracker.dto.response.UserDto;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Optional<UserDto> getCurrentUser();

    List<UserDto> getAllUsers();

    Optional<UserDto> getUserById(Long id);

    void deleteUser(Long id);
}
