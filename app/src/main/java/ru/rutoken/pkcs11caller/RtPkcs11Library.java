/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.Native;

import ru.rutoken.pkcs11jna.RtPkcs11;

public class RtPkcs11Library {
    private static volatile RtPkcs11 instance = null;

    public static synchronized RtPkcs11 getInstance() {
        RtPkcs11 localInstance = instance;

        if (localInstance == null) {
            synchronized (RtPkcs11Library.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = Native.loadLibrary("rtpkcs11ecp", RtPkcs11.class);
                }
            }
        }

        return localInstance;
    }
}
