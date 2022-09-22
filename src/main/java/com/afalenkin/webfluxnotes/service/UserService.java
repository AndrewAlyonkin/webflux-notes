package com.afalenkin.webfluxnotes.service;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
        if (newUser.getId() != null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object should have nullable ID."));
        }
        return repository.save(newUser);
    }

    @Transactional
    public Flux<User> save(List<User> users) {
        return repository.saveAll(users)
                .doOnNext(this::validate);
    }

    private void validate(User user) {
        if (user.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid user for update " + user.getId());
        }
    }

    public Mono<Void> update(User updatedUser) {
        return getById(updatedUser.getId())
                .flatMap(userFromDb -> repository.save(updatedUser))
                .then();
    }

    public Mono<Void> delete(int id) {
        return getById(id)
                .flatMap(repository::delete);
    }
}
