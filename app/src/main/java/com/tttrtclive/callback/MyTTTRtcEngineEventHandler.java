package com.tttrtclive.callback;

import android.content.Context;
import android.content.Intent;

import com.tttrtclive.LocalConfig;
import com.tttrtclive.bean.JniObjs;
import com.tttrtclive.utils.MyLog;
import com.wushuangtech.bean.ChatInfo;
import com.wushuangtech.bean.ConfVideoFrame;
import com.wushuangtech.bean.LocalAudioStats;
import com.wushuangtech.bean.LocalVideoStats;
import com.wushuangtech.bean.RemoteAudioStats;
import com.wushuangtech.bean.RemoteVideoStats;
import com.wushuangtech.bean.RtcStats;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;

import java.util.ArrayList;
import java.util.List;

import static com.tttrtclive.LocalConfig.mRole;
import static com.tttrtclive.LocalConfig.mUserEnterOrder;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_AUDIO_ROUTE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_ENTER_ROOM;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_ERROR;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_LOCAL_AUDIO_STATE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_LOCAL_VIDEO_STATE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_MUTE_AUDIO;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_REMOTE_AUDIO_STATE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_REMOTE_VIDEO_STATE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_REMOVE_FIRST_FRAME_COME;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_SCREEN_RECORD_TIME;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_SEI;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_SPEAK_MUTE_AUDIO;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_USER_MUTE_VIDEO;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_USER_OFFLINE;
import static com.tttrtclive.LocalConstans.CALL_BACK_ON_USER_ROLE_CHANGED;


/**
 * Created by wangzhiguo on 17/10/24.
 */

public class MyTTTRtcEngineEventHandler extends TTTRtcEngineEventHandler {

    public static final String TAG = "MyTTTRtcEngineEventHandlerMM";
    public static final String MSG_TAG = "MyTTTRtcEngineEventHandlerMSGMM";
    private boolean mIsSaveCallBack;
    private List<JniObjs> mSaveCallBack;
    private Context mContext;

    public MyTTTRtcEngineEventHandler(Context mContext) {
        this.mContext = mContext;
        mSaveCallBack = new ArrayList<>();
    }

    @Override
    public void onJoinChannelSuccess(String channel, long uid) {
        MyLog.i("wzg", "onJoinChannelSuccess.... channel ： " + channel + " | uid : " + uid);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ENTER_ROOM;
        mJniObjs.mChannelName = channel;
        mJniObjs.mUid = uid;
        sendMessage(mJniObjs);
        mIsSaveCallBack = true;
    }

