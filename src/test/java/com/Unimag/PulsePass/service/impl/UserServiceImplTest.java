package com.Unimag.PulsePass.service.impl;

import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.UserResponse;
import com.Unimag.PulsePass.exception.BusinessRuleException;
import com.Unimag.PulsePass.exception.DuplicateResourceException;
import com.Unimag.PulsePass.mapper.UserMapper;
import com.Unimag.PulsePass.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock UserRepository userRepository;
    @Mock UserMapper userMapper;
    @InjectMocks UserServiceImpl service;

    @Test
    void register_validRequest_savesActiveUserAndProfile() {
        RegisterUserRequest request = request("andrea@example.com", LocalDate.of(2000, 1, 1));
        UserResponse response = new UserResponse(1L, "andrea", "andrea@example.com", true,
                "Andrea", "Lopez", null, null, request.birthDate());
        when(userRepository.existsByUsername(request.username())).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toResponse(any(User.class))).thenReturn(response);

        assertThat(service.register(request)).isEqualTo(response);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getActive()).isTrue();
        assertThat(saved.getUserProfile()).isNotNull();
        assertThat(saved.getUserProfile().getUser()).isSameAs(saved);
        verify(userMapper).toResponse(saved);
    }

    @Test
    void register_duplicateUsername_throwsAndDoesNotSave() {
        RegisterUserRequest request = request("andrea@example.com", LocalDate.of(2000, 1, 1));
        when(userRepository.existsByUsername("andrea")).thenReturn(true);
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_duplicateEmailIgnoringCase_throwsAndDoesNotSave() {
        RegisterUserRequest request = request("ANDREA@example.com", LocalDate.of(2000, 1, 1));
        when(userRepository.existsByUsername("andrea")).thenReturn(false);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_futureBirthDate_throwsAndDoesNotSave() {
        RegisterUserRequest request = request("andrea@example.com", LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> service.register(request)).isInstanceOf(BusinessRuleException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    private RegisterUserRequest request(String email, LocalDate birthDate) {
        return new RegisterUserRequest("andrea", email, "Andrea", "Lopez", null, null, birthDate);
    }
}
