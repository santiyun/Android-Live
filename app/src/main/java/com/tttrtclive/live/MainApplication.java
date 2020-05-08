package com.tttrtclive.live;

import android.app.Application;

import com.tttrtclive.live.callback.MyTTTRtcEngineEventHandler;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.io.File;

public class MainApplication extends Application {

    /**
     * 回调类引用，用于接收SDK各种回调信令。
     */
    public MyTTTRtcEngineEventHandler mMyTTTRtcEngineEventHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        //1.创建自定义的 SDK 的回调接收类，继承自SDK的回调基类 TTTRtcEngineEventHandler
        mMyTTTRtcEngineEventHandler = new MyTTTRtcEngineEventHandler(getApplicationContext());
        //2.创建SDK的实例对象，APPID需要去官网上申请获取。
        TTTRtcEngine mTTTEngine = TTTRtcEngine.create(getApplicationContext(), <三体 APPID 的填写位置>, mMyTTTRtcEngineEventHandler);
        if (mTTTEngine == null) {
            System.exit(0);
            return;
        }
        // ------ SDK初始化完成，以下为 Demo 逻辑或 SDK 的可选操作接口。
        //开启日志
        File fileDir = getExternalFilesDir(null);
        if (fileDir == null) {
            throw new RuntimeException("getExternalFilesDir is null!");
        }
        String logPath = fileDir + "/3TLog";
        TTTRtcEngine.getInstance().setLogFile(logPath);
    }
}
