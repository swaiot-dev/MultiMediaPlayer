
package com.mediatek.wwtv.mediaplayer.mmp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.RowPresenter;

import android.os.HandlerThread;
import android.os.UserHandle;

import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;////Modified by duzhihong for solving "last memory not work when abnormal exit"
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.setting.presenter.SettingItem;
import com.mediatek.wwtv.mediaplayer.setting.presenter.SettingItemPresenter;
import com.mediatek.wwtv.mediaplayer.tvchannel.TVBlock;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.util.*;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.MusicPlayInfoView;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.iRootMenuListener;
import com.mediatek.wwtv.mediaplayer.nav.util.SundryImplement;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvHighLevelBase;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.mmp.util.AsyncLoader;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesBaseListActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;

import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter.ViewHolder;
import android.support.v17.leanback.widget.Row;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.Toast;
import android.os.SystemProperties;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.wwtv.mediaplayer.util.TextToSpeechUtil;
import com.mediatek.wwtv.mediaplayer.util.AudioBTManager;
import com.mediatek.dm.DeviceManagerEvent;

public class MediaMainActivity extends Activity {

  String TAG = "MediaMainActivity";
  //SKY luojie 20180113 modify begin
  public static int mSelection = 0;
  //SKY luojie 20180113 modify end
  private static final String TAG_BROWSE_FRAGMENT = "browse_fragment";
  private Context mContext;
  private BrowseFragment mBrowseFragment;
  protected final ArrayList<HeaderItem> mHeaderItems = new ArrayList<HeaderItem>();
  //protected final ArrayList<HeaderItem> mSettingItems = new ArrayList<HeaderItem>();//modified by yangxiong for "block samba function"
  protected final SparseArray<ArrayObjectAdapter> mRows = new SparseArray<ArrayObjectAdapter>();
  private final ClassPresenterSelector mItemPresenterSelector = new ClassPresenterSelector();
  private TVContent mTV;
  private ArrayObjectAdapter adapter;
  private final HashMap<String, Integer> activityMap = new HashMap<String, Integer>();
  private ShutDownBroadcastReceiver mReceiver;

  // when back key pressed,mmp_flag = true;
  private boolean mmp_flag = false;

  private static final int MSG_AUTO_TEST_START = 1106;
  private static final int MSG_START_EPG_DELAY = 1100;
  private static final int MSG_RESET_SEEKIING = 1101;
  private boolean mIsSeeking;
  private int mSeekingProgress;
  private LogicManager mLogicManager;
  public static boolean mIsDlnaAutoTest;
  public static boolean mIsSambaAutoTest;
  public static String mAutoTestFilePath;
  public static String mAutoTestFileName;
  public static List<String> mAutoTestFileDirectorys;
  public USBBroadcastReceiver mUSBBroadcastReceiver;
  private TextToSpeechUtil mTTSUtil;
  private static MediaMainActivity mMediaMainActivity = null;

  private HandlerThread mHandlerThead;
  private Handler mThreadHandler;

  private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(android.os.Message msg) {
      switch (msg.what) {
        case MSG_START_EPG_DELAY:
          // EPGManager.getInstance(MediaMainActivity.this).startEpg(MediaMainActivity.this,
          // NavBasic.NAV_REQUEST_CODE);
          break;
        case MSG_AUTO_TEST_START:
          startAutoTest();
          break;
        case MSG_RESET_SEEKIING:
          mIsSeeking = false;
          break;
        default:
          break;
      }
    };
  };

//  View mLayout = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

//    initMmp();
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    MmpApp mmp = (MmpApp) this.getApplication();
    mmp.setIsFirst(true);
    mmp.add(this);
    mmp.register();
    mmp.registerRootMenu(mRootMenuListener);
    // SKY luojie added begin
    TVBlock tvBlock = TVBlock.getInstance();
    tvBlock.init(this.getApplicationContext());
    // SKY luojie added end
    registerMyReceiver();
    mHandlerThead = new HandlerThread(TAG);
    mHandlerThead.start();
    mThreadHandler = new Handler(mHandlerThead.getLooper());
