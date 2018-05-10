package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;

public class AudioPlayStateDialog extends PlayStateDialog {
    @Override
    protected void fast() {

    }

    @Override
    protected void rewind() {

    }

    @Override
    protected void play() {
        mLogicManager.playAudio();
    }

    @Override
    protected void pause() {
        mLogicManager.pauseAudio();
    }

    @Override
    protected int getSpeed() {
        return 1;
    }

    @Override
    protected int getPlayState() {
        int status = mLogicManager.getAudioStatus();
        if (AudioConst.PLAY_STATUS_STARTED == status) {
            return PLAY;
        } else if (AudioConst.PLAY_STATUS_PAUSED == status) {
            return PAUSE;
        }
        return PLAY;
    }

    @Override
    protected boolean canFast() {
        return false;
    }

    @Override
    protected boolean canRewind() {
        return false;
    }
}
