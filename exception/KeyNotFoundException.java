
package ru.rutoken.Pkcs11Caller.exception;

public class KeyNotFoundException extends Pkcs11CallerException {
    public KeyNotFoundException() {
        super("Key not found");
    }
}
