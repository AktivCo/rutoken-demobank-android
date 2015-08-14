/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller.exception;

public class CertParsingException extends Pkcs11CallerException {
    public CertParsingException() {
        super("Failed to parse certificate");
    }
}
