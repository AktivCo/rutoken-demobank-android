package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;
import android.os.Parcelable;

import ru.rutoken.Pkcs11.CK_TOKEN_INFO;
import ru.rutoken.Pkcs11.CK_VERSION;

/**
 * Created by mironenko on 08.08.2014.
 */
public class TokenInfo extends Pkcs11Parcelable {
    CK_TOKEN_INFO mTokenInfo;

    public TokenInfo(CK_TOKEN_INFO tokenInfo) {
        mTokenInfo = tokenInfo;
    }

    private TokenInfo(Parcel parcel) {
        super(parcel);

        mTokenInfo = new CK_TOKEN_INFO();

        parcel.readByteArray(mTokenInfo.label);           /* blank padded */ // 32 bytes

        parcel.readByteArray(mTokenInfo.manufacturerID);  /* blank padded */

        parcel.readByteArray(mTokenInfo.model);           /* blank padded */

        parcel.readByteArray(mTokenInfo.serialNumber);    /* blank padded */

        mTokenInfo.flags = parcel.readInt();               /* see below */

        mTokenInfo.ulMaxSessionCount = parcel.readInt();     /* max open sessions */

        mTokenInfo.ulSessionCount = parcel.readInt();        /* sess. now open */

        mTokenInfo.ulMaxRwSessionCount = parcel.readInt();   /* max R/W sessions */

        mTokenInfo.ulRwSessionCount = parcel.readInt();      /* R/W sess. now open */

        mTokenInfo.ulMaxPinLen = parcel.readInt();           /* in bytes */

        mTokenInfo.ulMinPinLen = parcel.readInt();           /* in bytes */

        mTokenInfo.ulTotalPublicMemory = parcel.readInt();   /* in bytes */

        mTokenInfo.ulFreePublicMemory = parcel.readInt();    /* in bytes */

        mTokenInfo.ulTotalPrivateMemory = parcel.readInt();  /* in bytes */

        mTokenInfo.ulFreePrivateMemory = parcel.readInt();   /* in bytes */

        mTokenInfo.hardwareVersion = ((Version)parcel.readValue(Version.class.getClassLoader())).mVersion;       /* version of hardware */

        mTokenInfo.firmwareVersion = ((Version)parcel.readValue(Version.class.getClassLoader())).mVersion;       /* version of firmware */

        parcel.readByteArray(mTokenInfo.utcTime);           /* time */
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByteArray(mTokenInfo.label);           /* blank padded */ // 32 bytes

        parcel.writeByteArray(mTokenInfo.manufacturerID);  /* blank padded */

        parcel.writeByteArray(mTokenInfo.model);           /* blank padded */

        parcel.writeByteArray(mTokenInfo.serialNumber);    /* blank padded */

        parcel.writeInt(mTokenInfo.flags);               /* see below */

        parcel.writeInt(mTokenInfo.ulMaxSessionCount);     /* max open sessions */

        parcel.writeInt(mTokenInfo.ulSessionCount);        /* sess. now open */

        parcel.writeInt(mTokenInfo.ulMaxRwSessionCount);   /* max R/W sessions */

        parcel.writeInt(mTokenInfo.ulRwSessionCount);      /* R/W sess. now open */

        parcel.writeInt(mTokenInfo.ulMaxPinLen);           /* in bytes */

        parcel.writeInt(mTokenInfo.ulMinPinLen);           /* in bytes */

        parcel.writeInt(mTokenInfo.ulTotalPublicMemory);   /* in bytes */

        parcel.writeInt(mTokenInfo.ulFreePublicMemory);    /* in bytes */

        parcel.writeInt(mTokenInfo.ulTotalPrivateMemory);  /* in bytes */

        parcel.writeInt(mTokenInfo.ulFreePrivateMemory);   /* in bytes */

        parcel.writeValue(new Version(mTokenInfo.hardwareVersion));       /* version of hardware */

        parcel.writeValue(new Version(mTokenInfo.firmwareVersion));       /* version of firmware */

        parcel.writeByteArray(mTokenInfo.utcTime);           /* time */
    }
}
