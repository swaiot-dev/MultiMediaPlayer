
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MenuListView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MtkVideoView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.setting.TVStorage;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.mmp.model.SmbFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.DlnaFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.util.TextUtils;

public class MtkFilesListActivity extends MtkFilesBaseListActivity {

  private static final String TAG = "MtkFilesListActivity";

  private static final int MODE_FILES_LIST = 0;

  private static final int MODE_CONTENT_TYPES_LIST = 1;

  private ImageView vTopImage;

  private TextView vTopTV;

  private TextView vTopFilePath;

  private TextView vTopPageSize;

  private ImageView vThumbnail;

  private TextView vDetailInfo;

  private static MtkVideoView vVideoView;

  private TextView vTextShow;

  private LinearLayout vLinearLayout;

  private LinearLayout vMidLayout;

  private LinearLayout vTopLayout;

  private int mPlayPosition = -1;

  private final int mListItemHeght = 85;

  private int mMode = MODE_CONTENT_TYPES_LIST;

  public static boolean LOCALlogV = true;

  private AsyncLoader<Bitmap> mLoader;

  private AudioManager mAudioManager;

  private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener
    = new AudioManager.OnAudioFocusChangeListener() {

    @Override
    public void onAudioFocusChange(int focusChange) {}
  };

  private final AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {
      @Override
      public void sendAccessibilityEvent(View host, int eventType) {
          host.sendAccessibilityEventInternal(eventType);
          MtkLog.d(TAG, "sendAccessibilityEvent." + eventType + "," + host);
      }

    @Override
    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
          AccessibilityEvent event) {
          MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
          do {
              if (vList != host) {
                  MtkLog.d(TAG, ":" + vList + "," + host);
                  break;
              }

              List<CharSequence> texts = event.getText();
              if (texts == null) {
                  MtkLog.d(TAG, ":" + texts);
                  break;
              }

              //confirm which item is focus
              if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                  // test code
                  myHandler.removeMessages(TTS_FOCUS);
                  Message msg = new Message();
                  msg.what = TTS_FOCUS;
                  msg.obj = texts.get(0).toString();
                  MtkLog.d(TAG, "text name:" + texts.get(0).toString());
                  myHandler.sendMessageDelayed(msg, 100);
              }
          } while(false);