    @Override
    public void onError(final int errorType) {
        MyLog.i("wzg", "onError.... errorType ： " + errorType + "mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ERROR;
        mJniObjs.mErrorType = errorType;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserJoined(long nUserId, int identity) {
        MyLog.i("wzg", "onUserJoined.... nUserId ： " + nUserId + " | identity : " + identity
                + " | mIsSaveCallBack : " + mIsSaveCallBack);
        if (mRole == Constants.CLIENT_ROLE_ANCHOR) {
            mUserEnterOrder.add(nUserId);
        }

        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_JOIN;
        mJniObjs.mUid = nUserId;
        mJniObjs.mIdentity = identity;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserOffline(long nUserId, int reason) {
        MyLog.i("wzg", "onUserOffline.... nUserId ： " + nUserId + " | reason : " + reason);
        if (mRole == Constants.CLIENT_ROLE_ANCHOR) {
            mUserEnterOrder.remove(nUserId);
        }

        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_OFFLINE;
        mJniObjs.mUid = nUserId;
        mJniObjs.mReason = reason;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onConnectionLost() {
        MyLog.i("wzg", "onConnectionLost.... ");
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ERROR;
        mJniObjs.mErrorType = 100;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserEnableVideo(long uid, boolean muted) {
        MyLog.i("wzg", "onUserEnableVideo.... uid : " + uid + " | mute : " + muted);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_MUTE_VIDEO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsEnableVideo = muted;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onAudioVolumeIndication(long nUserID, int audioLevel, int audioLevelFullRange) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
        mJniObjs.mUid = nUserID;
        mJniObjs.mAudioLevel = audioLevel;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onFirstRemoteVideoFrame(long uid, int width, int height) {
        MyLog.i("wzg", "onFirstRemoteVideoFrame.... uid ： " + uid + " | width : " + width + " | height : " + height);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOVE_FIRST_FRAME_COME;
        mJniObjs.mUid = uid;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
//        MyLog.i("wzg", "onRemoteVideoStats.... uid : " + stats.getUid() + " | bitrate : " + stats.getReceivedBitrate()
//                + " | framerate : " + stats.getReceivedFrameRate());
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_VIDEO_STATE;
        mJniObjs.mRemoteVideoStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
//        MyLog.i("wzg", "RemoteAudioStats.... uid : " + stats.getUid() + " | bitrate : " + stats.getReceivedBitrate());
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_AUDIO_STATE;
        mJniObjs.mRemoteAudioStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_LOCAL_VIDEO_STATE;
        mJniObjs.mLocalVideoStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onLocalAudioStats(LocalAudioStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_LOCAL_AUDIO_STATE;
        mJniObjs.mLocalAudioStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onSetSEI(String sei) {
        MyLog.i("wzg", "onSei.... sei : " + sei);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_SEI;
        mJniObjs.mSEI = sei;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onUserMuteAudio(long uid, boolean muted) {
        MyLog.i("wzg", "OnRemoteAudioMuted.... uid : " + uid + " | muted : " + muted + " | mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_MUTE_AUDIO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsDisableAudio = muted;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onSpeakingMuted(long uid, boolean muted) {
        MyLog.i("wzg", "onSpeakingMuted.... uid : " + uid + " | muted : " + muted + " | mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_SPEAK_MUTE_AUDIO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsDisableAudio = muted;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onSpeechRecognized(String str) {

    }

    @Override
    public void onAudioRouteChanged(int routing) {
        MyLog.i("wzg", "onAudioRouteChanged.... routing : " + routing);
        LocalConfig.mCurrentAudioRoute = routing;
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_AUDIO_ROUTE;
        mJniObjs.mAudioRoute = routing;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void onLeaveChannel(RtcStats stats) {
        MyLog.i("wzg", "onLeaveChannel....");
    }

    @Override
    public void onRemoteVideoDecoded(long uid, ConfVideoFrame mFrame) {
//        MyLog.i("wzg", "onRemoteVideoDecoded.... uid : " + uid + " | width : " + mFrame.stride
//                + " | height : " + mFrame.height);
    }

    @Override
    public void onFirstRemoteVideoDecoded(long uid, int width, int height) {
//        MyLog.i("wzg", "onFirstRemoteVideoDecoded.... uid ： " + uid + " | width : " + width + " | height : " + height);
    }

    @Override
    public void onFirstLocalVideoFrame(int width, int height) {
        MyLog.i("wzg", "onFirstLocalVideoFrame.... width : " + width + " | height : " + height);
    }

    @Override
    public void OnChatMessageSent(ChatInfo chatInfo, int error) {
        MyLog.i("wzg", "OnChatMessageSent: ");
    }

    @Override
    public void OnSignalSent(String sSeqID, int error) {
        MyLog.i("wzg", "OnSignalSent: ");
    }

    @Override
    public void OnChatMessageRecived(long nSrcUserID, ChatInfo chatInfo) {
        MyLog.i("wzg", "OnChatMessageRecived: ");
    }

    @Override
    public void onUserRoleChanged(long userID, int userRole) {
        MyLog.i("wzg", "onUserRoleChanged... userID : " + userID + " userRole : " + userRole);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_ROLE_CHANGED;
        mJniObjs.mUid = userID;
        mJniObjs.mIdentity = userRole;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    @Override
    public void OnSignalRecived(long nSrcUserID, String sSeqID, String strData) {
        MyLog.i("wzg", "OnSignalRecived: ");
    }

    @Override
    public void onPlayChatAudioCompletion(String filePath) {
        MyLog.d("wzg", "onPlayChatAudioCompletion: ");
    }

    @Override
    public void onRtcStats(RtcStats stats) {
//        MyLog.i("wzg", "onRtcStats....  " + stats.toString());
    }

    @Override
    public void onScreenRecordTime(int s) {
        MyLog.i("wzg", "onScreenRecordTime: " + s);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_SCREEN_RECORD_TIME;
        mJniObjs.mScreenRecordTime = s;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    private void sendMessage(JniObjs mJniObjs) {
        Intent i = new Intent();
        i.setAction(TAG);
        i.putExtra(MSG_TAG, mJniObjs);
        i.setExtrasClassLoader(JniObjs.class.getClassLoader());
        mContext.sendBroadcast(i);
    }

    public void setIsSaveCallBack(boolean mIsSaveCallBack) {
        this.mIsSaveCallBack = mIsSaveCallBack;
        if (!mIsSaveCallBack) {
            for (int i = 0; i < mSaveCallBack.size(); i++) {
                sendMessage(mSaveCallBack.get(i));
            }
            mSaveCallBack.clear();
        }
    }

    private void saveCallBack(JniObjs mJniObjs) {
        if (mIsSaveCallBack) {
            mSaveCallBack.add(mJniObjs);
        }
    }
}
