package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.util.KeyMap;

import java.lang.ref.WeakReference;

public abstract class PlayStateDialog extends DialogFragment {

    public static final int OPERATOR_PLAY = 0x0001;
    public static final int OPERATOR_FAST = 0x0002;
    public static final int OPERATOR_REWIND = 0x0003;
    public static final int PLAY = 0x0100;
    public static final int PAUSE = 0x0101;
    public static final int FAST = 0x0102;
    public static final int REWIND = 0x0103;
    public static final int ERROR = 0x0104;

    private static final int AUTO_HIDE = 1003;
    private static final long DELAY_TIME = 5000;

    private ImageView mPlayState_iv;
    private TextView mSpeed_tv;
    protected LogicManager mLogicManager;
    private int mOperatorType;
    private PlayStateHandler mHandler = new PlayStateHandler(this);
    protected ControlPlayState mControlPlayState;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        mOperatorType = bundle.getInt("operator_type", OPERATOR_PLAY);

        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.play_or_pause_control_layout);
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        final Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.TOP | Gravity.LEFT;
        window.setAttributes(layoutParams);

        initView(window.getDecorView());
        initData();

        dialog.setOnKeyListener(mOnKeyListener);

        return dialog;
    }

    public void setControlPlayState(ControlPlayState controlPlayState) {
        mControlPlayState = controlPlayState;
    }

    private void initView(View view) {
        mPlayState_iv = view.findViewById(R.id.play_state_image_view);
        mSpeed_tv = view.findViewById(R.id.play_speed_text_view);
        mSpeed_tv.setVisibility(View.GONE);
    }

    private void initData() {
        mLogicManager = LogicManager.getInstance();
        if (OPERATOR_PLAY == mOperatorType) {
            mSpeed_tv.setVisibility(View.GONE);
            if (PAUSE == getPlayState()) {
                play();
                mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
            } else if (PLAY == getPlayState()) {
                pause();
                mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_pause);
            }
        } else if (OPERATOR_FAST == mOperatorType) {
            if (canFast()) {
                fast();
                int speed = getSpeed();
                if (1 == speed) {
                    mSpeed_tv.setVisibility(View.GONE);
                    mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                    sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                } else {
                    mSpeed_tv.setVisibility(View.VISIBLE);
                    mPlayState_iv.setImageResource(R.drawable.music_icon_control_next);
                    cancelMessage(mHandler, AUTO_HIDE);
                    mSpeed_tv.setText(speed + "X");
                }
            }
        } else if (OPERATOR_REWIND == mOperatorType) {
            if (canRewind()) {
                rewind();
                int speed = getSpeed();
                if (1 == speed) {
                    mSpeed_tv.setVisibility(View.GONE);
                    mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                    sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                } else {
                    mSpeed_tv.setVisibility(View.VISIBLE);
                    mPlayState_iv.setImageResource(R.drawable.music_icon_control_previous);
                    cancelMessage(mHandler, AUTO_HIDE);
                    mSpeed_tv.setText(speed + "X");
                }
            }
        }
    }

    private void sendDelayMessage(Handler handler, int flag, long delayTime) {
        if (handler.hasMessages(flag)) {
            handler.removeMessages(flag);
        }
        handler.sendEmptyMessageDelayed(flag, delayTime);
    }

    private void cancelMessage(Handler handler, int flag) {
        if (handler.hasMessages(flag)) {
            handler.removeMessages(flag);
        }
    }

    protected abstract void fast();

    protected abstract void rewind();

    protected abstract void play();

    protected abstract void pause();

    protected abstract int getSpeed();

    protected abstract int getPlayState();

    protected abstract boolean canFast();

    protected abstract boolean canRewind();

    private DialogInterface.OnKeyListener mOnKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyCode) {
                    case KeyMap.KEYCODE_DPAD_CENTER:
                    case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
                        if (PLAY != getPlayState()) {
                            play();
                            mSpeed_tv.setVisibility(View.GONE);
                            mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                            sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                        } else {
                            pause();
                            mSpeed_tv.setVisibility(View.GONE);
                            mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_pause);
                            cancelMessage(mHandler, AUTO_HIDE);
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        if (PLAY != getPlayState()) {
                            play();
                            mSpeed_tv.setVisibility(View.GONE);
                            mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                            sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                        }
                        return true;
                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        if (PAUSE != getPlayState()) {
                            pause();
                            mSpeed_tv.setVisibility(View.GONE);
                            mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_pause);
                            cancelMessage(mHandler, AUTO_HIDE);
                        }
                        return true;
                    case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
                        if (canFast()) {
                            fast();
                            int speed = getSpeed();
                            if (1 == speed) {
                                mSpeed_tv.setVisibility(View.GONE);
                                mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                                sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                            } else {
                                mPlayState_iv.setImageResource(R.drawable.music_icon_control_next);
                                mSpeed_tv.setVisibility(View.VISIBLE);
                                cancelMessage(mHandler, AUTO_HIDE);
                                mSpeed_tv.setText(speed + "X");
                            }
                        }
                        return true;
                    case KeyMap.KEYCODE_MTKIR_REWIND:
                        if (canRewind()) {
                            rewind();
                            int speed = getSpeed();
                            if (1 == speed) {
                                mSpeed_tv.setVisibility(View.GONE);
                                mPlayState_iv.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
                                sendDelayMessage(mHandler, AUTO_HIDE, DELAY_TIME);
                            } else {
                                mPlayState_iv.setImageResource(R.drawable.music_icon_control_previous);
                                mSpeed_tv.setVisibility(View.VISIBLE);
                                cancelMessage(mHandler, AUTO_HIDE);
                                mSpeed_tv.setText(speed + "X");
                            }
                        }
                        return true;
                }
            }
            return false;
        }
    };

    private static final class PlayStateHandler extends Handler {
        private final WeakReference<PlayStateDialog> mWeakReference;

        public PlayStateHandler(PlayStateDialog dialog) {
            mWeakReference = new WeakReference<>(dialog);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayStateDialog playStateDialog = mWeakReference.get();
            switch (msg.what) {
                case AUTO_HIDE:
                    if (null != playStateDialog) {
                        playStateDialog.dismiss();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public interface ControlPlayState {
        void play();

        void pause();
    }
}
