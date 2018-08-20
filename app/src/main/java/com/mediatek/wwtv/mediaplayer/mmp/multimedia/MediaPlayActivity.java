
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;
import java.io.File;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.Map;
import java.util.HashMap;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.mediatek.wwtv.mediaplayer.jni.PhotoRender;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;

import com.mediatek.SubtitleAttr;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoManager;

import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MeteDataView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ScoreView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ShowInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TipsDialog;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.gamekit.content.MutilMediaConst;
//import com.mediatek.wwtv.mediaplayer.mmp.util.DmrParser;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.mmp.util.GetDataImp;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.ModelConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.iRootMenuListener;
import com.mediatek.wwtv.mediaplayer.setting.AudioSettingActivity;
import com.mediatek.wwtv.mediaplayer.setting.SettingActivity;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.iDmrListener;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;

import android.os.SystemProperties;

import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.TextUtils;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;

import android.graphics.Color;
import android.graphics.Typeface;

import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;

import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;

import skyworth.skyworthlivetv.menu.QMenuManager;

/**
 * Multi-media play activty
 *
 * @author hs_weihuiluo
 */
public class MediaPlayActivity extends FragmentActivity {

  /**
   * Log tag
   */
  protected static final String TAG = "MediaPlayActivity";

  /**
   * Media content type :photo audio video text
   */
  protected static int sMediaType = 0;

  /**
   * Repeat key duration
   */
  public static final int KEY_DURATION = 400;

  /**
   * Message to update not support tips dialog
   */
  private static final int MSG_UPDATE_NOT_SUPPORT = 0;

  /**
   * Message to dismiss feature not support dialog
   */
  private static final int MSG_DISMISS_FEARTURE_NOT_SUPPORT = 1;

  /**
   * Message to show feature not support dialog
   */
  private static final int MSG_SHOW_FEATURE_NOT_SUPPORT = 2;

  private static final int MSG_HIDE_INFORBAR_POINT = 4;
  /**
   * Message to hide controller
   */
  private static final int MSG_HIDE_CONTROLLER = 5;
  /**
   * Update not support tips dialog delay milliseconds
   */
  private static final int MSG_DELAY = 1000;
  /**
   * Update not support tips dialog delay milliseconds
   */
  private static final int MSG_DMR_SET_MUTE = 1001;
  private static final int MSG_SET_MUTE = 1003;
  private static final int MSG_DMR = 1002;
  private static final int MSG_REFRESH_LISTVIEW = 10088;
  /**
   * Dismiss feature not support tips dialog delay milliseconds
   */
  protected static final int MSG_DISMISS_DELAY = 2000;

  public static final int DELAYED_LONG = 8000;

  public static final int DELAYED_MIDDLE = 5000;

  public static final int DELAYED_SHORT = 3000;


  protected TVContent mTvContent;

  /**
   * Last key down milliseconds
   */
  protected long mLastKeyDownTime;

  /**
  * for user press epg key , avoid auto next play.
  */
  protected boolean EPG_KEY_PRESS = false;

  /**
   * {@link Resources}
   */
  private Resources mResources;

  /**
   * The screen width
   */
  protected int mDisPlayWidth;

  /**
   * The screen height
   */
  protected int mDisPlayHeight;

  protected boolean mIsAutoPause;

  /**
   * {@link ControlView}
   */
  protected ControlView mControlView;
  /**
   * {@link ControlView}
   */
  protected MeteDataView mMeteDataView;

  /**
   * Show menu dialog
   */
  protected MenuListView menuDialog;

  protected MenuListView menuDialogFontList;

  /**
   * Show Lyric view
   */
  protected ScoreView mScoreView;

  /**
   * Show info view
   */
  protected ShowInfoView mInfo;

  /**
   * {@link LogicManager}
   */
  protected LogicManager mLogicManager;

  protected AudioManager mAudioManager;

  protected SubtitleAttr mSubtitleAttr;
  protected String[] mEncodingArray;
  private com.mediatek.MtkMediaPlayer mPlayer; // Android flow
  private UIMediaPlayer mtkMediaPlayer;
  private SundryDialog sundryDialog;
    private QMenuManager mQMenuManager;
  
  private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener
    = new AudioManager.OnAudioFocusChangeListener() {

    @Override
    public void onAudioFocusChange(int focusChange) {
      if (!MediaPlayActivity.this.isInPictureInPictureMode()) {
        MtkLog.d(TAG, "onAudioFocusChange, this is not in pip, return");
        return;
      }
      MtkLog.d(TAG, "onAudioFocusChange focusChange:" + focusChange);
      if(focusChange == AudioManager.AUDIOFOCUS_LOSS ||
          focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
        if (mControlView != null) {
          if (mControlView.isPlaying()) {
            mIsAutoPause = true;
            mControlView.pause();
          }
        }
      } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
        if (mControlView != null) {
          if (mControlView.isPause() && mIsAutoPause) {
            mIsAutoPause = false;
            mControlView.play();
          }
        }
      }
    }
  };

  /**
   * Max volume value
   */
  protected int maxVolume = 0;

  /**
   * The current volume value
   */
  protected int currentVolume = 0;

  /**
   * Lyric lines per screen
   */
  protected static int mPerLine = 8;

  /**
   * Tips dialog
   */
  protected TipsDialog mTipsDialog;

  /**
   * Control bar contentView
   */
  protected View contentView;
  /**
   * metedata bar contentView
   */
  protected View metedataView;

  /**
   * Resume from capureLog flag
   */
  protected boolean isBackFromCapture = false;

  protected boolean isHideSperum = false;

  public static final String AUTO_TEST_PROPERTY = "mtk.auto_test";
  public static final String AUTO_TEST_DLNA_PROPERTY = "mtk.dlna.auto_test";
  public static final String AUTO_TEST_SAMBA_PROPERTY = "mtk.samba.auto_test";

  /**
   * Not support flag
   */

  protected boolean SCREENMODE_NOT_SUPPORT = false;
  protected boolean isSetPicture = false;

  // add by keke for DTV00383992
  protected boolean isNotSupport = false;

  /**
   * Last not support content(used to switch from feature not support)
   */
  public enum PlayException {
    DEFAULT_STATUS, VIDEO_NOT_SUPPORT, VIDEO_ONLY, AUDIO_NOT_SUPPORT,
    FILE_NOT_SUPPORT, AV_NOT_SUPPORT
  }

  protected PlayException playExce = PlayException.DEFAULT_STATUS;

  /**
   * The current not support content
   */
  protected String mTitle;
  public static String mPhotoFramePath;
  public static int mPhotoFrameSource;

  // Added by Dan for fix bug DTV00373545
  private boolean mIsMute;

  // add for fix bug DTVDTV00392376
  private boolean mIsActiveLiving = true;

  protected ScheduledThreadPoolExecutor stpe = null;

   private int mRefreshTime = 0;
  /**
   * {@link ListView.OnItemClickListener}
   */
  private final ListView.OnItemClickListener mListener = new ListView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
        long arg3) {
      TextView tvTextView = (TextView) arg1
          .findViewById(R.id.mmp_menulist_tv);
      String content = tvTextView.getText().toString();
      controlState(content);
    }
  };

  @Override
  public void onConfigurationChanged(Configuration newConfig) {

    // TODO Auto-generated method stub
    super.onConfigurationChanged(newConfig);
  }

  private HandlerThread mHandlerThead;
  protected Handler mThreadHandler;

  /**
   * An handler used to send message
   */
  protected Handler mHandler = new Handler() {

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);

      // add for fix bug DTVDTV00392376
      if (!mIsActiveLiving) {
        return;
      }
      MtkLog.i(TAG, " handleMessage msg.what:" + msg.what);
      switch (msg.what) {
		  case MSG_REFRESH_LISTVIEW: {
                    if (mRefreshTime > 9) {
                        if (mHandler.hasMessages(MSG_REFRESH_LISTVIEW)) {
                            mHandler.removeMessages(MSG_REFRESH_LISTVIEW);
                        }
                        break;
                    }
                    if (hasLrc( ) && menuDialog != null) {
                        MenuFatherObject obj = menuDialog.getItem(4);
                        obj.enable = true;
                        menuDialog.updateItem(4, obj);
						mRefreshTime = 10;
                    } else {
                        mRefreshTime++;
                        if (mHandler.hasMessages(MSG_REFRESH_LISTVIEW)) {
                            mHandler.removeMessages(MSG_REFRESH_LISTVIEW);
                        }
                        mHandler.sendEmptyMessageDelayed(MSG_REFRESH_LISTVIEW, 500);
                    }
                }
                break;
        case MSG_DISMISS_FEARTURE_NOT_SUPPORT: {
          if (playExce == PlayException.AUDIO_NOT_SUPPORT) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources
                  .getString(R.string.mmp_audio_notsupport));
            }
          } else if (playExce == PlayException.VIDEO_NOT_SUPPORT) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources
                  .getString(R.string.mmp_video_notsupport));
            }
          } else if (playExce == PlayException.VIDEO_ONLY) {
            if (mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_STOPPED
                || mLogicManager.getPlayVideoStatusOfUI() == VideoConst.PLAY_STATUS_ERROR) {
              dismissNotSupprot();
            } else {
              onNotSuppsort(mResources
                  .getString(R.string.mmp_video_only));
            }
          } else if (playExce == PlayException.FILE_NOT_SUPPORT) {
            break;
          } else {
            MtkLog.i(TAG, "MSG.WHAT==1 else dismissNotSupprot");
            dismissNotSupprot();
          }

          break;
        }
        case MSG_SHOW_FEATURE_NOT_SUPPORT: {
          onNotSuppsort(mTitle);
          break;
        }

        case MSG_HIDE_INFORBAR_POINT:
          if (mControlView != null) {
            if (mControlView.getWidth() == 1 && mControlView.getHeight() == 1) {
              mControlView.update(-1, -1, -1, -1);
              // mControlView.hiddlen(View.INVISIBLE);
            }
          }
          break;
        case MSG_SET_MUTE:
          onMute();
          break;
        case MSG_DMR_SET_MUTE:
          // reSetController();
          // onMute();
          break;
        case MSG_DMR:
          // reSetController();
          // onMute();
          Log.i(TAG, " handdmrEvent:" + MSG_DMR);
          handleDmrEvent(msg.arg1, msg.arg2);
          Log.i(TAG, " handdmrEvent: end:" + MSG_DMR);
          break;
        case MSG_HIDE_CONTROLLER:
            hideController();
            break;
        default:
          break;
      }
    }

  };
  public boolean isDmrSource = false;
  public iDmrListener mDmrListener = null;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    MmpApp des = (MmpApp) this.getApplication();
    des.add(this);
    des.registerRootMenu(mRootMenuListener);
    Intent it = getIntent();
    if (it != null) {
      isDmrSource = it.getBooleanExtra(DmrHelper.DMRSOURCE, false);
      if (isDmrSource == false) {
        DmrHelper.setHandler(mHandler);
      } else {
        MtkFilesBaseListActivity.setFromDmr();
      }
    }
    //SKY luojie 20180110 add begin
    getScreenWH();
    //SKY luojie 20180110 add end

    mTvContent = TVContent.getInstance(this);
    mResources = MediaPlayActivity.this.getResources();
    mLogicManager = LogicManager.getInstance(this.getApplicationContext());
    mAudioManager = (AudioManager)MediaPlayActivity.this.getSystemService(Context.AUDIO_SERVICE);
