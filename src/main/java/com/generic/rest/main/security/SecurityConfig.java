package com.generic.rest.main.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.generic.rest.main.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, TokenAuthenticationFilter tokenAuthenticationFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(HttpMethod.POST, "/auth/signup", "/auth/login", "/auth/google-login").permitAll()
                .requestMatchers(HttpMethod.GET, "/product/list", "/product/show/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/auth/role", "/auth/account-menu").authenticated()

                // Product management - ADMIN only
                .requestMatchers(HttpMethod.POST, "/product/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/product/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/product/**").hasRole("ADMIN")

                // Collection management - ADMIN only
                .requestMatchers("/collection/**").hasRole("ADMIN")

                // Order management
                .requestMatchers(HttpMethod.POST, "/order/create").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/order/list/*").hasRole("ADMIN") // /order/list/{userId}
                .requestMatchers(HttpMethod.GET, "/order/list").authenticated() // User's own orders

                // All other authenticated requests
                .anyRequest().authenticated()
            );
        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        return new TokenAuthenticationFilter(jwtService, userRepository);
    }
}


