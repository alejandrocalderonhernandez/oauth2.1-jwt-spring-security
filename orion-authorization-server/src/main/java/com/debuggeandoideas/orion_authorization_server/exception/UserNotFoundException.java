package com.debuggeandoideas.orion_authorization_server.exception;

public class UserNotFoundException extends RuntimeException {

    private UserNotFoundException(String message) {
        super(message);
    }

    public static UserNotFoundException withId(Long id) {
        return new UserNotFoundException("User with id " + id + " not found");
    }

    public static UserNotFoundException withUsername(String username) {
        return new UserNotFoundException("User with username '" + username + "' not found");
    }
}
