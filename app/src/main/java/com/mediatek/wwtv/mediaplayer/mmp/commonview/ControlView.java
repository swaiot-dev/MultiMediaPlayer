
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo3DPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.DolbylogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmp.model.*;

import com.mediatek.twoworlds.tv.MtkTvVolCtrl;

/**
 * {@link PopupWindow}
 *
 * @author hs_weihuiluo
 *
 */
public class ControlView extends PopupWindow {

  private static final String TAG = "ControlView";
  
  private String mDolbyStr="DOLBY AUDIO";

  private Context mContext;

  private View vControlView;

  private ImageView vPStatePlay;

  private ImageView vPStatePause;

  private TextView vVideoSpeed;

  private TextView vPMediaType;

  private TextView vPZoomSize;

  private TextView vPRepeatTv;

  private ImageView vPShuffle;

  private TextView vPTimeLong;

  private TextView vPOrder;

  private TextView vPView;

  private TextView vPFileName;

  private TextView vMStartTime;

  private TextView vMEndTime;

  private ProgressBar vMProgressBar;

  private TextView vMVolumeTv;

  private ProgressBar vMVolumeBar;

  private TextView mVideoTrackNumber;

    private TextView mDubiIcon;

  private ImageView mRepeatLogo;

  private TextView mVideoSubtitle;

  private ImageView mVideoSubtitleIcon;

  private LinearLayout mVolumeProgressBg;

  private ControlPlayState mControlImp;

  private Drawable repeatAll;

  private Drawable repeatOne;

  private LogicManager mLogicManager;

  private RelativeLayout mPlayPauseLayout;
  private RelativeLayout mVideoLayout;
  private LinearLayout mControlbottom;

  private short subtitleIndex = -1;
  private short audioTrackIndex;

  // Added by Dan for fix bug DTV00376577
  private boolean mIsPlaying = true;

  private int mediaType;
  private String strOff;
  private int type;
    private int mDubiIconWidth;
    private int mDubiIconHeight;

 private String sttl = "STTL";//modified by yangxiong for solving "sa and na sttl need to change cc"
 
  public interface ControlPlayState {
    void play();

    void pause();
  }

  public ControlView(View contentView) {
    super(contentView);
  }
   //fix by tjs for add 
  public ControlView(View contentView, int width,int height) {
    super(contentView, width, height);
  }
 //fix by tjs for add 
  public ControlView(Context context, int mediatype,
      ControlPlayState statecontrol, View contentView, int width,
      int height) {
    super(contentView, width, height);
    mediaType = mediatype;
    vControlView = contentView;
    mControlImp = statecontrol;
    mContext = context;
    mLogicManager = LogicManager.getInstance(context);

    findCommonView();
    initDrawable();
    switchView(mediatype);
    strOff = mContext.getResources().getString(R.string.mmp_lable_subtitle_off);
    setOnDismissListener(mDismissListener);
    //begin=> modified by yangxiong for solving "sa and na sttl need to change cc"
      TVContent tvContent = TVContent.getInstance(context);
      if (tvContent.isSARegion() || tvContent.isUSRegion()){
          sttl = "CC";
      }else {
          sttl ="STTL";
      }
      //end=> modified by yangxiong for solving "sa and na sttl need to change cc"
  }

  // add by shuming for fix CR: DTV00407914

  private void setTopLayoutVisible(int visibility) {
    if (mVideoLayout != null) {
      mVideoLayout.setVisibility(visibility);
    } else {
      setPlayPauseLayoutVisible(visibility);
    }
  }

  private void setPlayPauseLayoutVisible(int visibility) {
    if (mPlayPauseLayout != null) {
      mPlayPauseLayout.setVisibility(visibility);
    }
  }

  private void setControlBottomLayoutVisible(int visibility) {
    if (mControlbottom != null) {
      mControlbottom.setVisibility(visibility);
    }
  }

   public void setControlbottomHide(int visibility){
        setControlBottomLayoutVisible(visibility);
    }
  public void hiddlen(int visibility) {
    // new Exception().printStackTrace();
    setTopLayoutVisible(visibility);
    setControlBottomLayoutVisible(visibility);
    affectRepeatLogo(visibility);
  }

  private void affectRepeatLogo(int visibility) {
    if (visibility == View.VISIBLE) {
      if (null != mRepeatLogo
          && !(mRepeatLogo.getVisibility() == View.VISIBLE)
          && (mRepeat != ABRpeatType.ABREPEAT_TYPE_NONE
          && mRepeat != ABRpeatType.ABREPEAT_TYPE_CANCEL_ALL)) {
        setRepeatLogoVisible(View.VISIBLE);
      }
    } else {
      if (null != mRepeatLogo) {
        setRepeatLogoVisible(View.GONE);
      }
    }
  }

  private void setRepeatLogoVisible(int visibility) {
    if (null != mRepeatLogo) {
      mRepeatLogo.setVisibility(visibility);
    }
  }
	public boolean isBottomVisible(){
		return mControlbottom.getVisibility() == View.VISIBLE ? true : false;
	}

  public boolean isShowed() {
    int isShowed = 0;
    if (vControlView != null) {
      MtkLog.i(TAG, "isShowed vControlView != null");
      if (mControlbottom != null && mControlbottom.getVisibility() == View.VISIBLE) {
        isShowed += 1;
      }
      if (mVideoLayout != null) {
        if (mVideoLayout.getVisibility() == View.VISIBLE) {
          isShowed += 4;
        }
      } else {
        if (mPlayPauseLayout != null && mPlayPauseLayout.getVisibility() == View.VISIBLE) {
          isShowed += 2;
        }
      }
    }
    MtkLog.i(TAG, "isShowing:" + isShowed);
    if (isShowed > 0) {
      return true;
    } else {
      return false;
    }
  }

  private final OnDismissListener mDismissListener = new OnDismissListener() {

    @Override
    public void onDismiss() {
      // vControlView = null;
      // mContext = null;
    }
  };

  public void initDrawable() {
    repeatOne = mContext.getResources().getDrawable(
        R.drawable.mmp_toolbar_icon_repeat_one);
    repeatOne.setBounds(0, 0, repeatOne.getMinimumWidth(), repeatOne
        .getMinimumHeight());
    repeatAll = mContext.getResources().getDrawable(
        R.drawable.mmp_toolbar_typeicon_repeat);
    repeatAll.setBounds(0, 0, repeatOne.getMinimumWidth(), repeatOne
        .getMinimumHeight());

        mDubiIconWidth = (int)mContext.getResources().getDimension(R.dimen.dubi_icon_width);
        mDubiIconHeight = (int)mContext.getResources().getDimension(R.dimen.dubi_icon_height);
  }

