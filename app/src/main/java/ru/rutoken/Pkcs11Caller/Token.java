
package ru.rutoken.Pkcs11Caller;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import ru.rutoken.Pkcs11.*;
import ru.rutoken.Pkcs11Caller.exception.CertNotFoundException;
import ru.rutoken.Pkcs11Caller.exception.KeyNotFoundException;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11CallerException;
import ru.rutoken.Pkcs11Caller.exception.Pkcs11Exception;

public class Token {
    public enum UserChangePolicy {
        USER, SO, BOTH
    }

    public enum BodyColor {
        WHITE, BLACK, UNKNOWN
    }

    private enum SmInitializedStatus {
        UNKNOWN, NEED_INITIALIZE, INITIALIZED
    }

    private NativeLong mId;

    private NativeLong mSession;

    private String mLabel;
    private String mModel;
    private String mSerialNumber;
    private String mShortDecSerialNumber;
    private String mHardwareVersion;
    private int mTotalMemory;
    private int mFreeMemory;
    private int mCharge;
    private int mUserPinRetriesLeft;
    private int mAdminPinRetriesLeft;
    private BodyColor mColor;
    private UserChangePolicy mUserPinChangePolicy;
    private boolean mSupportsSM;
    private SmInitializedStatus mSmInitializedStatus = SmInitializedStatus.UNKNOWN;
    private HashMap<NativeLong, Certificate> mCertificateMap = new HashMap<NativeLong, Certificate>();

    public String getLabel() {
        return mLabel;
    }

    public String getModel() {
        return mModel;
    }

    public String getSerialNumber() {
        return mSerialNumber;
    }

    public String getShortDecSerialNumber() {
        return mShortDecSerialNumber;
    }

    public String getHardwareVersion() {
        return mHardwareVersion;
    }

    public int getTotalMemory() {
        return mTotalMemory;
    }

    public int getFreeMemory() {
        return mFreeMemory;
    }

    public int getCharge() {
        return mCharge;
    }

    public int getUserPinRetriesLeft() {
        return mUserPinRetriesLeft;
    }

    public int getAdminPinRetriesLeft() {
        return mAdminPinRetriesLeft;
    }

    public BodyColor getColor() {
        return mColor;
    }

    public UserChangePolicy getUserPinChangePolicy() {
        return mUserPinChangePolicy;
    }

    public boolean supportsSM() {
        return mSupportsSM;
    }

