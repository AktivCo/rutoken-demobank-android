/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import ru.rutoken.bcprovider.CmsSigner;
import ru.rutoken.demobank.Pkcs11CallerActivity;
import ru.rutoken.demobank.nfc.NfcDetectCardFragment;
import ru.rutoken.pkcs11caller.Certificate.CertificateCategory;
import ru.rutoken.pkcs11caller.exception.CertNotFoundException;
import ru.rutoken.pkcs11caller.exception.GeneralErrorException;
import ru.rutoken.pkcs11caller.exception.Pkcs11CallerException;
import ru.rutoken.pkcs11caller.exception.Pkcs11Exception;
import ru.rutoken.pkcs11jna.CK_ATTRIBUTE;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO;
import ru.rutoken.pkcs11jna.CK_TOKEN_INFO_EXTENDED;
import ru.rutoken.pkcs11jna.Pkcs11Constants;
import ru.rutoken.pkcs11jna.RtPkcs11;
import ru.rutoken.pkcs11jna.RtPkcs11Constants;

public class Token {
    public enum UserChangePolicy {
        USER, SO, BOTH
    }

    public enum BodyColor {
        WHITE, BLACK, UNKNOWN
    }

    public enum SmInitializedStatus {
        UNKNOWN, NEED_INITIALIZE, INITIALIZED
    }

    private final int mExecutorKey;
    private final boolean mIsNfc;
    private String mPin = "";
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
    private final HashMap<String, CertificateAndGostKeyPair> mCertificateMap = new HashMap<>();
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final RtPkcs11 mRtPkcs11;

    public int getExecutorKey() {
        return mExecutorKey;
    }

    public boolean isNfc() {
        return mIsNfc;
    }

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

    public SmInitializedStatus smInitializedStatus() {
        return mSmInitializedStatus;
    }

    public void clearPin() {
        // TODO: implement secure pin caching
        mPin = "";
    }

    Token(int executorKey, NativeLong slotId, CK_TOKEN_INFO tokenInfo, boolean isNfc, RtPkcs11 pkcs11) throws Pkcs11CallerException {
        mExecutorKey = executorKey;
        mRtPkcs11 = Objects.requireNonNull(pkcs11);
        mIsNfc = isNfc;
        initTokenInfo(slotId, tokenInfo, pkcs11);
    }

    private Map<String, CertificateAndGostKeyPair> getCertificatesWithCategory(CertificateCategory category,
                                                                               NativeLong session)
            throws Pkcs11CallerException {
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

        NativeLong rv = mRtPkcs11.C_FindObjectsInit(session, template, new NativeLong(template.length));
        Pkcs11Exception.throwIfNotOk(rv);

        NativeLong[] objects = new NativeLong[30];
        NativeLongByReference count = new NativeLongByReference(new NativeLong(objects.length));
        ArrayList<NativeLong> certs = new ArrayList<>();
        do {
            rv = mRtPkcs11.C_FindObjects(session, objects, new NativeLong(objects.length), count);
            if (rv.longValue() != Pkcs11Constants.CKR_OK) break;
            certs.addAll(Arrays.asList(objects).subList(0, count.getValue().intValue()));
        } while (count.getValue().longValue() == objects.length);
        NativeLong rv2 = mRtPkcs11.C_FindObjectsFinal(session);
        Pkcs11Exception.throwIfNotOk(rv);
        Pkcs11Exception.throwIfNotOk(rv2);

        HashMap<String, CertificateAndGostKeyPair> certificateMap = new HashMap<>();
        for (NativeLong c : certs) {
            try {
                Certificate cert = new Certificate(mRtPkcs11, session.longValue(), c.longValue());
                GostKeyPair keyPair = GostKeyPair.getGostKeyPairByCertificate(mRtPkcs11, session.longValue(), cert.getCertificateHolder());
                certificateMap.put(cert.fingerprint(), new CertificateAndGostKeyPair(cert, keyPair));
            } catch (Pkcs11CallerException e) {
                e.printStackTrace();
            }
        }
        return certificateMap;
    }

