package com.project.gameVal.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.gameVal.common.JWT.Service.LogoutAccessTokenService;
import com.project.gameVal.common.JWT.Service.RefreshTokenService;
import com.project.gameVal.common.JWT.auth.JWTAuthenticationFilter;
import com.project.gameVal.common.JWT.auth.JWTAuthorizationFilter;
import com.project.gameVal.common.JWT.auth.JWTUtil;
import com.project.gameVal.web.probability.service.GameCompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final CorsConfigurationSource corsConfigurationSource;
    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final GameCompanyService gameCompanyService;
    private final LogoutAccessTokenService logoutAccessTokenService;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource)
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(
                        Customizer.withDefaults())//.formLogin(AbstractHttpConfigurer::disable) //TODO 테스트용 form login
                .sessionManagement(
                        (sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .logout(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        JWTAuthenticationFilter.builder()
                                .authenticationManager(authenticationManager)
                                .objectMapper(objectMapper)
                                .jwtUtil(jwtUtil)
                                .refreshTokenService(refreshTokenService)
                                .gameCompanyService(gameCompanyService)
                                .build(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(
                        new JWTAuthorizationFilter(jwtUtil, gameCompanyService, logoutAccessTokenService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
