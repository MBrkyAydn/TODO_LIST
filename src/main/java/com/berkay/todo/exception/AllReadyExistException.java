package com.berkay.todo.exception;

public class AllReadyExistException extends RuntimeException {
    public AllReadyExistException(String message) {
        super(message);
    }
}
