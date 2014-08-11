package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;

import ru.rutoken.Pkcs11.CK_VERSION;

/**
 * Created by mironenko on 08.08.2014.
 */
public class Version extends Pkcs11Parcelable {
    public CK_VERSION mVersion;

    private Version(Parcel parcel) {
        super(parcel);

        mVersion = new CK_VERSION();

        mVersion.major = parcel.readByte();

        mVersion.minor = parcel.readByte();
    }

    public Version(CK_VERSION version) {
        mVersion = version;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {

        parcel.writeByte(mVersion.major);

        parcel.writeByte(mVersion.minor);
    }
}
