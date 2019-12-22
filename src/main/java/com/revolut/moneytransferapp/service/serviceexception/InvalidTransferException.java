package com.revolut.moneytransferapp.service.serviceexception;

public class InvalidTransferException  extends Throwable {
    public InvalidTransferException(String message) {
        super(message);
    }
}
