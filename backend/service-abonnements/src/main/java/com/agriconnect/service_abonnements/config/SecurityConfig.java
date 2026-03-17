package com.agriconnect.service_abonnements.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * ============================================================
 * SecurityConfig — Configuration Spring Security
 * ============================================================
 * Routes publiques  :
 *   - GET  /api/abonnements/categories
 *   - POST /api/abonnements/webhook-notchpay  → webhook POST
 *   - GET  /api/abonnements/webhook-notchpay  → callback GET (redirect navigateur)
 *
 * Routes protégées  : tout le reste (JWT requis)
 * ============================================================
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/abonnements/categories").permitAll()
                        // GET et POST tous les deux publics pour NotchPay
                        .requestMatchers("/api/abonnements/webhook-notchpay").permitAll()
                        .requestMatchers("/api/abonnements/callback-notchpay").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}