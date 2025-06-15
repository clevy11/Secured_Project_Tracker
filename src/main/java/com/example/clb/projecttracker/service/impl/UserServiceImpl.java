package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.dto.response.UserDto;
import com.example.clb.projecttracker.repository.UserRepository;
import com.example.clb.projecttracker.service.UserMapper;
import com.example.clb.projecttracker.service.UserService;
import com.example.clb.projecttracker.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<UserDto> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .filter(UserPrincipal.class::isInstance)
                .map(UserPrincipal.class::cast)
                .map(UserPrincipal::getId)
                .flatMap(userRepository::findById)
                .map(userMapper::toDto);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id).map(userMapper::toDto);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
