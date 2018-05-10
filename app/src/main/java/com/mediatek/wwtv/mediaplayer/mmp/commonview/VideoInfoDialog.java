package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.util.KeyMap;


/**
 * Created by sniuniu on 2018/3/26.
 */

public class VideoInfoDialog extends InfoDialog implements View.OnClickListener {

    private TextView mVideoTitle_tv;
    private TextView mVideoFormat_tv;
    private TextView mVideoResolution_tv;
    private TextView mVideoSubtitle_tv;
    private TextView mVideoAudio_tv;

    @Override
    protected int setupContentView() {
        return R.layout.video_info_layout;
    }

    @Override
    public void initView(View view) {
        mVideoTitle_tv = view.findViewById(R.id.video_title_text_view);
        mVideoFormat_tv = view.findViewById(R.id.video_format_text_view);
        mVideoResolution_tv = view.findViewById(R.id.video_resolution_text_view);

        mVideoSubtitle_tv = view.findViewById(R.id.video_subtitle_text_view);
        mVideoAudio_tv = view.findViewById(R.id.video_audio_text_view);

        mVideoSubtitle_tv.setFocusable(true);
        mVideoAudio_tv.setFocusable(true);

    }

    @Override
    protected void initData() {

        LogicManager logicManager = LogicManager.getInstance();
        String videoName = logicManager.getFileName();
        mVideoTitle_tv.setText(videoName);

        String format = videoName.substring(videoName.lastIndexOf(".") + 1);
        mVideoFormat_tv.setText(format);

        int width = logicManager.getVideoWidth();
        int height = logicManager.getVideoHeight();
        mVideoResolution_tv.setText(width + " X " + height);
    }

    @Override
    public void initEvent() {
        mVideoSubtitle_tv.setOnClickListener(this);
        mVideoAudio_tv.setOnClickListener(this);

        mVideoSubtitle_tv.setOnFocusChangeListener(mOnFocusChangeListener);
        mVideoAudio_tv.setOnFocusChangeListener(mOnFocusChangeListener);

        mVideoAudio_tv.requestFocus();
    }

    @Override
    protected DialogInterface.OnKeyListener getKeyListener() {
        return new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction()){
                    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
                        keyCode = event.getScanCode();
                    }
                    switch (keyCode){
                        case KeyMap.KEYCODE_MTKIR_MTSAUDIO:
                            showAudioTrack();
                            dismiss();
                            return true;
                        case KeyMap.KEYCODE_MTKIR_MTKIR_CC:
                        case KeyMap.KEYCODE_MTKIR_SUBTITLE:
                            showSubtitle();
                            dismiss();
                            return true;
                    }
                }
                return false;
            }
        };
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.video_audio_text_view:
                showAudioTrack();
                dismiss();
                break;
            case R.id.video_subtitle_text_view:
                showSubtitle();
                dismiss();
                break;
        }
    }

    public void showSubtitle() {
        ((VideoPlayActivity)getActivity()).showSubtitleView();
    }

    public void showAudioTrack() {
        ((VideoPlayActivity)getActivity()).showAudioTrackView();
    }

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                ViewCompat.animate(view).scaleX(1.17f).scaleY(1.17f).translationZ(1).start();
            } else {
                ViewCompat.animate(view).scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
            }
        }
    };

}
