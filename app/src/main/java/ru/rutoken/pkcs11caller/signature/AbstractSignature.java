package ru.rutoken.pkcs11caller.signature;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.Objects;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_MECHANISM;
import ru.rutoken.pkcs11jna.Pkcs11;

abstract class AbstractSignature implements Signature {
    private final Pkcs11 mPkcs11;
    private final long mSessionHandle;
    private long mPrivateKeyHandle;

    AbstractSignature(Pkcs11 pkcs11, long sessionHandle) {
        mPkcs11 = Objects.requireNonNull(pkcs11);
        mSessionHandle = sessionHandle;
    }

    @Override
    public void signInit(long privateKeyHandle) {
        mPrivateKeyHandle = privateKeyHandle;
    }

    CK_MECHANISM makeMechanism(long type) {
        return new CK_MECHANISM(new NativeLong(type), Pointer.NULL, new NativeLong(0));
    }

    byte[] innerSign(final CK_MECHANISM mechanism, final byte[] data) throws Pkcs11Exception {
        NativeLong rv = mPkcs11.C_SignInit(new NativeLong(mSessionHandle), mechanism, new NativeLong(mPrivateKeyHandle));
        Pkcs11Exception.throwIfNotOk(rv);

        final NativeLongByReference count = new NativeLongByReference();
        rv = mPkcs11.C_Sign(new NativeLong(mSessionHandle), data, new NativeLong(data.length), null, count);
        Pkcs11Exception.throwIfNotOk(rv);

        final byte[] signature = new byte[count.getValue().intValue()];
        rv = mPkcs11.C_Sign(new NativeLong(mSessionHandle), data, new NativeLong(data.length), signature, count);
        Pkcs11Exception.throwIfNotOk(rv);

        return signature;
    }
}
