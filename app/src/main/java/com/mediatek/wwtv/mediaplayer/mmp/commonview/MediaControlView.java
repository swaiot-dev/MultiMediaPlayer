package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;

public class MediaControlView extends PopupWindow {

    public static final int NORMAL_SPEED = 1;

    private View mControlView;

    private LinearLayout ll_playStatus;
    private ImageView iv_playStatus;
    private TextView tv_speed;

    private LinearLayout ll_progress;
    private TextView tv_startTime;
    private ProgressBar pb_playProgress;
    private TextView tv_totalTime;

    public MediaControlView(View contentView) {
        super(contentView);
    }

    public MediaControlView(View contentView, int width, int height) {
        super(contentView, width, height);
    }

    public MediaControlView(Context context, View contentView, int width, int height) {
        super(contentView, width, height);
        this.mControlView = contentView;
        initView();
    }

    private void initView() {
        ll_playStatus = mControlView.findViewById(R.id.play_state_linear_layout);
        iv_playStatus = mControlView.findViewById(R.id.play_state_image_view);
        tv_speed = mControlView.findViewById(R.id.play_speed_text_view);

        ll_progress = mControlView.findViewById(R.id.play_progress_linear_layout);
        tv_startTime = mControlView.findViewById(R.id.current_play_time_text_view);
        pb_playProgress = mControlView.findViewById(R.id.play_media_progress_bar);
        tv_totalTime = mControlView.findViewById(R.id.total_play_time_text_view);
    }

    public boolean isPlayStatusShowing() {
        if (View.VISIBLE == ll_playStatus.getVisibility()) {
            return true;
        }
        return false;
    }

    public boolean isProgressShowing() {
        if (View.VISIBLE == ll_progress.getVisibility()) {
            return true;
        }
        return false;
    }

    public void showPlayStatusLayout() {
        ll_playStatus.setVisibility(View.VISIBLE);
    }

    public void hidePlayStatusLayout() {
        ll_playStatus.setVisibility(View.INVISIBLE);
    }

    public void showProgressLayout() {
        ll_progress.setVisibility(View.VISIBLE);
    }

    public void hideProgressLayout() {
        ll_progress.setVisibility(View.INVISIBLE);
    }

    public void showPause() {
        hideSpeed();
        iv_playStatus.setImageResource(R.drawable.mmp_thumbnail_player_icon_pause);
    }

    public void showPlay() {
        hideSpeed();
        iv_playStatus.setImageResource(R.drawable.mmp_thumbnail_player_icon_play);
    }

    public void showFastAndSpeed(int speed) {
        if (speed == NORMAL_SPEED) {
            showPlay();
            hideSpeed();
        } else {
            showFast();
            showSpeed();
            setSpeed(speed);
        }
    }

    public void showRewindAndSpeed(int speed) {
        if (speed == NORMAL_SPEED) {
            showPlay();
            hideSpeed();
        } else {
            showRewind();
            showSpeed();
            setSpeed(speed);
        }
    }

    private void showSpeed() {
        if (View.VISIBLE != tv_speed.getVisibility()) {
            tv_speed.setVisibility(View.VISIBLE);
        }
    }

    private void hideSpeed() {
        if (View.INVISIBLE != tv_speed.getVisibility()) {
            tv_speed.setVisibility(View.INVISIBLE);
        }
    }

    private void setSpeed(int speed) {
        tv_speed.setText(speed + "X");
    }

    private void showFast() {
        iv_playStatus.setImageResource(R.drawable.music_icon_control_next);
    }

    private void showRewind() {
        iv_playStatus.setImageResource(R.drawable.music_icon_control_previous);
    }

    public void setCurrTime(int currTime) {
        tv_startTime.setText(convertTimeToString(currTime));
        pb_playProgress.setProgress(currTime);
    }

    public void setTotalTime(int totalTime) {
        tv_totalTime.setText(convertTimeToString(totalTime));
        pb_playProgress.setMax(totalTime);
    }

    public int getTotalTime() {
        return pb_playProgress == null ? 0 : pb_playProgress.getMax();
    }


    public static String convertTimeToString(long mills) {
        long[] current = convertTimeToHMS(mills);
        return String.format("%02d:%02d:%02d", current[0], current[1], current[2]);
    }

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

}
