package com.dmsrosa.kubeauction.service.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, String s) {
        super(String.format(message, s));
    }
}
