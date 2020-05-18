/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See https://download.rutoken.ru/License_Agreement.pdf
 * All Rights Reserved.
 */

package ru.rutoken.demobank.ui;

import androidx.annotation.Nullable;

import ru.rutoken.demobank.pkcs11caller.Token;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;

abstract public class Pkcs11CallerActivity extends ManagedActivity {
    private final Pkcs11Callback mPkcs11Callback = new Pkcs11Callback();

    @Nullable
    private static Pkcs11Exception pkcs11exceptionFromCallerException(Pkcs11CallerException exception) {
        Pkcs11Exception exception1 = null;
        if (exception instanceof Pkcs11Exception) {
            exception1 = ((Pkcs11Exception) exception);
        }
        return exception1;
    }

    /**
     * This method is intended to be called after {@link #login(Token, String, String, byte[])} method,
     * so a pin is assumed to be cached
     */
    protected void sign(Token token, String certificate, byte[] signData) {
        token.loginAndSign(null, certificate, signData,
                mPkcs11Callback, this);
    }

    protected void login(Token token, String pin, String certificate, byte[] signData) {
        token.loginAndSign(pin, certificate, signData,
                mPkcs11Callback, this);
    }

    abstract protected void manageTokenOperationError(@Nullable Pkcs11Exception exception);

    abstract protected void manageTokenOperationCanceled();

    abstract protected void manageTokenOperationSucceed();

    public class Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            if (isDestroyed())
                return;
            if (exception.getCause() instanceof InterruptedException)
                manageTokenOperationCanceled();

            else
                manageTokenOperationError(pkcs11exceptionFromCallerException(exception));
        }

        public void execute(Object... arguments) {
            if (isDestroyed())
                return;
            manageTokenOperationSucceed();
        }
    }
}
