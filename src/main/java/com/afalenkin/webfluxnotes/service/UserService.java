package com.afalenkin.webfluxnotes.service;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UsersRepository repository;

    public Flux<User> getAll() {
        return repository.findAll();
    }

    public Mono<User> getById(int id) {
        return repository.findById(id);
    }

    public Mono<User> save(User newUser) {
        return repository.save(newUser);
    }

    public Mono<Void> update(User updatedUser) {
        return getById(updatedUser.getId())
                .flatMap(userFromDb -> repository.save(updatedUser))
                .then();
    }

    public Mono<Void> delete(int id) {
        return getById(id).flatMap(repository::delete);
    }
}
