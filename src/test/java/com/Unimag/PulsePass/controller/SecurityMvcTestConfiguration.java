package com.Unimag.PulsePass.controller;

import com.Unimag.PulsePass.security.CustomUserDetailsService;
import com.Unimag.PulsePass.security.JwtAuthenticationFilter;
import com.Unimag.PulsePass.security.RestAccessDeniedHandler;
import com.Unimag.PulsePass.security.RestAuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.io.IOException;
import static org.mockito.Mockito.mock;

@TestConfiguration(proxyBeanMethods = false)
class SecurityMvcTestConfiguration {
    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(null, null, null) {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {
                chain.doFilter(request, response);
            }
        };
    }

    @Bean CustomUserDetailsService customUserDetailsService() { return mock(CustomUserDetailsService.class); }
    @Bean RestAuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper mapper) {
        return new RestAuthenticationEntryPoint(mapper);
    }
    @Bean RestAccessDeniedHandler restAccessDeniedHandler(ObjectMapper mapper) {
        return new RestAccessDeniedHandler(mapper);
    }
}
