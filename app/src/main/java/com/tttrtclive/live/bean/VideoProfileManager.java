package com.tttrtclive.live.bean;

import com.wushuangtech.library.Constants;

import java.util.ArrayList;

public class VideoProfileManager {

    public ArrayList<VideoProfile> mVideoProfiles = new ArrayList<>();

    public VideoProfileManager() {
        mVideoProfiles.add(new VideoProfile("超低质量", Constants.TTTRTC_VIDEOPROFILE_120P, 160, 120, 65, 15));
        mVideoProfiles.add(new VideoProfile("低质量", Constants.TTTRTC_VIDEOPROFILE_180P, 320, 180, 140, 15));
        mVideoProfiles.add(new VideoProfile("中质量", Constants.TTTRTC_VIDEOPROFILE_240P, 320, 240, 200, 15));
        mVideoProfiles.add(new VideoProfile("高质量", Constants.TTTRTC_VIDEOPROFILE_360P, 640, 360, 600, 15));
        mVideoProfiles.add(new VideoProfile("超高质量", Constants.TTTRTC_VIDEOPROFILE_480P, 848, 480, 1000, 15));
        mVideoProfiles.add(new VideoProfile("特高质量", Constants.TTTRTC_VIDEOPROFILE_720P, 1280, 720, 2400, 20));
        mVideoProfiles.add(new VideoProfile("蓝光质量", Constants.TTTRTC_VIDEOPROFILE_1080P, 1920, 1080, 3000, 15));
        mVideoProfiles.add(new VideoProfile("640*480", Constants.TTTRTC_VIDEOPROFILE_640x480, 640, 480, 800, 15));
        mVideoProfiles.add(new VideoProfile("960*540", Constants.TTTRTC_VIDEOPROFILE_960x540, 960, 540, 1600, 24));
    }

    public VideoProfile getVideoProfile(String name) {
        for (VideoProfile videoProfile: mVideoProfiles) {
            if (videoProfile.name.equals(name))
                return videoProfile;
        }
        return null;
    }

    public VideoProfile getVideoProfile(int profile) {
        for (VideoProfile videoProfile: mVideoProfiles) {
            if (videoProfile.videoProfile == profile)
                return videoProfile;
        }
        return null;
    }

    public class VideoProfile {

        public String name;
        public int videoProfile;
        public int width, height;
        public int fRate, bRate;

        public VideoProfile(String name, int videoProfile, int width, int height, int bRate, int fRate) {
            this.name = name;
            this.videoProfile = videoProfile;
            this.width = width;
            this.height = height;
            this.fRate = fRate;
            this.bRate = bRate;
        }
    }

}
