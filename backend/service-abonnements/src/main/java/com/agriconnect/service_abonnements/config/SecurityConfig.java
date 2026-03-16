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
 *   - GET  /api/abonnements/categories         → liste des offres
 *   - POST /api/abonnements/webhook-notchpay   → callback NotchPay
 *     (NotchPay appelle ce endpoint sans JWT)
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
                        // Publique — pas de JWT requis
                        .requestMatchers("/api/abonnements/categories").permitAll()
                        // Webhook NotchPay — appelé par NotchPay sans JWT
                        .requestMatchers("/api/abonnements/webhook-notchpay").permitAll()
                        // Tout le reste — JWT obligatoire
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}