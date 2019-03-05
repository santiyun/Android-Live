package com.tttrtclive.live.Helper;

import android.util.DisplayMetrics;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.EnterUserInfo;
import com.tttrtclive.live.ui.MainActivity;
import com.wushuangtech.bean.VideoCompositingLayout;
import com.wushuangtech.wstechapi.TTTRtcEngine;

import java.util.ArrayList;
import java.util.List;

import static com.wushuangtech.library.Constants.CLIENT_ROLE_BROADCASTER;

public class WindowManager {

    private ArrayList<AudioRemoteWindow> mRemoteWindowList = new ArrayList();

    private int mScreenWidth;
    private int mScreenHeight;

    public WindowManager(MainActivity mainActivity) {
        //获取屏幕的宽和高
        DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;

        AudioRemoteWindow mAudioRemoteWindow0 = mainActivity.findViewById(R.id.remote1);
        mAudioRemoteWindow0.mIndex = 0;
        mRemoteWindowList.add(mAudioRemoteWindow0);
        AudioRemoteWindow mAudioRemoteWindow1 = mainActivity.findViewById(R.id.remote2);
        mAudioRemoteWindow1.mIndex = 1;
        mRemoteWindowList.add(mAudioRemoteWindow1);
        AudioRemoteWindow mAudioRemoteWindow2 = mainActivity.findViewById(R.id.remote3);
        mAudioRemoteWindow2.mIndex = 2;
        mRemoteWindowList.add(mAudioRemoteWindow2);
        AudioRemoteWindow mAudioRemoteWindow3 = mainActivity.findViewById(R.id.remote4);
        mAudioRemoteWindow3.mIndex = 3;
        mRemoteWindowList.add(mAudioRemoteWindow3);
        AudioRemoteWindow mAudioRemoteWindow4 = mainActivity.findViewById(R.id.remote5);
        mAudioRemoteWindow4.mIndex = 4;
        mRemoteWindowList.add(mAudioRemoteWindow4);
        AudioRemoteWindow mAudioRemoteWindow5 = mainActivity.findViewById(R.id.remote6);
        mAudioRemoteWindow5.mIndex = 5;
        mRemoteWindowList.add(mAudioRemoteWindow5);
    }

    public void add(long localId, long id, int oratation, int index) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mIndex == index && audioRemoteWindow.mId != id) {
                audioRemoteWindow.hide();
                audioRemoteWindow.show(localId, id, oratation);
                return;
            }
        }
    }

    public void addAndSendSei(long loginId, EnterUserInfo userInfo) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == -1) {
                if (userInfo.getRole() == CLIENT_ROLE_BROADCASTER)
                    audioRemoteWindow.show(userInfo.getId());
                break;
            }
        }

        // 发送SEI
        VideoCompositingLayout layout = new VideoCompositingLayout();
        layout.regions = buildRemoteLayoutLocation(loginId);
        TTTRtcEngine.getInstance().setVideoCompositingLayout(layout);
    }

    public void remove(long id, int index) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.hide();
                return;
            }
        }
    }

    public void removeAndSendSei(long loginId, long id) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.mute(false);
                audioRemoteWindow.hide();
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
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.mute(mute);
                return;
            }
        }
    }

    public void updateAudioBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.updateAudioBitrate(bitrate);
                return;
            }
        }
    }

    public void updateVideoBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.updateVideoBitrate(bitrate);
                return;
            }
        }
    }

    public void updateSpeakState(long id, int volumeLevel) {
        for (int i = 0; i < mRemoteWindowList.size(); i++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.updateSpeakState(volumeLevel);
                return;
            }
        }
    }

    public VideoCompositingLayout.Region[] buildRemoteLayoutLocation(long loginId) {
        List<VideoCompositingLayout.Region> tempList = new ArrayList<>();

        for (AudioRemoteWindow remoteWindow : mRemoteWindowList) {
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
