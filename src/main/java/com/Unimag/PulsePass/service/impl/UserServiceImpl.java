package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.UserProfile;
import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.UserResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.DuplicateResourceException;
import com.Unimag.PulsePass.exception.ResourceNotFoundException;
import com.Unimag.PulsePass.mapper.UserMapper;
import com.Unimag.PulsePass.repository.UserRepository;
import com.Unimag.PulsePass.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        if (request == null) throw new BusinessRuleException("Registration request is required.");
        if (request.username() == null || request.username().isBlank())
            throw new BusinessRuleException("Username is required.");
        if (request.email() == null || request.email().isBlank())
            throw new BusinessRuleException("Email is required.");
        if (request.firstName() == null || request.firstName().isBlank()
                || request.lastName() == null || request.lastName().isBlank())
            throw new BusinessRuleException("First name and last name are required.");
        if (request.birthDate() != null && request.birthDate().isAfter(LocalDate.now()))
            throw new BusinessRuleException("Birth date cannot be in the future.");
        if (userRepository.existsByUsername(request.username()))
            throw new DuplicateResourceException("Username already exists: " + request.username());
        if (userRepository.existsByEmailIgnoreCase(request.email()))
            throw new DuplicateResourceException("Email already exists: " + request.email());

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setActive(true);

        UserProfile profile = new UserProfile();
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setPhone(request.phone());
        profile.setCity(request.city());
        profile.setBirthDate(request.birthDate());
        profile.setUser(user);
        user.setUserProfile(profile);

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    public UserResponse findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
    }

    @Override
    public UserResponse findByUsername(String username) {
        return userRepository.findByUsername(username).map(userMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for username: " + username));
    }
}
