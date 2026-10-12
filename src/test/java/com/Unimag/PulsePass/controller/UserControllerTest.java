package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.dto.request.RegisterUserRequest;
import com.Unimag.PulsePass.dto.response.UserResponse;
import com.Unimag.PulsePass.exception.DuplicateResourceException;
import com.Unimag.PulsePass.service.UserService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void register_validRequest_returns201() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenReturn(userResponse());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "andrea",
                                  "email": "andrea@example.com",
                                  "firstName": "Andrea",
                                  "lastName": "Garcia",
                                  "phone": "3001234567",
                                  "city": "Santa Marta",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email")
                        .value("andrea@example.com"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "andrea",
                                  "email": "correo-invalido",
                                  "firstName": "Andrea",
                                  "lastName": "Garcia",
                                  "phone": "3001234567",
                                  "city": "Santa Marta",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Validation failed"))
                .andExpect(jsonPath("$.details.email").exists());

        verify(userService, never())
                .register(any(RegisterUserRequest.class));
    }

    @Test
    void register_duplicateUsername_returns409() throws Exception {
        when(userService.register(any(RegisterUserRequest.class)))
                .thenThrow(new DuplicateResourceException(
                        "Username already exists: andrea"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "andrea",
                                  "email": "andrea@example.com",
                                  "firstName": "Andrea",
                                  "lastName": "Garcia",
                                  "phone": "3001234567",
                                  "city": "Santa Marta",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Username already exists: andrea"));

        verify(userService).register(any(RegisterUserRequest.class));
    }

    @Test
    void findByEmail_existingUser_returns200() throws Exception {
        when(userService.findByEmail("andrea@example.com"))
                .thenReturn(userResponse());

        mockMvc.perform(get("/api/users/by-email")
                        .param("email", "andrea@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.email")
                        .value("andrea@example.com"));

        verify(userService).findByEmail("andrea@example.com");
    }

    @Test
    void findByUsername_existingUser_returns200() throws Exception {
        when(userService.findByUsername("andrea"))
                .thenReturn(userResponse());

        mockMvc.perform(get("/api/users/by-username")
                        .param("username", "andrea"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value("andrea"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService).findByUsername("andrea");
    }

    private UserResponse userResponse() {
        return new UserResponse(
                1L,
                "andrea",
                "andrea@example.com",
                true,
                "Andrea",
                "Garcia",
                "3001234567",
                "Santa Marta",
                LocalDate.of(1990, 1, 1)
        );
    }
}
