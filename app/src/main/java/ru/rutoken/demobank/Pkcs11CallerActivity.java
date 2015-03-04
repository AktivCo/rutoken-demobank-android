package ru.rutoken.demobank;

import android.content.Intent;

import com.sun.jna.NativeLong;

import ru.rutoken.Pkcs11Caller.Pkcs11Callback;
import ru.rutoken.Pkcs11Caller.Token;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;

/**
 * Created by mironenko on 28.08.2014.
 */
abstract public class Pkcs11CallerActivity extends ManagedActivity {
    public static String translatePkcs11Exception(Pkcs11CallerException exception) {
        if(Pkcs11Exception.class.isInstance(exception)) {
            return ((Pkcs11Exception)exception).getMessage();
        } else return "Unspecified error";
    }
    private static Pkcs11Exception pkcs11exceptionFromCallerException(Pkcs11CallerException exception) {
        Pkcs11Exception exception1 = null;
        if(Pkcs11Exception.class.isInstance(exception)) {
            exception1 = ((Pkcs11Exception)exception);
        }
        return exception1;
    }
    class LoginCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            manageLoginError(pkcs11exceptionFromCallerException(exception));
        }
        public void execute(Object... arguments) {
            assert(arguments.length == 0);

            Pkcs11CallerActivity.this.manageLoginSucceed();
        };
    }

    class SignCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            manageSignError(pkcs11exceptionFromCallerException(exception));
        }
        public void execute(Object... arguments) {
            assert(arguments.length != 0);
            manageSignSucceed((byte[])arguments[0]);
        };
    }

    class LogoutCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            manageLogoutError(pkcs11exceptionFromCallerException(exception));
        }
        public void execute(Object... arguments) {
            assert(arguments.length == 0);
            manageLogoutSucceed();
        };
    }

    LoginCallback mLoginCallback = new LoginCallback();
    SignCallback mSignCallback = new SignCallback();
    LogoutCallback mLogoutCallback = new LogoutCallback();

    protected void sign(Token token, NativeLong certificate, byte[] signData) {
        token.sign(certificate, signData, mSignCallback);
    }

    protected void login(Token token,  String pin) {
        token.login(pin, mLoginCallback);
    }

    protected void logout(Token token) {
        token.logout(mLogoutCallback);
    }

    abstract protected void manageLoginError(Pkcs11Exception exception);

    abstract protected void manageLoginSucceed();

    abstract protected void manageLogoutError(Pkcs11Exception exception);

    abstract protected void manageLogoutSucceed();

    abstract protected void manageSignError(Pkcs11Exception exception);

    abstract protected void manageSignSucceed(byte[] data);
}