//    setContentView(R.layout.browse_fragment);
//    mLayout = findViewById(R.id.main_frag_layout);
//    mBrowseFragment = (BrowseFragment) getFragmentManager()
//        .findFragmentById(R.id.browse_fragment);
    mBrowseFragment = (BrowseFragment) getFragmentManager().findFragmentByTag(
        TAG_BROWSE_FRAGMENT);
    if (mBrowseFragment == null) {
        mBrowseFragment = new BrowseFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mBrowseFragment, TAG_BROWSE_FRAGMENT)
                .commit();
    }
    mContext = this;
    mMediaMainActivity = this;
    mTV = TVContent.getInstance(mContext);
    Thumbnail.getInstance().setContext(mContext.getApplicationContext());
    DivxUtil.checkDivxSupport(this.getApplicationContext());
    ClassPresenterSelector rowPresenterSelector = new ClassPresenterSelector();
    ListRowPresenter mListRowPresenter = new ListRowPresenter();
    mListRowPresenter.enableChildRoundedCorners(true);
    rowPresenterSelector.addClassPresenter(ListRow.class, mListRowPresenter);

    adapter = new ArrayObjectAdapter();
    //mHeaderItems.add(new HeaderItem(0x0001, ""));
    mHeaderItems.add(new HeaderItem(0x0002, ""+getResources().getString(R.string.mmp_main_playback)));
    //mSettingItems.add(new HeaderItem(0x0003, " "+getResources().getString(R.string.mmp_main_settings)));//modified by yangxiong for "block samba function"
    mItemPresenterSelector.addClassPresenter(SettingItem.class, new SettingItemPresenter());
    initMenuRows();
    for (int i = 0; i < mHeaderItems.size(); i++) {
      ArrayObjectAdapter temp = mRows.get((int) mHeaderItems.get(i).getId());
      temp.setPresenterSelector(mItemPresenterSelector);
      adapter.add(new ListRow(mHeaderItems.get(i), temp));
    }
	//begin => modified by yangxiong for "block samba function"
    /*for (int i = 0; i < mSettingItems.size(); i++) {
      ArrayObjectAdapter temp = mRows.get((int) mSettingItems.get(i).getId());
      temp.setPresenterSelector(mItemPresenterSelector);
      adapter.add(new ListRow(mSettingItems.get(i), temp));
    }*/
    //end => modified by yangxiong for "block samba function"
    adapter.setPresenterSelector(rowPresenterSelector);
    mBrowseFragment.setAdapter(adapter);

    //SKY luojie 20180110 modify for UI begin
    BackgroundManager backgroundManager = BackgroundManager.getInstance(this);
    backgroundManager.attach(getWindow());
    Drawable background = getResources().getDrawable(R.drawable.mmp_main_background, null);
    backgroundManager.setDrawable(background);
    //SKY luojie 20180110 modify for UI end

    mBrowseFragment.setTitle("");
    mBrowseFragment.setBadgeDrawable(null);
    mBrowseFragment.setHeadersState(BrowseFragment.HEADERS_DISABLED);
    mBrowseFragment.setOnItemViewSelectedListener(new OnItemViewSelectedListener() {

      @Override
      public void onItemSelected(
          ViewHolder arg0,
          Object arg1,
          android.support.v17.leanback.widget.RowPresenter.ViewHolder arg2,
          Row arg3) {
        if (arg1 instanceof SettingItem) {
          SettingItem citem = (SettingItem) arg1;
          Log.i(TAG, "title:" + citem.getmTitle());
          mSelection = activityMap.get(citem.getmTitle());
          Log.i(TAG, "mSelection:" + mSelection);
        }
      }
    });
	mBrowseFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
      @Override
      public void onItemClicked(ViewHolder itemViewHolder, Object item, RowPresenter.ViewHolder rowViewHolder, Row row) {
         MtkLog.i("yangxiong", "item:"+item);
		 showFilesGrid(mSelection);
      }
    });

    mHandler.postDelayed(new Runnable() {

      @Override
      public void run() {
        if (null != mBrowseFragment){
            mBrowseFragment.setSelectedPosition(mSelection);
        }
      }
    }, 500);

    getScreenWH();

    mLogicManager = LogicManager.getInstance(this.getApplicationContext());
    mLogicManager.setThreadHandler(mThreadHandler);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }
    if (!Util.mIsEnterPip) {
      mLogicManager.finishVideo();
    }
    // mLogicManager.freeVideoResource();
    SundryImplement mNavSundryImplement = SundryImplement
        .getInstanceNavSundryImplement(this);
    boolean isFreeze = mNavSundryImplement.isFreeze();
    MtkLog.d(TAG, "MediaMain onCreate isFreeze:" + isFreeze);
    if (isFreeze) {
      int setValue = mNavSundryImplement.setFreeze(false);
    }

