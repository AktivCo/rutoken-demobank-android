package ru.rutoken.Pkcs11Caller;

import android.os.Parcel;
import android.os.Parcelable;

import ru.rutoken.Pkcs11.CK_SLOT_INFO;
import ru.rutoken.Pkcs11.CK_VERSION;

/**
 * Created by mironenko on 08.08.2014.
 */
public class SlotInfo extends Pkcs11Parcelable {
    CK_SLOT_INFO mSlotInfo;

    public SlotInfo(CK_SLOT_INFO slotInfo) {
        mSlotInfo = slotInfo;
    }

    private SlotInfo(Parcel parcel) {
        super(parcel);

        mSlotInfo = new CK_SLOT_INFO();

        parcel.readByteArray(mSlotInfo.slotDescription);

        parcel.readByteArray(mSlotInfo.manufacturerID);

        mSlotInfo.flags = parcel.readLong();

        mSlotInfo.hardwareVersion = ((Version)parcel.readValue(Version.class.getClassLoader())).mVersion;

        mSlotInfo.firmwareVersion = ((Version)parcel.readValue(Version.class.getClassLoader())).mVersion;
    }


    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByteArray(mSlotInfo.slotDescription);

        parcel.writeByteArray(mSlotInfo.manufacturerID);

        parcel.writeLong(mSlotInfo.flags);

        parcel.writeValue(new Version(mSlotInfo.hardwareVersion));

        parcel.writeValue(new Version(mSlotInfo.firmwareVersion));
    }
}