package com.afalenkin.webfluxnotes.exception;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 * <p>
 * Стандартные ErrorAttributes позволяют отдавать ограниченный набор полей.
 * Чтобы его расширить - можно создать наследника класса, в котором определить дополнительные атрибуты.
 * Достаточно будет просто поместить кастомный обработчик атрибутов в контекст спринга, чтобы он начал использоваться.
 */
@Component
public class CustomAttributes extends DefaultErrorAttributes {

    /**
     * В мапу с атрибутами ошибки запроса можно добавлять любые ключ-значение, которые потом можно использовать
     * и отдавать с ответом при возникновении исключений
     */
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        Throwable throwable = getError(request);

        if (throwable instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) throwable;
            errorAttributes.put("message", responseStatusException.getMessage());
        }

        errorAttributes.put("developer", "Alenkin Andrew");
        return errorAttributes;
    }
}
