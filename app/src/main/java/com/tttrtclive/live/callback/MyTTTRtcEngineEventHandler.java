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
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_USER_JOIN;
import static com.tttrtclive.live.LocalConstans.CALL_BACK_ON_USER_OFFLINE;


/**
 * Demo 自定义 MyTTTRtcEngineEventHandler 类，并继承了 SDK 的 TTTRtcEngineEventHandler 回调基类，接收 SDK 的各种回调通知。
 * 在接收到回调通知后，再通过发送广播，传递给 Activity 做处理。
 * <p/>
 * 注意：通过发送广播将 SDK 的回调信令传递给 Activity 只是 Demo 的逻辑，用户可以参考，但无须过多关注，重点是用户需要自定义类继承 TTTRtcEngineEventHandler
 * 去处理 SDK 的回调通知。
 */
public class MyTTTRtcEngineEventHandler extends TTTRtcEngineEventHandler {

    public static final String TAG = "MyTTTRtcEngineEventHandler_Live";
    public static final String MSG_TAG = "MyTTTRtcEngineEventHandlerMSG_Live";
    private Context mContext;

    public MyTTTRtcEngineEventHandler(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 本地用户加入频道成功的回调。
     * <p/>
     * 表示 App 已经成功登入服务，并且成功分配了频道 ID 和用户 ID。频道 ID 和用户 ID 分配是根据 joinChannel API 中指定的。
     *
     * @param channel 频道名称。
     * @param uid     用户ID。
     * @param elapsed 从 joinChannel 开始到发生此事件过去的时间（毫秒)。
     */
    @Override
    public void onJoinChannelSuccess(String channel, long uid, int elapsed) {
        MyLog.i(TAG, "onJoinChannelSuccess.... channel ： " + channel + " | uid : " + uid);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ENTER_ROOM;
        mJniObjs.mChannelName = channel;
        mJniObjs.mUid = uid;
        sendMessage(mJniObjs);
    }

    /**
     * 本地用户离开频道的回调。
     * <p/>
     * 表示调用 leaveChannel API 执行完毕，SDK 提示 App 离开频道。在该回调中，App 可以得到此次通话的总通话时长、SDK 收发数据的流量等信息。
     *
     * @param stats 此次直播/通话相关的统计信息。
     */
    @Override
    public void onLeaveChannel(RtcStats stats) {
        MyLog.i(TAG, "onLeaveChannel...");
    }

    /**
     * SDK 发生错误回调。
     * <p/>
     * 该回调表示 SDK 运行时出现了错误，无法自动恢复，需要 App 干预或提示用户。
     * <p/>
     * 例如直播模式下，以副播/观众身份加入一个不存在的频道，SDK 会上报 ERROR_ENTER_ROOM_NOEXIST(-6) 错误，App 可以提示用户加入频道失败，并再次尝试或检查频道名称是否正确。
     *
     * @param errorType 错误码，详细定义参考 Constants 类。
     */
    @Override
    public void onError(final int errorType) {
        MyLog.i(TAG, "onError.... errorType ： " + errorType);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_ERROR;
        mJniObjs.mErrorType = errorType;
        sendMessage(mJniObjs);
    }

    /**
     * 本地用户被服务器请出频道的回调。该回调触发的前提条件为本地用户加入频道成功后。
     * <p/>
     * 该回调表示服务器要求 SDK 将本地用户从当前频道中移除，SDK 不会主动调用 leaveChannel，需要 App 主动调用 leaveChannel 并提示用户已离开频道以及
     * 离开的原因。
     * <p/>
     * 例如 CHANNEL_PROFILE_LIVE_BROADCASTING(直播模式) 下，主播有权限将频道内某个用户请离频道，此时被请离的用户会接收到该回调，原因为
     * ERROR_KICK_BY_HOST(101) 。
     *
     * @param uid    本地用户 ID。
     * @param reason 本地用户被服务器请出的原因，详细定义参考 Constants 类。
     */
    @Override
    public void onUserKicked(long uid, int reason) {
        MyLog.i(TAG, "onUserKicked.... uid ： " + uid + "reason : " + reason);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_USER_KICK;
        mJniObjs.mErrorType = reason;
        sendMessage(mJniObjs);
    }

