package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

import com.mediatek.wwtv.util.KeyMap;

public class AudioProgressBarDialog extends ProgressBarDialog {
    @Override
    protected int getCurrTime() {
        return mLogicManager.getPlaybackProgress();
    }

    @Override
    protected int getTotalTime() {
        return mLogicManager.getTotalPlaybackTime();
    }

    @Override
    protected void seek(int position) {
        mLogicManager.seekToCertainTime(position);
    }

    @Override
    protected boolean isPlaying() {
        return mLogicManager.isAudioPlaying();
    }

    @Override
    protected DialogInterface.OnKeyListener getOnKeyListener() {
        return new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                Message message = getMessage();
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                            cancelMessage(AUTO_HIDE);
                            PlayStateDialog playStateDialog = null;
                            Bundle bundle = new Bundle();
                            if (isPlaying()) {
                                cancelMessage(CHANGE_PROGRESS);
                            } else {
                                sendMessage(CHANGE_PROGRESS);
                            }
                            playStateDialog = new AudioPlayStateDialog();
                            bundle.putInt("operator_type", PlayStateDialog.OPERATOR_PLAY);
                            playStateDialog.setArguments(bundle);
                            playStateDialog.show(getFragmentManager(), "play_state");
                            sendDelayMessage(AUTO_HIDE, 10 * 1000);
                            return true;
                        }
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                        case KeyMap.KEYCODE_MTKIR_FASTFORWARD: {
                            cancelMessage(AUTO_HIDE);
                            currPosition = getCurrTime();
                            currPosition += 10 * 1000;
                            if (currPosition > getTotalTime()) {
                                currPosition = getTotalTime();
                            }
                            message.what = SEEK;
                            message.arg1 = currPosition;
                            sendMessage(message);
                            return true;
                        }
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                        case KeyMap.KEYCODE_MTKIR_REWIND: {
                            cancelMessage(AUTO_HIDE);
                            currPosition = getCurrTime();
                            currPosition -= 10 * 1000;
                            if (currPosition < 0) {
                                currPosition = 0;
                            }
                            message.what = SEEK;
                            message.arg1 = currPosition;
                            sendMessage(message);
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            }
        };
    }
}
