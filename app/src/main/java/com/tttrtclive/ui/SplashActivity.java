package com.tttrtclive.ui;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tttrtclive.Helper.TTTRtcEngineHelper;
import com.tttrtclive.LocalConfig;
import com.tttrtclive.LocalConstans;
import com.tttrtclive.R;
import com.tttrtclive.bean.JniObjs;
import com.tttrtclive.callback.MyTTTRtcEngineEventHandler;
import com.tttrtclive.dialog.TestDialog;
import com.tttrtclive.utils.MyLog;
import com.tttrtclive.utils.SharedPreferencesUtil;
import com.wushuangtech.jni.RoomJni;
import com.wushuangtech.library.Constants;
import com.wushuangtech.library.GlobalConfig;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import static com.tttrtclive.LocalConfig.mAudience;
import static com.tttrtclive.LocalConfig.mAuthorSize;
import static com.tttrtclive.LocalConfig.mLoginRoomID;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_ANCHOR;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_AUDIENCE;
import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_BAD_VERSION;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_FAILED;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_TIMEOUT;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_UNKNOW;
import static com.wushuangtech.library.Constants.ERROR_ENTER_ROOM_VERIFY_FAILED;

public class SplashActivity extends BaseActivity {

    private int mRole = -1;
    private Dialog mDialog, testDialog;
    private boolean mIsLoging;
    private EditText mRoomIDET;
    private RadioButton mHostBT, mAuthorBT, mAudienceBT;
    private MyLocalBroadcastReceiver mLocalBroadcast;
    private TTTRtcEngineHelper mTTTRtcEngineHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);
        initView();
        mTTTRtcEngineHelper = new TTTRtcEngineHelper();
        // 读取保存的数据
        int roomID = (int) SharedPreferencesUtil.getParam(this, "RoomID", 0);
        // 设置保存的数据
        if (roomID != 0) {
            String s = String.valueOf(roomID);
            mRoomIDET.setText(s);
            mRoomIDET.setSelection(s.length());
        }

        // 注册回调函数接收的广播
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyTTTRtcEngineEventHandler.TAG);
        registerReceiver(mLocalBroadcast, filter);
        MyLog.d("SplashActivity onCreate.... model : " + Build.MODEL);
    }

    private void initView() {
        mAuthorBT = findViewById(R.id.vice);
        mAudienceBT = findViewById(R.id.audience);
        mRoomIDET = findViewById(R.id.room_id);
        mHostBT = findViewById(R.id.host);
        TextView mVersion = findViewById(R.id.version);
        String string = getResources().getString(R.string.version_info);
        String result = String.format(string, TTTRtcEngine.getInstance().getVersion());
        mVersion.setText(result);
    }

    @Override
    protected void onStart() {
        super.onStart();
        MyLog.d("SplashActivity onStart....");
        initEngine();
    }

    @Override
    protected void onStop() {
        super.onStop();
        MyLog.d("SplashActivity onStop....");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MyLog.d("SplashActivity onDestroy....");
        TTTRtcEngine.destroy();
        try {
            unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void onClickRoleButton(View v) {
        mHostBT.setChecked(false);
        mAuthorBT.setChecked(false);
        mAudienceBT.setChecked(false);

        ((RadioButton) v).setChecked(true);
        switch (v.getId()) {
            case R.id.host:
                mRole = CLIENT_ROLE_BROADCASTER;
                break;
            case R.id.vice:
                mRole = CLIENT_ROLE_ANCHOR;
                break;
            case R.id.audience:
                mRole = CLIENT_ROLE_AUDIENCE;
                break;
        }
    }

    private void initEngine() {
        // 设置频道属性
        mTTTEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
        // 启用视频模式
        mTTTEngine.enableVideo();
        // 由于有录屏功能，启用高音质
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTTTEngine.setHighQualityAudioParameters(true);
        }
        // 重置直播房间内的参数
        mAudience = 0;
        mAuthorSize = 0;
    }

    public void onClickEnterButton(View v) {
        boolean checkResult = mTTTRtcEngineHelper.splashCheckSetting(this, mRoomIDET.getText().toString());
        if (!checkResult) {
            return;
        }

        //TODO 以下代码发布时删除
        GlobalConfig.mPushUrl = LocalConfig.mPushUrl;
        if (!TextUtils.isEmpty(LocalConfig.mIP)) {
            MyLog.d("设置服务器地址 : " + LocalConfig.mIP);
            RoomJni.getInstance().setServerAddress(LocalConfig.mIP, LocalConfig.mPort);
        } else {
            MyLog.d("没有设置服务器地址，使用动态分配");
        }

        if (LocalConfig.mPushUrlPrefix.equals(GlobalConfig.mPushUrl)) {
            LocalConfig.mPushUrl = LocalConfig.mPushUrlPrefix + LocalConfig.mLoginRoomID;
            GlobalConfig.mPushUrl = LocalConfig.mPushUrlPrefix + LocalConfig.mLoginRoomID;
            MyLog.d("sdk推流没设置ID，自动填上ID");
        }

        // 设置进入直播房间的角色
        if (mRole == -1) {
            if (mHostBT.isChecked()) {
                mRole = CLIENT_ROLE_BROADCASTER;
            } else if (mAuthorBT.isChecked()) {
                mRole = CLIENT_ROLE_ANCHOR;
            } else if (mAudienceBT.isChecked()) {
                mRole = CLIENT_ROLE_AUDIENCE;
            }
        }
        LocalConfig.mRole = mRole;
        mTTTEngine.setClientRole(LocalConfig.mRole, null);
        // 拉流地址
        LocalConfig.mCDNAddress = "rtmp://pull.3ttech.cn/sdk/" + mLoginRoomID;
        // 保存配置
        SharedPreferencesUtil.setParam(this, "RoomID", LocalConfig.mLoginRoomID);
        if (mIsLoging) {
            return;
        }
        mIsLoging = true;
        showWaittingDialog();
        new Thread(() -> {
            int result = mTTTEngine.joinChannel("", String.valueOf(LocalConfig.mLoginRoomID), LocalConfig.mLoginUserID);
            MyLog.d("joinChannel result : " + result +
                    " | Room ID : " + LocalConfig.mLoginRoomID + " | User ID : " + LocalConfig.mLoginUserID);
        }).start();

    }

    private void showWaittingDialog() {
        if (mDialog == null) {
            mDialog = ProgressDialog.show(this, "", "正在进入房间...");
        } else {
            mDialog.show();
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
                    case LocalConstans.CALL_BACK_ON_ENTER_ROOM:
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        mIsLoging = false;
                        //界面跳转
                        startActivity(new Intent(mContext, MainActivity.class));
                        break;
                    case LocalConstans.CALL_BACK_ON_ERROR:
                        mIsLoging = false;
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        final int errorType = mJniObjs.mErrorType;
                        runOnUiThread(() -> {
                            MyLog.d("onReceive CALL_BACK_ON_ERROR errorType : " + errorType);
                            if (errorType == ERROR_ENTER_ROOM_TIMEOUT) {
                                Toast.makeText(mContext, "超时，10秒未收到服务器返回结果", Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_FAILED) {
                                Toast.makeText(mContext, "无法连接服务器", Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_VERIFY_FAILED) {
                                Toast.makeText(mContext, "验证码错误", Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_BAD_VERSION) {
                                Toast.makeText(mContext, "版本错误", Toast.LENGTH_SHORT).show();
                            } else if (errorType == ERROR_ENTER_ROOM_UNKNOW) {
                                Toast.makeText(mContext, "该房间不存在", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break;
                }
            }
        }
    }

    /**
     * Author: wangzg <br/>
     * Time: 2017-11-24 10:17:28<br/>
     * Description: 测试用，不用关注该函数
     */
    public void onTestButtonClick(View v) {
        Editable text = mRoomIDET.getText();
        String roomID = "";
        if (!TextUtils.isEmpty(text)) {
            roomID = text.toString();
        }

        if (testDialog == null) {
            testDialog = new TestDialog(this, roomID);
            testDialog.setCanceledOnTouchOutside(false);
        } else {
            ((TestDialog) testDialog).setRoomID(roomID);
            ((TestDialog) testDialog).setServerParams();
        }
        testDialog.show();
    }
}
