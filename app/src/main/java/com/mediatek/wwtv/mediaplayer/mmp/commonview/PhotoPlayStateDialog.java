package com.mediatek.wwtv.mediaplayer.mmp.commonview;

public class PhotoPlayStateDialog extends PlayStateDialog {

    private int playState = PLAY;

    @Override
    protected void fast() {

    }

    @Override
    protected void rewind() {

    }

    @Override
    protected void play() {
        mControlPlayState.play();
        playState = PLAY;
    }

    @Override
    protected void pause() {
        mControlPlayState.pause();
        playState = PAUSE;
    }

    @Override
    protected int getSpeed() {
        return 1;
    }

    @Override
    protected int getPlayState() {
        return playState;
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
