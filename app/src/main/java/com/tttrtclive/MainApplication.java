package com.tttrtclive;

import android.app.Application;
import android.os.Environment;

import com.tttrtclive.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.utils.CrashHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.io.File;
import java.io.IOException;
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
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), "a967ac491e3acf92eed5e1b5ba641ab7", mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
        }

        // 设置日志收集
        File externalStorageDirectory = Environment.getExternalStorageDirectory();
        String abs = externalStorageDirectory.toString() + "/TTTLog.txt";
        File file = new File(abs);
        try {
            if (file.exists() && file.getTotalSpace() > 10 * 1024 * 1024) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mTTTEngine.setLogFile(abs);

        CrashHandler mCrash = new CrashHandler(getApplicationContext());
        mCrash.init();
    }
}
