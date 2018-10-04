/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

public abstract class Pkcs11CallerException extends Exception {
    public Pkcs11CallerException(String message) {
        super(message);
    }
}
