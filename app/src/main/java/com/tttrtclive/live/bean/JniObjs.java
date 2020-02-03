package com.tttrtclive.live.bean;

import java.io.Serializable;

/**
 * Created by wangzhiguo on 17/10/13.
 */

public class JniObjs implements Serializable {

    public int mJniType;
    public long mUid;
    public int mIdentity;
    public int mReason;
    public boolean mIsDisableAudio;
    public int mAudioLevel;
    public String mChannelName;
    public String mSEI;
    public int mErrorType;
    public int mAudioRoute;
    public int mAudioSentBitrate;
    public int mVideoSentBitrate;
    public int mAudioRecvBitrate;
    public int mVideoRecvBitrate;
    public int mVideoSentFps;
}
