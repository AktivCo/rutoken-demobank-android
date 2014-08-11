package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;

import ru.rutoken.Pkcs11.CK_TOKEN_INFO_EXTENDED;

/**
 * Created by mironenko on 08.08.2014.
 */
public class TokenInfoEx extends Pkcs11Parcelable {
    CK_TOKEN_INFO_EXTENDED mTokenInfoEx;

    public TokenInfoEx(CK_TOKEN_INFO_EXTENDED tokenInfoEx) {
        mTokenInfoEx = tokenInfoEx;
    }

    private TokenInfoEx(Parcel parcel) {
        super(parcel);

        mTokenInfoEx = new CK_TOKEN_INFO_EXTENDED();

        /* init this field by size of this structure
        * [in] - size of input structure
        * [out] - return size of filled structure
        */
        mTokenInfoEx.ulSizeofThisStructure = parcel.readInt();
        /* type of token: */
        mTokenInfoEx.ulTokenType = parcel.readInt();       /* see below */
        /* exchange protocol number */
        mTokenInfoEx.ulProtocolNumber = parcel.readInt();
        /* microcode number */
        mTokenInfoEx.ulMicrocodeNumber = parcel.readInt();
        /* order number */
        mTokenInfoEx.ulOrderNumber = parcel.readInt();
        /* information flags */
        /* TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - administrator can change user PIN
        * TOKEN_FLAGS_USER_CHANGE_USER_PIN  - user can change user PIN
        * TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - administrator PIN not default
        * TOKEN_FLAGS_USER_PIN_NOT_DEFAULT  - user PIN not default
        * TOKEN_FLAGS_SUPPORT_FKN           - token support CryptoPro FKN
        */
        mTokenInfoEx.flags = parcel.readInt();            /* see below */
        /* maximum and minimum PIN length */
        mTokenInfoEx.ulMaxAdminPinLen = parcel.readInt();
        mTokenInfoEx.ulMinAdminPinLen = parcel.readInt();
        mTokenInfoEx.ulMaxUserPinLen = parcel.readInt();
        mTokenInfoEx.ulMinUserPinLen = parcel.readInt();
        /* max count of unsuccessful login attempts */
        mTokenInfoEx.ulMaxAdminRetryCount = parcel.readInt();
        /* count of unsuccessful attempts left (for administrator PIN)
        * if field equal 0 - that means that PIN is blocked */
        mTokenInfoEx.ulAdminRetryCountLeft = parcel.readInt();
        /* min counts of unsuccessful login attempts */
        mTokenInfoEx.ulMaxUserRetryCount = parcel.readInt();
        /* count of unsuccessful attempts left (for user PIN)
        * if field equal 0 - that means that PIN is blocked */
        mTokenInfoEx.ulUserRetryCountLeft = parcel.readInt();
        /* token serial number in Big Endian format */
        parcel.readByteArray(mTokenInfoEx.serialNumber);
        /* size of all memory */
        mTokenInfoEx.ulTotalMemory = parcel.readInt();    /* in bytes */
        /* size of free memory */
        mTokenInfoEx.ulFreeMemory = parcel.readInt();     /* in bytes */
        /* atr of the token */
        parcel.readByteArray(mTokenInfoEx.ATR);
        /* size of atr */
        mTokenInfoEx.ulATRLen = parcel.readInt();
        /* class of token */
        mTokenInfoEx.ulTokenClass = parcel.readInt();     /* see below */
        /* Battery Voltage */
        mTokenInfoEx.ulBatteryVoltage = parcel.readInt(); /* microvolts */
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        /* init this field by size of this structure
        * [in] - size of input structure
        * [out] - return size of filled structure
        */
        parcel.writeInt(mTokenInfoEx.ulSizeofThisStructure);
        /* type of token: */
        parcel.writeInt(mTokenInfoEx.ulTokenType);       /* see below */
        /* exchange protocol number */
        parcel.writeInt(mTokenInfoEx.ulProtocolNumber);
        /* microcode number */
        parcel.writeInt(mTokenInfoEx.ulMicrocodeNumber);
        /* order number */
        parcel.writeInt(mTokenInfoEx.ulOrderNumber);
        /* information flags */
        /* TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - administrator can change user PIN
        * TOKEN_FLAGS_USER_CHANGE_USER_PIN  - user can change user PIN
        * TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - administrator PIN not default
        * TOKEN_FLAGS_USER_PIN_NOT_DEFAULT  - user PIN not default
        * TOKEN_FLAGS_SUPPORT_FKN           - token support CryptoPro FKN
        */
        parcel.writeInt(mTokenInfoEx.flags);            /* see below */
        /* maximum and minimum PIN length */
        parcel.writeInt(mTokenInfoEx.ulMaxAdminPinLen);
        parcel.writeInt(mTokenInfoEx.ulMinAdminPinLen);
        parcel.writeInt(mTokenInfoEx.ulMaxUserPinLen);
        parcel.writeInt(mTokenInfoEx.ulMinUserPinLen);
        /* max count of unsuccessful login attempts */
        parcel.writeInt(mTokenInfoEx.ulMaxAdminRetryCount);
        /* count of unsuccessful attempts left (for administrator PIN)
        * if field equal 0 - that means that PIN is blocked */
        parcel.writeInt(mTokenInfoEx.ulAdminRetryCountLeft);
        /* min counts of unsuccessful login attempts */
        parcel.writeInt(mTokenInfoEx.ulMaxUserRetryCount);
        /* count of unsuccessful attempts left (for user PIN)
        * if field equal 0 - that means that PIN is blocked */
        parcel.writeInt(mTokenInfoEx.ulUserRetryCountLeft);
        /* token serial number in Big Endian format */
        parcel.writeByteArray(mTokenInfoEx.serialNumber);
        /* size of all memory */
        parcel.writeInt(mTokenInfoEx.ulTotalMemory);    /* in bytes */
        /* size of free memory */
        parcel.writeInt(mTokenInfoEx.ulFreeMemory);     /* in bytes */
        /* atr of the token */
        parcel.writeByteArray(mTokenInfoEx.ATR);
        /* size of atr */
        parcel.writeInt(mTokenInfoEx.ulATRLen);
        /* class of token */
        parcel.writeInt(mTokenInfoEx.ulTokenClass);     /* see below */
        /* Battery Voltage */
        parcel.writeInt(mTokenInfoEx.ulBatteryVoltage); /* microvolts */
    }
}