    /**
     * 远端副播用户加入当前频道的回调。
     * <p/>
     * 该回调表示有新的远端 CLIENT_ROLE_BROADCASTER(副播) 用户加入频道，并返回该用户的 ID 以及角色身份。在本地用户加入频道前，
     * 频道中已存在的主播或副播用户，本地用户在加入频道成功后也由该回调提示给 App。
     * <p/>
     * 该回调在以下情况下会被触发：<br/>
     * 远端用户(副播)调用 joinChannel 方法成功加入频道。<br/>
     * 远端用户(观众)加入频道后调用 setClientRole 将用户角色改变为副播。
     * <p/>
     * 注意：CLIENT_ROLE_AUDIENCE(观众) 对于频道内其他人来说是不可见，即以观众的身份加入频道，频道内其他用户不会触发 onUserJoined 回调。<br/>
     *
     * @param uid      加入频道的用户ID。
     * @param identity 加入频道的用户的身份，副播或观众。
     * @param elapsed  从 joinChannel 开始到发生此事件过去的时间（毫秒)。
     */
    @Override
    public void onUserJoined(long uid, int identity, int elapsed) {
        MyLog.i(TAG, "onUserJoined.... uid ： " + uid + " | identity : " + identity);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_JOIN;
        mJniObjs.mUid = uid;
        mJniObjs.mIdentity = identity;
        sendMessage(mJniObjs);
    }

    /**
     * 远端副播用户离开当前频道的回调。
     * <p/>
     * 该回调表示有新的远端 CLIENT_ROLE_BROADCASTER(副播) 用户离开频道，并返回该用户的 ID 以及离开的原因。
     * <p/>
     * 注意：CLIENT_ROLE_AUDIENCE(观众) 对于频道内其他人来说是不可见，即以观众的身份加入频道，离开频道时，频道内其他用户也不会触发 onUserOffline 。<br/>
     *
     * @param uid    离开频道的用户ID。
     * @param reason 用户离开频道的原因，有以下几种：<br/>
     *               USER_OFFLINE_NORMAL(201)：用户主动离开频道。<br/>
     *               USER_OFFLINE_NORMAL(202)：用户网络不好，超时离开。<br/>
     *               USER_OFFLINE_NORMAL(203)：用户网络断线离开。
     */
    @Override
    public void onUserOffline(long uid, int reason) {
        MyLog.i(TAG, "onUserOffline.... uid ： " + uid + " | reason : " + reason);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_USER_OFFLINE;
        mJniObjs.mUid = uid;
        mJniObjs.mReason = reason;
        sendMessage(mJniObjs);
    }

    /**
     * 网络连接中断，SDK 与服务器失去连接，并在规定的时间范围内重连失败的回调。
     * <p/>
     * 当网络异常断开后，SDK 将主动尝试重连，若在限定时间之内(默认90秒，App 可通过 setSignalTimeout API 设置变更)无法重连上服务器，SDK 会
     * 触发此回调通知 App ，App 在收到此回调后应该调用 leaveChannel 离开频道，服务器将会把该用户置为离线状态，并通知给频道内其他用户。
     * <p/>
     * 注意：在直播模式下，如果主播离线，服务器将会把频道置为不可用状态，并通知给频道内其他用户，其他用户会收到 onUserKicked 回调，错误码为
     * ERROR_KICK_BY_MASTER_EXIT(104) 。
     */
    @Override
    public void onReconnectServerFailed() {
        MyLog.i(TAG, "onReconnectServerFailed.... ");
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_CONNECTLOST;
        sendMessage(mJniObjs);
    }

    /**
     * 直播模式下，主播设置的画中画布局参数的回调。
     * <p/>
     * 该回调用于接收服务器下发的画中画布局参数(即 SEI 信息)，获得主播，连麦用户的显示相关信息。
     * <p/>
     * 通常在直播中，主播会与副播之间，跨频道主播之间进行连麦互动，主播会调用 setVideoCompositingLayout API 设置画中画布局，统一每个用户的视频在 CDN 拉流端显示的效果，
     * 连麦的副播在收到此回调时，应根据布局信息，调整本地视频的显示位置。
     *
     * @param sei 画中画布局参数信息。如连麦用户的 ID，视频宽，高，视频左上角x，y 的坐标等信息。
     */
    @Override
    public void onSetSEI(String sei) {
        MyLog.i(TAG, "onSei.... sei : " + sei);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_SEI;
        mJniObjs.mSEI = sei;
        sendMessage(mJniObjs);
    }