    Token(NativeLong slotId) throws Pkcs11CallerException {
        RtPkcs11 pkcs11 = RtPkcs11Library.getInstance();
        synchronized (pkcs11) {
            mId = slotId;
            initTokenInfo();

            NativeLongByReference session = new NativeLongByReference();
            NativeLong rv = RtPkcs11Library.getInstance().C_OpenSession(mId,
                    Pkcs11Constants.CKF_SERIAL_SESSION, null, null, session);

            if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);
            mSession = session.getValue();

            try {
                initCertificatesList(pkcs11);
            } catch (Pkcs11CallerException exception) {
                try {
                    close();
                } catch (Pkcs11CallerException exception2) {
                }
                throw exception;
            }
        }
    }

    void close() throws Pkcs11Exception {
        NativeLong rv = RtPkcs11Library.getInstance().C_CloseSession(mSession);
        if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);
    }

    private void initCertificatesList(RtPkcs11 pkcs11) throws Pkcs11CallerException {
        CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        NativeLongByReference certClass =
                new NativeLongByReference(Pkcs11Constants.CKO_CERTIFICATE);
        template[0].type = Pkcs11Constants.CKA_CLASS;
        template[0].pValue = certClass.getPointer();
        template[0].ulValueLen = new NativeLong(NativeLong.SIZE);

        // token user category
        NativeLongByReference certCategory = new NativeLongByReference(new NativeLong(1));
        template[1].type = Pkcs11Constants.CKA_CERTIFICATE_CATEGORY;
        template[1].pValue = certCategory.getPointer();
        template[1].ulValueLen = new NativeLong(NativeLong.SIZE);

        NativeLong rv =
                pkcs11.C_FindObjectsInit(mSession, template, new NativeLong(template.length));
        if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

        NativeLong[] objects = new NativeLong[30];
        NativeLongByReference count = new NativeLongByReference(new NativeLong(objects.length));
        ArrayList<NativeLong> certs = new ArrayList<NativeLong>();
        do {
            rv = pkcs11.C_FindObjects(mSession, objects, new NativeLong(objects.length), count);
            if (!rv.equals(Pkcs11Constants.CKR_OK)) break;
            certs.addAll(Arrays.asList(objects).subList(0, count.getValue().intValue()));
        } while (count.getValue().intValue() == objects.length);

        NativeLong rv2 = pkcs11.C_FindObjectsFinal(mSession);
        if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);
        else if (!rv2.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv2);

        for (NativeLong c : certs) {
            mCertificateMap.put(c, new Certificate(pkcs11, mSession, c));
        }
    }

    private void initTokenInfo() throws Pkcs11CallerException {
        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
        CK_TOKEN_INFO_EXTENDED tokenInfoEx = new CK_TOKEN_INFO_EXTENDED();
        tokenInfoEx.ulSizeofThisStructure = new NativeLong(tokenInfoEx.size());

        NativeLong rv = RtPkcs11Library.getInstance().C_GetTokenInfo(mId, tokenInfo);
        if (!rv.equals(Pkcs11Constants.CKR_OK)) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }

        rv = RtPkcs11Library.getInstance().C_EX_GetTokenInfoExtended(mId, tokenInfoEx);
        if (!rv.equals(Pkcs11Constants.CKR_OK)) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }

        mLabel = Utils.removeTrailingSpaces(tokenInfo.label);
        mModel = Utils.removeTrailingSpaces(tokenInfo.model);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.serialNumber);
        int decSerial = Integer.parseInt(mSerialNumber, 16);
        String decSerialString = String.valueOf(decSerial);
        mShortDecSerialNumber = decSerialString.substring(decSerialString.length() - 5);
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.hardwareVersion.major, tokenInfo.hardwareVersion.minor,
                tokenInfo.firmwareVersion.major, tokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.ulTotalPublicMemory.intValue();
        mFreeMemory = tokenInfo.ulFreePublicMemory.intValue();
        mCharge = tokenInfoEx.ulBatteryVoltage.intValue();
        mUserPinRetriesLeft = tokenInfoEx.ulUserRetryCountLeft.intValue();
        mAdminPinRetriesLeft = tokenInfoEx.ulAdminRetryCountLeft.intValue();

        if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_WHITE)) {
            mColor = BodyColor.WHITE;
        } else if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_BLACK)) {
            mColor = BodyColor.BLACK;
        } else if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_UNKNOWN)) {
            mColor = BodyColor.UNKNOWN;
        }

        if (((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN.intValue()) != 0x00)
                && ((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN.intValue()) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN.intValue()) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }

        mSupportsSM = ((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_SUPPORT_SM.intValue()) != 0);
    }

    public Set<NativeLong> enumerateCertificates() {
        return mCertificateMap.keySet();
    }

    public Certificate getCertificate(NativeLong handle) {
        return mCertificateMap.get(handle);
    }

    public void login(final String pin, Pkcs11Callback callback) {
        new Pkcs11AsyncTask(callback) {
            @Override
            protected Pkcs11Result doWork() throws Pkcs11CallerException {
                NativeLong rv = mPkcs11.C_Login(mSession, Pkcs11Constants.CKU_USER,
                        pin.getBytes(), new NativeLong(pin.length()));
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                return null;
            }
        }.execute();
    }

    public void logout(Pkcs11Callback callback) {
        new Pkcs11AsyncTask(callback) {
            @Override
            protected Pkcs11Result doWork() throws Pkcs11CallerException {
                NativeLong rv = mPkcs11.C_Logout(mSession);
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                return null;
            }
        }.execute();
    }

    public void sign(final NativeLong certificate, final byte[] data,
            Pkcs11Callback callback) {
        new Pkcs11AsyncTask(callback) {
            @Override
            protected Pkcs11Result doWork() throws Pkcs11CallerException {
                Certificate cert = mCertificateMap.get(certificate);
                if (cert == null) throw new CertNotFoundException();

                CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

                final NativeLongByReference keyClass =
                        new NativeLongByReference(Pkcs11Constants.CKO_PRIVATE_KEY);
                template[0].type = Pkcs11Constants.CKA_CLASS;
                template[0].pValue = keyClass.getPointer();
                template[0].ulValueLen = new NativeLong(NativeLong.SIZE);

                byte[] id = cert.getId();
                ByteBuffer idBuffer = ByteBuffer.allocateDirect(id.length);
                idBuffer.put(id);
                template[1].type = Pkcs11Constants.CKA_ID;
                template[1].pValue = Native.getDirectBufferPointer(idBuffer);
                template[1].ulValueLen = new NativeLong(id.length);

                NativeLong rv = mPkcs11.C_FindObjectsInit(mSession,
                        template, new NativeLong(template.length));
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                NativeLong objects[] = new NativeLong[2];
                NativeLongByReference count =
                        new NativeLongByReference(new NativeLong(objects.length));
                rv = mPkcs11.C_FindObjects(mSession, objects, new NativeLong(objects.length),
                        count);

                NativeLong rv2 = mPkcs11.C_FindObjectsFinal(mSession);
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);
                else if (!rv2.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);
                else if (count.getValue().intValue() <= 0) throw new KeyNotFoundException();

                final byte[] oid = {
                        0x06, 0x07, 0x2a, (byte) 0x85, 0x03, 0x02, 0x02, 0x1e, 0x01
                };
                ByteBuffer oidBuffer = ByteBuffer.allocateDirect(oid.length);
                oidBuffer.put(oid);
                CK_MECHANISM mechanism =
                        new CK_MECHANISM(RtPkcs11Constants.CKM_GOSTR3410_WITH_GOSTR3411,
                                Native.getDirectBufferPointer(oidBuffer),
                                new NativeLong(oid.length));
                rv = mPkcs11.C_SignInit(mSession, mechanism, objects[0]);
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                rv = mPkcs11.C_Sign(mSession, data, new NativeLong(data.length), null, count);
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                byte signature[] = new byte[count.getValue().intValue()];
                rv = mPkcs11.C_Sign(mSession, data, new NativeLong(data.length), signature, count);
                if (!rv.equals(Pkcs11Constants.CKR_OK)) throw Pkcs11Exception.exceptionWithCode(rv);

                return new Pkcs11Result(signature);
            }
        }.execute();
    }
}
