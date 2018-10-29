package com.tttrtclive.live.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.JniObjs;
import com.tttrtclive.live.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.live.dialog.VideoInfoDialog;
import com.tttrtclive.live.utils.MyLog;
import com.tttrtclive.live.utils.SharedPreferencesUtil;
import com.wushuangtech.api.EnterConfApi;
import com.wushuangtech.jni.NativeInitializer;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.utils.PviewLog;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.yanzhenjie.permission.AndPermission;

import java.util.Random;

import static com.wushuangtech.library.Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_NOEXIST;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;

public class SplashActivity extends BaseActivity {

    private ProgressDialog mDialog;
    public static boolean mIsLoging;
    private EditText mRoomIDET;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private String mRoomName;
    private long mUserId;
    private RadioButton mHostBT, mAuthorBT;
    private int mRole = CLIENT_ROLE_ANCHOR;
    private VideoInfoDialog videoInfoDialog;

    /*-------------------------------配置参数---------------------------------*/
    private int mLocalVideoProfile = Constants.VIDEO_PROFILE_DEFAULT;
    private int mPushVideoProfile = Constants.VIDEO_PROFILE_DEFAULT;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    private boolean mUseHQAudio = false;
    private int mEncodeType = 0;//0:H.264  1:H.265
    private int mAudioSRate = 0;// 0:48000 1:44100
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        // 权限申请
        AndPermission.with(this)
                .permission(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE)
                .start();

        init();
    }

    private void init() {
        initView();
        // 读取保存的数据
        String roomID = (String) SharedPreferencesUtil.getParam(this, "RoomID", "");
        mRoomIDET.setText(roomID);

        mTTTEngine.enableVideo();
        mTTTEngine.setChannelProfile(CHANNEL_PROFILE_LIVE_BROADCASTING);
        mTTTEngine.setVideoProfile(Constants.VIDEO_PROFILE_360P, true);

        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("");
        mDialog.setMessage("正在进入房间...");

        videoInfoDialog = new VideoInfoDialog(mContext, R.style.NoBackGroundDialog);
    }

    private void initView() {
        mAuthorBT = (RadioButton) findViewById(R.id.vice);
        mHostBT = (RadioButton) findViewById(R.id.host);
        mRoomIDET = (EditText) findViewById(R.id.room_id);
        TextView mVersion = (TextView) findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);

        TextView mSdkVersion = (TextView) findViewById(R.id.sdk_version);
        mSdkVersion.setText("sdk version : " + NativeInitializer.getIntance().getVersion());
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d("SplashActivity onDestroy....");
        TTTRtcEngine.destroy();
    }

    public void onClickRoleButton(View v) {
        mHostBT.setChecked(false);
        mAuthorBT.setChecked(false);

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.host:
                mRole = CLIENT_ROLE_ANCHOR;
                findViewById(R.id.set).setVisibility(View.VISIBLE);
                break;
            case R.id.vice:
                mRole = CLIENT_ROLE_BROADCASTER;
                findViewById(R.id.set).setVisibility(View.GONE);
                break;
        }
    }

    public void onClickEnterButton(View v) {
        mRoomName = mRoomIDET.getText().toString().trim();
        if (TextUtils.isEmpty(mRoomName)) {
            Toast.makeText(this, "房间ID不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.getTrimmedLength(mRoomName) > 18) {
            Toast.makeText(this, "房间ID不能超过19位", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsLoging) return;
        mIsLoging = true;

        Random mRandom = new Random();
        mUserId = mRandom.nextInt(999999);

        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mRoomName);

        mTTTEngine.setClientRole(mRole, null);

        // 设置推流格式H264/H265
        if (mEncodeType == 0) {
            GlobalConfig.mPushUrl = GlobalConfig.mCDNPushAddressPrefix + mRoomName;
        } else {
            GlobalConfig.mPushUrl = GlobalConfig.mCDNPushAddressPrefix + mRoomName + "?trans=1";
        }

        mTTTEngine.joinChannel("", mRoomName, mUserId);
        mDialog.show();
        return;

    }

    public void onSetButtonClick(View v) {
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra("LVP", mLocalVideoProfile);
        intent.putExtra("PVP", mPushVideoProfile);
        intent.putExtra("LWIDTH", mLocalWidth);
        intent.putExtra("LHEIGHT", mLocalHeight);
        intent.putExtra("LBRATE", mLocalBitRate);
        intent.putExtra("LFRATE", mLocalFrameRate);
        intent.putExtra("PWIDTH", mPushWidth);
        intent.putExtra("PHEIGHT", mPushHeight);
        intent.putExtra("PBRATE", mPushBitRate);
        intent.putExtra("PFRATE", mPushFrameRate);
        intent.putExtra("HQA", mUseHQAudio);
        intent.putExtra("EDT", mEncodeType);
        intent.putExtra("ASR", mAudioSRate);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
        mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
        mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
        mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
        mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
        mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
        mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
        mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
        mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
        mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
        mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
        mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
        mEncodeType = intent.getIntExtra("EDT", mEncodeType);
        mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        mDialog.dismiss();
                        //界面跳转
                        Intent activityIntent = new Intent();
                        activityIntent.putExtra("ROOM_ID", Long.parseLong(mRoomName));
                        activityIntent.putExtra("USER_ID", mUserId);
                        activityIntent.putExtra("ROLE", mRole);
                        activityIntent.setClass(SplashActivity.this, MainActivity.class);
                        startActivity(activityIntent);
                        PviewLog.testPrint("joinChannel", "end");
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mIsLoging = false;
                        mDialog.dismiss();
                        final int errorType = mJniObjs.mErrorType;
                        runOnUiThread(() -> {
                            MyLog.d("onReceive CALL_BACK_ON_ERROR errorType : " + errorType);
                            if (errorType == ERROR_ENTER_ROOM_TIMEOUT) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_timeout), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_unconnect), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_VERIFY_FAILED) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_verification_code), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_BAD_VERSION) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_NOEXIST) {
                                Toast.makeText(mContext, getResources().getString(R.string.error_noroom), Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        }
    }

}
