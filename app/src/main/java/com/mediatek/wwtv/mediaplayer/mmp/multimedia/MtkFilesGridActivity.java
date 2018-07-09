
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.mmp.MtkMediaPlayer.OnCompletionListener;
import com.mediatek.wwtv.mediaplayer.mmp.USBBroadcastReceiver;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileCategoryManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmpcm.audio.IAudioPlayListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.nav.util.SundryImplement;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileSuffixConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MediaGridView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader.LoadWork;
import com.mediatek.wwtv.mediaplayer.mmp.util.BitmapCache;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.tvchannel.TVBlock;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.wwtv.mediaplayer.util.GetCurrentTask;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import com.mediatek.wwtv.mediaplayer.util.TextToSpeechUtil;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.util.KeyMap;
import android.text.TextUtils;

import com.mediatek.twoworlds.tv.MtkTvAppTV;

public class MtkFilesGridActivity extends MtkFilesBaseListActivity {

  private static final String TAG = "MtkFilesGridActivity";

    private static final int[] FILE_CATEGORY = new int[]{
            MultiMediaConstant.VIDEO,
            MultiMediaConstant.PHOTO,
            MultiMediaConstant.AUDIO
    };

    private ImageView vLeftTopImg;

  private TextView vLeftTopTv;

  private TextView vLeftMidTv;

  private TextView vTopPath;

  private TextView vTopRight;

  private final int mGridViewH = (ScreenConstant.SCREEN_HEIGHT * 11 / 13);

  private final int mGridViewW = (int) (ScreenConstant.SCREEN_WIDTH * 0.75);

  //private final int mVerticalSpacing = ScreenConstant.SCREEN_HEIGHT / 36;
  private final int mVerticalSpacing = 18;
  //private final int mHorizontalSpacing = ScreenConstant.SCREEN_WIDTH / 64;
  private final int mHorizontalSpacing = 8;

  private int mItemWidth = 259;

  private int mItemHeight = 259;

  private boolean isBack =false;
  /*
   * if turn list mode from grid mode, true, else false, for clear info and thumbnail array not in
   * onstop function, but in press bule key. avoid clear array item when add in list mode Help me?
   */
  private boolean mIsListMode = false;

  /* if turn page, true, and will add Blank item, else false. */
  private boolean mTurnPage = false;

  public static final int SELECTED = 101;
  public static final int SHOW_MUSIC = 102;
  public static final int SET_BTN_FOCUSABLE = 103;
  public static final int CHANGE_BACKGROUND = 104;
  private static final int MSG_RESET_SEEKIING = 1101;

  //SKY luojie 20180108 added for UI begin
  private View mContentView;
    //    private RadioButton mBtnMovie;
    //    private RadioButton mBtnPicture;
    //    private RadioButton mBtnMusic;
    private RecyclerView mFileCategory_rv;
    private LinearLayoutManager mCategoriesLayoutManager;
    private FileCategoryAdapter mFileCategoryAdapter;

    //    private BtnOnFocusChangeListener mBtnFocusChangeListener;
  private TransitionDrawable mBackgroundTransitionDrawable;
  //SKY luojie 20180108 added for UI end

  //SKY luojie 20180211 add for remove the MediaMainActivity begin
  private USBBroadcastReceiver mUSBBroadcastReceiver;
  private ShutDownBroadcastReceiver mShutDownBroadcastReceiver;
  private HandlerThread mHandlerThead;
  private LogicManager mLogicManager;
  private TextToSpeechUtil mTTSUtil;
  private boolean mIsSeeking;
  private int mSeekingProgress;
  //SKY luojie 20180211 add for remove the MediaMainActivity end