  private void switchView(int mediatype) {
    if (mediatype == MultiMediaConstant.PHOTO) {
      findPhotoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_photo));
    } else if (mediatype == MultiMediaConstant.THRD_PHOTO) {
      findThrdPhotoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_photo));
    } else if (mediatype == MultiMediaConstant.AUDIO) {
      findMusicView();
      vPMediaType.setText(mContext.getString(R.string.mmp_audio));
    } else if (mediatype == MultiMediaConstant.VIDEO) {
      findVideoView();
      vPMediaType.setText(mContext.getString(R.string.mmp_video));
    } else if (mediatype == MultiMediaConstant.TEXT) {
      findTextView();
      vPMediaType.setText(mContext.getString(R.string.mmp_text));
    }
    // Add by keke 1215 for fix cr DTV00383194
    setInforbarTransparent();
  }

  public void initVideoTrackNumber() {
    if (null == mVideoTrackNumber) {
      return;
    }
    int number = mLogicManager.getAudioTranckNumber();
    if (number == 0) {
      // Modifyed by Dan 20111118 for fix bug DTV00373279
      // mVideoTrackNumber.setText(0 + "/" + number);
      mVideoTrackNumber.setText("");
	  mVideoTrackNumber.setVisibility(View.INVISIBLE);
      audioTrackIndex = 0;
    } else if (number > 0) {
      int index = mLogicManager.getAudioTrackIndex();
      if (index >= number || index < 0) {
        index = 0;
        audioTrackIndex = 0;
        mLogicManager.setAudioTranckNumber((short) index);
      }
      String type = mLogicManager.getCurrentAudioTranckType(index);
            String audioMimeType = mLogicManager.getCurrentAudioTranckMimeType(index);

            if (DolbylogicManager.getInstance().isDolbyAudio(audioMimeType)) {
                //mDubiIcon.setVisibility(View.VISIBLE);
                Drawable musicIcon = mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
                Drawable duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_icon);
                duiIcon.setBounds(0, 0, mDubiIconWidth, mDubiIconHeight);
        mVideoTrackNumber.setCompoundDrawables(duiIcon, null, null, null);
      } else {
//        mDubiIcon.setVisibility(View.GONE);
                Drawable musicIcon = mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
        musicIcon.setBounds(0, 0, musicIcon.getMinimumWidth(), musicIcon.getMinimumHeight());
        mVideoTrackNumber.setCompoundDrawables(musicIcon, null, null, null);
      }
	  mVideoTrackNumber.setVisibility(View.VISIBLE);
      mVideoTrackNumber.setText(type + "(" + (index+1) + "/" + number + ")");//+ "/" + number
	   if(mVideoTrackNumber.getText().toString().contains(mDolbyStr)){
           mVideoTrackNumber.setText(mDolbyStr + "(" + (index+1) + "/" + number + ")");
       }
//      mVideoTrackNumber.setText(mLogicManager.getCurrentAudioTranckType(index) + " " + (index+1));
//      //+ "/" + number
    }
  }

  public boolean changeVideoTrackNumber() {

    if (null == mVideoTrackNumber) {
      return false;
    }

    int tranckNumber = mLogicManager.getAudioTranckNumber();
    int currentTrack = mLogicManager.getAudioTrackIndex();
    String audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
    String audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack));
    MtkLog.d(TAG, "--- changeVideoTrackNumber tranckNumber :" + tranckNumber + "currentTrack:"
        + currentTrack + "  audioType:" + audioType);
    // modif by lei
        if (tranckNumber < 2 && !(DolbylogicManager.getInstance().isDolbyDualAudio(audioMimeType))) {
			MtkLog.d(TAG, "--- changeVideoTrackNumber tranckNumber < 2:tranckNumber:"+tranckNumber );
      return false;
    }
    if (currentTrack < 0) {
      currentTrack = audioTrackIndex;
    }
    try {
      String value = mVideoTrackNumber.getText().toString();
//      String[] datas = value.split("/");
//      short currentTrack = Short.parseShort(datas[0]);
//      short number = Short.parseShort(datas[1]);
      audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
      audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack));
            if (DolbylogicManager.getInstance().isDolbyDualAudio(audioMimeType)) {
				switch (CommonSet.getInstance(mContext).getAudioSpeakerMode()) {
				case AUDDEC_SPK_MODE_LR:
					CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LL);
					audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
					mVideoTrackNumber.setVisibility(View.VISIBLE);
					mVideoTrackNumber.setText(audioType + "(" + (currentTrack + 1) + "/" + mLogicManager.getAudioTranckNumber()  + ")");
					 if(mVideoTrackNumber.getText().toString().contains(mDolbyStr)){
						   mVideoTrackNumber.setText(mDolbyStr + "(" + (currentTrack + 1) + "/" + mLogicManager.getAudioTranckNumber() + ")");
					   }
					return true;
				case AUDDEC_SPK_MODE_LL:
					CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_RR);
					audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack));
					mVideoTrackNumber.setVisibility(View.VISIBLE);
					mVideoTrackNumber.setText(audioType + "(" + (currentTrack + 1) + "/" + mLogicManager.getAudioTranckNumber() + ")");
					 if(mVideoTrackNumber.getText().toString().contains(mDolbyStr)){
						   mVideoTrackNumber.setText(mDolbyStr + "(" + (currentTrack + 1) + "/" + mLogicManager.getAudioTranckNumber() + ")");
					   }
					return true;
				case AUDDEC_SPK_MODE_RR:
					CommonSet.getInstance(mContext).setAudioSpeakerMode(MtkTvVolCtrl.SpeakerType.AUDDEC_SPK_MODE_LR);
          break;
        default:
          break;
        }

      }
      currentTrack++;
      if (currentTrack >= tranckNumber) {
        currentTrack = 1;
      } else {
        currentTrack++;
      }
      MtkLog.d(TAG, "--- changeVideoTrackNumber --currentTrack:" + currentTrack + "--number:"
          + tranckNumber);
      audioType = mLogicManager.getCurrentAudioTranckType((short) (currentTrack - 1));
      audioMimeType = mLogicManager.getCurrentAudioTranckMimeType((short) (currentTrack - 1));
      if (mLogicManager.setAudioTranckNumber((short) (currentTrack - 1))) {
        audioTrackIndex = (short)(currentTrack - 1);
        if (DolbylogicManager.getInstance().isDolbyAudio(audioMimeType)) {
  //        mDubiIcon.setVisibility(View.VISIBLE);
                    Drawable musicIcon = mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
                    Drawable duiIcon = mContext.getResources().getDrawable(R.drawable.dubi_icon);
                    duiIcon.setBounds(0, 0, mDubiIconWidth, mDubiIconHeight);
                    mVideoTrackNumber.setCompoundDrawables(duiIcon, null, null, null);
                } else {
                    //mDubiIcon.setVisibility(View.GONE);
                    Drawable musicIcon = mContext.getResources().getDrawable(R.drawable.mmp_toolbar_typeicon_music);
          musicIcon.setBounds(0, 0, musicIcon.getMinimumWidth(), musicIcon.getMinimumHeight());
          mVideoTrackNumber.setCompoundDrawables(musicIcon, null, null, null);
        }
		mVideoTrackNumber.setVisibility(View.VISIBLE);
		
        mVideoTrackNumber.setText(audioType + "(" + currentTrack+ "/" + mLogicManager.getAudioTranckNumber() + ")");
		 if(mVideoTrackNumber.getText().toString().contains(mDolbyStr)){
						   mVideoTrackNumber.setText(mDolbyStr + "(" + currentTrack + "/" + mLogicManager.getAudioTranckNumber() + ")");
					   }
        return true;
      } else {
		  mVideoTrackNumber.setVisibility(View.INVISIBLE);
        MtkLog.d(TAG,
            "--- changeVideoTrackNumber --setAudioTranckNumber return false, set fail!!!!");
      }
    } catch (Exception e) {
		mVideoTrackNumber.setVisibility(View.INVISIBLE);
      MtkLog.d(TAG, "--- changeVideoTrackNumber --:" + e.getMessage());
    }

