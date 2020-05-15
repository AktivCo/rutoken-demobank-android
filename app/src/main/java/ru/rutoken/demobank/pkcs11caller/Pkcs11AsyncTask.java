/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.pkcs11caller;

import android.os.AsyncTask;

import ru.rutoken.demobank.pkcs11caller.exception.GeneralErrorException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.demobank.ui.Pkcs11CallerActivity;
import ru.rutoken.pkcs11jna.RtPkcs11;

abstract class Pkcs11AsyncTask extends AsyncTask<Void, Void, Pkcs11Result> {
    final RtPkcs11 mPkcs11 = RtPkcs11Library.getInstance();

    private final Pkcs11CallerActivity.Pkcs11Callback mCallback;

    Pkcs11AsyncTask(Pkcs11CallerActivity.Pkcs11Callback callback) {
        mCallback = callback;
    }

    protected abstract Pkcs11Result doWork() throws Pkcs11CallerException;

    @Override
    protected Pkcs11Result doInBackground(Void... voids) {
        try {
            synchronized (mPkcs11) {
                return doWork();
            }
        } catch (Pkcs11CallerException exception) {
            return new Pkcs11Result(exception);
        } catch (Exception exception) {
            return new Pkcs11Result(new GeneralErrorException(exception.getMessage()));
        }
    }

    @Override
    protected void onPostExecute(Pkcs11Result result) {
        if (result == null) mCallback.execute();
        else if (result.exception == null) mCallback.execute(result.arguments);
        else mCallback.execute(result.exception);
    }

    @Override
    protected void onCancelled(Pkcs11Result result) {
        mCallback.execute(result.exception);
    }
}
