package com.afalenkin.webfluxnotes.config;

import com.afalenkin.webfluxnotes.service.ApplicationUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 * <p>
 * Без использования WebFlux в конфигурации приходилось наследоваться от WebSecurityConfigurerAdapter
 * чтобы настроить аутентификацию и авторизацию.
 * <p>
 * С использованием WebFlux это можно сделать по-другому: с помощью EnableWebFluxSecurity
 */
@EnableWebFluxSecurity

// Позволяет управлять доступом к методам с помощью специальных аннотаций
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        //@formatter:off
        return httpSecurity
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/users/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/users/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/users/**").hasRole("USER")
                .pathMatchers("/swagger-ui.html",
                        "/swagger-ui.html/**",
                        "v3/api-docs/*",
                        "/webjars/**").permitAll()
                .anyExchange().authenticated()
                .and().formLogin()
                .and().httpBasic()
                .and().build();
        //@formatter:on
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(ApplicationUserDetailsService userService) {
        return new UserDetailsRepositoryReactiveAuthenticationManager(userService);
    }
}
