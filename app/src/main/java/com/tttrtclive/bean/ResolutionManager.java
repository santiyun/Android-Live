package com.tttrtclive.bean;

import android.graphics.Point;

import com.wushuangtech.library.Constants;

import java.util.ArrayList;

public class ResolutionManager {

    private ArrayList<Resolution> mResolutions = new ArrayList<>();

    public ResolutionManager() {
        mResolutions.add(new Resolution("160x120", Constants.VIDEO_PROFILE_120P));
//        mResolutions.add(new Resolution("320x180", Constants.VIDEO_PROFILE_180P));
        mResolutions.add(new Resolution("320x240", Constants.VIDEO_PROFILE_240P));
//        mResolutions.add(new Resolution("640x360", Constants.VIDEO_PROFILE_360P));
        mResolutions.add(new Resolution("640x480", Constants.VIDEO_PROFILE_480P));
//        mResolutions.add(new Resolution("1280x720", Constants.VIDEO_PROFILE_720P));
        mResolutions.add(new Resolution("1920x1080", Constants.VIDEO_PROFILE_1080P));
    }

    public ArrayList<String> getList() {
        ArrayList<String> resolutions = new ArrayList<>();
        for (Resolution rs : mResolutions)
            resolutions.add(rs.resolution);
        return resolutions;
    }

    public int getIndex(String name) {
        for (Resolution rs : mResolutions) {
            if (rs.resolution.equals(name))
                return rs.index;
        }
        return Constants.VIDEO_PROFILE_120P;
    }

    private class Resolution {
        private String resolution;
        private int index;

        public Resolution(String resolution, int index) {
            this.resolution = resolution;
            this.index = index;
        }
    }
}
