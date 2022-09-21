package com.afalenkin.webfluxnotes.repository;

import com.afalenkin.webfluxnotes.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 *
 * Spring data jpa не позволяет работать в реактивном стиле
 */
public interface UsersRepository extends ReactiveCrudRepository<User, Integer> {

    Mono<User> findById(int id);
}
