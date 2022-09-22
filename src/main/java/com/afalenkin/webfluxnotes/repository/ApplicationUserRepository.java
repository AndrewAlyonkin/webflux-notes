package com.afalenkin.webfluxnotes.repository;

import com.afalenkin.webfluxnotes.domain.ApplicationUser;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
public interface ApplicationUserRepository extends ReactiveCrudRepository<ApplicationUser, Integer> {

    Mono<ApplicationUser> findByUsername(String username);
}
