package com.tttrtclive.live.Helper;

import android.util.DisplayMetrics;

import com.tttrtclive.live.R;
import com.tttrtclive.live.bean.EnterUserInfo;
import com.tttrtclive.live.bean.SeiInfo;
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

        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote1));
        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote2));
        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote3));
        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote4));
        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote5));
        mRemoteWindowList.add((AudioRemoteWindow) mainActivity.findViewById(R.id.remote6));
    }

    public void updateWindow(long userId, SeiInfo seiInfo, int oratation) {
        ArrayList<Long> removeList = new ArrayList();
        for (AudioRemoteWindow remoteWindow : mRemoteWindowList) {
            boolean has = false;
            for (int i = 0; i < seiInfo.getPos().size(); i ++) {
                // 过滤主播
                if (seiInfo.getPos().get(i).getId().equals(seiInfo.getMid()))
                    continue;
                if (remoteWindow.mId == Long.parseLong(seiInfo.getPos().get(i).getId())) {
                    has = true;
                    break;
                }
            }
            if (!has)
                removeList.add(remoteWindow.mId);
        }

        ArrayList<Long> addList = new ArrayList();
        for (int i = 0; i < seiInfo.getPos().size(); i ++) {
            // 过滤主播
            if (seiInfo.getPos().get(i).getId().equals(seiInfo.getMid()))
                continue;
            long id = Long.parseLong(seiInfo.getPos().get(i).getId());
            boolean has = false;
            for (AudioRemoteWindow remoteWindow : mRemoteWindowList) {
                if (remoteWindow.mId == id) {
                    has = true;
                    break;
                }
            }
            if (!has)
                addList.add(id);
        }

        for (int i = 0; i < removeList.size(); i ++) {
            remove(removeList.get(i));
        }

        for (int i = 0; i < addList.size(); i ++) {
            add(userId, addList.get(i), oratation);
        }
    }

    public void add(long localId, long id, int oratation) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == -1) {
                audioRemoteWindow.show(localId, id, oratation);
                return;
            }
        }
    }

    public void addAndSendSei(long loginId, EnterUserInfo userInfo) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
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

    public void remove(long id) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.hide();
                return;
            }
        }
    }

    public void removeAndSendSei(long loginId, long id) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
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
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.mute(mute);
                return;
            }
        }
    }

    public void updateAudioBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.updateAudioBitrate(bitrate);
                return;
            }
        }
    }

    public void updateVideoBitrate(long id, String bitrate) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
            AudioRemoteWindow audioRemoteWindow = mRemoteWindowList.get(i);
            if (audioRemoteWindow.mId == id) {
                audioRemoteWindow.updateVideoBitrate(bitrate);
                return;
            }
        }
    }

    public void updateSpeakState(long id, int volumeLevel) {
        for (int i = 0; i < mRemoteWindowList.size(); i ++) {
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
                mRegion.width = (double) 1/3; // view宽度占屏幕宽度的比例
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
