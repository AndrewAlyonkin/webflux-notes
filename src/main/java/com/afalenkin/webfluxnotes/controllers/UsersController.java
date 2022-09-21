package com.afalenkin.webfluxnotes.controllers;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import com.afalenkin.webfluxnotes.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

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
}
