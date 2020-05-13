package ru.rutoken.demobank.pkcs11caller.exception;

public class KeyTypeNotSupported extends Pkcs11CallerException {
    public KeyTypeNotSupported() {super("Key type not supported"); }
}
