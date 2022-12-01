package org.javaboy.vhr.Exception;

import org.springframework.security.core.AuthenticationException;

public class AuthingException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public AuthingException(String message) {
        super(message);
    }
}
