package com.afalenkin.webfluxnotes.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@With
@Table("users")
public class User {

    @Id
    private Integer id;

    /**
     * Ко всем аннотациям валидации обязательно нужно добавлять сообщение.
     * Стандартные сообщения хранятся в файлах и при каждой валидации оттуда вычитываются - это
     * является блокирующей IO операцией, что не допустимо при работе в реактивном стиле.
     */
    @NotNull(message = "Should not be null!")
    @NotEmpty(message = "Name should not be blank")
    private String name;
}
