
package com.mediatek.wwtv.mediaplayer.mmp.util;

import android.text.TextUtils;
import android.util.Log;

import com.mediatek.twoworlds.tv.MtkTvVolCtrl;

public class DolbylogicManager {
  private static final String TAG = "DolbylogicManager";
  private static DolbylogicManager mInstance;

  public static DolbylogicManager getInstance() {
    if (mInstance == null) {
      mInstance = new DolbylogicManager();
    }
    return mInstance;
  }

  public String getDubiDisplayInfo(String mime) {
    String result = "";
    Log.d(TAG, "getAudioTrackInfo getDubiInfo mime = " + mime);
    if (TextUtils.isEmpty(mime)) {

    } else if (mime.toLowerCase().contains("eac3-dual")
        || mime.toLowerCase().contains("ac3-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getAudioTrackInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "DOLBY AUDIO STEREO";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "DOLBY AUDIO DUAL1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "DOLBY AUDIO DUAL2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase().contains("ac3-surround")) {
      result = "DOLBY AUDIO SURROUND";
    } else if (mime.toLowerCase().contains("ac3-mono")) {
      result = "DOLBY AUDIO MONO";
    } else if (mime.toLowerCase().contains("ac3-stereo")) {
      result = "DOLBY AUDIO STEREO";
    } else if (mime.toLowerCase().contains("heaacv2-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getAudioTrackInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "Heaacv2 Dual Stereo";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "Heaacv2 Dual1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "Heaacv2 Dual2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase().contains("heaacv2")) {
      result = "HEAACV2";
    } else if (mime.toLowerCase().contains("heaac-dual")) {
      MtkTvVolCtrl.SpeakerType speakerType = MtkTvVolCtrl.getInstance().getSpeakerOutMode();
      Log.d(TAG, "getAudioTrackInfo getDubiInfo speakerType = " + speakerType);
      switch (speakerType) {
        case AUDDEC_SPK_MODE_LR:
          result = "Heaac Dual Stereo";
          break;
        case AUDDEC_SPK_MODE_LL:
          result = "Heaac Dual1";
          break;
        case AUDDEC_SPK_MODE_RR:
          result = "Heaac Dual2";
          break;
        default:
          break;
      }
    } else if (mime.toLowerCase().contains("heaac")) {
      result = "HEAAC";
    } else if (mime.toLowerCase().contains("x-dts-surround")) {
      result = "DTS Surround";
    } else if (mime.toLowerCase().contains("x-dts")) {
      result = "DTS Stereo";
    } else if (mime.toLowerCase().contains("mpeg2")) {
      result = "MPEG2";
    } else if (mime.toLowerCase().contains("mpeg")) {
      result = "MP3";
    } else if (mime.toLowerCase().contains("mp4a-latm")) {
      result = "AAC";
    } else if (mime.toLowerCase().contains("x-ms-wmapro")) {
      result = "WMA Pro";
    } else if (mime.toLowerCase().contains("x-ms-wma")) {
      result = "WMA";
    } else if (mime.toLowerCase().contains("x-adpcm-ms")) {
      result = "LPCM";
    } else if (mime.toLowerCase().contains("flac")) {
      result = "FLAC";
    } else if (mime.toLowerCase().contains("vorbis")) {
      result = "Vorbis";
    } else if (mime.toLowerCase().contains("3gpp")) {
      result = "AMR-NB";
    } else if (mime.toLowerCase().contains("amr-wb")) {
      result = "AMR-WB";
    } else if (mime.toLowerCase().contains("ape")) {
      result = "APE";
    } else if (mime.toLowerCase().contains("vnd.rn-realaudio")) {
      result = "COOK";
    } else if (mime.toLowerCase().contains("vnd.dts.hd;profile=lbr")) {
      result = "DTS Express";
    } else if (mime.toLowerCase().contains("vnd.dts.hd")) {
      result = "DTS-HD Master Audio";
    } else if (mime.toLowerCase().contains("vnd.dts")) {
      result = "DTS";
    } else {
      if (mime.contains("audio/")) {
        result = mime.substring(6);
      } else {
        result = mime;
      }
    }
    Log.d(TAG, "getAudioTrackInfo getDubiInfo result =  " + result);
    return result;
  }

  public boolean isDolbyAudio(String mime) {
    if (mime.toLowerCase().contains("ac3")) {
      return true;
    }
    return false;
  }

  public boolean isDolbyDualAudio(String mime) {
    if (mime.toLowerCase().contains("eac3-dual")
        || mime.toLowerCase().contains("ac3-dual")) {
      return true;
    }
    return false;
  }

}
