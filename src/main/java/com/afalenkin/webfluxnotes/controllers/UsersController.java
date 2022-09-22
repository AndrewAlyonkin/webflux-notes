package com.afalenkin.webfluxnotes.controllers;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.List;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@RestController
@RequestMapping("users")
@Slf4j
@RequiredArgsConstructor
@SecurityScheme(
        name = "Basic Authentication",
        type = SecuritySchemeType.HTTP,
        scheme = "basic"
)
public class UsersController {

    private final UserService userService;

    /**
     * Мы не вызываем вручную метод subscribe а просто отдаем Flux в методе контроллера.
     * Спринг за нас создаст подписку и реактивно вернет результаты запроса из метода контроллера.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "get all users from storage",
            tags = {"users"},
            security = @SecurityRequirement(name = "Basic Authentication"))
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
    @ResponseStatus(HttpStatus.OK)
    @Operation(tags = {"users"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<User> getById(@PathVariable(value = "id", required = true) int id) {
        return userService.getById(id)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ooops, something went wrong..."))
                )
                .log();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = {"users"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<User> save(@Valid @RequestBody User user) {
        if (user.getId() != null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object should have nullable ID."));
        }
        return userService.save(user);
    }

    @PostMapping(path = "/batch")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(tags = {"users"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Flux<User> batchSave(@RequestBody List<User> users) {
        return userService.save(users);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(tags = {"users"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> update(@Valid @RequestBody User user) {
        if (user.getId() == null) {
            return Mono.error(
                    new ResponseStatusException(HttpStatus.BAD_REQUEST, "Object should have ID."));
        }
        return userService.update(user);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(tags = {"users"}, security = @SecurityRequirement(name = "Basic Authentication"))
    public Mono<Void> delete(@PathVariable(value = "id", required = true) int id) {
        return userService.delete(id);
    }
}
