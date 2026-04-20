package com.lending.aireview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // enables @PreAuthorize on controllers
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())   // disabled for POC REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/review/**").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});        // Basic Auth for POC (replace with JWT in prod)

        return http.build();
    }

    /**
     * In-memory users for POC.
     * Production: integrate with existing LDAP/OAuth2 identity provider.
     */
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var officer = User.builder()
            .username("officer1")
            .password(encoder.encode("password"))
            .roles("LOAN_OFFICER")
            .build();

        var underwriter = User.builder()
            .username("underwriter1")
            .password(encoder.encode("password"))
            .roles("UNDERWRITER")
            .build();

        return new InMemoryUserDetailsManager(officer, underwriter);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Allow Angular dev server (localhost:4200) to call this service.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}