  private final Handler mGridHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case SELECTED:
          setSelect();
          break;
        case SHOW_MUSIC:
          onShowMusicView();
          break;
        case SET_BTN_FOCUSABLE:
                    //                    setCategoryBtnFocusable(true);
          break;
        case CHANGE_BACKGROUND:
          mGridHandler.removeMessages(CHANGE_BACKGROUND);
          changeBackground();
          break;
        case MSG_RESET_SEEKIING:
          mIsSeeking = false;
          break;
      }

    }
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
                  int index = findSelectItem(texts.get(0).toString());
                  if (index >= 0) {
                      vList.setSelection(index);
                  }
              }

          } while(false);

          return host.onRequestSendAccessibilityEventInternal(child, event);
      }

      private int findSelectItem(String text) {
          if(mLoadFiles == null) {
              return -1;
          }

          for(int i = 0; i < mLoadFiles.size(); i++) {
              if(mLoadFiles.get(i).getName().equals(text)) {
                  return i;
              }
          }

          return -1;
      }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mViewMode = VIEW_MODE_GRID;
    super.onCreate(savedInstanceState);

    //SKY luojie 20180211 add for remove the MediaMainActivity begin
    checkDeviceNum();
    MmpApp mmp = (MmpApp) this.getApplication();
    mmp.setIsFirst(true);
    mmp.add(this);
    mmp.register();
    TVBlock tvBlock = TVBlock.getInstance();
    tvBlock.init(this.getApplicationContext());
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
    registerUSBReceiver();
    registerShutDownReceivers();
    Thumbnail.getInstance().setContext(getApplicationContext());
    DivxUtil.checkDivxSupport(this.getApplicationContext());
    getScreenWH();
    mLogicManager = LogicManager.getInstance(this.getApplicationContext());
    mLogicManager.setThreadHandler(mThreadHandler);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }
    if (!Util.mIsEnterPip) {
      mLogicManager.finishVideo();
    }
    SundryImplement mNavSundryImplement = SundryImplement
            .getInstanceNavSundryImplement(this);
    boolean isFreeze = mNavSundryImplement.isFreeze();
    Log.d(TAG, "onCreate isFreeze:" + isFreeze);
    if (isFreeze) {
      int setValue = mNavSundryImplement.setFreeze(false);
    }
    Util.isMMpActivity(this);
    mTTSUtil = new TextToSpeechUtil(getApplicationContext());
    if (mLogicManager.usbNotConnet){
      mLogicManager.stopAudio();
    }
    //SKY luojie 20180211 add for remove the MediaMainActivity end

	tempPostion = 0;
    // if(AnimationManager.getInstance().getIsAnimation()){
    // AnimationManager.getInstance().startActivityEnterAnimation(this,
    // findViewById(R.id.mmp_files_grid_layout), null);
    // }
    Util.LogLife(TAG, "onCreate");
	setCurrentMountName();
    vList.setAccessibilityDelegate(mAccDelegate);

    //SKY luojie 20180108 added for UI begin
    int contentType = getIntent().getIntExtra(MultiMediaConstant.MEDIAKEY,
            MultiMediaConstant.VIDEO);
    mContentView = findViewById(R.id.mmp_files_grid_layout);
    if (contentType == MultiMediaConstant.VIDEO) {
      mContentView.setBackgroundResource(R.drawable.mmp_files_bg_video);
    } else if (contentType == MultiMediaConstant.AUDIO) {
      mContentView.setBackgroundResource(R.drawable.mmp_files_bg_audio);
    } else {
      mContentView.setBackgroundResource(R.drawable.mmp_files_bg_photo);
    }
    //SKY luojie 20180108 added for UI end
  }

  @Override
  protected void onResume() {
    super.onResume();
    //Add by renchao for fix bug 71876
    MultiFilesManager fileManager = MultiFilesManager.getInstance(this);
    if(!fileManager.isUsbAvalibale()){
      MtkLog.d(TAG,"request refresh layout after usb is ejected");
      refresh();
    }
    if ((getListContentType() != MultiMediaConstant.VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      showMusicView();
      MtkLog.i(TAG, "onResume musicview status:" + LogicManager.getInstance(this).getAudioStatus());
    } else {
      MtkLog.i(TAG, "onResume status:" + LogicManager.getInstance(this).getAudioStatus());
    }

    mPlayMode = 0;
    Util.LogLife(TAG, "onResume");

    //SKY luojie 20180211 add for remove the MediaMainActivity begin
        //        changeCategoryStatus();
    if(mAdapter.getCount() > 0) {
            //            vList.requestFocus();
    } else {
            //            onCategoryBtnFousced();
    }
    mGridHandler.sendEmptyMessageDelayed(SET_BTN_FOCUSABLE, 2000);
  
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }

    if (!((MmpApp) getApplication()).isEnterMMP()) {
      ((MmpApp) getApplication()).setEnterMMP(true);
      ((MmpApp) getApplication()).setVolumeUpdate(1);
      initMmp();
      if (mLogicManager.isAudioOnly()) {
        mLogicManager.setAudioOnly(false);
      }
    }
    if (SaveValue.getInstance(this).readValue(MenuConfigManager.EXO_PLAYER_SWITCHER) != 0) {
      Log.d(TAG, "is exo");
      Util.mIsUseEXOPlayer = true;
    } else {
      Log.d(TAG, "is not exo");
      Util.mIsUseEXOPlayer = false;
    }
    //SKY luojie 20180211 add for remove the MediaMainActivity end
  }
  //Add by renchao for fix bug 71876
  private void refresh(){
    View view = getWindow().getDecorView();
    view.invalidate();
  }
  private void onShowMusicView() {
    if ((getListContentType() != MultiMediaConstant.VIDEO)
        && LogicManager.getInstance(this).isAudioStarted()) {
      if (mPopView != null && !mPopView.isShowing()) {
        if (GetCurrentTask.getInstance(getApplicationContext()).isCurActivtyGridActivity()) {
          showMusicView();
        }
      }
    }
  }

  IAudioPlayListener mListener = new IAudioPlayListener() {

    @Override
    public void notify(int status) {
      // TODO Auto-generated method stub
      LogicManager.getInstance(MtkFilesGridActivity.this).registerAudioPlayListener(null);
      mGridHandler.sendEmptyMessage(SHOW_MUSIC);
    }
  };

  @Override
  protected void onRestart() {
    super.onRestart();
    MultiFilesManager multiFileManager = MultiFilesManager
            .getInstance(this);
    multiFileManager.addObserver(this);
    String currentPath = mFilesManager.getCurrentPath();
    if (multiFileManager.isLocalDevUnmountByPath(currentPath)){
      openDir(MultiFilesManager.ROOT_PATH, 0);
    }else {
	    int position = setCurrentSelection();
	    vList.requestFocusFromTouch();
	    vList.setSelection(position);
	    mAdapter.notifyDataSetChanged();
	    Util.LogLife(TAG, "onRestart");
		setCurrentMountName();
	    // LogicManager.getInstance(this).registerAudioPlayListener(mListener);
	}
  }

  @Override
  protected void onStart() {
    super.onStart();
    Util.LogLife(TAG, "onStart");
    LogicManager.getInstance(this).registerAudioPlayListener(mListener);

  }

  @Override
  protected void initMusicView() {
    View contentView = LayoutInflater.from(this).inflate(
        R.layout.mmp_musicbackplay, null);
    // int width = getWindowManager().getDefaultDisplay().getRawWidth();
    // int height = getWindowManager().getDefaultDisplay().getRawHeight();
        mPopView = new PopupWindow(contentView, 350, 96);
    vMusicView = new MusicPlayInfoView(this, contentView, 0,
        mPopView);
    if (LogicManager.getInstance(getApplicationContext()).usbNotConnet){
      mPopView.dismiss();
      vMusicView.setVisibility(View.INVISIBLE);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    MtkLog.d(TAG, "onNewIntent call mAdapter.clearWork");
    mAdapter.clearWork();
    Util.LogLife(TAG, "onNewIntent");
    if(getIntent() != null) {
      ((MtkFilesGridAdapter) mAdapter).mSelectedPosition = getIntent().getIntExtra(
              INTENT_NAME_SELECTION, -1);
    }
        //        setCategoryBtnFocusable(true);
  }

  /*
   * private OnCompletionListener mCompletionListener = new OnCompletionListener() { public void
   * onCompletion(CHMtkMediaPlayer arg0) { // TODO Log.e("JF", "grid oncompletion"); //if (flag ==
   * 1) { vMusicView.removeMessage(); mPopView.dismiss(); //} //return false; } };
   */

  @Override
  protected void showMusicView() {
    super.showMusicView();

    Looper.myQueue().addIdleHandler(new IdleHandler() {
      @Override
      public boolean queueIdle() {

                mPopView.showAtLocation(vLeftMidTv, Gravity.LEFT | Gravity.BOTTOM,
                        50, 20);
        MtkLog.d(TAG,"showMusicView ");
        vMusicView.init(MtkFilesGridActivity.this);
        return false;
      }
    });
  }

  @Override
  protected int setupContentView() {
    return R.layout.mmp_files_grid;
  }

  @Override
  protected FilesAdapter getAdapter() {
    return new MtkFilesGridAdapter(this);
  }

  @Override
  protected void findViews() {
    vLeftTopImg = (ImageView) findViewById(R.id.multimedia_showinfo_img);
    vLeftMidTv = (TextView) findViewById(R.id.multimedia_showinfo_left);
    vLeftTopTv = (TextView) findViewById(R.id.multimedia_showinfo_tv);
    vTopPath = (TextView) findViewById(R.id.multimedia_tv_filepath);
    MtkLog.d(TAG,"--grid findViews--MediaType TextSize:"+vLeftTopTv.getTextSize()+" Path TextSize:"+vTopPath.getTextSize());
    //vTopRight = (TextView) findViewById(R.id.multimedia_tv_right);

        //shenqi 20180328 modify begin
        //luojie 20180208 added begin
        //        mBtnMovie = (RadioButton) findViewById(R.id.btn_category_movie);
        //        mBtnPicture = (RadioButton) findViewById(R.id.btn_category_picture);
        //        mBtnMusic = (RadioButton) findViewById(R.id.btn_category_music);
        //        mBtnFocusChangeListener = new BtnOnFocusChangeListener();
        //        mBtnMovie.setOnFocusChangeListener(mBtnFocusChangeListener);
        //        mBtnPicture.setOnFocusChangeListener(mBtnFocusChangeListener);
        //        mBtnMusic.setOnFocusChangeListener(mBtnFocusChangeListener);
        //luojie 20180208 added end

        mFileCategory_rv = findViewById(R.id.file_category_recycler_view);

        mCategoriesLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mFileCategory_rv.setLayoutManager(mCategoriesLayoutManager);
        FileCategoryManager fileCategoryManager = new FileCategoryManager(getApplicationContext());
        mFileCategoryAdapter = new FileCategoryAdapter(fileCategoryManager.getCategory());
        mFileCategory_rv.setAdapter(mFileCategoryAdapter);
        mFileCategoryAdapter.setOnItemFocusedListener(new FileCategoryFocusedListener());
        //end by shenqi
    }

  @Override
  protected void setupHeader() {
    int contentType = getListContentType();
    if (contentType == FilesManager.CONTENT_THRDPHOTO) {
      contentType = FilesManager.CONTENT_PHOTO;
    }
	if(contentType >-1){
    vLeftTopImg.setImageDrawable(mContentTypeIcons[contentType]);
    vLeftTopTv.setText(mContentTypeNames[contentType]);
	}
        //        vLeftTopTv.setSelected(true);

    String curPath = getListCurrentPath();
    if (curPath != null && curPath.startsWith("/storage")) {
      MultiFilesManager multiFileManager = MultiFilesManager
          .getInstance(this);
      List<FileAdapter> deviceList = multiFileManager.getLocalDviceAdapter();
      if (deviceList != null && deviceList.size() > 0) {
        for (int i = 0; i < deviceList.size(); i++) {
          if (curPath.contains(deviceList.get(i).getPath())) {
            curPath = curPath.substring(deviceList.get(i).getPath().length());
            curPath = "/storage/" + deviceList.get(i).getName() + curPath;
            break;
          }
        }
      }
    }
    vTopPath.setText(curPath);
    //vTopRight.setText("");
    vLeftMidTv.setText("");
  }

  @Override
  public void onBackPressed() {
	  if(mFilesManager != null){
		   tempPostion =1;
		   isBack = true;
	  }
	 
    if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      super.onBackPressed();
      return;
    }
    if (!MediaMainActivity.isValid(300)) {
      return;
    }
    LogicManager mLogicManager = LogicManager.getInstance(this);

    MultiFilesManager multiFileManager = MultiFilesManager
        .getInstance(this);
    String path = multiFileManager.getFirstDeviceMountPointPath();
    int sourceType = multiFileManager.getCurrentSourceType();

    String parentPath = getListParentPath();

    String currentPath = getListCurrentPath();

    MtkLog.d(TAG, "onBackPressed ParentPath :" + parentPath + "---curpath = " + currentPath
        + "---mMode= " + mMode + "---deviceNum= " + multiFileManager.getAllDevicesNum() + "--path:"
        + path);

    if ((null != parentPath && parentPath.equals("/mnt"))
        || MultiFilesManager.ROOT_PATH.equals(currentPath)) {
      onReachRoot(0);
      return;
    }

    if (multiFileManager.getAllDevicesNum() == MultiFilesManager.ONE_DEVICES) {
      if (getListCurrentPath().equals(path) || mMode == MODE_RECURSIVE) {
        onReachRoot(0);
        return;
      }
    } else if (multiFileManager.getAllDevicesNum() == MultiFilesManager.MORE_DEVICES) {
      if (MultiFilesManager.getInstance(getApplicationContext()).isContainMountPoint(currentPath)) {
        openDir(MultiFilesManager.ROOT_PATH, mFilesManager.popOpenedHistory());
        return;
      }
    }

    if (mMode == MODE_RECURSIVE) {
      openDir(MultiFilesManager.ROOT_PATH, 0);
    } else {
      openDir(getListParentPath(), mFilesManager.popOpenedHistory());
    }
    if (null != mLogicManager) {
      mLogicManager.stopDecode();
    }
  }

  @Override
  protected void onReachRoot(int selection) {
    stopMusicView();
    if (selection == 0) {

      mAdapter.cancel();
      final Thumbnail thumbnail = Thumbnail.getInstance();
      if (thumbnail.isLoadThumanil()) {
        if (mThreadHandler != null) {
          mThreadHandler.post(new Runnable() {

            @Override
            public void run() {
              thumbnail.stopThumbnail();
            }
          });
        }
      }
    }
    // Don't remove listener by jianfang.
    super.onReachRoot(selection);
    destroyManger();
    LogicManager mLogicManager = LogicManager.getInstance(this);
    if (mLogicManager.isAudioStarted()
        && mLogicManager.getPlayMode() != AudioConst.PLAYER_MODE_LOCAL) {
      mLogicManager.stopAudio();
    }
    finish();
//    exit();
  }

  @Override
  protected void playFile(String path) {
    if (mPopView != null && mPopView.isShowing()) {
      vMusicView.removeMessage();
      mPopView.dismiss();
    }

    super.playFile(path);
  }

  @Override
  protected void stopMusicView() {
    if (vMusicView != null) {
      vMusicView.removeMessage();
    }
    super.stopMusicView();
  }

  protected void moveTo(int selection) {
    // mAdapter.cancel();
    // setListAdapter(mAdapter);
    setListSelection(selection);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    if(event.getKeyCode() == KeyMap.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_UP) {
      final FileAdapter file = getListItem(getListSelectedItemPosition());
      if(file != null) {
                //                setCategoryBtnFocusable(false);
        //Prevent quick clicks ANR  ???
        if (isValid()) {
          onListItemClickHandle(getListSelectedItemPosition());
        }

        vList.requestFocus();
      }
      return false;
    }

	  tempPostion = 2;
	  isBack = false;
    if (LogicManager.getInstance(this).isAudioOnly()) {
      /* by lei add for fix cr DTV00390970 */
      if (event.getAction() == KeyEvent.ACTION_UP) {
        LogicManager.getInstance(this).setAudioOnly(false);
      }
      return true;
    }
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      int keyCode = event.getKeyCode();
      int position = getListSelectedItemPosition();
      int count = getListItemsCount();
      int rowSize = getNumberColum();
      MtkLog.i(TAG, "dispatchKeyEvent keyCode= " + keyCode);
	    switch (keyCode) {
	        case KeyMap.KEYCODE_0:
	            Log.d(TAG, "focused view is : " + getWindow().getDecorView().findFocus());
	            break;
	        case KeyMap.KEYCODE_DPAD_LEFT: {
	            Log.d(TAG, "dispatchKeyEvent   KEYCODE_DPAD_LEFT");
	            View focusedView = getWindow().getDecorView().findFocus();
	            if (vList.hasFocus() && vList.getSelectedItemPosition() % getNumberColum() == 0 || null == focusedView) {
	                if (null != focusedView) {
	                    ((MtkFilesGridAdapter) mAdapter).mSelectedPosition = -1;
	                    mAdapter.notifyDataSetChanged();
	                }
	                mFileCategoryAdapter.setShowSelect(false);
	                vList.setFocusable(false);
	                int pos = mFileCategoryAdapter.getSelectPosition();
	                mCategoriesLayoutManager.scrollToPosition(pos);
	                int firstItem = mCategoriesLayoutManager.findFirstVisibleItemPosition();
	                int lastItem = mCategoriesLayoutManager.findLastVisibleItemPosition();
	                mFileCategory_rv.setFocusable(true);
	                if (pos < firstItem) {
	                    mFileCategory_rv.scrollToPosition(pos);
	                    mFileCategory_rv.getChildAt(pos).requestFocus();
	                } else if (pos <= lastItem) {
	                    int top = mFileCategory_rv.getChildAt(pos - firstItem).getTop();
	                    mFileCategory_rv.scrollBy(0, top);
	                    mFileCategory_rv.getChildAt(pos).requestFocus();
	                } else {
	                    mFileCategory_rv.scrollToPosition(pos);
	                    mFileCategory_rv.getChildAt(pos).requestFocus();
	                }
	                return true;
	            } else if (mFileCategory_rv.hasFocus()) {
	                return true;
	            } else {
	                if (position >= 1) {
	                    position -= 1;
	                    mCurrentPage = computeCurrentPage(position);
	                    setListSelection(position);
	                    vList.playSoundEffect(SoundEffectConstants.NAVIGATION_LEFT);
	                    return true;
	                }
	            }
	            return true;
	        }
	        case KeyMap.KEYCODE_DPAD_RIGHT: {
	            Log.d(TAG, "dispatchKeyEvent   KEYCODE_DPAD_RIGHT");
	            if (mFileCategory_rv.hasFocus()) {
	                //                        mFileCategoryAdapter.setPositionFocusToOut();
	                mFileCategoryAdapter.setShowSelect(true);
	                mFileCategory_rv.setFocusable(false);
	                vList.setFocusable(true);
	                setListSelection(vList.getSelectedItemPosition());
	                ((MtkFilesGridAdapter) mAdapter).mSelectedPosition = vList.getSelectedItemPosition();
	                vList.requestFocus();
	                mAdapter.notifyDataSetChanged();
	                return true;
	            }
	            if ((position + 1) < count) {
	                if ((position + 1) % getNumberColum() == 0) {
	                    return true;
	                } else {
	                    position += 1;
	                    mCurrentPage = computeCurrentPage(position);
	                    setListSelection(position);
	                    vList.playSoundEffect(SoundEffectConstants.NAVIGATION_RIGHT);
	                    return true;
	                }
	            }
	            return true;
	        }
	        default:
	            break;
	    }
        }

    return super.dispatchKeyEvent(event);
  }

  void scrollTo(int count) {
    int scrollY;
    int marginRows = getMarginRows(count);
    if (marginRows > 0) {
      scrollY = marginRows * (mItemHeight + mVerticalSpacing)
          + mVerticalSpacing;
      vList.scrollTo(vList.getScrollX(), scrollY);
    }
  }

  private int getMarginRows(int count) {
    int marginRows = 0;
    if (count % mPageSize > 0) {
      marginRows = (mPageSize - count % mPageSize) / getNumberColum();
    }
    return marginRows;
  }

  //SKY luojie 20180211 add for remove the MediaMainActivity begin
  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND
            || keyCode == KeyMap.KEYCODE_MTKIR_FASTFORWARD) {
      if (mLogicManager.isAudioFast() || !mLogicManager.canSeek()) {
        return true;
      }
      try {
        Log.d(TAG, "onKeyUp seek progress:" + mSeekingProgress);
        mLogicManager.seekToCertainTime(mSeekingProgress);
        mGridHandler.sendEmptyMessageDelayed(MSG_RESET_SEEKIING, 100);
      } catch (Exception e) {
        Log.e(TAG, "onKeyUp Seek exception");
        mIsSeeking = false;
        return true;
      }
    }
    return super.onKeyUp(keyCode, event);
  }
  //SKY luojie 20180211 add for remove the MediaMainActivity end

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
      tempPostion = 2;
	  isBack = false;
	  if(keyCode == KeyMap.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
         if(mFilesManager != null){
		   tempPostion =1;
		   isBack = true;
		}
	  }

    //SKY luojie 20180211 add for remove the MediaMainActivity begin
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        Log.d(TAG, "onKeyDown KEYCODE_MTKIR_REPEAT");
        onRepeat();
        return true;
      }
      // Play next audio
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        Log.d(TAG, "onKeyDown KEYCODE_MTKIR_NEXT");
        mLogicManager.playNextAudio();
        return true;
      }
      // Play previous audio
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
        Log.d(TAG, "onKeyDown KEYCODE_MTKIR_PREVIOUS");
        mLogicManager.playPrevAudio();
        return true;
      }
      case KeyMap.KEYCODE_MTKIR_FASTFORWARD:
      case KeyMap.KEYCODE_MTKIR_REWIND: {
        if (!mLogicManager.canSeek()) {
          return true;
        }
        if (mLogicManager.isAudioPlaying()) {
          mLogicManager.pauseAudio();
        }
        if (!mIsSeeking) {
          mIsSeeking = true;
          int progressTemp = mLogicManager.getPlaybackProgress();
          mSeekingProgress = progressTemp;//progressTemp & 0xffffffffL;
        }
        if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND) {
          mSeekingProgress = mSeekingProgress - MusicPlayInfoView.SEEK_DURATION;
          if (mSeekingProgress < 0) {
            mSeekingProgress = 0;
          }
        } else {
          mSeekingProgress = mSeekingProgress + MusicPlayInfoView.SEEK_DURATION;
          int totalProgressTemp = mLogicManager.getTotalPlaybackTime();
          if (mSeekingProgress > totalProgressTemp) {
            mSeekingProgress = totalProgressTemp;
          }
        }
        Log.i(TAG, "seek progress calc:" + mSeekingProgress);
        return true;
      }
      // Pause audio
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
        Log.d(TAG, "onKeyDown KEYCODE_MTKIR_PLAYPAUSE");
        onPauseOrPlay();
        return true;
      }
      // Stop audio
      case KeyMap.KEYCODE_MTKIR_STOP: {
        Log.d(TAG, "onKeyDown KEYCODE_MTKIR_STOP");
        mLogicManager.stopAudio();
                vMusicView.changeVisualizer();
        return true;
      }
    }
    //SKY luojie 20180211 add for remove the MediaMainActivity end

      /*case KeyMap.KEYCODE_MTKIR_BLUE:
        if (!MediaMainActivity.isValid(400)) {
          break;
        }
        exitPIP();
        LogicManager.getInstance(getApplicationContext()).finishVideo();
        if (VideoPlayActivity.getInstance() != null) {
          VideoPlayActivity.getInstance().finish();
        }
        // if(MultiFilesManager.getInstance(this).getCurrentSourceType() == 0){
        // return true;
        // }

        // by lei add for stop video and audio get thumbnail 
        int contentType = getListContentType();
        // ((MtkFilesGridAdapter) mAdapter).stop();

        mAdapter.stop();
        if (contentType == FilesManager.CONTENT_AUDIO
            || contentType == FilesManager.CONTENT_VIDEO) {
          ((MtkFilesGridAdapter) mAdapter).stopFileDecode();
        }

        mIsListMode = true;
        if (null != mInfoLoader) {
          mInfoLoader.clearQueue();
        }

        Intent intent = new Intent(this, MtkFilesListActivity.class);
        intent.putExtra(INTENT_NAME_PATH, getListCurrentPath());
        intent.putExtra(INTENT_NAME_SELECTION,
            getListSelectedItemPosition() + 1);
        intent.putExtra("Mode", 0);
        intent.putStringArrayListExtra(INTENT_NAME_COPYED_FILES,
            mCopyedFiles);
        intent.putStringArrayListExtra(INTENT_NAME_SELECTED_FILES,
            mSelectedFiles);
        // add for ThumbnailSize bug
        intent.putExtra("mThumbnailSize", mThumbnailSize);

        startActivity(intent);
        finish();
        break;
      default:
        break;
    }
*/
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onListItemSelected(AbsListView l, View v, int position,
      long id) {
    //SKY luojie 20180108 added for modify UI begin
    ((MtkFilesGridAdapter) mAdapter).mSelectedPosition = position;
    mAdapter.notifyDataSetChanged();
    //SKY luojie 20180108 added for modify UI end

    MtkLog.d(TAG, "onListItemSelected~~~ position= " + position);
    mGridHandler.removeMessages(SELECTED);
    mGridHandler.sendEmptyMessageDelayed(SELECTED, 800);
	if ( position > 0) {
     com.mediatek.wwtv.mediaplayer.util.Util.UsingUsbName = mFilesManager.getCurrentPath();
    }
	MtkLog.d(TAG, "yangxiong= " + com.mediatek.wwtv.mediaplayer.util.Util.UsingUsbName);
  }

    public  void setCurrentMountName() {
		com.mediatek.wwtv.mediaplayer.util.Util.UsingUsbName = mFilesManager.getCurrentPath();
   
  }
  
  private void setSelect() {
    int position = vList.getSelectedItemPosition();
    MtkLog.d(TAG, "setSelect~~~:" + position);
    FileAdapter file = getListItem(position);
	//tempPostion =0    :   enter Filelist 
	//tempPostion = 2  :   onkeydown but not back 
    //tempPostion =	1    :  press back
	//tempPostion =	-1    :   first enter
	MtkLog.d("yangxiong", "tempPostion:" + tempPostion+"______________count:"+isBack);
	if(isBack || (tempPostion !=2 && (!("/".equals(getListCurrentPath()))))){
	//refreshListView();
    vList.setSelection(position);
	tempPostion = 2;
	isBack = false;
	}
    //if (mPageCount == 0) {
    //  vTopRight.setText("");
    //} else {
    //  mCurrentPage = computeCurrentPage(position);
    //  MtkLog.d(TAG, "onListItemSelected aft mCurrentPage" + mCurrentPage);
    //  vTopRight.setText(mCurrentPage + "/" + mPageCount);
    //}

    if (file == null) {
      return;
    }
    if (file.isIsoFile()) {
      // TODO mount iso file
      return;
    }
    if (file.isFile()) {
      int type = mFilesManager.getContentType();
      String info = "";
      if (type == MultiMediaConstant.AUDIO || type == MultiMediaConstant.VIDEO
          || type == MultiMediaConstant.PHOTO || type == MultiMediaConstant.THRD_PHOTO) {
        if (MultiFilesManager.isSourceDLNA(getApplicationContext())) {
          info = mInforCache.get(file.getAbsolutePath() + file.getSuffix());
        } else {
          info = mInforCache.get(file.getAbsolutePath());
        }
        if (info != null) {
          vLeftMidTv.setText(info);
          return;
        } else {
          vLeftMidTv.setText("");
        }
        String suffix = file.getSuffix();
        if (null != suffix && suffix.startsWith(".")) {
          suffix = suffix.substring(1);
        }
        if ("pcm".equalsIgnoreCase(suffix)
            || "lpcm".equalsIgnoreCase(suffix)) {
          MtkLog.w(TAG, "pcm file:" + file.getAbsolutePath());
          vLeftMidTv.setText(file.getName() + file.getSuffix());
          return;
        }
        // fix
        if (mInfoLoader != null) {
          // mInfoLoader.clearQueue();
          LoadInfo loadinfo = mLoadInfoWorks.get(file.getAbsolutePath());
          if (null == loadinfo) {
            loadinfo = new LoadInfo(file, vLeftMidTv);
          }
          mInfoLoader.addSelectedInfoWork(loadinfo);
        } else {

          mInfoLoader = AsyncLoader.getInstance(1);
          if (null == mLoadInfoWorks) {
            mLoadInfoWorks = new ConcurrentHashMap<String, LoadInfo>();
          }
          LoadInfo loadinfo = new LoadInfo(file, vLeftMidTv);
          mInfoLoader.addSelectedInfoWork(loadinfo);

        }
        // info = mInforCache.get(file.getAbsolutePath());
      } else {
        info = file.getInfo();
        vLeftMidTv.setText(info);
        return;
      }
    } else {
      vLeftMidTv.setText("");
    }

  }

  @Override
  public void onScrollStateChanged(AbsListView view, int scrollState) {
    // MtkLog.d(TAG,"onScrollStateChanged scrollState ="+scrollState);

  };

  private int lastFirstVisibleItem = 0;

  @Override
  public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
      int totalItemCount) {

    if (firstVisibleItem != lastFirstVisibleItem) {
      // cancelLoadFiles();
      mAdapter.cancel();
//      Thumbnail.getInstance().stopThumbnail();
      lastFirstVisibleItem = firstVisibleItem;
      vList.invalidateViews();
    }

    MtkLog.d(TAG, "onScroll firstVisibleItem =" + firstVisibleItem + "visibleItemCount ="
        + visibleItemCount +
        "totalItemCount = " + totalItemCount);

  };

  @Override
  public void onNothingSelected(AdapterView<?> parent) {
    vLeftMidTv.setText("");
  }

  protected int computePageSize() {
    mPageSize = getRowNumber() * getNumberColum();
    mItemWidth = getColumnWidth();
    mItemHeight = getColumnHeight();
    int filesCount = getListItemsCount();
    mPageCount = ceilPage(filesCount - 1, mPageSize);
    return mPageSize;
  }

  private int ceilNumber(int count, int number) {
    if (number == 0) {
      return 0;
    }
    if (count % number == 0) {
      return count / number;
    } else {
      return (count / number) + 1;
    }

  }

  public int ceilPage(int position, int pageSize) {

    if (pageSize == 0) {
      return 0;
    }
    // index begin from 0
    position += 1;
    if (position == 1) {
      return 1;
    } else if (position % pageSize == 0) {
      return position / pageSize;
    } else {
      return position / pageSize + 1;
    }

  }

  protected int computeCurrentPage(int position) {
    MtkLog.d(TAG, "computeCurrentPage  position: " + position);
    int page = mCurrentPage;
    if (mPageSize > 0 && position >= 0 && position < getListItemsCount()) {
      page = ceilPage(position, mPageSize);
    }

    return page;
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
    mPageSize = computePageSize();
    mCurrentPage = computeCurrentPage(getListSelectedItemPosition());
    MtkLog.e(TAG, "MtkFilesGridActivity refreshListView mPageSize:"+mPageSize+",mCurrentPage:"+mCurrentPage);
    //if (mPageCount == 0) {
    //  vTopRight.setText("0/0");
    //} else {
    //  vTopRight.setText(mCurrentPage + "/" + mPageCount);
    //}
  }

  public int getRowNumber() {
    /*switch (mThumbnailSize) {
      case MultiMediaConstant.SMALL: {
        return 5;
      }
      case MultiMediaConstant.MEDIUM: {
        return 4;
      }
      case MultiMediaConstant.LARGE: {
        return 3;
      }
      default:
        return 5;
    }*/
    //
    return 2;
  }

  public int getColumnHeight() {
    /*int rowNumber = getRowNumber();
    int height = (mGridViewH - mVerticalSpacing * (rowNumber + 1))
        / rowNumber;*/
    MtkLog.i(TAG, "getColumnHeight:" + mItemHeight);
    return mItemHeight;
  }

  public int getNumberColum() {
    /*switch (mThumbnailSize) {
      case MultiMediaConstant.SMALL: {
        return 7;
      }
      case MultiMediaConstant.MEDIUM: {
        return 6;
      }
      case MultiMediaConstant.LARGE: {
        return 5;
      }
      default:
        return 5;
    }*/
    //
    return 5;
  }

  public int getColumnWidth() {
    /*int columnNumber = getNumberColum();
    int width = (mGridViewW - mHorizontalSpacing * (columnNumber + 1))
        / columnNumber;*/
    MtkLog.i(TAG, "getColumnWidth:" + mItemWidth+" mHSpacing:"+mHorizontalSpacing);
    return mItemWidth;
  }

  @Override
  protected void refreshListView(List<FileAdapter> files) {
    ((MediaGridView) vList).setNumColumns(getNumberColum());

    super.refreshListView(files);

    //if (files != null) {
    //  mPageSize = computePageSize();
    //  mCurrentPage = computeCurrentPage(getListSelectedItemPosition());
    //  if (mPageCount == 0) {
    //    vTopRight.setText("0/0");
    //  } else {
    //    vTopRight.setText(mCurrentPage + "/" + mPageCount);
    //  }
    //}
  }

  @Override
  protected void openDevicePath() {
    List<FileAdapter> fileList = ((MultiFilesManager) mFilesManager).getCurrentFiles();
    MtkLog.d(TAG, "openDevicePath count:" + fileList.size());
    FileAdapter file = null;
    if (MediaMainActivity.mAutoTestFileDirectorys != null
        && MediaMainActivity.mAutoTestFileDirectorys.size() > 0) {
      MtkLog.d(TAG, "openDevicePath mAutoTestFileDirectorys:"
          + MediaMainActivity.mAutoTestFileDirectorys.get(0));
      for (int i = 0; i < fileList.size(); i++) {
        file =  fileList.get(i);
        if (file != null) {
          String name = file.getName();
          if (name.equals(MediaMainActivity.mAutoTestFileDirectorys.get(0))) {
            MediaMainActivity.mAutoTestFileDirectorys.remove(0);
            openDir(file.getAbsolutePath());
            return;
          }
        }
      }
    }
  }

  @Override
  protected void searchDesFile(boolean isDlna) {
    MtkFilesGridAdapter tempAdapter = (MtkFilesGridAdapter)getListAdapter();
    int count = tempAdapter.getCount();
    MtkLog.d(TAG, "searchDesFile count:" + count);
    tempFile = null;
    FileAdapter file = null;
    if (MediaMainActivity.mAutoTestFileDirectorys != null
        && MediaMainActivity.mAutoTestFileName != null
        && MediaMainActivity.mAutoTestFilePath != null) {
      if (MediaMainActivity.mAutoTestFileDirectorys.size() > 0) {
        for (int i = 0; i < count; i++) {
          file =  getListItem(i);
          if (file != null) {
            String name = file.getName();
            if (name.equals(MediaMainActivity.mAutoTestFileDirectorys.get(0))) {
              MediaMainActivity.mAutoTestFileDirectorys.remove(0);
              openDir(file.getAbsolutePath());
              return;
            }
          }
        }
        MtkLog.d(TAG, "searchDesFile has no find directory:"
            + MediaMainActivity.mAutoTestFileDirectorys.get(0));
      } else {
        MtkLog.d(TAG, "searchDesFile mAutoTestFileName:" + MediaMainActivity.mAutoTestFileName);
        for (int i = 0; i < count; i++) {
          file =  getListItem(i);
          if (file != null) {
            String name = file.getName();
            if (isDlna) {
              name = name + file.getSuffix();
            }
//            MtkLog.d(TAG, "searchDesFile name:" + name);
            if (name.equals(MediaMainActivity.mAutoTestFileName)) {
              mAdapter.cancel();
              if (mThreadHandler != null) {
                tempFile = file;
                mThreadHandler.post(new Runnable() {

                  @Override
                  public void run() {
                    try {
                      tempFile.stopThumbnail();
                    } catch (Exception ex) {
                      ex.printStackTrace();
                    }
                  }
                });
              }
              int contentType = getListContentType();
              PlayList.getPlayList().cleanList(contentType + 1);
              List<FileAdapter> files = new ArrayList<FileAdapter>();
              files.add(file);
              PlayList playlist = null;
              if (isDlna) {
                playlist = MultiFilesManager.getInstance(this)
                    .getPlayList(files, 0, contentType, MultiFilesManager.SOURCE_DLNA);
              } else {
                playlist = MultiFilesManager.getInstance(this)
                    .getPlayList(files, 0, contentType, MultiFilesManager.SOURCE_SMB);
              }
              MtkLog.d(TAG, "searchDesFile start to play:");
              playFile(file.getAbsolutePath());
              return;
            }
          }
        }
        MtkLog.d(TAG, "searchDesFile has no find file:");
      }
    }
  }

  @Override
  protected void cancelLoadFiles() {
    if (!mIsListMode) {
      mAdapter.cancel();
    }
    mIsListMode = false;
    super.cancelLoadFiles();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Util.LogLife(TAG, "onDestroy");
    vList.setAccessibilityDelegate(null);

    //SKY luojie 20180211 add for remove the MediaMainActivity begin
    unregisterReceiver(mUSBBroadcastReceiver);
    unregisterReceiver(mShutDownBroadcastReceiver);
    mLogicManager.setThreadHandler(null);
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    if (!Util.mIsEnterPip) {
      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
    }
    Util.LogLife(TAG, "onDestroy");

    if (null != mTTSUtil){
      mTTSUtil.shutdown();
    }
    exitMediaMain();
    //SKY luojie 20180211 add for remove the MediaMainActivity end
  }

  @Override
  protected void onStop() {
    super.onStop();
    LogicManager.getInstance(this).registerAudioPlayListener(null);
    Util.LogLife(TAG, "onStop");
  }

