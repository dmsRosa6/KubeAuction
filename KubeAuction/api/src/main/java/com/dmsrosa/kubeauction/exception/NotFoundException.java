package com.dmsrosa.kubeauction.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, String s) {
        super(String.format(message, s));
    }
}
