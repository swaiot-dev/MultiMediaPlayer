package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.mediatek.wwtv.mediaplayer.R;


/**
 * Created by sniuniu on 2018/3/8.
 */

public class LoadingDialog extends Dialog {

    private boolean mIsNeedFocus = false;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.dialog);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_dialog);
        initView();
        setDialogParams();
    }

    private void initView() {
        ImageView loadingImageView = findViewById(R.id.loading_imageview);
        RotateAnimation animation = (RotateAnimation) AnimationUtils.loadAnimation(this.getContext(), R.anim.loading_animation);
        loadingImageView.startAnimation(animation);
    }

    public void setFocusNeeded() {
        mIsNeedFocus = true;
    }

    // set toast dialog width and height
    private void setDialogParams() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        // lp.width = (int) (ScreenConstant.SCREEN_WIDTH * 0.5);
        // lp.height = (int) (ScreenConstant.SCREEN_HEIGHT * 0.1);
        if( ! mIsNeedFocus ){
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
        window.setAttributes(lp);
    }

    // set position at screen
    public void setWindowPosition() {
        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.x = 230;
        lp.y = 0;
        window.setAttributes(lp);
    }
}
