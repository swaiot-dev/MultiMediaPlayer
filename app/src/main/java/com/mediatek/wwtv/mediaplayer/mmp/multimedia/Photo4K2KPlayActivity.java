
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.mediatek.wwtv.mediaplayer.mmp.SkyPreviewListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectView;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectView.ImagePlay;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.SleepDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.EffectView.iCompleteListener;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager.ImageLoad;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.jni.PhotoRender;

//import com.mediatek.wwtv.mediaplayer.capturelogo.CaptureLogoActivity;

public class Photo4K2KPlayActivity extends SkyMediaPlayActivity {

  private static final String TAG = "Photo4K2KPlayActivity";

  // SKY luojie add 20171218 for add choose menu begin
  public static final int ACTIVITY_TYPE_PHOTO_4K2K = 5;
  // SKY luojie add 20171218 for add choose menu end

  private static final int MESSAGE_PLAY = 0;

  private static final int MESSAGE_POPHIDE = 1;

  private static final int MESSAGE_PHOTOMODE = 2;

  private static final int MESSAGE_HIDDLE_MESSAGE = 3;

  private static final int MESSAGE_HIDDLE_FRAME = 4;

  private static final int MESSAGE_NO_PHOTO_FRAME = 5;
  private static final int MESSAGE_DECODE_FAILURE = 6;
  private static final int FRAME_ONEPHOTO_MODE = 7;
  private static final int FRAME_MODE_HIDE = 8;

  private static final int MESSAGE_POPSHOWDEL = 10000;

  public static final int DELAYED_FRAME = 1000;
  private boolean isPhotoActivityLiving = true;

  public static int mDelayedTime = DELAYED_SHORT;

  private static int oriention = 0;

  private static int newOriention = 0;

  private EffectView vShowView;

  private LinearLayout vLayout;

  private MenuListView menuDialog;

  private MenuListView menuDialogSleepTime;

  private ImageManager mImageManager;

  private Resources mResources;

  //SKY luojie 20180111 modify begin
  private int playMode = NORMAL_MODE;
  //SKY luojie 20180111 modify end

  private PhotoUtil mCurBitmap;

  private int isRepeatMode = 0;

  private int menu_repeatmode = 0;// add by haixia

  private SharedPreferences mPreferences;

  public static final String PHOTO_FRAME_PATH = "photoframe";

  public static final String PHOTO_FRAME_KEY = "photo";
  public static final String PLAY_MODE = "PlayMode";

  private int mImageSource = 0;

  private boolean isZoom = false;

  private boolean isStop = false;
  private boolean isPause = false;
  private boolean photoPlayStatus = false;

  public static final int NORMAL_MODE = 0;
  public static final int FRAME_ONE_PHOTO_MODE = 1;
  public static final int FRAME_ALL_PHOTO_MODE = 2;

  private boolean isBackFlag;

  private boolean is4K2KFlag;

  private PhotoRender photoRender;

  private SleepDialog mSleepdialog;
  private SundryDialog sundryDialog;
  private Thread mRotateThread;

  private final iCompleteListener mCompleteListener = new iCompleteListener() {

    @Override
    public void drawEnd() {
      // TODO Auto-generated method stub
      handBack();
      handlePhotoPlayEnd();
    }

  };

