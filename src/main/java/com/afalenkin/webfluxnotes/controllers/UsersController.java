package com.afalenkin.webfluxnotes.controllers;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import com.afalenkin.webfluxnotes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@RestController
@RequestMapping("users")
@Slf4j
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    /**
     * Мы не вызываем вручную метод subscribe а просто отдаем Flux в методе контроллера.
     * Спринг за нас создаст подписку и реактивно вернет результаты запроса из метода контроллера.
     */
    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.getAll();
    }

    /**
     * Если в БД отсутствует запись с искомым идентификатором - этот метод вернет пустой моно и статус ответа 200.
     * Нам нужно чтобы в таком случае выбрасывалось исключение и возвращался ответ со статусом 400.
     * Для этого используется switchIfEmpty, который в случае, если из сервиса вернулся пустой моно -
     * создаст новый моно который содержит ошибку и отдаст его.
     */
    @GetMapping(path = "/{id}")
    public Mono<User> getById(@PathVariable(value = "id", required = true) int id) {
        return userService.getById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST)))
                .log();
    }
}
