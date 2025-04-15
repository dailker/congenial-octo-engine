package com.humber.Tasky.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // Disable CSRF for API endpoints
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) // Enable CSRF for non-API endpoints
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", 
                    "/login", 
                    "/register", 
                    "/logout", 
                    "/css/**", 
                    "/js/**", 
                    "/images/**", 
                    "/error", 
                    "/favicon.ico"
                ).permitAll() // Publicly accessible endpoints
                .requestMatchers("/api/auth/**", "/api/users/friends/accept/**", "/api/users/friends/reject/**").permitAll() // Allow specific API endpoints
                .requestMatchers(
                    "/profile/**", 
                    "/profile", 
                    "/edit-profile",
                    "/tasks/**",
                    "/dashboard/**"
                ).authenticated() // Require authentication for these endpoints
                .anyRequest().authenticated()) // All other endpoints require authentication
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/tasks", true) // Redirect to tasks page after successful login
                .usernameParameter("email")
                .failureUrl("/login?error=true")
                .permitAll())
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll())
            .userDetailsService(customUserDetailsService)
            .sessionManagement(session -> session
                .invalidSessionUrl("/login?invalid-session")
                .maximumSessions(1) // Limit to one session per user
                .expiredUrl("/login?session-expired"))
            .headers(headers -> headers
                .frameOptions().sameOrigin() // Allow frames from the same origin
                .httpStrictTransportSecurity().disable()); // Disable HSTS for development purposes

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }
}