package com.tttrtclive;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;
import com.tttrtclive.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.Random;

public class MainApplication extends Application {

    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();

        Random mRandom = new Random();
        LocalConfig.mLoginUserID = mRandom.nextInt(999999);

        //1.设置SDK的回调接收类
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //2.创建SDK的实例对象 "a967ac491e3acf92eed5e1b5ba641ab7" test900572e02867fab8131651339518
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), "a967ac491e3acf92eed5e1b5ba641ab7",
                mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }

        CrashReport.initCrashReport(getApplicationContext(), "5ade4cea78", true);
    }

}
