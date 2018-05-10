
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photo.IThrdListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photoimpl.MPlayback;
import com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photoimpl.PhotoManager;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;

import android.os.SystemProperties;

import com.mediatek.wwtv.mediaplayer.setting.SettingActivity;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.mediaplayer.util.Util;

//import com.mediatek.wwtv.mediaplayer.capturelogo.CaptureLogoActivity;

public class Photo3DPlayActivity extends MediaPlayActivity {

  private static final String TAG = "Photo3DPlayActivity";

  private static final int MESSAGE_PLAY = 0;

  private static final int MESSAGE_POPHIDE = 1;

  private static final int MESSAGE_FILE_NOT_SURPORT = 2;

  private static final int MESSAGE_HIDE_MENUDIALOG = 3;
  private static final int MESSAGE_3D_MODE = 1003;

  private static final int MESSAGE_FINISH = 4;

  private static final int MESSAGE_POPSHOWDEL = 10000;

  public static int mDelayedTime = DELAYED_SHORT/* DELAYED_MIDDLE */;

  private LinearLayout vLayout;

  private MenuListView menuDialog;

  private Resources mResources;

  private int playMode;

  private int isRepeatMode = 0;

  private SharedPreferences mPreferences;

  public static final String PHOTO_FRAME_PATH = "photoframe";

  public static final String PHOTO_FRAME_KEY = "photo";

  private int mImageSource = 0;

  private boolean isStop = false;

  private boolean mLastPlayState = true;

  private MtkTvConfig mConfig;
  private SundryDialog sundryDialog;

