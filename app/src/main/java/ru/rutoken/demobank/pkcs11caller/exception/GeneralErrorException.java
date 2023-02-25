package ru.rutoken.demobank.pkcs11caller.exception;

public class GeneralErrorException extends Pkcs11CallerException {
    public GeneralErrorException(String message) {
        super(message);
    }

    public GeneralErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
