/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

public class CertNotFoundException extends Pkcs11CallerException {
    public CertNotFoundException() {
        super("Certificate not found");
    }
}
