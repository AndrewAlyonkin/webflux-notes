package com.afalenkin.webfluxnotes.service;

import com.afalenkin.webfluxnotes.repository.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@Service
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements ReactiveUserDetailsService {

    @Autowired
    private ApplicationUserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .cast(UserDetails.class);
    }
}
