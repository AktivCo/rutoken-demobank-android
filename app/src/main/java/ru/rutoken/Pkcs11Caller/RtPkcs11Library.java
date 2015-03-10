/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11Caller;

import com.sun.jna.Native;

import ru.rutoken.Pkcs11.RtPkcs11;

public class RtPkcs11Library {
    public static RtPkcs11 instance = null;

    public static synchronized RtPkcs11 getInstance() {
        RtPkcs11 localInstance = instance;

        if (localInstance == null) {
            synchronized (RtPkcs11Library.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = (RtPkcs11) Native.loadLibrary("rtpkcs11ecp", RtPkcs11.class);
                }
            }
        }

        return localInstance;
    }
}
