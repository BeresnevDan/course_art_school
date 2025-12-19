package ru.coursework.artschool.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // публичные страницы (доступны всем, даже без логина)
                        .requestMatchers(
                                "/", "/about",
                                "/teachers", "/teachers/**",
                                "/exhibitions", "/exhibitions/**",
                                "/lessons",
                                "/login",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers(
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()



                        // регистрация — только для администратора
                        .requestMatchers("/register").hasRole("ADMIN")

                        // ученики: просмотр Teacher + Admin
                        .requestMatchers("/students/**").hasAnyRole("TEACHER", "ADMIN")

                        // журнал — только Teacher
                        .requestMatchers("/journal/**", "/grades/**").hasAnyRole("TEACHER", "ADMIN")

                        // дисциплины, группы, отчёты — только Admin
                        .requestMatchers("/subjects/**", "/groups/**", "/reports/**").hasRole("ADMIN")

                        // админка — только Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // всё остальное — только после логина
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
