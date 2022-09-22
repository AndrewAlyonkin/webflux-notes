package com.afalenkin.webfluxnotes.util;

import com.afalenkin.webfluxnotes.domain.User;

/**
 * @author Alenkin Andrew
 * oxqq@ya.ru
 */
public class UserData {

    public static User newUser() {
        return User.builder()
                .name("NewUser")
                .build();
    }

    public static User createdUser() {
        return User.builder()
                .id(1)
                .name("NewUser")
                .build();
    }

    public static User updatedUser() {
        return User.builder()
                .id(1)
                .name("UpdatedUser")
                .build();
    }

}
