package com.ecda.platform.config

import com.ecda.platform.security.JwtAuthFilter
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(private val jwtAuthFilter: JwtAuthFilter) {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOrigins = listOf("http://localhost:5173", "http://localhost:5175")
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
        }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/api/**", config)
        }
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain = http
        .cors { it.configurationSource(corsConfigurationSource()) }
        .csrf { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it.requestMatchers("/error").permitAll()
            it.requestMatchers("/api/auth/**").permitAll()
            it.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            it.requestMatchers(HttpMethod.GET, "/api/centres/*/waivers").hasRole("ECDA_OFFICER")
            it.requestMatchers(HttpMethod.GET, "/api/centres/**").hasAnyRole("ECDA_OFFICER", "HQ_ADMIN", "CENTRE_LEADER")
            it.requestMatchers(HttpMethod.POST, "/api/centres/**").hasAnyRole("ECDA_OFFICER", "HQ_ADMIN")
            it.requestMatchers(HttpMethod.PUT, "/api/centres/**").hasAnyRole("ECDA_OFFICER", "HQ_ADMIN")
            it.requestMatchers(HttpMethod.PATCH, "/api/centres/**").hasAnyRole("ECDA_OFFICER", "HQ_ADMIN")
            it.anyRequest().authenticated()
        }
        .exceptionHandling {
            it.authenticationEntryPoint { _, response, _ ->
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
            }
        }
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