  private final OnItemClickListener mListener = new OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {

      isRepeatMode = parent.getChildCount();
      TextView tvTextView = (TextView) view
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();
      controlState(content);
    }
  };

  private class SleepDialog extends Dialog {

    private TextView mSleepTime;

    private SleepDialog(Context context, int theme) {
      super(context, theme);
    }

    public SleepDialog(Context context) {
      this(context, R.style.videodialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.mmp_framephoto_sleep);
      setDialogPosition();
    }

    private void setDialogPosition() {
      WindowManager m = getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.x = -(int) (ScreenConstant.SCREEN_WIDTH * 0.4);
      lp.y = (int) (ScreenConstant.SCREEN_HEIGHT * 0.25);
      window.setAttributes(lp);

    }

    public void setSleepTime() {

    }

  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      mLastPlayState = true;
      if (mHandler.hasMessages(MESSAGE_PLAY))
      {
        mHandler.removeMessages(MESSAGE_PLAY);
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
    }

    @Override
    public void pause() {
      mLastPlayState = false;
      mHandler.removeMessages(MESSAGE_PLAY);

    }

  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_mediaplay);
    mConfig = MtkTvConfig.getInstance();

    mConfig.setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_IMG, 1);

    mPreferences = getSharedPreferences(PHOTO_FRAME_PATH, MODE_PRIVATE);
    // mMenuDataContainer = MenuDataContainer.getInstance(this);
    findView();
    getIntentData();
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      if (autoTest(Const.FILTER_IMAGE, MultiFilesManager.CONTENT_PHOTO)) {
        mImageSource = ConstPhoto.LOCAL;
        playMode = 0;

      }
    }
    showPopUpWindow(vLayout);
    mResources = getResources();
    mControlView.setRepeatVisibility(Const.FILTER_IMAGE);// wait confirm
    initShowPhoto();
    mLogicManager.reset3Dsetting();
    Util.LogLife(TAG, "onCreate");
  }

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {

        case MESSAGE_PLAY:
          dismissMenuDialog();
          dismissNotSupprot();
          finishSetting();
          removeFeatureMessage();
          reSetController();
          // Util.reset3D(getApplicationContext());
          mLogicManager.playThrdPhoto(Const.MANUALNEXT);
          setControlView();
          break;
        case MESSAGE_POPHIDE:
          MtkLog.d(TAG, "MESSAGE_POPHIDE:" + msg.what);
          hideController();
          break;
        case MESSAGE_FINISH:
          if (menuDialog != null && menuDialog.isShowing()) {
            menuDialog.dismiss();
          }
          removeMessage();
          finishSetting();
          mLogicManager.closeThrdPhoto();
          finish();
          break;
        case MESSAGE_HIDE_MENUDIALOG:
          dismissMenuDialog();
          break;
        case MESSAGE_3D_MODE:
          MtkLog.i(TAG, "MESSAGE_3D_MODE:" + MESSAGE_3D_MODE);
          mLogicManager.setPicSetting();
          break;
        case MESSAGE_FILE_NOT_SURPORT:
          dismissMenuDialog();
          featureNotWork(Photo3DPlayActivity.this.getResources()
              .getString(R.string.mmp_photo_type_notsupport));
          reSetController();
          setControlView();
          break;
        default:
          break;
      }
    }

  };

  /**
   * find view
   */
  private void findView() {
    vLayout = (LinearLayout) findViewById(R.id.mmp_mediaplay);
    vLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    getPopView(R.layout.mmp_popupphoto, MultiMediaConstant.THRD_PHOTO,
        mControlImp);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    isStop = false;
    isSetPicture = false;
    if (isBackFromCapture) {
      if (mLastPlayState) {
        mControlView.play();
      }
      isBackFromCapture = false;
    }
    Util.LogLife(TAG, "onResume");
  }

  private void getIntentData() {
    Bundle bundle = getIntent().getExtras();
    if (null != bundle) {
      playMode = bundle.getInt("PlayMode");
    }

    mImageSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();

    switch (mImageSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mImageSource = ConstPhoto.LOCAL;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mImageSource = ConstPhoto.SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mImageSource = ConstPhoto.DLNA;
        break;
      default:
        break;
    }
  }

  private final IThrdListener thrdPhotoEventlistener = new IThrdListener() {

    @Override
    public void onPlayFailed() {
      MtkLog.d(TAG, "play failed--");
      isNotSupport = true;

      if (mHandler.hasMessages(MESSAGE_FILE_NOT_SURPORT)) {
        mHandler.removeMessages(MESSAGE_FILE_NOT_SURPORT);
      }
      mHandler.sendEmptyMessage(MESSAGE_FILE_NOT_SURPORT);
      if (mControlView != null && mControlView.isPlaying()) {

        mHandler.removeMessages(MESSAGE_PLAY);
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);/* 5000 */
      }

    }

    @Override
    public void onCompleted() {
      MtkLog.d(TAG, "onCompleted--");

      vLayout.post(mtPlayFinishDely);
    }

    @Override
    public void onCloseFailed() {
      MtkLog.d(TAG, "close failed--");
    }

    @Override
    public void playDone() {
      // Util.printAutoTestImageResult("PLAY SUCCESS");
      if (!isStop && mControlView.isPlaying()) {
        mHandler.removeMessages(MESSAGE_PLAY);
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
        System.out.println("-shuming--play-path---" + "--" + new Exception().getStackTrace()[0]);
      }
    }

    @Override
    public void onOpenFailed() {
      // Util.printAutoTestImageResult("PLAY FAILED");
      MtkLog.d(TAG, "open failed--");

    }

    @Override
    public void decodeSuccess() {
      isNotSupport = false;
      // mLogicManager.setPicSetting();
      mLogicManager.reset3Dsetting();
      MtkLog.d(TAG, "decodeSuccess 100");
      mHandler.sendEmptyMessageDelayed(MESSAGE_3D_MODE, 100);
      mHandler.sendEmptyMessage(MESSAGE_HIDE_MENUDIALOG);

    }
  };

  private final PlayFinishDelay mtPlayFinishDely = new PlayFinishDelay();

  private class PlayFinishDelay implements Runnable {
    @Override
    public void run() {
      // if (mLastPlayState) {
      mHandler.sendEmptyMessage(MESSAGE_FINISH);
      // }
    }
  }

  /**
   * Initialize photo play
   */
  private void initShowPhoto() {
    mLogicManager = LogicManager.getInstance(this);
    mLogicManager.setImageSource(mImageSource);
    mLogicManager.initThrdPhoto(this);
    mLogicManager.setThrdPhotoCompelet(thrdPhotoEventlistener);
    mLogicManager.playThrdPhoto(Const.CURRENTPLAY);
    setControlView();

  }

  /**
   * Set control bar info
   */
  private void setControlView() {

    if (mControlView != null) {
      // TODO remove
      // if(isNotSupport){
      mControlView.setZoomEmpty();
      // }
      mControlView.setRepeat(Const.FILTER_IMAGE);
      mControlView.setFileName(mLogicManager
          .getCurrentFileName(Const.FILTER_IMAGE));
      mControlView.setFilePosition(mLogicManager.getThrdPhotoPageSize());
    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setPhotoView();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.d(TAG, "onKeyDown keyCode:" + keyCode);
    // temp solution
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    switch (keyCode) {
      case KeyMap.KEYCODE_MENU: {
        reSetController();
        showDialog();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_SEFFECT: {
        if (mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED) {
          sundryDialog = new SundryDialog(this, 2);
          sundryDialog.show();
        }
        return true;
      }
      case KeyMap.KEYCODE_DPAD_CENTER:
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
        // mLogicManager.setPicSetting();
        if (isValid()) {
          return super.onKeyDown(keyCode, event);
        } else {
          return true;
        }
      }
    // case KeyMap.KEYCODE_MTKIR_CHDN:
	case KeyMap.KEYCODE_MTKIR_PREVIOUS:
    case KeyMap.KEYCODE_DPAD_UP: {
        if (isValid()) {
          removeMessage();
          reSetController();
          removeFeatureMessage();
          // mLogicManager.reset3Dsetting();
        mLogicManager.playThrdPhoto(Const.MANUALNEXT);
          setControlView();

        }
        return true;
      }
    // case KeyMap.KEYCODE_MTKIR_CHUP:
	case KeyMap.KEYCODE_MTKIR_NEXT:
    case KeyMap.KEYCODE_DPAD_DOWN: {
        if (isValid()) {
          removeMessage();
          reSetController();
          removeFeatureMessage();
          // mLogicManager.reset3Dsetting();
		mLogicManager.playThrdPhoto(Const.MANUALPRE);
          setControlView();

        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        onRepeat();
        updateInfoView();
        return true;
        // break;
      }
      case KeyMap.KEYCODE_MTKIR_RECORD: {
        featureNotWork(getString(R.string.mmp_featue_notsupport));
        return true;

        /*
         * if (isNotSupport){ break; } else
         * if(MenuConfigManager.getInstance(this).getDefault(MenuConfigManager.VIDEO_3D_MODE) != 0){
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); }else{ reSetController();
         * if(mControlView.isPlaying()){ //mControlView.pause(); mControlView.onCapture();
         * mControlView.setPlayIcon(View.INVISIBLE); mLastPlayState = true; } Intent intent = new
         * Intent(this, CaptureLogoActivity.class); intent.putExtra(CaptureLogoActivity.FROM_MMP,
         * CaptureLogoActivity.MMP_VIDEO); startActivity(intent); isBackFromCapture = true; return
         * true; }
         */
      }
      /*case KeyMap.KEYCODE_MTKIR_YELLOW: {
        // MtkLog.i(TAG, "Yellow pressed to set3D mode");
        // mLogicManager.setPicSetting();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_GREEN: {
        reSetController();
        // add by shuming for fix CR DTV00410753
        // switchDuration();
        if (mControlView.isPlaying()) {
          switchDuration();
        }
        return true;
      }*/
      case KeyMap.KEYCODE_VOLUME_DOWN:
      case KeyMap.KEYCODE_VOLUME_UP:
      case KeyMap.KEYCODE_MTKIR_MUTE: {
        if (null != mLogicManager.getAudioPlaybackService()) {
          currentVolume = mLogicManager.getVolume();
          maxVolume = mLogicManager.getMaxVolume();
          break;
        } else {
          return true;
        }
      }
      case KeyMap.KEYCODE_MTKIR_STOP:
      case KeyMap.KEYCODE_BACK:
        removeMessage();
        mLogicManager.closeThrdPhoto();
        finishSetting();
        finish();
        return true;
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        // removeMessage();
        // mLogicManager.closeThrdPhoto();
        // finish();
        return super.onKeyDown(keyCode, event);
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  /**
   * Switch photo play duration
   */
  private void switchDuration() {
    if (mDelayedTime == DELAYED_SHORT) {
      mDelayedTime = DELAYED_MIDDLE;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_medium));
    } else if (mDelayedTime == DELAYED_MIDDLE) {
      mDelayedTime = DELAYED_LONG;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_long));
    } else {
      mDelayedTime = DELAYED_SHORT;
      mControlView.setPhotoTimeType(getString(R.string.mmp_menu_short));
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void hideControllerDelay() {
    mHandler.removeMessages(MESSAGE_POPHIDE);
    mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, MESSAGE_POPSHOWDEL);
  }

  /**
   * Menu right handler
   */
  private final MenuListView.MenuDismissCallBack mCallBack
  = new MenuListView.MenuDismissCallBack() {

    @Override
    public void onDismiss() {
      hideController();
    }

    @Override
    public void sendMessage() {
    }

    @Override
    public void noDismissPannel() {

    };
  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    if (menuDialog != null && menuDialog.isShowing()) {
      menuDialog.dismiss();
    }
    if (!isSetPicture) {
      removeMessage();
      isStop = true;
    }
    super.onStop();
    Util.LogLife(TAG, "onStop");
  };

  /**
   * Remove handler message
   */
  private void removeMessage() {
    mHandler.removeMessages(MESSAGE_PLAY);
    mHandler.removeMessages(MESSAGE_POPHIDE);
  }

  @Override
  protected void onPause() {
    super.onPause();
    Util.LogLife(TAG, "onPause");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    mConfig.setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_IMG, 0);
    mLogicManager.closeThrdPhoto();
    Util.reset3D(getApplicationContext());
    super.onDestroy();
    Util.LogLife(TAG, "onDestroy");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBackPressed() {

  }

  /**
   * Menu item click callback
   *
   * @param content
   *            the click item content value
   */
  protected void controlState(String content) {

    if (content.equals(mResources
        .getString(R.string.mmp_frame_photo_sleeptime))) {
      if (null != menuDialog && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
      SleepDialog dialog = new SleepDialog(this);
      dialog.show();
      dialog.setSleepTime();
      return;
    }

    if (content.equals(mResources.getString(R.string.mmp_menu_pause))) {
      showController();
      hideControllerDelay();
      mControlView.setMediaPlayState();
      MtkLog.d(TAG, "content:-----" + content);
      menuDialog.setList(0, mResources.getString(R.string.mmp_menu_play),
          false, 3, mResources.getString(R.string.mmp_menu_rotate),
          false, 4, mResources.getString(R.string.mmp_menu_zoom),
          true);
      menuDialog.setItemEnabled(3, false);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play))) {
      showController();
      hideControllerDelay();
      mControlView.setMediaPlayState();
      menuDialog.setList(0,
          mResources.getString(R.string.mmp_menu_pause), false, 3,
          mResources.getString(R.string.mmp_menu_duration), true, 4,
          mResources.getString(R.string.mmp_menu_effect), true);
      menuDialog.setItemEnabled(3, true);
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_none))
        && (isRepeatMode == 3)) {
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ONE);

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      mControlView.setRepeatAll();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ALL);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleon))) {
      mControlView.setShuffleVisble(View.VISIBLE);
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_ON);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_shuffleoff))) {
      mControlView.setShuffleVisble(View.INVISIBLE);
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_OFF);
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_short))) {
      mDelayedTime = DELAYED_SHORT;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_medium))) {
      mDelayedTime = DELAYED_MIDDLE;
      mControlView.setPhotoTimeType(content);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_long))) {
      mDelayedTime = DELAYED_LONG;
      mControlView.setPhotoTimeType(content);
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_none))) {

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      menuDialog.dismiss();
      showinfoview(MultiMediaConstant.PHOTO);
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_frame))) {
      mPhotoFramePath = mLogicManager.getCurrentPath(Const.FILTER_IMAGE);
      mPhotoFrameSource = mImageSource;
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_pic_setting))) {
      if (null != menuDialog && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
      if (mControlView != null && mControlView.isShowed()) {
        hideController();
      }
      isSetPicture = true;
      Intent intent = new Intent(MmpConst.INTENT_SETTING);
      intent.putExtra("fromwhere", 1);
      startActivity(intent);
    }
  }

  /**
   * Get photo play duration
   *
   * @return int duration
   */
  public static int getDelayedTime() {
    return mDelayedTime;
  }

  /**
   * Show menu dialog
   */
  private void showDialog() {

    mHandler.removeMessages(MESSAGE_POPHIDE);
    menuDialog = new MenuListView(Photo3DPlayActivity.this, GetDataImp
        .getInstance().getComMenu(Photo3DPlayActivity.this,
            R.array.mmp_menu_3d_photoplaylist,
            R.array.mmp_menu_3d_photoplaylist_enable,
            R.array.mmp_menu_3d_photoplaylist_hasnext), mListener,
        mCallBack);
    menuDialog.setItemEnabled(7, !isNotSupport);

    if (null != mControlView) {

      if (mControlView.isPlaying()) {
        menuDialog.setList(0,
            mResources.getString(R.string.mmp_menu_pause), false,
            3, mResources.getString(R.string.mmp_menu_duration),
            true, 4,
            mResources.getString(R.string.mmp_menu_effect), true);
        menuDialog.setItemEnabled(3, true);
      } else {
        menuDialog.setList(0,
            mResources.getString(R.string.mmp_menu_play), false, 3,
            mResources.getString(R.string.mmp_menu_rotate), false,
            4, mResources.getString(R.string.mmp_menu_zoom), true);
      }
    }
    menuDialog.setMediaType(sMediaType);
    menuDialog.show();

  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (mConfig != null)
      mConfig.setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_IMG, 0);
    if (mLogicManager != null)
      mLogicManager.closeThrdPhoto();
    Util.reset3D(getApplicationContext());
  }
}
