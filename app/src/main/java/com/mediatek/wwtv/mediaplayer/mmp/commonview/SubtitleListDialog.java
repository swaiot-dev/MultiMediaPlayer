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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.mediatek.SubtitleTrackInfo;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;

import java.util.Arrays;

public class SubtitleListDialog extends DialogFragment implements SubtitleAdapter.OnItemClickListener, DialogInterface.OnKeyListener {
    private static final String TAG = "SubtitleListDialog";
    private LogicManager mLogicManager;
    private RecyclerView mSubtitle_rv;
    private SubtitleAdapter subtitleAdapter;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
        dialog.setContentView(R.layout.subtitle_list_layout);
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
        mSubtitle_rv = view.findViewById(R.id.subtitle_list_recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(),
                LinearLayoutManager.VERTICAL, false);
        mSubtitle_rv.setLayoutManager(layoutManager);
        subtitleAdapter = new SubtitleAdapter(null, -1);
        subtitleAdapter.setOnItemClickListener(this);
        mSubtitle_rv.setAdapter(subtitleAdapter);
    }

    private void initData() {
        mLogicManager = LogicManager.getInstance();
        SubtitleTrackInfo[] trackInfos = mLogicManager.getSubtitleTracks();
        SubtitleTrackInfo[] subtitleTracks;
        if (null != trackInfos && 0 != trackInfos.length) {
            subtitleTracks = new SubtitleTrackInfo[trackInfos.length + 1];
            for (int i = 0; i < trackInfos.length; i++) {
                SubtitleTrackInfo subtitleTrack = trackInfos[i];
                subtitleTracks[i] = trackInfos[i];
            }
            subtitleTracks[trackInfos.length] = new SubtitleTrackInfo(-1, -1, "off", null);
        } else {
            subtitleTracks = new SubtitleTrackInfo[1];
            subtitleTracks[0] = new SubtitleTrackInfo(-1, -1, "off", null);
        }

        int subtitleIndex = mLogicManager.getSubtitleIndex();
        Log.d(TAG, "subtitle track is ==> subtitleIndex :" + subtitleIndex);
        if (65535 == subtitleIndex) {
            subtitleIndex = subtitleTracks.length - 1;
        }
        Log.d(TAG, "subtitle processed track is ==> subtitleIndex :" + subtitleIndex);
        subtitleAdapter.setSubtitleTrackInfos(Arrays.asList(subtitleTracks), subtitleIndex);
    }

    @Override
    public void onClick(View view, int position, SubtitleTrackInfo trackInfo) {
        if (trackInfo.getSubtitleLanguage().equalsIgnoreCase("off")) {
            mLogicManager.setSubtitleTrack((short) 255);
        } else {
            mLogicManager.setSubtitleTrack((short) position);
        }
        subtitleAdapter.setSelectPosition(position);
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
