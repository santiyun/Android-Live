package com.tttrtclive;

import java.util.ArrayList;

/**
 * Created by wangzhiguo on 17/6/15.
 */

public class LocalConfig {

    public static ArrayList<Long> mUserEnterOrder = new ArrayList<>();

    public static int mRole;

    public static String mIP = "";

    public static int mPort = 5000;

    public static String mPushUrl;
    //rtmp://cc-push.3ttech.cn/sdk/
    //rtmp://sjy-push.3ttech.cn/live/
    //rtmp://push.3ttech.cn/sdk/
    public static String mPushUrlPrefix = "rtmp://push.3ttech.cn/sdk/";

    public static long mLoginUserID;

    public static int mLoginRoomID;

    public static long mBroadcasterID;

    public static String mCDNAddress;

    public static int mAuthorSize;

    public static int mAudience;

    public static boolean mLocalMuteAuido;

    public static int mCurrentAudioRoute;
}
