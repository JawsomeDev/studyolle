package com.studyolle.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests( (auth) ->
                auth.requestMatchers("/", "/login", "/sign-up", "/check-email", "/check-email-token",
                        "/email-login" , "/check-email-login", "/login-link", "/static/**", "/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/profile/*").permitAll()
                        .anyRequest().authenticated());

        http.formLogin((a) -> a.loginPage("/login").permitAll() .defaultSuccessUrl("/"));

        http.logout((a) -> a.logoutSuccessUrl("/"));

        return http.build();
    }
}
