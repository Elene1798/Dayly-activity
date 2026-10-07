package com.example.dailyactivity;

import com.example.dailyactivity.model.User;
import com.example.dailyactivity.repository.UserRepository;
import com.example.dailyactivity.service.AchievementCheckerService;
import com.example.dailyactivity.service.UserVisitService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Value("${ADMIN_USERNAME:admin}")
    private String adminUsername;

    @Value("${ADMIN_PASSWORD:admin}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return username -> {

            // Администратор остаётся отдельным аккаунтом
            if (username.equals(adminUsername)) {
                return org.springframework.security.core.userdetails.User
                        .builder()
                        .username(adminUsername)
                        .password(passwordEncoder.encode(adminPassword))
                        .roles("ADMIN")
                        .build();
            }

            // Обычные пользователи берутся из PostgreSQL
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(
                                    "Пользователь не найден"
                            )
                    );

            return org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .roles(user.getRole())
                    .build();
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserRepository userRepository,
            UserVisitService userVisitService,
            AchievementCheckerService achievementCheckerService) throws Exception {

        AuthenticationSuccessHandler successHandler =
                (request, response, authentication) -> {

                    String username = authentication.getName();

                    userRepository.findByUsername(username)
                            .ifPresent(user -> {

                                userVisitService.recordVisit(user);

                                achievementCheckerService
                                        .checkVisitAchievements(user);
                            });

                    response.sendRedirect("/section");
                };

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/activity",
                                "/css/**",
                                "/login",
                                "/register",
                                "/register/**"
                        ).permitAll()

                        .requestMatchers("/favorites").authenticated()
                        .requestMatchers("/favorites/**").authenticated()
                        .requestMatchers("/section").authenticated()
                        .requestMatchers("/achievements").authenticated()

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .permitAll()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler)
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .key("daily-activity-remember-me")
                        .tokenValiditySeconds(60 * 60 * 24 * 30)
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}