package com.tttrtclive.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.tttrtclive.LocalConfig;
import com.tttrtclive.utils.MyLog;
import com.wushuangtech.jni.RoomJni;

import java.util.Vector;

/**
 *  a967ac491e3acf92eed5e1b5ba641ab7
 *  test900572e02867fab8131651339518
 */

public class TestUtils {

    public static Vector<String> mTestDatas = new Vector<>();
    private static MyLocalBroadcastReceiver mLocalBroadcast;
    public static TestDialog mTestDialog;
    public static TestInterfaceListAdapter mTestInterfaceListAdapter;

    public static void setAddressAndPushUrl() {
        // 设置服务器地址
        MyLog.d("设置服务器地址 : " + LocalConfig.mIP);
        RoomJni.getInstance().setServerAddress(LocalConfig.mIP, LocalConfig.mPort);
    }

    public static void initTestBroadcast(Context mContext) {
        mLocalBroadcast = new MyLocalBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addCategory("ttt.test.interface");
        filter.addAction("ttt.test.interface.string");
        mContext.registerReceiver(mLocalBroadcast, filter);
    }

    public static void initTestDialog(Context mContext) {
        mTestDialog = new TestDialog(mContext, "");
        mTestDialog.setCanceledOnTouchOutside(false);
    }

    public static void unInitTestBroadcast(Context mContext) {
        try {
            mContext.unregisterReceiver(mLocalBroadcast);
        } catch (Exception e) {
            Log.d("test" , "exception : " + e.getLocalizedMessage());
        }
    }

    // sdk的测试代码，请忽略
    private static class MyLocalBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("ttt.test.interface.string".equals(action)) {
                String testString = intent.getStringExtra("testString");
                if (!TextUtils.isEmpty(testString)) {
                    mTestDatas.add(testString);
                }
            }
        }
    }
}
