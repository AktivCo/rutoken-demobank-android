/*
 * Copyright (c) 2019, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller.exception;

public class ObjectNotFoundException extends Pkcs11CallerException {
    public ObjectNotFoundException() {
        super("Object not found");
    }
}
