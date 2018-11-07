/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;

public abstract class Pkcs11Callback {
    public abstract void execute(Pkcs11CallerException exception);

    public abstract void execute(Object... arguments);
}
