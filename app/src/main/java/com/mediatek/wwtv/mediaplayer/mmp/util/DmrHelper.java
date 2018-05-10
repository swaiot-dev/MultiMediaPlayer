
package com.mediatek.wwtv.mediaplayer.mmp.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import com.mediatek.dlna.*;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.dlna.object.ContentType;
import com.mediatek.dlna.object.Content;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.util.Util;

import com.mediatek.mmp.*;

//import com.mediatek.ui.mmp.multimedia.PhotoPlayActivity;
//import com.mediatek.ui.mmp.multimedia.PhotoPlayDmrActivity;
//import com.mediatek.ui.mmp.multimedia.VideoPlayActivity;
//import com.mediatek.ui.mmp.multimedia.VideoPlayDmrActivity;

public class DmrHelper {
  public static Content mDmrObject = null;
  public static iDmrListener mListener = null;
  public static Handler mHandler = null;
  public static boolean mDmr = false;
  public static Context mContext;

  public static String TAG = "DmrHelper";

  public static final int DLNA_DMR_MEDIAINFO = 0; // Param1(DLNA_DMR_MEDIA_LIST_T *pt_playlist)
  public static final int DLNA_DMR_PLAY = 20; // Param1(E_DMR_PLAYER_PLAYSPEED t_playspeed)
  public static final int DLNA_DMR_STOP = 21;
  public static final int DLNA_DMR_PAUSE = 22;
  public static final int DLNA_DMR_SEEKTIME = 23; // Param1(UINT32 ui4_seconds)
  public static final int DLNA_DMR_GET_VOLUME = 40; // Param1(UINT32 ui4_volume)
  public static final int DLNA_DMR_SET_VOLUME = 41; // Param1(UINT32 ui4_volume)
  public static final int DLNA_DMR_GET_MUTE = 42; // Param1(BOOL b_mute)
  public static final int DLNA_DMR_SET_MUTE = 43; // Param1(BOOL b_mute)
  public static final int DLNA_DMR_GET_PROGRESS = 44;
  public static final int DLNA_DMR_STOPOLDE_STARTNEW = 45;

  public static final int DLNA_DMR_AUDIO = 0;
  public static final int DLNA_DMR_VIDEO = 1;
  public static final int DLNA_DMR_PHOTO = 2;
  public static final int DLNA_DMR_UNVAILD = -1;
  public static final String audioItem = "audioItem";
  public static final String videoItem = "videoItem";
  public static final String imageItem = "imageItem";

  public static final String audioProtocal = "http-get:*:audio";
  public static final String videoProtocal = "http-get:*:video";
  public static final String imageProtocal = "http-get:*:image";

  public static final String DMR_KEY = "dmr_key";

  public static DmcEventListener mDmcListener = new DmcEventListener() {

    @Override
    public void notifyDmcEvent(FoundDMCEvent event) {
      Log.i(TAG, "notify event");
      parse(mContext, event);
      ;
    }
  };

  public final static String DMRSOURCE = "isDmr";

  public static boolean isNetworkConnect(Context context) {
    if (null != context) {
      ConnectivityManager cm = (ConnectivityManager) context
          .getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo mNetworkInfo = cm.getActiveNetworkInfo();
      if (null != mNetworkInfo) {
        return mNetworkInfo.isAvailable();
      }
    }
    return false;

  }

  // open dmr
  public static void openDmr(Context context) {
     if (Util.isUseExoPlayer())
     {
         DLNAStreamLoadable.setLoadableFlag(DLNAStreamLoadable.Loadable_FLG_DMR);
     }
    // if menu dmr on
    // network
    if (isNetworkConnect(context)) {
      Log.i(TAG, " network  available to start dmr");
      mContext = context;
      DigitalMediaRenderer.getInstance().setDmcEventListener(mDmcListener);
      // to modify dmrname to deviceName according to different device
//      int resultOfSetname = DigitalMediaRenderer.getInstance()
//          .SetFriendlyName(getDeviceName(context));
      int resultOfStart = DigitalMediaRenderer.getInstance().start();
//      Log.d(TAG, "openDmr resultOfSetname:" + resultOfSetname + "  resultOfStart:" + resultOfStart);
    } else {
      Log.i(TAG, " network not available");
    }
    /**/
  }

  public static void init(Context context) {
    boolean isMute = LogicManager.getInstance(context).isMute();
    int maxVolume = LogicManager.getInstance(context).getMaxVolume();
    Log.i(TAG, "maxVolume:" + maxVolume);
    int currentVolume = LogicManager.getInstance(context).getVolume();
    Log.i(TAG, "maxVolume:" + maxVolume + "---currentVolume:" + currentVolume + "---isMute:"
        + isMute);
    int mute = 0;
    if (isMute) {
      mute = 1;
    }
    notifyVolume(context, currentVolume * 100 / maxVolume, mute);

  }