//    try {
//      String value = mVideoTrackNumber.getText().toString();
////      String[] datas = value.split("/");
////      short currentTrack = Short.parseShort(datas[0]);
////      short number = Short.parseShort(datas[1]);
//      int currentTrack = mLogicManager.getAudioTrackIndex();
//      currentTrack++;
//      if (currentTrack >= tranckNumber) {
//        currentTrack = 1;
//      } else {
//        currentTrack++;
//      }
//      MtkLog.d(TAG, "--- changeVideoTrackNumber --currentTrack:"
//    +currentTrack+"--tranckNumber:"+tranckNumber );
//      if(mLogicManager.setAudioTranckNumber((short) (currentTrack - 1))){
//        mVideoTrackNumber.setText(mLogicManager.getCurrentAudioTranckType(currentTrack - 1)
//    + " " + currentTrack);
////        mVideoTrackNumber.setText(mLogicManager.getCurrentAudioTranckType(currentTrack -1)
//    + " " + currentTrack+ "/" + number);
//        return true;
//      }else{
//        MtkLog.d(TAG, "--- changeVideoTrackNumber" +
//    "--setAudioTranckNumber return false, set fail!!!!" );
//      }
//    } catch (Exception e) {
//      MtkLog.d(TAG, "--- changeVideoTrackNumber --:" + e.getMessage());
//    }
    return false;
  }

