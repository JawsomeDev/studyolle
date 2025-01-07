package com.studyolle.config;


import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
        http.authorizeHttpRequests( auth->
                auth.requestMatchers("/", "/login", "/sign-up","/check-email-token",
                        "/email-login" , "/check-email-login", "/login-link").permitAll()
                        .requestMatchers(HttpMethod.GET,"/profile/*").hasRole("USER")
                        .anyRequest().authenticated());
        http.formLogin(form -> form
                                .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .loginProcessingUrl("/login").permitAll() //
                 );
        http.sessionManagement(session -> session
                .sessionFixation().migrateSession()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
        );
        http.logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }

    @Bean
    protected WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/node_modules/**", "/jdenticon/**");
    }
}