  // mute == 1, mute,mute==0,not mute
  public static void notifyVolume(Context context, int volume, int mute) {
    Log.i(TAG, "notifyVolume volume:" + volume + " mute:" + mute);
    DigitalMediaRenderer.getInstance().NotifyRenderStatus(volume, mute);
  }

//  public static String getDeviceName(Context context) {
//    String deviceName = Settings.Global.getString(context.getContentResolver(),
//        Settings.Global.DEVICE_NAME);
//    Log.d(TAG, "deviceName deviceName:" + deviceName);
//    return deviceName;
//  }

  public static void handleStart() {
    // TODO Auto-generated method stub
    Log.d(TAG, "handleStart:");
    setBoolean(true);
  }

  public static void handleStop() {
    // TODO Auto-generated method stub
    // new Exception().printStackTrace();
    Log.d(TAG, "handleStop:");
    tellDmcState(null, 2);
    setBoolean(false);
    setHandler(null);
    setListener(null);
  }

  // close dmr
  public static void closeDmr(Context context) {

    if (Util.isUseExoPlayer())
    {
        DLNAStreamLoadable.setLoadableFlag(DLNAStreamLoadable.Loadable_FLG_BASE);
    }
    // if menu dmr on
    DigitalMediaRenderer.getInstance().setDmcEventListener(null);
    int result = DigitalMediaRenderer.getInstance().stop();
    Log.d(TAG, "closeDmr result:" + result);
    handleStop();
  }

  /*  */

  public static void setObject(Content object) {
    Log.i(TAG, "setObject object:" + object);
    mDmrObject = object;
  }

  public static Content getObject() {
    return mDmrObject;
  }

  public static void setListener(iDmrListener listener) {
    mListener = listener;
  }

  public static void setHandler(Handler handler) {
    mHandler = handler;
  }

  public static iDmrListener getListener() {
    return mListener;
  }

  /*
   * dispatch dmc-event to mediaplayer by listener
   */
  public static void notifyEvent(int action) {
    Log.i(TAG, "notifyEvent--action:" + action);
    if (!isDmr()) {
      Message msg = new Message();
      msg.what = 1002;
      msg.arg1 = DmrHelper.DLNA_DMR_STOPOLDE_STARTNEW;
      mHandler.sendMessage(msg);
      return;
    }
    if (null != mListener) {
      Log.i(TAG, "notifyEvent-null != mListener");
      if (action == DmrHelper.DLNA_DMR_GET_PROGRESS) {
      } else {
        mListener.notifyNewEvent(action);
      }

    } else {
      Log.i(TAG, "notifyEvent-null == mListener");
    }
  }

  public static void notifyEventWithParam(int action, int param) {
    if (null != mListener) {
      mListener.notifyNewEventWithParam(action, param);
    }
  }

  public static void setBoolean(Boolean isDmr) {
    Log.i(TAG, "setBoolean isDmr:" + isDmr);
    mDmr = isDmr;

  }

  public static String getUrl() {
    String url = "";
    if (mDmrObject != null) {
      url = mDmrObject.getResUri();
      Log.d(TAG, "getUrl title:" + mDmrObject.getTitle()
          + "  " + mDmrObject.getPath());
    }
    Log.d(TAG, "getUrl url:" + url);
    return url;
  }

  public static String getFileTitle(boolean addType) {
    String title = "";
    if (mDmrObject != null) {
      title = mDmrObject.getTitle();
      if (addType) {
        String mimeType = mDmrObject.getMimeType();
        String suffix = "";
        if (mimeType == null) {

        } else if (mimeType.startsWith(".")) {
          if (mimeType.length() == 1) {
            suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_UNKONWN;
          } else {
            suffix = mimeType;
          }
        }
        /* MUSIC */
        else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_MPEG)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MP3;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_3GPP)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_3PG;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_WAV)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_WAV;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_X_MS_WMA)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_WMA;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_X_SONY_OMA)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_OMA;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_L16)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_PCM;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_MP4)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MP4;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_VND_DLNA_ADTS)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_AAC;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_AUDIO_VND_DOLBY_DD_RAW)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_AC3;
        }
        /* IMAGE */
        else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_IMAGE_BMP)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_BMP;
        } else if (mimeType.equalsIgnoreCase(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_IMAGE_GIF)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_GIF;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_IMAGE_JPEG)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_JPG;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_IMAGE_PNG)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_PNG;
        }
        /* VIDEO */
        else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_MPEG)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MPG;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_VND_DLNA_MPEG_TTS)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_TTS;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_X_MS_ASF)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_ASF;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_X_MS_WMV)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_WMV;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_X_MS_AVI) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_AVI) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_DIVX) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_DIVX_EXT) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_H263) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_X_MS_VIDEO) ||
            mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_H264)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_AVI;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_MP4)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MP4;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_QK_TE_MOV)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MOV;
        } else if (mimeType.equals(FileSuffixConst.DLNA_MEDIA_MIME_TYPE_VIDEO_X_MATROSSKA_MKV)) {
          suffix = FileSuffixConst.DLNA_FILE_NAME_EXT_MKV;
        }
        title = title + suffix;
