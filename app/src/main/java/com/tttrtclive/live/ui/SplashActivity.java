package com.tttrtclive.live.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.live.LocalConfig;
import com.tttrtclive.live.LocalConstans;
import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.JniObjs;
import com.tttrtclive.live.bean.MyPermissionBean;
import com.tttrtclive.live.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.live.helper.MyPermissionManager;
import com.tttrtclive.live.utils.SharedPreferencesUtil;
import com.wushuangtech.library.Constants;
import com.wushuangtech.wstechapi.TTTRtcEngine;
import com.wushuangtech.wstechapi.model.PublisherConfiguration;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;

public class SplashActivity extends BaseActivity {

    public static final int ACTIVITY_MAIN = 100;
    public static final int ACTIVITY_SETTING = 101;
    private ProgressDialog mDialog;
    private MyPermissionManager mMyPermissionManager;
    private MyLocalBroadcastReceiver mLocalBroadcast;

    private boolean mIsLoging;
    private boolean isSetting;
    private String mRoomName;

    private EditText mRoomIDET;
    private View mAdvanceSetting;
    private RadioButton mHostBT, mAuthorBT;

    /*-------------------------------配置参数---------------------------------*/
    private int mLocalVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    private int mPushVideoProfile = Constants.TTTRTC_VIDEOPROFILE_DEFAULT;
    private String mLocalIP;
    public int mLocalWidth, mLocalHeight, mLocalFrameRate, mLocalBitRate, mLocalPort;
    public int mPushWidth, mPushHeight, mPushFrameRate, mPushBitRate;
    private boolean mUseHQAudio = false;
    private int mEncodeType = 0;//0:H.264  1:H.265
    private int mAudioSRate = 0;// 0:48000 1:44100
    /*-------------------------------配置参数---------------------------------*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        if (!this.isTaskRoot()) {
            Intent mainIntent = getIntent();
            String action = mainIntent.getAction();
            if (action != null && mainIntent.hasCategory(Intent.CATEGORY_LAUNCHER) && action.equals(Intent.ACTION_MAIN)) {
                finish();
                return;
            }
        }

        ArrayList<MyPermissionBean> mPermissionList = new ArrayList<>();
        mPermissionList.add(new MyPermissionBean(Manifest.permission.WRITE_EXTERNAL_STORAGE, getResources().getString(R.string.permission_write_external_storage)));
        mPermissionList.add(new MyPermissionBean(Manifest.permission.RECORD_AUDIO, getResources().getString(R.string.permission_record_audio)));
        mPermissionList.add(new MyPermissionBean(Manifest.permission.CAMERA, getResources().getString(R.string.permission_camera)));
        mPermissionList.add(new MyPermissionBean(Manifest.permission.READ_PHONE_STATE, getResources().getString(R.string.permission_read_phone_state)));
        mMyPermissionManager = new MyPermissionManager(this, new MyPermissionManager.PermissionUtilsInter() {
            @Override
            public List<MyPermissionBean> getApplyPermissions() {
                return mPermissionList;
            }

            @Override
            public AlertDialog.Builder getTipAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipDialog() {
                return null;
            }

            @Override
            public AlertDialog.Builder getTipAppSettingAlertDialog() {
                return null;
            }

            @Override
            public Dialog getTipAppSettingDialog() {
                return null;
            }
        });
        boolean isOk = mMyPermissionManager.checkPermission();
        if (isOk) {
            init();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMyPermissionManager != null) {
            mMyPermissionManager.checkPermission();
        }

        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }

        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (mMyPermissionManager != null) {
            boolean isOk = mMyPermissionManager.onRequestPermissionsResults(this, requestCode, permissions, grantResults);
            if (isOk) {
                init();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case MyPermissionManager.REQUEST_SETTING_CODE:
                if (mMyPermissionManager != null) {
                    boolean isOk = mMyPermissionManager.onActivityResults(requestCode);
                    if (isOk) {
                        init();
                    }
                }
                break;
            case ACTIVITY_MAIN:
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                break;
            case ACTIVITY_SETTING:
                if (mDialog != null) {
                    mDialog.dismiss();
                }
                mLocalVideoProfile = intent.getIntExtra("LVP", mLocalVideoProfile);
                mPushVideoProfile = intent.getIntExtra("PVP", mPushVideoProfile);
                mLocalWidth = intent.getIntExtra("LWIDTH", mLocalWidth);
                mLocalHeight = intent.getIntExtra("LHEIGHT", mLocalHeight);
                mLocalBitRate = intent.getIntExtra("LBRATE", mLocalBitRate);
                mLocalFrameRate = intent.getIntExtra("LFRATE", mLocalFrameRate);
                mLocalIP = intent.getStringExtra("LIP");
                mLocalPort = intent.getIntExtra("LPORT", mLocalPort);
                mPushWidth = intent.getIntExtra("PWIDTH", mPushWidth);
                mPushHeight = intent.getIntExtra("PHEIGHT", mPushHeight);
                mPushBitRate = intent.getIntExtra("PBRATE", mPushBitRate);
                mPushFrameRate = intent.getIntExtra("PFRATE", mPushFrameRate);
                mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
                mUseHQAudio = intent.getBooleanExtra("HQA", mUseHQAudio);
                mEncodeType = intent.getIntExtra("EDT", mEncodeType);
                mAudioSRate = intent.getIntExtra("ASR", mAudioSRate);
                isSetting = false;
                break;
        }
    }

    private void init() {
        initView();
        // 读取保存的数据
        String roomID = (String) SharedPreferencesUtil.getParam(this, "RoomID", "");
        mRoomIDET.setText(roomID);
        mRoomIDET.setSelection(mRoomIDET.length());
        //TODO ***注册广播，接收 SDK 的回调信令*** 重要操作!加TODO高亮
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);

        mDialog = new ProgressDialog(this);
        mDialog.setTitle("");
        mDialog.setCancelable(false);
        mDialog.setMessage(getString(R.string.ttt_hint_loading_channel));

        if (LocalConfig.mLocalRole == CLIENT_ROLE_ANCHOR) {
            mHostBT.setChecked(true);
            mAuthorBT.setChecked(false);
        } else if (LocalConfig.mLocalRole == CLIENT_ROLE_BROADCASTER) {
            mHostBT.setChecked(false);
            mAuthorBT.setChecked(true);
        }
    }

    @SuppressLint("SetTextI18n")
    private void initView() {
        mAuthorBT = findViewById(R.id.vice);
        mHostBT = findViewById(R.id.host);
        mRoomIDET = findViewById(R.id.room_id);
        mAdvanceSetting = findViewById(R.id.set);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getSdkVersion());
        mVersion.setText(result);
        TextView mLogoTextTV = findViewById(R.id.room_id_text);
        mLogoTextTV.setText(getString(R.string.ttt_prefix_live_channel_name) + ": ");
    }

    /**
     * TODO ***SDK 进房间前的配置。*** 重要操作!加TODO高亮
     */
    private void initSDK() {
        // 1.设置频道模式，这里用直播模式
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        // 2.启用视频功能
        mTTTEngine.enableVideo();
        // 3.设置角色
        int mRole = CLIENT_ROLE_ANCHOR;
        if (mHostBT.isChecked()) {
            mRole = CLIENT_ROLE_ANCHOR;
        } else if (mAuthorBT.isChecked()) {
            mRole = CLIENT_ROLE_BROADCASTER;
        }
        LocalConfig.mLocalRole = mRole;
        mTTTEngine.setClientRole(LocalConfig.mLocalRole);
        if (LocalConfig.mLocalRole == CLIENT_ROLE_ANCHOR) {
            // 4.设置推流地址，该推流地址仅供Demo运行演示使用，不可在正式环境中使用。
            String mPushUrlPrefix = "rtmp://push.3ttest.cn/sdk2/";
            String mPushUrl;
            if (mEncodeType == 0) {
                mPushUrl = mPushUrlPrefix + mRoomName; // H264视频推流格式，默认使用即可
            } else {
                mPushUrl = mPushUrlPrefix + mRoomName + "?trans=1"; //H265视频推流格式
            }
            PublisherConfiguration mPublisherConfiguration = new PublisherConfiguration();
            mPublisherConfiguration.setPushUrl(mPushUrl);
            mTTTEngine.configPublisher(mPublisherConfiguration);
        }
    }

