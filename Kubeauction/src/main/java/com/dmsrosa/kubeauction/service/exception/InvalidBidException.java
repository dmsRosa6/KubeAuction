package com.dmsrosa.kubeauction.service.exception;

public class InvalidBidException extends RuntimeException {

    public InvalidBidException(String s) {
        super(s);
    }
}
