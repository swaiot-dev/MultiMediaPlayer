
package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import java.util.ArrayList;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TextplayControlView;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonStorage;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
//import com.mediatek.mmpcm.text.ITextEventListener;
//import com.mediatek.mmpcm.textimpl.TextConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.PlayList;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.TextReader.LoadListener;

import com.mediatek.wwtv.mediaplayer.mmp.commonview.TextReader;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ControlView.ControlPlayState;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;
import android.os.SystemProperties;
import com.mediatek.wwtv.mediaplayer.mmp.util.TextUtils;

public class TextPlayActivity extends MediaPlayActivity {

  private static final String TAG = "TextPlayActivity";

  private static final int MESSAGE_PLAY = 0;

  private static final int MESSAGE_PRE = 1;

  private static final int MESSAGE_POPHIDE = 2;

  private static final int MESSAGE_ONMEASURE = 3;

  private static final int MESSAGE_PLAY_NEXT = 4;

  private static final int MESSAGE_SKIP_TO_PAGE = 5;

  private static final int MESSAGE_DISMISS = 6;

  private static final int MESSAGE_PLAY_STATE_SHOW = 7;

  private static final int MESSAGE_POPSHOWDEL = 10000;
  private static final int MESSAGE_REFRESH_PAGE = 10096;

  private static final int DELAY_TIME = 6000;

  private static final int DELAY_REQUEST_TOTALPAGE = 500;

  private static final int DELAY_REQUEST_SKIPTOPAGE = 6000;

  private static final int MAX_SIZE = 10;

  private FrameLayout vLayout; 

  private TextReader vTextView;
  private PlayList mPlayList;

  private int mTextSource = 0;

  /* true play, false pause */
  private boolean mPlayStauts = true;
  private boolean isActivityLiving = true;
  // added by keke 1.5
  private boolean mSKIPPlayStauts = false;

  private TextUtils mTextUtils;

  private final ControlPlayState mControlImp = new ControlPlayState() {

    @Override
    public void play() {
      mPlayStauts = true;
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, DELAY_TIME);
    }

