package com.tttrtclive.Helper;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.tttrtclive.R;

public class AudioRemoteWindow extends RelativeLayout {

    public AudioRemoteWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        View view = LayoutInflater.from(context).inflate(R.layout.remote_window,null);
        this.addView(view);
    }

}
