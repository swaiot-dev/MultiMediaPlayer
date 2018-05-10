package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;


public class RorateModeDialog extends DialogFragment {
    public static final int KEY_DURATION = 400;
    protected long mLastKeyDownTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.rorate_mode_layout);
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        final Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.CENTER;
        window.setAttributes(layoutParams);

        dialog.setOnKeyListener(mOnKeyListener);

        return dialog;
    }

    private DialogInterface.OnKeyListener mOnKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        if (isValid()){
                            ((PhotoPlayActivity)getActivity()).rotate();
                        }
                        return true;
                }
            }
            return false;
        }
    };

    protected boolean isValid() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - mLastKeyDownTime) >= KEY_DURATION) {
            mLastKeyDownTime = currentTime;
            return true;
        } else {
            mLastKeyDownTime = currentTime;
            return false;
        }
    }
}
