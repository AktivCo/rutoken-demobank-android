
package ru.rutoken.Pkcs11Caller.exception;

public class CertNotFoundException extends Pkcs11CallerException {
    public CertNotFoundException() {
        super("Certificate not found");
    }
}
