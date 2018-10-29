package com.tttrtclive.live.bean;

import android.support.annotation.NonNull;

/**
 * Created by root on 17-2-21.
 */

public class EnterUserInfo {

    private long id;
    private int role;

    public EnterUserInfo(long uid, int role) {
        this.id = uid;
        this.role = role;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

}