//  mAudioManager.requestAudioFocus(mAudioFocusListener,
//      AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//    if (MultiFilesManager.getInstance(getApplicationContext()).getContentType()
//        == MultiFilesManager.CONTENT_VIDEO
//        && CommonSet.VID_SCREEN_MODE_NORMAL == mLogicManager.getCurScreenMode()) {
//      setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
//    }
    // Added by Dan for fix bug DTV00373545
    mIsMute = mLogicManager.isMute();

    // add for fix bug DTVDTV00392376
    mIsActiveLiving = true;
    // For 3D gamekit
    get3DExtraData();
    // Thumbnail.getInstance().reset();

    Util.LogLife(TAG, "onCreate pip:" + "   " + Util.mIsEnterPip);
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SCREEN_OFF);
    filter.addAction(AudioManager.STREAM_MUTE_CHANGED_ACTION);
    filter.addAction(AudioManager.VOLUME_CHANGED_ACTION);
    filter.addAction(AudioManager.STREAM_DEVICES_CHANGED_ACTION);
    registerReceiver(mReceiver, filter);
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
	
    mSubtitleAttr = new SubtitleAttr();
	mEncodingArray = mResources.getStringArray(R.array.mmp_subtitle_encoding_array);

  }

  protected void getScreenWH() {
    if (SettingsUtil.SCREEN_WIDTH == 0 || SettingsUtil.SCREEN_HEIGHT == 0) {
      DisplayMetrics dm = new DisplayMetrics();
      this.getWindowManager().getDefaultDisplay().getMetrics(dm);
      SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
      SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;

      ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
      ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
    }
  }

  protected boolean autoTest(int constFilter, int mulFilter) {

    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0))
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {

      String dataStr = getIntent().getDataString();
      Log.d(TAG, "autoTest dataStr:" + dataStr);
      if (dataStr != null && dataStr.length() > 0) {

        File f = new File(dataStr);
        if (!f.exists()) {
          Log.d(TAG, "autoTest !f.exists()");
          return false;
        }

        PlayList.getPlayList().cleanList(constFilter);
        LocalFileAdapter file = new LocalFileAdapter(new MtkFile(f));
        List<FileAdapter> files = new ArrayList<FileAdapter>();
        files.add(file);
        PlayList playlist = MultiFilesManager.getInstance(this).getPlayList(files, 0, mulFilter,
            MultiFilesManager.SOURCE_LOCAL);
        Log.d(TAG, "autoTest playlist path:" + playlist.getCurrentPath(constFilter));
        return true;
      }

    }

    return false;
  }

  protected boolean playLocalPvr(int constFilter, int mulFilter) {

    if (getIntent().getData() != null) {

      Bundle bundle = getIntent().getExtras();
      String dataStr = bundle.getString("PATH");
      String[] paths = bundle.getStringArray("PATHS");
      int position = bundle.getInt("POSITION");
      Log.d(TAG, "playLocalPvr dataStr:" + dataStr + " position:" + position + " paths.length:"
          + paths.length);
      String file = "";
      List<FileAdapter> files = new ArrayList<FileAdapter>();
      PlayList.getPlayList().cleanList(constFilter);
      for (int i = 0; i < paths.length; i++) {
        file = paths[i];
        if (file != null && file.length() > 0) {
          File f = new File(file);
          if (!f.exists()) {

            if (file.equals(dataStr)) {
              Log.d(TAG, "!f.exists()");
              return false;
            }
          } else {
            Log.d(TAG, "playLocalPvr f.exists() " + " --file:" + file);
            LocalFileAdapter fl = new LocalFileAdapter(new MtkFile(f));
            files.add(fl);
          }
        }

      }
      MultiFilesManager.getInstance(this).setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);
      Log.d(TAG, "playLocalPvr playlist  files.size():" + files.size());
      PlayList playlist = MultiFilesManager.getInstance(this).getPlayList(files, position,
          mulFilter, MultiFilesManager.SOURCE_LOCAL);
      if (playlist != null) {
        Log.d(TAG, "playLocalPvr playlist path:" + playlist.getCurrentPath(constFilter));
      } else {
        Log.i(TAG, "playLocalPvr Error playlist == null");
        finish();
      }
      return true;
    }
    return false;
  }

  // For 3D gamekit start
  protected int mFrom3DBrowse = MutilMediaConst.CONTENT_UNKNOW;
  // private int m3DFromMode = -1;
  // private String m3DFromPath = null;
  protected Bundle mExtras;

  protected void get3DExtraData() {
    mExtras = getIntent().getExtras();
    if (mExtras != null)
      mFrom3DBrowse = mExtras.getInt(MmpConst.TO_3DBROWSE,
          MutilMediaConst.CONTENT_UNKNOW);
  }

  protected void isBack3DBrowseMode() {
    if (mFrom3DBrowse != MutilMediaConst.CONTENT_UNKNOW) {
      Intent intent = new Intent(MmpConst.INTENT_FILEBROWSE);
      mExtras.putInt(MmpConst.FROM_PLAYTYPE, mFrom3DBrowse);
      mExtras.putSerializable(MmpConst.CURRENT_BROWSE_STATUS,
          MmpConst.BrowseStatus.FROM_PLAY_STATUS);
      intent.putExtras(mExtras);
      startActivity(intent);
    }
  }

  /**
   * show not support tips dialog
   *
   * @param title
   *            the tips dialog content
   */
  private void onNotSuppsort(String title) {
    // new Exception().printStackTrace();

    MtkLog.i(TAG, "onNotSuppsort  :" + title);
    if (null == mTipsDialog) {
      MtkLog.i(TAG, "null == mTipsDialog");
      mTipsDialog = new TipsDialog(this);
      mTipsDialog.setText(title);
      mTipsDialog.show();
      MtkLog.i(TAG, "null == mTipsDialog2");
      mTipsDialog.setBackground(R.drawable.toolbar_playerbar_dialog_bg);
      Drawable drawable = this.getResources().getDrawable(
          R.drawable.toolbar_playerbar_dialog_bg);

      int weight = (int) (drawable.getIntrinsicWidth() * 0.6);
      int height = drawable.getIntrinsicHeight();
      // mTipsDialog.setDialogParams(weight, height);

      WindowManager m = mTipsDialog.getWindow().getWindowManager();
      Display display = m.getDefaultDisplay();

      /*int x = -((ScreenConstant.SCREEN_WIDTH) - weight / 2)
          + (ScreenConstant.SCREEN_WIDTH / 10);
      int y = (int) (ScreenConstant.SCREEN_HEIGHT * 3 / 8
          - ScreenConstant.SCREEN_HEIGHT * 0.16 - height / 2);
       mTipsDialog.setWindowPosition(x, y);*/

      /* ----heni add start */
      WindowManager.LayoutParams hnaddLp = mTipsDialog.getWindow().getAttributes();
      hnaddLp.gravity = Gravity.RIGHT|Gravity.BOTTOM;
      hnaddLp.x = 60;
      hnaddLp.y = 220;
      mTipsDialog.getWindow().setAttributes(hnaddLp);
      MtkLog.i(TAG, "--------mtipsDialog w,h :"+hnaddLp.width+", "+hnaddLp.height);
      /* ----heni add end */


    } else {
      MtkLog.i(TAG, "null != mTipsDialog");
      try {

        // need updata tip text , eg:video codec not support show,user do ff/fr need tmp show
        // feature not support
        /*
         * if(mTipsDialog!=null&&mTipsDialog.isShowing()){ return; }
         */
        mTipsDialog.setText(title);
        mTipsDialog.show();
        MtkLog.i(TAG, "mTipsDialog.showing()");
      } catch (Exception e) {
        // TODO: handle exception
        e.printStackTrace();
      }

    }
    // mLastText = title;
  }

  @Override
  public void finish() {
    dismissNotSupprot();
    // removeControlView();

    // For 3D gamekit start
    isBack3DBrowseMode();
    // For 3D gamekit end.
    super.finish();
  }

  /**
   * Show feature not support dialog
   *
   * @param title
   *            the dialog content
   */
  protected void featureNotWork(String title) {
    Log.d(TAG, "featureNotWork title:" + title + " xx "+ Log.getStackTraceString(new Throwable()));
    mTitle = title;
    if (mHandler != null) {
      mHandler.sendEmptyMessage(MSG_SHOW_FEATURE_NOT_SUPPORT);
      if (mHandler.hasMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT)) {
        mHandler.removeMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
      }
      mHandler.sendEmptyMessageDelayed(MSG_DISMISS_FEARTURE_NOT_SUPPORT, MSG_DISMISS_DELAY);
    }
  }

  /**
   * Remove feature not support messages
   */
  protected void removeFeatureMessage() {
    if (mHandler != null) {
      if (mHandler.hasMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT)){
        mHandler.removeMessages(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
      }
      if (mHandler.hasMessages(MSG_SHOW_FEATURE_NOT_SUPPORT)){
        mHandler.removeMessages(MSG_SHOW_FEATURE_NOT_SUPPORT);
      }
    }
  }

  /**
    * {@inheritDoc}
    */
  @Override
  protected void onStart() {
    super.onStart();
    Util.LogLife(TAG, "onStart pip:" + Util.mIsEnterPip);
    if (mThreadHandler != null) {
      mThreadHandler.post(new Runnable() {

        @Override
        public void run() {
          if (sMediaType == MultiMediaConstant.VIDEO
              || sMediaType == MultiMediaConstant.AUDIO) {
            mLogicManager.freeVideoResource();
            AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
          }
          if (sMediaType == MultiMediaConstant.PHOTO
              || sMediaType == MultiMediaConstant.THRD_PHOTO) {
            mLogicManager.setDisplayRegionToFullScreen();
          }
        }
      });
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    if (this instanceof VideoPlayActivity) {
      mAudioManager.requestAudioFocus(mAudioFocusListener,
          AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
    if (mIsAutoPause) {
      if (mControlView != null && mControlView.isPause()) {
        mIsAutoPause = false;
        mControlView.play();
      }
    }
    Util.LogLife(TAG, "onResume pip: " + Util.mIsEnterPip);
  }

  /**
   * Dismiss not support tips dialog
   */
  protected void dismissNotSupprot() {
    try {
      if (null != mTipsDialog) {
        // mTipsDialog.hide();
//        MtkLog.i(TAG, "dismissNotSupprot null != mTipsDialog && mTipsDialog.hide()"
//            + Log.getStackTraceString(new Throwable()));
        mTipsDialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void hiddenView() {
    try {
      if (null != mTipsDialog) {
        mTipsDialog.hide();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void dismissMenuDialog() {
    try {
      if (null != menuDialog && menuDialog.isShowing()) {
        menuDialog.dismiss();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Remove lyrics
   *
   * @param ishide
   *            is hidden lyrics view
   */
  public void removeScore(boolean ishide) {
  }

  /**
   * Setup lyrics lines per screen
   *
   * @param perline
   *            line number
   */
  public void setLrcLine(int perline) {
  }

  /**
   * Is show spectrum
   *
   * @return true:show spectrum, false: hidden spectrum
   */
  public boolean isShowSpectrum() {
    return false;
  }

  /**
   * Is the current audio has lyrics
   *
   * @return true:has lyrics, false: no lyrics
   */
  public boolean hasLrc() {
    return false;
  }

  /**
   * Hidden lyrics view
   */
  public void hideLrc() {
  }

  /**
   * Initialize volume
   *
   * @param manager
   */
  protected void initVulume(LogicManager manager) {
    mLogicManager = manager;
    maxVolume = mLogicManager.getMaxVolume();
    currentVolume = mLogicManager.getVolume();
    MtkLog.i(TAG, "maxVolume:" + maxVolume + "--currentVolume:=" + currentVolume);
    boolean isMute = mLogicManager.isMute();
    mControlView.setMute(isMute);
    if (DmrHelper.isDmr()) {
      if (!isMute) {
        DmrHelper.notifyVolume(this, currentVolume, 0);
      } else {
        DmrHelper.notifyVolume(this, currentVolume, 1);
      }
    }
  }

  /**
   * Initialize control bar
   *
   * @param resource
   *            Control bar Layout resource id
   * @param mediatype
   *            Media type
   * @param controlImp
   *            ControlPlayState:control play or pause
   */
  protected void getPopView(int resource, int mediatype,
      ControlPlayState controlImp) {
    sMediaType = mediatype;
    contentView = LayoutInflater.from(MediaPlayActivity.this).inflate(
        resource, null);
    mDisPlayWidth = ScreenConstant.SCREEN_WIDTH;
    mDisPlayHeight = ScreenConstant.SCREEN_HEIGHT;
    mControlView = new ControlView(this, sMediaType, controlImp,
        contentView, mDisPlayWidth , mDisPlayHeight);
  }

  /**
   * Initialize metedata bar
   *
   * @param resource
   *            metedata bar Layout resource id

   */
  protected void getMetePopView(int resource) {
    // not init MeteData while not support video fomat
    MtkLog.i(TAG, "getMetePopView");
    if (!DivxUtil.isDivxFormatFile(getApplicationContext())) {
      return;
    }
    MtkLog.i(TAG, "getMetePopView cons layout");
    metedataView = LayoutInflater.from(MediaPlayActivity.this).inflate(
        resource, null);
    mDisPlayWidth = ScreenConstant.SCREEN_WIDTH;
    mDisPlayHeight = ScreenConstant.SCREEN_HEIGHT;
    mMeteDataView = new MeteDataView(this, metedataView, mDisPlayWidth * 4 / 5,
        mDisPlayHeight * 3 / 5);
  }

    /**
     * Show control bar
     *
     * @param topview Control bar parent view
     */
    protected void showPopUpWindow(final View topview) {
        //    Looper.myQueue().addIdleHandler(new IdleHandler() {
        //
        //      @Override
        //      public boolean queueIdle() {
        //        MtkLog
        //            .i(TAG,
        //                "---------- showPopUpWindow   IdleHandler mControlView:" + mControlView);
        //        mControlView.hiddlen(View.INVISIBLE);
        //        if(!mControlView.isShowed()) {
        //          mControlView.showAtLocation(topview,
        //                  Gravity.LEFT | Gravity.TOP, 0,
        //                  0);
        //        }
        //        // isControlBarShow = true;
        //        return false;
        //      }
        //    });
    }

  protected void showMeteWindow(final View topview) {

    Looper.myQueue().addIdleHandler(new IdleHandler() {

      @Override
      public boolean queueIdle() {
        MtkLog
            .i(TAG,
                "---------- showMeteWindow   IdleHandler mMeteDataView:" + mMeteDataView);
        if (mMeteDataView != null) {
          mMeteDataView.showAtLocation(topview,
              Gravity.LEFT | Gravity.TOP, mDisPlayWidth / 30,
              mDisPlayHeight / 30);
        }
        // mMeteDataView.hiddlen(View.GONE);
        return false;
      }
    });
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
//    if (keyCode == KeyMap.KEYCODE_VOLUME_UP || keyCode == KeyMap.KEYCODE_VOLUME_DOWN) {
//      return true;
//    }
    return super.onKeyUp(keyCode, event);
  }

  //begin by yangxiong for block one marqueeTextView
    private void blockMarquee() {
        if (mInfo!=null && mControlView !=null && mControlView.isOverFlowed()){
            mInfo.blockInfoViewMarquee(true);
        } else if(mInfo!=null){
        mInfo.blockInfoViewMarquee(false);
        }
    }
    //end yangxiong for block one marqueeTextView
  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    blockMarquee();//add by yangxiong for block marqueeText
    MtkLog.i(TAG, "keyCode:" + keyCode);
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER:
      case KeyMap.KEYCODE_MTKIR_TIMESHIFT_PAUSE: //Modified by duzhihong for solving "pause not work in 7701 "
      case KeyMap.KEYCODE_DPAD_CENTER:
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
	//Begin==>Modified by yangxiong for solving "block PLAYPAUSE key when text playing"
		 if (keyCode == KeyMap.KEYCODE_MTKIR_PLAYPAUSE){
				if (MultiMediaConstant.TEXT == sMediaType){
				  return true;
				}
			  }
	//End==>Modified by yangxiong for solving "block PLAYPAUSE key when text playing"
        if (mControlView != null) {
		//Begin==>Modified by duzhihong for solving "pause not work in 7701 "
          if(keyCode ==KeyMap.KEYCODE_MTKIR_BLUETOOTH_ENTER) {
           // if(mControlView.isPause())// modified by yx for fix 72726 
              mControlView.play();
          }else if (keyCode ==KeyMap.KEYCODE_MTKIR_TIMESHIFT_PAUSE){
           // if(mControlView.isPlaying())// modified by yx for fix 72726 
              mControlView.pause();
          }else { 	//End==>Modified by duzhihong for solving "pause not work in 7701 "
            mControlView.setMediaPlayState();
          }
        }
        if (MultiMediaConstant.VIDEO == sMediaType && mMeteDataView != null) {
          if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED) {
            // mMeteDataView.hiddlen(View.GONE);
            showMeteViewTime();
          } else {
            // mMeteDataView.hiddlen(View.VISIBLE);
            hiddleMeteView();
          }
        }
                //        reSetController();
        return true;
        // break;
      }
      case KeyMap.KEYCODE_MTKIR_INFO: {
        onInfoClick();
        return true;
      }
//      case KeyMap.KEYCODE_VOLUME_UP:
//        MtkLog.d(TAG, "KEYCODE_VOLUME_UPs MediaType = " + sMediaType);
//        if (sMediaType == MultiMediaConstant.VIDEO
//            || sMediaType == MultiMediaConstant.AUDIO) {
//          reSetController();
//        }
//        if (mLogicManager.isMute()) {
//          Log.i(TAG, "mute true");
//          onMute();
//          if (currentVolume == 0) {
//            currentVolume = mLogicManager.getVolume();
//          }
//          mLogicManager.setVolume(currentVolume);
//          if (DmrHelper.isDmr()) {
//            DmrHelper.notifyVolume(this, currentVolume, 1);
//          }
//          return true;
//        } else {
//          Log.i(TAG, "unmute true");
//        }
//        if (currentVolume == 0) {
//          currentVolume = mLogicManager.getVolume();
//        }
//        currentVolume = currentVolume + 1;
//        if (currentVolume > maxVolume) {
//          currentVolume = maxVolume;
//        }
//        mLogicManager.setVolume(currentVolume);
//        mControlView.setCurrentVolume(currentVolume);
//        if (DmrHelper.isDmr()) {
//          DmrHelper.notifyVolume(this, currentVolume, 0);
//        }
//
//        return true;
//      case KeyMap.KEYCODE_VOLUME_DOWN:
//        if (sMediaType == MultiMediaConstant.VIDEO
//            || sMediaType == MultiMediaConstant.AUDIO) {
//          reSetController();
//        }
//        if (mLogicManager.isMute()) {
//          onMute();
//          if (currentVolume == 0) {
//            currentVolume = mLogicManager.getVolume();
//          }
//          mLogicManager.setVolume(currentVolume);
//          if (DmrHelper.isDmr()) {
//            DmrHelper.notifyVolume(this, currentVolume, 1);
//          }
//          return true;
//        }
//        if (currentVolume == 0) {
//          currentVolume = mLogicManager.getVolume();
//        }
//        currentVolume = currentVolume - 1;
//        if (currentVolume < 0) {
//          currentVolume = 0;
//        }
//        mLogicManager.setVolume(currentVolume);
//        mControlView.setCurrentVolume(currentVolume);
//        if (DmrHelper.isDmr()) {
//          DmrHelper.notifyVolume(this, currentVolume, 0);
//        }
//        return true;
      case KeyMap.KEYCODE_MENU:
                //        if (!isValid()) {
                ////          return true;
                ////        }
                ////        MtkLog.d(TAG, "KeyMap.KEYCODE_MENU");
                ////        reSetController();
                ////        if (null != menuDialog && menuDialog.isShowing()) {
                ////          menuDialog.dismiss();
                ////
                ////        } else {
                ////          showDialog();
                ////        }
                ////        reSetController();
				{
					if (isValid()) {
                      showQMenu();
                    }
                return true;
			}
      case KeyMap.KEYCODE_MTKIR_SEFFECT:
        if (sMediaType == MultiMediaConstant.TEXT
        && mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED) {
          sundryDialog = new SundryDialog(this, 2);
          sundryDialog.show();
          return true;
        }
        else {
          break;
        }

      case KeyMap.KEYCODE_MTKIR_ANGLE: {
        // Util.exitMmpActivity(this);
        break;
      }
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        reSetController();
        onRepeat();
        updateInfoView();
        break;
      }
//      case KeyMap.KEYCODE_MTKIR_MUTE: {
//        if (isValid()) {
//          if (sMediaType == MultiMediaConstant.VIDEO
//              || sMediaType == MultiMediaConstant.AUDIO) {
//            reSetController();
//          }
//          onMute();
//        }
//        return true;
//      }
      case KeyMap.KEYCODE_BACK: {
        handBack();
        break;
      }
      case KeyMap.KEYCODE_MTKIR_GUIDE: {
//        if (isValid()) {
//          EPG_KEY_PRESS = true;
//          EPG_KEY_PRESS = Util.startEPGActivity(this);
//        }
        break;
      }
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  protected void handleDmrPlayPause(int state) {
    Log.i(TAG, "state:" + state);
    reSetController();
    if (mControlView != null) {
      mControlView.setMediaPlayState();
    }
    if (state == DmrHelper.DLNA_DMR_PAUSE) {
      Log.d(TAG, "handleDmrPlayPause");
      DmrHelper.tellDmcState(this, 1);
    } else {
      Log.d(TAG, "handleDmrPlayPause");
      DmrHelper.tellDmcState(this, 0);
    }
  }

  protected void handleDmrStop() {
    DmrHelper.tellDmcState(this, 2);
  }

  protected void handBack() {
    dismissMenuDialog();
    dismissNotSupprot();
    removeControlView();
    removeMeteDataView();
  }

  /**
   * Is the current key down valid
   *
   * @return true:valid,false:invalid
   */
  protected boolean isValid() {
    long currentTime = System.currentTimeMillis();
    if ((currentTime - mLastKeyDownTime) >= KEY_DURATION) {
      mLastKeyDownTime = currentTime;
      return true;
    } else {
      MtkLog.i(TAG, " key down duration :"
          + (currentTime - mLastKeyDownTime) + "< 400 mmm");
      mLastKeyDownTime = currentTime;
      return false;
    }
  }

    /**
     * Show or hidden info menu
     */
    private void onInfoClick() {
//        MtkLog.d(TAG, "onInfoClick playExce:" + playExce);
//        //    if (playExce == PlayException.FILE_NOT_SUPPORT
//        //        || mLogicManager.getVideoPlayStatus() < VideoConst.PLAY_STATUS_STARTED
//        //        || mLogicManager.getVideoPlayStatus() > VideoConst.PLAY_STATUS_STOPPED) {
//        //      return;
//        //    }
//        if (mControlView != null && sMediaType == MultiMediaConstant.VIDEO && !mControlView.isBottomVisible()) {
//            reSetController();
//            return;
//        } else {
//            if (mControlView != null && !mControlView.isShowed()) {
//                reSetController();
//                return;
//            }
//        }
//        if (null != mInfo && mInfo.isShowing()) {
//            if (sMediaType != MultiMediaConstant.AUDIO) {
//                hideController();
//            }
//            mInfo.dismiss();
//            return;
//        }
//        hideControllerDelay();
//        showinfoview(sMediaType);

  }

  protected void updateInfoView() {
    MtkLog.i(TAG, "updateInfoView");
    if (null != mInfo) {
       blockMarquee();//add by yangxiong for block marqueeText
      mInfo.updateView();
    }
  }

  /**
   * Set mute or resume from mute
   */
  public void onMute() {
    mIsMute = mLogicManager.isMute();
    Log.i(TAG, "mIsMute before:" + mIsMute);
    mLogicManager.setMute();
    // Added by Dan for fix bug DTV00373545
    mIsMute = !mIsMute;
    // mIsMute =mLogicManager.isMute();
    Log.i(TAG, "mIsMute later:" + mIsMute);
    // Modified by Dan for fix bug DTV00373545
    mControlView.setMute(mIsMute);
    removeScore(mIsMute);
   /* if (sMediaType == MultiMediaConstant.AUDIO) {
      if (menuDialog != null && menuDialog.isShowing()) {
        if (mIsMute) {
          menuDialog.setItemEnabled(4, false);

        } else {
          menuDialog.setItemEnabled(4, true);
        }
        String content = menuDialog.getItem(4).content;
        menuDialog.setItem(4, content);
      }
    }
*/
  }
	  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
     if (requestCode == 10086){
      reSetController();
    }
  }

  private void onMute(boolean isMute) {
    if (mControlView != null) {
      if (isMute) {
        mControlView.setMute(true);
      } else {
        mControlView.setMute(false);
        int currentVolume = mLogicManager.getVolume();
        mControlView.setCurrentVolume(currentVolume);
      }
    }
    removeScore(isMute);
    /*if (sMediaType == MultiMediaConstant.AUDIO) {
      if (menuDialog != null && menuDialog.isShowing()) {
        if (isMute) {
          menuDialog.setItemEnabled(4, false);
        } else {
          menuDialog.setItemEnabled(4, true);
        }
        String content = menuDialog.getItem(4).content;
        menuDialog.setItem(4, content);
      }
    }*/
  }

  private void updateMute() {
    mIsMute = mLogicManager.isMute();
    Log.i(TAG, "mIsMute later:" + mIsMute);
    // Modified by Dan for fix bug DTV00373545
    mControlView.setMute(mIsMute);
    removeScore(mIsMute);
  }

  /**
   * Switch repeat mode
   */
  protected void onRepeat() {
    MtkLog.i(TAG, "onRepeat~~");

    if (null == mControlView) {
      MtkLog.i(TAG, "onRepeat mControlView = null");
      return;
    }
    int type;
    switch (sMediaType) {
      case MultiMediaConstant.AUDIO: {
        type = Const.FILTER_AUDIO;
        break;
      }
      case MultiMediaConstant.VIDEO: {
        type = Const.FILTER_VIDEO;
        break;
      }
      case MultiMediaConstant.PHOTO: {
        type = Const.FILTER_IMAGE;
        break;
      }
      case MultiMediaConstant.TEXT: {
        type = Const.FILTER_TEXT;
        break;
      }
      case MultiMediaConstant.THRD_PHOTO: {
        type = Const.FILTER_IMAGE;
        break;
      }

      default:
        type = 0;
        break;
    }
    int model = mLogicManager.getRepeatModel(type);

    MtkLog.i(TAG, "onRepeat mediatype = " + type + "repeatmode = " + model);

    switch (model) {
      case Const.REPEAT_NONE: {
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(type, Const.REPEAT_ONE);
        break;
      }
      case Const.REPEAT_ONE: {
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(type, Const.REPEAT_ALL);
        break;
      }
      case Const.REPEAT_ALL: {
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(type, Const.REPEAT_NONE);
        break;

      }
      default:
        break;
    }
    // writeRepeatMode(type);
  }

  private void writeRepeatMode(int type) {
    // TODO Auto-generated method stub
    int model = mLogicManager.getRepeatModel(type);
    model = (model + 1) % 3;
    // Util.setMediaRepeatMode(getApplicationContext(), type, model);
  }

  /**
   * Show menu dialog
   */
  private void showDialog() {

    if (sMediaType == MultiMediaConstant.AUDIO) {
      if (isNotSupport || getPlayerStop()) {
        ArrayList<MenuFatherObject> menunotList = GetDataImp.getInstance()
            .getComMenu(MediaPlayActivity.this,
                R.array.mmp_menu_musicplaynotsupportlist,
                R.array.mmp_menu_musicplaynotsupportlist_enable,
                R.array.mmp_menu_musicplaynotsupportlist_hasnext);
        menuDialog = new MenuListView(MediaPlayActivity.this, menunotList,
            mListener, null);

        if (null != mLogicManager) {
          boolean isShuffle = mLogicManager
              .getShuffleMode(Const.FILTER_AUDIO);
          if (isShuffle) {
            menuDialog.setItem(1, mResources
                .getString(R.string.mmp_menu_shuffleoff));
          }
        }
        menuDialog.mControlView(MediaPlayActivity.this);
      } else {
        ArrayList<MenuFatherObject> menuList = GetDataImp.getInstance()
            .getComMenu(MediaPlayActivity.this,
                R.array.mmp_menu_musicplaylist,
                R.array.mmp_menu_musicplaylist_enable,
                R.array.mmp_menu_musicplaylist_hasnext);
        if (!hasLrc()) {
          if (menuList.size() > 4) {
            menuList.get(4).enable = false;
          }
		  mHandler.sendEmptyMessage(MSG_REFRESH_LISTVIEW);
        }

        if (isShowSpectrum()) {
         // menuList.get(4).content = mResources
            //  .getString(R.string.mmp_menu_hidescore);
        } else {
         // menuList.get(4).content = mResources
             // .getString(R.string.mmp_menu_showscore);
        }
        menuDialog = new MenuListView(MediaPlayActivity.this, menuList,
            mListener, null);

        //Begin==>Modified by yangxiong for solving "playorpause state can not refresh in menu for audio"
         String content =  mResources.getString(
                mControlView.isPlaying() ? R.string.mmp_menu_pause : R.string.mmp_menu_play);
        menuDialog.setItem(0, content);
        //End==>Modified by yangxiong for solving "playorpause state can not refresh in menu for audio"
        if (null != mLogicManager) {
          /*boolean isShuffle = mLogicManager
              .getShuffleMode(Const.FILTER_AUDIO);
          if (isShuffle) {
            menuDialog.setItem(2, mResources
                .getString(R.string.mmp_menu_shuffleoff));

          }*/
          if (mLogicManager.isMute()) {
            //menuDialog.setItemEnabled(4, false);
          } else {
           // menuDialog.setItemEnabled(4, true);
          }
        }
        menuDialog.mControlView(MediaPlayActivity.this);
      }
    } else if (sMediaType == MultiMediaConstant.VIDEO) {
      VideoManager mPlayer=VideoManager.getInstance();
      mPlayer.setEncodeing(mPlayer.getEncodeing());
      int menuid = R.array.mmp_menu_videoplaylist;
      int menuenableid = R.array.mmp_menu_videoplaylist_enable;
      int menunextid = R.array.mmp_menu_videoplaylist_hasnext;
      if (!MultiFilesManager.isSourceLocal(getApplicationContext())) {
        menuid = R.array.mmp_menu_videoplaylist_net;
        menuenableid = R.array.mmp_menu_videoplaylist_enable_net;
        menunextid = R.array.mmp_menu_videoplaylist_hasnext_net;
      }
      if (DivxUtil.isDivxSupport(this) && DivxUtil.isDivxFormatFile(this)) {
        menuid = R.array.mmp_menu_videoplaylist_divx;
        menuenableid = R.array.mmp_menu_videoplaylist_enable_divx;
        menunextid = R.array.mmp_menu_videoplaylist_hasnext_divx;
        if (!MultiFilesManager.isSourceLocal(getApplicationContext())) {
          menuid = R.array.mmp_menu_videoplaylist_divx_net;
          menuenableid = R.array.mmp_menu_videoplaylist_enable_divx_net;
          menunextid = R.array.mmp_menu_videoplaylist_hasnext_divx_net;
        }
      }

      menuDialog = new MenuListView(MediaPlayActivity.this, GetDataImp
          .getInstance().getComMenu(MediaPlayActivity.this,
              menuid,
              menuenableid,
              menunextid), mListener, mCallBack);
        //Chapter and Edition
      if (mControlView.isInABRepeat()
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_FF
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_FR
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SF
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_SR
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STEP) {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_divx_chapter), false);
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_divx_edition), false);
      }
      //Begin==>Modified by yangxiong for solving "playorpause state can not refresh in menu for video"
         String content =  mResources.getString(
                mControlView.isPlaying() ? R.string.mmp_menu_pause : R.string.mmp_menu_play);
        menuDialog.setItem(0, content);
        //End==>Modified by yangxiong for solving "playorpause state can not refresh in menu for video"
		
      //screen mode
      if (SCREENMODE_NOT_SUPPORT
          || mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED
          || Util.isUseExoPlayer()) {
        //menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_screenmode), false);
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_screenmode));
        if (index != -1){
       // menuDialog.setItemEnabled(4, false);
        }
      } else {
       // menuDialog.setItemEnabled(4, true);
      }
      //last memory
      if (mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STOPPED
          || Util.isUseExoPlayer()) {
        //menuDialog.setItemEnableState(mResources.getString(R.string.mmp_last_memory), false);
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_last_memory));
        if (index != -1){
        menuDialog.setItemEnabled(6, false);
        }
      } else {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_last_memory), true);
      }
      MtkLog.d(TAG, "mLogicManager.getMediaType():" + mLogicManager.getMediaType()
          + "  mLogicManager.getTSVideoNum():" + mLogicManager.getTSVideoNum());
      if (mLogicManager.getMediaType() == FileConst.MEDIA_TYPE_MPEG2_TS
          && mLogicManager.getTSVideoNum() > 0 && mLogicManager.isInPlaybackState()) {
        menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_ts_program), true);
      } else {
        //menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_ts_program), false);
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_ts_program));
        if (index != -1){
        menuDialog.setItemEnabled(6, true);
        }
      }
