package com.tttrtclive.live.callback;

import android.content.Context;
import android.content.Intent;

import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.bean.JniObjs;
import com.tttrtclive.live.ui.MainActivity;
import com.tttrtclive.live.utils.MyLog;
import com.wushuangtech.expansion.bean.LocalAudioStats;
import com.wushuangtech.expansion.bean.LocalVideoStats;
import com.wushuangtech.expansion.bean.RemoteAudioStats;
import com.wushuangtech.expansion.bean.RemoteVideoStats;
import com.wushuangtech.expansion.bean.RtcStats;
import com.wushuangtech.wstechapi.TTTRtcEngineEventHandler;

import java.util.ArrayList;
import java.util.List;

import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_AUDIO_ROUTE;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_CONNECTLOST;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_ENTER_ROOM;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_ERROR;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_LOCAL_AUDIO_STATE;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_LOCAL_VIDEO_STATE;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_MUTE_AUDIO;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_REMOTE_AUDIO_STATE;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_REMOTE_VIDEO_STATE;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_REMOVE_FIRST_FRAME_COME;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_SEI;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_SPEAK_MUTE_AUDIO;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_USER_MUTE_VIDEO;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_USER_OFFLINE;


/**
 * Created by wangzhiguo on 17/10/24.
 */

public class MyTTTRtcEngineEventHandler extends TTTRtcEngineEventHandler {

    public static final String TAG = "MyTTTRtcEngineEventHandler_Live";
    public static final String MSG_TAG = "MyTTTRtcEngineEventHandlerMSG_Live";
    private boolean mIsSaveCallBack;
    private List<JniObjs> mSaveCallBack;
    private Context mContext;

    public MyTTTRtcEngineEventHandler(Context mContext) {
        this.mContext = mContext;
        mSaveCallBack = new ArrayList<>();
    }

    /**
     * 频道加入成功的通知
     */
    @Override
    public void onJoinChannelSuccess(String channel, long uid, int time) {
        MyLog.i("wzg", "onJoinChannelSuccess.... channel ： " + channel + " | uid : " + uid);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ENTER_ROOM;
        mJniObjs.mChannelName = channel;
        mJniObjs.mUid = uid;
        sendMessage(mJniObjs);
        mIsSaveCallBack = true;
    }

    /**
     * 频道退出成功的通知
     * <p>
     * 强烈建议监听该回调，并在接收到该回调后执行其他操作。
     */
    @Override
    public void onLeaveChannel(RtcStats stats) {
        MyLog.i("wzg", "onLeaveChannel...");
    }

    /**
     * 频道加入失败的通知
     */
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

    /**
     * 直播模式下，在频道中活动异常中断通知
     */
    @Override
    public void onUserKicked(long uid, int reason) {
        MyLog.i("wzg", "onUserKicked.... uid ： " + uid + "reason : " + reason + "mIsSaveCallBack : " + mIsSaveCallBack);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_KICK;
        mJniObjs.mErrorType = reason;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 频道中有新用户加入的通知
     */
    @Override
    public void onUserJoined(long nUserId, int identity, int time) {
        MyLog.i("wzg", "onUserJoined.... nUserId ： " + nUserId + " | identity : " + identity
                + " | mIsSaveCallBack : " + mIsSaveCallBack);
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

    /**
     * 频道中有用户退出的通知
     */
    @Override
    public void onUserOffline(long nUserId, int reason) {
        MyLog.i("wzg", "onUserOffline.... nUserId ： " + nUserId + " | reason : " + reason);

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

    /**
     * 网络链路处于中断，并且在尝试重连后也失败，触发该通知
     */
    @Override
    public void onReconnectServerFailed() {
        MyLog.i("wzg", "onReconnectServerFailed.... ");
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_CONNECTLOST;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 频道中某个用户的视频设备可用状态发生改变的通知，如果某个用户视频设备不可用，则接收不到该用户的视频流，即黑屏。
     * <p>
     * enabled true 设备可用，false 设备不可用
     */
    @Override
    public void onUserEnableVideo(long uid, boolean enabled) {
        MyLog.i("wzg", "onUserEnableVideo.... uid : " + uid + " | enabled : " + enabled);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_MUTE_VIDEO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsEnableVideo = enabled;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 画中画布局的通知
     */
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

    /**
     * 本地角色切换成功的通知，加入频道后调用 setClientRole 触发，加入频道前调用不触发
     * <p>
     * 对应接口 setClientRole
     */
    @Override
    public void onClientRoleChanged(long uid, int userRole) {
        super.onClientRoleChanged(uid, userRole);
        MyLog.i("wzg", "onUserRoleChanged... userID : " + uid + " userRole : " + userRole);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_ROLE_CHANGED;
        mJniObjs.mUid = uid;
        mJniObjs.mIdentity = userRole;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 频道中某个用户的音量上报通知 (可选的监听)
     * <p>
     * audioLevel 有小到大范围0~9
     * audioLevelFullRange 有小到大范围0~32767
     * <p>
     * 一般 audioLevel 参数的精度已经够用
     */
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

    /**
     * 接收到频道中某个用户的视频流第一帧，并且已经绘制出来。(可选的监听)
     */
    @Override
    public void onFirstRemoteVideoFrame(long uid, int width, int height, int time) {
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

    /**
     * 远端用户的视频流相关的信息，比如接收的码率。(可选的监听)
     */
    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_VIDEO_STATE;
        mJniObjs.mRemoteVideoStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 远端用户的音频流相关的信息，比如接收的码率。(可选的监听)
     */
    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_AUDIO_STATE;
        mJniObjs.mRemoteAudioStats = stats;
        if (mIsSaveCallBack) {
            saveCallBack(mJniObjs);
        } else {
            sendMessage(mJniObjs);
        }
    }

    /**
     * 本地用户的视频流相关的信息，比如发送的码率。(可选的监听)
     */
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

    /**
     * 本地用户的音频流相关的信息，比如发送的码率。(可选的监听)
     */
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

    /**
     * 远端用户的音频流状态发送改变的通知。(可选的监听)
     * <p>
     * muted true 代表对方停止发送音频流 ，false 代表对方恢复发送音频流
     * <p>
     * 对应接口 muteLocalAudioStream
     */
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

    /**
     * 远端用户的发言权限改变的通知。(可选的监听)
     * <p>
     * muted true 代表对方不可以发送音频流 ，false 代表对方可以发送音频流
     * <p>
     * 对应接口 muteRemoteSpeaking
     */
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

    /**
     * 本地音频路由发生改变，比如插入耳机。(可选的监听)
     */
    @Override
    public void onAudioRouteChanged(int routing) {
        MyLog.i("wzg", "onAudioRouteChanged.... routing : " + routing);
        MainActivity.mCurrentAudioRoute = routing;
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_AUDIO_ROUTE;
        mJniObjs.mAudioRoute = routing;
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
