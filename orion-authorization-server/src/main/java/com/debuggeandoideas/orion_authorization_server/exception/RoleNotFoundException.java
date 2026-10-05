package com.debuggeandoideas.orion_authorization_server.exception;

public class RoleNotFoundException extends RuntimeException {

    public RoleNotFoundException(String roleName) {
        super("Role '" + roleName + "' does not exist");
    }
}
