package com.studyolle.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import javax.sql.DataSource;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
        http.authorizeHttpRequests( auth->
                auth.requestMatchers("/","/login",  "/sign-up","/check-email-token","/login-by-email",
                        "/email-login" , "/check-email-login").permitAll()
                        .requestMatchers(HttpMethod.GET,"/profile/*").hasRole("USER")
                        .anyRequest().authenticated());
        http.formLogin(form -> form
                                .loginPage("/login")
                        .defaultSuccessUrl("/")
                        .usernameParameter("email").permitAll() //
                 );
        http.logout(logout -> logout.logoutSuccessUrl("/"));

        http.rememberMe(key -> key.userDetailsService(userDetailsService)
                .tokenRepository(tokenRepository()));
        return http.build();
    }

    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    @Bean
    protected WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/static/**", "/images/**", "/css/**", "/js/**", "/node_modules/**");
    }
}