//      if (ABRpeatType.ABREPEAT_TYPE_NONE != mControlView.getRepeatAB()) {
//        menuDialog.setItemEnabled(0, false);
//      } else {
//        menuDialog.setItemEnabled(0, true);
//      }

      if (SCREENMODE_NOT_SUPPORT) {
        //menuDialog.setItemEnableState(mResources.getString(R.string.mmp_menu_pic_setting), false);
        int index = menuDialog.getItemIndex(mResources.getString(R.string.mmp_menu_pic_setting));
        if (index != -1){
        menuDialog.setItemEnabled(4, false);
        }
      } else {
        menuDialog.setItemEnabled(4, true);
      }

      // if(MultiFilesManager.isSourceLocal(getApplicationContext())){
      // MtkLog.i(TAG, "isSourceLocal true");
      // menuDialog.setItemEnabled(6, true);
      // }else{
      // MtkLog.i(TAG, "isSourceLocal false");
      // menuDialog.setItemEnabled(6, false);
      // }
    } else if (sMediaType == MultiMediaConstant.TEXT) {
      menuDialog = new MenuListView(MediaPlayActivity.this, GetDataImp
          .getInstance().getComMenu(MediaPlayActivity.this,
              R.array.mmp_menu_textplaylist,
              R.array.mmp_menu_textplaylist_enable,
              R.array.mmp_menu_textplaylist_hasnext), mListener,
              mCallBack);
      //menuDialog.setItemEnabled(0, !isNotSupport);
      menuDialog.setItemEnabled(2, !isNotSupport);

      if (null != mLogicManager) {
        boolean isShuffle = mLogicManager
            .getShuffleMode(Const.FILTER_TEXT);
        if (isShuffle) {
          menuDialog.setItem(1, mResources
              .getString(R.string.mmp_menu_shuffleoff));

        }
      }
    } else {
      menuDialog = new MenuListView(MediaPlayActivity.this, GetDataImp
          .getInstance().getComMenu(MediaPlayActivity.this,
              R.array.mmp_menu_textplaylist,
              R.array.mmp_menu_textplaylist_enable,
              R.array.mmp_menu_textplaylist_hasnext), mListener,
          null);
    }

    if (null != mControlView && (!isNotSupport || sMediaType == MultiMediaConstant.TEXT)
        && !getPlayerStop()) {
      if (mControlView.isPlaying()) {
        //menuDialog.setItem(0, mResources.getString(R.string.mmp_menu_pause));
      } else {
        //menuDialog.setItem(0, mResources.getString(R.string.mmp_menu_play));
      }
    }
    menuDialog.setMediaType(sMediaType);
    menuDialog.show();
  }

  boolean isFromStop = false;


    @Override
	protected void onPause() {
	Util.LogLife(TAG, "onPause");
    if(mLogicManager.isAudioOnly()){
         mLogicManager.setAudioOnly(false);
     }
    if (this instanceof VideoPlayActivity) {
      boolean isPIP = isInPictureInPictureMode();
      Util.mIsEnterPip = isPIP;
    }
    Util.LogLife(TAG, "onPause isPIP:" + Util.mIsEnterPip);
    // TODO Auto-generated method stub
    if (!Util.mIsEnterPip
        && GetCurrentTask.getInstance(MediaPlayActivity.this).isCurTaskTKUI()) {
      Util.LogLife(TAG, "onPause is not PIP handle root menu:");
      handleRootMenuEvent();
    }
	  super.onPause();

	}

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onStop() {
    if (null != mInfo && mInfo.isShowing()) {
      mInfo.onDismiss(mInfo);
      mInfo.dismiss();
      mInfo = null;
    }
    super.onStop();
    Util.LogLife(TAG, "onStop Util.mIsEnterPip:" + Util.mIsEnterPip);
    if (!Util.isMMpActivity(getApplicationContext())
        && !Util.mIsEnterPip) {
      Util.LogLife(TAG, "top is not mmp, go to finish.");
      isFromStop = true;
      handleRootMenuEvent();
      MediaPlayActivity.this.finish();
    }

    Util.LogLife(TAG, "onStop");
  }

  public void finishSetting() {
    if (isSetPicture) {
      SettingActivity.getInstance().finish();
      isSetPicture = false;
    }
    if (AudioSettingActivity.getInstance() != null) {
      AudioSettingActivity.getInstance().finish();
    }
  }

  public void resetResource() {

  }

  public void resetListener() {

  }

  /**
   * Dismiss control bar
   */
  protected void removeControlView() {
    if (mControlView != null && mControlView.isShowing()) {
      try {
        mControlView.dismiss();
      } catch (Exception e) {
        // TODO: handle exception
      }
      mControlView = null;
      contentView = null;
    }
  }

  /**
   * Dismiss control bar
   */
  protected void removeMeteDataView() {
    if (mMeteDataView != null && mMeteDataView.isShowing()) {
      try {
        mMeteDataView.dismiss();
      } catch (Exception e) {
        // TODO: handle exception
      }
      mMeteDataView = null;
      metedataView = null;
    }
  }

  /**
   * Dismiss control bar
   */
  protected void removeMenuDialog() {
    if (menuDialog != null && menuDialog.isShowing()) {
      try {
        menuDialog.dismiss();
      } catch (Exception e) {
        // TODO: handle exception
      }
      menuDialog = null;
    }
    if (menuDialogFontList != null && menuDialogFontList.isShowing()) {
      try {
        menuDialogFontList.dismiss();
      } catch (Exception e) {
        // TODO: handle exception
      }
      menuDialogFontList = null;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onDestroy() {
    if (mAudioManager != null) {
      if (this instanceof VideoPlayActivity) {
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
      }
      mAudioManager = null;
    }
    removeControlView();
    removeMeteDataView();
    removeMenuDialog();
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      mHandler = null;
    }
    ((MmpApp) getApplication()).removeRootMenuListener(mRootMenuListener);
    ((MmpApp) getApplication()).remove(this);
    super.onDestroy();
    // add for fix bug DTVDTV00392376
    mIsActiveLiving = false;
    if (isNeedStartNewActivity) {
      isNeedStartNewActivity = false;
      DmrHelper.handleStart();
    }
    unregisterReceiver(mReceiver);
    Util.LogLife(TAG, "onDestroy pip:" + Util.mIsEnterPip);
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    if (!((MmpApp) getApplication()).isEnterMMP()) {
      mLogicManager.restoreVideoResource();
      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
    }
    hideQMenu();
  }


  public void showQMenu() {
      //added by zhangqing
      if (null != mQMenuManager) {
        try {
          int type = getMediaType();
          Map<String, Boolean> maps = new HashMap<>();
          maps = getQMenuMaps(type);
          mQMenuManager.ShowQMenuByItem(maps);
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        // Action = "android.media.tv.QMENU"
        String pkg = "skyworth.skyworthlivetv";
        String cls = "skyworth.skyworthlivetv.global.service.QMenuManagerService";
        Intent intent = checkIntentExist(this, pkg, cls);
        if (null != intent) {
          bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
      }
  }

    //added by zhangqing
  Map<String, Boolean> getQMenuMaps(int type) {
    Map<String, Boolean> maps = new HashMap<>();
    switch (type) {
      case MultiMediaConstant.PHOTO:
        maps = getQMenuItem(false, false, true,
            true, false, false, false);
        break;
      case MultiMediaConstant.VIDEO:
        maps = getQMenuItem(true, true, true,
            true, false, false, false);
        break;
      case MultiMediaConstant.AUDIO:
        maps = getQMenuItem(false, true, true,
            true, false, false, false);
        break;
    }
    return maps;
  }

  //added by zhangqing
  private Intent checkIntentExist(Context context, String pkg, String cls) {
    PackageManager pm = context.getPackageManager();
    Intent intent = new Intent();
    ComponentName component = new ComponentName(pkg, cls);
    intent.setComponent(component);

    List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);
    if (null == resolveInfos || 0 == resolveInfos.size()) {
      return null;
    }
    return intent;
  }

  //add by zhangqing
  Map<String, Boolean> getQMenuItem(boolean picEnabled, boolean soundEnabled, boolean
      sleepEnabled, boolean inputEnabled, boolean moreEnabled, boolean sourceSetEnabled,
      boolean applinkEnabled) {
    Map<String, Boolean> maps = new HashMap<>();
    if (picEnabled) {
      maps.put("PictureMode", picEnabled);
    }

    if (soundEnabled) {
      maps.put("SoundMode", soundEnabled);
    }

    if (sleepEnabled) {
      maps.put("SleepTime", sleepEnabled);
    }

    if (inputEnabled) {
      maps.put("InputSource", inputEnabled);
    }

    if (moreEnabled) {
      maps.put("More", moreEnabled);
    }

    if (sourceSetEnabled) {
      maps.put("SourceSetup", sourceSetEnabled);
    }

    if (applinkEnabled) {
      maps.put("AppLink", applinkEnabled);
    }
    return maps;
  }

  public void hideQMenu() {
    if (null != mQMenuManager){
      try {
        mQMenuManager.ShowQMenu(false);
        mQMenuManager.DismissPictureModeUserDialog();
      } catch (RemoteException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Click menu item callback
   *
   * @param content
   *            menu item value
   */
  private void controlState(String content) {
    MtkLog.d(TAG, "controlState content:" + content);
    menuDialog.hideMenuDelay();
    if (sMediaType == MultiMediaConstant.AUDIO) {
      if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
        // Util.setMediaRepeatMode(getApplicationContext(), MultiMediaConstant.AUDIO,Util.NONE);
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
            Const.REPEAT_NONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatone))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.AUDIO,Util.REPEATE_ONE);
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
            Const.REPEAT_ONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatall))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.AUDIO,Util.REPEATE_ALL);
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
            Const.REPEAT_ALL);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_pause))) {
        mControlView.setMediaPlayState();
        menuDialog.initItem(0, mResources
            .getString(R.string.mmp_menu_play));
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_play))) {
        mControlView.setMediaPlayState();
        menuDialog.initItem(0, mResources
            .getString(R.string.mmp_menu_pause));
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_shuffleon))) {
        if (isNotSupport || getPlayerStop()) {
          mControlView.setShuffleVisble(View.VISIBLE);
          /*menuDialog.initItem(1, mResources
              .getString(R.string.mmp_menu_shuffleoff));*/
          mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_ON);
        } else {
          mControlView.setShuffleVisble(View.VISIBLE);
          /*menuDialog.initItem(2, mResources
              .getString(R.string.mmp_menu_shuffleoff));*/
          mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_ON);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_shuffleoff))) {
        if (isNotSupport || getPlayerStop()) {
          mControlView.setShuffleVisble(View.INVISIBLE);
          /*menuDialog.initItem(1, mResources
              .getString(R.string.mmp_menu_shuffleon));*/
          mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_OFF);
        } else {
          mControlView.setShuffleVisble(View.INVISIBLE);
          /*menuDialog.initItem(2, mResources
              .getString(R.string.mmp_menu_shuffleon));*/
          mLogicManager.setShuffle(Const.FILTER_AUDIO, Const.SHUFFLE_OFF);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_showinfo))) {
        showinfoview(sMediaType);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_lyricoptions))) {
        menuDialog.dismiss();
        menuDialog = new MenuListView(
            MediaPlayActivity.this,
            GetDataImp.getInstance().getComMenu(
                MediaPlayActivity.this,
                R.array.mmp_menu_lyricplaylist,
                R.array.mmp_menu_lyricplaylist_enable,
                R.array.mmp_menu_lyricplaylist_hasnext),
            mListener, null);
        menuDialog.show();
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_showscore))) {
        isHideSperum = false;
       // menuDialog.initItem(4, mResources
        //    .getString(R.string.mmp_menu_hidescore));
       // menuDialog.setSelectShowText(mResources
        //    .getString(R.string.mmp_menu_hidescore));
        removeScore(false);

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_hidescore))) {
        //menuDialog.initItem(4, mResources
          //  .getString(R.string.mmp_menu_showscore));
        //isHideSperum = true;
        menuDialog.setSelectShowText(mResources
            .getString(R.string.mmp_menu_showscore));
        removeScore(true);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_singleline))) {
        mPerLine = 1;
        mLogicManager.lrcHide = false;
        setLrcLine(mPerLine);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_multiline))) {
        mPerLine = 8;
        mLogicManager.lrcHide = false;
        setLrcLine(mPerLine);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_off))) {
        // Modified by Dan for fix bug DTV00389330&DTV00389362
        if (menuDialog.isInLrcOffsetMenu()) {
          mLogicManager.setLrcOffsetMode(0);
        } else {
          mPerLine = 0;
          mLogicManager.lrcHide = true;
          hideLrc();
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_onlyaudio))) {
        mLogicManager.setAudioOnly(true);
        MtkLog.i(TAG, "  audio  only  :  "
            + mLogicManager.isAudioOnly());
        // add by keke 1229 for DTV00386510
        dismissMenuDialog();
        // keke end

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_auto))) {
        // Modified by Dan for fix bug DTV00389362
        if (menuDialog.isInLrcOffsetMenu()) {
          mLogicManager.setLrcOffsetMode(1);
        } else if (menuDialog.isInEncodingMenu()) {
          mLogicManager.setLrcEncodingMode(0);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_userdefine))) {
        // Added by Dan for fix bug DTV00389362
        if (menuDialog.isInLrcOffsetMenu()) {
          mLogicManager.setLrcOffsetMode(2);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_gb))) {
        if (menuDialog.isInEncodingMenu()) {
          mLogicManager.setLrcEncodingMode(1);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_big5))) {
        if (menuDialog.isInEncodingMenu()) {
          mLogicManager.setLrcEncodingMode(2);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_utf8))) {
        if (menuDialog.isInEncodingMenu()) {
          mLogicManager.setLrcEncodingMode(3);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_utf16))) {
        if (menuDialog.isInEncodingMenu()) {
          mLogicManager.setLrcEncodingMode(4);
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_audio_setting))) {
        dismissMenuDialog();
        if (mControlView != null && mControlView.isShowed()) {
          hideController();
        }
        Intent intent = new Intent(MmpConst.INTENT_AUDIO_SETTING);
        intent.putExtra("fromwhere", 1);
        //startActivity(intent);
		startActivityForResult(intent,10086);
      }
      return;
    }
    if (sMediaType == MultiMediaConstant.TEXT) {
      if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
        // Util.setMediaRepeatMode(getApplicationContext(), MultiMediaConstant.TEXT,Util.NONE);
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(Const.FILTER_TEXT,
            Const.REPEAT_NONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatone))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.TEXT,Util.REPEATE_ONE);
        mControlView.setRepeatSingle();
        mLogicManager
            .setRepeatMode(Const.FILTER_TEXT, Const.REPEAT_ONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatall))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.TEXT,Util.REPEATE_ONE);
        mControlView.setRepeatAll();
        mLogicManager
            .setRepeatMode(Const.FILTER_TEXT, Const.REPEAT_ALL);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_pause))) {
        mControlView.setMediaPlayState();
        menuDialog.initItem(0, mResources
            .getString(R.string.mmp_menu_play));
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_play))) {
        mControlView.setMediaPlayState();
        menuDialog.initItem(0, mResources
            .getString(R.string.mmp_menu_pause));
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_shuffleon))) {
        menuDialog.initItem(1, mResources
            .getString(R.string.mmp_menu_shuffleoff));
        mControlView.setShuffleVisble(View.VISIBLE);
        // Modified by Dan for fix bug DTV00375629
        mLogicManager.setShuffle(Const.FILTER_TEXT, Const.SHUFFLE_ON);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_shuffleoff))) {
        menuDialog.initItem(1, mResources
            .getString(R.string.mmp_menu_shuffleon));
        mControlView.setShuffleVisble(View.INVISIBLE);
        // Modified by Dan for fix bug DTV00375629
        mLogicManager.setShuffle(Const.FILTER_TEXT, Const.SHUFFLE_OFF);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_showinfo))) {
        showinfoview(sMediaType);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_font))) {
        menuDialog.dismiss();
        menuDialogFontList = new MenuListView(
            MediaPlayActivity.this, GetDataImp.getInstance()
                .getComMenu(MediaPlayActivity.this,
                    R.array.mmp_menu_fontlist,
                    R.array.mmp_menu_fontlist_enable,
                    R.array.mmp_menu_fontlist_hasnext),
            mListener, null);
        menuDialogFontList.show();
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_small))) {
        setFontSize(TextUtils.SMALLSIZE);
        // reflashPageNumber();

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_medium))) {
        setFontSize(TextUtils.MEDSIZE);
        // reflashPageNumber();

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_large))) {
        setFontSize(TextUtils.LARSIZE);
        // reflashPageNumber();

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_regular))) {
        setFontStyle(Typeface.NORMAL);

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_italic))) {
        setFontStyle(Typeface.ITALIC);

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_bold))) {
        setFontStyle(Typeface.BOLD);

      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_outline))) {
        setFontStyle(Typeface.BOLD_ITALIC);

      } else {
        setFontColor(changeContent(content));
      }
      return;
    }
    if (sMediaType == MultiMediaConstant.VIDEO) {
		VideoManager mPlayer=VideoManager.getInstance();
		 if (LogicManager.curMenuListType == LogicManager.MENULIST_SUBTTILE_ENCODING
            && content.equals(mEncodingArray[0])){//modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_AUTO);
        }else if(content.equals(mEncodingArray[1])){
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_UTF8);
        }else if(content.equals(mEncodingArray[2])){
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_UTF16);
        }else if(content.equals(mEncodingArray[3])){
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_BIG5);	
        }else if(content.equals(mEncodingArray[4])){
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_GB);
        }else if(content.equals(mEncodingArray[5])){			
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_GB18030);	
        }else if(content.equals(mEncodingArray[6])){			
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_KOI8_R);
        }else if(content.equals(mEncodingArray[7])){
			mPlayer.setEncodeing(SubtitleAttr.SbtlFontEnc.SBTL_FONT_ENC_ASCI);
        }else if (content.equals(mResources.getString(R.string.mmp_menu_none))) {
        // Util.setMediaRepeatMode(getApplicationContext(), MultiMediaConstant.VIDEO,Util.NONE);
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
            Const.REPEAT_NONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatone))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.VIDEO,Util.REPEATE_ONE);
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
            Const.REPEAT_ONE);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_repeatall))) {
        // Util.setMediaRepeatMode(getApplicationContext(),
        // MultiMediaConstant.VIDEO,Util.REPEATE_ALL);
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(Const.FILTER_VIDEO,
            Const.REPEAT_ALL);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_pause))) {

        mControlView.setMediaPlayState();
        if (!mLogicManager.isPlaying()) {
          menuDialog.initItem(0,
              mResources.getString(R.string.mmp_menu_play));
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_play))) {

        mControlView.setMediaPlayState();
        if (mLogicManager.isPlaying()) {
          if (Util.mIsDolbyVision){
            Util.showDoViToast(this);
          }
          menuDialog.initItem(0,
              mResources.getString(R.string.mmp_menu_pause));
        }
      } else if (content.startsWith(mResources
          .getString(R.string.mmp_menu_ts_program) + " ")
          && content.length() > mResources.getString(R.string.mmp_menu_ts_program).length() + 1) {
        playExce = PlayException.DEFAULT_STATUS;
        dismissNotSupprot();
        String index = content.substring(content.indexOf(" ") + 1);
        MtkLog.d(TAG, "controlState index :" + index);
        try {
          int pos = Integer.parseInt(index);
          mLogicManager.setTSProgram(pos);
        } catch (Exception e) {
          // TODO: handle exception
        }
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_showinfo))) {
        showinfoview(sMediaType);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_user))) {
        mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_USER);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_cinema))) {
        mLogicManager
            .setPictureMode(ModelConstant.PICTURE_MODEL_CINEMA);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_sport))) {
        mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_SPORT);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_vivid))) {
        mLogicManager.setPictureMode(ModelConstant.PICTURE_MODEL_VIVID);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_hibright))) {
        mLogicManager
            .setPictureMode(ModelConstant.PICTURE_MODEL_HIBRIGHT);
      } else if (LogicManager.curMenuListType == LogicManager.MENULIST_SCREEN_MODE
            && content.equals(mResources//modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
          .getString(R.string.mmp_menu_auto))) {
        setScreenMode(CommonSet.VID_SCREEN_MODE_AUTO);
        // mLogicManager.setScreenMode(CommonSet.VID_SCREEN_MODE_AUTO);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_normal))) {
        setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_letterbox))) {
        setScreenMode(CommonSet.VID_SCREEN_MODE_LETTER_BOX);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_scan))) {
        setScreenMode(CommonSet.VID_SCREEN_MODE_PAN_SCAN);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_zoom))) {
        setScreenMode(CommonSet.VID_SCREEN_MODE_NON_LINEAR_ZOOM);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_dotbydot))) {

        setScreenMode(CommonSet.VID_SCREEN_MODE_DOT_BY_DOT);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_pic_setting))) {
        dismissMenuDialog();
        if (mControlView != null && mControlView.isShowed()) {
          hideController();
        }
        isSetPicture = true;
        Intent intent = new Intent(MmpConst.INTENT_SETTING);
        intent.putExtra("fromwhere", 1);
        startActivity(intent);
      } else if (content.equals(mResources
          .getString(R.string.mmp_menu_audio_setting))) {
        dismissMenuDialog();
        if (mControlView != null && mControlView.isShowed()) {
          hideController();
        }
        Intent intent = new Intent(MmpConst.INTENT_AUDIO_SETTING);
        intent.putExtra("fromwhere", 1);
        //startActivity(intent);
		startActivityForResult(intent,10086);
      } else if (content.equals(mResources
          .getString(R.string.mmp_last_memory_off))) {
        MtkLog.i(TAG, "MEMEORY OFF");
        LastMemory.clearLastMemory(getApplicationContext());
        SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID,
            LastMemory.LASTMEMORY_OFF);

      } 
	  //Begin==>Modified by yangxiong for solving "delete position item and change tittle 'time' to 'on' for mmp lastmemory"
	  else if (content.equals(mResources
          .getString(R.string.menu_video_mjc_demo_on))) {
        MtkLog.i(TAG, "MEMEORY TIME");
        LastMemory.clearLastMemory(getApplicationContext());
        SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID,
            LastMemory.LASTMEMORY_TIME);
      } /*else if (content.equals(mResources
          .getString(R.string.mmp_last_memory_position))) {
        LastMemory.clearLastMemory(getApplicationContext());
        MtkLog.i(TAG, "MEMEORY POSITION");
        SaveValue.getInstance(getApplicationContext()).saveValue(LastMemory.LASTMEMORY_ID,
            LastMemory.LASTMEMORY_POSITION);
      } */
	  
	  //End==>Modified by yangxiong for solving "delete position item and change tittle 'time' to 'on' for mmp lastmemory"
	  else {
        if (content.startsWith(mResources.getString(R.string.mmp_divx_title))) {
          int index = Integer.valueOf(content.substring(content.lastIndexOf("_") + 1));
          MtkLog.i(TAG, "index:" + index);
          index = DivxUtil.getDivxTitleIndex(this, index);
          // mLogicManager.stopDrmVideo();
          // DivxTitleInfo titleinfo = new DivxTitleInfo(index,-1,-1);
          // mLogicManager.setDivxTitleInfo(titleinfo);

          mControlView.pause();
          mControlView.showPausePlayIcon(false);
          switchPlay();
          DivxUtil.setChapterChanged(true);
          // setDivxTitleVideo(index);
          showDrmDialog(index);
        } else if (content.startsWith(mResources.getString(R.string.mmp_divx_edition))) {
          int index = Integer.valueOf(content.substring(content.lastIndexOf("_") + 1));
          index = DivxUtil.getDivxEditionIndex(this, index);
          DivxPlayListInfo playlistinfo = new DivxPlayListInfo(index, -1, -1, 0);
          switchPlay();
          mLogicManager.setDivxPlayListInfo(playlistinfo);

        } else if (content.startsWith(mResources.getString(R.string.mmp_divx_chapter))) {
          int index = Integer.valueOf(content.substring(content.lastIndexOf("_") + 1));
          index = DivxUtil.getDivxChapterIndex(this, index);
          DivxChapInfo chapinfo = new DivxChapInfo(index, -1, -1, 0);
          switchPlay();
          mLogicManager.setDivxChapInfo(chapinfo);
        }

      }
      return;
    }
  }

  // add by keke for fix DTV00380564
  protected void setScreenMode(int screenmode) {
    mLogicManager.videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
    if (null != mControlView) {
      mControlView.setZoomSize();
    }
    mLogicManager.setScreenMode(screenmode);
    TVStorage.getInstance(this).set("SCREENMODE_FILELIST",
        screenmode + "");

  }

  /**
   * Refresh control bar page number
   */
  protected void reflashPageNumber() {

  }

  /**
   * Change the content to match common logic
   *
   * @param content
   * @return
   */
  private int changeContent(String content) {
    int color = Color.WHITE;
    if (mResources.getString(R.string.mmp_menu_red).equals(content)) {
      color = Color.RED;

    } else if (mResources.getString(R.string.mmp_menu_green)
        .equals(content)) {
      color = Color.GREEN;

    } else if (mResources.getString(R.string.mmp_menu_black)
        .equals(content)) {
      color = Color.BLACK;

    } else if (mResources.getString(R.string.mmp_menu_white)
        .equals(content)) {
      color = Color.WHITE;

    } else if (mResources.getString(R.string.mmp_menu_blue).equals(content)) {
      color = Color.BLUE;

    }

    return color;

  }

  /**
   * Show info view
   *
   * @param type
   */
  protected void showinfoview(int type) {
    int resid;
    switch (type) {
      case MultiMediaConstant.AUDIO: {
        resid = R.layout.mmp_musicinfo;
        break;
      }
      case MultiMediaConstant.PHOTO:
      case MultiMediaConstant.THRD_PHOTO: {
        resid = R.layout.mmp_photoinfo;
        break;
      }
      case MultiMediaConstant.VIDEO: {
        resid = R.layout.mmp_videoinfo;
        break;
      }
      case MultiMediaConstant.TEXT: {
        resid = R.layout.mmp_textinfo;
        break;
      }
      default:
        return;
    }
    View contentView = LayoutInflater.from(MediaPlayActivity.this).inflate(
        resid, null);
    mInfo = new ShowInfoView(this, contentView, type, mLogicManager);
    dismissMenuDialog();
    blockMarquee();//add by yangxiong for block marqueeText
    mInfo.show();
  }

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
   * Send a delay message to hidden control bar
   */
  protected void hideControllerDelay() {
      if (mHandler != null && sMediaType == MultiMediaConstant.TEXT){
          if(mHandler.hasMessages(MSG_HIDE_CONTROLLER))
          {
              mHandler.removeMessages(MSG_HIDE_CONTROLLER);
          }
          mHandler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 10000);
      }
      
  }

