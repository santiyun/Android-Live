package com.tttrtclive.live.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.View;

import com.tttrtclive.live.R;


/**
 * Created by wangzhiguo on 17/10/23.
 */

public class ExitRoomDialog extends Dialog {

    public View mConfirmBT;
    public View mDenyBT;

    public ExitRoomDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        setContentView(R.layout.mainly_more_exit_dialog);
        mConfirmBT = findViewById(R.id.mainly_dialog_exit_confirm);
        mDenyBT = findViewById(R.id.mainly_dialog_exit_deny);
    }
}
