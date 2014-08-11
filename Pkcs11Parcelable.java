package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;
import android.os.Parcelable;

import ru.rutoken.Pkcs11.CK_VERSION;

/**
 * Created by mironenko on 08.08.2014.
 */
public class Pkcs11Parcelable implements Parcelable {
    public Pkcs11Parcelable() {}

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Pkcs11Parcelable> CREATOR
            = new Parcelable.Creator<Pkcs11Parcelable>() {
        public Pkcs11Parcelable createFromParcel(Parcel in) {
            return new Pkcs11Parcelable(in);
        }

        public Pkcs11Parcelable[] newArray(int size) {
            return new Pkcs11Parcelable[size];
        }
    };

    protected Pkcs11Parcelable(Parcel parcel) {}

    @Override
    public void writeToParcel(Parcel parcel, int i) {}
}