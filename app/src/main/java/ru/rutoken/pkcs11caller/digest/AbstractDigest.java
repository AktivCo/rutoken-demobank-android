package ru.rutoken.pkcs11caller.digest;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.Objects;

import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_MECHANISM;
import ru.rutoken.pkcs11jna.Pkcs11;

abstract class AbstractDigest implements Digest {
    private final Pkcs11 mPkcs11;
    private final long mSessionHandle;

    AbstractDigest(Pkcs11 pkcs11, long sessionHandle) {
        mPkcs11 = Objects.requireNonNull(pkcs11);
        mSessionHandle = sessionHandle;
    }

    CK_MECHANISM makeMechanism(long type, byte[] parameter) {
        Memory memory = new Memory(parameter.length);
        memory.write(0, parameter, 0, parameter.length);
        return new CK_MECHANISM(new NativeLong(type), memory, new NativeLong(memory.size()));
    }

    byte[] innerDigest(final CK_MECHANISM mechanism, final byte[] data) throws Pkcs11Exception {
        NativeLong rv = mPkcs11.C_DigestInit(new NativeLong(mSessionHandle), mechanism);
        Pkcs11Exception.throwIfNotOk(rv);

        final NativeLongByReference count = new NativeLongByReference();
        rv = mPkcs11.C_Digest(new NativeLong(mSessionHandle), data, new NativeLong(data.length), null, count);
        Pkcs11Exception.throwIfNotOk(rv);

        final byte[] digest = new byte[count.getValue().intValue()];
        rv = mPkcs11.C_Digest(new NativeLong(mSessionHandle), data, new NativeLong(data.length), digest, count);
        Pkcs11Exception.throwIfNotOk(rv);

        return digest;
    }
}
