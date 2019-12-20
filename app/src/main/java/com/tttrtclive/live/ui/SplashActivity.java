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

    /**
     * TODO ***SDK 进房间前的配置。*** 重要操作!加TODO高亮
     */
    private void mustConfigSdk() {
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
        int mRole = CLIENT_ROLE_ANCHOR;
        if (mAuthorBT.isChecked()) {
            mRole = CLIENT_ROLE_BROADCASTER;
        }
        LocalConfig.mLocalRole = mRole;
        mTTTEngine.setClientRole(LocalConfig.mLocalRole); // 必须设置的 API
        // 3.启用视频模块功能
        mTTTEngine.enableVideo(); // 必须设置的 API
        // 4.设置推流地址，只有主播角色的用户设置有效。该推流地址仅供Demo运行演示使用，不可在正式环境中使用。
        // 必须设置的 API
        if (LocalConfig.mLocalRole == CLIENT_ROLE_ANCHOR) {
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

    /**
     * TODO ***SDK 进房间前的配置。*** 重要操作!加TODO高亮
     */
    private void optConfigSdk() {
        // 1.设置音频编码参数，SDK 默认为 ISAC 音频编码格式，32kbps 音频码率，适用于通话；高音质选用 AAC 格式编码，码率设置为96kbps。
        //  可选操作的 API
        if (mUseHQAudio) {
            mTTTEngine.setPreferAudioCodec(Constants.TTT_AUDIO_CODEC_AAC, 96, 1);
        } else {
            mTTTEngine.setPreferAudioCodec(Constants.TTT_AUDIO_CODEC_ISAC, 32, 1);
        }
        // 2.设置视频编码参数，SDK 默认为 360P 质量等级。
        // 可选操作的 API
        if (LocalConfig.mLocalRole == Constants.CLIENT_ROLE_BROADCASTER) {
            // 若角色为副播，视频质量等级设置为 120P，若感觉视频不清晰，可自行调整等级，如360P。
            mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_120P, false);
        } else {
            // 若角色为主播，视频质量根据 SetActivity 设置界面所设置的参数来决定。
            if (mLocalVideoProfile != 0) {
                mTTTEngine.setVideoProfile(mLocalVideoProfile, false);
            } else {
                // 自定义视频参数，而不用 SDK 内部定义的视频质量等级。
                if (mLocalHeight != 0 && mLocalWidth != 0 && mLocalBitRate != 0 && mLocalFrameRate != 0) {
                    mTTTEngine.setVideoProfile(mLocalHeight, mLocalWidth, mLocalFrameRate, mLocalBitRate);
                } else {
                    mTTTEngine.setVideoProfile(Constants.TTTRTC_VIDEOPROFILE_360P, false);
                }
            }
        }
        // 3.设置直播推流，连麦场景下服务器混屏的视频参数。不连麦，即单主播推流场景，无需设置该 API。
        // 可选操作的 API
        if (mPushBitRate != 0 && mPushFrameRate != 0 && mPushHeight != 0 && mPushWidth != 0) {
            mTTTEngine.setVideoMixerParams(mPushBitRate, mPushFrameRate, mPushHeight, mPushWidth);
        }
        // 4.设置直播推流，连麦场景下服务器混屏的音频参数。不连麦，即单主播推流场景，无需设置该 API。
        // 可选操作的 API
        if (mAudioSRate != 0) {
            mTTTEngine.setAudioMixerParams(mAudioSRate, mAudioSRate == 0 ? 48000 : 44100, 1);
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
        mDialog.setMessage(getString(R.string.ttt_hint_loading_channel));
        mDialog.show();
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", mRoomName);
        LocalConfig.mRoomID = Long.valueOf(mRoomName);
        // SDK必须配置的 API
        mustConfigSdk();
        // SDK可选配置的 API
        optConfigSdk();
        // 加入频道
        mTTTEngine.joinChannel("", mRoomName, LocalConfig.mLocalUserID);
    }

    public void onSetButtonClick(View v) {
        if (mDialog.isShowing()) {
            return;
        }

        if (isSetting) return;
        isSetting = true;

        mDialog.setMessage(getString(R.string.ttt_hint_progress_channel));
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
