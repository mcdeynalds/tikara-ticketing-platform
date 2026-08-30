package com.tiketkonser.order_service.handler;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}