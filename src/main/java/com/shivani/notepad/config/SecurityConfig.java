package com.shivani.notepad.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Central security configuration for the Notepad application.
 *
 * Responsibilities:
 *  - Defines which URLs are publicly accessible vs which require authentication.
 *  - Configures the login/logout flow using Spring Security's form login.
 *  - Configures "Remember Me" persistent login via a signed cookie.
 *  - Registers the password encoder bean (BCrypt) used across the app for
 *    hashing and verifying user passwords.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;

    @Autowired
    public SecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // NOTE: CSRF disabled for development simplicity — see Step 4's
                // original note. A production-grade app would keep this enabled
                // and use Thymeleaf's built-in CSRF token support instead.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/reset-password",
                                "/verify-email",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )

                .rememberMe(remember -> remember
                        .key("notepad-remember-me-key") // used to sign the remember-me cookie
                        .tokenValiditySeconds(14 * 24 * 60 * 60) // 14 days
                        .userDetailsService(userDetailsService)
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                );

        return http.build();
    }

    /**
     * Registers a BCrypt password encoder as a Spring bean, used
     * throughout the app wherever passwords need to be hashed or verified.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}