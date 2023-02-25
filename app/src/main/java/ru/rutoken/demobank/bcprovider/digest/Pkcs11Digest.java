package ru.rutoken.demobank.bcprovider.digest;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import org.bouncycastle.crypto.RuntimeCryptoException;

import java.util.Arrays;
import java.util.Objects;

import ru.rutoken.pkcs11jna.CK_MECHANISM;
import ru.rutoken.pkcs11jna.Pkcs11;
import ru.rutoken.pkcs11jna.Pkcs11Constants;

abstract class Pkcs11Digest implements Digest {
    private final Pkcs11 mPkcs11;
    private final NativeLong mSessionHandle;
    private final CK_MECHANISM mMechanism;
    private boolean mIsOperationInitialized = false;

    Pkcs11Digest(Pkcs11 pkcs11, long sessionHandle, long mechanismType, byte[] mechanismParameter) {
        mPkcs11 = Objects.requireNonNull(pkcs11);
        mSessionHandle = new NativeLong(sessionHandle);
        mMechanism = makeMechanism(mechanismType, mechanismParameter);
    }

    private static CK_MECHANISM makeMechanism(long type, byte[] parameter) {
        Memory memory = new Memory(parameter.length);
        memory.write(0, parameter, 0, parameter.length);
        return new CK_MECHANISM(new NativeLong(type), memory, new NativeLong(memory.size()));
    }

    private static void checkReturnValue(NativeLong rv, String functionName) {
        if (rv.longValue() != Pkcs11Constants.CKR_OK)
            throw new RuntimeCryptoException(functionName + " failed with rv: " + rv.intValue());
    }

    @Override
    public void update(byte in) {
        update(new byte[]{in});
    }

    @Override
    public void update(byte[] in, int inOff, int len) {
        byte[] chunk = Arrays.copyOfRange(in, inOff, inOff + len);
        update(chunk);
    }

    private void update(byte[] chunk) {
        NativeLong rv;
        if (!mIsOperationInitialized) {
            rv = mPkcs11.C_DigestInit(mSessionHandle, mMechanism);
            checkReturnValue(rv, "C_DigestInit");
            mIsOperationInitialized = true;
        }

        rv = mPkcs11.C_DigestUpdate(mSessionHandle, chunk, new NativeLong(chunk.length));
        checkReturnValue(rv, "C_DigestUpdate");
    }

    @Override
    public int doFinal(byte[] out, int outOff) {
        final NativeLongByReference count = new NativeLongByReference();
        NativeLong rv = mPkcs11.C_DigestFinal(mSessionHandle, null, count);
        checkReturnValue(rv, "C_DigestFinal");

        final byte[] digest = new byte[count.getValue().intValue()];
        rv = mPkcs11.C_DigestFinal(mSessionHandle, digest, count);
        checkReturnValue(rv, "C_DigestFinal");

        mIsOperationInitialized = false;

        int length = count.getValue().intValue();
        System.arraycopy(digest, 0, out, outOff, length);
        return length;
    }

    @Override
    public void reset() {
        if (mIsOperationInitialized) {
            byte[] result = new byte[getDigestSize()];
            doFinal(result, 0);
        }
    }
}
