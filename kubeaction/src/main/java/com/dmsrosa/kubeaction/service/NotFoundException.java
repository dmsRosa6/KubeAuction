package com.dmsrosa.kubeaction.service;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message, String s) {
        super(String.format(message, s));
    }
}