    private void initTokenInfo(NativeLong slotId, CK_TOKEN_INFO tokenInfo, RtPkcs11 pkcs11) throws Pkcs11CallerException {
        CK_TOKEN_INFO_EXTENDED tokenInfoEx = new CK_TOKEN_INFO_EXTENDED();
        tokenInfoEx.ulSizeofThisStructure = new NativeLong(tokenInfoEx.size());

        NativeLong rv = pkcs11.C_EX_GetTokenInfoExtended(slotId, tokenInfoEx);
        Pkcs11Exception.throwIfNotOk(rv);

        mLabel = Utils.removeTrailingSpaces(tokenInfo.label);
        mModel = Utils.removeTrailingSpaces(tokenInfo.model);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.serialNumber);
        long decSerial = Long.parseLong(mSerialNumber, 16);
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

    public void readCertificates(Runnable onResult) {
        TokenExecutors.getInstance().get(mExecutorKey).execute(() -> {
            try (Session session = new Session()) {
                CertificateCategory[] supportedCategories = {CertificateCategory.UNSPECIFIED, CertificateCategory.USER};
                for (CertificateCategory category : supportedCategories) {
                    mCertificateMap.putAll(getCertificatesWithCategory(category, session.get()));
                }
            } catch (Pkcs11CallerException e) {
                e.printStackTrace();
            } finally {
                mHandler.post(onResult);
            }
        });
    }

    public Set<String> enumerateCertificates() {
        return mCertificateMap.keySet();
    }

    public Certificate getCertificate(String fingerprint) {
        return Objects.requireNonNull(mCertificateMap.get(fingerprint)).getCertificate();
    }

    public void loginAndSign(@Nullable final String pin, final String certificate, final byte[] data,
                             Pkcs11CallerActivity.Pkcs11Callback callback,
                             NfcDetectCardFragment.Control nfcFragmentControl) {
        new Pkcs11AsyncTask(callback) {
            @Override
            protected Pkcs11Result doWork() throws Pkcs11CallerException {
                String pinToUse = (!mPin.isEmpty())? mPin : pin;

                CertificateAndGostKeyPair cert = mCertificateMap.get(certificate);
                if (cert == null) throw new CertNotFoundException();

                if (mIsNfc)
                    nfcFragmentControl.show(() -> cancel(true));

                try (Session session = new Session()) {
                    NativeLong rv = mPkcs11.C_Login(session.get(), new NativeLong(Pkcs11Constants.CKU_USER),
                            pinToUse.getBytes(), new NativeLong(pinToUse.length()));
                    Pkcs11Exception.throwIfNotOk(rv);

                    // TODO: implement secure pin caching
                    mPin = pinToUse;

                    try {
                        long keyHandle = cert.getGostKeyPair()
                                .getPrivateKeyHandle(mPkcs11, session.get().longValue());

                        final CmsSigner signer = new CmsSigner(cert.getGostKeyPair().getKeyType(),
                                session.get().longValue());

                        return new Pkcs11Result(signer.sign(data, keyHandle,
                                cert.getCertificate().getCertificateHolder()));
                    } finally {
                        rv = mPkcs11.C_Logout(session.get());
                        Pkcs11Exception.throwIfNotOk(rv);
                    }
                } finally {
                    if (mIsNfc)
                        nfcFragmentControl.dismiss();
                }
            }
        }.execute();
    }


    private class Session implements AutoCloseable {
        private NativeLong mSession;

        Session() throws Pkcs11CallerException {
            final NativeLongByReference session = new NativeLongByReference();

            final NativeLong slotId;
            try {
                slotId = TokenManager.getInstance().getSlotIdByTokenSerial(getSerialNumber()).get();
            } catch (ExecutionException e) {
                throw new GeneralErrorException("Cannot get slot id", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new GeneralErrorException("Thread was interrupted", e);
            }

            final NativeLong rv = mRtPkcs11.C_OpenSession(
                    slotId, new NativeLong(Pkcs11Constants.CKF_SERIAL_SESSION), null, null, session);
            if (rv.longValue() != Pkcs11Constants.CKR_OK) {
                if (rv.longValue() == Pkcs11Constants.CKR_FUNCTION_NOT_SUPPORTED)
                    mSmInitializedStatus = SmInitializedStatus.NEED_INITIALIZE;

                throw new Pkcs11Exception(rv);
            }

            mSession = session.getValue();
        }

        @Override
        public void close() {
            try {
                NativeLong rv = mRtPkcs11.C_CloseSession(mSession);
                Pkcs11Exception.throwIfNotOk(rv);
            } catch (Pkcs11CallerException e) {
                e.printStackTrace();
            }
        }

        NativeLong get() {
            return mSession;
        }
    }
}
