package com.tttrtclive.bean;

/**
 * Created by wangzhiguo on 17/5/31.
 */

public class DisplayDevice {

    private VideoViewObj mDisplayView;
    private EnterUserInfo mUserInfo;

    public DisplayDevice(VideoViewObj mDisplayView, EnterUserInfo mUserInfo) {
        this.mDisplayView = mDisplayView;
        this.mUserInfo = mUserInfo;
    }

    public VideoViewObj getDisplayView() {
        return mDisplayView;
    }

    public void setDisplayView(VideoViewObj mDisplayView) {
        this.mDisplayView = mDisplayView;
    }

    public EnterUserInfo getUserInfo() {
        return mUserInfo;
    }

    public void setUserInfo(EnterUserInfo mUserInfo) {
        this.mUserInfo = mUserInfo;
    }
}
