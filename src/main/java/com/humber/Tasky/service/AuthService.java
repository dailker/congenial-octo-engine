package com.humber.Tasky.service;

import com.humber.Tasky.model.User;
import com.humber.Tasky.repository.UserRepository;
import com.humber.Tasky.security.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtUtil;
    private final SecurityContextRepository securityContextRepository;

    public AuthService(UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       JwtTokenUtil jwtUtil,
                       PasswordEncoder passwordEncoder,
                       SecurityContextRepository securityContextRepository) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
    }

    public String registerUser(User user) {
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new RuntimeException("Full name is required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    public String loginUser(String email, String password,
                            HttpServletRequest request,
                            HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password)
        );

        if (authentication.isAuthenticated()) {
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // Extract UserDetails from Authentication
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // Generate and return the JWT token
            return jwtUtil.generateToken(userDetails);
        } else {
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}