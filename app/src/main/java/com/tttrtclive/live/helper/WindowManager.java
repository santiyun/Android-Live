package com.tttrtclive.live.helper;

import android.util.DisplayMetrics;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.EnterUserInfo;
import com.tttrtclive.live.ui.MainActivity;
import com.wushuangtech.expansion.bean.VideoCompositingLayout;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;
import java.util.List;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;

public class WindowManager {

    private ArrayList<RemoteWindow> mRemoteWindowList = new ArrayList<>();

    private int mScreenWidth;
    private int mScreenHeight;

    public WindowManager(MainActivity mainActivity) {
        //获取屏幕的宽和高
        DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        RemoteWindow mRemoteWindow0 = mainActivity.findViewById(R.id.remote1);
        mRemoteWindow0.mIndex = 0;
        mRemoteWindowList.add(mRemoteWindow0);
        RemoteWindow mRemoteWindow1 = mainActivity.findViewById(R.id.remote2);
        mRemoteWindow1.mIndex = 1;
        mRemoteWindowList.add(mRemoteWindow1);
        RemoteWindow mRemoteWindow2 = mainActivity.findViewById(R.id.remote3);
        mRemoteWindow2.mIndex = 2;
        mRemoteWindowList.add(mRemoteWindow2);
        RemoteWindow mRemoteWindow3 = mainActivity.findViewById(R.id.remote4);
        mRemoteWindow3.mIndex = 3;
        mRemoteWindowList.add(mRemoteWindow3);
        RemoteWindow mRemoteWindow4 = mainActivity.findViewById(R.id.remote5);
        mRemoteWindow4.mIndex = 4;
        mRemoteWindowList.add(mRemoteWindow4);
        RemoteWindow mRemoteWindow5 = mainActivity.findViewById(R.id.remote6);
        mRemoteWindow5.mIndex = 5;
        mRemoteWindowList.add(mRemoteWindow5);
    }

    public void add(long localId, long id, int oratation, int index) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mIndex == index && remoteWindow.mId != id) {
                remoteWindow.hide();
                remoteWindow.show(localId, id, oratation);
                return;
            }
        }
    }

    public void add(long localId, long id, int oratation) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == -1) {
                remoteWindow.show(localId, id, oratation);
                return;
            }
        }
    }

    public void addAndSendSei(long loginId, EnterUserInfo userInfo) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == -1) {
                if (userInfo.getRole() == CLIENT_ROLE_BROADCASTER)
                    remoteWindow.show(userInfo.getId());
                break;
            }
        }

        // 发送SEI
        VideoCompositingLayout layout = new VideoCompositingLayout();
        layout.regions = buildRemoteLayoutLocation(loginId);
        TTTRtcEngine.getInstance().setVideoCompositingLayout(layout);
    }

    public void removeAndSendSei(long loginId, long id) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == id) {
                remoteWindow.mute(false);
                remoteWindow.hide();
                return;
            }
        }

        // 发送SEI
        VideoCompositingLayout layout = new VideoCompositingLayout();
        layout.regions = buildRemoteLayoutLocation(loginId);
        TTTRtcEngine.getInstance().setVideoCompositingLayout(layout);
    }

    public void muteAudio(long id, boolean mute) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == id) {
                remoteWindow.mute(mute);
                return;
            }
        }
    }

    public void updateAudioBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == id) {
                remoteWindow.updateAudioBitrate(bitrate);
                return;
            }
        }
    }

    public void updateVideoBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == id) {
                remoteWindow.updateVideoBitrate(bitrate);
                return;
            }
        }
    }

    public void updateSpeakState(long id, int volumeLevel) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            RemoteWindow remoteWindow = mRemoteWindowList.get(i);
            if (remoteWindow.mId == id) {
                remoteWindow.updateSpeakState(volumeLevel);
                return;
            }
        }
    }

    private VideoCompositingLayout.Region[] buildRemoteLayoutLocation(long loginId) {
        List<VideoCompositingLayout.Region> tempList = new ArrayList<>();

        for (RemoteWindow remoteWindow : mRemoteWindowList) {
            if (remoteWindow.mId != -1) {
                int[] location = new int[2];
                remoteWindow.getLocationOnScreen(location);
                VideoCompositingLayout.Region mRegion = new VideoCompositingLayout.Region();
                mRegion.mUserID = remoteWindow.mId;
                mRegion.x = (double) location[0] / mScreenWidth;
                mRegion.y = (double) location[1] / mScreenHeight;
                mRegion.width = (double) 1 / 3; // view宽度占屏幕宽度的比例
                mRegion.height = (double) mScreenWidth / 3 * 4 / 3 / mScreenHeight; // view高度占屏幕的比例
                mRegion.zOrder = 1;
                tempList.add(mRegion);
            }
        }

        VideoCompositingLayout.Region mRegion = new VideoCompositingLayout.Region();
        mRegion.mUserID = loginId;
        mRegion.x = 0;
        mRegion.y = 0;
        mRegion.width = 1;
        mRegion.height = 1;
        mRegion.zOrder = 0;
        tempList.add(mRegion);

        VideoCompositingLayout.Region[] mRegions = new VideoCompositingLayout.Region[tempList.size()];
        for (int k = 0; k < tempList.size(); k++) {
            VideoCompositingLayout.Region region = tempList.get(k);
            mRegions[k] = region;
        }
        return mRegions;
    }

}
