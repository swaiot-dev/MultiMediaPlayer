package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.mediatek.SubtitleTrackInfo;
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

    private TextView mVideoAudioCurStatusTv, mVideoSubtitleCurStatusTv;
    private LogicManager logicManager;
    private String[] mAudioTracks;
    private SubtitleTrackInfo[] mSubtitleTrackInfo;
    private int audioTrackIndex;
    private int subtitleTrackIndex;

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

        mVideoAudioCurStatusTv = view.findViewById(R.id.video_audio_current_status_tv);
        mVideoSubtitleCurStatusTv = view.findViewById(R.id.video_subtitle_current_status_tv);

        mVideoSubtitle_tv.setFocusable(true);
        mVideoAudio_tv.setFocusable(true);

    }

    @Override
    protected void initData() {

        logicManager = LogicManager.getInstance();
        String videoName = logicManager.getFileName();
        mVideoTitle_tv.setText(videoName);

        String format = videoName.substring(videoName.lastIndexOf(".") + 1);
        mVideoFormat_tv.setText(format);

        int width = logicManager.getVideoWidth();
        int height = logicManager.getVideoHeight();
        mVideoResolution_tv.setText(width + " X " + height);

        initAudioTvData();
        initSubtitleTvData();

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
                            //changed by y.wan for intercept subtitle start 2018/5/10
                            /*showSubtitle();
                            dismiss();*/
                            //changed by y.wan for intercept subtitle end 2018/5/10
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
                /*showAudioTrack();
                dismiss();*/
                //add by y.wan for setting track info start 2018/5/9
                audioTrackIndex ++;
                if (audioTrackIndex > mAudioTracks.length) {
                    audioTrackIndex = 1;
                }
                setAudioTrackNumber(mAudioTracks[audioTrackIndex -1], audioTrackIndex -1);
                //add by y.wan for setting track info end 2018/5/9
                break;
            case R.id.video_subtitle_text_view:
                /*showSubtitle();
                dismiss();*/
                //add by y.wan for setting subtitle info start 2018/5/9
                subtitleTrackIndex ++;
                if (subtitleTrackIndex > mSubtitleTrackInfo.length) {
                    subtitleTrackIndex = 1;
                }
                setSubtitleTrackInfo(subtitleTrackIndex - 1, mSubtitleTrackInfo[subtitleTrackIndex - 1]);
                //add by y.wan for setting subtitle info end 2018/5/9
                break;
        }
    }

    public void showSubtitle() {
        ((VideoPlayActivity)getActivity()).showSubtitleView();
    }

    public void showAudioTrack() {
        ((VideoPlayActivity)getActivity()).showAudioTrackView();
    }

    /**
     * init audio textview and some status
     * @author y.wan
     * create at 2018/5/9
     */
    private void initAudioTvData() {
        mAudioTracks = getAudioTracks();
        audioTrackIndex = logicManager.getAudioTrackIndex() + 1;
        if (mAudioTracks[0].equalsIgnoreCase("off")) {
            mVideoAudio_tv.setFocusable(false);
            mVideoAudioCurStatusTv.setText("off");
        } else {
            mVideoAudioCurStatusTv.setText(String.format("%d/%d", audioTrackIndex, mAudioTracks.length));
        }
    }

    /**
     * nit subtitle textview and some status
     * @author y.wan
     * create at 2018/5/9
     */
    private void initSubtitleTvData() {
        mSubtitleTrackInfo = getSubtitleTracks();
        subtitleTrackIndex = logicManager.getSubtitleIndex() == 65535 ? mSubtitleTrackInfo.length:
                logicManager.getSubtitleIndex();
        if (mSubtitleTrackInfo.length == 1) {
            mVideoSubtitle_tv.setFocusable(false);
            mVideoSubtitleCurStatusTv.setText(getString(R.string.menu_video_mjc_demo_off));
        } else {
            if (logicManager.getSubtitleIndex() == 65535) {
                mVideoSubtitleCurStatusTv.setText(getString(R.string.menu_video_mjc_demo_off));
            } else {
                mVideoSubtitleCurStatusTv.setText(String.format("%d/%d", subtitleTrackIndex + 1, mSubtitleTrackInfo.length - 1));
            }
        }
    }

    /**
     * obtain all audio tracks
     * @author y.wan
     * create at 2018/5/9
     */
    private String[] getAudioTracks() {
        logicManager = LogicManager.getInstance();
        String[] audioInfos = logicManager.getAudioTracks();
        String[] audioTracks = null;
        if (null != audioInfos && 0 != audioInfos.length){
            for (String info : audioInfos) {
                Log.d("y.wan", "**audio track**" + info);
            }
            return audioInfos;
        }else {
            audioTracks = new String[1];
            audioTracks[0] = "off";
            return audioTracks;
        }
    }

    /**
     * set audio track info and change text content
     * @author y.wan
     * create at 2018/5/9
     */
    private void setAudioTrackNumber(String trackInfo, int position) {
        Log.d("y.wan", "####audio track#####" + trackInfo);

        if (trackInfo.equalsIgnoreCase("off")) {
            logicManager.setSubtitleTrack((short) 255);
            logicManager.setAudioTranckNumber((short) 0);
        } else {
            logicManager.setAudioTranckNumber((short) position);
        }

        mVideoAudioCurStatusTv.setText(String.format("%d/%d", position + 1, mAudioTracks.length));
    }

    /**
     * obtain subtitle track infos
     * @author y.wan
     * create at 2018/5/9
     */
    private SubtitleTrackInfo[] getSubtitleTracks() {
        SubtitleTrackInfo[] trackInfos = logicManager.getSubtitleTracks();
        SubtitleTrackInfo[] subtitleTracks;
        if (null != trackInfos && 0 != trackInfos.length) {
            subtitleTracks = new SubtitleTrackInfo[trackInfos.length + 1];
            for (int i = 0; i < trackInfos.length; i++) {
                subtitleTracks[i] = trackInfos[i];
            }
            subtitleTracks[trackInfos.length] = new SubtitleTrackInfo(-1, -1, "off", null);
        } else {
            subtitleTracks = new SubtitleTrackInfo[1];
            subtitleTracks[0] = new SubtitleTrackInfo(-1, -1, "off", null);
        }

        for (SubtitleTrackInfo info : subtitleTracks) {
            Log.d("y.wan", "#####subtitle track####" + info.getSubtitleLanguage());
        }

        return subtitleTracks;
    }

    /**
     * set subtitle info and change text content
     * @author y.wan
     * create at 2018/5/9
     */
    private void setSubtitleTrackInfo(int position, SubtitleTrackInfo trackInfo) {
        Log.d("y.wan", "####subtitle track#####" + trackInfo.getSubtitleLanguage());

        if (trackInfo.getSubtitleLanguage().equalsIgnoreCase("off")) {
            logicManager.setSubtitleTrack((short) 255);
        } else {
            logicManager.setSubtitleTrack((short) position);
        }

        if (position == mSubtitleTrackInfo.length -1) {
            mVideoSubtitleCurStatusTv.setText(getString(R.string.menu_video_mjc_demo_off));
        } else {
            mVideoSubtitleCurStatusTv.setText(String.format("%d/%d", subtitleTrackIndex, mSubtitleTrackInfo.length - 1));
        }
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
