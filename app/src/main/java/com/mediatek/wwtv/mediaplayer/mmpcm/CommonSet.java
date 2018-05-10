
package com.mediatek.wwtv.mediaplayer.mmpcm;

import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvVolCtrl;
import com.mediatek.twoworlds.tv.MtkTvAVMode;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.model.MtkTvRectangle;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.mediaplayer.util.MarketRegionInfo;
import com.mediatek.wwtv.mediaplayer.util.Util;

import android.media.AudioManager;
import android.util.Log;
import android.content.Context;
import android.media.session.MediaSessionManager;

/*
 *
 * set audiovolume,picture mode,screen mode,videoresource
 *
 */
public class CommonSet implements ICommonSet {
  public static final String TAG = "CommonSet";

  public static final int COMMON_OFF = 0;

  public static final int COMMON_ON = 1;

  /*
   * public static final int VID_SCREEN_MODE_AUTO = 0; public static final int
   * VID_SCREEN_MODE_NORMAL = 1; public static final int VID_SCREEN_MODE_LETTER_BOX = 2; public
   * static final int VID_SCREEN_MODE_PAN_SCAN = 3; public static final int
   * VID_SCREEN_MODE_NON_LINEAR_ZOOM = 4; public static final int VID_SCREEN_MODE_DOT_BY_DOT = 5;
   * <item>Unknown</item> <item>Normal</item> <item>Letterbox</item> <item>Pan Scan</item>
   * <item>User Defined</item> <item>Non Linear</item> <item>Dot by Dot</item> <item>Auto</item>
   */

  private static final String SOURCE_MAIN = "main";
  public static final int VID_SCREEN_MODE_AUTO = 7;
  public static final int VID_SCREEN_MODE_NORMAL = 1;
  public static final int VID_SCREEN_MODE_LETTER_BOX = 2;
  public static final int VID_SCREEN_MODE_PAN_SCAN = 3;
  public static final int VID_SCREEN_MODE_NON_LINEAR_ZOOM = 5;
  public static final int VID_SCREEN_MODE_DOT_BY_DOT = 6;
  public static final int VID_SCREEN_MODE_CUSTOM_DEF_1 = 8;

  private int volumeMax = -1;
  private int volumeMin = -1;
  private int picModeMax = -1;
  private int picModeMin = -1;
  private int scnModeMax = -1;
  private int scnModeMin = -1;

  private final MtkTvUtil mtkTvUtil;
  private static CommonSet mmpCom = null;
  private final Context mContext;
  /*
   * private TVMethods mTV; private TVOptionRange<Integer> volumeOption; private
   * TVOptionRange<Integer> picModeOption; private TVOptionRange<Integer> audModeOption; private
   * TVOptionRange<Integer> screenModeOption; private TVOptionRange<Integer> audOnlyOption; private
   * TVOptionRange<Integer> blueMuteOption; private TVConfigurer tvcfg; private ConfigService
   * mmpCfg;
   */
  private int blackmute = -1;

  // TV API
  private final MtkTvVolCtrl mVolCtrl;
  private final MtkTvConfig mConfig;

  private final MtkTvAVMode mtkTvAVMode;

  private final AudioManager mAudManager;

  private boolean isAudio = false;

  private final boolean isM = true;
  private MediaSessionManager mMediaSessionManager = null;

  // ConfigType.CFG_VOLUME --- MtkTvConfigType.CFG_AUD_VOLUME_ALL
  // ConfigType.CFG_PICTURE_MODE ----MtkTvConfigType.CFG_VIDEO_PIC_MODE;
  // ConfigType.CFG_AUDIO_MODE --- delay add. new not found useing.
  // ConfigType.CFG_SCREEN_MODE ---MtkTvConfigType.CFG_VIDEO_SCREEN_MODE;
  // ConfigType.CFG_AUDIO_ONLY

  // ConfigType.CFG_BLUE_SCREEN -- MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE;

