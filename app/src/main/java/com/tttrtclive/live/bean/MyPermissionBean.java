package com.tttrtclive.live.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by wangzhiguo on 18/3/5.
 */
public class MyPermissionBean implements Parcelable {

    public String mPermissionName;
    public String mPermissionReason;

    public MyPermissionBean(String mPermissionName, String mPermissionReason) {
        this.mPermissionName = mPermissionName;
        this.mPermissionReason = mPermissionReason;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPermissionName);
        dest.writeString(this.mPermissionReason);
    }

    public MyPermissionBean() {
    }

    protected MyPermissionBean(Parcel in) {
        this.mPermissionName = in.readString();
        this.mPermissionReason = in.readString();
    }

    public static final Creator<MyPermissionBean> CREATOR = new Creator<MyPermissionBean>() {
        @Override
        public MyPermissionBean createFromParcel(Parcel source) {
            return new MyPermissionBean(source);
        }

        @Override
        public MyPermissionBean[] newArray(int size) {
            return new MyPermissionBean[size];
        }
    };
}
