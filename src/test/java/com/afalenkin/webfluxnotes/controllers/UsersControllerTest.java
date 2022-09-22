package com.afalenkin.webfluxnotes.controllers;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.afalenkin.webfluxnotes.util.UserData.createdUser;
import static com.afalenkin.webfluxnotes.util.UserData.newUser;
import static com.afalenkin.webfluxnotes.util.UserData.updatedUser;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@ExtendWith(SpringExtension.class)
class UsersControllerTest {

    @InjectMocks
    private UsersController controller;

    @Mock
    private UserService service;

    @BeforeAll
    static void blockHoundSetup() {
        BlockHound.install();
    }

    @Test
    void blockHoundWorks() throws InterruptedException, TimeoutException {
        try {
            FutureTask<?> task = new FutureTask<>(() -> {
                Thread.sleep(0);  // NOSONAR
                return "";
            });
            Schedulers.parallel().schedule(task);

            task.get(10, TimeUnit.SECONDS);
            Assertions.fail("should fail");
        } catch (ExecutionException e) {
            Assertions.assertTrue(e.getCause() instanceof BlockingOperationError);
        }
    }

    @Test
    @DisplayName("getAll should return a flux of users")
    void getAllTest() {
        BDDMockito.when(service.getAll()).thenReturn(Flux.just(createdUser()));

        StepVerifier.create(controller.getAllUsers())
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("getById should return a Mono with one user")
    void getByIdTest() {
        BDDMockito.when(service.getById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));

        StepVerifier.create(controller.getById(1))
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    void getByIdNotFoundTest() {
        BDDMockito.when(service.getById(ArgumentMatchers.eq(2))).thenReturn(Mono.empty());

        StepVerifier.create(controller.getById(2))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("save creates new user")
    void saveTest() {
        BDDMockito.when(service.save(newUser())).thenReturn(Mono.just(createdUser()));

        StepVerifier.create(controller.save(newUser()))
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("saving user with ID should be failed")
    void saveWithIdTest() {
        StepVerifier.create(controller.save(createdUser()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }

    @Test
    @DisplayName("delete user by ID")
    void deleteTest() {
        BDDMockito.when(service.delete(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

        StepVerifier.create(controller.delete(1))
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("update should update user if it exists")
    void updateTest() {
        BDDMockito.when(service.update(ArgumentMatchers.any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(controller.update(updatedUser()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("update should be failed if id is absent")
    void updateWitNullableIdTest() {
        BDDMockito.verify(service, Mockito.never()).update(ArgumentMatchers.any());

        StepVerifier.create(controller.update(newUser()))
                .expectSubscription()
                .expectError(ResponseStatusException.class)
                .verify();
    }
}