    public void onClickRoleButton(View v) {
        if (mDialog.isShowing()) {
            return;
        }

        mHostBT.setChecked(false);
        mAuthorBT.setChecked(false);

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.host:
                mAdvanceSetting.setVisibility(View.VISIBLE);
                break;
            case R.id.vice:
                mAdvanceSetting.setVisibility(View.GONE);
                break;
        }
    }

    public void onClickEnterButton(View v) {
        if (mDialog.isShowing()) {
            return;
        }

        mRoomName = mRoomIDET.getText().toString().trim();
        if (TextUtils.isEmpty(mRoomName)) {
            Toast.makeText(this, getString(R.string.ttt_error_enterchannel_check_channel_empty), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.getTrimmedLength(mRoomName) > 19) {
            Toast.makeText(this, R.string.hint_channel_name_limit, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long roomId = Long.valueOf(mRoomName);
            if (roomId <= 0) {
                Toast.makeText(this, "房间号必须大于0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "房间号只支持整型字符串", Toast.LENGTH_SHORT).show();
        }

        if (mIsLoging) return;
        mIsLoging = true;
        mDialog.show();
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mRoomName);
        initSDK();
        // 6.加入频道
        mTTTEngine.joinChannel("", mRoomName, LocalConfig.mLocalUserID);
    }

    public void onSetButtonClick(View v) {
        if (mDialog.isShowing()) {
            return;
        }

        if (isSetting) return;
        isSetting = true;

        mDialog.show();
        Intent intent = new Intent(this, SetActivity.class);
        intent.putExtra("LVP", mLocalVideoProfile);
        intent.putExtra("PVP", mPushVideoProfile);
        intent.putExtra("LWIDTH", mLocalWidth);
        intent.putExtra("LHEIGHT", mLocalHeight);
        intent.putExtra("LBRATE", mLocalBitRate);
        intent.putExtra("LFRATE", mLocalFrameRate);
        intent.putExtra("LIP", mLocalIP);
        intent.putExtra("LPORT", mLocalPort);
        intent.putExtra("PWIDTH", mPushWidth);
        intent.putExtra("PHEIGHT", mPushHeight);
        intent.putExtra("PBRATE", mPushBitRate);
        intent.putExtra("PFRATE", mPushFrameRate);
        intent.putExtra("HQA", mUseHQAudio);
        intent.putExtra("EDT", mEncodeType);
        intent.putExtra("ASR", mAudioSRate);
        startActivityForResult(intent, ACTIVITY_SETTING);
    }

    private class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (MyTTTRtcEngineEventHandler.TAG.equals(action)) {
                JniObjs mJniObjs = intent.getParcelableExtra(MyTTTRtcEngineEventHandler.MSG_TAG);
                switch (mJniObjs.mJniType) {
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        //界面跳转
                        Intent activityIntent = new Intent();
                        activityIntent.putExtra("ROOM_ID", Long.parseLong(mRoomName));
                        activityIntent.putExtra("USER_ID", LocalConfig.mLocalUserID);
                        activityIntent.setClass(mContext, MainActivity.class);
                        startActivityForResult(activityIntent, ACTIVITY_MAIN);
                        mIsLoging = false;
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mDialog.dismiss();
                        mIsLoging = false;
                        int errorType = mJniObjs.mErrorType;
                        if (errorType == Constants.ERROR_ENTER_ROOM_INVALIDCHANNELNAME) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_format), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_TIMEOUT) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_timeout), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_token_invaild), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_BAD_VERSION) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_version), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_CONNECT_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_unconnect), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_NOEXIST) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_room_no_exist), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_SERVER_VERIFY_FAILED) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_verification_failed), Toast.LENGTH_SHORT).show();
                        } else if (errorType == Constants.ERROR_ENTER_ROOM_UNKNOW) {
                            Toast.makeText(mContext, mContext.getResources().getString(R.string.ttt_error_enterchannel_unknow), Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
            }
        }
    }

}
