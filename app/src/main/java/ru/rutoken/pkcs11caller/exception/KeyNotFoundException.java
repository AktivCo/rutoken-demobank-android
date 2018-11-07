/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

public class KeyNotFoundException extends Pkcs11CallerException {
    public KeyNotFoundException() {
        super("Key not found");
    }
}
