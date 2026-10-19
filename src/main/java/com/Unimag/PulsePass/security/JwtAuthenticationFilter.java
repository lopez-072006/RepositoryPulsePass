package com.Unimag.PulsePass.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService users;
    private final RestAuthenticationEntryPoint entryPoint;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService users,
                                   RestAuthenticationEntryPoint entryPoint) {
        this.jwtService = jwtService;
        this.users = users;
        this.entryPoint = entryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Enumeration<String> values = request.getHeaders("Authorization");
        int count = values == null ? 0 : Collections.list(values).size();
        String header = request.getHeader("Authorization");
        if (count > 1 || (header != null && header.regionMatches(true, 0, "Bearer", 0, 6)
                && (header.length() <= 7 || !header.regionMatches(true, 6, " ", 0, 1)))) {
            reject(request, response);
            return;
        }
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                reject(request, response);
                return;
            }
            try {
                String token = header.substring(7).trim();
                if (token.isEmpty() || token.contains(" ")) throw new JwtException("Malformed Bearer token.");
                SecurityPrincipal principal = users.loadById(jwtService.parseUserId(token));
                if (!principal.isEnabled()) throw new UsernameNotFoundException("User is unavailable.");
                var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException exception) {
                SecurityContextHolder.clearContext();
                reject(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        entryPoint.commence(request, response, new BadCredentialsException("Invalid authentication."));
    }
}
