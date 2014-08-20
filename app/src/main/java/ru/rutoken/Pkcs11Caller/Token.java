package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;

import com.sun.jna.IntegerType;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import ru.rutoken.Pkcs11.*;

/**
 * Created by mironenko on 11.08.2014.
 */
public class Token extends Pkcs11Parcelable {
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(mLabel);
        parcel.writeString(mModel);
        parcel.writeString(mSerialNumber);
        parcel.writeString(mHardwareVersion);
        parcel.writeInt(mTotalMemory);
        parcel.writeInt(mFreeMemory);
        parcel.writeInt(mCharge);
        parcel.writeInt(mUserPinRetriesLeft);
        parcel.writeInt(mAdminPinRetriesLeft);
        parcel.writeSerializable(mColor);
        parcel.writeSerializable(mUserPinChangePolicy);
        parcel.writeValue(mSupportsSM);
        parcel.writeSerializable(mSmInitializedStatus);
        parcel.writeInt(mCertificateMap.size());
        for (String id: mCertificateMap.keySet()) {
            parcel.writeString(id);
            parcel.writeParcelable(mCertificateMap.get(id), 0);
        }
    }

    Token(Parcel parcel) {
        mLabel = parcel.readString();
        mModel = parcel.readString();
        mSerialNumber = parcel.readString();
        mHardwareVersion = parcel.readString();
        mTotalMemory = parcel.readInt();
        mFreeMemory = parcel.readInt();
        mCharge = parcel.readInt();
        mUserPinRetriesLeft = parcel.readInt();
        mAdminPinRetriesLeft = parcel.readInt();
        mColor = (BodyColor) parcel.readSerializable();
        mUserPinChangePolicy = (UserChangePolicy) parcel.readSerializable();
        mSupportsSM = (Boolean) parcel.readValue(boolean.class.getClassLoader());
        mSmInitializedStatus = (SmInitializedStatus) parcel.readSerializable();
        int mapSize = parcel.readInt();
        for (int i = 0; i < mapSize; ++i) {
            String id = parcel.readString();
            Certificate cert = (Certificate) parcel.readParcelable(Certificate.class.getClassLoader());
            mCertificateMap.put(id,cert);
        }
    }

    public enum UserChangePolicy {
        USER, SO, BOTH
    }
    public enum BodyColor {
        WHITE, BLACK, UNKNOWN
    }

    private enum SmInitializedStatus {
        UNKNOWN, NEED_INITIALIZE, INITIALIZED
    }
    private int mId;
    //private String mReaderName;

    private String mLabel;
    private String mModel;
    private String mSerialNumber;
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
    private Map<String, Certificate> mCertificateMap = new HashMap<String, Certificate>();

    public String label() { return mLabel; }
    public String model() { return mModel; }
    public String serialNumber() { return mSerialNumber; }
    public String hardwareVersion() { return mHardwareVersion; }
    public int totalMemory() { return mTotalMemory; }
    public int freeMemory() { return mFreeMemory; }
    public int charge() { return mCharge; }
    public int userPinRetriesLeft() { return mUserPinRetriesLeft; }
    public int adminPinRetriesLeft() { return mAdminPinRetriesLeft; }
    public BodyColor color() { return mColor; }
    public UserChangePolicy userPinChangePolicy() { return mUserPinChangePolicy; }
    public boolean supportsSM() {return mSupportsSM;}

    Token(int slotId, SlotInfo slotInfo, TokenInfo tokenInfo, TokenInfoEx tokenInfoEx) {
        mId = slotId;
        //mReaderName = Utils.removeTrailingSpaces(slotInfo.mSlotInfo.slotDescription);
        mLabel = Utils.removeTrailingSpaces(tokenInfo.mTokenInfo.label);
        mModel = Utils.removeTrailingSpaces(tokenInfo.mTokenInfo.model);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.mTokenInfo.serialNumber);
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.mTokenInfo.hardwareVersion.major, tokenInfo.mTokenInfo.hardwareVersion.minor,
                tokenInfo.mTokenInfo.firmwareVersion.major, tokenInfo.mTokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.mTokenInfo.ulTotalPublicMemory;
        mFreeMemory = tokenInfo.mTokenInfo.ulFreePublicMemory;
        mCharge = tokenInfoEx.mTokenInfoEx.ulBatteryVoltage;
        mUserPinRetriesLeft = tokenInfoEx.mTokenInfoEx.ulUserRetryCountLeft;
        mAdminPinRetriesLeft = tokenInfoEx.mTokenInfoEx.ulAdminRetryCountLeft;
        switch (tokenInfoEx.mTokenInfoEx.ulBodyColor) {
            case RtPkcs11Constants.TOKEN_BODY_COLOR_WHITE:
                mColor = BodyColor.WHITE;
                break;
            case RtPkcs11Constants.TOKEN_BODY_COLOR_BLACK:
                mColor = BodyColor.BLACK;
                break;
            case RtPkcs11Constants.TOKEN_BODY_COLOR_UNKNOWN:
                mColor = BodyColor.UNKNOWN;
                break;
        }
        if(((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)
                && ((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }
        mSupportsSM = ((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_SUPPORT_SM) != 0);
    }

    Token (int slotId) throws Pkcs11Exception {
        int rv;
        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
        CK_TOKEN_INFO_EXTENDED extendedInfo = new CK_TOKEN_INFO_EXTENDED();
        extendedInfo.ulSizeofThisStructure = extendedInfo.size();
        mId = slotId;
        rv = RtPkcs11Library.getInstance().C_GetTokenInfo(slotId, tokenInfo);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        rv = RtPkcs11Library.getInstance().C_EX_GetTokenInfoExtended(slotId, extendedInfo);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        initTokenInfo(tokenInfo, extendedInfo);
        initCertificatesList();
    }

    int[] findPkcs11Objects(int hSession, CK_ATTRIBUTE[] attributes) throws Pkcs11Exception  {
        int rv;
        final int ulMaxObjectsCount = 100;
        IntByReference ulObjectCount = new IntByReference(ulMaxObjectsCount);
        int[] hObjects = null;
        int[] hTmpObjects = new int[ulMaxObjectsCount];


        rv = RtPkcs11Library.getInstance().C_FindObjectsInit(hSession, attributes, attributes.length);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        try {
            rv = RtPkcs11Library.getInstance().C_FindObjects(hSession, hTmpObjects, ulMaxObjectsCount, ulObjectCount);
            if (Pkcs11Constants.CKR_OK != rv) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            hObjects = Arrays.copyOfRange(hTmpObjects, 0, ulObjectCount.getValue()-1);
        } finally {
            rv = RtPkcs11Library.getInstance().C_FindObjectsFinal(hSession);
        }
        return hObjects;
    }

    void initCertificatesList() throws Pkcs11Exception {
        IntByReference phSession = new IntByReference(0);
        int[] hCertificates = null;
        int rv = RtPkcs11Library.getInstance().C_OpenSession(mId,
                Pkcs11Constants.CKF_SERIAL_SESSION | Pkcs11Constants.CKF_SERIAL_SESSION,
                null,
                null,
                phSession);
        if (Pkcs11Constants.CKR_OK != rv) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        int hSession = phSession.getValue();
        try {
            IntByReference ocCertClass = new IntByReference(Pkcs11Constants.CKO_CERTIFICATE);
            CK_ATTRIBUTE[] certAttributes = new CK_ATTRIBUTE[1];

            certAttributes[0].type = Pkcs11Constants.CKA_CLASS;
            certAttributes[0].pValue = ocCertClass.getPointer();
            certAttributes[0].ulValueLen = NativeLong.SIZE;

            hCertificates = findPkcs11Objects(hSession, certAttributes);
            for (int hCertificate : hCertificates) {
                Certificate certificate = new Certificate(hSession, hCertificate);
                mCertificateMap.put(certificate.id(), certificate);
            }
        } finally {
            RtPkcs11Library.getInstance().C_CloseSession(hSession);
        }
    }

    void initTokenInfo(CK_TOKEN_INFO tokenInfo, CK_TOKEN_INFO_EXTENDED tokenInfoEx) {
        mLabel = Utils.removeTrailingSpaces(tokenInfo.label);
        mModel = Utils.removeTrailingSpaces(tokenInfo.model);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.serialNumber);
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.hardwareVersion.major, tokenInfo.hardwareVersion.minor,
                tokenInfo.firmwareVersion.major, tokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.ulTotalPublicMemory;
        mFreeMemory = tokenInfo.ulFreePublicMemory;
        mCharge = tokenInfoEx.ulBatteryVoltage;
        mUserPinRetriesLeft = tokenInfoEx.ulUserRetryCountLeft;
        mAdminPinRetriesLeft = tokenInfoEx.ulAdminRetryCountLeft;
        switch (tokenInfoEx.ulBodyColor) {
            case RtPkcs11Constants.TOKEN_BODY_COLOR_WHITE:
                mColor = BodyColor.WHITE;
                break;
            case RtPkcs11Constants.TOKEN_BODY_COLOR_BLACK:
                mColor = BodyColor.BLACK;
                break;
            case RtPkcs11Constants.TOKEN_BODY_COLOR_UNKNOWN:
                mColor = BodyColor.UNKNOWN;
                break;
        }
        if(((tokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)
                && ((tokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }
        mSupportsSM = ((tokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_SUPPORT_SM) != 0);
    }

//    public boolean needInitSM() throws Pkcs11Exception {
//        if(mSmInitializedStatus != SmInitializedStatus.UNKNOWN) {
//            return mSmInitializedStatus == SmInitializedStatus.NEED_INITIALIZE;
//        }
//        boolean result;
//        IntByReference phSession = new IntByReference();
//        int rv = RtPkcs11Library.getInstance().C_OpenSession(mId, ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKF_SERIAL_SESSION | ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKF_RW_SESSION, null, null, phSession);
//        if (rv == ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_OK) {
//            result = false;
//        } else if (rv == ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_FUNCTION_NOT_SUPPORTED) {
//            result = true;
//        } else {
//            throw Pkcs11Exception.exceptionWithCode(rv);
//        }
//        if (false == result)  {
//            rv = RtPkcs11Library.getInstance().C_CloseSession(phSession.getValue());
//            if (rv != ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_OK) {
//                throw Pkcs11Exception.exceptionWithCode(rv);
//            }
//        }
//        return result;
//    }

}
