package com.afalenkin.webfluxnotes.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 * <p>
 * WebFlux имеет собственную реализацию AbstractErrorWebExceptionHandler, которая обрабатывает исключения.
 * Этот хэндлер имеет более высокий приоритет и заменит хэндлер вебфлакса.
 */
@Component
@Order(-2)
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
    }

    /**
     * Если был пойман запрос с ошибкой - перенаправить его в обработчик
     */
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), // какие запросы будут обрабатываться в обработчике исключений
                this::formatErrorResponse // какой обработчик будет обрабатывать запросы с ошибками
        );
    }

    /**
     * Сформировать ответ для запроса, при обработке которого возникло исключение
     */
    private Mono<ServerResponse> formatErrorResponse(ServerRequest request) {
        // запрос, при обработке которого возникло исключение, уже содержит в себе его детали.
        // Они содержатся в нем в виде мапы
        Map<String, Object> errorAttributes = getErrorAttributes(request,
                ErrorAttributeOptions.of(ErrorAttributeOptions.Include.STACK_TRACE));

        // статус ответа можно получить из деталей, полученных из запроса. Если его там нет - вернем дефолтное значение
        int status = (int) Optional.ofNullable(errorAttributes.get("status")).orElse(500);

        return ServerResponse
                .status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorAttributes));

    }
}
