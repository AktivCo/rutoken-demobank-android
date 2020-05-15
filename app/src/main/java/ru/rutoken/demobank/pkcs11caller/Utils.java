/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.demobank.pkcs11caller;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import ru.rutoken.demobank.pkcs11caller.exception.KeyNotFoundException;
import ru.rutoken.demobank.pkcs11caller.exception.ObjectNotFoundException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.demobank.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.Pkcs11Constants;

class Utils {
    static String removeTrailingSpaces(byte[] string) {
        return new String(string, StandardCharsets.UTF_8).replaceAll(" *$", "");
    }

    static long findObject(Pkcs11 pkcs11, long session, CK_ATTRIBUTE[] template)
            throws Pkcs11CallerException {
        NativeLong nativeSession = new NativeLong(session);
        NativeLong rv = pkcs11.C_FindObjectsInit(nativeSession,
                template, new NativeLong(template.length));
        Pkcs11Exception.throwIfNotOk(rv);

        NativeLong[] objects = new NativeLong[1];
        NativeLongByReference count =
                new NativeLongByReference(new NativeLong(objects.length));
        rv = pkcs11.C_FindObjects(nativeSession, objects, new NativeLong(objects.length),
                count);

        NativeLong rv2 = pkcs11.C_FindObjectsFinal(nativeSession);
        Pkcs11Exception.throwIfNotOk(rv);
        Pkcs11Exception.throwIfNotOk(rv2);
        if (count.getValue().longValue() <= 0) throw new ObjectNotFoundException();

        return objects[0].longValue();
    }

    static long findKey(Pkcs11 pkcs11, long session, byte[] id, boolean isPrivate)
            throws Pkcs11CallerException {
        CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        final NativeLongByReference keyClass = new NativeLongByReference(isPrivate ?
                new NativeLong(Pkcs11Constants.CKO_PRIVATE_KEY) : new NativeLong(Pkcs11Constants.CKO_PUBLIC_KEY));
        template[0].type = new NativeLong(Pkcs11Constants.CKA_CLASS);
        template[0].pValue = keyClass.getPointer();
        template[0].ulValueLen = new NativeLong(NativeLong.SIZE);
        ByteBuffer idBuffer = ByteBuffer.allocateDirect(id.length);
        idBuffer.put(id);
        template[1].type = new NativeLong(Pkcs11Constants.CKA_ID);
        template[1].pValue = Native.getDirectBufferPointer(idBuffer);
        template[1].ulValueLen = new NativeLong(id.length);
        try {
            return findObject(pkcs11, session, template);
        } catch (ObjectNotFoundException e) {
            throw new KeyNotFoundException();
        }
    }
}
