package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;


/**
 * Created by sniuniu on 2018/3/26.
 */

public class MusicInfoDialog extends InfoDialog implements View.OnClickListener {

    private TextView mMusicTitle_tv;
    private TextView mMusicFormat_tv;

    private ImageView mMusicSimple_iv;
    private ImageView mMusicRepeatOne_iv;
    private ImageView mMusicRepeatAll_iv;
    private ImageView mMusicShuffle_iv;
    private LogicManager mLogicManager;

    @Override
    protected int setupContentView() {
        return R.layout.music_info_layout;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void initView(View view) {
        mMusicTitle_tv = view.findViewById(R.id.music_title_text_view);
        mMusicFormat_tv = view.findViewById(R.id.music_format_text_view);

        mMusicSimple_iv = view.findViewById(R.id.music_simple_image_view);
        mMusicRepeatOne_iv = view.findViewById(R.id.music_repeat_one_image_view);
        mMusicRepeatAll_iv = view.findViewById(R.id.music_repeat_all_image_view);
        mMusicShuffle_iv = view.findViewById(R.id.music_shuffle_image_view);

        mMusicSimple_iv.setFocusable(true);
        mMusicRepeatOne_iv.setFocusable(true);
        mMusicRepeatAll_iv.setFocusable(true);
        mMusicShuffle_iv.setFocusable(true);

    }

    @Override
    protected void initData() {

        mLogicManager = LogicManager.getInstance();
        String musicName = mLogicManager.getCurrentFileName(Const.FILTER_AUDIO);
        mMusicTitle_tv.setText(musicName);

        String format = musicName.substring(musicName.lastIndexOf(".") + 1);
        mMusicFormat_tv.setText(format);

        int repeatModel = mLogicManager.getRepeatModel(Const.FILTER_AUDIO);
        mMusicSimple_iv.setBackgroundResource(R.drawable.board_normal_selector);
        mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_normal_selector);
        mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_normal_selector);
        switch (repeatModel) {
            case Const.REPEAT_NONE:
                mMusicSimple_iv.setBackgroundResource(R.drawable.board_selected_selector);
                break;
            case Const.REPEAT_ONE:
                mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_selected_selector);
                break;
            case Const.REPEAT_ALL:
                mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_selected_selector);
                break;
        }
        boolean shuffle = mLogicManager.getShuffleMode(Const.FILTER_AUDIO);
        if (shuffle){
            mMusicShuffle_iv.setBackgroundResource(R.drawable.board_selected_selector);
        }else {
            mMusicShuffle_iv.setBackgroundResource(R.drawable.board_normal_selector);
        }

    }

    @Override
    public void initEvent() {
        mMusicSimple_iv.setOnClickListener(this);
        mMusicRepeatOne_iv.setOnClickListener(this);
        mMusicRepeatAll_iv.setOnClickListener(this);
        mMusicShuffle_iv.setOnClickListener(this);

        mMusicSimple_iv.setOnFocusChangeListener(mOnFocusChangeListener);
        mMusicRepeatOne_iv.setOnFocusChangeListener(mOnFocusChangeListener);
        mMusicRepeatAll_iv.setOnFocusChangeListener(mOnFocusChangeListener);
        mMusicShuffle_iv.setOnFocusChangeListener(mOnFocusChangeListener);

        mMusicSimple_iv.requestFocus();
    }

    @Override
    protected DialogInterface.OnKeyListener getKeyListener() {
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.music_simple_image_view:
                mLogicManager.setRepeatMode(Const.FILTER_AUDIO, Const.REPEAT_NONE);
                //add by y.wan for fix bug88056 start 2018/5/9
                setAudioShuffle(false);
                //add by y.wan for fix bug88056 end 2018/5/9
                mMusicSimple_iv.setBackgroundResource(R.drawable.board_selected_selector);
                mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_normal_selector);
                mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_normal_selector);
                break;
            case R.id.music_repeat_one_image_view:
                mLogicManager.setRepeatMode(Const.FILTER_AUDIO, Const.REPEAT_ONE);
                //add by y.wan for fix bug88056 start 2018/5/9
                setAudioShuffle(false);
                //add by y.wan for fix bug88056 end 2018/5/9
                mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_selected_selector);
                mMusicSimple_iv.setBackgroundResource(R.drawable.board_normal_selector);
                mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_normal_selector);
                break;
            case R.id.music_repeat_all_image_view:
                mLogicManager.setRepeatMode(Const.FILTER_AUDIO, Const.REPEAT_ALL);
                //add by y.wan for fix bug88056 start 2018/5/9
                setAudioShuffle(false);
                //add by y.wan for fix bug88056 end 2018/5/9
                mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_selected_selector);
                mMusicSimple_iv.setBackgroundResource(R.drawable.board_normal_selector);
                mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_normal_selector);

                break;
            case R.id.music_shuffle_image_view:
                /*boolean shuffle = mLogicManager.getShuffleMode(Const.FILTER_AUDIO);
                mLogicManager.setShuffle(Const.FILTER_AUDIO, !shuffle);
                if (!shuffle){
                    mMusicShuffle_iv.setBackgroundResource(R.drawable.board_selected_selector);
                }else {
                    mMusicShuffle_iv.setBackgroundResource(R.drawable.board_normal_selector);
                }*/
                //add by y.wan for fix bug88056 start 2018/5/9
                mLogicManager.setRepeatMode(Const.FILTER_AUDIO, -1);
                setAudioShuffle(true);
                mMusicSimple_iv.setBackgroundResource(R.drawable.board_normal_selector);
                mMusicRepeatOne_iv.setBackgroundResource(R.drawable.board_normal_selector);
                mMusicRepeatAll_iv.setBackgroundResource(R.drawable.board_normal_selector);
                //add by y.wan for fix bug 88056 end 2018/5/9

                break;
        }
    }

    private void setAudioShuffle(boolean shuffle) {
        mLogicManager.setShuffle(Const.FILTER_AUDIO, shuffle);
        mMusicShuffle_iv.setBackgroundResource(shuffle ? R.drawable.board_selected_selector :
                R.drawable.board_normal_selector);

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
