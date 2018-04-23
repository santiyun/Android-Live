package com.tttrtclive.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.Helper.TTTRtcEngineHelper;
import com.tttrtclive.LocalConfig;
import com.tttrtclive.LocalConstans;
import com.tttrtclive.MainApplication;
import com.tttrtclive.R;
import com.tttrtclive.bean.DisplayDevice;
import com.tttrtclive.bean.EnterUserInfo;
import com.tttrtclive.bean.JniObjs;
import com.tttrtclive.bean.VideoViewObj;
import com.tttrtclive.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.callback.PhoneListener;
import com.tttrtclive.dialog.DataInfoShowCallback;
import com.tttrtclive.dialog.ExitRoomDialog;
import com.tttrtclive.dialog.MoreInfoDialog;
import com.tttrtclive.dialog.MusicListDialog;
import com.tttrtclive.utils.MyLog;
import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.screenrecorder.ScreenCapture;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.wstechapi.model.VideoCanvas;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends BaseActivity implements DataInfoShowCallback {

    public static final int MSG_TYPE_ERROR_ENTER_ROOM = 0;
    public static final int DISCONNECT = 100;

    public ViewGroup mFullScreenShowView;

    public ArrayList<VideoViewObj> mLocalSeiList;
    public HashSet<Long> mMutedAudioUserID;
    public List<EnterUserInfo> listData;
    public ConcurrentHashMap<Long, DisplayDevice> mShowingDevices;

    public Handler mHandler;

    public TextView mAudioSpeedShow;
    public TextView mVideoSpeedShow;
    private TextView mBroadcasterID;
    public ImageView mAudioChannel;
    private View mCannelMusicBT;
    private View mLocalMusicListBT;
    public View mRecordScreenBT;
    public TextView mRecordScreen;
    public ImageView mRecordScreenShare;
    private View mReversalCamera;
    public ScrollView mScrollView;

    private MoreInfoDialog mMoreInfoDialog;
    private MusicListDialog mMusicListDialog;
    private ExitRoomDialog mExitRoomDialog;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;
    private boolean mIsMute;
    private boolean mIsPhoneComing;
    private boolean mIsStop;
    private boolean mIsSpeaker;
    public boolean mIsHeadset;

    private TelephonyManager mTelephonyManager;
    private PhoneListener mPhoneListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_videochat);
        mTTTRtcEngineHelper = new TTTRtcEngineHelper(this);
        initView();
        initData();
        initEngine();
        initDialog();
        mTelephonyManager = (TelephonyManager) getSystemService(Service.TELEPHONY_SERVICE);
        mPhoneListener = new PhoneListener(this);
        if (mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        }
        mTTTEngine.enableAudioVolumeIndication(300, 3);
        MyLog.d("MainActivity onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.d("MainActivity onStart mIsMute : " + mIsMute);
        mIsStop = false;
        if (LocalConfig.mRole != Constants.CLIENT_ROLE_AUDIENCE) {
            mTTTEngine.resumeAudioMixing();
            if (!mIsMute && !LocalConfig.mLocalMuteAuido) {
                mTTTEngine.muteLocalAudioStream(false);
            }
            mTTTEngine.muteLocalVideoStream(false);
        }
        mTTTEngine.muteAllRemoteAudioStreams(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.d("MainActivity onStop");
        mIsStop = true;
        if (LocalConfig.mRole != Constants.CLIENT_ROLE_AUDIENCE) {
            mTTTEngine.pauseAudioMixing();
            mTTTEngine.muteLocalAudioStream(true);
            mTTTEngine.muteLocalVideoStream(true);
        }
        mTTTEngine.muteAllRemoteAudioStreams(true);
    }

    @Override
    public void onBackPressed() {
        mExitRoomDialog.show();
    }

    @Override
    protected void onDestroy() {
        mMoreInfoDialog.dismiss();
        mMusicListDialog.dismiss();
        LocalConfig.mUserEnterOrder.clear();
        for (int i = 0; i < mLocalSeiList.size(); i++) {
            VideoViewObj videoViewObj = mLocalSeiList.get(i);
            videoViewObj.clear();
        }

        if (mPhoneListener != null && mTelephonyManager != null) {
            mTelephonyManager.listen(mPhoneListener, PhoneStateListener.LISTEN_NONE);
            mPhoneListener = null;
            mTelephonyManager = null;
        }
        mHandler.removeCallbacksAndMessages(null);
        unregisterReceiver(mLocalBroadcast);
        mTTTEngine.stopScreenRecorder();
        super.onDestroy();
        MyLog.d("MainActivity onDestroy");
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        mTTTRtcEngineHelper.handlerRemoteDialogVisibile(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScreenCapture.CAPTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            mTTTRtcEngineHelper.realStartCapture(data);
        } else {
            mTTTRtcEngineHelper.mFlagRecord = 0;
        }
    }

    private void initView() {
        mAudioSpeedShow = findViewById(R.id.main_btn_audioup);
        mVideoSpeedShow = findViewById(R.id.main_btn_videoup);
        mFullScreenShowView = findViewById(R.id.main_background);
        mBroadcasterID = findViewById(R.id.main_btn_host);
        mCannelMusicBT = findViewById(R.id.main_btn_cannel_music);
        mLocalMusicListBT = findViewById(R.id.main_btn_music_channel);
        mAudioChannel = findViewById(R.id.main_btn_audio_channel);
        TextView mHourseID = findViewById(R.id.main_btn_title);
        mReversalCamera = findViewById(R.id.main_btn_camera);
        mRecordScreenBT = findViewById(R.id.main_btn_video_recorder);
        mRecordScreenShare = findViewById(R.id.main_btn_video_share);
        mRecordScreen = findViewById(R.id.main_btn_video_recorder_time);
        mScrollView = findViewById(R.id.main_btn_listly);

        mReversalCamera.setOnClickListener(v -> mTTTEngine.switchCamera());

        setTextViewContent(mHourseID, R.string.main_title, String.valueOf(LocalConfig.mLoginRoomID));

        findViewById(R.id.main_btn_exit).setOnClickListener((v) -> mExitRoomDialog.show());

        findViewById(R.id.main_btn_more).setOnClickListener(v -> mMoreInfoDialog.show());

        mLocalMusicListBT.setOnClickListener(v -> mMusicListDialog.show());

        mCannelMusicBT.setOnClickListener(v -> {
            mTTTEngine.stopAudioMixing();
            mMusicListDialog.setPlaying(false);
            mCannelMusicBT.setVisibility(View.INVISIBLE);
        });

        mAudioChannel.setOnClickListener(v -> {
            if (mIsMute) {
                if (mIsHeadset) {
                    mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_selector);
                } else {
                    mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_selector);
                }
                mTTTEngine.muteLocalAudioStream(false);
                mIsMute = false;
            } else {
                if (mIsHeadset) {
                    mAudioChannel.setImageResource(R.drawable.mainly_btn_muted_headset_selector);
                } else {
                    mAudioChannel.setImageResource(R.drawable.mainly_btn_mute_speaker_selector);
                }
                mTTTEngine.muteLocalAudioStream(true);
                mIsMute = true;
            }
        });

        mRecordScreenBT.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Toast.makeText(mContext, "录制屏幕功能仅支持安卓5.0以上系统", Toast.LENGTH_SHORT).show();
                return;
            }
            startScreenRecord();
        });

        mRecordScreenShare.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Toast.makeText(mContext, "屏幕分享功能仅支持安卓5.0以上系统", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mTTTRtcEngineHelper.mIsRecordering) {
                return;
            }

            if (mTTTRtcEngineHelper.mIsShareRecordering) {
                mTTTRtcEngineHelper.mIsShareRecordering = false;
                mTTTEngine.shareScreenRecorder(false);
                mTTTEngine.stopScreenRecorder();
                mRecordScreenShare.setImageResource(R.drawable.mainly_btn_video_share);
            } else {
                mTTTEngine.shareScreenRecorder(true);
                mTTTEngine.tryScreenRecorder(this);
                mTTTRtcEngineHelper.mFlagRecord = TTTRtcEngineHelper.RECORD_TYPE_SHARE;
            }
        });
    }

    private void startScreenRecord() {
        if (mTTTRtcEngineHelper.mIsShareRecordering) {
            return;
        }

        if (mTTTRtcEngineHelper.mIsRecordering) {
            mTTTRtcEngineHelper.mIsRecordering = false;
            mTTTEngine.stopScreenRecorder();
            mRecordScreen.setVisibility(View.GONE);
            mRecordScreen.setText("00:00:00");
            mRecordScreenBT.setBackgroundResource(R.drawable.mainly_btn_video_recorder);
        } else {
            mTTTRtcEngineHelper.mFlagRecord = TTTRtcEngineHelper.RECORD_TYPE_FILE;
            mTTTEngine.tryScreenRecorder(this);
        }
    }

    public void setTextViewContent(TextView textView, int resourceID, String value) {
        String string = getResources().getString(resourceID);
        String result = String.format(string, value);
        textView.setText(result);
    }

    private void initEngine() {
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        ((MainApplication) getApplicationContext()).mMyTTTRtcEngineEventHandler.setIsSaveCallBack(false);
        // 设置视频分辨率
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
            mTTTEngine.setVideoProfile(Constants.VIDEO_PROFILE_120P, true);
        } else if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            mTTTEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);
        }
    }

    private void initDialog() {
        mMoreInfoDialog =
                new MoreInfoDialog(mContext, R.style.NoBackGroundDialog);
        mMoreInfoDialog.setDataInfoShowCallback(this);

        mExitRoomDialog = new ExitRoomDialog(mContext, R.style.NoBackGroundDialog);
        mExitRoomDialog.setCanceledOnTouchOutside(false);
        mExitRoomDialog.mConfirmBT.setOnClickListener(v -> {
            mTTTEngine.stopScreenRecorder();
            exitRoom();
            mExitRoomDialog.dismiss();
        });

        mExitRoomDialog.mDenyBT.setOnClickListener(v -> mExitRoomDialog.dismiss());

        mMusicListDialog = new MusicListDialog(mContext, R.style.NoBackGroundDialog);
        mMusicListDialog.setCanceledOnTouchOutside(true);
        mMusicListDialog.setMusicListOnClickListener(new MusicListDialog.MusicListOnClickListener() {
            @Override
            public void startAudioMixing(String filePath, boolean loopback, boolean replace, int cycle) {
                mTTTEngine.startAudioMixing(filePath, loopback, replace, cycle);
            }

            @Override
            public void stopAudioMixing() {
                mTTTEngine.stopAudioMixing();
            }

            @Override
            public void showCannelMusicPlayingBT() {
                mCannelMusicBT.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initData() {
        listData = new ArrayList<>();
        mShowingDevices = new ConcurrentHashMap<>();
        mMutedAudioUserID = new HashSet<>();
        if (mHandler == null) {
            mHandler = new LocalHandler();
        }
        mLocalSeiList = new ArrayList<>();
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_AUDIENCE) {
            LocalConfig.mAudience++;
        } else if (LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR) {
            EnterUserInfo localInfo = new EnterUserInfo(LocalConfig.mLoginUserID, Constants.CLIENT_ROLE_ANCHOR);
            addListData(localInfo);
        }

        mTTTRtcEngineHelper.initRemoteLayout(mLocalSeiList);

        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            SurfaceView localSurfaceView;
            localSurfaceView = mTTTEngine.CreateRendererView(mContext);
            localSurfaceView.setZOrderMediaOverlay(false);
            mTTTEngine.setupLocalVideo(new VideoCanvas(0, Constants.RENDER_MODE_HIDDEN,
                    localSurfaceView), getRequestedOrientation());
            mFullScreenShowView.addView(localSurfaceView, 0);
        }

        if (LocalConfig.mRole != Constants.CLIENT_ROLE_BROADCASTER) {
            mAudioChannel.setClickable(false);
        } else {
            mAudioChannel.setClickable(true);
        }

        TextView mRoleShow = findViewById(R.id.main_btn_role);
        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
            setTextViewContent(mBroadcasterID, R.string.main_broadcaster, String.valueOf(LocalConfig.mLoginUserID));
        }

        switch (LocalConfig.mRole) {
            case Constants.CLIENT_ROLE_BROADCASTER:
                setTextViewContent(mRoleShow, R.string.main_local_role, "主播");
                LocalConfig.mBroadcasterID = LocalConfig.mLoginUserID;
                break;
            case Constants.CLIENT_ROLE_ANCHOR:
                setTextViewContent(mRoleShow, R.string.main_local_role, "副播");
                mLocalMusicListBT.setVisibility(View.GONE);
                mReversalCamera.setVisibility(View.GONE);
                mRecordScreenBT.setVisibility(View.GONE);
                mRecordScreenShare.setVisibility(View.GONE);
                break;
            case Constants.CLIENT_ROLE_AUDIENCE:
                setTextViewContent(mRoleShow, R.string.main_local_role, "观众");
                mLocalMusicListBT.setVisibility(View.GONE);
                mReversalCamera.setVisibility(View.GONE);
                mRecordScreenBT.setVisibility(View.GONE);
                mRecordScreenShare.setVisibility(View.GONE);
                break;
        }

        if (LocalConfig.mCurrentAudioRoute != Constants.AUDIO_ROUTE_SPEAKER) {
            mIsHeadset = true;
            if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_selector);
            } else {
                for (int i = 0; i < mLocalSeiList.size(); i++) {
                    VideoViewObj videoCusSei = mLocalSeiList.get(i);
                    if (videoCusSei.mBindUid == LocalConfig.mLoginUserID) {
                        videoCusSei.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_selector);
                        break;
                    }
                }
            }
        }
    }

    private void addListData(EnterUserInfo info) {
        boolean bupdate = false;
        for (int i = 0; i < listData.size(); i++) {
            EnterUserInfo info1 = listData.get(i);
            if (info1.getId() == info.getId()) {
                listData.set(i, info);
                bupdate = true;
                break;
            }
        }
        if (!bupdate) {
            listData.add(info);
        }
    }

    private EnterUserInfo removeListData(long uid) {
        int index = -1;
        for (int i = 0; i < listData.size(); i++) {
            EnterUserInfo info1 = listData.get(i);
            if (info1.getId() == uid) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            return listData.remove(index);
        }
        return null;
    }

    public void exitRoom() {
        MyLog.d("exitRoom was called!... leave room , id : " + LocalConfig.mLoginUserID);
        mTTTEngine.stopAudioMixing();
        mTTTEngine.leaveChannel();
        LocalConfig.mLocalMuteAuido = false;
        finish();
    }

    @Override
    public void showDataInfo(boolean isShow) {
        Set<Map.Entry<Long, DisplayDevice>> entries = mShowingDevices.entrySet();
        if (isShow) {
            mAudioSpeedShow.setVisibility(View.VISIBLE);
            mVideoSpeedShow.setVisibility(View.VISIBLE);
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                next.getValue().getDisplayView().mContentRoot.findViewById(R.id.videoly_video_down).setVisibility(View.VISIBLE);
                next.getValue().getDisplayView().mContentRoot.findViewById(R.id.videoly_audio_down).setVisibility(View.VISIBLE);
            }
        } else {
            mAudioSpeedShow.setVisibility(View.INVISIBLE);
            mVideoSpeedShow.setVisibility(View.INVISIBLE);
            for (Map.Entry<Long, DisplayDevice> next : entries) {
                next.getValue().getDisplayView().mContentRoot.findViewById(R.id.videoly_video_down).setVisibility(View.INVISIBLE);
                next.getValue().getDisplayView().mContentRoot.findViewById(R.id.videoly_audio_down).setVisibility(View.INVISIBLE);
            }
        }
    }

    private class LocalHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TYPE_ERROR_ENTER_ROOM:
                    String message = "";
                    int errorType = (int) msg.obj;
                    if (errorType == Constants.ERROR_KICK_BY_HOST) {
                        message = "被主播踢出";
                    } else if (errorType == Constants.ERROR_KICK_BY_PUSHRTMPFAILED) {
                        message = "rtmp推流失败";
                    } else if (errorType == Constants.ERROR_KICK_BY_SERVEROVERLOAD) {
                        message = "服务器过载";
                    } else if (errorType == Constants.ERROR_KICK_BY_MASTER_EXIT) {
                        message = "主播已退出";
                    } else if (errorType == Constants.ERROR_KICK_BY_RELOGIN) {
                        message = "重复登录";
                    } else if (errorType == Constants.ERROR_KICK_BY_NEWCHAIRENTER) {
                        message = "其他人以主播身份进入";
                    } else if (errorType == Constants.ERROR_KICK_BY_NOAUDIODATA) {
                        message = "长时间没有上行音频数据";
                    } else if (errorType == Constants.ERROR_KICK_BY_NOVIDEODATA) {
                        message = "长时间没有上行视频数据";
                    } else if (errorType == DISCONNECT) {
                        message = "网络连接断开，请检查网络";
                    }
                    mTTTRtcEngineHelper.showErrorExitDialog(message);
                    break;
            }
        }
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(
                        MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        MyLog.d("UI onReceive CALL_BACK_ON_ERROR... ");
                        Message.obtain(mHandler, MSG_TYPE_ERROR_ENTER_ROOM, mJniObjs.mErrorType).sendToTarget();
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_JOIN:
                        long uid = mJniObjs.mUid;
                        int identity = mJniObjs.mIdentity;
//                        Log.d("zhx", "onReceive: CALL_BACK_ON_USER_JOIN uid:" + uid);
                        MyLog.d("UI onReceive CALL_BACK_ON_USER_JOIN... uid : " + uid);
                        EnterUserInfo userInfo = new EnterUserInfo(uid, identity);
                        addListData(userInfo);
                        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                            if (identity == Constants.CLIENT_ROLE_ANCHOR) {
                                if (LocalConfig.mAuthorSize == TTTRtcEngineHelper.AUTHOR_MAX_NUM) {
                                    mTTTEngine.kickChannelUser(uid);
                                    return;
                                }
                            }

                            if (identity == Constants.CLIENT_ROLE_ANCHOR) {
                                // 打开视频
                                DisplayDevice mRemoteDisplayDevice = mShowingDevices.get(uid);
                                if (mRemoteDisplayDevice == null) {
                                    mTTTRtcEngineHelper.adJustRemoteViewDisplay(true, userInfo);
                                }
                            } else {
                                // 向观众发送sei
                                VideoCompositingLayout layout = new VideoCompositingLayout();
                                layout.regions = mTTTRtcEngineHelper.buildRemoteLayoutLocation();
                                mTTTEngine.setVideoCompositingLayout(layout);
                                LocalConfig.mAudience++;
                            }
                        } else {
                            if (identity == Constants.CLIENT_ROLE_BROADCASTER) {
                                LocalConfig.mBroadcasterID = uid;
                                setTextViewContent(mBroadcasterID, R.string.main_broadcaster, String.valueOf(uid));
                                DisplayDevice mRemoteDisplayDevice = mShowingDevices.get(uid);
                                if (mRemoteDisplayDevice == null) {
                                    mTTTRtcEngineHelper.adJustRemoteViewDisplay(true, userInfo);
                                }
                            } else if (identity == Constants.CLIENT_ROLE_ANCHOR) {
                                DisplayDevice mRemoteDisplayDevice = mShowingDevices.get(uid);
                                if (mRemoteDisplayDevice == null) {
                                    mTTTRtcEngineHelper.adJustRemoteViewDisplay(true, userInfo);
                                }
                            } else if (identity == Constants.CLIENT_ROLE_AUDIENCE) {
                                LocalConfig.mAudience++;
                            }
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_USER_OFFLINE:
                        long offLineUserID = mJniObjs.mUid;
                        MyLog.d("UI onReceive CALL_BACK_ON_USER_OFFLINE... offLineUserID : " + offLineUserID);
                        EnterUserInfo enterUserInfo = removeListData(offLineUserID);
                        if (enterUserInfo != null) {
                            if (enterUserInfo.getRole() == Constants.CLIENT_ROLE_AUDIENCE) {
                                LocalConfig.mAudience--;
                            }
                            mTTTRtcEngineHelper.mRemoteUserLastVideoData.remove(enterUserInfo.getId());
                            mTTTRtcEngineHelper.mRemoteUserLastAudioData.remove(enterUserInfo.getId());
                            mTTTRtcEngineHelper.adJustRemoteViewDisplay(false, enterUserInfo);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_AUDIO_VOLUME_INDICATION:
                        mTTTRtcEngineHelper.audioVolumeIndication(mJniObjs.mUid, mJniObjs.mAudioLevel, mIsMute);
                        break;
                    case LocalConstans.CALL_BACK_ON_SEI:
                        String sei = mJniObjs.mSEI;
                        TreeSet<EnterUserInfo> mInfos = new TreeSet<>();
                        try {
                            JSONObject jsonObject = new JSONObject(sei);
                            String mid = (String) jsonObject.get("mid");
                            LocalConfig.mBroadcasterID = Integer.valueOf(mid);
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

                                if (userId != LocalConfig.mBroadcasterID) {
                                    for (int j = 0; j < listData.size(); j++) {
                                        EnterUserInfo temp = listData.get(j);
                                        if (temp.getId() == userId) {
                                            temp.setXYLocation(y, x);
                                            mInfos.add(temp);
                                            break;
                                        }
                                    }
                                }

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        mTTTRtcEngineHelper.removeErrorIndexView(mInfos);
                        Iterator<EnterUserInfo> iterator = mInfos.iterator();
                        while (iterator.hasNext()) {
                            EnterUserInfo next = iterator.next();
                            mTTTRtcEngineHelper.adJustRemoteViewDisplay(true, next);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_REMOTE_VIDEO_STATE:
                        mTTTRtcEngineHelper.remoteVideoStatus(mJniObjs.mRemoteVideoStats);
                        break;
                    case LocalConstans.CALL_BACK_ON_REMOTE_AUDIO_STATE:
                        mTTTRtcEngineHelper.remoteAudioStatus(mJniObjs.mRemoteAudioStats);
                        break;
                    case LocalConstans.CALL_BACK_ON_LOCAL_VIDEO_STATE:
                        mTTTRtcEngineHelper.localVideoStatus(mJniObjs.mLocalVideoStats);
                        break;
                    case LocalConstans.CALL_BACK_ON_LOCAL_AUDIO_STATE:
                        mTTTRtcEngineHelper.LocalAudioStatus(mJniObjs.mLocalAudioStats);
                        break;
                    case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_COME:
                        mIsPhoneComing = true;
                        Log.d("WebRtcAudioRecord COME", "mIsStop : " + mIsStop);
                        mIsSpeaker = mTTTEngine.isSpeakerphoneEnabled();
                        if (mIsSpeaker) {
                            mTTTEngine.setEnableSpeakerphone(false);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_PHONE_LISTENER_IDLE:
                        Log.d("WebRtcAudioRecord IDLE", "mIsStop : " + mIsStop
                                + " | mIsPhoneComing ： " + mIsPhoneComing);
                        if (mIsPhoneComing) {
                            if (mIsSpeaker) {
                                mTTTEngine.setEnableSpeakerphone(true);
                            }
                            mIsPhoneComing = false;
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_SCREEN_RECORD_TIME:
                        if (!mTTTRtcEngineHelper.mIsShareRecordering) {
                            runOnUiThread(() -> {
                                String s = mTTTRtcEngineHelper.showTimeCount(mJniObjs.mScreenRecordTime);
                                mRecordScreen.setText(s);
                            });
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_MUTE_AUDIO:
                        long muteUid = mJniObjs.mUid;
                        boolean mIsMuteAuido = mJniObjs.mIsDisableAudio;
                        PviewLog.i("OnRemoteAudioMuted CALL_BACK_ON_MUTE_AUDIO start! .... " + mJniObjs.mUid
                            + " | mIsMuteAuido : " + mIsMuteAuido);
                        boolean mIsFound = false;
                        for (int i = 0; i < mLocalSeiList.size(); i++) {
                            VideoViewObj videoCusSei = mLocalSeiList.get(i);
                            if (videoCusSei.mBindUid == muteUid) {
                                mIsFound = true;
                                PviewLog.i("OnRemoteAudioMuted find it .... " + mJniObjs.mUid);
                                if (mIsMuteAuido) {
                                    videoCusSei.mIsRemoteDisableAudio = true;
                                    videoCusSei.mMuteVoiceIcon.setVisibility(View.VISIBLE);
                                    videoCusSei.mSpeakImage.setVisibility(View.INVISIBLE);
                                    videoCusSei.mIsMuted = true;
                                    videoCusSei.mMuteVoiceBT.setText("取消禁言");
                                } else {
                                    videoCusSei.mIsRemoteDisableAudio = false;
                                    videoCusSei.mMuteVoiceIcon.setVisibility(View.INVISIBLE);
                                    videoCusSei.mSpeakImage.setVisibility(View.VISIBLE);
                                    videoCusSei.mIsMuted = false;
                                    videoCusSei.mMuteVoiceBT.setText("禁言");

                                    if (muteUid == LocalConfig.mLoginUserID) {
                                        if (videoCusSei.mIsMuteRemote) {
                                            mTTTRtcEngineHelper.speakMuteClick(videoCusSei);
                                        }
                                    }
                                }
                                break;
                            }
                        }

//                        Log.d("zhx", "onReceive: mIsFound:" + mIsFound + " mJniObjs.mUid:" + mJniObjs.mUid + " LocalConfig.mBroadcasterID:" + LocalConfig.mBroadcasterID + " mIsMuteAudio:" + mIsMuteAuido);
                        if (!mIsFound && mJniObjs.mUid != LocalConfig.mBroadcasterID && mIsMuteAuido) {
                            PviewLog.i("OnRemoteAudioMuted could't find it .... " + mJniObjs.mUid);
                            mMutedAudioUserID.add(mJniObjs.mUid);
                        }
                        break;
                    case LocalConstans.CALL_BACK_ON_AUDIO_ROUTE:
                        int mAudioRoute = mJniObjs.mAudioRoute;
                        if (LocalConfig.mRole == Constants.CLIENT_ROLE_BROADCASTER) {
                            if (mAudioRoute == Constants.AUDIO_ROUTE_SPEAKER) {
                                mIsHeadset = false;
                                if (mIsMute) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_mute_speaker_selector);
                                } else {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_speaker_selector);
                                }
                            } else {
                                mIsHeadset = true;
                                if (mIsMute) {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_muted_headset_selector);
                                } else {
                                    mAudioChannel.setImageResource(R.drawable.mainly_btn_headset_selector);
                                }
                            }
                        } else if(LocalConfig.mRole == Constants.CLIENT_ROLE_ANCHOR){
                            for (int i = 0; i < mLocalSeiList.size(); i++) {
                                VideoViewObj videoCusSei = mLocalSeiList.get(i);
                                if (videoCusSei.mBindUid == LocalConfig.mLoginUserID) {
                                    if (mAudioRoute == Constants.AUDIO_ROUTE_SPEAKER) {
                                        mIsHeadset = false;
                                        if (videoCusSei.mIsMuteRemote) {
                                            videoCusSei.mSpeakImage.setImageResource(R.drawable.mainly_btn_mute_speaker_selector);
                                        } else {
                                            videoCusSei.mSpeakImage.setImageResource(R.drawable.mainly_btn_speaker_selector);
                                        }
                                    } else {
                                        mIsHeadset = true;
                                        if (videoCusSei.mIsMuteRemote) {
                                            videoCusSei.mSpeakImage.setImageResource(R.drawable.mainly_btn_muted_headset_selector);
                                        } else {
                                            videoCusSei.mSpeakImage.setImageResource(R.drawable.mainly_btn_headset_selector);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        break;
                }
            }
        }
    }
}
