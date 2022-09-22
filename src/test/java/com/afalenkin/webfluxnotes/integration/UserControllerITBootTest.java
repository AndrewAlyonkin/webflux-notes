package com.afalenkin.webfluxnotes.integration;

import com.afalenkin.webfluxnotes.domain.User;
import com.afalenkin.webfluxnotes.repository.UsersRepository;
import com.afalenkin.webfluxnotes.util.WebTestClientUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
 * Запуск тестов в полном контексте спринга
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class UserControllerITBootTest {

    @Autowired
    private WebTestClientUtils testClientUtils;

    @MockBean
    private UsersRepository repository;

    private WebTestClient userClient;
    private WebTestClient adminClient;
    private WebTestClient gangsterClient;

    @BeforeAll
    static void blockHoundSetup() {
        BlockHound.install();
    }

    @BeforeEach
    void securitySetUp() {
        userClient = testClientUtils.authenticateClient("dog", "root");
        adminClient = testClientUtils.authenticateClient("god", "root");
        gangsterClient = testClientUtils.authenticateClient("x", "y");
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
        User user = createdUser();
        BDDMockito.when(repository.findAll()).thenReturn(Flux.just(user));

        adminClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody()
                .jsonPath("$.[0].id").isEqualTo(user.getId())
                .jsonPath("$.[0].name").isEqualTo(user.getName());
    }

    @Test
    void getAllGangsterTest() {
        gangsterClient
                .get()
                .uri("/users")
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody();
    }

    @Test
    @DisplayName("getAll should return a flux of users")
//    @WithMockUser("god")         лучше использовать такой способ
    void getAllListTest() {
        User user = createdUser();
        BDDMockito.when(repository.findAll()).thenReturn(Flux.just(user));

        adminClient
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
    void getByIdTest() {
        User user = createdUser();
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(user));

        userClient
                .get()
                .uri("/users/{id}", 1)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(user);
    }

    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    void getByIdNotFoundTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.anyInt())).thenReturn(Mono.empty());

        userClient
                .get()
                .uri("/users/{id}", 2)
                .exchange()
                .expectStatus().is4xxClientError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.developer").isEqualTo("Alenkin Andrew");
    }

    @Test
    @DisplayName("save creates new user")
    void saveTest() {
        BDDMockito.when(repository.save(newUser())).thenReturn(Mono.just(createdUser()));

        adminClient
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
    void saveWithIdTest() {
        userClient
                .post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser()))
                .exchange()
                .expectStatus().is4xxClientError();
    }

    @Test
    @DisplayName("saving invalid user should be failed")
    void saveInvalidTest() {

        User invalid = newUser().withName("");

        adminClient
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

    @Test
    @DisplayName("getById should return a empty Mono if user not exists")
    void deleteTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.delete(ArgumentMatchers.any())).thenReturn(Mono.empty());

        adminClient
                .delete()
                .uri("/users/{id}", 1)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("update should update user if it exists")
    void updateTest() {
        BDDMockito.when(repository.findById(ArgumentMatchers.eq(1))).thenReturn(Mono.just(createdUser()));
        BDDMockito.when(repository.save(ArgumentMatchers.any(User.class))).thenReturn(Mono.just(createdUser()));

        adminClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser()))
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    @DisplayName("update should be failed if id is absent")
    void updateWitNullableIdTest() {
        BDDMockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any());

        adminClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(newUser()))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("update should be failed if id is absent")
    void updateInvalidTest() {
        BDDMockito.verify(repository, Mockito.never()).save(ArgumentMatchers.any());

        adminClient
                .put()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(createdUser().withName("")))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void saveBatchTest() {
        User user = createdUser();
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(user, user));

        adminClient
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

    @Test
    void saveBatchFailedTest() {
        User user = createdUser();
        BDDMockito.when(repository.saveAll(List.of(newUser(), newUser())))
                .thenReturn(Flux.just(user, user.withName("")));

        adminClient
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
