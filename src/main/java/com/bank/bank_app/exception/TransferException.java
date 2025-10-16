package com.bank.bank_app.exception;

public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}