    @Override
    public void pause() {
      mPlayStauts = false;
      mHandler.removeMessages(MESSAGE_PLAY);
    }
  };

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      switch (msg.what) {
		case MESSAGE_REFRESH_PAGE:
    if (null!=mControlView) {
      mControlView.setPhotoTimeType(getPageNum());
    }
          mHandler.removeMessages(MESSAGE_REFRESH_PAGE);
          mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_PAGE,1000);
          break;
		case MESSAGE_POPHIDE:
          hideController();
          break;
        case MESSAGE_PLAY:
          playPage(true);
          break;
        case MESSAGE_PRE:
          playPage(false);
          break;
        case MESSAGE_PLAY_NEXT:
          playSpFile(mPlayList.getNext(Const.FILTER_TEXT, Const.MANUALNEXT));
          break;
        case MESSAGE_DISMISS:
          dismissNotSupprot();
          break;
        case MESSAGE_ONMEASURE:
          if (null != mControlView) {
            if (isNotSupport) {
              mControlView.setPhotoTimeType("");
            } else {
              mControlView.setPhotoTimeType(getPageNum());
            }
          }
          break;
        case MESSAGE_SKIP_TO_PAGE:
          skipToPage();
          break;
        case MESSAGE_PLAY_STATE_SHOW:
          if (null != mControlView) {
              mControlView.setPlayIcon();
          }
          break;
        default:
          break;
      }
    }

  };

  private void playPage(boolean next) {
    if (isNotSupport && !mHandler.hasMessages(MESSAGE_PLAY_NEXT)) {
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_NEXT, DELAY_TIME);
      return;
    }
    if (next) {
      vTextView.pageDown();
    } else {
      vTextView.pageUp();
    }

    if (null != mControlView) {
      mControlView.setPhotoTimeType(getPageNum());
    }
    if (mPlayStauts) {
     // mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, DELAY_TIME);
    }
  }

  private void playSpFile(String path) {
    if (isActivityLiving) {
      dismissNotSupprot();
    }
    if (mHandler.hasMessages(MESSAGE_PLAY)) {
      mHandler.removeMessages(MESSAGE_PLAY);
    }

    try {
      if (menuDialog != null) {
        menuDialog.dismiss();
      }
      if (menuDialogFontList != null) {
        menuDialogFontList.dismiss();

      }

    } catch (Exception ex) {

      ex.printStackTrace();
    }

    vTextView.setPath(path);
    vTextView.play(true);
    reSetController();
    setControlView();

  }

  /**
   * Set control bar info
   */
  public void setControlView() {
    if (mControlView != null) {
      mControlView.setRepeat(Const.FILTER_TEXT);
      mControlView.setPhotoTimeType(getPageNum());
      mControlView.setFileName(mLogicManager
          .getCurrentFileName(Const.FILTER_TEXT));
      mControlView.setFilePosition(mLogicManager.getTextPageSize());
    }
    if (null != mInfo && mInfo.isShowing())
    {
      mInfo.setTextView();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String dataStr = getIntent().getDataString();
    if ((0 != SystemProperties.getInt(AUTO_TEST_PROPERTY, 0) && dataStr != null)
        && !MediaMainActivity.mIsDlnaAutoTest && !MediaMainActivity.mIsSambaAutoTest) {
      autoTest(Const.FILTER_TEXT, MultiFilesManager.CONTENT_TEXT);
    }
    //begin by yx for talkback
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(TALKBACK_BRDCAST_ACTION);
      registerReceiver(mTalkBackReceiver,intentFilter);
    //end by yx for talkback
    setContentView(R.layout.mmp_textplay);
    findView();
    getIntentData();
    init();
    // add by keke for fix DTV00381264
    mControlView.setRepeatVisibility(Const.FILTER_TEXT);
    showPopUpWindow(vLayout);
    MtkLog.d(TAG, "onCreate~~");
    setRepeatMode();
    vTextView.play(true);
  }

  @Override
  protected void onResume() {
    super.onResume();

//    vTextView.play(true);
    MtkLog.d(TAG, "onResume~~");
	mHandler.removeMessages(MESSAGE_POPHIDE);
	mHandler.sendEmptyMessageDelayed(MESSAGE_POPHIDE, 10000);
  }

  private final LoadListener txtLoadListener = new LoadListener() {

    @Override
    public void onLoadDone() {
      MtkLog.d(TAG, "txtLoadListener onLoadDone");
      if (mPlayStauts) {
        if (mHandler.hasMessages(MESSAGE_PLAY)) {
          mHandler.removeMessages(MESSAGE_PLAY);
        }
       // mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, DELAY_TIME);
      }
	  if (mHandler.hasMessages(MESSAGE_REFRESH_PAGE)) {
          mHandler.removeMessages(MESSAGE_REFRESH_PAGE);
        }
	  
      mHandler.sendEmptyMessage(MESSAGE_ONMEASURE);

    }

    @Override
    public void fileNotSupport() {
      MtkLog.d(TAG, "txtLoadListener fileNotSupport");
      isNotSupport = true;
      mHandler.sendEmptyMessage(MESSAGE_ONMEASURE);
      featureNotWork(TextPlayActivity.this.getResources().getString(
          R.string.mmp_file_notsupport));
      mHandler.sendEmptyMessage(MESSAGE_PLAY_STATE_SHOW);
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY_NEXT, DELAY_TIME);

    }

    @Override
    public void onComplete() {
      MtkLog.d(TAG, "txtLoadListener onComplete");
      if (mPlayStauts) {
        mHandler.removeMessages(MESSAGE_PLAY);
        mHandler.sendEmptyMessage(MESSAGE_PLAY_NEXT);
      }

    }

    @Override
    public void onExit() {
      MtkLog.d(TAG, "txtLoadListener onExit~");
      finish();
    }

    @Override
    public void onPrepare() {
      MtkLog.d(TAG, "txtLoadListener onPrepare");
      isNotSupport = false;
      mHandler.sendEmptyMessage(MESSAGE_ONMEASURE);
    }

  };

  private void init() {
    mTextUtils = TextUtils.getInstance(this);
    mLogicManager = LogicManager.getInstance(this);
    mPlayList = PlayList.getPlayList();
    vTextView.setPlayMode(mTextSource);
    vTextView.setPath(mPlayList.getCurrentPath(Const.FILTER_TEXT));

    vTextView.init(mTextUtils.getFontColor(), mTextUtils.getFontSize(), mTextUtils.getFontStyle());

    setControlView();
	
	mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH_PAGE,1000);
  }

  private void getIntentData() {

    mTextSource = MultiFilesManager.getInstance(this)
        .getCurrentSourceType();

    switch (mTextSource) {
      case MultiFilesManager.SOURCE_LOCAL:
        mTextSource = TextUtils.PLAYER_MODE_LOCAL;
        break;
      case MultiFilesManager.SOURCE_SMB:
        mTextSource = TextUtils.PLAYER_MODE_SAMBA;
        break;
      case MultiFilesManager.SOURCE_DLNA:
        mTextSource = TextUtils.PLAYER_MODE_DLNA;
        break;
      default:
        break;
    }
  }

  private void findView() {
    vLayout = (FrameLayout) findViewById(R.id.mmp_text);
    vTextView = (TextReader) findViewById(R.id.mmp_text_show);
    vTextView.setLoadListener(txtLoadListener);
    getTextplayPopView(R.layout.mmp_popuptext, MultiMediaConstant.TEXT, mControlImp);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    MtkLog.d(TAG, "onKeyDown:" + keyCode);
	mControlView.setPhotoTimeType(getPageNum());
    switch (keyCode) {
    // added by keke 1.5 for cr DTV00388058
      case KeyMap.KEYCODE_DPAD_CENTER:
        if (mHandler.hasMessages(MESSAGE_SKIP_TO_PAGE)) {
          mHandler.removeMessages(MESSAGE_SKIP_TO_PAGE);
          skipToPage();
          return true;
        }
        if (isNotSupport) {
          return true;
        }
        break;
      // end
      case KeyMap.KEYCODE_DPAD_UP:
        if (!isValid()) {
          return true;
        }

        reSetController();
        mHandler.removeMessages(MESSAGE_PLAY_NEXT);
        mHandler.sendEmptyMessage(MESSAGE_PRE);
        playSpFile(mPlayList.getNext(Const.FILTER_TEXT, Const.MANUALPRE));
        return true;
      case KeyMap.KEYCODE_DPAD_DOWN:
        if (!isValid()) {
          return true;
        }
        reSetController();
        if (!vTextView.isEnd()) {
        mHandler.removeMessages(MESSAGE_PLAY_NEXT);
        playSpFile(mPlayList.getNext(Const.FILTER_TEXT, Const.MANUALNEXT));
        }
        return true;
      case KeyMap.KEYCODE_DPAD_LEFT:
        if (!isValid()) {
          return true;
        }
        reSetController();
        mHandler.removeMessages(MESSAGE_PLAY);
        mHandler.sendEmptyMessage(MESSAGE_PRE);

        return true;
      case KeyMap.KEYCODE_DPAD_RIGHT:
        if (!isValid()) {
          return true;
        }
        reSetController();
        if (!vTextView.isEnd()) {
          mHandler.removeMessages(MESSAGE_PLAY);
          mHandler.sendEmptyMessage(MESSAGE_PLAY);
        }
        return true;
      case KeyMap.KEYCODE_MTKIR_PREVIOUS:
      case KeyMap.KEYCODE_MTKIR_CHUP:
        if (!isValid()) {
          return true;
        }
        mHandler.removeMessages(MESSAGE_PLAY_NEXT);
        playSpFile(mPlayList.getNext(Const.FILTER_TEXT, Const.MANUALPRE));

        return true;
      case KeyMap.KEYCODE_MTKIR_NEXT:
      case KeyMap.KEYCODE_MTKIR_CHDN:
        if (!isValid()) {
          return true;
        }
        mHandler.removeMessages(MESSAGE_PLAY_NEXT);
        playSpFile(mPlayList.getNext(Const.FILTER_TEXT, Const.MANUALNEXT));
        return true;
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
      case KeyMap.KEYCODE_0:
      case KeyMap.KEYCODE_1:
      case KeyMap.KEYCODE_2:
      case KeyMap.KEYCODE_3:
      case KeyMap.KEYCODE_4:
      case KeyMap.KEYCODE_5:
      case KeyMap.KEYCODE_6:
      case KeyMap.KEYCODE_7:
      case KeyMap.KEYCODE_8:
      case KeyMap.KEYCODE_9:
        reSetController();
        parseSkipToPage(keyCode);
        break;
	  case KeyMap.KEYCODE_MTKIR_STOP:
      case KeyMap.KEYCODE_BACK: {
        finish();
        break;
      }
      default:
        break;
    }

    return super.onKeyDown(keyCode, event);
  }

  private void removeMessage() {
    mHandler.removeMessages(MESSAGE_PLAY);
    mHandler.removeMessages(MESSAGE_ONMEASURE);
    mHandler.removeMessages(MESSAGE_PLAY_NEXT);
    mHandler.removeMessages(MESSAGE_DISMISS);
    dismissNotSupprot();
  }

  private int mSkipPage = 0;
  private final ArrayList<Integer> mPageNum = new ArrayList<Integer>();

  protected void parseSkipToPage(int keyCode) {
    int pageNum;
    if (keyCode < KeyMap.KEYCODE_0 || keyCode > KeyMap.KEYCODE_9) {
      return;
    }
    pageNum = keyCode - KeyMap.KEYCODE_0;

    mPageNum.add(new Integer(pageNum));
    if (setSkipToPage(getSkipPage())) {
      mHandler.removeMessages(MESSAGE_SKIP_TO_PAGE);
      mHandler.sendEmptyMessageDelayed(MESSAGE_SKIP_TO_PAGE,
          DELAY_REQUEST_SKIPTOPAGE);

      // added by keke 1.05 for cr DTV00388058 and DTV00387862
      if (mHandler.hasMessages(MESSAGE_PLAY)) {
        mHandler.removeMessages(MESSAGE_PLAY);
        mSKIPPlayStauts = true;
      }
      // end
    }
  }

  /**
   * Get to skip to page.
   * @return
   */
  protected int getSkipPage() {
    int pageNum = 0;
    for (Integer p : mPageNum) {
      pageNum *= 10;
      pageNum += p.intValue();
    }
    if (pageNum > Integer.MAX_VALUE) {
      pageNum = Integer.MAX_VALUE;
    }
    if (pageNum <= vTextView.getTotalPage()) {
      mSkipPage = pageNum;
    }
    MtkLog.d(TAG, "getSkipPage mSkipPage =" + mSkipPage);
    return mSkipPage;
  }

  protected void skipToPage() {
    MtkLog.d(TAG, "getSkipPage mSkipPage =" + mSkipPage);
    int pageNum = mSkipPage;
    if (pageNum <= 0) {
      return;
    }
    mPageNum.clear();
    reSetController();
    vTextView.skipToPage(pageNum);
    setPageSize();
    // added by keke 1.5 for fix cr DTV00388058 and DTV00387862
    if (mSKIPPlayStauts) {
      mHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, DELAY_TIME);
      mSKIPPlayStauts = false;
    }
    // end
  }

  /**
   * May be skip to page.
   * @param num
   * @return true to success, flase to failed
   */
  protected boolean setSkipToPage(int num) {
    if (mControlView != null) {
      String result;
      int currentPos = num;
      int count = vTextView.getTotalPage();
      if (currentPos > 0 && count > 0 && num <= count) {
        result = currentPos + "/" + count;
        mControlView.setPhotoTimeType(result);
        return true;
      }
    }

    return false;
  }

  private void setPageSize() {
    if (mControlView != null) {
      mControlView.setPhotoTimeType(getPageNum());
    }
  }

  private String getPageNum() {

    int currentPos = vTextView.getCurPagenum();
    int count = vTextView.getTotalPage();

    String result = "";
    if (currentPos > 0 && count > 0) {
      result = currentPos + "/" + count;
    }

    return result;
  }

  @Override
  protected void onDestroy() {

    isActivityLiving = false;
    vTextView.release();
    removeMessage();
    unregisterReceiver(mTalkBackReceiver);//add by yx for talkback
    super.onDestroy();
  }

  @Override
  protected void setFontStyle(int style) {
    mTextUtils.saveFontStyle(style);
    vTextView.setFontStyle(style);

  }

  @Override
  protected void setFontColor(int color) {
    mTextUtils.saveFontColor(color);
    vTextView.setFontColor(color);

  }

  @Override
  protected void setFontSize(float size) {
    mTextUtils.saveFontSize(size);
    vTextView.setFontSize(size);

  }
  // start02 fix by tjs for  change ui
  private void getTextplayPopView(int resource, int mediatype,
                            ControlPlayState controlImp) {
    super.setMediaType(mediatype);
    contentView = LayoutInflater.from(TextPlayActivity.this).inflate(
            resource, null);
    mDisPlayWidth = ScreenConstant.SCREEN_WIDTH;
    mDisPlayHeight = ScreenConstant.SCREEN_HEIGHT;
    mControlView = new TextplayControlView(this, mediatype, controlImp,
            contentView, mDisPlayWidth, mDisPlayHeight);
  }
  //end02

   //begin by yx for talkback
  @Override
  protected void onStop() {
    
    super.onStop();

  };

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
          if (!(menuDialogFontList != null && menuDialogFontList.isShowing() //add by yx for fix talkback issue
            || menuDialog != null && menuDialog.isShowing() || mInfo!=null &&  mInfo.isShowing())){
            TextPlayActivity.this.dispatchKeyEvent(new KeyEvent(action,keycode));
          }
         }
          }
    };

  //end by yx for talkback
}
