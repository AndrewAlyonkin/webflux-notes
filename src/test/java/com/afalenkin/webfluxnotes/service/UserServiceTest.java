package com.afalenkin.webfluxnotes.service;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
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

import java.util.List;
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
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UsersRepository repository;

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

    /**
     * BDDMockito - наследник стандартного Mockito и обладает тем же функционалом, но предоставляет
     * более понятный и читаемый способ формирования выражений.
     */
    @Test
    @DisplayName("getAll should return a flux of users")
    void getAllTest() {
        BDDMockito.when(repository.findAll()).thenReturn(Flux.just(createdUser()));

        StepVerifier.create(userService.getAll())
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("getById should return a Mono with one user")
    void getByIdTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));

        StepVerifier.create(userService.getById(1))
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    void getByIdNotFoundTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(2))).thenReturn(Mono.empty());

        StepVerifier.create(userService.getById(2))
                .expectSubscription()
                .expectNextCount(0) // Проверка что не вызывался onNext() - т.е. Моно пустой
                .verifyComplete();
    }

    @Test
    @DisplayName("save creates new user")
    void saveTest() {
        BDDMockito.when(repository.save(newUser())).thenReturn(Mono.just(createdUser()));

        StepVerifier.create(userService.save(newUser()))
                .expectSubscription()
                .expectNext(createdUser())
                .verifyComplete();
    }

    @Test
    @DisplayName("delete user by ID")
    void deleteTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.delete(ArgumentMatchers.any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.delete(1))
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete user by ID if user does not exists")
    void deleteNotExistsTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());
        BDDMockito.verify(repository, Mockito.never()).delete(ArgumentMatchers.any());

        StepVerifier.create(userService.delete(2))
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    @DisplayName("update should update user if it exists")
    void updateTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.save(ArgumentMatchers.any(User.class))).thenReturn(Mono.just(updatedUser()));

        StepVerifier.create(userService.update(updatedUser()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    @DisplayName("updatedo nothing if user does not exists")
    void updateNotExistsTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());
        BDDMockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any());

        StepVerifier.create(userService.update(updatedUser()))
                .expectSubscription()
                .verifyComplete();
    }

    @Test
    void saveBatchTest() {
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(createdUser(), createdUser()));

        StepVerifier.create(userService.save(List.of(newUser(), newUser())))
                .expectSubscription()
                .expectNext(createdUser(), createdUser())
                .verifyComplete();
    }

    @Test
    void saveBatchFailedTest() {
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(createdUser(), createdUser().withName("")));

        StepVerifier.create(userService.save(List.of(newUser(), newUser())))
                .expectSubscription()
                .expectNext(createdUser())
                .expectError(ResponseStatusException.class)
                .verify();
    }

}