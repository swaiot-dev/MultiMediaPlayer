package com.mediatek.wwtv.mediaplayer.util;

import android.util.Log;
import android.content.Context;

import com.mediatek.mtkaudiopatchmanager.MtkAudioPatchManager;

public class AudioBTManager {

  private static final String TAG = "AudioBTManager";
  private static AudioBTManager mInstance;
  private Context mContext;
  private MtkAudioPatchManager mMtkAudioPatchManager;
  private boolean mHasCreated;

  private AudioBTManager(Context context) {
    mContext = context;
    mMtkAudioPatchManager = new MtkAudioPatchManager(mContext);
  }

  public static AudioBTManager getInstance(Context context) {
    if (mInstance == null) {
      mInstance = new AudioBTManager(context);
    }
    return mInstance;
  }

  public boolean creatAudioPatch() {
    if (!mHasCreated && !Util.mIsUseEXOPlayer) {
      mHasCreated = true;
      boolean reslut = mMtkAudioPatchManager.createAudioPatch();
      Log.i(TAG,"createAudioPatch reslut = " + reslut);
      return reslut;
    } else {
      Log.i(TAG,"createAudioPatch mHasCreated = " + mHasCreated + ",Util.mIsUseEXOPlayer=="+Util.mIsUseEXOPlayer);
      return true;
    }
  }

  public boolean releaseAudioPatch() {
    if (mHasCreated && !Util.mIsUseEXOPlayer) {
      mHasCreated = false;
      boolean reslut = mMtkAudioPatchManager.releaseAudioPatch();
      Log.i(TAG,"releaseAudioPatch reslut = " + reslut);
      mMtkAudioPatchManager = null;
      mContext = null;
      mInstance = null;
      return reslut;
    } else {
      Log.i(TAG,"releaseAudioPatch mHasCreated = " + mHasCreated + ",Util.mIsUseEXOPlayer=="+Util.mIsUseEXOPlayer);
      return true;
    }
  }

}