          return host.onRequestSendAccessibilityEventInternal(child, event);
      }
  };

  private int findSelectItem(String text) {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
          if(mContentTypeNames == null) {
              return -1;
          }
          for(int i = 0; i < mContentTypeNames.length; i++) {
              if(mContentTypeNames[i].equals(text)) {
                  return i;
              }
          }
      } else if (mMode == MODE_FILES_LIST) {
          if(mLoadFiles == null) {
              return -1;
          }

          for(int i = 0; i < mLoadFiles.size(); i++) {
              if(mLoadFiles.get(i).getName().equals(text)) {
                  return i + 1;
              } else if ("[..]".equals(text)) {
                  return 0;
              }
          }
      }

      return -1;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mViewMode = VIEW_MODE_LIST;
    super.onCreate(savedInstanceState);
    // mLoader = new AsyncLoader<Bitmap>(1);
    mLoader = AsyncLoader.getInstance(1);
    Util.LogLife(TAG, "onCreate");
    vList.setAccessibilityDelegate(mAccDelegate);
    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    // Toast if TalkbackService enabled
    if (Util.isTTSEnabled(getApplicationContext())) {
        Util.showToast(this, "TalkBackService is enabled. \nPlease use red or green key to exit and enter.");
    }
  }
  /**
   * {@inheritDoc}
   */
  @Override
  protected void initMusicView() {
    View contentView = LayoutInflater.from(this).inflate(
        R.layout.mmp_musicbackplay, null);
    //contentView.findViewById(R.id.mmp_music_playback_spectrum)
    //    .setVisibility(View.GONE);
    mPopView = new PopupWindow(contentView, vLinearLayout.getWidth(),
        vLinearLayout.getHeight());
    vMusicView = new MusicPlayInfoView(this, contentView, 1,
        mPopView);

  }

  /**
   * Play video completion listener
   */
  /*
   * private OnCompletionListener mCompletionListener = new OnCompletionListener() { public void
   * onCompletion(CHMtkMediaPlayer arg0) { // flag==1 play all files finish ,flag==0 play a file
   * finish //if (flag == 1) { vMusicView.removeMessage(); mPopView.dismiss(); //} //return false; }
   * };
   */

  /**
   * {@inheritDoc}
   */
  @Override
  protected void showMusicView() {
    super.showMusicView();

    Looper.myQueue().addIdleHandler(new IdleHandler() {
      @Override
      public boolean queueIdle() {

        MtkLog.i(TAG, "  vMidLayout.getLeft()  :"
            + vMidLayout.getLeft() + "  vLinearLayout.getTop():"
            + vLinearLayout.getTop());
        mPopView.setWidth(vLinearLayout.getWidth());
        mPopView.setHeight(vLinearLayout.getHeight());

        mPopView.showAtLocation(vMidLayout, Gravity.LEFT | Gravity.TOP,
            vMidLayout.getLeft(), vLinearLayout.getTop()
                + vTopLayout.getHeight() + 5);
        vMusicView.init(MtkFilesListActivity.this);
        return false;
      }
    });
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if (LogicManager.getInstance(this).isAudioOnly()) {
      /* by lei add for fix cr DTV00390970 */
      if (event.getAction() == KeyEvent.ACTION_UP) {
        LogicManager.getInstance(this).setAudioOnly(false);
      }
      return true;
    }
    return super.dispatchKeyEvent(event);
  }

  int position = 0;

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onResume() {
    super.onResume();
    position = setCurrentSelection();
    MtkLog.i(TAG, "onResume  position:" + position);
    if (vList != null && vList.isInTouchMode()) {
      new Thread() {
        @Override
        public void run() {
          try {
            Instrumentation inst = new Instrumentation();
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
          } catch (Exception e) {
            e.getStackTrace();
          }
        }
      }.start();
    }
    String currentPath = MultiFilesManager.getInstance(this).getCurrentPath();
    MtkLog.d(TAG, "onResume currentPath:" + currentPath);
    if (((currentPath != null && currentPath.equals("/"))
        || getListContentType() != FilesManager.CONTENT_VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      vLinearLayout.setVisibility(View.VISIBLE);
      showMusicView();
    } else {
      // modified by keke for fix DTV00380564
      LogicManager.getInstance(this).setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
      vLinearLayout.setVisibility(View.INVISIBLE);
      stopMusicView();

    }

    mPlayMode = 0;
    if (getListContentType() == FilesManager.CONTENT_VIDEO) {
      Log.i(TAG, "currentPlay :" + currentPlay);
      if (currentPlay == position) {
        LogicManager.getInstance(this).videoZoom(VideoConst.VOUT_ZOOM_TYPE_1X);
        myHandler.sendEmptyMessage(PLAY);
      } else {
        setListSelection(position);
      }
    }
    Util.LogLife(TAG, "onResume");
  }

  @Override
  protected void onStop() {
    super.onStop();
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    Util.LogLife(TAG, "onStop");
  }

  private final static int PLAY = 100;
  private final static int MUSIC_PLAYING = 101;
  private final static int TTS_FOCUS = 102;
  private final Handler myHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case PLAY:
          FileAdapter file = getListItem(position);
          if (null != file) {
            if (file.isFile()
                && (getListContentType() == FilesManager.CONTENT_VIDEO)) {
              if (vVideoView == null) {
                vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
                vVideoView.setVisibility(View.VISIBLE);
              }
              // vVideoView.init();

              if (file instanceof LocalFileAdapter || file instanceof SmbFileAdapter) {
                playSelectionItem(file.getAbsolutePath());
              } else {
                playSelectionItem(file.getName() + file.getSuffix());
              }

            }
          }
          break;
        case MUSIC_PLAYING:
          onShowMusicView();
          break;
        case TTS_FOCUS:
            int index = findSelectItem(msg.obj.toString());
            if (index >= 0) {
                setListSelection(index);
            }
          break;

      }
    }
  };

  // modified by keke for fix DTV00380564
  @Override
  protected void onPause() {
    if (getListContentType() == FilesManager.CONTENT_VIDEO) {
      String m = TVStorage.getInstance(this).get(
          "SCREENMODE_FILELIST");
      try {
        if (null != m && m.length() > 0) {
          int u = Integer.parseInt(m);
          if (u == 1) {
          } else {
            LogicManager.getInstance(this).setScreenMode(u);
          }
        }
      } catch (Exception e) {

      }
    }
    super.onPause();
    removeView();
    Util.LogLife(TAG, "onPause");
  }

  /*
   * if from list mode to grid mode, true, else false, for clear info and thumbnail array not in
   * onstop function, but in press bule key. avoid clear array item when add in grid mode Help me?
   */
  private boolean mIsGridMode = false;

  /* For close preview mode video add by lei */
  @Override
  protected void onDestroy() {
    if (vVideoView != null) {
      vVideoView.onRelease();
    }
    if (!mIsGridMode) {
      if (null != mInfoLoader) {
        mInfoLoader.clearQueue();
      }

      if (null != mLoader) {
        mLoader.clearQueue();
      }
    }
    mIsGridMode = false;

    mAudioManager.abandonAudioFocus(mAudioFocusListener);
    mAudioManager = null;
    vList.setAccessibilityDelegate(null);
    super.onDestroy();
    Util.LogLife(TAG, "onDestroy");
    vList.setAccessibilityDelegate(null);
  }

  IAudioPlayListener mListListener = new IAudioPlayListener() {

    @Override
    public void notify(int status) {
      // TODO Auto-generated method stub
      myHandler.sendEmptyMessage(MUSIC_PLAYING);
      LogicManager.getInstance(MtkFilesListActivity.this).registerAudioPlayListener(null);
    }

  };

  private void onShowMusicView() {
    // TODO Auto-generated method stub
    if ((getListContentType() != FilesManager.CONTENT_VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      if (mPopView != null && !mPopView.isShowing()) {
        if (GetCurrentTask.getInstance(this)
            .getCurRunningClassName()
            .equals("com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesListActivity")) {
          showMusicView();
        }
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    Util.LogLife(TAG, "onStart");
    LogicManager.getInstance(this).registerAudioPlayListener(mListListener);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onRestart() {
    super.onRestart();
    // int position = setCurrentSelection();
    // FileAdapter file = getListItem(position);
    // if (null != file) {
    // if (file.isFile()
    // && (getListContentType() == FilesManager.CONTENT_VIDEO)&&vVideoView!=null){
    // modified by keke for DTV00383229
    // vVideoView.init();
    // playSelectionItem(file.getAbsolutePath());
    // }
    // }
    Util.LogLife(TAG, "onRestart");
  }

  private void showNowPlayingAudio() {
    MtkLog.d(TAG, "ShowNowPlayingAudio!!");

    String audioName = null;
    try {
      audioName = LogicManager.getInstance(this).getCurrentFileName(
          Const.FILTER_AUDIO);
    } catch (IndexOutOfBoundsException e) {
    }

    if (audioName != null) {
      MtkLog.d(TAG, "Now Playing Audio Name : " + audioName);
      for (int i = 1, length = getListItemsCount(); i <= length; i++) {
        String fileName = getListItem(i).getName();
        if (audioName.equals(fileName)) {
          mPlayPosition = i;
          MtkLog.d(TAG, "Find Playing Audio : " + mPlayPosition);

          break;
        }
      }

      if (mPlayPosition >= 0) {
        setListSelection(mPlayPosition);
        mAdapter.notifyDataSetChanged();
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected int setupContentView() {
    return R.layout.mmp_files_list;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void bindData(Intent intent) {
    int mode = intent.getIntExtra("Mode", -1);
    if (mode > -1) {
      mMode = mode;
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      getIntentData(intent);
      setupHeader();
      refreshListView(null);
    } else {
      super.bindData(intent);
    }

  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected FilesAdapter getAdapter() {
    return new MtkFilesListAdapter(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void findViews() {
    vTopImage = (ImageView) findViewById(R.id.mmp_list_topimg);
    vTopTV = (TextView) findViewById(R.id.mmp_list_toptv);
    vTopFilePath = (TextView) findViewById(R.id.mmp_list_filepath);
    //vTopPageSize = (TextView) findViewById(R.id.mmp_list_pagesize);
    vThumbnail = (ImageView) findViewById(R.id.mmp_listmode_thumnail);
    vDetailInfo = (TextView) findViewById(R.id.mmp_list_detailinfo);

    vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
    vTextShow = (TextView) findViewById(R.id.mmp_listmode_textshow);

    vLinearLayout = (LinearLayout) findViewById(R.id.mmp_listmode_musicicon);
    vMidLayout = (LinearLayout) findViewById(R.id.mmp_listmode_rl);
    vTopLayout = (LinearLayout) findViewById(R.id.multimedia_top);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void setupHeader() {
    vThumbnail.setVisibility(View.GONE);
    if (vVideoView != null) {
      vVideoView.setVisibility(View.GONE);
    }
    vTextShow.setVisibility(View.GONE);

    int contentType = getListContentType();
    if (contentType >= 0) {
      if (contentType == FilesManager.CONTENT_PHOTO
          || contentType == FilesManager.CONTENT_AUDIO) {
        vThumbnail.setVisibility(View.VISIBLE);
      } else if (contentType == FilesManager.CONTENT_VIDEO) {

        if (vVideoView != null) {
          if (mMode == MODE_FILES_LIST) {
            vVideoView.setVisibility(View.VISIBLE);
          } else {
            vVideoView.setVisibility(View.GONE);
          }
        }
      } else if (contentType == FilesManager.CONTENT_TEXT) {
        vTextShow.setVisibility(View.VISIBLE);
      } else if (contentType == FilesManager.CONTENT_THRDPHOTO) {
        // DMR,THRDPHOT BOTH==4
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          contentType = FilesManager.CONTENT_THRDPHOTO;
        } else {
          vThumbnail.setVisibility(View.VISIBLE);
          contentType = FilesManager.CONTENT_PHOTO;
        }
      }
      if (contentType < mContentTypeIcons.length && contentType < mContentTypeNames.length) {
        vTopImage.setImageDrawable(mContentTypeIcons[contentType]);
        vTopTV.setText(mContentTypeNames[contentType]);
        vTopTV.setSelected(true);
      }
    }

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      vTopFilePath.setText("");
    } else if (mMode == MODE_FILES_LIST) {
      String curPath = getListCurrentPath();
//      if (curPath != null && curPath.startsWith("/storage")) {
//        MultiFilesManager multiFileManager = MultiFilesManager
//            .getInstance(this);
//        List<FileAdapter> deviceList = multiFileManager.getLocalDviceAdapter();
//        if (deviceList != null && deviceList.size() > 0) {
//          for (int i = 0; i < deviceList.size(); i++) {
//            if (curPath.contains(deviceList.get(i).getPath())) {
//              curPath = curPath.substring(deviceList.get(i).getPath().length());
//              curPath = "/storage/" + deviceList.get(i).getName() + curPath;
//              break;
//            }
//          }
//        }
//      }
      vTopFilePath.setText(curPath);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void customMenu(MenuListView menu) {
    menu.removeItem(2);
    // Added by Dan for fix bug DTV00379191
    menu.setFirstIndex(-1);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      if ((Util.isTTSEnabled(getApplicationContext()) &&
              KeyMap.KEYCODE_MTKIR_BLUE == keyCode)
              || (!Util.isTTSEnabled(getApplicationContext()) &&
                      KeyMap.KEYCODE_MTKIR_YELLOW == keyCode)) {
        MtkLog.i(TAG, "onKeyDown keycode: " + keyCode);
        return super.onKeyDown(keyCode, event);
      }

    switch (keyCode) {
      case KeyMap.KEYCODE_MENU:
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          return true;
        }
        break;
      case KeyMap.KEYCODE_MTKIR_RED:
      case KeyMap.KEYCODE_DPAD_LEFT:
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          removeView();
          mFilesManager.deleteObserver(this);
          mFilesManager.destroy();
          ((MmpApp) getApplication()).setVolumeUpdate(0);
          MmpApp destroyApp = (MmpApp) getApplicationContext();
          destroyApp.finishAll();
        } else if (mMode == MODE_FILES_LIST) {
          onListItemClick(null, null, 0, 0);
        }
        return true;
      case KeyMap.KEYCODE_MTKIR_GREEN:
      case KeyMap.KEYCODE_DPAD_RIGHT:
        onListItemClick(null, null, getListSelectedItemPosition() + 1, 0);
        return true;
      case KeyMap.KEYCODE_VOLUME_UP: {
        if (vVideoView != null
            && vVideoView.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_STARTED) {
          LogicManager.getInstance(this).setVolumeUp();
          return true;
        } else {
          break;
        }

      }
      case KeyMap.KEYCODE_VOLUME_DOWN: {
        if (vVideoView != null
            && vVideoView.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_STARTED) {
          LogicManager.getInstance(this).setVolumeDown();
          return true;
        } else {
          break;
        }
      }
      case KeyMap.KEYCODE_MTKIR_MUTE: {
        if (vVideoView != null
            && vVideoView.getVideoPlayStatus() >= VideoConst.PLAY_STATUS_STARTED) {
          LogicManager.getInstance(this).setMute();
          return true;
        } else {
          break;
        }
      }
      /*case KeyMap.KEYCODE_MTKIR_YELLOW:
      case KeyMap.KEYCODE_MTKIR_BLUE:
        if (!MediaMainActivity.isValid(400)) {
          break;
        }
        Intent intent = new Intent();
        if (mMode == MODE_CONTENT_TYPES_LIST) {
          // mFilesManager.destroy();
          // mFilesManager.destroyManager();
          // mFilesManager.deleteObservers();
          stopMusicView();
          // intent.setClass(this, MediaMainActivity.class);
          // MediaMainActivity.mSelection=getListSelectedItemPosition() + 1;
          // intent.putExtra("selection", getListSelectedItemPosition() + 1);
          // intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          finish();
        if (MultiFilesManager.hasInstance()) {
          MultiFilesManager.getInstance(this).destroy();
        }
          return true;
        } else if (mMode == MODE_FILES_LIST) {
          removeView();
          mLoader.clearQueue();
          mIsGridMode = true;
          intent.setClass(this, MtkFilesGridActivity.class);
          intent.putExtra(INTENT_NAME_PATH, getListCurrentPath());
          intent.putExtra(INTENT_NAME_SELECTION, getListSelectedItemPosition());
          intent.putStringArrayListExtra(INTENT_NAME_COPYED_FILES, mCopyedFiles);
          intent.putStringArrayListExtra(INTENT_NAME_SELECTED_FILES, mSelectedFiles);
          // add for ThumbnailSize bug
          intent.putExtra("mThumbnailSize", mThumbnailSize);

          // MediaMainActivity.mSelection=getListSelectedItemPosition();
        }
        startActivity(intent);
        finish();
        break;*/
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        // removeView();
        // Util.exitMmpActivity(getApplicationContext());
        break;
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  /**
   * {@inheritDoc}
   */
  private int currentPlay = -1;

  @Override
  protected void onListItemClick(AbsListView l, View v, int position, long id) {
    MtkLog.i(TAG, "onListItemClick position:" + position);

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      if (4 == position) {
        exitPIP();
        LogicManager.getInstance(this).finishVideo();
        if (VideoPlayActivity.getInstance() != null) {
          VideoPlayActivity.getInstance().finish();
        }
        LogicManager.getInstance(this).stopAudio();
        Intent intent = new Intent(MmpConst.INTENT_DMR);
        intent.putExtra("TKUI", true);
        intent.putExtra("FILE_LIST", true);
        this.startActivity(intent);
      } else {

        SaveValue pref = SaveValue.getInstance(this);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false
            : true;
        MtkLog.d(TAG, "Samba Available : " + smbAvailable);
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false
            : true;
        MtkLog.d(TAG, "Dlna Available : " + dlnaAvailable);

        MultiFilesManager.getInstance(this).getLocalDevices();
        int deviceNum = MultiFilesManager.getInstance(this).getAllDevicesNum();
        if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable && !smbAvailable) {
          return;
        }

        mMode = MODE_FILES_LIST;

        mFilesManager.pushOpenedHistory(position);
        openDir(getListRootPath());

        if (getListContentType() == FilesManager.CONTENT_VIDEO) {
          LogicManager.getInstance(this).stopAudio();
          stopMusicView();
        }
      }
      return;

    }

    if (position == 0) {
      if (super.mMode == MODE_NORMAL) {
        MultiFilesManager multiFileManager = MultiFilesManager
            .getInstance(this);
        String path = multiFileManager.getFirstDeviceMountPointPath();
        if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
          if (getListCurrentPath().equals(path)) {
            onReachRoot(getListContentType());
            return;
          }
        }
        int cur = mFilesManager.popOpenedHistory();
        if (null != getListParentPath()) {
          cur = cur + 1;
        }
        openDir(getListParentPath(), cur);
      } else if (super.mMode == MODE_RECURSIVE) {
        MultiFilesManager multiFileManager = MultiFilesManager
            .getInstance(this);
        if (MultiFilesManager.ROOT_PATH.equals(getListCurrentPath())) {
          onReachRoot(getListContentType());

        } else {
          if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
            onReachRoot(getListContentType());
          } else {
            openDir(MultiFilesManager.ROOT_PATH, 0);
          }
        }

      }
    } else {
      if (getListContentType() == FilesManager.CONTENT_VIDEO) {
        removeView();
      }
      // super.onListItemClick(l, v, position, id);

      FileAdapter file = getListItem(position);
      mAdapter.cancel();
      if (null != file && file.isDirectory()) {
        // open directory
        mFilesManager.pushOpenedHistory(position - 1);
        currentPosition = position;
        isFirstRequest = true;
        openDir(file.getAbsolutePath(), 0);

      } else if (null != file && file.isIsoFile()) {
        // TODO ISO mount
      } else if (null != file) {
        currentPlay = position;
        // play multi media file
        // int pos = 0;
        /*
         * pos = getListSelectedItemPosition(); if (pos < 0) { return; }
         */
        int pos = position;
        if (this instanceof MtkFilesListActivity) {
          pos -= 1;
        }
        if (pos < 0) {
          return;
        }// support mouse click by lei modif.
        ((MultiFilesManager) mFilesManager).getPlayList(pos);
        // int contentType = getListContentType();
        // if (contentType == FilesManager.CONTENT_PHOTO ||
        // contentType == FilesManager.CONTENT_THRDPHOTO) {
        // file.stopDecode();

        // file.stopThumbnail();
        // }
        playFile(file.getAbsolutePath());
      }
    }
  }

  @Override
  protected void playFile(String path) {
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    super.playFile(path);
  }

  /**
   * Count page size
   */
  protected void computePageSize() {
    if (mPageSize == 0) {
      MtkLog.d(TAG, "H : " + vList.getHeight());
      MtkLog.d(TAG, "W : " + vList.getWidth());

      int h = vList.getHeight();
      int size = mListItemHeght;
      MtkLog.d(TAG, "Size : " + size);

      int row = h / size;
      mPageSize = row;

      MtkLog.d(TAG, "PageSize : " + mPageSize);
    }

    if (mPageSize > 0) {
      int filesCount = getListItemsCount();
      mPageCount = filesCount / mPageSize;
      if (filesCount % mPageSize > 0 || filesCount == 0) {
        mPageCount += 1;
      }

      MtkLog.d(TAG, "ItemCount : " + filesCount);
      MtkLog.d(TAG, "PageCount : " + mPageCount);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onReachRoot(int selection) {
    // super.onReachRoot(selection);

    mMode = MODE_CONTENT_TYPES_LIST;

    Intent intent = new Intent(this, this.getClass());
    intent.putExtra(INTENT_NAME_PATH, getListParentPath());
    intent.putExtra(INTENT_NAME_SELECTION, selection);
    startActivity(intent);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
    MtkLog.i(TAG, "onListItemSelected position:" + position + "  mMode:" + mMode
        + "  pip:" + Util.mIsEnterPip);
    currentPlay = -1;
    if (myVideoPlayDelay != null && vVideoView != null) {
      vVideoView.removeCallbacks(myVideoPlayDelay);
    }
    if (mInfoLoader != null && mInfoLoader.getTaskSize() > 0) {
      mInfoLoader.clearQueue();
    }
    if (mLoader != null && mLoader.getTaskSize() > 0) {
      mLoader.clearQueue();
    }
/*
    if (mPageCount == 0) {
      vTopPageSize.setText("");
    } else {
      int fileCount = getListItemsCount();
      int listCount = getListView().getCount();

      MtkLog.i(TAG, "mMode:" + mMode);
      if (mMode != MODE_CONTENT_TYPES_LIST) {
        if (position == 0) {
          vTopPageSize.setText("");
        } else {
          vTopPageSize.setText(getListSelectedItemPosition() + 1 + "/"
              + fileCount);
        }
      } else if (mMode == MODE_CONTENT_TYPES_LIST) {
        vTopPageSize.setText((getListSelectedItemPosition() + 2) + "/"
            + listCount);
      }

    }*/

    if (mMode == MODE_CONTENT_TYPES_LIST) {
      setListContentType(position);
      setupHeader();
      vDetailInfo.setText("");
      if (vVideoView != null && !Util.mIsEnterPip) {
        vVideoView.reset();
      }
    } else {
      if (position == 0) {
        //vTopPageSize.setText("");
        vThumbnail.setImageBitmap(null);
        vDetailInfo.setText("");
        vTextShow.setText("");
        if (vVideoView != null && !Util.mIsEnterPip) {
          vVideoView.reset();
        }
      } else if (position > 0) {
        FileAdapter file = getListItem(position);
        int contentType = getListContentType();
        // ((MultiFilesManager) mFilesManager).getPlayList(position-1);
        if (file != null
            && (file.isDirectory() || file.isIsoFile())) {
          if (contentType == FilesManager.CONTENT_TEXT) {
            vTextShow.setText("");
          } else if (contentType ==
              FilesManager.CONTENT_VIDEO && vVideoView != null) {
            if (!Util.mIsEnterPip) {
              vVideoView.reset();
            }
          } else {
            vThumbnail.setImageBitmap(null);
          }
          vDetailInfo.setText("");
        } else {
          if (contentType != FilesManager.CONTENT_TEXT
              && contentType != FilesManager.CONTENT_AUDIO) {
            exitPIP();
            LogicManager.getInstance(getApplicationContext()).finishVideo();
            if (VideoPlayActivity.getInstance() != null) {
              VideoPlayActivity.getInstance().finish();
            }
            if (Util.mIsEnterPip) {
              Util.mIsEnterPip = false;
              if (vVideoView != null) {
                vVideoView.reset();
              }
            }
          }

          if (mFilesManager.getContentType() == MultiMediaConstant.VIDEO) {
              AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
              mAudioManager.requestAudioFocus(mAudioFocusListener,
                  AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
          }

          if (contentType == FilesManager.CONTENT_TEXT) {
            vTextShow.setText(file.getPreviewBuf());
          } else if (contentType == FilesManager.CONTENT_VIDEO) {
            // TODO paly video
            currentPlay = position;
            MultiFilesManager filesManager = MultiFilesManager
                .getInstance(this);
            int source = filesManager.getCurrentSourceType();
            myVideoPlayDelay = new VideoPlayDelay(file, source);

            if (vVideoView == null) {
              MtkLog.d(TAG, "onListItemSelected~~vVideoView == null~");

              vVideoView = (MtkVideoView) findViewById(R.id.mmp_video_videoview);
              vVideoView.setVisibility(View.VISIBLE);
            }
            if (vVideoView != null) {
              vVideoView.postDelayed(myVideoPlayDelay, 1000);
            }
          } else {
            vThumbnail.setImageBitmap(null);
            // mLoader.clearQueue();
            if (mLoader != null) {
              mLoader.addWork(new LoadCorverPic(file));
            }
          }

          int type = mFilesManager.getContentType();
          String info = "";
          if (file != null && (type == MultiMediaConstant.AUDIO || type == MultiMediaConstant.VIDEO
              || type == MultiMediaConstant.PHOTO || type == MultiMediaConstant.THRD_PHOTO)) {
            info = mInforCache.get(file.getAbsolutePath());
            if (info != null) {
              vDetailInfo.setText(info);
              return;
            } else {
              vDetailInfo.setText("");
            }
            String suffix = file.getSuffix();
            if (!FileSuffixConst.DLNA_FILE_NAME_EXT_PCM
                .equalsIgnoreCase(suffix)) {
              // mInfoLoader.clearQueue();
              if (mInfoLoader != null) {
                mInfoLoader.addSelectedInfoWork(new LoadInfo(file, vDetailInfo));
              }
            } else
            {
              vDetailInfo.setText(file.getName() + file.getSuffix());
            }
          } else {
            info = file.getInfo();
            vDetailInfo.setText(info);
            return;
          }
        }
      }
    }

  }

  private class LoadCorverPic implements LoadWork<Bitmap> {

    private final FileAdapter mFile;

    public LoadCorverPic(FileAdapter file) {
      mFile = file;
    }

    @Override
    public Bitmap load() {

      Bitmap thumbnail = null;
      try {
        //Original
        thumbnail = mFile.getThumbnail(vThumbnail.getWidth(),
            vThumbnail.getHeight(), false);
        //EXO DLNA MARK
        //thumbnail = null;
      } catch (OutOfMemoryError e) {
        thumbnail = ((BitmapDrawable) mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_big))
            .getBitmap();
      }

      return thumbnail;
    }

    @Override
    public void loaded(final Bitmap result) {
      if (null != result) {
        vThumbnail.post(new Runnable() {

          @Override
          public void run() {
            vThumbnail.setImageBitmap(result);
          }
        });
      }

    }

  }

  private static VideoPlayDelay myVideoPlayDelay;

  private class VideoPlayDelay implements Runnable {
    private final FileAdapter file;
    private final int sourceType;

    public VideoPlayDelay(FileAdapter file, int sourceType) {
      this.file = file;
      this.sourceType = sourceType;
    }

    @Override
    public void run() {
      if (file instanceof LocalFileAdapter) {
        playSelectionItem(file.getAbsolutePath());
      } else if (sourceType == MultiFilesManager.SOURCE_SMB) {
        playSelectionItem(file.getAbsolutePath());
      } else {
        playSelectionItem(file.getName() + file.getSuffix());
      }

      myVideoPlayDelay = null;
    }
  }

  private void playSelectionItem(String path) {
    if (isValid() && vVideoView != null) {
      MtkLog.d(TAG, "playSelectionItem path:" + path + "  vVideoView.isVideoPlaybackInit():"
          + vVideoView.isVideoPlaybackInit());
      if (!vVideoView.isVideoPlaybackInit()) {
        vVideoView.init();
      }
      vVideoView.reset();
      vVideoView.setPreviewMode(true);
      vVideoView.play(path);
      // Canvas canvas = vVideoView.getHolder().lockCanvas(null);
      // vVideoView.getHolder().unlockCanvasAndPost(canvas);
    }
  }

  @Override
  public FileAdapter getListItem(int position) {
    return super.getListItem(position - 1);
  }

  @Override
  public FileAdapter getListSelectedItem() {
    return getListItem(super.getListSelectedItemPosition());
  }

  @Override
  public int getListSelectedItemPosition() {
    return super.getListSelectedItemPosition() - 1;
  }

  @Override
  protected void refreshListView(List<FileAdapter> files) {
    super.refreshListView(files);

    if (mMode == MODE_FILES_LIST) {
      //vTopPageSize.setText(mCurrentPage + "/" + mPageCount);
    }
  }

  @Override
  protected void refreshListView() {
    super.refreshListView();
    if (mFilesManager.isRefresh()) {
      setListSelection(mFilesManager.getPositionInParent());
      MtkLog.i(TAG, "is same path, need set position");
    } else {
      MtkLog.i(TAG, "is not same path, no need set position");
    }
  }

  private class MtkFilesListAdapter extends FilesAdapter {
    private Drawable mFolder;
    private Drawable mVideoDefault;
    private Drawable mAudioDefault;
    private Drawable mPhotoDefault;
    private Drawable mTextDefault;
    private Drawable mFailed;
    private Drawable mPlaying;
    private final String[] mContentTypes;
    private final Drawable[] mListMenueDrawables = {mResources.getDrawable(R.drawable.mmp_list_menu_video),
            mResources.getDrawable(R.drawable.mmp_list_menu_photo),
            mResources.getDrawable(R.drawable.mmp_list_menu_audio),
            mResources.getDrawable(R.drawable.mmp_list_menu_text),
            mResources.getDrawable(R.drawable.mmp_list_menu_dmr)};
    private final BitmapCache mCache;
    private final AsyncLoader<Bitmap> mLoader;
    private final ConcurrentHashMap<View, LoadBitmap> mWorks;

    private final ConcurrentHashMap<View, Runnable> mRunnables;
    private final Handler mBindHandler;

    public MtkFilesListAdapter(Context context) {
      super(context);
      mCache = BitmapCache.createCache(false);
      mLoader = AsyncLoader.getInstance(1);
      mWorks = new ConcurrentHashMap<View, LoadBitmap>();
      mBindHandler = new Handler();
      mRunnables = new ConcurrentHashMap<View, Runnable>();
      prepareDefaultThumbnails();

      mContentTypes = mContentTypeNames;
    }

    private void prepareDefaultThumbnails() {
      Resources resources = getResources();

      mFolder = resources
          .getDrawable(R.drawable.mmp_listmod_icon_folder_default);
      mPlaying = resources
          .getDrawable(R.drawable.mmp_toolbar_typeicon_paly);
      mVideoDefault = mResources.getDrawable(R.drawable.mmp_listmod_icon_video_default);
      mAudioDefault = mResources.getDrawable(R.drawable.mmp_listmod_icon_music_default);
      mTextDefault = mResources.getDrawable(R.drawable.mmp_listmod_icon_text_default);
      mPhotoDefault = mResources.getDrawable(R.drawable.mmp_listmod_icon_photo_default);
      mFailed = mResources.getDrawable(R.drawable.mmp_thumbnail_loading_failed_mid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return mContentTypes.length;
      }

      return super.getCount() + 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileAdapter getItem(int position) {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return null;
      }

      if (position == 0) {
        return null;
      }

      return super.getItem(position - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getItemId(int position) {
      if (mMode == MODE_CONTENT_TYPES_LIST) {
        return position;
      }

      if (position == 0) {
        return 0;
      }

      return super.getItemId(position - 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int getItemLayout() {
      return R.layout.mmp_listmode_item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initView(View v, FileAdapter data, boolean flag) {
      if (!flag)
      {
        ViewHolder holder = new ViewHolder();
        holder.folderIcon = (ImageView) v
                .findViewById(R.id.mmp_listmode_icon);
        holder.folderName = (TextView) v
                .findViewById(R.id.mmp_listmode_foldername);
        holder.layout = (FrameLayout)v
                .findViewById(R.id.mmp_list_layout) ;
        //holder.layout.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT,mListItemHeght));
        v.setTag(holder);
        MtkLog.d("jgui","flat = false and height="+mListItemHeght);
      }else
      {
        //ListView.LayoutParams lp = (ListView.LayoutParams)v.getLayoutParams();
        //lp.width = LayoutParams.MATCH_PARENT;
        //lp.height = mListItemHeght;
        //v.setLayoutParams(lp);
        MtkLog.d("jgui","flat = true and height="+mListItemHeght);
      }
      /*ViewHolder holder = new ViewHolder();

      holder.folderIcon = (ImageView) v
          .findViewById(R.id.mmp_listmode_icon);
      holder.folderName = (TextView) v
          .findViewById(R.id.mmp_listmode_foldername);
      holder.layout = (FrameLayout)v
              .findViewById(R.id.mmp_largeview) ;
      //holder.copyIcon = (ImageView) v
       //   .findViewById(R.id.mmp_listmode_copyicon);

      v.setLayoutParams(new ListView.LayoutParams(
          LayoutParams.MATCH_PARENT, mListItemHeght));
      v.setTag(holder);*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void bindView(View v, FileAdapter data, int position) {
      ViewHolder holder = (ViewHolder) v.getTag();
      v.setBackgroundDrawable(null);

      if (mMode == MODE_CONTENT_TYPES_LIST) {
        holder.folderIcon.setVisibility(View.VISIBLE);
        holder.folderIcon.setImageDrawable(mListMenueDrawables[position]);
        holder.folderName.setText(mContentTypes[position]);
        //holder.copyIcon.setImageDrawable(null);
        return;
      }

      if (position == 0) {
        holder.folderIcon.setVisibility(View.VISIBLE);
        holder.folderIcon.setImageDrawable(mFolder);
        holder.folderName.setText("[..]");
        //holder.copyIcon.setImageDrawable(null);
      } else {
        if (data == null) {

          holder.folderIcon.setImageDrawable(null);
          holder.folderName.setText("");
          //holder.copyIcon.setImageDrawable(null);
          return;
        }

        if (data.isDirectory()) {
          holder.folderIcon.setVisibility(View.VISIBLE);
          holder.folderIcon.setImageDrawable(mFolder);
          holder.folderName.setText(data.getName());
        }else
        {
          int type = mFilesManager.getContentType();
          String path = data.getAbsolutePath();
          if (type == MultiMediaConstant.TEXT) {
            holder.folderIcon.setImageDrawable(mTextDefault);
          } else if (type == MultiMediaConstant.AUDIO) {
            holder.folderIcon.setImageDrawable(mAudioDefault);
          } else if (type == MultiMediaConstant.PHOTO) {
            holder.folderIcon.setImageDrawable(mPhotoDefault);
          } else if (type == MultiMediaConstant.VIDEO) {
            holder.folderIcon.setImageDrawable(mVideoDefault);
          } else if (type == MultiMediaConstant.THRD_PHOTO) {
            holder.folderIcon.setImageDrawable(mPhotoDefault);
          }
          bindThumbnail(data,holder.folderIcon,path);
        }


        /*if (data.isDirectory()) {
          holder.folderIcon.setVisibility(View.VISIBLE);
          holder.folderIcon.setImageDrawable(mFolder);
          holder.folderName.setText(data.getName());
          //holder.copyIcon.setImageDrawable(null);
        } else {
          if (position == mPlayPosition) {
            holder.folderIcon.setVisibility(View.VISIBLE);
            holder.folderIcon.setImageDrawable(mPlaying);
          } else {
            holder.folderIcon.setVisibility(View.INVISIBLE);
          }*/

          /*String path = data.getAbsolutePath();
          if (mCopyedFiles.size() > 0 && mCopyedFiles.contains(path)) {
            holder.copyIcon
                .setImageResource(R.drawable.mmp_listmode_icon_copy);
          } else if (mSelectedFiles.size() > 0 && mSelectedFiles.contains(path)) {
            holder.copyIcon
                .setImageResource(R.drawable.mmp_listmode_icon_select);
          } else {
            holder.copyIcon.setImageDrawable(null);
          }*/
          holder.folderName
              .setText(data.getName() + data.getSuffix());


      }
    }

    private void bindThumbnail(FileAdapter data, ImageView view, String path) {

      // if(!Util.isGridActivity(getApplicationContext())){
      // return;
      // }
      Bitmap image = mCache.get(path);
      if (image != null) {
        view.setImageBitmap(image);
      } else {
        MtkLog.i(TAG, "bindThumbnail LoadBitmap!!" + mWorks.get(view) + "  " + path);
        if (mWorks.get(view) == null) {
          LoadBitmap work = new LoadBitmap(data, view);
          mWorks.put(view, work);
          mLoader.addWork(work);
        }
      }
    }
    private class LoadBitmap implements LoadWork<Bitmap> {
      private final FileAdapter mData;
      private final ImageView vImage;
      private Bitmap mResult;
      private boolean mNeedCache = true;

      public LoadBitmap(FileAdapter data, ImageView iamge) {
        mData = data;
        vImage = iamge;
      }

      public FileAdapter getData() {
        return mData;
      }

      @Override
      public Bitmap load() {
        Bitmap bitmap = null;
        try {
          MtkLog.i(TAG, "mThumbnailSize:" + mThumbnailSize);
          int width = 160;
          int height =110;
          /*if (mThumbnailSize == MultiMediaConstant.MEDIUM) {
            width = mItemWidth*2;
          } else if (mThumbnailSize == MultiMediaConstant.LARGE) {
            width = mItemWidth*2;
          }*/
          //Original
          bitmap = mData.getThumbnail(width, height, true);
          //EXO DLNA MARK
          //bitmap = null;
          if (bitmap == null) {
            if (getListContentType() == MultiMediaConstant.PHOTO) {
              bitmap = ((BitmapDrawable) mFailed).getBitmap();
            } else if (getListContentType() == MultiMediaConstant.THRD_PHOTO) {
              bitmap = ((BitmapDrawable) mFailed).getBitmap();
            }
          }
          /*
           * if (getListContentType() == MultiMediaConstant.PHOTO && bitmap == null) { bitmap =
           * mData.getThumbnail(mThumbnailSize MultiMediaConstant.ZOOM, mThumbnailSize
           * MultiMediaConstant.ZOOM); if (getListContentType() == MultiMediaConstant.PHOTO &&
           * bitmap == null) { bitmap = ((BitmapDrawable) mFailed).getBitmap(); } }
           */
        } catch (OutOfMemoryError e) {
          MtkLog.e(TAG, "Get Image Thumbnail!!", e);
          bitmap = ((BitmapDrawable) mFailed).getBitmap();
          mNeedCache = false;
        }

        mResult = bitmap;
        MtkLog.d(TAG, "Decode Bitmap : " + mResult);
        return bitmap;
      }

      @Override
      public void loaded(Bitmap result) {
        /* by lei add for optimization */
        String cacheInfo = mData.getCacheInfo();
        if (cacheInfo != null) {
          mInforCache.put(mData.getAbsolutePath(), mData.getCacheInfo());
        }
        if (result == null) {
          int cntType = getListContentType();
          switch (cntType) {
            case MultiMediaConstant.AUDIO:
              mCache.put(mData.getAbsolutePath(),
                      ((BitmapDrawable) mAudioDefault).getBitmap());
              break;
            case MultiMediaConstant.VIDEO:
              mCache.put(mData.getAbsolutePath(),
                      ((BitmapDrawable) mVideoDefault).getBitmap());
              break;
            /*
             * case MultiMediaConstant.PHOTO: mCache.put(mData.getAbsolutePath(), ((BitmapDrawable)
             * mFailed).getBitmap()); break;
             */
            default:
              break;
          }

        } else if (result != null && mNeedCache) {
          mCache.put(mData.getAbsolutePath(), result);
          mWorks.remove(vImage);
        }

        Runnable r = new BindImage();
        mRunnables.put(vImage, r);
        mBindHandler.post(r);
        // loaded a bitmap sleep 100 ms
        // try {
        // Thread.sleep(10);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

      }

      private class BindImage implements Runnable {
        @Override
        public void run() {
          if (mResult != null) {
            // TODO null image
            if (null != vImage.getDrawable())
              vImage.setImageBitmap(mResult);
          }
        }
      }
    }

    private class ViewHolder {
      ImageView folderIcon;
      TextView folderName;
      ImageView copyIcon;
      FrameLayout layout;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListEmpty() {

    MultiFilesManager multiFileManager = MultiFilesManager
        .getInstance(this);
    if (multiFileManager.getAllDevicesNum() == MultiFilesManager.NO_DEVICES) {
      super.onListEmpty();
      vList.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onListNotEmpty() {
    super.onListNotEmpty();
    vList.setVisibility(View.VISIBLE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onBackPressed() {
    Log.i(TAG, "onBackPressed");
    removeView();
    mFilesManager.deleteObserver(this);
    mFilesManager.destroy();
    // MmpApp destroyApp = (MmpApp) getApplicationContext();
    // destroyApp.finishAll();
    ((MmpApp) getApplication()).setVolumeUpdate(0);
    Util.exitMmpActivity(getApplicationContext());
  }

  @Override
  protected void removeVideoView() {
    removeView();
  }

  protected static void removeView() {
    if (vVideoView != null) {
      vVideoView.removeCallbacks(myVideoPlayDelay);
      vVideoView.setVisibility(View.GONE);
      vVideoView.onRelease();
      vVideoView = null;
    }
  }

  @Override
  protected void stopMusicView() {
    if (vMusicView != null) {
      vMusicView.removeMessage();
    }
    super.stopMusicView();
  }

  @Override
  public void openDir(String path, int selection) {
    if (mMode == MODE_CONTENT_TYPES_LIST && mFlag) {
      if ((getListContentType() != FilesManager.CONTENT_VIDEO)
          && LogicManager.getInstance(this).isAudioStarted()) {
        vLinearLayout.setVisibility(View.VISIBLE);
        showMusicView();
      } else {
        LogicManager.getInstance(this).setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
        vLinearLayout.setVisibility(View.INVISIBLE);
        stopMusicView();
      }
      return;
    }
    super.openDir(path, selection);
  }

  @Override
  protected void stopVideoListMode() {
    if (null != vVideoView && !Util.mIsEnterPip) {
      vVideoView.reset();
    }
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    removeView();
    LogicManager.getInstance(this).stopAudio();
    stopMusicView();
  }
}
