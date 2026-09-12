package com.tiketkonser.user_service.handler;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}