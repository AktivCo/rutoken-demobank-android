package ru.rutoken.Pkcs11Caller;

import ru.rutoken.Pkcs11.Pkcs11Constants;
import ru.rutoken.Pkcs11.RtPkcs11Constants;

/**
 * Created by mironenko on 11.08.2014.
 */
public class Token {
    private int mId;
    private String mReaderName;

    private String mLabel;
    private String mSerialNumber;
    private String mHardwareVersion;
    private int mTotalMemory;
    private int mFreeMemory;
    private int mCharge;
    private int mUserPinRetriesLeft;
    private int mAdminPinRetriesLeft;
    private UserChangePolicy mUserPinChangePolicy;

    public String label() { return mLabel; }
    public String serialNumber() { return mSerialNumber; }
    public String hardwareVersion() { return mHardwareVersion; }
    public int totalMemory() { return mTotalMemory; }
    public int freeMemory() { return mFreeMemory; }
    public int charge() { return mCharge; }
    public int userPinRetriesLeft() { return mUserPinRetriesLeft; }
    public int adminPinRetriesLeft() { return mAdminPinRetriesLeft; }
    public UserChangePolicy userPinChangePolicy() { return mUserPinChangePolicy; }

    Token(int slotId, SlotInfo slotInfo, TokenInfo tokenInfo, TokenInfoEx tokenInfoEx) {
        mId = slotId;
        mReaderName = Utils.removeTrailingSpaces(slotInfo.mSlotInfo.slotDescription);
        mLabel = Utils.removeTrailingSpaces(tokenInfo.mTokenInfo.label);
        mSerialNumber = Utils.removeTrailingSpaces(tokenInfo.mTokenInfo.serialNumber);
        mHardwareVersion = String.format("%d.%d.%d.%d",
                tokenInfo.mTokenInfo.hardwareVersion.major, tokenInfo.mTokenInfo.hardwareVersion.minor,
                tokenInfo.mTokenInfo.firmwareVersion.major, tokenInfo.mTokenInfo.firmwareVersion.minor);
        mTotalMemory = tokenInfo.mTokenInfo.ulTotalPublicMemory;
        mFreeMemory = tokenInfo.mTokenInfo.ulFreePublicMemory;
        mCharge = tokenInfoEx.mTokenInfoEx.ulBatteryVoltage;
        mUserPinRetriesLeft = tokenInfoEx.mTokenInfoEx.ulUserRetryCountLeft;
        mAdminPinRetriesLeft = tokenInfoEx.mTokenInfoEx.ulAdminRetryCountLeft;
        if(((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)
                && ((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_USER_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.BOTH;
        } else if (((tokenInfoEx.mTokenInfoEx.flags & RtPkcs11Constants.TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN) != 0x00)) {
            mUserPinChangePolicy = UserChangePolicy.SO;
        } else {
            mUserPinChangePolicy = UserChangePolicy.USER;
        }
    }

}
