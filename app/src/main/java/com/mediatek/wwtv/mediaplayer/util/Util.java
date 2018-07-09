/**
 * @Description: TODO()
 *
 */

package com.mediatek.wwtv.mediaplayer.util;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.zip.Adler32;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.device.DevManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvAppTV;

import android.os.SystemProperties;
import android.provider.Settings;

import com.mediatek.wwtv.mediaplayer.jni.PhotoRender;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;

/**
 *
 */
public class Util {

  public interface iDrmlistener {
    public void listenTo(boolean isSure, boolean isContinue, int index);
  }

  public static  String UsingUsbName;
  public static final boolean EXOPLAYER = false;
  public static final String STRICTMODE = "com.mediatek.wwtv.mediaplayer.debug";

  public static final String VOLUMESET = "mtk.intent.volume.status";

  public static final String SOURCEACTION="mtk.intent.input.source";

  public static final String EXO_PLAYER_PROP = "use.exoplayer.in.videoview";

  public static final String TALKBACK_SERVICE = "com.google.android.marvin.talkback/.TalkBackService";

  public static boolean mIsUseEXOPlayer;

  private static boolean isPvrPlaying = false;

  public static boolean mIsEnterPip = false;

  public static boolean mIsMmpFlag;

  public static boolean mIsDolbyVision = false;

  //enter MMP MtkFilesBaseListActivity from other app and not play video
  public static boolean mIsEnterMMPAndNotPlayVideo = false;

  public static void setPvrPlaying(boolean pvr) {
    isPvrPlaying = pvr;
  }

  public static boolean getPvrPlaying() {
    if (isPvrPlaying) {
      isPvrPlaying = false;
      return true;
    }
    return false;
  }

  /*
   * public static String mapKeyCodeToStr(int keyCode) { String _mStr = ""; char _ch; switch
   * (keyCode) { case KeyMap.KEYCODE_0: _ch = '0'; _mStr = "0"; break; case KeyMap.KEYCODE_1: _ch =
   * '1'; _mStr = "1"; break; case KeyMap.KEYCODE_2: _ch = '2'; _mStr = "2"; break; case
   * KeyMap.KEYCODE_3: _ch = '3'; _mStr = "3"; break; case KeyMap.KEYCODE_4: _ch = '4'; _mStr = "4";
   * break; case KeyMap.KEYCODE_5: _ch = '5'; _mStr = "5"; break; case KeyMap.KEYCODE_6: _ch = '6';
   * _mStr = "6"; break; case KeyMap.KEYCODE_7: _ch = '7'; _mStr = "7"; break; case
   * KeyMap.KEYCODE_8: _ch = '8'; _mStr = "8"; break; case KeyMap.KEYCODE_9: _ch = '9'; _mStr = "9";
   * break; default: break; } return _mStr; }
   */

  public static String TAG = "Util";
  public static final boolean PHOTO_4K2K_ON = PhotoRender.is4KPanel();
//  private static Activity mActivity;
//  private static boolean isMMP;

  public static final String ISLISTACTIVITY = "islistactivity";
  public static final String MEDIASETTINGS = "mediasettings";

//  private static Handler mEpgHandler;
//
//  public static void setHandler(Handler handler) {
//    mEpgHandler = handler;
//  }

  static {
    // MtkLog.i("MMPUtil"," ro.mtk.4k2k.photo = "+SystemProperties.getInt("ro.mtk.4k2k.photo",0));
    // if(0!=PhotoRender.is4KPanel() ){
    // PHOTO_4K2K_ON = PhotoRender.is4KPanel();
    // }
    MtkLog.i("MMPUtil", "is4K2K:" + PHOTO_4K2K_ON);

  }

  public static boolean isUseExoPlayer() {
    Log.i(TAG, "isUseExoPlayer mIsUseEXOPlayer:" + mIsUseEXOPlayer);
    return mIsUseEXOPlayer;
  }

//  public static boolean startEPGActivity(Activity actvity) {
//    if (mEpgHandler == null) {
//      return false;
//    }
//    mActivity = actvity;
//    boolean success = false;
//    /*
//     * if
//     * (CommonIntegration.getInstanceWithContext(mActivity.getApplicationContext())
//     * .isCurrentSourceTv
//     * () ) { if (CommonIntegration.getInstance().isMenuInputTvBlock()) { if
//     * (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US) { success = true; }
//     * } else { if (MarketRegionInfo.getCurrentMarketRegion() != MarketRegionInfo.REGION_US &&
//     * CommonIntegration.getInstance().getAllEPGChannelLength() <= 0) { success = true; } } String
//     * country = MtkTvConfig.getInstance().getCountry(); if
//     * (MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_OCEANIA) &&
//     * country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NZL)) { success = true; } } else {
//     * success = true; } if (!success) { LogicManager.getInstance(mActivity).restoreVideoResource();
//     * LogicManager.getInstance(mActivity).finishAudioService();
//     * MultiFilesManager.getInstance(mActivity).destroy(); ((MmpApp)
//     * (mActivity).getApplication()).finishAll(); MtkFilesBaseListActivity.reSetModel();
//     * mHandler.sendEmptyMessageDelayed(MeidaMainActivity.MSG_START_EPG_DELAY, 2000); }
//     */
//    return success;
//  }