  private final OnItemClickListener mListener = new OnItemClickListener() {

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
        long id) {

      isRepeatMode = parent.getChildCount();
      TextView tvTextView = (TextView) view
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();
      if(null!=mCurBitmap) {
        controlState(content);
      }
    }
  };

  private class SleepingDialog extends Dialog {

    private TextView mSleepTime;
    private Context msContext;
    private Timer timer;
    private TimerTask task;
    private int timeInt = 5;

    private SleepingDialog(Context context, int theme) {
      super(context, theme);
      this.msContext = context;
    }

    public SleepingDialog(Context context) {
      this(context, R.style.dialog);
      this.msContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.mmp_framephoto_sleep);
      setDialogPosition();
      initData();
      timerTask();
    }

    private void initData() {
      mSleepTime = (TextView) findViewById(R.id.mmp_framephoto_sleeptime);

    }

    private void timerTask() {
      timer = new Timer("Chang");
      task = new TimerTask() {
        @Override
        public void run() {
          if (timeInt <= 0) {
            SleepingDialog.this.dismiss();
          }
          timeInt--;
        }
      };
      timer.schedule(task, 10, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

      // temp solution
      if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
        keyCode = event.getScanCode();
      }

      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_SLEEP:
          Log.i(TAG, "SLEEP");
          if (isValid()) {
            Log.i(TAG, "SLEEP VALIDE");
            setSleepTime();
          }
          return true;
        case KeyMap.KEYCODE_BACK:
          // case KeyMap.KEYCODE_MTKIR_ANGLE:
        case KeyMap.KEYCODE_MTKIR_GUIDE:
          if (null != msContext && msContext instanceof Photo4K2KPlayActivity) {
            ((Photo4K2KPlayActivity) msContext).onKeyDown(keyCode, event);
          }
          this.dismiss();
        case KeyMap.KEYCODE_MENU:
          if (null != msContext && msContext instanceof Photo4K2KPlayActivity) {
            ((Photo4K2KPlayActivity) msContext).onKeyDown(keyCode, event);
          }
        default:
          return true;
      }
    }

    private void setDialogPosition() {
      WindowManager m = getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();
      Window window = getWindow();
      WindowManager.LayoutParams lp = window.getAttributes();
      lp.x = -(int) (ScreenConstant.SCREEN_WIDTH * 0.35);
      lp.y = (int) (ScreenConstant.SCREEN_HEIGHT * 0.25);
      window.setAttributes(lp);

    }

  }

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      // fix CR DTV00375799
      if (isPause == true) {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
        isPause = false;
      } else
      {
        mHandler.sendEmptyMessage(MESSAGE_PLAY);
      }
    }

    @Override
    public void pause() {
      isPause = true;
      mHandler.removeMessages(MESSAGE_PLAY);
    }

  };
  private final ImageLoad mLoad = new ImageLoad() {

    @Override
    public void imageLoad(PhotoUtil bitmap) {
      loadImageDone(bitmap);
    }
  };

  private final ImagePlay mPlay = new ImagePlay() {

    @Override
    public void playDone() {
      MtkLog.d(TAG, "ImagePlay playDone ~");
      isNotSupport = false;
      if (vShowView != null) {
        vShowView.recycleLastBitmap();
      }
      Log.i(TAG, "isStop:" + isStop + "--mControlView:" + mControlView
          + "--isNotSupport:" + isNotSupport);
      if (mControlView != null) {
        Log.i(TAG, "--isPlaying:" + mControlView.isPlaying());
      }
      if (!isStop && mControlView != null && mControlView.isPlaying()) {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
      }
    }

    @Override
    public void playError() {

      MtkLog.d(TAG, "ImagePlay playError ~");
      isNotSupport = true;
      if (isStop) {
        return;
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);

    }

  };

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_mediaplay);
    //begin by yx for talkback
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(TALKBACK_BRDCAST_ACTION);
      registerReceiver(mTalkBackReceiver,intentFilter);
    //end by yx for talkback
    // mPreferences = getSharedPreferences(PHOTO_FRAME_PATH, MODE_PRIVATE);
    findView();
    getIntentData();
    // add by keke for fix DTV00380644
    mControlView.setRepeatVisibility(Const.FILTER_IMAGE);

    // SKY luojie add 20171218 for add Channel function begin
    boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(!isEnterFromDesktop) {
      initShowPhoto();
    }
    // SKY luojie add 20171218 for add Channel function end

    setRepeatMode();
    Util.LogLife(TAG, "onCreate");

    // SKY luojie add 20171218 for add choose menu begin
    Intent intent = getIntent();
    mHideChooseMenu = intent.getBooleanExtra(KEY_EXTRA_HIDE_CHOOSE_MENU, false);
    mFirstPlayFilePath =  intent.getStringExtra(KEY_EXTRA_FILE_PATH);

    setupFilesManager();
    // SKY luojie add 20171218 for add choose menu end
  }

  protected void setPreviewListDialogParams() {
    mPreviewListDialog.setFilesManager(mFilesManager);
    mPreviewListDialog.setOnLoadedFilesListener(mOnLoadedFilesListener);
    mPreviewListDialog.setTheFirstFilePath(mFirstPlayFilePath);
    mPreviewListDialog.changeContentType(MultiMediaConstant.PHOTO);
    mPreviewListDialog.setActivityType(ACTIVITY_TYPE_PHOTO_4K2K);
  }
    /**
     * add by yx
     * reset photo scale size
     */
    private void resetZoom() {
        vShowView.setmZoomScale();//add by yx for reset photo size when pause
        //int size = LogicManager.getInstance(getApplicationContext( )).getPicCurZoom( );
        //changed by zhangqing
        float size = LogicManager.getInstance(getApplicationContext()).getPicCurZoom();
        if (size != (float)ConstPhoto.ZOOM_1X) {
           MtkLog.d(TAG, "size != ConstPhoto.ZOOM_1X!");

            if (mCurBitmap !=null && null != mCurBitmap.getMovie( )) {
                vShowView.setMultiple(ConstPhoto.ZOOM_1X);
            } else {
                mControlView.setPhotoZoom(mResources.getString(R.string.mmp_menu_1x));
                vShowView.setRes(mCurBitmap);
                vShowView.setMultiple(ConstPhoto.ZOOM_1X);
                vShowView.setType(ConstPhoto.ZOOMOUT);
                vShowView.run( );
            }
        }
    }

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case MESSAGE_PLAY:
         resetZoom();//add by yx for reset size
          mCurBitmap = null;
          if (playMode == FRAME_ONE_PHOTO_MODE) {
            mImageManager.load(Const.CURRENTPLAY);
          }
          else {
            // vShowView.setInterrupted(true);
            mImageManager.load(Const.AUTOPLAY);
          }
          break;
        case MESSAGE_POPHIDE:
          MtkLog.d(TAG, "MESSAGE_POPHIDE:" + msg.what);
          if (menuDialog != null && menuDialog.isShowing()) {
            if (mHandler.hasMessages(MESSAGE_POPHIDE)) {
              mHandler.removeMessages(MESSAGE_POPHIDE);
            }
            sendEmptyMessageDelayed(MESSAGE_POPHIDE, 3000);
            break;
          }
          hideController();
          break;
        case MESSAGE_PHOTOMODE:
          break;
        case MESSAGE_HIDDLE_MESSAGE: {
          dismissNotSupprot();
          break;
        }
        case MESSAGE_HIDDLE_FRAME: {
          mResources = Photo4K2KPlayActivity.this.getResources();
          String photoFrame = mResources
              .getString(R.string.mmp_menu_photo_frame);
          if (mTipsDialog != null && mTipsDialog.isShowing()
              && mTipsDialog.getTitle().equals(photoFrame)) {
            mTipsDialog.dismiss();
          }
          dismissNotSupprot();
          if (isNotSupport == true && isPhotoActivityLiving == true) {
            try {
              featureNotWork(Photo4K2KPlayActivity.this.getResources()
                  .getString(R.string.mmp_photo_type_notsupport));
            } catch (Exception ex) {
              ex.printStackTrace();
            }

            vShowView.clearScreen();
          }
          break;
        }
        case MESSAGE_NO_PHOTO_FRAME: {
          if (mTipsDialog != null && mTipsDialog.isShowing()) {
            mTipsDialog.dismiss();
          }
          if (isPhotoActivityLiving == true) {
            featureNotWork(Photo4K2KPlayActivity.this.getResources().getString(
                R.string.mmp_photo_type_notsupport));
          }
          break;
        }
        case MESSAGE_DECODE_FAILURE:
          if (!isStop) {
            featureNotWork(Photo4K2KPlayActivity.this.getResources()
                .getString(R.string.mmp_photo_type_notsupport));
          }
          vShowView.clearScreen();
          break;
        case FRAME_ONEPHOTO_MODE:
          if (null == mCurBitmap) {
            showPhotoFrameInfo(getString(R.string.mmp_toast_no_photoframe));
            if (null != mPhotoFramePath && mPhotoFramePath.length() > 0) {
              mHandler.sendEmptyMessageDelayed(MESSAGE_NO_PHOTO_FRAME,
                  DELAYED_FRAME);
            }
            return;
          }
          loadImageDone(mCurBitmap);
          return;
        case FRAME_MODE_HIDE:
          if (mSleepDialog != null) {
            if (mSleepDialog.isShowing()) {
              mSleepDialog.dismiss();
            }
          }
          return;
        default:
          break;
      }
    }

  };

  private SleepDialog mSleepDialog;

  private void setSleepTime() {
    // TODO Auto-generated method stub
    if (mSleepDialog == null) {
      Log.i(TAG, "mSleepDialog == NULL");
      mSleepDialog = new SleepDialog(this);
    }
    mHandler.removeMessages(FRAME_MODE_HIDE);
    mSleepDialog.show();
    mHandler.sendEmptyMessageDelayed(FRAME_MODE_HIDE, 5000);
    mSleepDialog.updateValue(true);
  }

  /**
   *  find view
   */
  private void findView() {
    vShowView = new EffectView();
    vShowView.setPlayLisenter(mPlay);
    vLayout = (LinearLayout) findViewById(R.id.mmp_mediaplay);
    vLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    getPopView(R.layout.mmp_popupphoto, MultiMediaConstant.PHOTO,
        mControlImp);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (isBackFromCapture) {
      showController();

      if (photoPlayStatus) {
        mControlView.play();
        photoPlayStatus = false;
      } else
      {
        mControlView.setPauseIcon(View.VISIBLE);
      }
      isBackFromCapture = false;
    } else {
      if (mImageManager != null && playMode != FRAME_ONE_PHOTO_MODE) {
        mImageManager.load(Const.CURRENTPLAY);
      }
    }

    isStop = false;
    Util.LogLife(TAG, "onResume");
  }

  @Override
  protected void onPause() {
    if (isBackFlag) {
      Util.LogResRelease("onPause 1 deinitPhotoPlay");
      photoRender.deinitPhotoPlay();
      is4K2KFlag = false;
    }
    Log.i(TAG, "xiuqin test");
    if (Util.isNeedEndPhotoPlayWhenPause() && !isBackFlag) {
      Util.LogResRelease("onPause 2 deinitPhotoPlay");
      photoRender.deinitPhotoPlay();
      is4K2KFlag = false;
    } else {
      Util.setEndPhotoPlayWhenPause(true);
    }

    if (playMode == FRAME_ALL_PHOTO_MODE)
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, menu_repeatmode);

    super.onPause();
    Util.LogLife(TAG, "onPause");
  }

  private void getIntentData() {
    Bundle bundle = getIntent().getExtras();
    if (null != bundle) {
      playMode = bundle.getInt(PLAY_MODE);
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

    // SKY luojie add 20171218 for add Channel function begin
    boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
    if(isEnterFromDesktop) {
      mImageSource = ConstPhoto.LOCAL;
    }
    // SKY luojie add 20171218 for add Channel function end
  }

  private final OnPhotoCompletedListener mPhotoCompleteListener = new OnPhotoCompletedListener() {

    @Override
    public void onComplete() {
      playToEnd();
    }
  };

  private void playToEnd() {
    if (null != mImageManager) {
      mImageManager.finish();
    }
    vShowView.setCompleteListener(mCompleteListener);
    vShowView.setInterrupted(true);
  }

  private final OnPhotoDecodeListener mPhotoDecodeListener = new OnPhotoDecodeListener() {

    @Override
    public void onDecodeFailure() {
      Log.i(TAG, "onDecodeFailure isNotSupport:" + isNotSupport + "--isStop:" + isStop);
      isNotSupport = true;
      if (isStop) {
        return;
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);

    }

    @Override
    public void onDecodeSuccess() {
      isNotSupport = false;
    }
  };

  /**
   * Initialize photo play
   */
  private void initShowPhoto() {
    mResources = Photo4K2KPlayActivity.this.getResources();
    mLogicManager = LogicManager.getInstance(this);

    vShowView.setRotate(0);

    Display display = getWindowManager().getDefaultDisplay();
    mLogicManager.initPhotoFor4K2K(display, vShowView);
    mLogicManager.initRotate();
    photoRender = new PhotoRender(0);

    vShowView.setRender(photoRender);
    if (photoRender.initPhotoPlay() != 0)
    {
      MtkLog.d(TAG, "initShowPhoto 4k2k native init fail~");
      isNotSupport = false;
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);
      if (null != mImageManager) {
        mImageManager.finish();
      }
      mHandler.postDelayed(new Runnable() {
        @Override
        public void run() {
          finish();
        }
      }, 1000);

      return;
    }

    int dw = PhotoRender.is4KPanel() ? 3840 : 1920;
    int dh = PhotoRender.is4KPanel() ? 2160 : 1080;
    vShowView.setWindow(dw, dh);

    is4K2KFlag = true;
    isBackFlag = false;
    mLogicManager.setPhotoCompleteListener(mPhotoCompleteListener);
    if (!(playMode == FRAME_ONE_PHOTO_MODE)) {
      mLogicManager.setPhotoDecodeListener(mPhotoDecodeListener);
    }
    mImageManager = ImageManager.getInstance();
    // add by xiaojie fix cr DTV00390950
    if (!(playMode == FRAME_ONE_PHOTO_MODE)) {
      mImageManager.setImageLoad(mLoad, mLogicManager);
    }

    mControlView.setPhotoAnimationEffect(vShowView.getEffectValue());
    mCurBitmap = null;

    if (playMode == NORMAL_MODE) {
      showPopUpWindow(vLayout);
      hideControllerDelay();
      setControlView();
    } else if (playMode == FRAME_ONE_PHOTO_MODE) {
      new Thread(new Runnable() {
        @Override
        public void run() {
          // TODO Auto-generated method stub
          /*
           * String playPath = getSharedPreferences(PHOTO_FRAME_PATH,
           * MODE_PRIVATE).getString(PHOTO_FRAME_KEY, "");
           */
          mCurBitmap = mLogicManager.transfBitmap(mPhotoFramePath, mPhotoFrameSource);
          mHandler.sendEmptyMessage(FRAME_ONEPHOTO_MODE);
        }
      }).start();
      return;
    } else {
      mImageSource = ConstPhoto.LOCAL;
      menu_repeatmode = mLogicManager.getRepeatModel(Const.FILTER_IMAGE);// add by haixia for fix CR
                                                                         // DTV00379219
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ALL);
      mLogicManager.setShuffle(Const.FILTER_IMAGE, Const.SHUFFLE_OFF);
    }
    mLogicManager.setImageSource(mImageSource);

  }

  private final OnDismissListener mDismissListener = new OnDismissListener() {

    @Override
    public void onDismiss(DialogInterface dialog) {
      Intent intent = new Intent(Photo4K2KPlayActivity.this,
          MtkFilesGridActivity.class);
      intent.putExtra(MultiMediaConstant.MEDIAKEY, MultiFilesManager
          .getInstance(Photo4K2KPlayActivity.this).getContentType());
      startActivity(intent);
      finish();
    }
  };

  /**
   * Set control bar info
   */
  private void setControlView() {
    if (mControlView != null) {
      // TODO remove
      if (isNotSupport) {
        if (null != mControlView) {
          mControlView.setZoomEmpty();
        }
      } else {
        if (null != mControlView) {
          mControlView.setPhotoZoomSize();
        }
      }
      if (null != mControlView) {
        mControlView.setRepeat(Const.FILTER_IMAGE);
      }
      if (null != mControlView) {
        mControlView.setFileName(mLogicManager
            .getCurrentFileName(Const.FILTER_IMAGE));
      }
      if (null != mControlView) {
        mControlView.setFilePosition(mLogicManager.getImagePageSize());
      }
    }
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.setPhotoView();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.d(TAG, "onKeyDown keyCode:" + keyCode);
    mLastKeyDownTime = System.currentTimeMillis();
    // temp solution
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    if (playMode == FRAME_ALL_PHOTO_MODE/* ||isNotSupport */) {

      if (keyCode == KeyMap.KEYCODE_BACK) {
              //begin by yx for set scale = 1 if !=1
        if (null != vShowView) {
            //int size = LogicManager.getInstance(getApplicationContext( )).getPicCurZoom( );
            //changed by zhangqing
            float size = LogicManager.getInstance(getApplicationContext()).getPicCurZoom();
            if (size != (float)ConstPhoto.ZOOM_1X) {
                MtkLog.d(TAG, "onBackPressed-------->size != ConstPhoto.ZOOM_1X!");
                if (mCurBitmap !=null && null != mCurBitmap.getMovie( )) {
                    vShowView.setMultiple(ConstPhoto.ZOOM_1X);
                } else {
                    mControlView.setPhotoZoom(mResources.getString(R.string.mmp_menu_1x));
                    vShowView.setRes(mCurBitmap);
                    vShowView.setMultiple(ConstPhoto.ZOOM_1X);
                    vShowView.setType(ConstPhoto.ZOOMOUT);
                    vShowView.run( );
                }
                return true;
            }
        }
        //end by yx for set scale = 1 if !=1
        finish();
      } else if (keyCode != KeyMap.KEYCODE_MTKIR_SLEEP) {
        return true;
      }
    }
    switch (keyCode) {
        //Begin ==> Modified by yangxiong for solving "add zoom or aspect option of remote keying for photo in MMP"
      case KeyMap.KEYCODE_MTKIR_ZOOM:
      case KeyMap.KEYCODE_MTKIR_ASPECT:
              // mControlView.pause();//fix bug 72194 by yangxiong
              // int size = LogicManager.getInstance(getApplicationContext()).getPicCurZoom( );
              // String content = mResources.getString(R.string.mmp_menu_1x);
              // switch (size) {
              //     case ConstPhoto.ZOOM_1X:
              //         content = mResources.getString(R.string.mmp_menu_2x);
              //         size = ConstPhoto.ZOOM_2X;
              //         break;
              //     case ConstPhoto.ZOOM_2X:
              //         content = mResources.getString(R.string.mmp_menu_4x);
              //         size = ConstPhoto.ZOOM_4X;
              //         break;
              //     case ConstPhoto.ZOOM_4X:
              //         content = mResources.getString(R.string.mmp_menu_1x);
              //         size = ConstPhoto.ZOOM_1X;
              //         break;
              //     default:
              //         content = mResources.getString(R.string.mmp_menu_1x);
              //         size = ConstPhoto.ZOOM_1X;
              //         break;
              // }
              // mControlView.setPhotoZoom(content);
              // if (null!=mCurBitmap) {
              //     if (null != mCurBitmap.getMovie( )) {
              //       vShowView.setMultiple(size);
              //   } else {
              //       vShowView.setRes(mCurBitmap);
              //       vShowView.setMultiple(size);
              //       vShowView.setType(ConstPhoto.ZOOMOUT);
              //       vShowView.run( );
              //   }
              // }

              return true;
      //End ==> Modified by yangxiong for solving "add zoom or aspect option of remote keying for photo in MMP"
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
    // case KeyMap.KEYCODE_MTKIR_CHDN:
    // SKY luojie modify 20171218 for add choose menu begin
	case KeyMap.KEYCODE_MTKIR_NEXT:{
      // begin by yangxiong for fix MTK the function of photo moving
      if (!mControlView.isPlaying( ) && vShowView.isBeyongScreen()) {//add by yx for 71495

        vShowView.setType(ConstPhoto.MOVE_DOWN);
        new Thread(vShowView).start();
        MtkLog.i("yangxiong", "isBeyongScreen");
        return true;
      }
      vShowView.setmZoomScale();//add by yx for reset photo size when pause
      // end by yangxiong for fix MTK the function of photo moving
      if (isValid()) {
        isNotSupport = false;
        reSetController();
        mHandler.removeMessages(MESSAGE_PLAY);
        mCurBitmap = null;
        vShowView.setNotifyOnce();
        mImageManager.load(Const.MANUALNEXT);

      }
      return true;
    }
    case KeyMap.KEYCODE_DPAD_DOWN:
//                if (!isPreviewListDialogShown()) {
//                    hideController();
//                    showPreviewListDialog();
//                    return true;
//                }
                return true;
    // case KeyMap.KEYCODE_MTKIR_CHUP:
	case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
      // begin by yangxiong for fix MTK the function of photo moving
      if (!mControlView.isPlaying( ) && vShowView.isBeyongScreen()) {//add by yx for 71495
        vShowView.setType(ConstPhoto.MOVE_UP);
        new Thread(vShowView).start();
        MtkLog.i("yangxiong", "isBeyongScreen");
        return true;
      }
      // end by yangxiong for fix MTK the function of photo moving
      vShowView.setmZoomScale();//add by yx for reset photo size when pause
      if (isValid()) {
        MtkLog.i(TAG, "KEYCODE_MTKIR_CHUP START");
        isNotSupport = false;
        reSetController();
        mHandler.removeMessages(MESSAGE_PLAY);
        mCurBitmap = null;
        vShowView.setNotifyOnce();
        mImageManager.load(Const.MANUALPRE);
        MtkLog.i(TAG, "KEYCODE_MTKIR_CHUP END");
      }
      return true;
    }
    case KeyMap.KEYCODE_DPAD_UP:
//                if (isPreviewListDialogShown()) {
//                    hidePreviewListDialog();
//                } else {
//                    hideController();
//                    showPreviewListDialog();
//                }
      return true;
      // SKY luojie modify 20171218 for add choose menu end
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        onRepeat();
        updateInfoView();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_RECORD: {

        featureNotWork(getString(R.string.mmp_featue_notsupport));
        return true;
        /*
         * if (isNotSupport){ break; } if (mCurBitmap == null){
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } //
         * mLogicManager.setCapturer(vShowView); if(null != mControlView){
         * if(mControlView.isPlaying()){ mControlView.onCapture();
         * mControlView.setPlayIcon(View.INVISIBLE); photoPlayStatus=true; }else{
         * mControlView.setPauseIcon(View.INVISIBLE); photoPlayStatus=false; } } hideController();
         * Intent intent = new Intent(this, CaptureLogoActivity.class);
         * intent.putExtra(CaptureLogoActivity.FROM_MMP, CaptureLogoActivity.MMP_PHOTO);
         * startActivity(intent); isBackFromCapture = true; return true;
         */
      }
      // SKY luojie modify 20171218 for add choose menu begin
      case KeyMap.KEYCODE_DPAD_RIGHT:
        if (mControlView.isPlaying() || !isValid()) {//add by yx for 71495
          vShowView.setmZoomScale();//add by yx for reset photo size when pause
          return true;
        }
        vShowView.setType(ConstPhoto.MOVE_RIGHT);
        new Thread(vShowView).start();

        return true;
      case KeyMap.KEYCODE_DPAD_LEFT://add by yx for 71495
        if (mControlView.isPlaying() || !isValid()) {
          vShowView.setmZoomScale();//add by yx for reset photo size when pause
          return true;
        }
        vShowView.setType(ConstPhoto.MOVE_LEFT);
        new Thread(vShowView).start();

        return true;
      // SKY luojie modify 20171218 for add choose menu end
      /*case KeyMap.KEYCODE_DPAD_UP:
        if (!isValid()) {
          return true;
        }
        vShowView.setType(ConstPhoto.MOVE_UP);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_DPAD_DOWN:
        if (!isValid()) {
          return true;
        }
        vShowView.setType(ConstPhoto.MOVE_DOWN);
        new Thread(vShowView).start();

        return true;

      case KeyMap.KEYCODE_MTKIR_YELLOW: {
        if (mRotateThread != null && mRotateThread.isAlive()) {
            // toast rotate is alive.
            Util.showToast(getApplicationContext(), "Rotate is alive, please wait.");
            return true;
        }
        MtkLog.i(TAG, "YELLOWKEYEVENT");
        if (!isValid()) {
          return true;
        }
        if (null != mControlView && mControlView.isPlaying()) {
          reSetController();
          switchEffect();
        } else {
          MtkLog.i(TAG, "YELLOWKEYEVENT PAUSE");
          // fix cr DTV00385117 add by lei
          // Modified by Dan for fix bug DTV00390943
          if (isNotSupport || mCurBitmap == null) {
            return true;
          }

          reSetController();

          int size = vShowView.getMultiple();
          size = size * 2;
          if (size > ConstPhoto.ZOOM_4X) {
            size = ConstPhoto.ZOOM_1X;
          }
          int zoom = R.string.mmp_menu_1x;
          switch (size) {
            case ConstPhoto.ZOOM_1X:
              zoom = R.string.mmp_menu_1x;
              break;
            case ConstPhoto.ZOOM_2X:
              zoom = R.string.mmp_menu_2x;
              break;
            case ConstPhoto.ZOOM_4X:
              zoom = R.string.mmp_menu_4x;
              break;
            default:
              break;
          }
          if (null != mControlView) {
            mControlView.setPhotoZoom(mResources.getString(zoom));
          }
          vShowView.setMultiple(size);
          MtkLog.i(TAG, "multisize:" + size);
          vShowView.setType(ConstPhoto.ZOOMOUT);
          new Thread(vShowView).start();
          isZoom = true;
        }

        return true;
      }
      case KeyMap.KEYCODE_MTKIR_GREEN: {
        if (null != mControlView && mControlView.isPlaying()) {
          reSetController();
          switchDuration();
        } else {
          MtkLog.i(TAG, "isNotSupport" + isNotSupport + "---mCurBitmap:" + mCurBitmap);
          if (isNotSupport || mCurBitmap == null) {
            return true;
          }

          reSetController();

          String currPath = mLogicManager.getCurrentPath(Const.FILTER_IMAGE);
          mLogicManager.incRotate();
          if (null != mCurBitmap && null != mCurBitmap.getMovie()) {
            // if (null != currPath && currPath.endsWith(".gif")){

            oriention = vShowView.getRotate();
            if (oriention >= 360) {
              oriention = 0;
            }

            newOriention = vShowView.getRotate();
            MtkLog.i(TAG, "Photo oriention change gif:" + oriention);
            vShowView.setRotate(oriention + 90);
            new Thread(vShowView).start();
            mRotateThread.start();
          } else {
            oriention = mLogicManager.getPhotoOrientation();
            MtkLog.d(TAG, "Rotate set Bitmap start: " + System.currentTimeMillis());
            mCurBitmap.setBitmap(mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
            MtkLog.d(TAG, "Rotate set Bitmap end: " + System.currentTimeMillis());
            newOriention = mLogicManager.getPhotoOrientation();

            MtkLog.i(TAG, "Photo oriention change :" + oriention
                + "-->" + newOriention);
            vShowView.setRes(mCurBitmap);
            if (newOriention != oriention) {
              // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),
              // MultiMediaConstant.LARGE_THUMBNAIL_SIZE,
              // MultiMediaConstant.LARGE_THUMBNAIL_SIZE, true);
              // BitmapCache.createCache(false).put(mLogicManager
              // .getCurrentPath(Const.FILTER_IMAGE), thumb);
              MtkLog.d(TAG, "Rotate create cache start: " + System.currentTimeMillis());
              BitmapCache.createCache(false).del(mLogicManager.getCurrentPath(Const.FILTER_IMAGE));
              MtkLog.d(TAG, "Rotate create cache end: " + System.currentTimeMillis());
              mLogicManager.setRotationChanged();

            }
            vShowView.setType(ConstPhoto.ROTATE_R);
            new Thread(vShowView).start();
            mRotateThread.start();
          }
          updateInfoView();
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
      case KeyMap.KEYCODE_MTKIR_SLEEP: {
        // if (playMode == FRAME_ONE_PHOTO_MODE || playMode == FRAME_ALL_PHOTO_MODE) {
        // SleepDialog dialog = new SleepDialog(this);
        // dialog.show();
        // return true;
        // }

        if (isValid()) {
          if (playMode == FRAME_ONE_PHOTO_MODE || playMode == FRAME_ALL_PHOTO_MODE) {
            setSleepTime();
          }
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_INFO:
        if (playMode == FRAME_ONE_PHOTO_MODE) {
          return true;
        }
        break;
      case KeyEvent.KEYCODE_ESCAPE:
      case KeyMap.KEYCODE_BACK:
        // SKY luojie add 20171219 for add choose menu begin
        if(isPreviewListDialogShown()) {
          hidePreviewListDialog();
          //setControllerVisible();
          return true;
        }
        // SKY luojie add 20171219 for add choose menu end
	  case KeyMap.KEYCODE_MTKIR_STOP:
        // handlePhotoPlayEnd();
       //begin by yx for set scale = 1 if !=1
        if (null != vShowView) {
            //int size1 = LogicManager.getInstance(getApplicationContext( )).getPicCurZoom( );
            //changed by zhangqing
            float size1 = LogicManager.getInstance(getApplicationContext()).getPicCurZoom();
            if (size1 != (float)ConstPhoto.ZOOM_1X) {
                MtkLog.d(TAG, "onBackPressed-------->size1 != ConstPhoto.ZOOM_1X!");
                if (mCurBitmap !=null && null != mCurBitmap.getMovie( )) {
                    vShowView.setMultiple(ConstPhoto.ZOOM_1X);
                } else {
                    mControlView.setPhotoZoom(mResources.getString(R.string.mmp_menu_1x));
                    vShowView.setRes(mCurBitmap);
                    vShowView.setMultiple(ConstPhoto.ZOOM_1X);
                    vShowView.setType(ConstPhoto.ZOOMOUT);
                    vShowView.run( );
                }
                return true;
            }
        }
        //end by yx for set scale = 1 if !=1
        playToEnd();
        // SKY luojie add 20171218 for add choose menu begin
        finish();
        // SKY luojie add 20171218 for add choose menu end
        break;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void handlePhotoPlayEnd() {
    isBackFlag = true;

    if (null != vShowView) {
      vShowView.bitmapRecycle();
    }
    finishSetting();

    finish();
  }

  /**
   * Switch photo play effective
   */
  private void switchEffect() {
    int value = vShowView.getEffectValue();
    if (value == ConstPhoto.DEFAULT) {
      value = ConstPhoto.dissolve;
    } else if (value < ConstPhoto.RADNOM) {
      value++;
    } else {
      value = ConstPhoto.DEFAULT;
    }
    vShowView.setType(value);
    mControlView.setPhotoAnimationEffect(value);
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

  private void showSleepMenuDialog() {

    ArrayList<MenuFatherObject> list = new ArrayList<MenuFatherObject>(2);
    MenuFatherObject object = new MenuFatherObject();
    object.content = getResources().getString(
        R.string.mmp_frame_photo_sleeptime);
    object.hasnext = false;
    object.enable = true;
    list.add(object);

    menuDialogSleepTime = new MenuListView(Photo4K2KPlayActivity.this, list,
        mListener, null);
    menuDialogSleepTime.setMediaType(sMediaType);
    menuDialogSleepTime.show();
  }

  /**
   * Show menu dialog
   */
  private void showDialog() {

    if (playMode == FRAME_ONE_PHOTO_MODE) {

      showSleepMenuDialog();
      return;
    }
    mHandler.removeMessages(MESSAGE_POPHIDE);
    menuDialog = new MenuListView(Photo4K2KPlayActivity.this, GetDataImp
        .getInstance().getComMenu(Photo4K2KPlayActivity.this,
            R.array.mmp_menu_photoplaylist,
            R.array.mmp_menu_photoplaylist_enable,
            R.array.mmp_menu_photoplaylist_hasnext), mListener,
        mCallBack);

    if (null != mControlView) {

      if (mControlView.isPlaying()) {
        menuDialog
            .setList(0, mResources
                .getString(R.string.mmp_menu_pause), false, 3,
                mResources
                    .getString(R.string.mmp_menu_duration),
                true, 4, mResources
                    .getString(R.string.mmp_menu_effect),
                true);
				  menuDialog.setItemEnabled(3, true);
      } else {
        menuDialog.setList(0, mResources
            .getString(R.string.mmp_menu_play), false, 3,
            mResources.getString(R.string.mmp_menu_rotate), false,
            4, mResources.getString(R.string.mmp_menu_zoom), true);
		//MtkLog.i("yangxiong", "1010:" + mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
        if (isNotSupport  /* || mCurBitmap == null*/) {

          menuDialog.setItemEnabled(3, false);
          menuDialog.setItemEnabled(4, false);
        }
		 if (null != mLogicManager.getCurrentPath(Const.FILTER_IMAGE) && mLogicManager.getCurrentPath(Const.FILTER_IMAGE).endsWith(".gif")){//gif
		 
          if (null == mCurBitmap.getMovie()){
            menuDialog.setItemEnabled(3, false);
          }
        }else{
			 menuDialog.setItemEnabled(3, true);
		}
      }
    }
	
    menuDialog.setMediaType(sMediaType);
    menuDialog.show();

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
  private final MenuListView.MenuDismissCallBack
  mCallBack = new MenuListView.MenuDismissCallBack() {

    @Override
    public void onDismiss() {
      if (menuDialog != null && menuDialog.isShowing()) {
        if (mHandler.hasMessages(MESSAGE_POPHIDE)) {
          mHandler.removeMessages(MESSAGE_POPHIDE);
        }
        mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, 3000);
      } else {
        hideController();
      }
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
    removeMessage();
    isStop = true;
    super.onStop();
    Util.LogLife(TAG, "onStop");

  };

  /**
   * Remove handler message
   */
  private void removeMessage() {
    mHandler.removeMessages(MESSAGE_PHOTOMODE);
    mHandler.removeMessages(MESSAGE_PLAY);
    mHandler.removeMessages(MESSAGE_POPHIDE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    isPhotoActivityLiving = false;
    if (is4K2KFlag) {
      Util.LogResRelease("deinitPhotoPlay");
      photoRender.deinitPhotoPlay();
      is4K2KFlag = false;
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    if (mControlView != null) {
      mControlView.dismiss();
    }
    if (mTipsDialog != null) {
      mTipsDialog.dismiss();
    }
    mRotateThread = null;

    super.onDestroy();
    unregisterReceiver(mTalkBackReceiver);//add by yx for talkback
    Util.LogLife(TAG, "onDestroy");

    // SKY luojie add 20171218 for add choose menu begin
    mMenuHandler.removeMessages(DETECT_USER_OPERATION);
    // SKY luojie add 20171218 for add choose menu end
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBackPressed() {
    // SKY luojie add 20171219 for add choose menu begin
    if(isPreviewListDialogShown()) {
      hidePreviewListDialog();
      //setControllerVisible();
      return;
    }
    // SKY luojie add 20171219 for add choose menu end
    if (null != mImageManager) {
      mImageManager.finish();
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    if (null != vShowView) {
      vShowView.setInterrupted(true);
      vShowView.bitmapRecycle();
    }
    super.onBackPressed();
  }

  /**
   * show information when photo frame is selected in menu
   * @param menuSetupPhotoFrame
   * add by haixia for fix bug DTV00383200
   */
  private void showPhotoFrameInfo(String menuSetupPhotoFrame) {
    if (null == mTipsDialog) {
      mTipsDialog = new TipsDialog(this);
      mTipsDialog.setText(menuSetupPhotoFrame);
      if (isPhotoActivityLiving == true) {
        mTipsDialog.show();
        mTipsDialog.setBackground(R.drawable.toolbar_playerbar_test_bg);
        Drawable drawable = this.getResources().getDrawable(
            R.drawable.toolbar_playerbar_test_bg);

        int weight = (int) (drawable.getIntrinsicWidth() * 0.6);
        int height = drawable.getIntrinsicHeight();
        // mTipsDialog.setDialogParams(weight, height);

        WindowManager m = getWindow().getWindowManager();
        Display display = m.getDefaultDisplay();

        int x = -((ScreenConstant.SCREEN_WIDTH / 2) - weight / 2)
            + (ScreenConstant.SCREEN_WIDTH / 10);
        int y = (int) (ScreenConstant.SCREEN_HEIGHT * 3 / 8
            - ScreenConstant.SCREEN_HEIGHT * 0.16 - height / 2);
        mTipsDialog.setWindowPosition(x, y);
      }
    } else {
      mTipsDialog.setText(menuSetupPhotoFrame);
      mTipsDialog.show();
    }

  }

  /**
   * Menu item click callback
   * @param content the click item content value
   */
  private void controlState(String content) {

    if (content.equals(mResources
        .getString(R.string.mmp_frame_photo_sleeptime))) {
      if (null != menuDialogSleepTime && menuDialogSleepTime.isShowing()) {
        menuDialogSleepTime.dismiss();
      }
      if (mSleepdialog != null && mSleepdialog.isShowing()) {
        // mSleepdialog.setSleepTime();
        return;
      } else {
        mSleepdialog = new SleepDialog(this);
        mSleepdialog.show();
        mSleepdialog.updateValue(true);
        return;
      }

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
      // Added by Dan for fix bug DTV00384878& DTV00389285
      menuDialog.setItemEnabled(3, !isNotSupport);
      menuDialog.setItemEnabled(4, !isNotSupport);
	  //MtkLog.i("yangxiong", "1201:" + mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
	  if (null != mLogicManager.getCurrentPath(Const.FILTER_IMAGE) && mLogicManager.getCurrentPath(Const.FILTER_IMAGE).endsWith(".gif")){//gif
           if (null == mCurBitmap.getMovie()){
            menuDialog.setItemEnabled(3, false);
          }
        }else{
			 menuDialog.setItemEnabled(3, true);
		}
    } else if (content.equals(mResources.getString(R.string.mmp_menu_play))) {
      showController();
      hideControllerDelay();
      mControlView.setMediaPlayState();
      menuDialog.setList(0,
          mResources.getString(R.string.mmp_menu_pause), false, 3,
          mResources.getString(R.string.mmp_menu_duration), true, 4,
          mResources.getString(R.string.mmp_menu_effect), true);
      // add by keke 12.2.27 for DTV00399637
	    menuDialog.setItemEnabled(3, true);
      if (isNotSupport) {
        menuDialog.setItemEnabled(3, true);
        menuDialog.setItemEnabled(4, true);
      }
    }

    else if (content.equals(mResources.getString(R.string.mmp_menu_none))
        && (isRepeatMode == 3)) {
      // Util.setMediaRepeatMode(getApplicationContext(), MultiMediaConstant.PHOTO,Util.NONE);
      mControlView.setRepeatNone();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_NONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatone))) {
      // Util.setMediaRepeatMode(getApplicationContext(),
      // MultiMediaConstant.PHOTO,Util.REPEATE_ONE);
      mControlView.setRepeatSingle();
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, Const.REPEAT_ONE);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_repeatall))) {
      // Util.setMediaRepeatMode(getApplicationContext(),
      // MultiMediaConstant.PHOTO,Util.REPEATE_ALL);
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
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.DEFAULT);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_dissolve))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.dissolve);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wiperight))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_right);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipeleft))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_left);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipeup))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_top);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_wipedown))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.wipe_bottom);
    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_boxin))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.box_in);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_boxout))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.box_out);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_random))) {
      mControlView.setPhotoAnimationEffect(content);
      vShowView.setType(ConstPhoto.RADNOM);
    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_showinfo))) {
      menuDialog.dismiss();
      showinfoview(MultiMediaConstant.PHOTO);
    } else if (content.equals(mResources.getString(R.string.mmp_menu_1x))
        || content.equals(mResources.getString(R.string.mmp_menu_2x))
        || content.equals(mResources.getString(R.string.mmp_menu_4x))) {

      menuDialog.hideMenuDelay();

      mControlView.setPhotoZoom(content);

      int size = Integer.parseInt(content.substring(0, 1));
      // vShowView.setRes(mCurBitmap);
      vShowView.setMultiple(size);
      vShowView.setType(ConstPhoto.ZOOMOUT);
      new Thread(vShowView).start();
      isZoom = true;

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_rotate))) {

      menuDialog.hideMenuDelay();

      mLogicManager.incRotate();
      // if (mLogicManager.getCurrentPath(Const.FILTER_IMAGE).endsWith(".gif")){
      if (null != mCurBitmap.getMovie()) {
        MtkLog.i(TAG, " gif file");
        oriention = vShowView.getRotate();
        MtkLog.i(TAG, " gif file");
        if (oriention >= 360) {
          oriention = 0;
        }

        newOriention = mLogicManager.getPhotoOrientation();
        newOriention = vShowView.getRotate();

        vShowView.setRotate(oriention + 90);
        MtkLog.i(TAG, "Photo oriention change gif:" + oriention
            + "-->" + newOriention);
        // new Thread(vShowView).start();
      } else {
        oriention = mLogicManager.getPhotoOrientation();
        mCurBitmap.setBitmap(mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
        newOriention = mLogicManager.getPhotoOrientation();

        MtkLog.i(TAG, "Photo oriention change :" + oriention
            + "-->" + newOriention);
        vShowView.setRes(mCurBitmap);
        // if (newOriention != oriention) {
        // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),
        // MultiMediaConstant.LARGE_THUMBNAIL_SIZE,
        // MultiMediaConstant.LARGE_THUMBNAIL_SIZE, true);
        // BitmapCache.createCache(false).put(mLogicManager
        // .getCurrentPath(Const.FILTER_IMAGE), thumb);
        // }
        if (newOriention != oriention) {
          // Bitmap thumb=Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),100,100, true);
          BitmapCache.createCache(false).del(mLogicManager.getCurrentPath(Const.FILTER_IMAGE));
          // .put(mLogicManager.getCurrentPath(Const.FILTER_IMAGE), thumb);
          mLogicManager.setRotationChanged();
        }
        vShowView.setType(ConstPhoto.ROTATE_R);
        new Thread(vShowView).start();

      }
      updateInfoView();

    } else if (content
        .equals(mResources.getString(R.string.mmp_menu_frame))) {
      /*
       * String path = mLogicManager.getCurrentPath(Const.FILTER_IMAGE); Editor editor =
       * mPreferences.edit(); editor.putString(PHOTO_FRAME_KEY, path); editor.commit();
       */
      mPhotoFramePath = mLogicManager.getCurrentPath(Const.FILTER_IMAGE);
      mPhotoFrameSource = mImageSource;
      if (menuDialog != null && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
      this.showPhotoFrameInfo(mResources
          .getString(R.string.mmp_menu_photo_frame));

      mHandler.sendEmptyMessageDelayed(MESSAGE_HIDDLE_FRAME, DELAYED_FRAME);
    }
  }

  /**
   * Show the bitmap with EffectView
   * @param bitmap
   */
  private void loadImageDone(PhotoUtil bitmap) {
//        if (menuDialog != null && menuDialog.isShowing()) {
//            menuDialog.dismiss();
//        }
    updateIndex();
    mLogicManager.initRotate();
    setControlView();
    if (null == bitmap) {
      return;
    }

    mCurBitmap = bitmap;
    if (null == mControlView) {

      MtkLog.i(TAG, "loadImageDone()  photoPlayActivity has finished");
      return;
    }

    if (null != mTipsDialog && mTipsDialog.isShowing()) {
      mTipsDialog.dismiss();
    }

    int value = vShowView.getEffectValue();
    vShowView.setInterrupted(false);

    if ((bitmap.getBitmap() == null && bitmap.getMovie() == null) || isNotSupport) {
      vShowView.setInterrupted(true);
      mControlView.setPhotoZoom("");
      return;

    } else {
      int size = vShowView.getMultiple();
      int rotate = vShowView.getRotate();
      mControlView.setPhotoZoom(mResources
          .getString(R.string.mmp_menu_1x));
      // vShowView.setMultiple(1);
      mPhotoParams[0] = 1;
      // vShowView.setPreMultiple(size);
      mPhotoParams[1] = size;
      // vShowView.setRotate(0);
      mPhotoParams[2] = 0;
      // vShowView.setPreRotate(rotate);
      mPhotoParams[3] = rotate;
    }

    if (null != vShowView) {
      if (bitmap.getMovie() != null) {
        vShowView.setRes(bitmap, mPhotoParams);
        vShowView.setType(value);
        vShowView.run();
      } else {
        MtkLog.i(TAG, "setBitmap:" + bitmap.getBitmap());
        // vShowView.setEffectRes(bitmap.getBitmap(),mPhotoParams);
        vShowView.syncSetEffectResToRun(bitmap.getBitmap(),
            mPhotoParams, value);
      }
    }
  }

  int mPhotoParams[] = {
      0, 0, 0, 0
  };

  /**
   * Get photo play duration
   * @return int duration
   */
  public static int getDelayedTime() {
    return mDelayedTime;
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    if (is4K2KFlag) {
      Util.LogResRelease("deinitPhotoPlay");
      if (photoRender != null)
        photoRender.deinitPhotoPlay();
      is4K2KFlag = false;
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
  }
   //begin by yx for talkback
    private final String TALKBACK_KEYCODE = "talkback_keycode";
    private final String TALKBACK_BRDCAST_ACTION = "com.skyworth.talkback.keyevent";
    private final String TALKBACK_EVENT_ACTION = "action.down.up";

    private BroadcastReceiver mTalkBackReceiver = new BroadcastReceiver( ) {
        @Override
        public void onReceive(Context context, Intent intent) {
          Log.d("MMPtalkback", "mTalkBackReceiver onReceive:  "+intent.getAction( ));
          if (TALKBACK_BRDCAST_ACTION.equals(intent.getAction( ))) {
            
            int action = intent.getIntExtra(TALKBACK_EVENT_ACTION,-1);
            int keycode = intent.getIntExtra(TALKBACK_KEYCODE, -1);
          Log.d("MMPtalkback", "getAction:  "+action+",keycode:  "+keycode);
          if (!(menuDialog != null && menuDialog.isShowing() || mInfo!=null &&  mInfo.isShowing())){
            Photo4K2KPlayActivity.this.dispatchKeyEvent(new KeyEvent(action,keycode));
          }
         }
          }
    };

  //end by yx for talkback

  // SKY luojie add 20171218 for add choose menu begin
  public void play(String photoPath) {
    mFilesManager.getPlayList(
            mCurrentTypePlayFiles, getIndexByPath(mCurrentTypePlayFiles, photoPath), FilesManager.CONTENT_PHOTO,
            MultiFilesManager.SOURCE_LOCAL);

    if (isValid()) {
      isNotSupport = false;
      //reSetController();
      mHandler.removeMessages(MESSAGE_PLAY);
      mCurBitmap = null;
      resetZoom();
      PlayList.getPlayList().setCurrentIndex(Const.FILTER_IMAGE,
              getIndexByPath(mCurrentTypePlayFiles, photoPath));
      mImageManager.load(Const.CURRENTPLAY);
      mHandler.removeMessages(MESSAGE_PLAY);
      hidePreviewListDialog();
    }
  }

  SkyPreviewListDialog.OnLoadedFilesListener mOnLoadedFilesListener =
          new SkyPreviewListDialog.OnLoadedFilesListener() {
            @Override
            public void onLoaded(List<FileAdapter> files, int type) {
              if(files == null || files.size() < 1) return;

              if(mCurrentTypePlayFiles.size() < 1) {
                mCurrentTypePlayFiles.addAll(files);
                mFilesManager.getPlayList(
                        mCurrentTypePlayFiles, 0, FilesManager.CONTENT_PHOTO,
                        MultiFilesManager.SOURCE_LOCAL);

                boolean isEnterFromDesktop = getIntent().getBooleanExtra(KEY_EXTRA_ENTER_FROM_DESKTOP, false);
                if (isEnterFromDesktop && mFirstPlayFilePath != null && !"".equals(mFirstPlayFilePath)) {
                  Util.reset3D(getApplicationContext());
                  mImageSource = ConstPhoto.LOCAL;
                  initShowPhoto();
                  play(mFirstPlayFilePath);
                }
              } else {
                mCurrentTypePlayFiles.addAll(files);
                mFilesManager.getPlayList(
                        mCurrentTypePlayFiles, getCurrentPlayingIndex(Const.FILTER_IMAGE), FilesManager.CONTENT_PHOTO,
                        MultiFilesManager.SOURCE_LOCAL);
              }
            }
          };

  @Override
  public void finish() {
    super.finish();
    if(mIsEnterFromDesktop && !mPreviewListDialog.mIsStartedActivity) {
      startFilesGridActivity(MultiMediaConstant.PHOTO);
    }
  }
  // SKY luojie add 20171218 for add choose menu end

}
