package com.debuggeandoideas.orion_authorization_server.exception;

public class UsernameAlreadyExistsException extends RuntimeException {

    public UsernameAlreadyExistsException(String username) {
        super("Username '" + username + "' already exists");
    }
}
