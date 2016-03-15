package com.columbasms.columbasms.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Matteo Brienza on 3/15/16.
 */
public class Address implements Parcelable {

    private String address;
    private double latitude;
    private double longitude;

    public Address(String address, double latitude, double longitude){
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Address(Parcel in) {
        address = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>()
    {
        public Address createFromParcel(Parcel in)
        {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };
}
