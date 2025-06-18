package com.dmsrosa.kubeauction.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(String message, String s) {
        super(String.format(message, s));
    }
}
