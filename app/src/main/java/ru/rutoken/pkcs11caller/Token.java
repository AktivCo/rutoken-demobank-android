/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.CK_MECHANISM;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO_EXTENDED;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;
import ru.rutoken.pkcs11caller.Certificate.CertificateCategory;
import ru.rutoken.pkcs11caller.exception.CertNotFoundException;
import ru.rutoken.pkcs11caller.exception.KeyNotFoundException;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;

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

    private final NativeLong mId;

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
    private final HashMap<NativeLong, Certificate> mCertificateMap = new HashMap<>();

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
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (pkcs11) {
            mId = slotId;
            initTokenInfo();

            NativeLongByReference session = new NativeLongByReference();
            NativeLong rv = RtPkcs11Library.getInstance().C_OpenSession(mId,
                    new NativeLong(Pkcs11Constants.CKF_SERIAL_SESSION), null, null, session);
            Pkcs11Exception.throwIfNotOk(rv);
            mSession = session.getValue();

            try {
                initCertificatesList(pkcs11);
            } catch (Pkcs11CallerException exception) {
                try {
                    close();
                } catch (Pkcs11CallerException exception2) {
                    exception2.printStackTrace();
                }
                throw exception;
            }
        }
    }

    void close() throws Pkcs11Exception {
        NativeLong rv = RtPkcs11Library.getInstance().C_CloseSession(mSession);
        Pkcs11Exception.throwIfNotOk(rv);
    }

    private Map<NativeLong, Certificate> getCertificatesWithCategory(RtPkcs11 pkcs11, CertificateCategory category) throws Pkcs11CallerException {
        CK_ATTRIBUTE[] template = (CK_ATTRIBUTE[]) (new CK_ATTRIBUTE()).toArray(2);

        NativeLongByReference certClass =
                new NativeLongByReference(new NativeLong(Pkcs11Constants.CKO_CERTIFICATE));
        template[0].type = new NativeLong(Pkcs11Constants.CKA_CLASS);
        template[0].pValue = certClass.getPointer();
        template[0].ulValueLen = new NativeLong(NativeLong.SIZE);

        NativeLongByReference certCategory = new NativeLongByReference(new NativeLong(category.getValue()));
        template[1].type = new NativeLong(Pkcs11Constants.CKA_CERTIFICATE_CATEGORY);
        template[1].pValue = certCategory.getPointer();
        template[1].ulValueLen = new NativeLong(NativeLong.SIZE);

        NativeLong rv = pkcs11.C_FindObjectsInit(mSession, template, new NativeLong(template.length));
        Pkcs11Exception.throwIfNotOk(rv);

        NativeLong[] objects = new NativeLong[30];
        NativeLongByReference count = new NativeLongByReference(new NativeLong(objects.length));
        ArrayList<NativeLong> certs = new ArrayList<>();
        do {
            rv = pkcs11.C_FindObjects(mSession, objects, new NativeLong(objects.length), count);
            if (rv.longValue() != Pkcs11Constants.CKR_OK) break;
            certs.addAll(Arrays.asList(objects).subList(0, count.getValue().intValue()));
        } while (count.getValue().longValue() == objects.length);

        NativeLong rv2 = pkcs11.C_FindObjectsFinal(mSession);
        Pkcs11Exception.throwIfNotOk(rv);
        Pkcs11Exception.throwIfNotOk(rv2);

        HashMap<NativeLong, Certificate> certificateMap = new HashMap<>();
        for (NativeLong c : certs) {
            certificateMap.put(c, new Certificate(pkcs11, mSession, c));
        }

        return certificateMap;
    }

    private void initCertificatesList(RtPkcs11 pkcs11) throws Pkcs11CallerException {
        CertificateCategory supportedCategories[] = {CertificateCategory.UNSPECIFIED, CertificateCategory.USER};
        for (CertificateCategory category: supportedCategories) {
            mCertificateMap.putAll(getCertificatesWithCategory(pkcs11, category));
        }
    }

    private void initTokenInfo() throws Pkcs11CallerException {
        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
        CK_TOKEN_INFO_EXTENDED tokenInfoEx = new CK_TOKEN_INFO_EXTENDED();
        tokenInfoEx.ulSizeofThisStructure = new NativeLong(tokenInfoEx.size());

        NativeLong rv = RtPkcs11Library.getInstance().C_GetTokenInfo(mId, tokenInfo);
        Pkcs11Exception.throwIfNotOk(rv);

        rv = RtPkcs11Library.getInstance().C_EX_GetTokenInfoExtended(mId, tokenInfoEx);
        Pkcs11Exception.throwIfNotOk(rv);

        mLabel = Utils.removeTrailingSpaces(tokenInfo.label);
        mModel = Utils.removeTrailingSpaces(tokenInfo.model);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.serialNumber);
        int decSerial = Integer.parseInt(mSerialNumber, 16);
        String decSerialString = String.valueOf(decSerial);
        mShortDecSerialNumber = String.valueOf(decSerial % 100000);
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.hardwareVersion.major, tokenInfo.hardwareVersion.minor,
                tokenInfo.firmwareVersion.major, tokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.ulTotalPublicMemory.intValue();
        mFreeMemory = tokenInfo.ulFreePublicMemory.intValue();
        mCharge = tokenInfoEx.ulBatteryVoltage.intValue();
        mUserPinRetriesLeft = tokenInfoEx.ulUserRetryCountLeft.intValue();
        mAdminPinRetriesLeft = tokenInfoEx.ulAdminRetryCountLeft.intValue();

        if (tokenInfoEx.ulBodyColor.longValue() == RtPkcs11Constants.TOKEN_BODY_COLOR_WHITE) {
            mColor = BodyColor.WHITE;
        } else if (tokenInfoEx.ulBodyColor.longValue() == RtPkcs11Constants.TOKEN_BODY_COLOR_BLACK) {
            mColor = BodyColor.BLACK;
        } else if (tokenInfoEx.ulBodyColor.longValue() == RtPkcs11Constants.TOKEN_BODY_COLOR_UNKNOWN) {
            mColor = BodyColor.UNKNOWN;
        }

        if (((tokenInfoEx.flags.longValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)
                && ((tokenInfoEx.flags.longValue() & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.flags.longValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }

        mSupportsSM = ((tokenInfoEx.flags.longValue() & RtPkcs11Constants.TOKEN_FLAGS_SUPPORT_SM) != 0);
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
                NativeLong rv = mPkcs11.C_Login(mSession, new NativeLong(Pkcs11Constants.CKU_USER),
                        pin.getBytes(), new NativeLong(pin.length()));
                Pkcs11Exception.throwIfNotOk(rv);

                return null;
            }
        }.execute();
    }

    public void logout(Pkcs11Callback callback) {
        new Pkcs11AsyncTask(callback) {
            @Override
            protected Pkcs11Result doWork() throws Pkcs11CallerException {
                NativeLong rv = mPkcs11.C_Logout(mSession);
                Pkcs11Exception.throwIfNotOk(rv);

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

                NativeLong keyHandle = cert.getPrivateKeyHandle(mPkcs11, mSession);
                if (keyHandle == null) throw new KeyNotFoundException();

                NativeLongByReference count =
                        new NativeLongByReference(new NativeLong());

                final byte[] oid = {
                        0x06, 0x07, 0x2a, (byte) 0x85, 0x03, 0x02, 0x02, 0x1e, 0x01
                };
                ByteBuffer oidBuffer = ByteBuffer.allocateDirect(oid.length);
                oidBuffer.put(oid);
                CK_MECHANISM mechanism =
                        new CK_MECHANISM(new NativeLong(RtPkcs11Constants.CKM_GOSTR3410_WITH_GOSTR3411),
                                Native.getDirectBufferPointer(oidBuffer),
                                new NativeLong(oid.length));
                NativeLong rv = mPkcs11.C_SignInit(mSession, mechanism, keyHandle);
                Pkcs11Exception.throwIfNotOk(rv);

                rv = mPkcs11.C_Sign(mSession, data, new NativeLong(data.length), null, count);
                Pkcs11Exception.throwIfNotOk(rv);

                byte signature[] = new byte[count.getValue().intValue()];
                rv = mPkcs11.C_Sign(mSession, data, new NativeLong(data.length), signature, count);
                Pkcs11Exception.throwIfNotOk(rv);

                return new Pkcs11Result(signature);
            }
        }.execute();
    }
}
