package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;

import java.lang.ref.WeakReference;


public abstract class ProgressBarDialog extends DialogFragment {
    private static final String TAG = ProgressBarDialog.class.getSimpleName();

    protected static final int CHANGE_PROGRESS = 1001;
    protected static final int SEEK = 1002;
    protected static final int AUTO_HIDE = 1003;
    private TextView mCurrPlayTime_tv;
    private ProgressBar mPlayMediaProgress_pb;
    private TextView mTotalPlayTime_tv;
    protected LogicManager mLogicManager;
    protected int currPosition;
    protected final ProgressHandler mHandler = new ProgressHandler(this);
    private DialogInterface.OnKeyListener mOnKeyListener = getOnKeyListener();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.progress_layout);
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

        dialog.setOnKeyListener(mOnKeyListener);

        return dialog;
    }

    private void initView(View view) {
        mCurrPlayTime_tv = view.findViewById(R.id.current_play_time_text_view);
        mPlayMediaProgress_pb = view.findViewById(R.id.play_media_progress_bar);
        mTotalPlayTime_tv = view.findViewById(R.id.total_play_time_text_view);
    }

    private void initData() {
        mLogicManager = LogicManager.getInstance();
        int currTime = getCurrTime();
        int totalTime = getTotalTime();

        long current[] = convertTimeToHMS(currTime);
        mCurrPlayTime_tv.setText(String.format("%02d:%02d:%02d", current[0], current[1], current[2]));
        long total[] = convertTimeToHMS(totalTime);
        mTotalPlayTime_tv.setText(String.format("%02d:%02d:%02d", total[0], total[1], total[2]));

        mPlayMediaProgress_pb.setMax(totalTime);
        mPlayMediaProgress_pb.setProgress(currTime);
        mHandler.sendEmptyMessage(CHANGE_PROGRESS);
        mHandler.sendEmptyMessageDelayed(AUTO_HIDE, 10 * 1000);
    }

    protected abstract int getCurrTime();

    protected abstract int getTotalTime();

    protected abstract void seek(int position);

    protected abstract boolean isPlaying();

    protected abstract DialogInterface.OnKeyListener getOnKeyListener();

    public static long[] convertTimeToHMS(long mills) {
        long time[] = new long[3];
        mills /= 1000;
        long minute = mills / 60;
        long hour = minute / 60;
        long second = mills % 60;
        minute %= 60;
        time[0] = hour;
        time[1] = minute;
        time[2] = second;
        return time;
    }

    protected Message getMessage(){
        return mHandler.obtainMessage();
    }

    protected void cancelMessage(int what){
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
    }

    protected void sendMessage(int what){
        sendDelayMessage(what,0);
    }

    protected void sendDelayMessage(int what, long delay){
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
    }

    protected void sendMessage(Message message){
        mHandler.sendMessage(message);
    }

    private static final class ProgressHandler extends Handler {
        private final WeakReference<ProgressBarDialog> mWeakReference;

        public ProgressHandler(ProgressBarDialog dialog) {
            mWeakReference = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            ProgressBarDialog progressBarDialog = mWeakReference.get();
            switch (msg.what) {
                case CHANGE_PROGRESS:
                    if (null != progressBarDialog) {
                        int currTime = progressBarDialog.getCurrTime();
                        long current[] = convertTimeToHMS(currTime);
                        progressBarDialog.mCurrPlayTime_tv.setText(
                                String.format("%02d:%02d:%02d", current[0], current[1], current[2]));
                        progressBarDialog.mPlayMediaProgress_pb.setProgress(currTime);
                        sendEmptyMessageDelayed(CHANGE_PROGRESS, 200);
                    }
                    break;
                case SEEK:
                    int seekPos = msg.arg1;
                    if (null != progressBarDialog) {
                        long current[] = convertTimeToHMS(seekPos);
                        progressBarDialog.mCurrPlayTime_tv.setText(
                                String.format("%02d:%02d:%02d", current[0], current[1], current[2]));
                        progressBarDialog.mPlayMediaProgress_pb.setProgress(seekPos);
                        progressBarDialog.seek(seekPos);
                        sendEmptyMessageDelayed(AUTO_HIDE, 10 * 1000);
                    }
                    break;
                case AUTO_HIDE:
                    if (null != progressBarDialog) {
                        progressBarDialog.dismiss();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
