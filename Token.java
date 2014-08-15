package ru.rutoken.Pkcs11Caller;

import com.sun.jna.ptr.IntByReference;

import ru.rutoken.Pkcs11.Pkcs11Constants;
import ru.rutoken.Pkcs11.RtPkcs11Constants;

/**
 * Created by mironenko on 11.08.2014.
 */
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
    private int mId;
    private String mReaderName;

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
        mReaderName = Utils.removeTrailingSpaces(slotInfo.mSlotInfo.slotDescription);
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

    public boolean needInitSM() throws Pkcs11Exception {
        if(mSmInitializedStatus != SmInitializedStatus.UNKNOWN) {
            return mSmInitializedStatus == SmInitializedStatus.NEED_INITIALIZE;
        }
        boolean result;
        IntByReference phSession = new IntByReference();
        int rv = RtPkcs11Library.getInstance().C_OpenSession(mId, ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKF_SERIAL_SESSION | ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKF_RW_SESSION, null, null, phSession);
        if (rv == ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_OK) {
            result = false;
        } else if (rv == ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_FUNCTION_NOT_SUPPORTED) {
            result = true;
        } else {
            throw Pkcs11Exception.exceptionWithCode(rv);
        }
        if (false == result)  {
            rv = RtPkcs11Library.getInstance().C_CloseSession(phSession.getValue());
            if (rv != ru.rutoken.pkcs11wrapper.Pkcs11Constants.CKR_OK) {
                throw Pkcs11Exception.exceptionWithCode(rv);
            }
        }
        return result;
    }

}
