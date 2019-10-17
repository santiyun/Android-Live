package com.tttrtclive.live.bean;

import androidx.annotation.NonNull;

/**
 * Created by root on 17-2-21.
 */

public class EnterUserInfo implements Comparable<EnterUserInfo> {

    private long id;
    private int role;
    private float mYLocation;
    private float mXLocation;
    public int mShowIndex;

    public EnterUserInfo(long uid, int role) {
        this.id = uid;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setXYLocation(float mXLocation, float mYLocation) {
        this.mXLocation = mXLocation;
        this.mYLocation = mYLocation;
    }

    @Override
    public int compareTo(@NonNull EnterUserInfo o) {
        if (mYLocation > o.mYLocation) {
            return 1;
        } else if (mYLocation == o.mYLocation) {
            if (mXLocation > o.mXLocation) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

}