//        if (mimeType != null && mimeType.contains("/")) {
//          String [] mimeArray = mimeType.split("/");
//          if (mimeArray.length > 0) {
//            title = title + "." + mimeArray[mimeArray.length - 1];
//          }
//        }
      }
    }
    Log.d(TAG, "getFileTitle title:" + title + "  " + mDmrObject.getPath()
        + "  :" + mDmrObject.getMediaType());
    return title;
  }

  public static String getType() {

    String type = "";
    if (mDmrObject != null) {
      type = "testtype";// mDmrObject.get_upnp_class();
    } else {
      Log.i(TAG, "mDmrObject == null");
    }
    Log.d(TAG, "getType type:" + type);
    return type;
  }

  public static String getProtocolinfo() {

    String info = "";
    if (mDmrObject != null) {
      info = mDmrObject.getProtocolInfo();
    } else {
      Log.i(TAG, "mDmrObject == null");
    }
    Log.d(TAG, "getProtocolinfo info:" + info);
    return info;
  }

  public static boolean isDmr() {
    Log.i(TAG, "isDmr:" + mDmr);
    return mDmr;
  }

  public static int getTopActivityType() {
    int type = 5;
    return type;
  }

  public static final int DLNA_DMR_PLAYED = 0;
  public static final int DLNA_DMR_PAUSED = 1;
  public static final int DLNA_DMR_STOPPED = 2;
  public static final int DLNA_DMR_NFY_MUTE = 3;
  public static final int DLNA_DMR_NFY_VOLUME = 4;
  public static final int DLNA_DMR_PLAYLIST_STOPPED = 5;
  public static final int DLNA_DMR_NFY_INTERNAL_SUBTITLE = 6;

  public static void tellDmcState(Context context, int state) {
    if (isDmr() && null != DigitalMediaRenderer.getInstance()) {
      Log.i(TAG, "tellDmcState: state:" + state);
      DigitalMediaRenderer.getInstance().NotifyStatus(state);
    }
  }

  public static void tellDmcProgressState(long duration, long progress) {
    if (isDmr() && null != DigitalMediaRenderer.getInstance()) {
      Log.d(TAG, "tellDmcProgressState duration:" + duration + "  progress:" + progress);
      DigitalMediaRenderer.getInstance().NotifyProgressStatus(duration, progress);
    }
  }

  static boolean isPlayed = false;

  public static void parse(Context context, FoundDMCEvent event) {
    if (null != event) {
      DmrDevice dmrdevice = event.getdmcobj();
      if (dmrdevice != null) {
        int cmd = dmrdevice.getcmd();
        Log.i(TAG, "parse cmd:" + cmd);
        switch (cmd) {
          case DmrHelper.DLNA_DMR_MEDIAINFO:
            Log.d(TAG, "parse DLNA_DMR_MEDIAINFO");
            String topClassName = GetCurrentTask.getInstance(context).getCurRunningClassName();
            Log.i(TAG, "parse topClassName:" + topClassName);
            if (topClassName.equalsIgnoreCase("com.mediatek.wwtv.mediaplayer.mmp.DmrActivity")) {
              isPlayed = false;
              if (dmrdevice instanceof Dmc) {
                DmrHelper.setObject(((Dmc) dmrdevice).getContentObject());
                Log.i(TAG, "parse url:" + getUrl());
              }
            }
            break;
          case DmrHelper.DLNA_DMR_PLAY:
            Log.d(TAG, "DLNA_DMR_PLAY");
            int nowActivity = DmrHelper.getTopActivityType();
            if (nowActivity == 5) {
              handlePlay(context, false);
            } else {
              if (nowActivity == 11 || nowActivity == 22 || nowActivity == 33 || nowActivity == 44) {
                return;
              }
              DmrHelper.notifyEvent(DmrHelper.DLNA_DMR_STOPOLDE_STARTNEW);
            }
            break;
          case DmrHelper.DLNA_DMR_STOP:
            Log.d(TAG, "DLNA_DMR_STOP");
            DmrHelper.notifyEvent(DmrHelper.DLNA_DMR_STOP);
            break;
          case DmrHelper.DLNA_DMR_PAUSE:
            Log.d(TAG, "DLNA_DMR_PAUSE");
            DmrHelper.notifyEvent(DmrHelper.DLNA_DMR_PAUSE);
            break;
          case DmrHelper.DLNA_DMR_SEEKTIME:
            int param = dmrdevice.getparam();
            Log.d(TAG, "DLNA_DMR_SEEKTIME param:" + param);
            DmrHelper.notifyEventWithParam(DmrHelper.DLNA_DMR_SEEKTIME, param);
            break;
          case DmrHelper.DLNA_DMR_GET_VOLUME:
            Log.d(TAG, "DLNA_DMR_GET_VOLUME");
            break;
          case DmrHelper.DLNA_DMR_SET_VOLUME:
            int volume = dmrdevice.getparam();
            Log.i(TAG, "DLNA_DMR_SET_VOLUME mrdevice.getparam() volume:" + volume);
            DmrHelper.notifyEventWithParam(DmrHelper.DLNA_DMR_SET_VOLUME, volume);
            break;
          case DmrHelper.DLNA_DMR_GET_MUTE:
            Log.d(TAG, "DLNA_DMR_GET_MUTE");
            break;
          case DmrHelper.DLNA_DMR_SET_MUTE:
            int mute = dmrdevice.getparam();
            Log.d(TAG, "DLNA_DMR_SET_MUTE mute:" + mute);
            DmrHelper.notifyEventWithParam(DmrHelper.DLNA_DMR_SET_MUTE, mute);
            break;
          case DmrHelper.DLNA_DMR_GET_PROGRESS:
            Log.d(TAG, "DLNA_DMR_GET_PROGRESS");
            break;
        }
      } else {
        Log.i(TAG, "dmr parse device == null");
      }
    } else {
      Log.i(TAG, "dmr parse event == null");
    }

  }

  public static void handlePlay(Context context, boolean fromlist) {
    // TODO Auto-generated method stub

    // new Exception().printStackTrace();
    if (!DmrHelper.isDmr() || fromlist == true) {
      DmrHelper.handleStart();
      int type = getType(context);
      Log.d(TAG, "handlePlay type:" + type);

      // switch(getType(context)){
      // case DmrHelper.DLNA_DMR_AUDIO:
      // startMusicActivity(context,fromlist);
      // break;
      // case DmrHelper.DLNA_DMR_VIDEO:
      // startVideoActivity(context,fromlist);
      // break;
      // case DmrHelper.DLNA_DMR_PHOTO:
      // startPhotoActivity(context,fromlist);
      // break;
      // }
      if (mStartListener != null) {
        mStartListener.notifyStartActivity(type);
      }
    } else {
      Log.i(TAG, "handlePlay:" + DmrHelper.DLNA_DMR_PLAY);
      DmrHelper.notifyEvent(DmrHelper.DLNA_DMR_PLAY);
    }

  }

  /*
   * to check current url which type of media
   */
  private static int getType(Context context) {
    int type = DmrHelper.DLNA_DMR_UNVAILD;

    ContentType ctype = mDmrObject.getType();
    Log.i(TAG, "ctype:" + ctype);
    switch (ctype) {
      case Video:
        type = DmrHelper.DLNA_DMR_VIDEO;
        break;
      case Audio:
        type = DmrHelper.DLNA_DMR_AUDIO;
        break;
      case Photo:
        type = DmrHelper.DLNA_DMR_PHOTO;
        break;
    }
    return type;
  }

  private static void startMusicActivity(Context context, boolean fromlist) {
    Intent intent = new Intent("android.mtk.intent.action.mmp.music");
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    if (fromlist == false) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  private static void startVideoActivity(Context context, boolean fromlist) {
    Intent intent = new Intent("android.mtk.intent.action.mmp.video");
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    if (fromlist == false) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  private static void startPhotoActivity(Context context, boolean fromlist) {
    Intent intent = null;
    if (Util.PHOTO_4K2K_ON) {
      intent = new Intent("android.mtk.intent.action.mmp.4k2kphoto");
    } else {
      intent = new Intent("android.mtk.intent.action.mmp.photo");
    }
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    if (fromlist == false) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    context.startActivity(intent);
  }

  private static iStartListener mStartListener = null;

  public static void setStartListener(iStartListener listener) {
    mStartListener = listener;
  }

  public interface iStartListener {
    void notifyStartActivity(int type);

  }

}