//	public boolean isAC3DualAudio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("ac3 dual")) {
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isAC3Audio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("ac3")) {
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isEAC3DualAudio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("eac3 dual")) {
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isEAC3Audio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("eac3")) {
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isHEAACDualAudio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("heaac dual")) {
//			return true;
//		}
//		return false;
//	}
//
//	public boolean isHEAACAudio(String mime) {
//		if (mime != null && mime.toLowerCase().contains("heaac")) {
//			return true;
//		}
//		return false;
//	}

  public void setRepeat(int type) {
    int casetype = mLogicManager.getRepeatModel(type);
    MtkLog.i(TAG, "casetype:" + casetype);
    switch (casetype) {
      case Const.REPEAT_ALL:
        setRepeatAll();
        break;
      case Const.REPEAT_NONE:
        setRepeatNone();
        break;
      case Const.REPEAT_ONE:
        setRepeatSingle();
        break;
      default:
        break;
    }

  }

  private void findCommonView() {
    mPlayPauseLayout = (RelativeLayout) vControlView.findViewById(R.id.mmp_pop_playstatus_layout);
    vPStatePlay = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_playstateplay);
    vPStatePause = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_playstatepause);

    vPMediaType = (TextView) vControlView
        .findViewById(R.id.mmp_pop_mediatype);
    vPOrder = (TextView) vControlView.findViewById(R.id.mmp_pop_order_tv);
    vPFileName = (TextView) vControlView
        .findViewById(R.id.mmp_pop_filename_tv);

    mControlbottom = (LinearLayout) vControlView.findViewById(R.id.mmp_control_bottom);
    // SKY luojie add 20171219 for add choose menu begin
    mControlbottom.setVisibility(View.GONE);
    // SKY luojie add 20171219 for add choose menu end
  }

  private void findThrdPhotoView() {
    vPZoomSize = (TextView) vControlView
        .findViewById(R.id.mmp_pop_zoomsize);
    // vPRepeat = (LinearLayout) vControlView
    // .findViewById(R.id.mmp_pop_repeat);
    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPRepeatTv.setSelected(true);
    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);
    vPTimeLong = (TextView) vControlView.findViewById(R.id.mmp_pop_time_tv);
    vPView = (TextView) vControlView.findViewById(R.id.mmp_pop_view_tv);

    setRepeat(Const.FILTER_IMAGE);
    setShuffle(Const.FILTER_IMAGE);
    setThrdPhotoDuration();
  }

  private void findPhotoView() {
    vPZoomSize = (TextView) vControlView
        .findViewById(R.id.mmp_pop_zoomsize);
    // vPRepeat = (LinearLayout) vControlView
    // .findViewById(R.id.mmp_pop_repeat);
    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPRepeatTv.setSelected(true);
    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);
    vPTimeLong = (TextView) vControlView.findViewById(R.id.mmp_pop_time_tv);
    vPView = (TextView) vControlView.findViewById(R.id.mmp_pop_view_tv);

    setRepeat(Const.FILTER_IMAGE);
    setShuffle(Const.FILTER_IMAGE);
    setPhotoDuration();

  }

  private void setThrdPhotoDuration() {
    type = Photo3DPlayActivity.mDelayedTime;
    if (type == Photo3DPlayActivity.DELAYED_SHORT) {
      vPTimeLong.setText(R.string.mmp_menu_short);
    } else if (type == Photo3DPlayActivity.DELAYED_MIDDLE) {
      vPTimeLong.setText(R.string.mmp_menu_medium);
    } else if (type == Photo3DPlayActivity.DELAYED_LONG) {
      vPTimeLong.setText(R.string.mmp_menu_long);
    }
  }

  private void setPhotoDuration() {
    if (Util.PHOTO_4K2K_ON) {

      type = Photo4K2KPlayActivity.mDelayedTime;
      if (type == Photo4K2KPlayActivity.DELAYED_SHORT) {
        vPTimeLong.setText(R.string.mmp_menu_short);
      } else if (type == Photo4K2KPlayActivity.DELAYED_MIDDLE) {
        vPTimeLong.setText(R.string.mmp_menu_medium);
      } else if (type == Photo4K2KPlayActivity.DELAYED_LONG) {
        vPTimeLong.setText(R.string.mmp_menu_long);
      }
    } else {
      type = PhotoPlayActivity.mDelayedTime;
      if (type == PhotoPlayActivity.DELAYED_SHORT) {
        vPTimeLong.setText(R.string.mmp_menu_short);
      } else if (type == PhotoPlayActivity.DELAYED_MIDDLE) {
        vPTimeLong.setText(R.string.mmp_menu_medium);
      } else if (type == PhotoPlayActivity.DELAYED_LONG) {
        vPTimeLong.setText(R.string.mmp_menu_long);
      }
    }
  }

  private void setShuffle(int type) {
    boolean isShuffle = mLogicManager.getShuffleMode(type);
    if (isShuffle) {
      setShuffleVisble(View.VISIBLE);
    } else {
      setShuffleVisble(View.INVISIBLE);
    }
  }

  private void findMusicView() {
    vVideoSpeed = (TextView) vControlView
        .findViewById(R.id.mmp_video_repeata);
    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPRepeatTv.setSelected(true);
    // vPRepeat = (LinearLayout) vControlView
    // .findViewById(R.id.mmp_pop_repeat);
    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);
    vMStartTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_starttime);
    vMEndTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_endtime);
    vMProgressBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_music_progress);
    vMVolumeTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_volume);
    vMVolumeBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_musicvolume_progress);
    mVolumeProgressBg = (LinearLayout) vControlView
        .findViewById(R.id.mmp_volume_progress_bg);

    setRepeat(Const.FILTER_AUDIO);
    setShuffle(Const.FILTER_AUDIO);
    // mute
  }

  /**
   * if true show video display current playback time and duration in control panel
   * fix DTV00367339
   *@param flag
   */
  // public void setVisibility(boolean flag)
  // {
  // if(flag)
  // {
  // setStartTimeVisible(View.VISIBLE);
  // setEndTimeVisible(View.VISIBLE);
  // }else
  // {
  // setStartTimeVisible(View.INVISIBLE);
  // setEndTimeVisible(View.INVISIBLE);
  // }
  // }
  public void setTimeViewVisibility(boolean flag)
  {
    if (flag)
    {
      setStartTimeVisible(View.VISIBLE);
      setEndTimeVisible(View.VISIBLE);
    } else
    {
      setStartTimeVisible(View.INVISIBLE);
      setEndTimeVisible(View.INVISIBLE);
    }
  }

  private void setStartTimeVisible(int visible) {
    Log.i(TAG, "setStartTimeVisible visible:" + visible);
//    new Exception("setStartTimeVisible").printStackTrace();
    if (vMStartTime != null) {
      vMStartTime.setVisibility(visible);
    }
  }

  private void setEndTimeVisible(int visible) {
    Log.i(TAG, "setEndTimeVisible visible:" + visible);
//    new Exception("setEndTimeVisible").printStackTrace();
    if (vMEndTime != null) {
      vMEndTime.setVisibility(visible);
    }
  }

  /**
   * fix reStart play 2D,3D photo , music and video repeatOne reserves problem
   */
  public void setRepeatVisibility(int type) {
    setRepeat(mediaType);
  }

  /**
   * if true, display start time and end time, else dont.
   * @return
   */
  public boolean isTimeViewVisiable() {
    if (vMStartTime.getVisibility() == View.VISIBLE) {
      return true;
    }
    return false;
  }

  private void findVideoView() {

    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPRepeatTv.setSelected(true);

    mRepeatLogo = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_repeat_logo);

    vVideoSpeed = (TextView) vControlView
        .findViewById(R.id.mmp_video_repeata);

    vPZoomSize = (TextView) vControlView
        .findViewById(R.id.mmp_pop_zoomsize);

    vMStartTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_starttime);

    vMEndTime = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_endtime);

    vMProgressBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_music_progress);

    vMVolumeTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_music_volume);

    vMVolumeBar = (ProgressBar) vControlView
        .findViewById(R.id.mmp_pop_musicvolume_progress);

    mVideoTrackNumber = (TextView) vControlView
        .findViewById(R.id.mmp_pop_video_order);

