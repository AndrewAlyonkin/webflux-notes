package com.afalenkin.webfluxnotes.integration;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.exception.CustomAttributes;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import com.afalenkin.webfluxnotes.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.afalenkin.webfluxnotes.util.UserData.createdUser;
import static com.afalenkin.webfluxnotes.util.UserData.newUser;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 * <p>
 * Использование @WebFluxTest отключает основную конфигурацию, будет сконфигурирован только тот контекст,
 * который необходим для работы WebFlux.
 * При этом не происходит сканирование пакетов, нужные для теста классы нужно импортировать.
 */
@ExtendWith(SpringExtension.class)
@WebFluxTest
@Import({UserService.class,
        CustomAttributes.class})
class UserControllerITTest {

    @MockBean
    private UsersRepository repository;

    @Autowired
    private WebTestClient testClient;

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
    @WithMockUser("dog")
    void getAllTest() {
        User user = createdUser();
        BDDMockito.when(repository.findAll()).thenReturn(Flux.just(user));

        testClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(user.getId())
                .jsonPath("$.[0].name").isEqualTo(user.getName());
    }

    @Test
    @DisplayName("getAll should return a flux of users")
    @WithMockUser()
    void getAllListTest() {
        User user = createdUser();
        BDDMockito.when(repository.findAll()).thenReturn(Flux.just(user));

        testClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1)
                .contains(user);
    }

    @Test
    @DisplayName("getById should return a Mono with one user")
    @WithMockUser()
    void getByIdTest() {
        User user = createdUser();
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(user));

        testClient
                .get()
                .uri("/users/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(user);
    }

    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    @WithMockUser()
    void getByIdNotFoundTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

        testClient
                .get()
                .uri("/users/{id}", 2)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.developer").isEqualTo("Alenkin Andrew");
    }

    @Disabled
    @Test
    @DisplayName("save creates new user")
    @WithMockUser()
    void saveTest() {
        BDDMockito.when(repository.save(newUser())).thenReturn(Mono.just(createdUser()));

        testClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser()))
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody(User.class)
                .isEqualTo(createdUser());
    }

    @Test
    @DisplayName("saving user with ID should be failed")
    @WithMockUser()
    void saveWithIdTest() {
        testClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser()))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Disabled
    @Test
    @DisplayName("saving invalid user should be failed")
    @WithMockUser()
    void saveInvalidTest() {

        User invalid = newUser().withName("");

        testClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(invalid))
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.developer").isEqualTo("Alenkin Andrew");
    }

    @Disabled
    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    @WithMockUser()
    void deleteTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.delete(ArgumentMatchers.any())).thenReturn(Mono.empty());

        testClient
                .delete()
                .uri("/users/{id}", 1)
                .exchange()
                .expectStatus().isOk();
    }

    @Disabled
    @Test
    @DisplayName("update should update user if it exists")
    @WithMockUser()
    void updateTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.save(ArgumentMatchers.any(User.class))).thenReturn(Mono.just(createdUser()));

        testClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser()))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Disabled
    @Test
    @DisplayName("update should be failed if id is absent")
    @WithMockUser()
    void updateWitNullableIdTest() {
        BDDMockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any());

        testClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Disabled
    @Test
    @DisplayName("update should be failed if id is absent")
    @WithMockUser(authorities = "ROLE_ADMIN")
    void updateInvalidTest() {
        BDDMockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any());

        testClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser().withName("")))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Disabled
    @Test
    @WithMockUser()
    void saveBatchTest() {
        User user = createdUser();
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(user, user));

        testClient
                .post()
                .uri("/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(newUser(), newUser())))
                .exchange()
                .expectStatus().isCreated()
                .expectBodyList(User.class)
                .hasSize(2)
                .contains(user, user);
    }

    @Disabled
    @Test
    @WithMockUser()
    void saveBatchFailedTest() {
        User user = createdUser();
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(user, user.withName("")));

        testClient
                .post()
                .uri("/users/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(List.of(newUser(), newUser())))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBodyList(User.class)
                .hasSize(1);
    }

}
