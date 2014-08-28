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
abstract public class Pkcs11CallerActivity extends ExternallyDismissableActivity {
    public static String translatePkcs11Exception(Pkcs11CallerException exception) {
        if(Pkcs11Exception.class.isInstance(exception)) {
            return ((Pkcs11Exception)exception).getMessage();
        } else return "Unspecified error";
    }
    class LoginCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            showError(Pkcs11CallerActivity.translatePkcs11Exception(exception));
            manageLoginError();
        }
        public void execute(Object... arguments) {
            if(arguments.length != 0) {
                showError("Unspecified error");
                return;
            }

            Pkcs11CallerActivity.this.manageLoginSucceed();
        };
    }

    class SignCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            showError(Pkcs11CallerActivity.translatePkcs11Exception(exception));
            manageSignError();
        }
        public void execute(Object... arguments) {
            if(arguments.length == 0) {
                showError("Unspecified error");
                return;
            }
            manageSignSucceed((byte[])arguments[0]);
        };
    }

    class LogoutCallback extends Pkcs11Callback {
        public void execute(Pkcs11CallerException exception) {
            showError(Pkcs11CallerActivity.translatePkcs11Exception(exception));
            manageLogoutError();
        }
        public void execute(Object... arguments) {
            if(arguments != null) {
                showError("Unspecified error");
                return;
            }
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

    abstract protected void manageLoginError();

    abstract protected void manageLoginSucceed();

    abstract protected void manageLogoutError();

    abstract protected void manageLogoutSucceed();

    abstract protected void manageSignError();

    abstract protected void manageSignSucceed(byte[] data);

    abstract protected void showError(String error);
}