//  protected void hideMeteDataDelay() {
//
//  }

  /**
   * Recount hidden control bar delay time
   */
  protected void reSetController() {
    showController();
    hideControllerDelay();
  }

//  protected void reSetMeteView() {
//    showMeteViewTime();
//  }

  /**
   * Hidden Control bar
   */
  protected void hideController() {
	  MtkLog.i("yangxiong", "isPlaying :"+mLogicManager.isPlaying()+"~~~~yx:"+VideoManager.getInstance().isNormalSpeed());
        if (mControlView != null && sMediaType == MultiMediaConstant.VIDEO){
				if(!mLogicManager.isPlaying()) {//pause
                mControlView.setControlbottomHide(View.INVISIBLE);
                return;
				}else{
				if(!VideoManager.getInstance().isNormalSpeed()) {
                mControlView.setControlbottomHide(View.INVISIBLE);
                return;
				}
				}
        }
    MtkLog.d(TAG, "hideController::menuDialog = " + menuDialog);
    if (null != menuDialog && menuDialog.isShowing()) {
        MtkLog.d(TAG, "hideController::menuDialog.isShowing()=true,return");
        return;
    }
    if (mHandler != null && mControlView != null && mControlView.isShowed()) {
      // add by shuming for fix CR: DTV00407914
      mControlView.hiddlen(View.INVISIBLE);
      // mControlView.update(mDisPlayWidth / 10, mDisPlayHeight*20, -1, -1);
      if (mHandler != null) {
        mHandler.sendEmptyMessageDelayed(MSG_HIDE_INFORBAR_POINT, 70);
      }
    }
    // isControlBarShow = false;
    removeProgressMessage();
  }

  /**
   * Remove to get progress inforamtion and time information message.
   */
  protected void removeProgressMessage() {

  }

  /**
   * Add to get progress inforamtion and time information message
   */
  protected void addProgressMessage() {

  }

  /**
   * Show control bar
   */
  protected void showController() {
    if (mControlView != null && sMediaType == MultiMediaConstant.VIDEO){
            mControlView.hiddlen(View.VISIBLE);
            addProgressMessage();
            return;
        }
    if (mControlView != null && !mControlView.isShowed()) {
      mControlView.hiddlen(View.VISIBLE);
      // isControlBarShow = true;
    }
    addProgressMessage();
  }

  protected void showMeteViewTime() {

  }

  /**
   * hiddle MeteView bar
   */
  protected void hiddleMeteView() {
    MtkLog.i(TAG, "hiddleMeteView");
    if (mMeteDataView != null) {
      mMeteDataView.hiddlen(View.GONE);
    } else {
      MtkLog.i(TAG, "showMeteView!=NULL");
    }
  }

  /**
   * Get lines number per screen
   *
   * @return int lines number
   */

  // change by xudong.chen 20111204 fix DTV00379662
  public static int getPerLine() {
    return mPerLine;
  }

  // end

  /**
   * Get media type
   *
   * @return int type:photo audio video text
   */
  public static int getMediaType() {
    return sMediaType;
  }

  /**
   * blue screen status. true to blue screen.
   */
  /**
   * Set bule dialog status.
   * @param status
   */
  /**
   * Get current blue dialog status.
   * @return
   */
  // add by keke for fix DTV00383992
  protected void hideFeatureNotWork() {
    if (mHandler != null) {
      mHandler.sendEmptyMessage(MSG_DISMISS_FEARTURE_NOT_SUPPORT);
    }
  }

  protected boolean getPlayerStop() {
    return false;
  }

  protected void setFontSize(float size) {

  }

  protected void setFontStyle(int style) {

  }

  protected void setFontColor(int color) {

  }

  public void updateIndex() {
    if (null != mControlView && !isDmrSource) {
      mControlView.setFilePosition(mLogicManager.getImagePageSize());
      mControlView.setFileName(mLogicManager.getPhotoName());
    }
  }

  public void setRepeatMode() {
    MtkLog.i(TAG, "setRepeatMode");

    if (mControlView == null || mLogicManager == null) {
      MtkLog.i(TAG, "mControlView==null||mLogicManager==null");
      return;
    }
    int type_util = MmpConst.AUDIO;
    int type_const = Const.FILTER_AUDIO;
    switch (sMediaType) {
      case MultiMediaConstant.AUDIO:
        type_util = MmpConst.AUDIO;
        type_const = Const.FILTER_AUDIO;
        break;
      case MultiMediaConstant.VIDEO:
        type_util = MmpConst.VIDEO;
        type_const = Const.FILTER_VIDEO;
        break;
      case MultiMediaConstant.PHOTO:
        type_util = MmpConst.PHOTO;
        type_const = Const.FILTER_IMAGE;
        break;
      case MultiMediaConstant.TEXT:
        type_util = MmpConst.TXT;
        type_const = Const.FILTER_TEXT;
        break;
    }

    // if (sMediaType == MultiMediaConstant.AUDIO) {
    // int value = Util.getMediaRepeatMode(getApplicationContext(), type_util);
    MtkLog.i(TAG, "setRepeatMode type_util:" + type_util + "--type_const:" + type_const);
    int value = mLogicManager.getRepeatModel(type_const);
    switch (value) {
      case MmpConst.NONE:
        MtkLog.i(TAG, "setRepeatMode NONE");
        mControlView.setRepeatNone();
        mLogicManager.setRepeatMode(type_const,
            Const.REPEAT_NONE);
        break;
      case MmpConst.REPEATE_ONE:
        MtkLog.i(TAG, "setRepeatMode REPEATE_ONE");
        mControlView.setRepeatSingle();
        mLogicManager.setRepeatMode(type_const,
            Const.REPEAT_ONE);
        break;
      case MmpConst.REPEATE_ALL:
        MtkLog.i(TAG, "setRepeatMode REPEATE_ALL");
        mControlView.setRepeatAll();
        mLogicManager.setRepeatMode(type_const,
            Const.REPEAT_ALL);
        break;
    }
    // }
  }

  private boolean isNeedStartNewActivity = false;

  private void handleDmrEvent(int state, int param) {
    Log.i(TAG, "state:" + state);
    switch (state) {
      case DmrHelper.DLNA_DMR_STOP:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_STOP");
        handleDmrStop();
        break;
      case DmrHelper.DLNA_DMR_PAUSE:
        // if(mControlView.isPlaying()){
        Log.d(TAG, "handleDmrEvent DLNA_DMR_PAUSE");
        handleDmrPlayPause(state);
        // }
        break;
      case DmrHelper.DLNA_DMR_PLAY:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_PLAY");
        // if(!mControlView.isPlaying()){
        handleDmrPlayPause(state);
        // }
        break;
      case DmrHelper.DLNA_DMR_SEEKTIME:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SEEKTIME");
        handleDmrSeek(param);
        break;
      case DmrHelper.DLNA_DMR_SET_VOLUME:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SET_VOLUME");
        handleDmrSetVolume(param);
        break;
      case DmrHelper.DLNA_DMR_SET_MUTE:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_SET_MUTE");
        handleDmrSetMute(param);
        break;
      case DmrHelper.DLNA_DMR_STOPOLDE_STARTNEW:
        Log.d(TAG, "handleDmrEvent DLNA_DMR_STOPOLDE_STARTNEW");
        DmrHelper.handleStart();
        isNeedStartNewActivity = true;
        this.finish();
        break;
    }
    // if(state == DmrHelper.DLNA_DMR_STOP){
    // handleDmrStop();
    // }
    // if(state == DmrHelper.DLNA_DMR_PAUSE){
    // if(mControlView.isPlaying()){
    // handleDmrPlayPause(state);
    // }
    // }
    // if(state == DmrHelper.DLNA_DMR_PLAY){
    // if(!mControlView.isPlaying()){
    // handleDmrPlayPause(state);
    // }
    // }
    //
    // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
    // Log.i(TAG,"DLNA_DMR_SEEKTIME");
    // handleDmrSeek(param);
    // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
    // Log.i(TAG,"handleDmrSetVolume");
    // handleDmrSetVolume(param);
    // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
    // Log.i(TAG,"handleDmrSetMute");
    // handleDmrSetMute(param);
    // }
  }

  // private void handleDmrEventWithParam(int state,int param){
  // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
  // Log.i(TAG,"DLNA_DMR_SEEKTIME");
  // handleDmrSeek(param);
  // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
  // Log.i(TAG,"handleDmrSetVolume");
  // handleDmrSetVolume(param);
  // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
  // Log.i(TAG,"handleDmrSetMute");
  // handleDmrSetMute(param);
  // }
  // }
  public class DmrListener implements iDmrListener {

    @Override
    public void notifyNewEvent(int state) {
      // TODO Auto-generated method stub
      // if(state == DmrHelper.DLNA_DMR_STOP){
      // handleDmrStop();
      // }
      // if(state == DmrHelper.DLNA_DMR_PAUSE){
      // if(mControlView.isPlaying()){
      // handleDmrPlayPause(state);
      // }
      // }
      // if(state == DmrHelper.DLNA_DMR_PLAY){
      // if(!mControlView.isPlaying()){
      // handleDmrPlayPause(state);
      // }
      // }
      Log.i(TAG, "notifyNewEvent:" + state);
      if (mHandler != null) {
        Message msg = new Message();
        msg.what = MSG_DMR;
        msg.arg1 = state;
        mHandler.sendMessage(msg);
      }
    }

    @Override
    public void notifyNewEventWithParam(int state, int param) {
      // if(DmrHelper.DLNA_DMR_SEEKTIME== state){
      // Log.i(TAG,"DLNA_DMR_SEEKTIME");
      // handleDmrSeek(param);
      // }else if(DmrHelper.DLNA_DMR_SET_VOLUME== state){
      // Log.i(TAG,"handleDmrSetVolume");
      // handleDmrSetVolume(param);
      // }else if(DmrHelper.DLNA_DMR_SET_MUTE== state){
      // Log.i(TAG,"handleDmrSetMute");
      // handleDmrSetMute(param);
      // }
      Log.i(TAG, "notifyNewEventWithParam:" + state + " param:" + param);
      if (mHandler != null) {
        Message msg = new Message();
        msg.what = MSG_DMR;
        msg.arg1 = state;
        msg.arg2 = param;
        mHandler.sendMessage(msg);
      }
    }

    @Override
    public long getProgress() {
      // TODO Auto-generated method stub
      return getCurrentTime();
    }

    @Override
    public long getDuration() {
      // TODO Auto-generated method stub
      return getDurationTime();
    }
  }

  protected long getCurrentTime() {
    return 0;
  }

  protected long getDurationTime() {
    return 0;
  }

  protected void handleDmrSeek(int param) {

  }

  protected void handleDmrSetVolume(int currentVolume) {
    if (mLogicManager.isMute()) {
      onMute();
//      return;
    }
    Log.i(TAG, "c:" + mLogicManager.getVolume() + " max:" + mLogicManager.getMaxVolume()
        + "--setVolume:" + currentVolume);
    currentVolume = (currentVolume * mLogicManager.getMaxVolume()) / 100;
    mLogicManager.setVolume(currentVolume);
    mControlView.setCurrentVolume(currentVolume);

  }

  public void handleDmrSetMute(int param) {
    reSetController();
    onMute();
  }

  protected void showDrmDialog(int index) {
  }

  protected void setDivxTitleVideo(int index) {
  }

  protected void switchTitle() {
  }

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

    @Override
    public void onReceive(Context content, Intent intent) {
      // TODO Auto-generated method stub
      if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
        Log.i(TAG, "register receiver screen off");
        finishSetting();
        handBack();
        MediaPlayActivity.this.finish();
      } else if (intent.getAction().equals(
          AudioManager.VOLUME_CHANGED_ACTION)) {
        Log.i(TAG, "register receiver VOLUME_CHANGED_ACTION");
        currentVolume = mLogicManager.getVolume();
        if (mControlView != null)
          mControlView.setCurrentVolume(currentVolume);
      } else if (intent.getAction().equals(AudioManager.STREAM_MUTE_CHANGED_ACTION)) {
        if (mAudioManager == null) {
          mAudioManager = (AudioManager)content.getSystemService(Context.AUDIO_SERVICE);
		}
        if (mControlView != null) {
          if (mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)) {
            onMute(true);
          } else {
            onMute(false);
          }
        }
      } else if (intent.getAction().equals(AudioManager.STREAM_DEVICES_CHANGED_ACTION)) {
        int device = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_DEVICES, -1);
        int preDevice = intent.getIntExtra(AudioManager.EXTRA_PREV_VOLUME_STREAM_DEVICES, -1);
        int streamType = intent.getIntExtra(AudioManager.EXTRA_VOLUME_STREAM_TYPE, -1);
        Log.i(TAG, "onReceive device:" + device + "  preDevice:" + preDevice
            + "  streamType:" + streamType);
        if (streamType == AudioManager.STREAM_MUSIC) {
          if (mControlView != null && mControlView.isPlaying()
              && (isA2dpToSpeaker(device, preDevice))) {
           mControlView.setMediaPlayState();
          }
          currentVolume = mLogicManager.getVolume();
          if(mControlView != null) {
            mControlView.setCurrentVolume(currentVolume);
          }
        }
      }
    }
  };

  private boolean isA2dpToSpeaker(int device, int preDevice) {
    return device == AudioSystem.DEVICE_OUT_SPEAKER
        && (preDevice == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP
         || preDevice == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES
         || preDevice == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER);
  }

  private boolean isSpeakerToA2dp(int device, int preDevice) {
    return preDevice == AudioSystem.DEVICE_OUT_SPEAKER
        && (device == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP
         || device == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_HEADPHONES
         || device == AudioSystem.DEVICE_OUT_BLUETOOTH_A2DP_SPEAKER);
  }

  private void switchPlay() {
    if (mControlView != null) {
      mControlView.initRepeatAB();
    }
  }

  public void handleCIIssue(boolean isTrue) {
    MtkLog.i(TAG, "handleCIIssue:" + isTrue);
  }

  public void reInitWhenReplay() {
//    if (mControlView != null) {
//      mControlView.initVideoTrackNumber();
//    }
//    if (mControlView != null) {
//      mControlView.reinitSubtitle(mLogicManager.getSubtitleTrackNumber());
//    }
    if (mLogicManager != null) {
      mLogicManager.setPlayStatus(VideoConst.PLAY_STATUS_STARTED);
    }
  }

  public void handleRootMenuEvent() {
    if (!isFromStop) {
      Util.enterMmp(0, getApplicationContext());
    }
  }

  iRootMenuListener mRootMenuListener = new iRootMenuListener() {

    @Override
    public void handleRootMenu() {
      // TODO Auto-generated method stub
      MtkLog.i(TAG, "handleRootMenu Received!");
      finishSetting();
      handleRootMenuEvent();
    }
  };

  public void reSetUIWhenAvDbChanged() {
    if (mControlView != null) {
      mControlView.initVideoTrackNumber();
    }
    if (mControlView != null) {
      mControlView.reinitSubtitle(mLogicManager.getSubtitleTrackNumber());
    }
  }

  //start 01fix by tjs for set mediatype
  public void setMediaType(int type)
  {
      this.sMediaType = type;
  }
  //end 01
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mQMenuManager = QMenuManager.Stub.asInterface(service);
            try {
              int type = getMediaType();
              Map<String, Boolean> maps = new HashMap<>();
              maps = getQMenuMaps(type);
              mQMenuManager.ShowQMenuByItem(maps);
            } catch (RemoteException e) {
              e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    private Intent createExplicitFromImplicitIntent(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);

        if (null == resolveInfos || 0 == resolveInfos.size()){
            return null;
        }

        ResolveInfo resolveInfo = resolveInfos.get(0);
        String packageName = resolveInfo.serviceInfo.packageName;
        String className = resolveInfo.serviceInfo.name;

        ComponentName componentName = new ComponentName(packageName, className);
        Intent explicitIntent = new Intent();
        explicitIntent.setComponent(componentName);

        return explicitIntent;
    }
}