//  mDubiIcon = (TextView) vControlView
//  .findViewById(R.id.mmp_dubi_icon);

    mVolumeProgressBg = (LinearLayout) vControlView
        .findViewById(R.id.mmp_volume_progress_bg);

    mVideoSubtitle = (TextView) vControlView
        .findViewById(R.id.mmp_pop_subtitle_number);
    mVideoSubtitleIcon = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_subtitle_icon);
    mVideoLayout = (RelativeLayout) vControlView.findViewById(R.id.mmp_video_rl);
    setRepeat(Const.FILTER_VIDEO);
  }

  // Added by Dan for fix bug DTV00380300
  public void setZoomEmpty() {
    vPZoomSize.setText("");
  }

  public void setPhotoZoomSize() {
    int size = mLogicManager.getCurrentZoomSize();
    if (size == 0) {
      size = 1;
    } else {
      size = 2 << (size - 1);
    }
    vPZoomSize.setText(size + "X");
  }

  public void setZoomSize() {

    // if(mLogicManager.getVideoWidth() <= 0
    // ||mLogicManager.getVideoHeight() <= 0){
    //
    // vPZoomSize.setText("");
    //
    // }else{

    int size = mLogicManager.getCurZomm();
    if (size == 0) {
      size = 1;
    } else {
      size = 2 << (size - 1);
    }
    vPZoomSize.setText(size + "X");
    // }
  }

  private void findTextView() {

    vPRepeatTv = (TextView) vControlView
        .findViewById(R.id.mmp_pop_repeat_tv);
    vPRepeatTv.setSelected(true);

    vPShuffle = (ImageView) vControlView
        .findViewById(R.id.mmp_pop_shuffle_img);

    vPTimeLong = (TextView) vControlView.findViewById(R.id.mmp_pop_time_tv);

    setRepeat(Const.FILTER_TEXT);
    setShuffle(Const.FILTER_TEXT);
  }

  public void setMediaPlayState() {
    // Modified by Dan for fix bug DTV00376577
    if (isPlaying()) {
      pause();
    } else {
      play();
    }

    // if(isPause()){
    // play();
    // }else{
    // pause();
    // }
    if (null != mRepeatLogo
        && !(mRepeatLogo.getVisibility() == View.VISIBLE)
        && (mRepeat != ABRpeatType.ABREPEAT_TYPE_NONE
        && mRepeat != ABRpeatType.ABREPEAT_TYPE_CANCEL_ALL)) {
      setRepeatLogoVisible(View.VISIBLE);
    }

    if (null != vVideoSpeed) {
      Log.i(TAG, "vVideoSpeed!=NULL VISIBLE:" + vVideoSpeed.getVisibility() + "---shown:"
          + vVideoSpeed.isShown());
      if (vVideoSpeed.getVisibility() == View.VISIBLE) {
        setVideoSpeedVisible(View.INVISIBLE);
      }
    }
  }

  public void stopKeyPause() {
    mLogicManager.pauseVideoWhenStopKey();
    if (null != mRepeatLogo
        && !(mRepeatLogo.getVisibility() == View.VISIBLE)
        && (mRepeat != ABRpeatType.ABREPEAT_TYPE_NONE
        && mRepeat != ABRpeatType.ABREPEAT_TYPE_CANCEL_ALL)) {
      setRepeatLogoVisible(View.VISIBLE);
    }

    if (null != vVideoSpeed) {
      Log.i(TAG, "vVideoSpeed!=NULL VISIBLE:" + vVideoSpeed.getVisibility() + "---shown:"
          + vVideoSpeed.isShown());
      if (vVideoSpeed.getVisibility() == View.VISIBLE) {
        setVideoSpeedVisible(View.INVISIBLE);
      }
    }
  }

  public void setVideoSpeedVisible(int visible) {
    MtkLog.i(TAG, "setVideoSpeedVisible visible:" + visible);
    //new Exception("setVideoSpeedVisible").printStackTrace();
    if (vVideoSpeed != null) {
      vVideoSpeed.setVisibility(visible);
    }
  }

  public void setPlayIcon()
  {
    setPauseVisiblity(View.GONE);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
  }

  public void play() {
    // Moved to bottom by keke 1.18
    try {
      mControlImp.play();
      setPauseVisiblity(View.GONE);
      setPlayVisibilty(View.VISIBLE);
      // Added by Dan for fix bug DTV00376577
      mIsPlaying = true;
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  public void pause() {
    try {
      mControlImp.pause();
      if (isPause() || (mediaType != MultiMediaConstant.VIDEO && mediaType != MultiMediaConstant.AUDIO)){
          setPauseVisiblity(View.VISIBLE);
          setPlayVisibilty(View.GONE);
          // end
          // Added by Dan for fix bug DTV00376577
          mIsPlaying = false;
      }
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  private void setPlayVisibilty(int visible) {
    Log.i(TAG, "setPlayVisibilty vPStatePlay:" + vPStatePlay + "--visible:" + visible);
    // new Exception().printStackTrace();
    if (vPStatePlay != null) {
      vPStatePlay.setVisibility(visible);
    }
    if (visible == View.VISIBLE) {
      setVideoSpeedVisible(View.GONE);
    }
  }

  private void setPauseVisiblity(int visible) {
    Log.i(TAG, "setPauseVisiblity vPStatePause:" + vPStatePause + "--visible:" + visible);
    // new Exception().printStackTrace();
    if (vPStatePause != null) {
      vPStatePause.setVisibility(visible);
    }
    if (visible == View.VISIBLE) {
      setVideoSpeedVisible(View.GONE);
    }
  }

  public void onCapture()
  {
    try {
      mControlImp.pause();
      mIsPlaying = false;
    } catch (Exception e) {
      MtkLog.i(TAG, e.getMessage());
    }
  }

  // add by shuming for fix CR: DTV00407914
  public void setPauseIcon(int visibility)
  {
    setPauseVisiblity(visibility);
    setPlayVisibilty(View.GONE);
  }

  // add by shuming for fix CR: DTV00407914
  public void setPlayIcon(int visibility)
  {
    setPlayVisibilty(visibility);
    setPauseVisiblity(View.GONE);
  }

  public void setPauseIconGone() {
    setPauseVisiblity(View.GONE);
    setPlayVisibilty(View.GONE);
  }

  public void reSetVideo() {
    setPauseVisiblity(View.INVISIBLE);
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_pause);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
    setVideoSpeedVisible(View.INVISIBLE);
  }

  public short getSubtitleIndex() {
    return subtitleIndex;
  }

  public void setVideoSubtitle(short number, short index) {
    MtkLog.d(TAG, "setVideoSubtitle number:" + number + "  index:" + index);
    if (number <= 0) {
      setVideoSubtitleVisible(View.INVISIBLE);
    } else {
      setVideoSubtitleVisible(View.VISIBLE);
      if (index < 0) {
        mLogicManager.setSubOnOff(false);
        mVideoSubtitle.setText(sttl+": "+strOff);//modified by yangxiong for solving "sa and na sttl need to change cc"
      } else {
        mLogicManager.setSubtitleTrack(index);
        mVideoSubtitle.setText(sttl+": "+(index + 1) + "/" + number);//modified by yangxiong for solving "sa and na sttl need to change cc"
      }
      subtitleIndex = index;
    }
  }

  public void setVideoSubtitleVisible(int visible) {
    if (mVideoSubtitle != null) {
      mVideoSubtitle.setVisibility(visible);
    }
    if (mVideoSubtitleIcon != null) {
      //mVideoSubtitleIcon.setVisibility(visible);
    }

  }

  /**
   * Initialize subtitle,set subtitle off
   *
   * @param number
   */
  public void initSubtitle(short trackNum) {
    MtkLog.i(TAG, "initSubtitle: trackNum:" + trackNum);
//    mLogicManager.setSubtitleTrack((short)255);
    short number = (mLogicManager.getSubtitleTrackNumber());
    if (number <= 0) {
      mLogicManager.setSubtitleTrack((short)255);
      setVideoSubtitleVisible(View.INVISIBLE);
    } else {
      short index = (short)(mLogicManager.getSubtitleIndex());
      MtkLog.d(TAG, "initSubtitle index :" + index);
      setVideoSubtitleVisible(View.VISIBLE);
      //modified by keke for DTV00384824
      int lastMemoryValue = SaveValue.getInstance(mContext.getApplicationContext())
          .readValue(LastMemory.LASTMEMORY_ID);
      if (lastMemoryValue == LastMemory.LASTMEMORY_OFF
          || lastMemoryValue == LastMemory.LASTMEMORY_TIME) {
        mLogicManager.setSubtitleTrack((short)255);
        mVideoSubtitle.setText(sttl+": "+strOff);//modified by yangxiong for solving "sa and na sttl need to change cc"
        subtitleIndex = -1;
      } else if (lastMemoryValue == LastMemory.LASTMEMORY_POSITION) {
        if (index < 0) {
          mLogicManager.setSubtitleTrack((short)255);
          mVideoSubtitle.setText(sttl+": "+strOff);//modified by yangxiong for solving "sa and na sttl need to change cc"
          subtitleIndex = -1;
        } else {
          mLogicManager.setSubtitleTrack(index);
          mVideoSubtitle.setText(sttl+": "+(index + 1) + "/" + number);//modified by yangxiong for solving "sa and na sttl need to change cc"
          subtitleIndex = index;
        }
      }
    }
  }

  /**
   * Initialize subtitle,set subtitle off
   *
   * @param number
   */
  public void reinitSubtitle(short number) {
    int index = mLogicManager.getSubtitleIndex();
    MtkLog.i(TAG, "reinitSubtitle:" + index + " number:" + number);
    int videoSource = MultiFilesManager.getInstance(mContext)
        .getCurrentSourceType();
    if (videoSource != MultiFilesManager.SOURCE_LOCAL) {
      index = subtitleIndex;
    }
    if (index >= 0 && index < number) {
      setVideoSubtitle(number, (short) index);
      subtitleIndex = (short) index;
    } else {
      initSubtitle(number);
    }
  }

  public void reSetAudio() {
    setPauseVisiblity(View.INVISIBLE);
    vPStatePause.setBackgroundResource(R.drawable.mmp_top_pause);
    setPlayVisibilty(View.VISIBLE);
    mIsPlaying = true;
    if (null != vVideoSpeed) {
      setVideoSpeedVisible(View.INVISIBLE);
    }
  }

  public void stop() {
    if (null != vVideoSpeed && (vVideoSpeed.getVisibility() == View.VISIBLE)) {
      setVideoSpeedVisible(View.INVISIBLE);
    }
    setPauseVisiblity(View.VISIBLE);
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_stop);
    setPlayVisibilty(View.GONE);
    // Added by Dan for fix bug DTV00376577
    mIsPlaying = false;
  }

  public void onFast(int speed, int status, int type) {
    MtkLog.d(TAG, "onFast speed:" + speed + "  status:" + status + "  type:" + type);
    setPlayVisibilty(View.INVISIBLE);
    mIsPlaying = false;
    setPauseVisiblity(View.INVISIBLE);
    setVideoSpeedVisible(View.VISIBLE);

    if (speed == 1) {
      setPlayVisibilty(View.VISIBLE);
      mIsPlaying = true;
      setVideoSpeedVisible(View.INVISIBLE);
      return;
    }

    vVideoSpeed.setText(speed + "x");
    Drawable img_left = null;
    if (type == Const.FILTER_VIDEO) {
      if (null != mRepeatLogo && (mRepeatLogo.getVisibility() == View.VISIBLE)) {
        setRepeatLogoVisible(View.INVISIBLE);
      }
      vVideoSpeed.setTextColor(Color.WHITE);
    }

    if (status == 0) {
      if (type == Const.FILTER_VIDEO) {
        img_left = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_ff_video);
      } else {
        img_left = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_ff);
      }
    } else if (status == 1) {
      if (type == Const.FILTER_VIDEO) {
        img_left = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_rew_video);
      } else {
        img_left = mContext.getResources().getDrawable(
            R.drawable.toolbar_typeicon_rew);

      }
    } else if (status == 2) {
      img_left = mContext.getResources().getDrawable(
          R.drawable.toolbar_typeicon_ff);

      vVideoSpeed.setText("1/" + speed + "x");

    } else if (status == 3) {

      img_left = mContext.getResources().getDrawable(
          R.drawable.toolbar_typeicon_rew);

      vVideoSpeed.setText("1/" + speed + "x");
    }

    if (img_left != null) {
      img_left.setBounds(0, 0, img_left.getMinimumWidth(), img_left
          .getMinimumHeight());
    }

    vVideoSpeed.setCompoundDrawables(img_left, null, null, null);
  }

  public void setSlow() {

  }

  public boolean isPlaying() {
    switch (mediaType) {
      case MultiMediaConstant.VIDEO:
        return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STARTED;
      case MultiMediaConstant.AUDIO:
        return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED;
      default:
        break;
    }
    return mIsPlaying;
  }

  public boolean isPause() {
    switch (mediaType) {
      case MultiMediaConstant.VIDEO:
        return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED;
      case MultiMediaConstant.AUDIO:
        return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_PAUSED;
      default:
        break;
    }
    return !mIsPlaying;
  }

  public void setFileName(String name) {
    // if(MultiFilesManager.isSourceDLNA(mContext)){
    // int index = name.lastIndexOf(".");
    // if(index > 0){
    // name = name.substring(0,index);
    // }
    // }
    vPFileName.setText(name);
  }

  public void setFilePosition(String pagesize) {
    vPOrder.setText(pagesize);
  }

  public void setVideoRepeat(int type) {

    // switch (type) {
    // case Const.REPEAT_NONE: {
    // mRepeatLogo.setBackgroundResource(0);
    // break;
    // }
    // case Const.REPEAT_ONE: {
    // mRepeatLogo.setBackgroundResource(R.drawable.toolbar_toprepeat_a);
    // break;
    // }
    // case Const.REPEAT_ALL: {
    // mRepeatLogo.setBackgroundResource(R.drawable.toolbar_toprepeat_ab);
    // }
    // default:
    // break;
    // }

  }

  public void setRepeatAll() {
    setRepeatModeVisible(View.VISIBLE);
    vPRepeatTv.setCompoundDrawables(repeatAll, null, null, null);
    // vPRepeatTv.setText(mContext.getString(R.string.mmp_pop_repeat_tvrepeat));
    vPRepeatTv.setText("");
  }

  private void setRepeatModeVisible(int visible) {
    if (vPRepeatTv != null) {
      vPRepeatTv.setVisibility(visible);
    }
  }

  public void setRepeatSingle() {
    setRepeatModeVisible(View.VISIBLE);
    vPRepeatTv.setCompoundDrawables(repeatOne, null, null, null);
    // vPRepeatTv.setText(mContext.getString(R.string.mmp_pop_repeat_tvsingle));
    vPRepeatTv.setText("");
  }

  public void setRepeatNone() {
    setRepeatModeVisible(View.INVISIBLE);
    // add by yongzhengwei for fix bug DTV00379498
    vPRepeatTv.setCompoundDrawables(null, null, null, null);
    vPRepeatTv.setText(mContext.getString(R.string.mmp_pop_repeat_tvnone));
  }

  public void setShuffleVisble(int visibility) {
    if (vPShuffle != null) {
      vPShuffle.setVisibility(visibility);
    }

  }

  public void setPhotoZoom(String scale) {
    vPZoomSize.setText(scale);
  }

  public void setPhotoTimeType(String type) {
    vPTimeLong.setText(type);
  }

  public void setPhotoAnimationEffect(String animation) {
    vPView.setText(animation);
  }

  public void setPhotoAnimationEffect(int type) {
    String value = "";
    switch (type) {
      case ConstPhoto.DEFAULT:
        value = mContext.getString(R.string.mmp_menu_none);
        break;
      case ConstPhoto.dissolve:
        value = mContext.getString(R.string.mmp_menu_dissolve);
        break;

      case ConstPhoto.wipe_right:
        value = mContext.getString(R.string.mmp_menu_wiperight);
        break;
      case ConstPhoto.wipe_left:
        value = mContext.getString(R.string.mmp_menu_wipeleft);
        break;
      case ConstPhoto.wipe_top:
        value = mContext.getString(R.string.mmp_menu_wipeup);
        break;
      case ConstPhoto.wipe_bottom:
        value = mContext.getString(R.string.mmp_menu_wipedown);
        break;
      case ConstPhoto.box_in:
        value = mContext.getString(R.string.mmp_menu_boxin);
        break;
      case ConstPhoto.box_out:
        value = mContext.getString(R.string.mmp_menu_boxout);
        break;
      case ConstPhoto.RADNOM:
        value = mContext.getString(R.string.mmp_menu_random);
        break;
      default:
        break;
    }
    vPView.setText(value);
  }

  public void setCurrentTime(long mills) {
    // mills = mills+500;
    mills /= 1000;
    long minute = mills / 60;
    long hour = minute / 60;
    long second = mills % 60;
    minute %= 60;
    vMStartTime.setText(String.format("%02d:%02d:%02d", hour, minute,
        second));
    Log.i(TAG, "setCurrentTime starttime:" + vMStartTime.getText());
  }

  public void setEndtime(int mills) {
    setProgressMax(mills);
    mills /= 1000;
    int minute = mills / 60;
    int hour = minute / 60;
    int second = mills % 60;
    minute %= 60;
    // String text = "";
    try {
      vMEndTime.setText(String.format("%02d:%02d:%02d", hour, minute, second));
      // text = String.format("%02d:%02d:%02d", hour, minute, second);
    } catch (Exception e) {
      vMEndTime.setText("");
    }

  }

  public void setProgressMax(int max) {
    MtkLog.d(TAG, "setProgressMax max:" + max);
//    MtkLog.d(TAG, "setProgressMax max:" + max + "  " + Log.getStackTraceString(new Throwable()));
    vMProgressBar.setMax(max);
  }

  public int getProgressMax() {
    return vMProgressBar.getMax();
  }

  public int getCurrentProgress() {
    return vMProgressBar.getProgress();
  }

  public void setProgress(int progress) {
    MtkLog.d(TAG, "setProgress progress:" + progress);
//    MtkLog.d(TAG, "setProgress progress:" + progress
//        + "  " + Log.getStackTraceString(new Throwable()));
    vMProgressBar.setProgress(progress);
  }

  public void setVolumeMax(int max) {
    vMVolumeBar.setMax(max);
  }

  public void setCurrentVolume(int volume) {
    // new Exception("setCurrentVolume").printStackTrace();
    Log.i(TAG, "setCurrentVolume: volume:" + volume);
    if (null == vMVolumeBar) {
      return;
    }

    vMVolumeBar.setProgress(volume);

  }

  private void setVolumeProgressBgVisible(int visible) {
    if (mVolumeProgressBg != null) {
      mVolumeProgressBg.setVisibility(visible);
    }
  }

  public void setMute(boolean isMute) {
    if (null == vMVolumeBar) {
      return;
    }
    Log.i(TAG, "setMute isMute:" + isMute);
    if (!isMute) {
      setVolumeMax(mLogicManager.getMaxVolume());
      setCurrentVolume(mLogicManager.getVolume());
    }

    if (null != mVolumeProgressBg) {
      if (isMute) {
        Log.i(TAG, "mVolumeProgressBg INVISIBLE isMute:" + isMute);
        setVolumeProgressBgVisible(View.INVISIBLE);
      } else {
        Log.i(TAG, "mVolumeProgressBg VISIBLE isMute:" + isMute);
        setVolumeProgressBgVisible(View.VISIBLE);
      }

    }

    if (null != vMVolumeTv) {
      if (isMute) {
        Log.i(TAG, "vMVolumeTv isMute true:" + isMute);
        Drawable img_left = mContext.getResources().getDrawable(
            R.drawable.mmp_toolbar_icon_mute);
        img_left.setBounds(0, 0, img_left.getMinimumWidth(), img_left
            .getMinimumHeight());
        vMVolumeTv.setCompoundDrawables(img_left, null, null, null);
        vMVolumeTv.setTag(Boolean.TRUE);
      } else {
        Log.i(TAG, "vMVolumeTv isMute false :" + isMute);
        Drawable img_left = mContext.getResources().getDrawable(
            R.drawable.mmp_toolbar_icon_volume);
        img_left.setBounds(0, 0, img_left.getMinimumWidth(), img_left
            .getMinimumHeight());
        vMVolumeTv.setCompoundDrawables(img_left, null, null, null);
        vMVolumeTv.setTag(Boolean.FALSE);
      }
    }

  }

  public void reSetPause() {
    vPStatePause.setBackgroundResource(R.drawable.toolbar_top_pause);
  }

  // Added by keke 1202 for fix bug DTV00379478
  public void setInforbarNull() {
    setStartTimeVisible(View.INVISIBLE);
    vMStartTime.setText("");
    Log.i(TAG, "vMStartTime.setTex NULL");
    setEndTimeVisible(View.INVISIBLE);
    vMEndTime.setText("");
    vMProgressBar.setProgress(0);
    vPZoomSize.setText("");
    mVideoTrackNumber.setText("");
	mVideoTrackNumber.setVisibility(View.INVISIBLE);
    setVideoSubtitleVisible(View.INVISIBLE);
    vPOrder.setText("");
    vPFileName.setText("");
    setVideoSpeedVisible(View.INVISIBLE);
  }

  // Add by keke 1215 for fix cr DTV00383194
  private void setInforbarTransparent() {
    try {
      LinearLayout m = (LinearLayout) vControlView.findViewById(R.id.mmp_control_bottom);
      // SKY luojie add 20171220 for nullpointer exception begin
      if(m != null) {
          if(m.getBackground() != null) {
              m.getBackground().setAlpha(220);
          }
      }
      // SKY luojie add 20171220 for nullpointer exception end
      // LinearLayout m2 = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_popwindow_Operator_Message);
      // m2.getBackground().setAlpha(220);
      // LinearLayout m = (LinearLayout) vControlView.findViewById(R.id.mmp_popwindow);
      // m.getBackground().setAlpha(220);
      // LinearLayout m2 = (LinearLayout)
      // vControlView.findViewById(R.id.mmp_popwindow_Operator_Message);
      // m2.getBackground().setAlpha(220);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setVMProgressBarVisible(int visible) {
    if (null != vMProgressBar) {
      vMProgressBar.setVisibility(visible);
    }
  }

  public void hideProgress() {
    setVMProgressBarVisible(View.INVISIBLE);
    setStartTimeVisible(View.INVISIBLE);
    setEndTimeVisible(View.INVISIBLE);
  }

  public void showProgress() {
    setStartTimeVisible(View.VISIBLE);
    setEndTimeVisible(View.VISIBLE);
    setVMProgressBarVisible(View.VISIBLE);
  }

  public void showPausePlayIcon(boolean isPlaying) {

    if (isPlaying == true) {
      setPlayVisibilty(View.VISIBLE);
    }

    setPauseVisiblity(View.GONE);

    mIsPlaying = isPlaying;
  }

  public void resetSpeepView() {

    if (vVideoSpeed != null) {
      if (mediaType == MultiMediaConstant.VIDEO
          && 1 < mLogicManager.getVideoSpeed()) {
        // setVideoSpeedVisible(View.INVISIBLE);
      } else {
        setVideoSpeedVisible(View.INVISIBLE);
      }
    }

  }

  public void hideOrder() {
    vPOrder.setVisibility(View.INVISIBLE);
  }

  ABRpeatType mRepeat = ABRpeatType.ABREPEAT_TYPE_NONE;

  public void initRepeatAB() {
    mRepeat = ABRpeatType.ABREPEAT_TYPE_NONE;
    // mLogicManager.setABRepeat(mRepeat);
    setRepeatLogoVisible(View.GONE);
  }

  public ABRpeatType getRepeatAB() {
    return mRepeat;
  }

  public boolean setRepeatAB() {
    if (mRepeat == ABRpeatType.ABREPEAT_TYPE_NONE) {
      mRepeat = ABRpeatType.ABREPEAT_TYPE_A;
      mRepeatLogo.setImageResource(R.drawable.toolbar_toprepeat_a);
    } else if (mRepeat == ABRpeatType.ABREPEAT_TYPE_A) {
      mRepeat = ABRpeatType.ABREPEAT_TYPE_B;
      mRepeatLogo.setImageResource(R.drawable.toolbar_toprepeat_ab);
    } else if (mRepeat == ABRpeatType.ABREPEAT_TYPE_B) {
      mRepeat = ABRpeatType.ABREPEAT_TYPE_CANCEL_ALL;
    }
    boolean success = mLogicManager.setABRepeat(mRepeat);
    if (mRepeat == ABRpeatType.ABREPEAT_TYPE_CANCEL_ALL || false == success) {
      mRepeat = ABRpeatType.ABREPEAT_TYPE_NONE;
      setRepeatLogoVisible(View.GONE);
    } else {
      setRepeatLogoVisible(View.VISIBLE);
    }
    return success;
  }

  public boolean isInABRepeat() {
    boolean isABRepeat = false;

    if (ABRpeatType.ABREPEAT_TYPE_NONE != getRepeatAB()) {
      isABRepeat = true;
    }
    return isABRepeat;
  }
//begin by yangxiong for block one marqueeTextView
   public boolean isOverFlowed() {
        switch (mediaType) {
            case MultiMediaConstant.VIDEO:
                if (isOverFlowed(vPFileName)) {
                    return true;
                }
                break;
            case MultiMediaConstant.AUDIO:
                if (isOverFlowed(vPFileName)) {
                    return true;
                }
                break;
            case MultiMediaConstant.TEXT:
                if (isOverFlowed(vPFileName)) {
                    return true;
                }
                break;
            case MultiMediaConstant.PHOTO:
            case MultiMediaConstant.THRD_PHOTO:
                if (isOverFlowed(vPFileName)) {
                    return true;
                }
                break;
            default:
                return false;
        }
        return false;
    }

    private boolean isOverFlowed(TextView tv) {
      if (tv == null) return false;//add by yx for 71550
        int availableWidth = tv.getWidth( ) - tv.getPaddingLeft( ) - tv.getPaddingRight( );
        Paint textViewPaint = tv.getPaint( );
        float textWidth = textViewPaint.measureText(tv.getText( ).toString( ));
        if (textWidth > availableWidth) {
            return true;
        } else {
            return false;
        }
    }
     //end yangxiong for block one marqueeTextView
}