  private CommonSet(Context context) {
    mContext = context;
    mAudManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    mMediaSessionManager = (MediaSessionManager) mContext
        .getSystemService(Context.MEDIA_SESSION_SERVICE);
    isAudio = MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_VOLUME_SYNC);
    Log.d(TAG, "CommonSet isAudio = " + isAudio);
    mConfig = MtkTvConfig.getInstance();
    mVolCtrl = MtkTvVolCtrl.getInstance();
    mtkTvAVMode = MtkTvAVMode.getInstance();
    mtkTvUtil = MtkTvUtil.getInstance();
    int configVol = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_AUD_VOLUME_ALL);
    if (isAudio) {
      volumeMax = mAudManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      volumeMin = 0;// default 0
    } else {
      volumeMax = MtkTvConfig.getMaxValue(configVol);
      volumeMin = MtkTvConfig.getMinValue(configVol);
    }

    int configPic = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
    picModeMax = MtkTvConfig.getMaxValue(configPic);
    picModeMin = MtkTvConfig.getMinValue(configPic);

    int configScn = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_VIDEO_SCREEN_MODE);
    scnModeMax = MtkTvConfig.getMaxValue(configScn);
    scnModeMin = MtkTvConfig.getMinValue(configScn);

    /*
     * volumeOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_VOLUME);
     * picModeOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_PICTURE_MODE);
     * audModeOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_AUDIO_MODE);
     * screenModeOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_SCREEN_MODE);
     * audOnlyOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_AUDIO_ONLY);
     * blueMuteOption = (TVOptionRange<Integer>) tvcfg .getOption(ConfigType.CFG_BLUE_SCREEN);
     * if(volumeOption != null){ this.volumeMax = volumeOption.getMax(); this.volumeMin =
     * volumeOption.getMin(); }
     */
  }

  public static CommonSet getInstance(Context context) {
    if (mmpCom == null) {
      synchronized (CommonSet.class) {
        // if (mmpCom == null) {
        mmpCom = new CommonSet(context);
        // }
      }
    }
    return mmpCom;
  }

  /**
   * Get volume value
   * @return
   *
   */
  @Override
  public int getVolume() {
    int volume = 0;

    if (isAudio) {

      volume = mAudManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    } else {

      volume = mConfig.getConfigValue(MtkTvConfigType.CFG_AUD_VOLUME_ALL);

    }

    Log.d(TAG, "getVolume cur volume = " + volume);

    return volume;
  }

  /**
   * Get max volume value
   * @return
   */
  @Override
  public int getMaxVolume() {

    if (volumeMax == -1) {
      if (isAudio) {

        volumeMax = mAudManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      } else {

        int configValue = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_AUD_VOLUME_ALL);
        volumeMax = MtkTvConfig.getMaxValue(configValue);

      }

    }
    Log.d(TAG, "getMaxVolume cur volumeMax = " + volumeMax);

    return volumeMax;
  }

  /**
   * Get min volume value
   * @return
   */
  @Override
  public int getMinVolume() {

    if (volumeMin == -1) {

      if (isAudio) {

        volumeMin = 0;
      } else {
        int configValue = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_AUD_VOLUME_ALL);
        volumeMin = MtkTvConfig.getMinValue(configValue);

      }

    }
    Log.d(TAG, "getMinVolume cur volumeMin = " + volumeMin);
    return volumeMin;
  }

  /**
   * Set volume value
   * @return
   */
  @Override
  public void setVolume(int volume) {
    int curVolume;
    int current = mAudManager.getStreamVolume(AudioManager.STREAM_MUSIC);

    if (volume <= this.volumeMax && volume >= volumeMin) {
      curVolume = volume;
    } else if (volume > volumeMax) {
      curVolume = this.volumeMax;
    } else {
      curVolume = this.volumeMin;

    }

        int direction = 0;
        if(current > curVolume){
            direction = -1;
        } else if (current < curVolume){
            direction = 1;
        }

    Log.d(TAG, "setVolume cur volume = " + curVolume + "isAudio = " + isAudio + "--direction:"
        + direction);

    if (isAudio) {
      if (isM) {
        mAudManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
      } else {
        mMediaSessionManager.dispatchAdjustVolume(
            AudioManager.USE_DEFAULT_STREAM_TYPE,
            direction,
            0);
      }
    } else {
      mConfig.setConfigValue(MtkTvConfigType.CFG_AUD_VOLUME_ALL, curVolume);

    }

  }

  /**
   * Set Mute or UnMute,
   * if the state is mute, call it will unmute, else mute.
   * @return
   */
  @Override
  public void setMute() {

    boolean isMute = isMute();// mVolCtrl.getMute();
    Log.d(TAG, "setMute cur mute = " + isMute);

    if (isAudio) {
      // mAudManager.setStreamMute(AudioManager.STREAM_MUSIC, !isMute);
      mMediaSessionManager.dispatchAdjustVolume(
          AudioManager.USE_DEFAULT_STREAM_TYPE,
          AudioManager.ADJUST_TOGGLE_MUTE,
          AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_VIBRATE);
    } else {
      mVolCtrl.setMute(!isMute);

    }

  }

  /**
   * Check the mute state,
   * Returns: true, mute, false unmute.
   * @return
   */
  @Override
  public boolean isMute() {
    // boolean isMute = mVolCtrl.getMute();
    boolean isMute = mAudManager.isStreamMute(AudioManager.STREAM_MUSIC);

    Log.d(TAG, "isMute cur mute = " + isMute);
    return isMute;
  }

  /**
   * Get picture mode min value.
   * @return
   */
  @Override
  public int getPictureModeMin() {

    // int configValue = mConfig.getMinMaxConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
    Log.d(TAG, "getPictureModeMin  = " + picModeMin);

    return picModeMin;

  }

  /**
   * Get picture mode max value.
   * @return
   */
  @Override
  public int getPictureModeMax() {
    Log.d(TAG, "getPictureModeMax  = " + picModeMax);
    return picModeMax;
  }

  /**
   * Get current picture mode
   * @return
   */
  @Override
  public int getCurPictureMode() {
    int curPicMode = mConfig.getConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE);
    Log.d(TAG, "getCurPictureMode curPicMode =" + curPicMode);
    return curPicMode;
  }

  /**
   * Set picture mode
   * @return
   */
  @Override
  public void setPictureMode(int type) {
    Log.d(TAG, "setPictureMode type =" + type);
    mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_PIC_MODE, type);
  }

  /**
   * Get audio effect min value.
   * @return
   */
  @Override
  public int getAudioEffectMin() {
    return 0;// audModeOption.getMin();
  }

  /**
   * Get audio effect max value.
   * @return
   */
  @Override
  public int getAudioEffectMax() {
    return 0;// audModeOption.getMax();
  }

  /**
   * Get current audio effect value.
   * @return
   */
  @Override
  public int getCurAudioEffect() {
    return 0;// audModeOption.get();
  }

  /**
   * Set current audio effect value.
   * @return
   */
  @Override
  public void setAudioEffect(int type) {
    // audModeOption.set(type);
  }

  /**
   * Get screen mode min value.
   * @return
   */
  @Override
  public int getScreenModeMin() {
    Log.d(TAG, "getScreenModeMin ScnModeMin =" + scnModeMin);
    return scnModeMin;
  }

  /**
   * Get screen mode max value.
   * @return
   */
  @Override
  public int getScreenModeMax() {
    Log.d(TAG, "getScreenModeMax ScnModeMax =" + scnModeMax);
    return scnModeMax;
  }

  /**
   * Get available Screen mode.
   *
   * @return
   * VID_SCREEN_MODE_AUTO
   * VID_SCREEN_MODE_NORMAL
   * VID_SCREEN_MODE_LETTER_BOX
   * VID_SCREEN_MODE_PAN_SCAN
   * VID_SCREEN_MODE_NON_LINEAR_ZOOM
   * VID_SCREEN_MODE_DOT_BY_DOT
  0TV
   *<item>Unknown</item>
  <item>Normal</item>
  <item>Letterbox</item>
  <item>Pan Scan</item>
  <item>User Defined</item>
  <item>Non Linear</item>
  <item>Dot by Dot</item>
  <item>Auto</item>

  MMP

  <item>Auto</item>
  <item>Normal</item>
  <item>Letter box</item>
  <item>Scan</item>
  <item>Zoom</item>
  <item>Dot by Dot</item>
   */
  @Override
  public int[] getAvailableScreenMode() {
    /*
     * int[] scrMode = new int[6]; scrMode[0] = VID_SCREEN_MODE_AUTO; scrMode[1] =
     * VID_SCREEN_MODE_NORMAL; scrMode[2] = VID_SCREEN_MODE_LETTER_BOX; scrMode[3] =
     * VID_SCREEN_MODE_PAN_SCAN; scrMode[4] = VID_SCREEN_MODE_NON_LINEAR_ZOOM; scrMode[5] =
     * VID_SCREEN_MODE_DOT_BY_DOT;
     */
    int tmp[] = new int[6];

    for (int i = 0; i < 6; i++) {
      tmp[i] = -1;
    }

    try {

      int[] allMode = mtkTvAVMode.getAllScreenMode();
      for (int mode : allMode) {

        Log.d(TAG, "getAvailableScreenMode Mode = " + mode);

      }


      for (int i = 0; i < allMode.length; i++) {
        switch (allMode[i]) {
          case VID_SCREEN_MODE_AUTO:
            tmp[0] = 7;
            break;
          case VID_SCREEN_MODE_NORMAL:
            tmp[1] = 1;
            break;
          case VID_SCREEN_MODE_LETTER_BOX:
            tmp[2] = 2;
            break;
          case VID_SCREEN_MODE_PAN_SCAN:
            tmp[3] = 3;
            break;
          case VID_SCREEN_MODE_NON_LINEAR_ZOOM:
            tmp[4] = 5;
            break;
          case VID_SCREEN_MODE_DOT_BY_DOT:
            tmp[5] = 6;
            break;
          default:
            break;

        }
      }

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (int mode : tmp) {

      Log.d(TAG, "getAvailableScreenMode tmp = " + mode);

    }

    return tmp;
  }

  /**
   * Get current Screen mode.
   * @return,VID_SCREEN_MODE_AUTO
   * VID_SCREEN_MODE_NORMAL
   * VID_SCREEN_MODE_LETTER_BOX
   * VID_SCREEN_MODE_PAN_SCAN
   * VID_SCREEN_MODE_NON_LINEAR_ZOOM
   * VID_SCREEN_MODE_DOT_BY_DOT
   */
  @Override
  public int getCurScreenMode() {
    int curScnMode = mConfig.getConfigValue(MtkTvConfigType.CFG_VIDEO_SCREEN_MODE);
    Log.d(TAG, "getCurScreenMode curScnMode = " + curScnMode);

    switch (curScnMode) {
      case MtkTvConfigType.SCREEN_MODE_CUSTOM_DEF_0:
        return VID_SCREEN_MODE_AUTO;

      case MtkTvConfigType.SCREEN_MODE_NORMAL:
        return VID_SCREEN_MODE_NORMAL;

      case MtkTvConfigType.SCREEN_MODE_LETTERBOX:
        return VID_SCREEN_MODE_LETTER_BOX;

      case MtkTvConfigType.SCREEN_MODE_PAN_SCAN:
        return VID_SCREEN_MODE_PAN_SCAN;

      case MtkTvConfigType.SCREEN_MODE_NON_LINEAR_ZOOM:
        return VID_SCREEN_MODE_NON_LINEAR_ZOOM;

      case MtkTvConfigType.SCREEN_MODE_DOT_BY_DOT:
        return VID_SCREEN_MODE_DOT_BY_DOT;

      case MtkTvConfigType.SCREEN_MODE_CUSTOM_DEF_1:
        return VID_SCREEN_MODE_CUSTOM_DEF_1;

      default:
        return VID_SCREEN_MODE_AUTO;
    }
  }

  /**
   * Set screen mode
   * @param: the screen type user want to set .
   */
  @Override
  public void setScreenMode(int type) {

    int mode = MtkTvConfigType.SCREEN_MODE_CUSTOM_DEF_0;

    switch (type) {
      case VID_SCREEN_MODE_AUTO:
        mode = MtkTvConfigType.SCREEN_MODE_CUSTOM_DEF_0;
        break;

      case VID_SCREEN_MODE_NORMAL:
        mode = MtkTvConfigType.SCREEN_MODE_NORMAL;//SCREEN_MODE_CUSTOM_DEF_1
        break;

      case VID_SCREEN_MODE_LETTER_BOX:
        mode = MtkTvConfigType.SCREEN_MODE_LETTERBOX;
        break;

      case VID_SCREEN_MODE_PAN_SCAN:
        mode = MtkTvConfigType.SCREEN_MODE_PAN_SCAN;
        break;

      case VID_SCREEN_MODE_NON_LINEAR_ZOOM:
        mode = MtkTvConfigType.SCREEN_MODE_NON_LINEAR_ZOOM;
        break;

      case VID_SCREEN_MODE_DOT_BY_DOT:
        mode = MtkTvConfigType.SCREEN_MODE_DOT_BY_DOT;
        break;

      default:
        mode = MtkTvConfigType.SCREEN_MODE_CUSTOM_DEF_0;
        break;
    }

    Log.d(TAG, "setScreenMode pram type = " + type + " set mode =" + mode);
    mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_SCREEN_MODE, mode);
  }

  /**
   * Set audio only, tv power-saving state
   * @param, true, OFF, false, ON.
   *
   */
  @Override
  public void setAudOnly(boolean on) {
    if (on == true) {
      mConfig.setConfigValue(MtkTvConfigType.CFG_AUD_AUDIO_ONLY, COMMON_OFF);
    } else {
      mConfig.setConfigValue(MtkTvConfigType.CFG_AUD_AUDIO_ONLY, COMMON_ON);
    }
  }

  /**
   * Get current TV power-saving state
   * @return ture, OFF; false, ON.
   */
  @Override
  public boolean getAudOnly() {

    int audOnly = mConfig.getConfigValue(MtkTvConfigType.CFG_AUD_AUDIO_ONLY);

    if (audOnly == COMMON_OFF) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Free all video resource.
   */
  @Override
  public void mmpFreeVideoResource() {
    blackmute = mConfig.getConfigValue(MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE);
    Log.d(TAG, "mmpFreeVideoResource blackmute = " + blackmute);
    if (blackmute != MtkTvConfigType.COMMON_ON) {
      return;
    }
    mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE, MtkTvConfigType.COMMON_OFF);
  }

  /**
   * Save all video resource.
   */
  @Override
  public void mmpRestoreVideoResource() {
    Log.d(TAG, "mmpRestoreVideoResource blackmute = " + blackmute);
    Util.LogResRelease("mmpRestoreVideoResource");
    if (Util.getPvrPlaying()) {
      // do nothing;
    } else if (blackmute != -1) {
      mConfig.setConfigValue(MtkTvConfigType.CFG_VIDEO_VID_BLUE_MUTE, blackmute);
      blackmute = -1;
    }
  }

  public void setDisplayRegionToFullScreen() {
    Log.d(TAG, "setDisplayRegionToFullScreen = ");
    //Log.d(TAG, "setDisplayRegionToFullScreen = " + Log.getStackTraceString(new Throwable()));
    if (!Util.mIsEnterPip) {
      mtkTvUtil.setScreenSourceRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f, 1.0f, 1.0f));
      mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN, new MtkTvRectangle(0.0f, 0.0f, 1.0f, 1.0f));
    }
  }

  public void setBluetoothOut() {
    mAudManager.setStreamMute(AudioManager.STREAM_VOICE_CALL, true);
    mAudManager.startBluetoothSco();
    mAudManager.setBluetoothScoOn(true);
  }

  public MtkTvVolCtrl.SpeakerType getAudioSpeakerMode() {
    return mVolCtrl.getSpeakerOutMode();
  }

  public void setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType speakerMode) {
    mVolCtrl.setSpeakerOutMode(speakerMode);
  }
}
