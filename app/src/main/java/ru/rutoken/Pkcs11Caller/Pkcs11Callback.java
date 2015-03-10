
package ru.rutoken.Pkcs11Caller;

import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;

public abstract class Pkcs11Callback {
    public abstract void execute(Pkcs11CallerException exception);

    public abstract void execute(Object... arguments);
}
