package com.tttrtclive.live.bean;

import android.support.annotation.NonNull;

import com.tttrtclive.live.LocalConfig;

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

    public void setXYLocation(float mYLocation, float mXLocation, float width, float height) {
        if (LocalConfig.mIsPCAnchor) {
            if (mYLocation == 0) {
                if (mXLocation == 0) {
                    mShowIndex = 0;
                } else if ((mXLocation + width) <= 0.65f) {
                    mShowIndex = 1;
                } else {
                    mShowIndex = 2;
                }
            } else if (mYLocation == 0.5) {
                if (mXLocation == 0) {
                    mShowIndex = 3;
                } else if (mXLocation + width <= 0.65f) {
                    mShowIndex = 4;
                } else {
                    mShowIndex = 5;
                }
            }
        } else if (LocalConfig.mIsMacAnchor) {
            if (mYLocation < 0.6) {
                if (mXLocation < 0.14f) {
                    mShowIndex = 0;
                } else if (mXLocation > 0.14f && mXLocation < 0.29f) {
                    mShowIndex = 1;
                } else {
                    mShowIndex = 2;
                }
            } else {
                if (mXLocation < 0.14f) {
                    mShowIndex = 0;
                } else if (mXLocation > 0.14f && mXLocation < 0.29f) {
                    mShowIndex = 1;
                } else {
                    mShowIndex = 2;
                }
            }
        } else {
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
    }

    @Override
    public int compareTo(@NonNull EnterUserInfo o) {
        if (mYLocation > o.mYLocation) {
            return -1;
        } else if (mYLocation == o.mYLocation) {
            if (mXLocation > o.mXLocation) {
                return -1;
            } else {
                return 1;
            }
        } else {
            return 1;
        }
    }

}
