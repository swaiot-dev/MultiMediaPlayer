
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.SkyPreviewListDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.InfoDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaControlView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MoveModeDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PhotoInfoDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PhotoPlayStateDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PlayStateDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.ImageManager.ImageLoad;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.EffectView;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoCompletedListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.Imageshowimpl.OnPhotoDecodeListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

//import com.mediatek.wwtv.mediaplayer.nav.CaptureLogoActivity;
//import com.mediatek.wwtv.mediaplayer.nav.util.NavSundryImplement;

//import com.mediatek.wwtv.mediaplayer.capturelogo.CaptureLogoActivity;

public class PhotoPlayActivity extends SkyMediaPlayActivity implements
		PhotoInfoDialog.OnZoomChangeListener, MoveModeDialog.OnMovePicListener {

  private static final String TAG = "PhotoPlayActivity";

  private static final int MESSAGE_PLAY = 0;

  private static final int MESSAGE_POPHIDE = 1;

  private static final int MESSAGE_PHOTOMODE = 2;

  private static final int MESSAGE_HIDDLE_MESSAGE = 3;

  private static final int MESSAGE_HIDDLE_FRAME = 4;

  private static final int MESSAGE_NO_PHOTO_FRAME = 5;
  private static final int MESSAGE_DECODE_FAILURE = 6;
  private static final int FRAME_ONEPHOTO_MODE = 7;

  private static final int AUTO_HIDE_PLAY_STATUS = 0x00101;

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

  private int playMode;

  private PhotoUtil mCurBitmap;

  private int isRepeatMode = 0;

  private int menu_repeatmode = 0;// add by haixia

  private SharedPreferences mPreferences;

  public static final String PHOTO_FRAME_PATH = "photoframe";

  public static final String PHOTO_FRAME_KEY = "photo";
  public static final String PLAY_MODE = "PlayMode";

  private int mImageSource = 0;

  private boolean isStop = false;
  private boolean isPause = false;
  private boolean photoPlayStatus = false;

  public static final int NORMAL_MODE = 0;
  public static final int FRAME_ONE_PHOTO_MODE = 1;
  public static final int FRAME_ALL_PHOTO_MODE = 2;

  private SleepDialog mSleepdialog;

  private boolean isRealGif = false;
  private SundryDialog sundryDialog;

    private PhotoPlayStateDialog mPlayStateDialog;
    private PhotoInfoDialog mInfoDialog;
    private MediaControlView mMediaControlView;

	// begin by zhangqing ==> picture zoom function
	public static int mZoomState = -1;
	public static int ZOOMOUT = 0;
	public static int ZOOMIN = 1;
	private final float ZOOMMAX = 3.0f;
	private final float ZOOMMIN = 0.6f;
	private MoveModeDialog moveModeDialog;
  boolean isZoomState = false;
	// end by zhangqing ==> picture zoom function

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

	// begin by zhangqing ==> picture zoom function
	@Override
	public void zoomChange(int type) {
    isZoomState = true;
		if (mControlView.isPlaying()) {
			mControlView.pause();
			Log.i(TAG, "zoomChange:  mControlView pause");
		}

		float scale = vShowView.getMultiple();

		if (type == ZOOMOUT) {
			if (scale < ZOOMMAX && scale >= ZOOMMIN) {
				scale += 0.2f;
			} else if (scale >= ZOOMMAX) {
				scale = 1.0f;
			}
		} else if (type == ZOOMIN) {
			if (scale <= ZOOMMAX && scale > ZOOMMIN) {
				scale -= 0.2f;
			} else if (scale <= ZOOMMIN) {
				scale = 1.0f;
			}
		}
		setImageZoomChange(scale);

		//If the picture size zooms bigger than display width or display height
		//show MoveModeDialog
		if (vShowView.getGifMovie() == null) {
			int curWidth = (int) (mCurBitmap.getBitmap().getWidth() * scale);
			int curHeight = (int) (mCurBitmap.getBitmap().getHeight() * scale);
			if (curWidth > ScreenConstant.SCREEN_WIDTH
					|| curHeight > ScreenConstant.SCREEN_HEIGHT) {
				if (!moveModeDialog.getShowsDialog()) {
					moveModeDialog.show(getFragmentManager(), "move_mode");
					moveModeDialog.setShowsDialog(true);
				}
			}
			Log.i("zhangqing", "zoomChange: scale = " + scale);
		} else {
			int curWidth = (int) (vShowView.getGifMovie().width() * scale);
			int curHeight = (int) (vShowView.getGifMovie().height() * scale);
			if (curWidth > ScreenConstant.SCREEN_WIDTH
					|| curHeight > ScreenConstant.SCREEN_HEIGHT) {
				if (!moveModeDialog.getShowsDialog()) {
					moveModeDialog.show(getFragmentManager(), "move_mode");
					moveModeDialog.setShowsDialog(true);
				}
			}
			Log.i("zhangqing", "zoomChange: scale = " + scale);
		}
	}
	// end by zhangqing ==> picture zoom function

	// begin by zhangqing ==> picture translation function
	@Override
	public void movePic(int action) {
		if (mControlView.isPlaying()) {
			mControlView.pause();
			Log.i(TAG, "movePic: mControlView pause");
		}

    if (action == KeyEvent.KEYCODE_BACK) {
      vShowView.setX(0);
      vShowView.setY(0);
      setImageZoomChange(1.0f);
      mControlView.play();
      Log.i(TAG, "movePic: mControlView play");
      isZoomState = false;
      return;
    }

		if (action == KeyEvent.KEYCODE_DPAD_CENTER) {
			zoomChange(PhotoPlayActivity.ZOOMOUT);
			return;
		}

		float originX = vShowView.getX();
		float originY = vShowView.getY();
		Log.i("zhangqing", "movePic: originX = " + originX);
		Log.i("zhangqing", "movePic: originY = " + originY);
		if (false) {
			Log.i("zhangqing", "movePic: translationX  = " + vShowView.getTranslationX());
			Log.i("zhangqing", "movePic: translatioinY = " + vShowView.getTranslationY());
			Log.i("zhangqing", "movePic: x = " + originX + ",y = " + originY);
			Log.i("zhangqing", "movePic: width = " + vShowView.getWidth() + ",height = "
					+ vShowView.getHeight());
			Log.i("zhangqing", "movePic: getLeft() = " + vShowView.getLeft());
			Log.i("zhangqing", "movePic: getRight = " + vShowView.getRight());
			Log.i("zhangqing", "movePic: getTop = " + vShowView.getTop());
			Log.i("zhangqing", "movePic: getBottom = " + vShowView.getBottom());
		}


		float scale = vShowView.getMultiple();
		int curWidth;
		int curHeight;
		if (vShowView.getGifMovie() == null) {
			curWidth = (int) (mCurBitmap.getBitmap().getWidth() * scale);
			curHeight = (int) (mCurBitmap.getBitmap().getHeight() * scale);
      Log.i("zhangqing", "movePic: not gif movie curWidth = " + curWidth+",curHeight = "+curHeight);
		} else {
			curWidth = (int) (vShowView.getGifMovie().width() * scale);
			curHeight = (int) (vShowView.getGifMovie().height() * scale);
      Log.i("zhangqing", "movePic: gif movie curWidth = " + curWidth+",curHeight = "+curHeight);
		}
		int picX = (ScreenConstant.SCREEN_WIDTH - curWidth) / 2;
		int picY = (ScreenConstant.SCREEN_HEIGHT - curHeight) / 2;
		float paddingHorizontal = (curWidth - ScreenConstant.SCREEN_WIDTH) / 2;
		float paddingVertical = (curHeight - ScreenConstant.SCREEN_HEIGHT) / 2;
    Log.i("zhangqing", "movePic: paddingHorizontal = " + paddingHorizontal+",paddingVertical = "+paddingVertical);
		switch (action) {
			case KeyEvent.KEYCODE_DPAD_UP:
				if (originY > 0 || Math.abs(originY) < paddingVertical) {
					originY -= 20;
					vShowView.setTranslationY(originY);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				if (originY < 0 || Math.abs(originY) < paddingVertical) {
					originY += 20;
					vShowView.setTranslationY(originY);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				if (originX > 0 || Math.abs(originX) < paddingHorizontal) {
					originX -= 20;
					vShowView.setTranslationX(originX);
				}
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				if (originX < 0 || Math.abs(originX) < paddingHorizontal) {
					originX += 20;
					vShowView.setTranslationX(originX);
				}
				break;
		}
	}
	// end by zhangqing ==> picture translation function

	// begin by zhangqing ==> picture zoom function
  private void setImageZoomChange(float scale) {
    if (null != mCurBitmap.getMovie()) {
      vShowView.setMultiple(scale);
    } else {
      vShowView.setRes(mCurBitmap);
      vShowView.setMultiple(scale);
      vShowView.setType(ConstPhoto.ZOOMOUT);
      vShowView.run();
    }
  }
	// end by zhangqing ==> picture zoom function

	private class SleepDialog extends Dialog {

    private TextView mSleepTime;
    private Context msContext;

    private Timer timer;
    private TimerTask task;
    private int timeInt = 5;

    private SleepDialog(Context context, int theme) {
      super(context, theme);
      this.msContext = context;
    }

    public SleepDialog(Context context) {
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
            SleepDialog.this.dismiss();
          }
          timeInt--;
        }
      };
      timer.schedule(task, 10, 1000);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      switch (keyCode) {
        case KeyMap.KEYCODE_MTKIR_SLEEP:

          return false;
        case KeyMap.KEYCODE_BACK:
          // case KeyMap.KEYCODE_MTKIR_ANGLE:
        case KeyMap.KEYCODE_MTKIR_GUIDE:
          if (null != msContext && msContext instanceof PhotoPlayActivity) {
            ((PhotoPlayActivity) msContext).onKeyDown(keyCode, event);
          }
          this.dismiss();
        case KeyMap.KEYCODE_MENU:
          if (null != msContext && msContext instanceof PhotoPlayActivity) {
            ((PhotoPlayActivity) msContext).onKeyDown(keyCode, event);
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

    private final PlayStateDialog.ControlPlayState mControlImp = new PlayStateDialog.ControlPlayState() {

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

    private final ControlPlayState mOldControlImp = new ControlPlayState() {

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

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_mediaplay);

    // mPreferences = getSharedPreferences(PHOTO_FRAME_PATH, MODE_PRIVATE);
    findView();
    getIntentData();
    // add by keke for fix DTV00380644
    mControlView.setRepeatVisibility(Const.FILTER_IMAGE);
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      if (autoTest(Const.FILTER_IMAGE, MultiFilesManager.CONTENT_PHOTO)) {
        mImageSource = ConstPhoto.LOCAL;
        playMode = 0;

      }
    }

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

		// begin by zhangqing ==> picture zoom and translation function
		moveModeDialog = new MoveModeDialog();
		moveModeDialog.setOnMovePicListener(this);
		moveModeDialog.setShowsDialog(false);
	}

  protected void setPreviewListDialogParams() {
    mPreviewListDialog.setFilesManager(mFilesManager);
    mPreviewListDialog.setOnLoadedFilesListener(mOnLoadedFilesListener);
    mPreviewListDialog.setTheFirstFilePath(mFirstPlayFilePath);
    mPreviewListDialog.changeContentType(MultiMediaConstant.PHOTO);
    mPreviewListDialog.setActivityType(MultiMediaConstant.PHOTO);
  }

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
        case MESSAGE_PLAY:
          mCurBitmap = null;
          isNotSupport = false;
                    hideInfoDialog();
          if (playMode == FRAME_ONE_PHOTO_MODE) {
            mImageManager.load(Const.CURRENTPLAY);
          }
          else {
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
          mResources = PhotoPlayActivity.this.getResources();
          String photoFrame = mResources
              .getString(R.string.mmp_menu_photo_frame);
          if (mTipsDialog != null && mTipsDialog.isShowing()
              && mTipsDialog.getTitle().equals(photoFrame)) {
            mTipsDialog.dismiss();
          }
          dismissNotSupprot();
          if (isNotSupport == true && isPhotoActivityLiving == true) {
            try {
              featureNotWork(PhotoPlayActivity.this.getResources()
                  .getString(R.string.mmp_photo_type_notsupport));
            } catch (Exception ex) {
              ex.printStackTrace();
            }

            vShowView.setRes(null);
            vShowView.run();
          }
          break;
        }
        case MESSAGE_NO_PHOTO_FRAME: {
          // //mLogicManager.setPhotoDecodeFailureListener(mPhotoDecodeFailListener);
          if (mTipsDialog != null && mTipsDialog.isShowing()) {
            mTipsDialog.dismiss();
          }
          if (isPhotoActivityLiving == true) {
            featureNotWork(PhotoPlayActivity.this.getResources().getString(
                R.string.mmp_photo_type_notsupport));
          }
          // mLogicManager.setPhotoDecodeListener(mPhotoDecodeListener);
          break;
        }
        case MESSAGE_DECODE_FAILURE:
          featureNotWork(PhotoPlayActivity.this.getResources()
              .getString(R.string.mmp_photo_type_notsupport));
          vShowView.setRes(null);
          vShowView.run();
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
          case AUTO_HIDE_PLAY_STATUS:
              mMediaControlView.hidePlayStatusLayout();
              break;
        default:
          break;
      }
    }

  };

  /**
   *  find view
   */
  private void findView() {
    vShowView = new EffectView(this);
    vLayout = (LinearLayout) findViewById(R.id.mmp_mediaplay);
    vLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        | View.SYSTEM_UI_FLAG_LOW_PROFILE
        | View.SYSTEM_UI_FLAG_FULLSCREEN
        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    vLayout.addView(vShowView);

    Display display = getWindowManager().getDefaultDisplay();
    LayoutParams params = vShowView.getLayoutParams();
    params.width = ScreenConstant.SCREEN_WIDTH;
    params.height = ScreenConstant.SCREEN_HEIGHT;
    vShowView.setLayoutParams(params);
    vShowView.setWindow(ScreenConstant.SCREEN_WIDTH, ScreenConstant.SCREEN_HEIGHT);

    getPopView(R.layout.mmp_popupphoto, MultiMediaConstant.PHOTO,
            mOldControlImp);
    initPopupView(R.layout.play_control_layout);
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
    Util.LogResRelease("onResume");
  }

  @Override
  // add by haixia for fix CR DTV00379219
  protected void onPause() {
    // TODO Auto-generated method stub
    if (playMode == FRAME_ALL_PHOTO_MODE)
      mLogicManager.setRepeatMode(Const.FILTER_IMAGE, menu_repeatmode);

    super.onPause();
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
  }

    protected void initPopupView(int resource){
        int width = ScreenConstant.SCREEN_WIDTH;
        int height = ScreenConstant.SCREEN_HEIGHT;
        View controlView = LayoutInflater.from(this).inflate(resource, null);
        mMediaControlView = new MediaControlView(this, controlView, width, height);
        showPlayStatusView();
    }

    private void showPlayStatusView() {
        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                mMediaControlView.showAtLocation(vLayout, Gravity.TOP | Gravity.LEFT, 0, 0);
                mHandler.sendEmptyMessageDelayed(AUTO_HIDE_PLAY_STATUS, 5000);
                return false;
            }
        });
    }

  private final OnPhotoCompletedListener mPhotoCompleteListener = new OnPhotoCompletedListener() {

    @Override
    public void onComplete() {
      if (null != mImageManager) {
        mImageManager.finish();
      }
      // finish();
      mHandler.post(new Runnable() {
        @Override
        public void run() {
          finish();
        }
      });

    }
  };

  private final OnPhotoDecodeListener mPhotoDecodeListener = new OnPhotoDecodeListener() {

    @Override
    public void onDecodeFailure() {
      MtkLog.d(TAG, "onDecodeFailure~");
      isNotSupport = true;
      if (isStop) {
        return;
      }
      mHandler.sendEmptyMessageDelayed(MESSAGE_DECODE_FAILURE, 100);

    }

    @Override
    public void onDecodeSuccess() {
      MtkLog.d(TAG, "onDecodeSuccess~");
      isNotSupport = false;
    }
  };

  /**
   * Initialize photo play
   */
  private void initShowPhoto() {
    mResources = PhotoPlayActivity.this.getResources();
    mLogicManager = LogicManager.getInstance(this);

    vShowView.setRotate(0);

    Display display = getWindowManager().getDefaultDisplay();
    mLogicManager.initPhoto(display, vShowView);
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
      MtkLog.i(TAG, "playMode == NORMAL_MODE");
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
      MtkLog.i(TAG, "playMode != NORMAL_MODE------------");
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
      Intent intent = new Intent(PhotoPlayActivity.this,
          MtkFilesGridActivity.class);
      intent.putExtra(MultiMediaConstant.MEDIAKEY, MultiFilesManager
          .getInstance(PhotoPlayActivity.this).getContentType());
      startActivity(intent);
      finish();
    }
  };

  // Deleted by Dan for fix bug DTV00376577
  // private boolean mLastPlayState;

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
      // TODO sleep
      // if (keyCode == KeyMap.KEYCODE_MENU) {
      // showSleepDialog();
      // }
      if (keyCode == KeyMap.KEYCODE_BACK) {
        finish();
      }
      return true;
    }
    switch (keyCode) {
        //Begin ==> Modified by yangxiong for solving "add zoom or aspect option of remote keying for photo in MMP"
                case KeyMap.KEYCODE_MTKIR_ZOOM:
                case KeyMap.KEYCODE_MTKIR_ASPECT:
                 // mControlView.pause();//fix bug 72194 by yangxiong
                 //        int size = LogicManager.getInstance(getApplicationContext()).getPicCurZoom( );
                 //        String content = mResources.getString(R.string.mmp_menu_1x);
                 //        switch (size) {
                 //            case ConstPhoto.ZOOM_1X:
                 //                content = mResources.getString(R.string.mmp_menu_2x);
                 //                size = ConstPhoto.ZOOM_2X;
                 //                break;
                 //            case ConstPhoto.ZOOM_2X:
                 //                content = mResources.getString(R.string.mmp_menu_4x);
                 //                size = ConstPhoto.ZOOM_4X;
                 //                break;
                 //            case ConstPhoto.ZOOM_4X:
                 //                content = mResources.getString(R.string.mmp_menu_1x);
                 //                size = ConstPhoto.ZOOM_1X;
                 //                break;
                 //            default:
                 //                content = mResources.getString(R.string.mmp_menu_1x);
                 //                size = ConstPhoto.ZOOM_1X;
                 //                break;
                 //        }
                 //        mControlView.setPhotoZoom(content);
                 //        if (null != mCurBitmap.getMovie( )) {
                 //            vShowView.setMultiple(size);
                 //        } else {
                 //            vShowView.setRes(mCurBitmap);
                 //            vShowView.setMultiple(size);
                 //            vShowView.setType(ConstPhoto.ZOOMOUT);
                 //            vShowView.run( );
                 //        }
                        return true;
                //End ==> Modified by yangxiong for solving "add zoom or aspect option of remote keying for photo in MMP"
            //    case KeyMap.KEYCODE_MENU: {
            //      reSetController();
            //      showDialog();
            //      return true;
            //    }
    case KeyMap.KEYCODE_MTKIR_SEFFECT: {
      if (mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED) {
        sundryDialog = new SundryDialog(this, 2);
        sundryDialog.show();
      }
      return true;
    }
      //case KeyMap.KEYCODE_MTKIR_CHDN: {
    case KeyMap.KEYCODE_DPAD_DOWN: 
    case KeyMap.KEYCODE_MTKIR_INFO: {
      // SKY luojie modify 20171218 for add choose menu begin
//      if(!isPreviewListDialogShown()) {
//        hideController();
//        showPreviewListDialog();
//        return true;
//      }
                if (null == mInfoDialog) {
                    mInfoDialog = new PhotoInfoDialog();
					mInfoDialog.setOnZoomChangeListener(this);
                    mInfoDialog.show(getFragmentManager(), "photo_info");
                } else if (mInfoDialog.isVisible()) {
                    mInfoDialog.dismiss();
                } else {
                    mInfoDialog.show(getFragmentManager(), "photo_info");
                }
      return true;
      /*
        if (isValid()) {
          isNotSupport = false;
          reSetController();
          mHandler.removeMessages(MESSAGE_PLAY);
          mCurBitmap = null;
        Util.LogLife(TAG, "KEYCODE_DPAD_DOWN");
        mImageManager.load(Const.MANUALNEXT);
        }
        return true;
        */
      }
    // case KeyMap.KEYCODE_MTKIR_CHUP:
    case KeyMap.KEYCODE_DPAD_UP: {
//      if(isPreviewListDialogShown()) {
//        hidePreviewListDialog();
//      } else {
//        hideController();
//        showPreviewListDialog();
//      }
      return true;
      /*
        if (isValid()) {
          isNotSupport = false;
          reSetController();
          mHandler.removeMessages(MESSAGE_PLAY);
          mCurBitmap = null;
        Util.LogLife(TAG, "KEYCODE_DPAD_UP");
        mImageManager.load(Const.MANUALPRE);
        }
        return true;
      */
      }
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
        if (isValid()) {
            isNotSupport = false;
            reSetController();
            pausePhoto();
            mCurBitmap = null;
            hideInfoDialog();
            Util.LogLife(TAG, "KEYCODE_DPAD_UP");
            mImageManager.load(Const.MANUALPRE);
        }
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_NEXT: {
          if (isValid()) {
              isNotSupport = false;
              reSetController();
              pausePhoto();
              mCurBitmap = null;
              hideInfoDialog();
              Util.LogLife(TAG, "KEYCODE_DPAD_DOWN");
              mImageManager.load(Const.MANUALNEXT);
          }
          return true;
      }
      case KeyMap.KEYCODE_DPAD_LEFT:
      case KeyMap.KEYCODE_DPAD_RIGHT:
      {
          if (event.getRepeatCount() == 0) {
              event.startTracking();
              isLongPressLRKey = false;
          }
          return true;
      }
        case KeyMap.KEYCODE_DPAD_CENTER:

        // added by zhangqing
            if (isZoomState) {
                Log.i(TAG, "onKeyDown: isZoomState = true");
                return true;
            }
        // ended by zhangqing
        case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
           if (mHandler.hasMessages(MESSAGE_PLAY)){
               pausePhoto();
           }else {
               playPhoto();
               isZoomState = false;
           }
            return true;
        }
      // SKY luojie modify 20171218 for add choose menu end
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
         * if (isNotSupport){ break; } //fix cr DTV00385195 add by lei if (mCurBitmap == null){
         * featureNotWork(getString(R.string.mmp_featue_notsupport)); return true; } // Deleted by
         * Dan for fix bug DTV00376577 // if (null != mControlView) { // mLastPlayState =
         * mControlView.isPalying(); // mControlView.pause(); // } //add by shuming for fix CR:
         * DTV00407914 mLogicManager.setCapturer(vShowView); //keke change if(null != mControlView){
         * if(mControlView.isPlaying()){ mControlView.onCapture();
         * mControlView.setPlayIcon(View.INVISIBLE); photoPlayStatus=true; }else{
         * mControlView.setPauseIcon(View.INVISIBLE); photoPlayStatus=false; } } hideController();
         * Intent intent = new Intent(this, CaptureLogoActivity.class);
         * intent.putExtra(CaptureLogoActivity.FROM_MMP, CaptureLogoActivity.MMP_PHOTO);
         * startActivity(intent); isBackFromCapture = true; return true;
         */
      }
    /*case KeyMap.KEYCODE_MTKIR_YELLOW: {
      if (null != mControlView && mControlView.isPlaying()) {
        reSetController();
        switchEffect();
      } else {
        /* fix cr DTV00385117 add by lei */
        // Modified by Dan for fix bug DTV00390943
       /* MtkLog.d(TAG, "isNotSupport = " + isNotSupport + "mCurBitmap = " + mCurBitmap);
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
        vShowView.setRes(mCurBitmap);
        vShowView.setMultiple(size);
        vShowView.setType(ConstPhoto.ZOOMOUT);
        vShowView.run();
        // isZoom = true;
      }

      return true;
    }*/
    /*case KeyMap.KEYCODE_MTKIR_GREEN: {
      if (null != mControlView && mControlView.isPlaying()) {
        reSetController();
        switchDuration();
      } else {
        /* fix cr DTV00385117 add by lei */
        /*// Modified by Dan for fix bug DTV00390943
        if (isNotSupport || mCurBitmap == null) {
          return true;
        }

        reSetController();

        // add by shuming for Fix CR DTV00401969
        String currPath = mLogicManager.getCurrentPath(Const.FILTER_IMAGE);
        rotate();
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
        if (playMode == FRAME_ONE_PHOTO_MODE) {
          SleepDialog dialog = new SleepDialog(this);
          dialog.show();
          return true;
        }
      }
            //      case KeyMap.KEYCODE_MTKIR_INFO:
            //        if (playMode == FRAME_ONE_PHOTO_MODE) {
            //          return true;
            //        }
            //        break;
      case KeyEvent.KEYCODE_ESCAPE:
      case KeyMap.KEYCODE_BACK:
        // added by zhangqing
        isZoomState = false;
        // SKY luojie add 20171219 for add choose menu begin
        if(isPreviewListDialogShown()) {
          hidePreviewListDialog();
          //setControllerVisible();
          return true;
        }
        finish();
        break;
        // SKY luojie add 20171219 for add choose menu end
	case KeyMap.KEYCODE_MTKIR_STOP:
        finish();
        break;
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        //add by y.wan for pressing LR key to pre or next start
        if (keyCode == KeyMap.KEYCODE_DPAD_LEFT && !isLongPressLRKey) {
            isNotSupport = false;
            reSetController();
            pausePhoto();
            mCurBitmap = null;
            hideInfoDialog();
            Util.LogLife(TAG, "KEYCODE_DPAD_UP");
            mImageManager.load(Const.MANUALPRE);
            isLongPressLRKey = false;
        } else if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT && !isLongPressLRKey) {
            isNotSupport = false;
            reSetController();
            pausePhoto();
            mCurBitmap = null;
            hideInfoDialog();
            Util.LogLife(TAG, "KEYCODE_DPAD_DOWN");
            mImageManager.load(Const.MANUALNEXT);
            isLongPressLRKey = false;
        }
        //add by y.wan for pressing LR key to pre or next end
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        //add by y.wan for long press LR key to show preview dialog start
        if (keyCode == KeyMap.KEYCODE_DPAD_RIGHT || keyCode == KeyMap.KEYCODE_DPAD_LEFT) {
            isLongPressLRKey = true;
            pausePhoto();
            showPreviewListDialog();
        }
        //add by y.wan for long press LR key to show preview dialog end
        return true;
    }

    public void pausePhoto() {
        mHandler.removeMessages(MESSAGE_PLAY);
        mMediaControlView.showPlayStatusLayout();
        mMediaControlView.showPause();
        isPause = true;
        cancelMessage(AUTO_HIDE_PLAY_STATUS);
    }

    public void playPhoto() {
        mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
        mMediaControlView.showPlayStatusLayout();
        mMediaControlView.showPlay();
        isPause = false;
        sendDelayMessage(AUTO_HIDE_PLAY_STATUS, 5000);
    }

    private void hideInfoDialog() {
        if (null != mInfoDialog && mInfoDialog.isResumed()) {
            mInfoDialog.dismiss();
        }
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

  private void showSleepMenuDialog() {

    ArrayList<MenuFatherObject> list = new ArrayList<MenuFatherObject>(2);
    MenuFatherObject object = new MenuFatherObject();
    object.content = getResources().getString(
        R.string.mmp_frame_photo_sleeptime);
    object.hasnext = false;
    object.enable = true;
    list.add(object);

    menuDialogSleepTime = new MenuListView(PhotoPlayActivity.this, list,
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
    menuDialog = new MenuListView(PhotoPlayActivity.this, GetDataImp
        .getInstance().getComMenu(PhotoPlayActivity.this,
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
      } else {
        menuDialog.setList(0, mResources
            .getString(R.string.mmp_menu_play), false, 3,
            mResources.getString(R.string.mmp_menu_rotate), false,
            4, mResources.getString(R.string.mmp_menu_zoom), true);

        if (isNotSupport/* || mCurBitmap == null*/) {

          menuDialog.setItemEnabled(3, false);
          menuDialog.setItemEnabled(4, false);
        }

        // if(isRealGif){
        //
        //
        // menuDialog.setItemEnabled(3, false);
        //
        //
        // }
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
    vShowView.removeMessage();
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
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    if (mControlView != null) {
      mControlView.dismiss();
    }
    if (mTipsDialog != null) {
      mTipsDialog.dismiss();
    }
    vShowView.bitmapRecycle();
    super.onDestroy();
    Util.LogResRelease("onDestroy");

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
//        mSleepdialog.updateValue(true);
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
  if (null != mLogicManager.getCurrentPath(Const.FILTER_IMAGE) && mLogicManager.getCurrentPath(Const.FILTER_IMAGE).endsWith(".gif")){//gif
           if (null == mCurBitmap.getMovie()){
            menuDialog.setItemEnabled(3, false);
          }
        }else{
			 menuDialog.setItemEnabled(3, true);
		}
      // not support gif rotate
      if (isRealGif) {

        menuDialog.setItemEnabled(3, false);

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

      mControlView.setPhotoZoom(content);
      if (null != mCurBitmap.getMovie()) {

        int size = Integer.parseInt(content.substring(0, 1));
        // vShowView.setRes(mCurBitmap);
        vShowView.setMultiple(size);
        // vShowView.setType(ConstPhoto.ZOOMOUT);
        // vShowView.run();
      } else {
        int size = Integer.parseInt(content.substring(0, 1));
        vShowView.setRes(mCurBitmap);
        vShowView.setMultiple(size);
        vShowView.setType(ConstPhoto.ZOOMOUT);
        vShowView.run();
      }
      // isZoom = true;

    } else if (content.equals(mResources
        .getString(R.string.mmp_menu_rotate))) {
      rotate();
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

    public void rotate() {
		// added by zhangqing BUG 88076
		if (mControlView.isPlaying()) {
			mControlView.pause();
			Log.i(TAG, "rotate: mControlView pause");
		}
		//ended by zhangqing
		
    // added by zhangqing BUG 87419
    if (mCurBitmap == null) {
      Log.i(TAG, "rotate: mCurBitmap = null");
      return;
    }
    // ended by zhangqing BUG 87419
    mLogicManager.incRotate();
    if (null != mCurBitmap.getMovie()) {
      oriention = vShowView.getRotate();
      if (oriention >= 360) {
        oriention = 0;
      }
      vShowView.setRotate(oriention + 90);
      newOriention = vShowView.getRotate();
      MtkLog.i(TAG, "Photo oriention change gif:" + oriention
          + "-->" + newOriention);
      vShowView.setRes(mCurBitmap);
      vShowView.setType(ConstPhoto.ROTATE_PHOTO);
      vShowView.run();
    } else {
      oriention = mLogicManager.getPhotoOrientation();
      mCurBitmap.setBitmap(mLogicManager.setRightRotate(mCurBitmap.getBitmap()));
      newOriention = mLogicManager.getPhotoOrientation();

      MtkLog.i(TAG, "Photo oriention change :" + oriention
          + "-->" + newOriention);
      vShowView.setRes(mCurBitmap);

      if (newOriention != oriention) {
        Bitmap thumb = Bitmap.createScaledBitmap(mCurBitmap.getBitmap(),
            MultiMediaConstant.LARGE_THUMBNAIL_SIZE,
            MultiMediaConstant.LARGE_THUMBNAIL_SIZE, true);
        BitmapCache.createCache(false).put(mLogicManager
            .getCurrentPath(Const.FILTER_IMAGE), thumb);
        mLogicManager.setRotationChanged();
      }
      vShowView.setType(ConstPhoto.ROTATE_R);
      vShowView.run();
    }
    updateInfoView();
  }

  /**
   * Show the bitmap with EffectView
   * @param bitmap
   */
  private void loadImageDone(PhotoUtil bitmap) {

    updateIndex();
    mCurBitmap = bitmap;
    mLogicManager.initRotate();
    isZoomState = false;
    if (null == mControlView) {
      MtkLog.i(TAG, "loadImageDone()  photoPlayActivity has finished");
      return;
    }
    // fix CR DTV00357597
    // Modifyed by yongzheng 20111212 for fix bug DTV00381167
    // if (!isStop /*&& mControlView.isPalying()*/) {
    if (!isPause) {
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, mDelayedTime);
    }

    setControlView();

    // if (isZoom) {

    MtkLog.i(TAG, "loadImageDone()  bitmap = " + bitmap + " isNotSupport  = " + isNotSupport);
    if (null == bitmap
        || (bitmap.getBitmap() == null && bitmap.getMovie() == null) || isNotSupport) {

      mControlView.setPhotoZoom("");
      return;

    } else {
      // begin by zhangqing ==> picture zoom function
	  //int size = vShowView.getMultiple();
	  float size = vShowView.getMultiple();
	  // end by zhangqing ==> picture zoom function
      int rotate = vShowView.getRotate();
      mControlView.setPhotoZoom(mResources
          .getString(R.string.mmp_menu_1x));
      // Added by Dan for fix bug DTV00375281
      vShowView.setMultiple(1);
      vShowView.setPreMultiple(size);
      vShowView.setRotate(0);
      vShowView.setPreRotate(rotate);
    }
    // isZoom = false;
    // }

    if (null != mTipsDialog && mTipsDialog.isShowing()) {
      mTipsDialog.dismiss();
    }

    int value = vShowView.getEffectValue();

    if (bitmap.getMovie() != null) {
      MtkLog.d(TAG, "isRealGif = true;");
      isRealGif = true;

    } else {
      MtkLog.d(TAG, "isRealGif = false;");
      isRealGif = false;

    }
    vShowView.resetFlag();
    if (value == ConstPhoto.DEFAULT || bitmap.getMovie() != null) {
      MtkLog.i(TAG, "value == ConstPhoto.DEFAULT || bitmap.getMovie() != null");
      vShowView.setRes(bitmap);
    } else {
      MtkLog.i(TAG, "vShowView.setEffectRes(bitmap.getBitmap());");
      vShowView.setEffectRes(bitmap.getBitmap());
    }
    vShowView.setType(value);
    if (null != vShowView) {
      vShowView.run();
    }

  }

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
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
    removeMessage();
    if (vShowView != null) {
      vShowView.removeMessage();
    }
    isStop = true;
  }

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

    protected void cancelMessage(int what) {
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
    }

    protected void sendMessage(int what) {
        sendDelayMessage(what, 0);
    }

    protected void sendDelayMessage(int what, long delay) {
        if (mHandler.hasMessages(what)) {
            mHandler.removeMessages(what);
        }
        mHandler.sendEmptyMessageDelayed(what, delay);
    }

}
