/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller;

import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;

public abstract class Pkcs11Callback {
    public abstract void execute(Pkcs11CallerException exception);

    public abstract void execute(Object... arguments);
}
