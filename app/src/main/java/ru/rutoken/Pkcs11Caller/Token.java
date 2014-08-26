package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

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
        parcel.writeInt(mUserPinRetriesMax);
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
        mUserPinRetriesMax = parcel.readInt();
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
    private NativeLong mId;

    private String mLabel;
    private String mModel;
    private String mSerialNumber;
    private String mHardwareVersion;
    private int mTotalMemory;
    private int mFreeMemory;
    private int mCharge;
    private int mUserPinRetriesLeft;
    private int mUserPinRetriesMax;
    private int mAdminPinRetriesLeft;
    private BodyColor mColor;
    private UserChangePolicy mUserPinChangePolicy;
    private boolean mSupportsSM;
    private SmInitializedStatus mSmInitializedStatus = SmInitializedStatus.UNKNOWN;
    private Map<String, Certificate> mCertificateMap = new HashMap<String, Certificate>();

    private String mCachedPIN = null;

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

    Token (NativeLong slotId) throws Pkcs11Exception {
        mId = slotId;
        doInitTokenInfo();
//        doInitCertificatesList();
    }

    NativeLong[] doFindPkcs11Objects(NativeLong hSession, CK_ATTRIBUTE[] attributes) throws Pkcs11Exception  {
        NativeLong rv;
        final int ulMaxObjectsCount = 100;
        NativeLongByReference ulObjectCount = new NativeLongByReference(new NativeLong(ulMaxObjectsCount));
        NativeLong[] hObjects = null;
        NativeLong[] hTmpObjects = new NativeLong[ulMaxObjectsCount];


        rv = RtPkcs11Library.getInstance().C_FindObjectsInit(hSession, attributes, new NativeLong(attributes.length));
        if (!rv.equals(Pkcs11Constants.CKR_OK)) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        try {
            rv = RtPkcs11Library.getInstance().C_FindObjects(hSession, hTmpObjects, new NativeLong(ulMaxObjectsCount), ulObjectCount);
            if (!rv.equals(Pkcs11Constants.CKR_OK)) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
            hObjects = Arrays.copyOfRange(hTmpObjects, 0, ulObjectCount.getValue().intValue()-1);
        } finally {
            rv = RtPkcs11Library.getInstance().C_FindObjectsFinal(hSession);
        }
        return hObjects;
    }

    void doInitCertificatesList() throws Pkcs11Exception {
        NativeLongByReference phSession = new NativeLongByReference(new NativeLong(0));
        NativeLong[] hCertificates = null;
        NativeLong rv = RtPkcs11Library.getInstance().C_OpenSession(mId,
                new NativeLong(Pkcs11Constants.CKF_SERIAL_SESSION.intValue() | Pkcs11Constants.CKF_SERIAL_SESSION.intValue()),
                null,
                null,
                phSession);
        if (!rv.equals(Pkcs11Constants.CKR_OK)) {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        NativeLong hSession = phSession.getValue();
        try {
            NativeLongByReference ocCertClass = new NativeLongByReference(Pkcs11Constants.CKO_CERTIFICATE);
            CK_ATTRIBUTE[] certAttributes = new CK_ATTRIBUTE[1];

            certAttributes[0].type = Pkcs11Constants.CKA_CLASS;
            certAttributes[0].pValue = ocCertClass.getPointer();
            certAttributes[0].ulValueLen = new NativeLong(NativeLong.SIZE);

            hCertificates = doFindPkcs11Objects(hSession, certAttributes);
            for (NativeLong hCertificate : hCertificates) {
                Certificate certificate = new Certificate(hSession, hCertificate);
                mCertificateMap.put(certificate.id(), certificate);
            }
        } finally {
            RtPkcs11Library.getInstance().C_CloseSession(hSession);
        }
    }

    void doInitTokenInfo() throws Pkcs11Exception {
        NativeLong rv;
        CK_TOKEN_INFO tokenInfo = new CK_TOKEN_INFO();
        CK_TOKEN_INFO_EXTENDED tokenInfoEx = new CK_TOKEN_INFO_EXTENDED();
        tokenInfoEx.ulSizeofThisStructure = new NativeLong(tokenInfoEx.size());

        rv = RtPkcs11Library.getInstance().C_GetTokenInfo(mId, tokenInfo);
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
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.hardwareVersion.major, tokenInfo.hardwareVersion.minor,
                tokenInfo.firmwareVersion.major, tokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.ulTotalPublicMemory.intValue();
        mFreeMemory = tokenInfo.ulFreePublicMemory.intValue();
        mCharge = tokenInfoEx.ulBatteryVoltage.intValue();
        mUserPinRetriesLeft = tokenInfoEx.ulUserRetryCountLeft.intValue();
        mUserPinRetriesMax = tokenInfoEx.ulMaxUserRetryCount.intValue();
        mAdminPinRetriesLeft = tokenInfoEx.ulAdminRetryCountLeft.intValue();
        if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_WHITE)) {
            mColor = BodyColor.WHITE;
        } else if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_BLACK)) {
                mColor = BodyColor.BLACK;
        } else if (tokenInfoEx.ulBodyColor.equals(RtPkcs11Constants.TOKEN_BODY_COLOR_UNKNOWN)) {
                mColor = BodyColor.UNKNOWN;
        }
        if(((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN.intValue()) != 0x00)
                && ((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN.intValue()) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN.intValue()) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }
        mSupportsSM = ((tokenInfoEx.flags.intValue() & RtPkcs11Constants.TOKEN_FLAGS_SUPPORT_SM.intValue()) != 0);
    }

    boolean doLoginUser(int hSession, String userPIN) throws Pkcs11Exception {
//        if(null != mCachedPIN && mCachedPIN == userPIN) return true;
//        int rv = RtPkcs11Library.getInstance().C_Login(hSession, Pkcs11Constants.CKU_USER, userPIN.getBytes(), userPIN.length());
//        if (Pkcs11Constants.CKR_USER_ALREADY_LOGGED_IN == rv || Pkcs11Constants.CKR_OK == rv) {
//            mCachedPIN = userPIN;
//            mUserPinRetriesLeft = mUserPinRetriesMax;
//        } else {
//            mCachedPIN = null;
//        }
        return true;



    }

}
