package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;

import java.util.Arrays;

public class AudioTrackListDialog extends DialogFragment implements AudioTrackAdapter.OnItemClickListener, DialogInterface.OnKeyListener {
    private static final String TAG = "AudioTrackListDialog";
    private LogicManager mLogicManager;
    private RecyclerView mAudio_rv;
    private AudioTrackAdapter mAudioAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.audio_track_list_layout);
        dialog.setCanceledOnTouchOutside(true); // 外部点击取消

        final Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.TOP | Gravity.RIGHT;
        window.setAttributes(layoutParams);

        initView(window.getDecorView());
        initData();

        dialog.setOnKeyListener(this);

        return dialog;
    }

    private void initView(View view) {
        mAudio_rv = view.findViewById(R.id.audio_list_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.VERTICAL, false);
        mAudio_rv.setLayoutManager(layoutManager);
        mAudioAdapter = new AudioTrackAdapter(null, -1);
        mAudioAdapter.setOnItemClickListener(this);
        mAudio_rv.setAdapter(mAudioAdapter);
    }

    private void initData() {
        mLogicManager = LogicManager.getInstance();
        int audioTrackIndex = mLogicManager.getAudioTrackIndex();
        String[] audioInfos = mLogicManager.getAudioTracks();
        String[] audioTracks = null;
        if (null != audioInfos && 0 != audioInfos.length){
            audioTracks = audioInfos;
//            audioTracks[audioInfos.length] = "off";
        }else {
            audioTracks = new String[1];
            audioTracks[0] = "off";
        }
        mAudioAdapter.setAudioTrackInfos(Arrays.asList(audioTracks), audioTrackIndex);
    }


    @Override
    public void onClick(View view, int position, String trackInfo) {
        if (trackInfo.equalsIgnoreCase("off")) {
            mLogicManager.setSubtitleTrack((short) 255);
            mLogicManager.setAudioTranckNumber((short) 0);
        } else {
            mLogicManager.setAudioTranckNumber((short) position);
        }
        mAudioAdapter.setSelectPosition(position);
    }

    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MENU:
                    ((MediaPlayActivity)getActivity()).showQMenu();
                    return true;
            }
        }
        return false;
    }
}
