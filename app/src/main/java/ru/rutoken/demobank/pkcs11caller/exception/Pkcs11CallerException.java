/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.pkcs11caller.exception;

public abstract class Pkcs11CallerException extends Exception {
    public Pkcs11CallerException(String message) {
        super(message);
    }

    public Pkcs11CallerException(String message, Throwable cause) {
        super(message, cause);
    }
}