    /**
     * 提示频道内谁正在说话、说话者音量及本地用户是否在说话的回调。
     * <p/>
     * 该回调默认禁用，可以通过启用说话者音量提示 enableAudioVolumeIndication 方法开启。
     * <p/>
     * 注意：开启后无论频道内是否有人说话，都会按 enableAudioVolumeIndication API 中设置的时间间隔返回提示音量。
     *
     * @param nUserID             每个说话者的用户 ID 。
     * @param audioLevel          说话者的音量，范围在 0 - 9 之间，一般情况此参数的精度已足够。
     * @param audioLevelFullRange 说话者的音量，范围更大更精细，在0 - 32767.5 之间。
     */
    @Override
    public void onAudioVolumeIndication(long nUserID, int audioLevel, int audioLevelFullRange) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_AUDIO_VOLUME_INDICATION;
        mJniObjs.mUid = nUserID;
        mJniObjs.mAudioLevel = audioLevel;
        sendMessage(mJniObjs);
    }

    /**
     * 已显示远端视频首帧的回调。
     * <p/>
     * 该回调表示某个远端用户的第一帧视频已显示在视图上。App 可在此调用中获知出图时间（elapsed）。
     * <p/>
     * 注意：该回调仅会回调一次，即远端用户首次上线，若远端用户调用 enableLocalVideo 禁用本地的视频功能，该回调不会被再次触发。
     *
     * @param uid     远端用户 ID，指定是哪个用户的视频流。
     * @param width   视频流宽（像素）。
     * @param height  视频流高（像素）。
     * @param elapsed 从本地用户调用 joinChannel 方法直至该回调被触发的延迟（毫秒）。
     */
    @Override
    public void onFirstRemoteVideoFrame(long uid, int width, int height, int elapsed) {
        MyLog.i(TAG, "onFirstRemoteVideoFrame.... uid ： " + uid + " | width : " + width + " | height : " + height);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOVE_FIRST_FRAME_COME;
        mJniObjs.mUid = uid;
        sendMessage(mJniObjs);
    }

    /**
     * 通话/直播中远端视频流统计信息的回调。
     * <p/>
     * 该回调描述远端用户在通话/直播中端到端的视频流统计信息，针对每个远端用户每 2 秒触发一次。如果远端同时存在多个，该回调每 2 秒会被触发多次。
     *
     * @param stats 远端视频相关的统计信息，主要的信息如下：<br/>
     *              mUid：远端用户的 ID ，描述视频流是对应哪个远端用户。<br/>
     *              mReceivedBitrate：下行的码率(kbps) 。<br/>
     *              mVideoLossRate：下行的丢包率(0.xx%) 。
     */
    @Override
    public void onRemoteVideoStats(RemoteVideoStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_VIDEO_STATE;
        mJniObjs.mUid = stats.getUid();
        mJniObjs.mVideoRecvBitrate = stats.getReceivedBitrate();
        sendMessage(mJniObjs);
    }

    /**
     * 通话/直播中远端音频流统计信息的回调。
     * <p/>
     * 该回调描述远端用户在通话/直播中端到端的音频流统计信息，针对每个远端用户每 2 秒触发一次。如果远端同时存在多个，该回调每 2 秒会被触发多次。
     *
     * @param stats 远端音频相关的统计信息，主要的信息如下：<br/>
     *              mUid：远端用户的 ID ，描述音频流是对应哪个远端用户。<br/>
     *              mReceivedBitrate：下行的码率(kbps) 。<br/>
     *              mAudioLossRate：下行的丢包率(0.xx%) 。
     */
    @Override
    public void onRemoteAudioStats(RemoteAudioStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_REMOTE_AUDIO_STATE;
        mJniObjs.mUid = stats.getUid();
        mJniObjs.mAudioRecvBitrate = stats.getReceivedBitrate();
        sendMessage(mJniObjs);
    }

    /**
     * 通话/直播中本地视频流统计信息的回调。
     * <p/>
     * 该回调描述本地设备发送视频流的统计信息，每 2 秒触发一次。
     *
     * @param stats 本地视频相关的统计信息，主要的信息如下：<br/>
     *              mSentBitrate：上行的码率(kbps) 。<br/>
     *              mSentFrameRate：上行的帧率(fps) 。<br/>
     *              mVideoLossRate：上行的丢包率(0.xx%) 。
     */
    @Override
    public void onLocalVideoStats(LocalVideoStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_LOCAL_VIDEO_STATE;
        mJniObjs.mVideoSentBitrate = stats.getSentBitrate();
        sendMessage(mJniObjs);
    }

    /**
     * 通话/直播中本地音频流统计信息的回调。
     * <p/>
     * 该回调描述本地设备发送音频流的统计信息，每 2 秒触发一次。
     *
     * @param stats 本地音频相关的统计信息，主要的信息如下：<br/>
     *              mSentBitrate：上行的码率(kbps) 。<br/>
     *              mAudioLossRate：上行的丢包率(0.xx%) 。
     */
    @Override
    public void onLocalAudioStats(LocalAudioStats stats) {
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_LOCAL_AUDIO_STATE;
        mJniObjs.mAudioSentBitrate = stats.getSentBitrate();
        sendMessage(mJniObjs);
    }

    /**
     * 远端用户停止/恢复发送音频流回调。该回调触发的前提条件为本地用户加入频道成功后。
     * <p/>
     * 该回调表示频道内某个用户将他的音频流静音/取消静音。
     * <p/>
     * 该回调在以下情况下会被触发：<br/>
     * 远端用户调用 joinChannel 方法成功加入频道，首次上线。<br/>
     * 远端用户调用 muteLocalAudioStream API 关闭或开启音频流发送。
     *
     * @param uid   用户ID 。
     * @param muted true: 表示该用户关闭了音频流发送，false：表示该用户开启了音频流发送。
     */
    @Override
    public void onUserMuteAudio(long uid, boolean muted) {
        MyLog.i(TAG, "OnRemoteAudioMuted.... uid : " + uid + " | muted : " + muted);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_MUTE_AUDIO;
        mJniObjs.mUid = uid;
        mJniObjs.mIsDisableAudio = muted;
        sendMessage(mJniObjs);
    }

    /**
     * 语音路由已变更的回调。
     * <p/>
     * 该回调表示在用户插拔耳机，连接/断开蓝牙耳机，或 App 调用 setEnableSpeakerphone 成功时，触发此回调。SDK 会通知 App 语音路由状态已发生变化。
     * 当前的语音路由已切换至听筒，外放(扬声器)，耳机或蓝牙。
     *
     * @param routing 当前已切换到的语音路由： <br/>
     *                Constants.AUDIO_ROUTE_HEADSET(0) ：耳机。 <br/>
     *                Constants.AUDIO_ROUTE_SPEAKER(1) ：扬声器。 <br/>
     *                Constants.AUDIO_ROUTE_HEADPHONE(2) ：听筒。 <br/>
     *                Constants.AUDIO_ROUTE_HEADSETNOMIC(3) ：不带麦的耳机。 <br/>
     *                Constants.AUDIO_ROUTE_HEADSETBLUETOOTH(4) ：蓝牙耳机。
     */
    @Override
    public void onAudioRouteChanged(int routing) {
        MyLog.i(TAG, "onAudioRouteChanged.... routing : " + routing);
        MainActivity.mCurrentAudioRoute = routing;
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = CALL_BACK_ON_AUDIO_ROUTE;
        mJniObjs.mAudioRoute = routing;
        sendMessage(mJniObjs);
    }

    /**
     * 摄像头链接中断
     * <p/>
     * 若 SDK 在打开本地摄像头时发生失败，或者在使用摄像头期间出现了异常情况，SDK 将通过此回调上报给 app 。app 在接收到此回调后，应该重新设置本地视频，即把 SurfaceView 控件从布局中移除再添加即可。
     */
    @Override
    public void onCameraConnectError(int errorType) {
        MyLog.i(TAG, "onCameraConnectError... errorType : " + errorType);
        JniObjs mJniObjs = new JniObjs();
        mJniObjs.mJniType = LocalConstans.CALL_BACK_ON_CAMERA_CONNECT_ERROR;
        mJniObjs.mErrorType = errorType;
        sendMessage(mJniObjs);
    }

    private void sendMessage(JniObjs mJniObjs) {
        Intent i = new Intent();
        i.setAction(TAG);
        i.putExtra(MSG_TAG, mJniObjs);
        mContext.sendBroadcast(i);
    }
}