//  public void exit() {
//    // if(AnimationManager.getInstance().getIsAnimation()){
//    // AnimationManager.getInstance().startActivityEndAnimation(this,
//    // findViewById(R.id.mmp_files_grid_layout), null);
//    // }else{
//    finish();
//    // }
//  }

  private class MtkFilesGridAdapter extends FilesAdapter {
    private static final String TAG = "MtkFilesGridAdapter";
    private static final int MAX_NUM_RNUUNABLE = 300;

    private Drawable mVideoDefault;
    private Drawable mAudioDefault;
    private Drawable mPhotoDefault;
    private Drawable mTextDefault;
    private Drawable mSmbFolder;
    private Drawable mDlnaFolder;
    private Drawable mFolder;
    private Drawable mFailed;

    private final Handler mBindHandler;
    private final BitmapCache mCache;
    private final AsyncLoader<Bitmap> mLoader;
    private final ConcurrentHashMap<View, LoadBitmap> mWorks;
    private final ConcurrentHashMap<View, Runnable> mRunnables;

    //SKY luojie 20180108 added for modify UI begin
        public int mSelectedPosition = -1;
    //SKY luojie 20180108 added for modify UI end

    @Override
    public void clearWork() {
      // mWorks.clear();
      if (mRunnables.size() > MAX_NUM_RNUUNABLE)
        mRunnables.clear();
    }

    // private PhotoLoader mPhotoLoader;
    public MtkFilesGridAdapter(Context context) {
      super(context);
      mBindHandler = new Handler();
      mCache = BitmapCache.createCache(false);
      // mLoader = new AsyncLoader<Bitmap>(1);
      mLoader = AsyncLoader.getInstance(1);
      mWorks = new ConcurrentHashMap<View, LoadBitmap>();
      mRunnables = new ConcurrentHashMap<View, Runnable>();

      // mPhotoLoader = new PhotoLoader(context,
      // R.drawable.mmp_thumbnail_loading_failed_samll);

      prepareDefaultThumbnails(mThumbnailSize);
    }

    private void prepareDefaultThumbnails(int size) {
      if (size == MultiMediaConstant.SMALL) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_small);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_samll);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_samll);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_samll);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_samll);
      } else if (size == MultiMediaConstant.MEDIUM) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_middle);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_middle);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_middle);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_middle);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_mid);
      } else if (size == MultiMediaConstant.LARGE) {
        mVideoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_video_big);
        mAudioDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_audio_big);
        mPhotoDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_photo_big);
        mTextDefault = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_text_big);
        mFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_middle);
        mSmbFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_smb);
        mDlnaFolder = mResources
            .getDrawable(R.drawable.mmp_thumbnail_icon_folder_dlna);
        mFailed = mResources
            .getDrawable(R.drawable.mmp_thumbnail_loading_failed_big);
      }
      mVideoDefault = mResources.getDrawable(R.drawable.mmp_thumbnail_icon_video_default);
      mAudioDefault = mResources.getDrawable(R.drawable.mmp_thumbnail_icon_music_default);
      mTextDefault = mResources.getDrawable(R.drawable.mmp_thumbnail_icon_text_default);
      mFolder = mResources.getDrawable(R.drawable.mmp_thumbnail_icon_folder_default);
      mPhotoDefault = mResources.getDrawable(R.drawable.mmp_thumbnail_icon_photo_default);
            mFailed = mResources.getDrawable(R.drawable.mmp_thumbnail_loading_failed_default);
    }

    @Override
    public int getCount() {
      if (mDataList == null) {
        return 0;
      }
      // TODO fixed bug may be bug
      if (mTurnPage) {
        if (mPageSize > 0 && (getListItemsCount() > mPageSize)
            && getListItemsCount() % mPageSize > 0) {
          return super.getCount()
              + (mPageSize - getListItemsCount() % mPageSize);
        }
      }
      // MtkLog.i(TAG, "getCount count =files size");
      return super.getCount();
    }

    @Override
    public FileAdapter getItem(int position) {
      if (position >= getListItemsCount()) {
        return null;
      }

      return super.getItem(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
      return false;
    }

    @Override
    public boolean isEnabled(int position) {
      // return false;
      if (position >= getListItemsCount()) {
        return false;
      }

      return super.isEnabled(position);
    }

    @Override
    protected void updateThumbnail() {
      mCache.clear();
      prepareDefaultThumbnails(mThumbnailSize);
      computePageSize();
      int selection = getListSelectedItemPosition();
      mCurrentPage = computeCurrentPage(selection);
      //if (mPageCount == 0) {
      //  vTopRight.setText("0/0");
      //} else {
      //  vTopRight.setText(mCurrentPage + "/" + mPageCount);
      //}
      ((MediaGridView) vList).setNumColumns(getNumberColum());
      // setListAdapter(mAdapter);
      notifyDataSetChanged();
      setListSelection(selection);

    }

    @Override
    protected int getItemLayout() {
      return R.layout.mmp_asycgriditem;
    }

//    @Override
//    public void onClick(View v) {
//      Log.d("zwh", "----------------onClick:------------------ ");
//    }
//
//    @Override
//    public boolean onKey(View v, int keyCode, KeyEvent event) {
//      Log.d("zwh", "-----------------onKey:-------------------- ");
//      return false;
//    }

    @Override
    protected void initView(View v,  FileAdapter data, boolean flag) {
      MtkLog.i(TAG, "initView mItemWidth:" + mItemWidth + "--mItemHeight:" + mItemHeight);
      if (!flag) {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mItemWidth, mItemHeight);
        ViewHolder holder = new ViewHolder();
        holder.img = (ImageView) v.findViewById(R.id.multimedia_gv_img);
		holder.imgOver = (ImageView) v.findViewById(R.id.multimedia_gv_img_over);
        holder.tv = (TextView) v.findViewById(R.id.multimedia_gv_tv);
        holder.layout = v.findViewById(R.id.mmp_grid_highlight);
        holder.layoutRoot = v.findViewById(R.id.mmp_grid_root);
        holder.layoutRoot.setLayoutParams(lp);
        holder.tv.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        v.setTag(holder);
      } else {
        GridView.LayoutParams lp = (GridView.LayoutParams) v.getLayoutParams();
        lp.width = mItemWidth;
        lp.height = mItemHeight;
        v.setLayoutParams(lp);
      }
    }

    @Override
    protected void bindView(View v, FileAdapter data, int position) {
      MtkLog.i(TAG, "bindView start");
      ViewHolder holder = (ViewHolder) v.getTag();
      if (data == null) {
	    v.setBackground(null);
        holder.path = null;
        holder.img.setImageDrawable(null);
                holder.imgOver.setVisibility(View.GONE);
        holder.tv.setText("");
        holder.layout.setBackground(null);
        return;
      }

      String path = data.getAbsolutePath();
      // MtkLog.d(TAG, "BindView : " + path);

      /*
       * Cancel task when load success, so move to loaded function and the task had add queue, will
       * don't add.
       */
      // cancelLastWork(holder.img);

      MultiFilesManager manager = ((MultiFilesManager) mFilesManager);
      int source = manager.getSourceType(path);
      if(!data.isPhotoFile()) {
        holder.tv.setText(data.getName());
      } else {
        holder.tv.setText("");
      }
      holder.tv.setVisibility(View.VISIBLE);
      if (data.isDirectory() ||
          (source == MultiFilesManager.SOURCE_LOCAL && data.isIsoFile())) {
        switch (source) {
          case MultiFilesManager.SOURCE_LOCAL:
            holder.img.setImageDrawable(mFolder);
			holder.tv.setText(data.getName());
            break;
          case MultiFilesManager.SOURCE_SMB:
            holder.img.setImageDrawable(mFolder);
			holder.tv.setText("SMB:"+data.getName());
            break;
          case MultiFilesManager.SOURCE_DLNA:
            holder.img.setImageDrawable(mFolder);
			holder.tv.setText("DLNA:"+data.getName());
            break;
          default:
            break;
        }

        if (holder.tv != null) {
          MtkLog.i(TAG,
              "holder.tv:width:" + holder.tv.getWidth() + "--height:" + holder.tv.getHeight()
                  + "--content:" + holder.tv.getText().toString());
          if (holder.tv.getVisibility() != View.VISIBLE) {
            MtkLog.i(TAG, "holder.tv not visible");
          } else {
            MtkLog.i(TAG, "holder.tv visible");
          }
        }
        holder.img.invalidate();
        //holder.layout.setBackgroundDrawable(null);
      } else {
        int type = mFilesManager.getContentType();
        if (type == MultiMediaConstant.TEXT) {
          holder.img.setImageDrawable(mTextDefault);
        } else if (type == MultiMediaConstant.AUDIO) {
          holder.img.setImageDrawable(mAudioDefault);
        } else if (type == MultiMediaConstant.PHOTO) {
          holder.img.setImageDrawable(mPhotoDefault);
        } else if (type == MultiMediaConstant.VIDEO) {
          holder.img.setImageDrawable(mVideoDefault);
        } else if (type == MultiMediaConstant.THRD_PHOTO) {
          holder.img.setImageDrawable(mPhotoDefault);
        }
        //if (mCopyedFiles.size() > 0 && mCopyedFiles.contains(path)) {
        //  holder.layout
        //      .setBackgroundResource(R.drawable.mmp_gridview_copyed);
        //} else if (mSelectedFiles.size() > 0 && mSelectedFiles.contains(path)) {
        //  holder.layout
        //      .setBackgroundResource(R.drawable.mmp_gridview_selected);
        //} else {
        //  holder.layout.setBackgroundDrawable(null);
        //}
        if (type == MultiMediaConstant.AUDIO)
        {
          String suffix = data.getSuffix();
          if (FileSuffixConst.DLNA_FILE_NAME_EXT_PCM.equalsIgnoreCase(suffix))
          {
            return;
          }
        }
        //EXO DLNA MARK
//      if (!(Util.isUseExoPlayer()
//      && ((MultiFilesManager) mFilesManager).getCurrentSourceType()
//      != MultiFilesManager.SOURCE_LOCAL)) {
				if (getListContentType() != MultiMediaConstant.AUDIO) {
					Log.i(TAG, "bindThumbnail: audio");
					bindThumbnail(data, holder.img, holder.imgOver, path);
				}
//      }
      }
      MtkLog.i(TAG, "bindView end");

      //SKY luojie 20180108 added for modify UI begin
	    if (mSelectedPosition == position && mFileCategoryAdapter.isShowSelect()) {
	        holder.layout.setBackgroundColor(getResources().getColor(R.color.mmp_asycgriditem_color_selected));
	        onItemFocusedState(v);
	    } else {
	        holder.layout.setBackgroundColor(getResources().getColor(R.color.mmp_asycgriditem_color_normal));
	        onItemNormalState(v);
	    }
      //SKY luojie 20180108 added for modify UI begin
    }

        private void bindThumbnail(FileAdapter data, ImageView view, ImageView viewOver, String path) {

      // if(!Util.isGridActivity(getApplicationContext())){
      // return;
      // }
      Bitmap image = mCache.get(path);
      if (image != null) {
        view.setImageBitmap(image);
		view.setScaleType(ImageView.ScaleType.CENTER_CROP);
		viewOver.setVisibility(View.VISIBLE);
      } else {
        Log.i(TAG, "bindThumbnail LoadBitmap!!" + mWorks.get(view) + "  " + path);
        MtkLog.i(TAG, "bindThumbnail LoadBitmap!!" + mWorks.get(view) + "  " + path);
        if (mWorks.get(view) == null) {
          LoadBitmap work = new LoadBitmap(data, view, viewOver);
          mWorks.put(view, work);
          mLoader.addWork(work);
        }
      }
    }

    private void cancelLastWork(View view) {
      LoadBitmap work = mWorks.get(view);

      if (work != null) {
        MtkLog.w(TAG, "Cancel Work!!");
        // work.getData().stopThumbnail();
        mLoader.cancel(work);
        mWorks.remove(view);

      }

      Runnable r = mRunnables.get(view);
      if (r != null) {
        MtkLog.w(TAG, "Cancel Runnable!!");
        mBindHandler.removeCallbacks(r);
        mRunnables.remove(view);
      }
    }

    @Override
    public void stop() {
      // stopThumbnail();
      synchronized (mWorks) {
        mLoader.clearQueue();
        mWorks.clear();
      }
    }

    public void stopFileDecode() {
      // LoadBitmap work = null;
      // Enumeration<View> views = mWorks.keys();
      // ArrayList<View> viewList = Collections.list(views);
      // View view = null;
      //
      // for (int i = 0; i < mWorks.size(); i++) {
      // view = viewList.get(i);
      // work = mWorks.get(view);
      // if (work != null && work.getData() != null) {
      // FileAdapter file = work.getData();
      // if (!file.isDirectory()) {
      // file.stopThumbnail();
      // break;
      // }
      // }
      // }
    }

    private void logCaheSize() {
      ConcurrentHashMap<String, SoftReference<Bitmap>> map = mCache
          .getCache();
      Iterator<Entry<String, SoftReference<Bitmap>>> iterator = map
          .entrySet().iterator();
      int count = 0;
      int recycles = 0;
      while (iterator.hasNext()) {
        SoftReference<Bitmap> ref = iterator.next().getValue();
        if (null != ref) {
          Bitmap value = ref.get();
          if (null != value) {
            count++;
            if (!value.isRecycled()) {
              recycles++;
              value.recycle();
              value = null;
            }
          }
        }
      }

      MtkLog.i(TAG, " count:" + count + " recycles:" + recycles);

    }

    // private void stopThumbnail(){
    //
    // MtkLog.d(TAG, "stopThumbnail : ---------------" );
    // LoadBitmap work = null;
    // Enumeration<View> views= mWorks.keys();
    // ArrayList< View> viewList = Collections.list(views);
    // View view = null;
    // for (int i=0; i < mWorks.size(); i++){
    // view = viewList.get(i);
    // work = mWorks.get(view);
    // work.getData().stopThumbnail();
    // }
    // }

    @Override
    protected void cancel() {
      mLoader.clearQueue();
      mWorks.clear();
    }

    private class ViewHolder {
      ImageView img;
      TextView tv;
      RelativeLayout layout;
      RelativeLayout layoutRoot;
      String path;
	  ImageView imgOver;
    }

    private class LoadBitmap implements LoadWork<Bitmap> {
      private final FileAdapter mData;
      private final ImageView vImage;
	  private final ImageView vImageOver;
      private Bitmap mResult;
      private boolean mNeedCache = true;

      public LoadBitmap(FileAdapter data, ImageView image, ImageView imageOver) {
        mData = data;
        vImage = image;
		vImageOver = imageOver;
      }

      public FileAdapter getData() {
        return mData;
      }

      @Override
      public Bitmap load() {
        Bitmap bitmap = null;
        try {
          MtkLog.i(TAG, "mThumbnailSize:" + mThumbnailSize);
          int width = (int) (mItemWidth*1.2);
          if (mThumbnailSize == MultiMediaConstant.MEDIUM) {
            width = (int) (mItemWidth*1.2);
          } else if (mThumbnailSize == MultiMediaConstant.LARGE) {
            width = (int) (mItemWidth*1.2);
          }
          //Original
          bitmap = mData.getThumbnail(width, width, true);
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
              Log.d(TAG, "run: " + mData.getAbsolutePath());
			  vImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
			  vImageOver.setVisibility(View.VISIBLE);
          }
        }
      }
    }
  }

  @Override
  public void handleRootMenuEvent() {
    // TODO Auto-generated method stub
    super.handleRootMenuEvent();
    LogicManager.getInstance(this).stopAudio();
    stopMusicView();
    if (MediaMainActivity.mIsDlnaAutoTest || MediaMainActivity.mIsSambaAutoTest) {
      finish();
    }
  }

  //SKY luojie 20180108 added for add choose menu begin
  @Override
  public void finish() {
    super.finish();
    //Intent intent = new Intent(this, MediaMainActivity.class);
    //startActivity(intent);
  }

  private void onItemNormalState(View v) {
    if(v.getElevation()==4){
      v.setElevation(0);
      v.animate().scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
    }
  }

  private void onItemFocusedState(View v) {
    v.setElevation(4);
    v.animate().scaleX(1.16f).scaleY(1.16f).translationZ(6).start();
  }

  protected void onBtnCategoryNormalStatus(View view) {
    if(view == null) return;
    view.animate().scaleX(1.0f).scaleY(1.0f).translationZ(1.0f).translationX(-2).start();
  }

  protected void onBtnCategoryFocusedStatus(View view) {
    if(view == null) return;
    view.animate().scaleX(1.3f).scaleY(1.3f).translationZ(8f).translationX(2).start();
  }

  private void changeContentType(int type) {
    if(type == getListContentType()) return;
    Log.d(TAG, "changeContentType  type:" + type);
    ((MtkFilesGridAdapter) mAdapter).mSelectedPosition = -1;
    setListContentType(type);
//    setCategoryBtnFocusable(false);
    openDir(getListCurrentPath(), -1);
    mGridHandler.removeMessages(CHANGE_BACKGROUND);
    mGridHandler.sendEmptyMessage(CHANGE_BACKGROUND);
//    vList.requestFocus();
  }

    private class FileCategoryFocusedListener implements FileCategoryAdapter.OnItemFocusedListener {

        @Override
        public void onItemFocused(View view, int position) {
            changeContentType(FILE_CATEGORY[position]);
            if (mGridHandler.hasMessages(CHANGE_BACKGROUND)) {
                mGridHandler.removeMessages(CHANGE_BACKGROUND);
            }
            mGridHandler.sendEmptyMessage(CHANGE_BACKGROUND);
        }
    }

  private void changeBackground() {
    if (getListContentType() == MultiMediaConstant.VIDEO) {
      mBackgroundTransitionDrawable = new TransitionDrawable(new Drawable[] {
              mContentView.getBackground(),
              getResources().getDrawable(R.drawable.mmp_files_bg_video)});

    } else if (getListContentType() == MultiMediaConstant.AUDIO) {
      mBackgroundTransitionDrawable = new TransitionDrawable(new Drawable[] {
              mContentView.getBackground(),
              getResources().getDrawable(R.drawable.mmp_files_bg_audio)});
    } else {
      mBackgroundTransitionDrawable = new TransitionDrawable(new Drawable[] {
              mContentView.getBackground(),
              getResources().getDrawable(R.drawable.mmp_files_bg_photo)});
    }
    mContentView.setBackground(mBackgroundTransitionDrawable);
    mBackgroundTransitionDrawable.startTransition(1000);
  }


  //SKY luojie 20180211 add for remove the MediaMainActivity begin
  private final static String ACTION_USB ="android.hardware.usb.action.USB_STATE";

  private void registerUSBReceiver() {
    IntentFilter filter = new IntentFilter();
    filter.addAction(ACTION_USB);
    filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
    mUSBBroadcastReceiver = new USBBroadcastReceiver();
    registerReceiver(mUSBBroadcastReceiver,filter);
  }

  private void registerShutDownReceivers() {
    // to restore videoResource when powerdown
    mShutDownBroadcastReceiver = new ShutDownBroadcastReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SHUTDOWN);
    filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    registerReceiver(mShutDownBroadcastReceiver, filter);
    Util.LogLife(TAG, "registerShutDownReceivers: " + ((MmpApp) getApplication()).isEnterMMP());
  }

  private class ShutDownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)){
        LogicManager.getInstance(MtkFilesGridActivity.this)
                .restoreVideoResource();
      }
      if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
        //Modified by duzhihong for solving "last memory not work when abnormal exit"
        LastMemory.saveLastMemory(getApplicationContext());
        mLogicManager.sendCloseBroadCast();
      }
    }
  }

  private void initMmp() {
    Log.i(TAG, "MtkTvAppTV updatedSysStatus");
    MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    Log.i(TAG, "MtkTvAppTV updatedSysStatus later");
  }

  private void getScreenWH() {
    DisplayMetrics dm = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(dm);
    SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
    SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;

    ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
    ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
  }

  private void checkDeviceNum() {
    SaveValue pref = SaveValue.getInstance(this);
    boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false : true;
    boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false : true;

    MultiFilesManager.getInstance(this, smbAvailable, dlnaAvailable)
            .getLocalDevices();
    int deviceNum = MultiFilesManager.getInstance(this)
            .getAllDevicesNum();
    if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable
            && !smbAvailable) {
      //Toast.makeText(this, getString(R.string.mmp_nodevice), Toast.LENGTH_LONG).show();
    }

    if (dlnaAvailable || smbAvailable){
      MtkFilesBaseListActivity.reSetModel();
    }
  }

  private void exitMediaMain() {
    if (!Util.mIsEnterPip) {
      ((MmpApp) getApplication()).setVolumeUpdate(0);
    }
    MtkLog.d(TAG, "exitMediaMain.... ");
    Util.exitMmpActivity(getApplicationContext());
    resetResouce();
  }

  private void resetResouce() {
    Log.d(TAG, "resetResouce.... ");
    ((MmpApp) getApplication()).remove(this);
    mLogicManager.sendCloseBroadCast();
    AsyncLoader.getInstance(0).clearQueue();
    Thumbnail thumbnail = Thumbnail.getInstance();
    if (thumbnail.isLoadThumanil()) {
      thumbnail.stopThumbnail();
    }
    MtkFilesBaseListActivity.reSetModel();
  }

  private void onRepeat() {
    int repeatModel = mLogicManager.getRepeatModel(Const.FILTER_AUDIO);
    repeatModel = (repeatModel + 1) % 3;
    mLogicManager.setRepeatMode(Const.FILTER_AUDIO, repeatModel);
  }

  private void onPauseOrPlay() {
    if (mLogicManager.getAudioPlaybackService() == null) {
      Log.i(TAG, "mLogicManager.getAudioPlaybackService() == NULL");
      return;
    }
    if (mLogicManager.isAudioPause() || mLogicManager.isAudioFast()
            || mLogicManager.isAudioStoped()) {
      Log.i(TAG, "onPauseOrPlay audio status= pasue | Fast | Stop");
      mLogicManager.playAudio();
	  vMusicView.changeVisualizer();
    } else if (mLogicManager.isAudioPlaying()) {
      Log.i(TAG, "onPauseOrPlay audio is playing");
      mLogicManager.pauseAudio();
	  vMusicView.changeVisualizer();
    }
  }
  //SKY luojie 20180211 add for remove the MediaMainActivity end
}
