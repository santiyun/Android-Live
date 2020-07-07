package com.tttrtclive.live.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.EnterUserInfo;
import com.tttrtclive.live.bean.JniObjs;
import com.tttrtclive.live.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.live.callback.PhoneListener;
import com.tttrtclive.live.dialog.ExitRoomDialog;
import com.tttrtclive.live.helper.WEChatShare;
import com.tttrtclive.live.helper.WindowManager;
import com.tttrtclive.live.utils.MyLog;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import androidx.annotation.Nullable;

import static com.tttrtclive.live.ui.SplashActivity.ACTIVITY_MAIN;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private long mAnchorId = -1;
    private long uid;
    private int userRole;
    private String roomId;
    private boolean hqAudio;
    private int videoLevel, videoWidth, videoHeight, videoFps, videoBitrate;
    private int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    private int audioSamplerate;// 0:48000 1:44100
    private int mixEncodeType;//0:H.264  1:H.265

    private TextView mAudioSpeedShow, mVideoSpeedShow, mFpsSpeedShow;
    private ImageView mAudioChannel;
    private ViewGroup mShareLayout;
    private ViewGroup mBigVideoLayout;

    private ExitRoomDialog mExitRoomDialog;
    private AlertDialog.Builder mErrorExitDialog;
    private ProgressDialog mJoinDialog;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private boolean mIsMute = false;
    private boolean mIsHeadset;
    private boolean mIsPhoneComing;
    private boolean mIsSpeaker, mIsBackCamera;
    private boolean mHasLocalView = false;
    private boolean mIsReceiveSei;
    private boolean mIsBackground;

    private boolean needResetLocalVideo;

    private WindowManager mWindowManager;
    private TelephonyManager mTelephonyManager;
    private PhoneListener mPhoneListener;
    private WEChatShare mWEChatShare;
    private Map<Long, Boolean> mUserMutes = new HashMap<>();
    private final Object obj = new Object();

    private SurfaceView mAnchorSurfaceView;
    public static int mCurrentAudioRoute;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        roomId = intent.getStringExtra("roomId");
        uid = intent.getLongExtra("uid", 0);
        userRole = intent.getIntExtra("userRole", 0);
        hqAudio = intent.getBooleanExtra("audio_hq", false);
        videoLevel = intent.getIntExtra("videoLevel", Constants.TTTRTC_VIDEOPROFILE_DEFAULT);
        videoWidth = intent.getIntExtra("videoWidth", 360);
        videoHeight = intent.getIntExtra("videoHeight", 640);
        videoFps = intent.getIntExtra("videoFps", 15);
        videoBitrate = intent.getIntExtra("videoBitrate", 500);
        mPushWidth = intent.getIntExtra("videoMixWidth", 360);
        mPushHeight = intent.getIntExtra("videoMixHeight", 640);
        mPushFrameRate = intent.getIntExtra("videoMixFps", 15);
        mPushBitRate = intent.getIntExtra("videoMixBitrate", 500);
        audioSamplerate = intent.getIntExtra("audioSamplerate", 0);
        mixEncodeType = intent.getIntExtra("mixEncodeType", 0);
        mWEChatShare = new WEChatShare(this);
        initView();
        initData();
        MyLog.d("MainActivity onCreate ...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        mIsBackground = false;
        if (needResetLocalVideo) {
            openLocalVideo();
            needResetLocalVideo = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mIsBackground = true;
    }

    @Override
    public void onBackPressed() {
        mExitRoomDialog.show();
    }

    @Override
    protected void onDestroy() {
        if (mPhoneListener != null && mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
            mPhoneListener = null;
            mTelephonyManager = null;
        }

        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mTTTEngine.muteLocalAudioStream(false);
        if (mIsBackCamera) {
            mTTTEngine.switchCamera();
        }
        super.onDestroy();
        MyLog.d("MainActivity onDestroy... ");
    }

    private void initView() {
        mAudioSpeedShow = findViewById(R.id.main_btn_audioup);
        mVideoSpeedShow = findViewById(R.id.main_btn_videoup);
        mFpsSpeedShow = findViewById(R.id.main_btn_fpsup);
        mAudioChannel = findViewById(R.id.main_btn_audio_channel);
        mAudioChannel.setOnClickListener(this);
        mShareLayout = findViewById(R.id.main_share_layout);
        mBigVideoLayout = findViewById(R.id.local_view_layout);
        View switchCamera = findViewById(R.id.main_btn_switch_camera);
        TextView titleTV = findViewById(R.id.main_btn_title);

        switchCamera.setOnClickListener(this);
        mShareLayout.findViewById(R.id.friend).setOnClickListener(this);
        mBigVideoLayout.setOnClickListener(this);
        findViewById(R.id.main_btn_exit).setOnClickListener(this);
        findViewById(R.id.main_button_share).setOnClickListener(this);
        findViewById(R.id.friend_circle).setOnClickListener(this);
        findViewById(R.id.shared_copy).setOnClickListener(this);
        findViewById(R.id.friend_circle_close).setOnClickListener(this);

        String localChannelName = getString(R.string.ttt_prefix_channel_name) + ":" + roomId;
        titleTV.setText(localChannelName);
        if (userRole != CLIENT_ROLE_ANCHOR) {
            switchCamera.setVisibility(View.GONE);
        }
        // 退房间提示对话框
        mExitRoomDialog = new ExitRoomDialog(mContext, R.style.NoBackGroundDialog);
        mExitRoomDialog.setCanceledOnTouchOutside(false);
        mExitRoomDialog.mConfirmBT.setOnClickListener(v -> {
            exitRoom();
            mExitRoomDialog.dismiss();
        });
        mExitRoomDialog.mDenyBT.setOnClickListener(v -> mExitRoomDialog.dismiss());
        // 异常错误提示对话框
        mErrorExitDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.ttt_error_exit_dialog_title))//设置对话框标题
                .setCancelable(false)
                .setPositiveButton(getString(R.string.ttt_confirm), (dialog, which) -> {//确定按钮的响应事件
                    exitRoom();
                });

        mJoinDialog = new ProgressDialog(this);
        mJoinDialog.setCancelable(false);
        mJoinDialog.setTitle("");
        mJoinDialog.setMessage(getResources().getString(R.string.ttt_hint_loading_channel));
    }

    public void setTextViewContent(TextView textView, int resourceID, String value) {
        String string = getResources().getString(resourceID);
        String result = String.format(string, value);
        textView.setText(result);
    }

    private void initData() {
        mWindowManager = new WindowManager(this);
        if (mCurrentAudioRoute != Constants.AUDIO_ROUTE_SPEAKER) {
            mIsHeadset = true;
            mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_selector);
        }

        mTelephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mPhoneListener = new PhoneListener(this);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }

        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);

        // 启用视频模块功能，不然打开本地或远端视频黑屏
        mTTTEngine.enableVideo();
        // 如果角色是主播，打开自己的本地视频
        if (userRole == Constants.CLIENT_ROLE_ANCHOR) {
            String localUserName = getString(R.string.ttt_prefix_user_name) + ":" + uid;
            ((TextView) findViewById(R.id.main_btn_host)).setText(localUserName);
            openLocalVideo();
        }
        new Thread(this::joinChannel).start();
    }

    private void joinChannel() {
        // 创建 SDK 实例对象，请看 MainApplication 类。
        /*
         * 1.设置频道模式，SDK 默认就是 CHANNEL_PROFILE_COMMUNICATION(通信模式)，这里需要显式调用设置为 CHANNEL_PROFILE_LIVE_BROADCASTING(直播模式)。
         * 注意:该接口是全局接口，离开频道后状态不会清除，所以在模式需要发生变化时调用即可，无需每次加入频道都设置。Demo在这里设置是为了简化代码。
         */
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING); // 必须设置的 API
        /*
         * 2.设置角色身份，CHANNEL_PROFILE_LIVE_BROADCASTING 模式下可以设置三种角色
         * CLIENT_ROLE_ANCHOR(主播) ：频道的创建者，只有主播可以创建频道，创建成功后，其他角色的用户才能加入频道。
         * CLIENT_ROLE_BROADCASTER(副播) ：默认可以收发音视频流。
         * CLIENT_ROLE_AUDIENCE(观众) ：默认音视频流只收不发。
         *
         * SDK 默认是 CLIENT_ROLE_BROADCASTER 角色，Demo 不展示观众角色。
         * 注意:该接口是全局接口，离开频道后状态不会清除，所以在角色需要发生变化时调用即可，无需每次加入频道都设置。Demo在这里设置是为了简化代码。
         */
        mTTTEngine.setClientRole(userRole); // 必须设置的 API
        // 3.设置推流地址，只有主播角色的用户设置有效。该推流地址仅供Demo运行演示使用，不可在正式环境中使用。
        // 必须设置的 API
        if (userRole == CLIENT_ROLE_ANCHOR) {
            String mPushUrlPrefix = "rtmp://push.3ttest.cn/sdk2/";
            String mPushUrl;
            if (mixEncodeType == 0) {
                mPushUrl = mPushUrlPrefix + roomId; // H264视频推流格式，默认使用即可
            } else {
                mPushUrl = mPushUrlPrefix + roomId + "?trans=1"; //H265视频推流格式
            }
            PublisherConfiguration mPublisherConfiguration = new PublisherConfiguration();
            mPublisherConfiguration.setPushUrl(mPushUrl);
            mTTTEngine.configPublisher(mPublisherConfiguration);
        }

        // 可选操作
        optConfigSdk();
        int join = mTTTEngine.joinChannel("", roomId, uid);
        if (join == 0) {
            runOnUiThread(() -> mJoinDialog.show());
        }
    }

    private void optConfigSdk() {
        // 1.设置音频编码参数，SDK 默认为 ISAC 音频编码格式，32kbps 音频码率，适用于通话；高音质选用 AAC 格式编码，码率设置为96kbps。
        //  可选操作的 API
        if (hqAudio) {
            mTTTEngine.setPreferAudioCodec(Constants.TTT_AUDIO_CODEC_AAC, 96, 1);
        } else {
            mTTTEngine.setPreferAudioCodec(Constants.TTT_AUDIO_CODEC_ISAC, 32, 1);
        }
        // 2.设置视频编码参数，SDK 默认为 360P 质量等级。
        // 可选操作的 API
        if (userRole == Constants.CLIENT_ROLE_BROADCASTER) {
            // 若角色为副播，视频质量等级设置为 240P，若感觉视频不清晰，可自行调整等级，如360P。
            mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_240P, false);
        } else {
            // 若角色为主播，视频质量根据 SetActivity 设置界面所设置的参数来决定。
            if (videoLevel != 0) {
                mTTTEngine.setVideoProfile(videoLevel, false);
            } else {
                // 自定义视频参数，而不用 SDK 内部定义的视频质量等级。
                if (videoWidth != 0 && videoHeight != 0 && videoFps != 0 && videoBitrate != 0) {
                    mTTTEngine.setVideoProfile(videoWidth, videoHeight, videoFps, videoBitrate);
                } else {
                    mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_360P, false);
                }
            }
        }
        // 3.设置直播推流，连麦场景下服务器混屏的视频参数。不连麦，即单主播推流场景，无需设置该 API。
        // 可选操作的 API
        if (mPushBitRate != 0 && mPushFrameRate != 0 && mPushHeight != 0 && mPushWidth != 0) {
            mTTTEngine.setVideoMixerParams(mPushBitRate, mPushFrameRate, mPushHeight, mPushWidth, Constants.VIDEO_SERVER_MIX_MODE_NORMAL);
        }
        // 4.设置直播推流，连麦场景下服务器混屏的音频参数。不连麦，即单主播推流场景，无需设置该 API。
        // 可选操作的 API
        mTTTEngine.setAudioMixerParams(audioSamplerate, audioSamplerate == 0 ? 48000 : 44100, 1);
        // 5.启用 sdk 上报所有说话者的音量大小
        mTTTEngine.enableAudioVolumeIndication(300, 3);
    }

    public void exitRoom() {
        MyLog.d("exitRoom was called!... leave room");
        mTTTEngine.leaveChannel();
        setResult(ACTIVITY_MAIN);
        finish();
    }

    private void openLocalVideo() {
        if (userRole == CLIENT_ROLE_ANCHOR && mAnchorSurfaceView == null) {
            mAnchorSurfaceView = TTTRtcEngine.CreateRendererSurfaceView(mContext);
            mAnchorSurfaceView.setZOrderMediaOverlay(false);
            mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN, mAnchorSurfaceView, null), getRequestedOrientation());
            mTTTEngine.startPreview();
        }

        if (userRole == CLIENT_ROLE_ANCHOR) {
            mBigVideoLayout.addView(mAnchorSurfaceView);
        } else if (userRole == CLIENT_ROLE_BROADCASTER) {
            mWindowManager.add(uid, uid, getRequestedOrientation());
        }
    }

    private void closeLocalVideo() {
        runOnUiThread(() -> {
            if (userRole == CLIENT_ROLE_ANCHOR) {
                mBigVideoLayout.removeAllViews();
            } else if (userRole == CLIENT_ROLE_BROADCASTER) {
                mWindowManager.hide(uid);
            }
        });
    }

    private void openRemoteUserVideo(ViewGroup vp, long uid, boolean zOrderMediaOverlay) {
        runOnUiThread(() -> {
            SurfaceView mSurfaceView = TTTRtcEngine.CreateRendererView(mContext);
            mTTTEngine.setupRemoteVideo(new VideoCanvas(uid, Constants.RENDER_MODE_HIDDEN, mSurfaceView));
            mSurfaceView.setZOrderMediaOverlay(zOrderMediaOverlay);
            vp.addView(mSurfaceView);
        });
    }

    private String getWXLink() {
        return "http://3ttech.cn/3tplayer.html?flv=http://pull.3ttest.cn/sdk2/" + roomId + ".flv&hls=http://pull.3ttest.cn/sdk2/" + roomId + ".m3u8";
    }

    private void showErrorExitDialog(String message) {
        if (!TextUtils.isEmpty(message)) {
            String msg = getString(R.string.ttt_error_exit_dialog_prefix_msg) + ": " + message;
            mErrorExitDialog.setMessage(msg);//设置显示的内容
            mErrorExitDialog.show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_button_share:
                mShareLayout.setVisibility(View.VISIBLE);
                break;
            case R.id.main_share_layout:
                mWEChatShare.sendText(SendMessageToWX.Req.WXSceneSession, Long.parseLong(roomId), getWXLink());
                mShareLayout.setVisibility(View.GONE);
                break;
            case R.id.friend_circle:
                mWEChatShare.sendText(SendMessageToWX.Req.WXSceneTimeline, Long.parseLong(roomId), getWXLink());
                mShareLayout.setVisibility(View.GONE);
                break;
            case R.id.friend_circle_close:
                mShareLayout.setVisibility(View.GONE);
                break;
            case R.id.main_btn_exit:
                mExitRoomDialog.show();
                break;
            case R.id.main_btn_audio_channel:
                if (userRole != CLIENT_ROLE_ANCHOR) return;
                mIsMute = !mIsMute;
                if (mIsHeadset)
                    mAudioChannel.setImageResource(mIsMute ? R.drawable.mainly_btn_muted_headset_selector : R.drawable.mainly_btn_headset_selector);
                else
                    mAudioChannel.setImageResource(mIsMute ? R.drawable.mainly_btn_mute_speaker_selector : R.drawable.mainly_btn_speaker_selector);
                mTTTEngine.muteLocalAudioStream(mIsMute);
                break;
            case R.id.main_btn_switch_camera:
                mTTTEngine.switchCamera();
                mIsBackCamera = !mIsBackCamera;
                break;
            case R.id.shared_copy:
                ClipboardManager cm = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(getWXLink());
                Toast.makeText(mContext, getString(R.string.ttt_copy_success), Toast.LENGTH_SHORT).show();
                break;
            case R.id.local_view_layout:
                if (mShareLayout.getVisibility() == View.VISIBLE)
                    mShareLayout.setVisibility(View.GONE);
                break;
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = (JniObjs) intent.getSerializableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                if (mJniObjs == null) {
                    return;
                }
                MyLog.d("UI onReceive callBack... mJniType : " + mJniObjs.mJniType);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM: // 接收到加入频道成功到信令，跳转界面。
                        Toast.makeText(mContext, "加入频道成功", Toast.LENGTH_SHORT).show();
                        mJoinDialog.dismiss();
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR: // 接收加入频道失败的信令，或是sdk运行中出现的错误信令，需要手动调用leaveChannel
                        String errorMsg = "";
                        int errorType = mJniObjs.mErrorType;
                        if (errorType == Constants.ERROR_ENTER_ROOM_INVALIDCHANNELNAME) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_format);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_TIMEOUT) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_timeout);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_VERIFY_FAILED) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_token_invaild);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_BAD_VERSION) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_version);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_CONNECT_FAILED) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_unconnect);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_NOEXIST) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_room_no_exist);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_SERVER_VERIFY_FAILED) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_verification_failed);
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                            errorMsg = mContext.getResources().getString(R.string.ttt_error_enterchannel_unknow);
                        }
                        mJoinDialog.dismiss();
                        showErrorExitDialog(errorMsg);
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_KICK: // 接收到服务器下发到异常退出信令，需要手动调用leaveChannel
                        String message = "";
                        int kickType = mJniObjs.mErrorType;
                        if (kickType == Constants.ERROR_KICK_BY_HOST) {
                            message = getResources().getString(R.string.ttt_error_exit_kicked);
                        } else if (kickType == Constants.ERROR_KICK_BY_PUSHRTMPFAILED) {
                            message = getResources().getString(R.string.ttt_error_exit_push_rtmp_failed);
                        } else if (kickType == Constants.ERROR_KICK_BY_SERVEROVERLOAD) {
                            message = getResources().getString(R.string.ttt_error_exit_server_overload);
                        } else if (kickType == Constants.ERROR_KICK_BY_MASTER_EXIT) {
                            message = getResources().getString(R.string.ttt_error_exit_anchor_exited);
                        } else if (kickType == Constants.ERROR_KICK_BY_RELOGIN) {
                            message = getResources().getString(R.string.ttt_error_exit_relogin);
                        } else if (kickType == Constants.ERROR_KICK_BY_NEWCHAIRENTER) {
                            message = getResources().getString(R.string.ttt_error_exit_other_anchor_enter);
                        } else if (kickType == Constants.ERROR_KICK_BY_NOAUDIODATA) {
                            message = getResources().getString(R.string.ttt_error_exit_noaudio_upload);
                        } else if (kickType == Constants.ERROR_KICK_BY_NOVIDEODATA) {
                            message = getResources().getString(R.string.ttt_error_exit_novideo_upload);
                        } else if (kickType == Constants.ERROR_TOKEN_EXPIRED) {
                            message = getResources().getString(R.string.ttt_error_exit_token_expired);
                        }
                        showErrorExitDialog(message);
                        break;
                    case LocalConstans.CALL_BACK_ON_CONNECTLOST:
                        showErrorExitDialog(getString(R.string.ttt_error_network_disconnected));
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_JOIN: // 接收到用户加入频道
                        long remoteUid = mJniObjs.mUid;
                        int identity = mJniObjs.mIdentity;
                        if (identity == CLIENT_ROLE_ANCHOR) {
                            mAnchorId = remoteUid;
                            String localAnchorName = getString(R.string.ttt_role_anchor) + "ID: " + roomId;
                            ((TextView) findViewById(R.id.main_btn_host)).setText(localAnchorName);
                        }
                        // 如果自己角色是观众
                        if (userRole == CLIENT_ROLE_AUDIENCE) {
                            if (identity == CLIENT_ROLE_ANCHOR) { // 如果用户角色为主播，打开视频
                                if (!mHasLocalView) {
                                    mHasLocalView = true;
                                    openRemoteUserVideo(mBigVideoLayout, remoteUid, true);
                                }
                            } else {
                                mWindowManager.add(uid, remoteUid, getRequestedOrientation());
                            }
                        } else if (userRole == CLIENT_ROLE_ANCHOR) {
                            EnterUserInfo userInfo = new EnterUserInfo(remoteUid, identity);
                            mWindowManager.addAndSendSei(uid, userInfo);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_OFFLINE:
                        long offLineUserID = mJniObjs.mUid;
                        mWindowManager.removeAndSendSei(uid, offLineUserID);
                        break;
                    case LocalConstans.CALL_BACK_ON_SEI:
                        TreeSet<EnterUserInfo> mInfos = new TreeSet<>();
                        try {
                            JSONObject jsonObject = new JSONObject(mJniObjs.mSEI);
                            JSONArray jsonArray = jsonObject.getJSONArray("pos");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonobject2 = (JSONObject) jsonArray.get(i);
                                String devid = jsonobject2.getString("id");
                                float x = Float.valueOf(jsonobject2.getString("x"));
                                float y = Float.valueOf(jsonobject2.getString("y"));

                                long userId;
                                int index = devid.indexOf(":");
                                if (index > 0) {
                                    userId = Long.parseLong(devid.substring(0, index));
                                } else {
                                    userId = Long.parseLong(devid);
                                }
                                MyLog.d("CALL_BACK_ON_SEI", "parse user id : " + userId);
                                if (userId != mAnchorId) {
                                    EnterUserInfo temp = new EnterUserInfo(userId, Constants.CLIENT_ROLE_BROADCASTER);
                                    temp.setXYLocation(x, y);
                                    mInfos.add(temp);
                                } else {
                                    if (!mHasLocalView) {
                                        mHasLocalView = true;
                                        openRemoteUserVideo(mBigVideoLayout, userId, false);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            MyLog.d("CALL_BACK_ON_SEI", "parse xml error : " + e.getLocalizedMessage());
                        }

                        int count = 0;
                        for (EnterUserInfo temp : mInfos) {
                            temp.mShowIndex = count;
                            count++;
                        }

                        for (EnterUserInfo next : mInfos) {
                            MyLog.d("CALL_BACK_ON_SEI", "user list : " + next.getId() + " | index : " + next.mShowIndex);
                            mWindowManager.add(uid, next.getId(), getRequestedOrientation(), next.mShowIndex);
                        }

                        synchronized (obj) {
                            if (mUserMutes.size() > 0) {
                                Set<Map.Entry<Long, Boolean>> entries = mUserMutes.entrySet();
                                for (Map.Entry<Long, Boolean> next : entries) {
                                    mWindowManager.muteAudio(next.getKey(), next.getValue());
                                }
                            }
                            mUserMutes.clear();
                            mIsReceiveSei = true;
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_REMOTE_AUDIO_STATE:
                        if (mJniObjs.mUid != mAnchorId) {
                            String audioString = getResources().getString(R.string.ttt_audio_downspeed);
                            String audioResult = String.format(audioString, String.valueOf(mJniObjs.mAudioRecvBitrate));
                            mWindowManager.updateAudioBitrate(mJniObjs.mUid, audioResult);
                        } else
                            setTextViewContent(mAudioSpeedShow, R.string.ttt_audio_downspeed, String.valueOf(mJniObjs.mAudioRecvBitrate));
                        break;
                    case LocalConstans.CALL_BACK_ON_REMOTE_VIDEO_STATE:
                        if (mJniObjs.mUid != mAnchorId) {
                            String videoString = getResources().getString(R.string.ttt_video_downspeed);
                            String videoResult = String.format(videoString, String.valueOf(mJniObjs.mVideoRecvBitrate));
                            mWindowManager.updateVideoBitrate(mJniObjs.mUid, videoResult);
                        } else
                            setTextViewContent(mVideoSpeedShow, R.string.ttt_video_downspeed, String.valueOf(mJniObjs.mVideoRecvBitrate));
                        break;
                    case LocalConstans.CALL_BACK_ON_LOCAL_AUDIO_STATE:
                        if (userRole == CLIENT_ROLE_ANCHOR)
                            setTextViewContent(mAudioSpeedShow, R.string.ttt_audio_upspeed, String.valueOf(mJniObjs.mAudioSentBitrate));
                        else {
                            String localAudioString = getResources().getString(R.string.ttt_audio_upspeed);
                            String localAudioResult = String.format(localAudioString, String.valueOf(mJniObjs.mAudioSentBitrate));
                            mWindowManager.updateAudioBitrate(uid, localAudioResult);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_LOCAL_VIDEO_STATE:
                        if (userRole == CLIENT_ROLE_ANCHOR) {
                            mFpsSpeedShow.setText("FPS-" + mJniObjs.mVideoSentFps);
                            setTextViewContent(mVideoSpeedShow, R.string.ttt_video_upspeed, String.valueOf(mJniObjs.mVideoSentBitrate));
                        } else {
                            String localVideoString = getResources().getString(R.string.ttt_video_upspeed);
                            String localVideoResult = String.format(localVideoString, String.valueOf(mJniObjs.mVideoSentBitrate));
                            mWindowManager.updateVideoBitrate(uid, localVideoResult);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_MUTE_AUDIO:
                        long muteUid = mJniObjs.mUid;
                        boolean mIsMuteAuido = mJniObjs.mIsDisableAudio;
                        MyLog.i("OnRemoteAudioMuted CALL_BACK_ON_MUTE_AUDIO start! .... " + mJniObjs.mUid
                                + " | mIsMuteAuido : " + mIsMuteAuido);
                        if (muteUid == mAnchorId) {
//                            mIsMute = mIsMuteAuido;
//                            if (mIsHeadset)
//                                mAudioChannel.setImageResource(mIsMuteAuido ? R.drawable.mainly_btn_muted_headset_selector : R.drawable.mainly_btn_headset_selector);
//                            else
//                                mAudioChannel.setImageResource(mIsMuteAuido ? R.drawable.mainly_btn_mute_speaker_selector : R.drawable.mainly_btn_speaker_selector);
                        } else {
                            if (userRole != Constants.CLIENT_ROLE_ANCHOR) {
                                if (mIsReceiveSei) {
                                    mWindowManager.muteAudio(muteUid, mIsMuteAuido);
                                } else {
                                    mUserMutes.put(muteUid, mIsMuteAuido);
                                }
                            } else {
                                mWindowManager.muteAudio(muteUid, mIsMuteAuido);
                            }
                        }
                        break;

                    case LocalConstans.CALL_BACK_ON_AUDIO_ROUTE:
                        int mAudioRoute = mJniObjs.mAudioRoute;
                        if (mAudioRoute == Constants.AUDIO_ROUTE_SPEAKER || mAudioRoute == Constants.AUDIO_ROUTE_HEADPHONE) {
                            mIsHeadset = false;
                            mAudioChannel.setImageResource(mIsMute ? R.drawable.mainly_btn_mute_speaker_selector : R.drawable.mainly_btn_speaker_selector);
                        } else {
                            mIsHeadset = true;
                            mAudioChannel.setImageResource(mIsMute ? R.drawable.mainly_btn_muted_headset_selector : R.drawable.mainly_btn_headset_selector);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_COME:
                        mIsPhoneComing = true;
                        mIsSpeaker = mTTTEngine.isSpeakerphoneEnabled();
                        if (mIsSpeaker) {
                            mTTTEngine.setEnableSpeakerphone(false);
                        }

                        if (!mIsMute) {
                            mTTTEngine.muteLocalAudioStream(true);
                        }
                        mTTTEngine.muteAllRemoteAudioStreams(true);
                        break;
                    case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_IDLE:
                        if (mIsPhoneComing) {
                            if (mIsSpeaker) {
                                mTTTEngine.setEnableSpeakerphone(true);
                            }

                            if (!mIsMute) {
                                mTTTEngine.muteLocalAudioStream(false);
                            }
                            mTTTEngine.muteAllRemoteAudioStreams(false);
                            mIsPhoneComing = false;
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION:
                        if (mIsMute) return;
                        int volumeLevel = mJniObjs.mAudioLevel;
                        if (mJniObjs.mUid == uid) {
                            if (mIsHeadset) {
                                if (volumeLevel >= 0 && volumeLevel <= 3) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_small_selector);
                                } else if (volumeLevel > 3 && volumeLevel <= 6) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_middle_selector);
                                } else if (volumeLevel > 6 && volumeLevel <= 9) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_big_selector);
                                }
                            } else {
                                if (volumeLevel >= 0 && volumeLevel <= 3) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_small_selector);
                                } else if (volumeLevel > 3 && volumeLevel <= 6) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_middle_selector);
                                } else if (volumeLevel > 6 && volumeLevel <= 9) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_big_selector);
                                }
                            }
                        } else {
                            mWindowManager.updateSpeakState(mJniObjs.mUid, mJniObjs.mAudioLevel);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_CAMERA_CONNECT_ERROR:
                        closeLocalVideo();
                        if (!mIsBackground) {
                            openLocalVideo();
                        } else {
                            needResetLocalVideo = true;
                        }
                        break;
                }
            }
        }
    }

}
