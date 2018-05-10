package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mediatek.wwtv.mediaplayer.R;


/**
 * Created by sniuniu on 2018/3/26.
 */

public abstract class InfoDialog extends DialogFragment{

    private DialogInterface.OnKeyListener mOnKeyListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(setupContentView());
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        final Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM;
        window.setAttributes(layoutParams);

        initView(window.getDecorView());
        initData();
        initEvent();
        mOnKeyListener = getKeyListener();

        dialog.setOnKeyListener(mOnKeyListener);

        return dialog;
    }

    protected abstract int setupContentView();

    protected abstract void initView(View view);

    protected abstract void initData();

    protected abstract void initEvent();

    protected abstract DialogInterface.OnKeyListener getKeyListener();
}