  public static void exitMmpActivity(Context context) {
    if (((MmpApp) (context).getApplicationContext()).isFirstFinishAll()) {
      LogicManager.getInstance(context).stopAudio();
      if (VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
      } else {
        LogicManager.getInstance(context).finishVideo();
        if (Thumbnail.getInstance() != null) {
          Thumbnail.getInstance().setRestRigionFlag(false);
        }
      }
      DevManager.getInstance().destroy();
      if (MultiFilesManager.hasInstance()) {
        MultiFilesManager.getInstance(context).destroy();
      }
      BitmapCache.createCache(true);
      com.mediatek.wwtv.mediaplayer.mmp.gamekit.util.BitmapCache.createCache(true);
      if (VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {

      } else {
        //remove for DTV00892498
        //MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 0);
      }
      LogicManager.getInstance(context).restoreVideoResource();
      ((MmpApp) (context).getApplicationContext()).setEnterMMP(false);
      ((MmpApp) (context).getApplicationContext()).finishAll();
    } else {
      new Exception().printStackTrace();
    }
  }

  public static void exitPIP(Context context) {
    //noral case, should close other android PIP.
    String package_name = context.getPackageName();
    MtkLog.d(TAG, "send broadcast exit pip in util package_name:" + package_name
        + "  " + GetCurrentTask.getInstance(context).getCurRunningClassName());
    if (package_name == null || package_name.equals("")) {
      package_name = "com.mediatek.wwtv.mediaplayer";
    }
    Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
    intent.putExtra(Intent.EXTRA_PACKAGES,
        new String[]{package_name});
    intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE,
        Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
    context.sendBroadcastAsUser(intent,
          new UserHandle(ActivityManager.getCurrentUser()),
          android.Manifest.permission.RECEIVE_MEDIA_RESOURCE_USAGE);
  }

//  public static void setMMPFlag(boolean flag) {
//    isMMP = flag;
//  }
//
//  public static boolean getMMPFlag() {
//    return isMMP;
//  }

  public static void reset3D(Context context) {
    Log.i("UTIL", "reset3d");
    MenuConfigManager.getInstance(context).setValue(MenuConfigManager.VIDEO_3D_MODE, 0, null);
  }

  static boolean isEndPhotoPlay = true;

  /*
   * if 4k2kactivity pause,no need to end play, you need to set false;
   */
  public static void setEndPhotoPlayWhenPause(boolean isNeedEndPhotoPlay) {
    isEndPhotoPlay = isNeedEndPhotoPlay;
  }

  public static boolean isNeedEndPhotoPlayWhenPause() {
    return isEndPhotoPlay;
  }

  public static void onStop(Context context) {
    if (!isMMpActivity(context)) {
      exitMmpActivity(context);
    }
  }

  public static boolean isMMpActivity(Context context) {
    String topClassName = GetCurrentTask.getInstance(context)
        .getCurRunningPackageName();
    boolean isMmpActiity = false;
    String packageName = context.getPackageName();
    if (topClassName != null && topClassName.startsWith(packageName)) {
      isMmpActiity = true;
    }
    Log.i(TAG, "packageName:" + packageName
        + "--topClassName:" + topClassName + "--isMmpActiity:" + isMmpActiity);
    return isMmpActiity;

  }

  public static boolean isGridActivity(Context context) {
    String topClassName = GetCurrentTask.getInstance(context)
        .getCurRunningClassName();
    MtkLog.i(TAG, "isGridActivity:" + topClassName);
    boolean is = false;
    if (topClassName != null
        && topClassName
           .equalsIgnoreCase("com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesGridActivity")) {
      is = true;
    }
    return is;
  }

  public static void LogResRelease(String res) {
    new Exception().printStackTrace();
    Log.i("RESOURCE", "---" + res);
  }

  public static void LogListener(String res) {
    MtkLog.i("LISTENER", "---" + res);
  }

  public static void LogLife(String TAG, String info) {
    MtkLog.i(TAG, "LogLife---" + info);
  }

  private static long getHashValue(Bitmap bitmap) {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
    byte[] arraystream = stream.toByteArray();
    Adler32 hash = new Adler32();
    hash.update(arraystream);
    return hash.getValue();
  }

  private static long getPixal(Bitmap bitmap) {
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    long checksum = 0;
    int a = 0, r = 0, g = 0, b = 0;
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        int pixal = bitmap.getPixel(i, j);
        a = (pixal & 0xff);
        r = ((pixal >> 8) & 0xff);
        g = ((pixal >> 16) & 0xff);
        b = ((pixal >> 24) & 0xff);
        checksum += (a + r + g + b);
      }

    }
    return checksum;
  }

  static HashMap hm = new HashMap() {
    {
      put(".bmp", 1);
      put(".gif", 2);
      put(".icon", 3);
      put(".jpg", 4);
      put(".png", 5);
      put(".wbmp", 6);
      put(".webp", 7);
    }
  };
  public static final String AUTO_TEST_PROPERTY = "mtk.auto_test";

  public static void printAutoTestImage(String str, Bitmap bitmap) {
    if (0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)
        || MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      int length = str.length();
      int start = str.lastIndexOf(".") + 1;
      if (start < length && bitmap != null) {
        Log.i(
            "AUTO_TEST",
            "image_format:" + str.substring(start) + " checkSum: 0x"
                + Long.toHexString(getPixal(bitmap)));
        // +"--checkHash:0x"+Long.toHexString(getHashValue(bitmap)));
      } else {
        if (bitmap == null) {
          Log.i("AUTO_TEST", "bitmap is null fail ");
        } else {
          Log.i("AUTO_TEST", "bitmap is null can't get suffix fail");
        }

      }
    }
  }

  public static void printAutoTestImageResult(String result) {
    if (0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)
        || MediaMainActivity.mIsDlnaAutoTest) {
      Log.i("AUTO_TEST", " play result: " + result);
    }
  }

  //
  public static void printAutoTestImage3D(String str, String process) {
    if (0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)
        || MediaMainActivity.mIsDlnaAutoTest) {
      if (str != null) {
        int length = str.length();
        int start = str.lastIndexOf(".") + 1;
        if (start < length) {
          Log.i(
              "AUTO_TEST",
              "image_format:" + str.substring(start) + " file:"
                  + str.substring(str.lastIndexOf("/") + 1) + " checkSum: " + process);
        } else {
          Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
        }
      } else {
        Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
      }
    }
  }

  public static void printAutoTestImageGif(String str, String process) {
    if (0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0)
        || MediaMainActivity.mIsDlnaAutoTest) {

      int length = str.length();
      int start = str.lastIndexOf(".") + 1;

      if (str != null) {
        if (start < length) {
          Log.i(
              "AUTO_TEST",
              "image_format:" + str.substring(start) + " file:"
                  + str.substring(str.lastIndexOf("/") + 1) + " checkSum:" + process);
        } else {
          Log.i("AUTO_TEST", "image_format:" + str + " checkSum: " + process);
        }
      } else {
        Log.i("AUTO_TEST", "image_format:" + str.substring(start) + " checkSum: " + process);
      }
    }
  }

  public static void enterMmp(int status, Context context) {
    // TODO Auto-generated method stub
    new Exception().printStackTrace();
    MtkLog.i(TAG, "enterMmp before status:" + status);
    //if (status == 0 && VideoPlayActivity.getInstance() != null
    //    && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
//
    //} else {
    //  MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, status);
    //}
    //int cur = LogicManager.getInstance(context).getCurPictureMode();
    //Log.i(TAG, "cur:--" + cur);
    //LogicManager.getInstance(context).setPictureMode(cur);

    if (1 == status) {
      MtkLog.i(TAG, "enterMmp 1 == status ");
      MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    }
    MtkLog.i(TAG, "enterMmp after status: " + status);
  }

  public static boolean isTTSEnabled(Context context) {
      AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
      List<AccessibilityServiceInfo> enableServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
      for (int i = 0; i < enableServices.size(); i++) {
          if (enableServices.get(i).getId().contains(TALKBACK_SERVICE)) {
              return true;
          }
      }

      return false;
  }
  
  /**
   * Check current is TTS Enable or disable status.
   * @return true when current TTS is enable from Launcher Settings.
   * @return false when disable TTS.
   */
   public static boolean isTTSEnable(Context context) {
       AccessibilityManager mAccm = AccessibilityManager.getInstance(context);
       boolean accessibilityEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 0) == 1;
       Log.d(TAG, "accessibilityEnabled = "+accessibilityEnabled);

       boolean talkBackEnabled = mAccm.isEnabled() && mAccm.isTouchExplorationEnabled();
       Log.d(TAG, "talkBackEnabled = "+talkBackEnabled);
       return talkBackEnabled;
   }

  public static void showToast(Context context, String msg) {
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
  }


  public static boolean isDolbyVision(Context context){
      int cur = TVContent.getInstance(context).getConfigValue(MenuConfigManager.PICTURE_MODE);
      MtkLog.d(TAG, "isDolbyVision cur: " + cur);
      if (5 == cur || 6 == cur){
          mIsDolbyVision = true;
      } else {
          mIsDolbyVision = false;
      }

      return mIsDolbyVision;
  }


  public static void showDoViToast(Context context) {
      MtkLog.d(TAG, "MMP showDoViToast");
      Intent intent = new Intent("mtk.intent.action.dolby.version");
      context.sendBroadcast(intent);
  }

    public static void printStackTrace() {
        if (true) {
        	Throwable tr = new Throwable();
        	Log.getStackTraceString(tr);
        	tr.printStackTrace();
        }
    }
}
