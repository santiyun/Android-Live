package com.tttrtclive.bean;

import android.support.annotation.NonNull;

/**
 * Created by root on 17-2-21.
 */

public class EnterUserInfo implements Comparable<EnterUserInfo> {

    private long id;
    private String device;
    private long mSessionID;
    private boolean agreespeak;
    private int speak_status;
    private int mRole;
    public float mYLocation;
    public float mXLocation;
    public int mShowIndex;

    public EnterUserInfo(long uid, int mRole) {
        this.id = uid;
        this.mRole = mRole;
    }

    public EnterUserInfo(long uid, int mRole, float mYLocation, float mXLocation) {
        this.id = uid;
        this.mRole = mRole;
        this.mYLocation = mYLocation;
        this.mXLocation = mXLocation;
        setXYLocation(mYLocation, mXLocation);
    }

    public void setXYLocation(float mYLocation, float mXLocation) {
        if (mYLocation > 0.4 && mYLocation < 0.6) {
            if (mXLocation < 0.3) {
                mShowIndex = 0;
            } else if (mXLocation > 0.3 && mXLocation < 0.6) {
                mShowIndex = 1;
            } else {
                mShowIndex = 2;
            }
        } else if (mYLocation > 0.6) {
            if (mXLocation < 0.3) {
                mShowIndex = 3;
            } else if (mXLocation > 0.3 && mXLocation < 0.6) {
                mShowIndex = 4;
            } else {
                mShowIndex = 5;
            }
        }
    }

    public long getSessionID() {
        return mSessionID;
    }

    public void setSessionID(long mSessionID) {
        this.mSessionID = mSessionID;
    }

    public boolean isAgreespeak() {
        return agreespeak;
    }

    public void setAgreespeak(boolean agreespeak) {
        this.agreespeak = agreespeak;
    }

    public int getSpeak_status() {
        return speak_status;
    }

    public void setSpeak_status(int speak_status) {
        this.speak_status = speak_status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public int getRole() {
        return mRole;
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

//        if (mCompareValue > o.mCompareValue) {
//            return 1;
//        } else {
//            return -1;
//        }
    }
}
