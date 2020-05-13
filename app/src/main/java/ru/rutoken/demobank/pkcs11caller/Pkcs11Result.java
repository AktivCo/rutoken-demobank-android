package ru.rutoken.demobank.pkcs11caller;

import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;

class Pkcs11Result {
    Pkcs11CallerException exception;
    Object[] arguments;

    Pkcs11Result(Object... arguments) {
        this.arguments = arguments;
    }

    Pkcs11Result(Pkcs11CallerException exception) {
        this.exception = exception;
    }
}