//    Util.setHandler(mHandler);
//    Util.setMMPFlag(true);
    mmp_flag = true;
    Util.isMMpActivity(this);
    registerReceivers();
    mTTSUtil = new TextToSpeechUtil(mContext);
    if (mLogicManager.usbNotConnet){
      mLogicManager.stopAudio();
    }

  }
 // private USBBroadcastReceiver mUSBBroadcastReceiver;
	private final static String ACTION ="android.hardware.usb.action.USB_STATE";
		private void registerMyReceiver() {
			Log.i("yangxiong", "cur:registerMyReceiver");
			IntentFilter filter = new IntentFilter();
			filter.addAction(ACTION);
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
          mUSBBroadcastReceiver = new USBBroadcastReceiver();
            registerReceiver(mUSBBroadcastReceiver,filter);
		}
  private void initMmp() {
    // TODO Auto-generated method stub
    // MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_MISC_AV_COND_MMP_MODE, 1);
    //int cur = LogicManager.getInstance(this).getCurPictureMode();
    //Log.i(TAG, "cur:--" + cur);
    //LogicManager.getInstance(this).setPictureMode(cur);
    Log.i(TAG, "MtkTvAppTV updatedSysStatus");
    MtkTvAppTV.getInstance().updatedSysStatus(MtkTvAppTV.SYS_MMP_RESUME);
    Log.i(TAG, "MtkTvAppTV updatedSysStatus later");
//    AudioBTManager.getInstance(getApplicationContext()).creatAudioPatch();
  }

  private void registerReceivers() {
    // to restore videoResource when powerdown
    mReceiver = new ShutDownBroadcastReceiver();
    IntentFilter filter = new IntentFilter();
    filter.addAction(Intent.ACTION_SHUTDOWN);
	filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    registerReceiver(mReceiver, filter);
    Util.LogLife(TAG, "onCreate end: " + ((MmpApp) getApplication()).isEnterMMP());
  }

  private void unRegisterReceivers() {
    unregisterReceiver(mReceiver);
  }

  private class ShutDownBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN)){
		LogicManager.getInstance(MediaMainActivity.this)
				.restoreVideoResource();
		}
		if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)){
          LastMemory.saveLastMemory(getApplicationContext());//Modified by duzhihong for solving "last memory not work when abnormal exit"
		  mLogicManager.sendCloseBroadCast();
		}
    }
  }

  private void showFilesGrid(int contentType) {
    MtkLog.i(TAG, "showFilesGrid:" + mSelection);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    } else {
      Util.mIsEnterPip = false;
    }
    if (MultiFilesManager.hasInstance()) {
      MultiFilesManager.getInstance(this).destroy();
    } else if (!Util.mIsEnterPip) {
      int result = DLNAManager.stoDMP();
      MtkLog.d(TAG, "showFilesGrid is not pip, stop dmp result:" + result);
    }
    if (mSelection == 5) {
      Intent intent = new Intent(MmpConst.INTENT_NETSETTING);
      startActivity(intent);
    } else if (mSelection == 4) {
      exitPIP();
      mLogicManager.stopAudio();
      mLogicManager.finishVideo();
      if (VideoPlayActivity.getInstance() != null) {
        VideoPlayActivity.getInstance().finish();
      }
      Intent intent = new Intent(MmpConst.INTENT_DMR);
      // Class clazz = Class.forName("com.mediatek.ui.mmp.DmrActivity");
      // intent.setClass(MediaMainActivity.this, DmrActivity.class);
      intent.putExtra("TKUI", true);
      this.startActivity(intent);
    } else {
      SaveValue pref = SaveValue.getInstance(this);

      boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false
          : true;
      boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false
          : true;
      MtkLog.d(TAG, "Dlna Available : " + dlnaAvailable
          + "Samba Available : " + smbAvailable);
      if (mIsDlnaAutoTest) {
        Log.d(TAG, "Dlna Available : " + dlnaAvailable
            + "Samba Available : " + smbAvailable);
      }
      if (contentType == FileConst.MMP_TYPE_ALL) {
        dlnaAvailable = false;
        smbAvailable = false;
      }
      MultiFilesManager.getInstance(this, smbAvailable, dlnaAvailable)
          .getLocalDevices();
      int deviceNum = MultiFilesManager.getInstance(this)
          .getAllDevicesNum();
      if (deviceNum == MultiFilesManager.NO_DEVICES && !dlnaAvailable
          && !smbAvailable) {
        return;
      }

      if (dlnaAvailable || smbAvailable){
        MtkFilesBaseListActivity.reSetModel();
      }

      MultiFilesManager.getInstance(this).pushOpenedHistory(contentType);
      Intent intent = new Intent(MmpConst.INTENT_FILEGRID);
      intent.putExtra(MultiMediaConstant.MEDIAKEY, contentType);
      startActivity(intent);
    }
  }

  protected void exitPIP() {
    //noral case, should close other android PIP.
    String package_name = this.getApplication().getPackageName();
    MtkLog.d(TAG, "send broadcast exit pip in main package_name:" + package_name);
    if (package_name == null || package_name.equals("")) {
      package_name = "com.mediatek.wwtv.mediaplayer";
    }
    Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
    intent.putExtra(Intent.EXTRA_PACKAGES,
        new String[]{package_name});
    intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE,
        Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
    this.sendBroadcastAsUser(intent,
          new UserHandle(ActivityManager.getCurrentUser()),
          android.Manifest.permission.RECEIVE_MEDIA_RESOURCE_USAGE);
  }

  private void showFilesList(int content) {
    MtkLog.i(TAG, "showFilesList:" + content);
    exitPIP();
    mLogicManager.finishVideo();
    if (VideoPlayActivity.getInstance() != null) {
      VideoPlayActivity.getInstance().finish();
    }
    Intent intent = new Intent(MmpConst.INTENT_FILELIST);
    intent.putExtra(MmpConst.INTENT_NAME_SELECTION, content);
    startActivity(intent);
  }

  private void exitMediaMain() {
    if (!Util.mIsEnterPip) {
      ((MmpApp) getApplication()).setVolumeUpdate(0);
    }
    MtkLog.d(TAG, "MediaMain exitMediaMain ");
    Util.exitMmpActivity(getApplicationContext());
    resetResouce();
  }

  private void resetResouce() {
    MtkLog.d(TAG, "MediaMain resetResouce ");
    ((MmpApp) getApplication()).remove(this);
    mLogicManager.sendCloseBroadCast();
    mSelection = 0;
    AsyncLoader.getInstance(0).clearQueue();
    Thumbnail thumbnail = Thumbnail.getInstance();
    if (thumbnail.isLoadThumanil()) {
      thumbnail.stopThumbnail();
    }
    MtkFilesBaseListActivity.reSetModel();

//    Util.setMMPFlag(false);
    mmp_flag = false;

  }

  private void getScreenWH() {
    DisplayMetrics dm = new DisplayMetrics();
    this.getWindowManager().getDefaultDisplay().getMetrics(dm);
    SettingsUtil.SCREEN_WIDTH = dm.widthPixels;
    SettingsUtil.SCREEN_HEIGHT = dm.heightPixels;

    ScreenConstant.SCREEN_WIDTH = dm.widthPixels;
    ScreenConstant.SCREEN_HEIGHT = dm.heightPixels;
  }

  private void initMenuRows() {
	//SKY luojie 20180110 modify for UI begin
    String videoTitle = getResources().getString(R.string.mmp_menu_video);
    String audioTitle = getResources().getString(R.string.mmp_menu_music);
    String photoTitle = getResources().getString(R.string.mmp_menu_phototext);
    //ArrayObjectAdapter emptyRow = new ArrayObjectAdapter();
    //mRows.put(0x0001, emptyRow);
    ArrayObjectAdapter menuRow = new ArrayObjectAdapter();
    SettingItem mVideo = new SettingItem(videoTitle, this.getResources().getDrawable(
        R.drawable.menu_video));
    activityMap.put(MmpConst.MMP_VIDEO, MmpConst.VIDEO);
    SettingItem mAudio = new SettingItem(photoTitle, this.getResources().getDrawable(
        R.drawable.menu_photo));
    activityMap.put(MmpConst.MMP_PHOTO, MmpConst.PHOTO);
    SettingItem mTV = new SettingItem(audioTitle, this.getResources().getDrawable(
        R.drawable.menu_audio));
    activityMap.put(MmpConst.MMP_AUDIO, MmpConst.AUDIO);
    //SettingItem mSetup = new SettingItem(MmpConst.MMP_TEXT, this.getResources().getDrawable(
    //    R.drawable.menu_text));
    //activityMap.put(MmpConst.MMP_TEXT, MmpConst.TXT);
    //SKY luojie 20180104 modify end
   // SettingItem mParent = new SettingItem(MmpConst.MMP_DMR, this.getResources().getDrawable(
   //     R.drawable.menu_dmr));
    //activityMap.put(MmpConst.MMP_DMR, MmpConst.DMR);
    //SKY luojie 20180110 modify for UI end

    menuRow.add(mVideo);
    menuRow.add(mAudio);
    menuRow.add(mTV);
    //SKY luojie 20180104 modify begin
    //menuRow.add(mSetup);
    //SKY luojie 20180104 modify end
    //menuRow.add(mParent);
    mRows.put(0x0002, menuRow);
    ArrayObjectAdapter SettingRow = new ArrayObjectAdapter();
	//begin => modified by yangxiong for "block samba function"
    /*SettingItem mSetting = new SettingItem(MmpConst.MMP_SETTING, this.getResources().getDrawable(
        R.drawable.content_setting));
    SettingRow.add(mSetting);
    activityMap.put(MmpConst.MMP_SETTING, MmpConst.MMP_SETTING_INT);
    mRows.put(0x0003, SettingRow);
    */// modified by yangxiong for "block samba function"
    //end => modified by yangxiong for "block samba function"
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    Util.LogLife(TAG, "onPause");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Util.LogLife(TAG, "onResume:" + ((MmpApp) getApplication()).isEnterMMP()
        + "  Util.mIsEnterPip:" + Util.mIsEnterPip);
    if (VideoPlayActivity.getInstance() != null) {
      Util.mIsEnterPip = VideoPlayActivity.getInstance().isInPictureInPictureMode();
    }
    Util.LogLife(TAG, "onResume:" + VideoPlayActivity.getInstance()
        + "  Util.mIsEnterPip:" + Util.mIsEnterPip);
    ((MmpApp) getApplication()).register();

    SaveValue pref = SaveValue.getInstance(this);
    if (0 != SystemProperties.getInt(MediaPlayActivity.AUTO_TEST_DLNA_PROPERTY, 0)) {
      mIsDlnaAutoTest = true;
      if (pref.readValue(MmpConst.DLNA) == 0) {
        pref.saveValue(MmpConst.DLNA, 1);
      }
    } else {
      mIsDlnaAutoTest = false;
      if (0 != SystemProperties.getInt(MediaPlayActivity.AUTO_TEST_PROPERTY, 0)){
          if (pref.readValue(MmpConst.DLNA) == 1) {
            pref.saveValue(MmpConst.DLNA, 0);
          }
      }
    }
    if (0 != SystemProperties.getInt(MediaPlayActivity.AUTO_TEST_SAMBA_PROPERTY, 0)) {
      mIsSambaAutoTest = true;
      if (pref.readValue(MmpConst.MY_NET_PLACE) == 0) {
        pref.saveValue(MmpConst.MY_NET_PLACE, 1);
      }
    } else {
      mIsSambaAutoTest = false;
      if (0 != SystemProperties.getInt(MediaPlayActivity.AUTO_TEST_PROPERTY, 0)){
          if (pref.readValue(MmpConst.MY_NET_PLACE) == 1) {
            pref.saveValue(MmpConst.MY_NET_PLACE, 0);
          }
      }
    }
    if (!((MmpApp) getApplication()).isEnterMMP()) {
      ((MmpApp) getApplication()).setEnterMMP(true);
      ((MmpApp) getApplication()).setVolumeUpdate(1);
      initMmp();
//      mLogicManager.freeVideoResource();
      if (mLogicManager.isAudioOnly()) {
        mLogicManager.setAudioOnly(false);
      }
      if (mIsDlnaAutoTest || mIsSambaAutoTest) {
        mHandler.sendEmptyMessageDelayed(MSG_AUTO_TEST_START, 1500);
      }
    }
    if (SaveValue.getInstance(this).readValue(MenuConfigManager.EXO_PLAYER_SWITCHER) != 0) {
      MtkLog.d(TAG, "is exo");
      Util.mIsUseEXOPlayer = true;
    } else {
      MtkLog.d(TAG, "is not exo");
      Util.mIsUseEXOPlayer = false;
    }

  }

  private void startAutoTest() {
    Intent intent = getIntent();
    mAutoTestFilePath = intent.getStringExtra("urlStr");
    int contentType = intent.getIntExtra("contentType", -1);
    Log.d(TAG, "contentType:" + contentType + "   mAutoTestFilePath:" + mAutoTestFilePath
        + "  " + intent.getDataString());
    String [] temArray = null;
    if (mAutoTestFilePath != null) {
      temArray = mAutoTestFilePath.split("/");
    }
    if (temArray != null && temArray.length > 0 && mSelection > -1 && mSelection < 5) {
      mAutoTestFileName = temArray[temArray.length - 1];
      if (mAutoTestFileDirectorys == null) {
        mAutoTestFileDirectorys = new ArrayList<String>();
      } else {
        mAutoTestFileDirectorys.clear();
      }
      for (int i = 0; i < temArray.length - 1; i++) {
        if (temArray[i] != null && temArray[i].length() > 0) {
          mAutoTestFileDirectorys.add(temArray[i]);
        }
      }
      mSelection = contentType;
      showFilesGrid(mSelection);
    } else {
      if (mSelection < 0 || mSelection > 5) {
        mSelection = 0;
      }
      Toast.makeText(getApplicationContext(), "Params error, please check.",
          Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  protected void onRestart() {
    // TODO Auto-generated method stub
    super.onRestart();
//    mLayout.setVisibility(View.VISIBLE);
    Log.i(TAG, "onRestart --mSelection:" + mSelection);
    Util.LogLife(TAG, "onRestart");
  }

  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
//    mLayout.setVisibility(View.INVISIBLE);
    Util.LogLife(TAG, "onStop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mmp_flag) {
      exitMediaMain();
    }
    if (mHandler != null) {
      mHandler.removeCallbacksAndMessages(null);
      mHandler = null;
    }
    if (mThreadHandler != null) {
      mThreadHandler.removeCallbacksAndMessages(null);
      mThreadHandler.getLooper().quit();
      mThreadHandler = null;
      mHandlerThead = null;
    }
    ((MmpApp) getApplication()).removeRootMenuListener(mRootMenuListener);
    ((MmpApp) getApplication()).remove(this);
    unregisterReceiver(mReceiver);
    // add by sky luojie begin
    unregisterReceiver(mUSBBroadcastReceiver);
    // add by sky luojie end
    if (!Util.mIsEnterPip) {
      AudioBTManager.getInstance(getApplicationContext()).releaseAudioPatch();
    }
    Util.LogLife(TAG, "onDestroy");

    if (null != mTTSUtil){
        mTTSUtil.shutdown();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.i(TAG, "onKeyDown keyCode:" + keyCode);
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    switch (keyCode) {
      case KeyMap.KEYCODE_DPAD_UP:
//        int selectionRow = mBrowseFragment.getSelectedPosition();
        if (mSelection == 5) {
          return false;
        } else {
          return true;
        }
//        break;
      case KeyMap.KEYCODE_DPAD_CENTER:

        showFilesGrid(mSelection);

        break;
      /*case KeyMap.KEYCODE_MTKIR_BLUE:
        if (isValid()) {
          // if(MultiFilesManager.getInstance(mContext).getCurrentSourceType() != 0){
          //showFilesList(mSelection);
          // }else{
          // return true;
          // }
        }
        // if (AnimationManager.getInstance().getIsAnimation()) {

        // }
        break;*/

      case KeyMap.KEYCODE_MTKIR_ANGLE:
        if (SystemProperties.getInt(MmpConst.AllMedia, 0) != 0) {

        }
        return true;
      case KeyMap.KEYCODE_BACK: {
        exitMediaMain();
        // if (AnimationManager.getInstance().getIsAnimation()) {
        // AnimationManager.getInstance().startActivityEndAnimation(this,
        // findViewById(R.id.mmp_main_layout), null);
        // } else {
        if (isValid()) {
          finish();
        }
        // }
        break;
      }
      case KeyMap.KEYCODE_DPAD_DOWN: {
        return false;
      }
//      // Mute
//      case KeyMap.KEYCODE_MTKIR_MUTE: {
//        mLogicManager.setMute();
//        return true;
//      }
      // Repeat
      case KeyMap.KEYCODE_MTKIR_REPEAT: {
        onRepeat();
        return true;
      }
      // Play next audio
      case KeyMap.KEYCODE_MTKIR_NEXT: {
        mLogicManager.playNextAudio();
        return true;
      }
      // Play previous audio
      case KeyMap.KEYCODE_MTKIR_PREVIOUS: {
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
        MtkLog.i(TAG, "seek progress calc:" + mSeekingProgress);

        return true;
      }


      // Pause audio
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE: {
        onPauseOrPlay();
        return true;
      }
      // Stop audio
      case KeyMap.KEYCODE_MTKIR_STOP: {
        mLogicManager.stopAudio();
        return true;
      }

      case KeyMap.KEYCODE_MTKIR_PIPPOP:
      case KeyMap.KEYCODE_MTKIR_PIPPOS:
      case KeyMap.KEYCODE_MTKIR_PIPSIZE:
      case KeyMap.KEYCODE_MTKIR_CHDN:
      case KeyMap.KEYCODE_MTKIR_CHUP:
        return false;
      case KeyMap.KEYCODE_MTKIR_GUIDE:
//        Util.startEPGActivity(MediaMainActivity.this);
        break;
      case KeyMap.KEYCODE_MENU:
        Intent intent = new Intent(MmpConst.INTENT_NETSETTING);
        if (MultiFilesManager.hasInstance()) {
          MultiFilesManager.getInstance(this).destroy();
        }
        startActivity(intent);
        break;
      default:
        MtkLog.i(TAG, "onKeyDown default");
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (keyCode == KeyMap.KEYCODE_MTKIR_REWIND
        || keyCode == KeyMap.KEYCODE_MTKIR_FASTFORWARD) {
      if (mLogicManager.isAudioFast() || !mLogicManager.canSeek()) {
        return true;
      }
      try {
        MtkLog.i(TAG, "seek progress:" + mSeekingProgress);
        mLogicManager.seekToCertainTime(mSeekingProgress);
        mHandler.sendEmptyMessageDelayed(MSG_RESET_SEEKIING, 100);
      } catch (Exception e) {
        MtkLog.i(TAG, "Seek exception");
        mIsSeeking = false;
        return true;
      }
    }
    return super.onKeyUp(keyCode, event);
  }

  private void onRepeat() {
    int repeatModel = mLogicManager.getRepeatModel(
        Const.FILTER_AUDIO);

    repeatModel = (repeatModel + 1) % 3;

    mLogicManager.setRepeatMode(Const.FILTER_AUDIO,
        repeatModel);
  }

  private void onPauseOrPlay() {

    if (mLogicManager.getAudioPlaybackService() == null) {
      MtkLog.i(TAG, "mLogicManager.getAudioPlaybackService() == NULL");
      return;
    }
    if (mLogicManager.isAudioPause() || mLogicManager.isAudioFast()
        || mLogicManager.isAudioStoped()) {
      MtkLog.i(TAG, "onPauseOrPlay audio status= pasue | Fast | Stop");
      mLogicManager.playAudio();
    } else if (mLogicManager.isAudioPlaying()) {
      MtkLog.i(TAG, "onPauseOrPlay audio is playing");
      mLogicManager.pauseAudio();
    }
  }

  private static long mLastKeyDownTime;

  public static boolean isValid() {
    boolean isValid = false;
    long currentTime = System.currentTimeMillis();
    if ((currentTime - mLastKeyDownTime) >= 1000) {
      mLastKeyDownTime = currentTime;
      isValid = true;
    } else {
      mLastKeyDownTime = currentTime;
    }
    return isValid;
  }

  /*
   * Used by other classes
   */
  public static boolean isValid(int millSeconds) {
    long currentTime = System.currentTimeMillis();
    boolean isValid = false;
    if ((currentTime - mLastKeyDownTime) >= millSeconds) {
      mLastKeyDownTime = currentTime;
      isValid = true;
    } else {
      mLastKeyDownTime = currentTime;
    }
    return isValid;
  }

  iRootMenuListener mRootMenuListener = new iRootMenuListener() {

    @Override
    public void handleRootMenu() {
      MtkLog.i(TAG, "handleRootMenu Received!");
      if (mIsDlnaAutoTest || mIsSambaAutoTest) {
        finish();
      }
    }
  };


    public static MediaMainActivity getInstance() {
        return mMediaMainActivity;
    }

    public TextToSpeechUtil getTTSUtil(){
        if (null == mTTSUtil){
            mTTSUtil = new TextToSpeechUtil(mContext);
        }

        return mTTSUtil;
    }
}
