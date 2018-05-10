package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;

public class VideoPlayStateDialog extends PlayStateDialog {

    @Override
    protected void fast() {
        mLogicManager.fastForwardVideo();
    }

    @Override
    protected void rewind() {
        mLogicManager.fastRewindVideo();
    }

    @Override
    protected void play() {
        mLogicManager.playVideo();
    }

    @Override
    protected void pause() {
        mLogicManager.pauseVideo();
    }

    @Override
    protected int getSpeed() {
        return mLogicManager.getVideoSpeed();
    }

    @Override
    protected int getPlayState() {
        int status = mLogicManager.getVideoPlayStatus();
        if (VideoConst.PLAY_STATUS_STARTED == status) {
            return PLAY;
        } else if (VideoConst.PLAY_STATUS_PAUSED == status) {
            return PAUSE;
        } else if (VideoConst.PLAY_STATUS_FF == status) {
            return FAST;
        } else if (VideoConst.PLAY_STATUS_FR == status) {
            return REWIND;
        }
        return ERROR;
    }

    @Override
    protected boolean canFast() {
        return true;
    }

    @Override
    protected boolean canRewind() {
        return true;
    }

}
