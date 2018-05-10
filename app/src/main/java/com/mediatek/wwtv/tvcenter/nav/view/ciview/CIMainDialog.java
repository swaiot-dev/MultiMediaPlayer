
package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.InputFilter;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.NumberKeyListener;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.view.View;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.mediatek.wwtv.mediaplayer.R;
//import com.mediatek.wwtv.tvcenter.menu.commonview.TurnkeyCommDialog;
import com.mediatek.wwtv.tvcenter.menu.util.TVContent;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIStateChangedCallBack.CIMenuUpdateListener;
//import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.CancelBackListener;
//import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.ResultListener;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PwdPincodeFragment.CancelBackListener;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PwdPincodeFragment.ResultListener;
import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIEnqBase;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIMenuBase;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.MtkTvCIBase;
import android.app.Dialog;
import android.content.Context;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;



public class CIMainDialog extends Dialog {

  private static final String TAG = "MMPCI_CIMainDialog";
  private CIMainDialog mCiMainDialog;
  private LayoutInflater inflater;
  private LinearLayout ciDialog, mCiCamMenuLayout, mCiNoCardLayout, mCiMenuLayout, mCiEnqLayout;
  private CIViewType mCIViewType = CIViewType.CI_DATA_TYPE_CAM_MENU;
  private CIStateChangedCallBack mCIState = null;
  private TextView mCiCamMenu, mCiPinCode, mCiCamScan;
  private TextView mCiNoCard;
  private TextView mCiMenuTitle, mCiMenuName, mCiMenuSubtitle, mCiMenuBottom;
  private ListView mCiMenuList;
  private TextView mCiEnqTitle, mCiEnqName, mCiEnqSubtitle;
  private PwdPincodeFragment mCiEnqInput;
  // for password
  private String mEditStr = "";// real string
  private int mCurrentIndex = 0; // index of string
  private char[] tempChar;// temp char
  private char num;// input num
  private byte length;// editText length
  private String mPreEditStr = ""; // previous string
  private boolean mFirstShow = false;// the first enter the password eidttext
  private boolean mInputCharChange = true;// whether up or down key
  private boolean bNeedShowCamScan = false;// whether should show cam scam
  static boolean tryToCamScan = false;
  private int mEnqType = 1;// whether password or display num
  private CIListAdapter mCiAdapter;
  private String[] mCIGroup;
  private boolean isMmiItemBack = false;
  private static boolean needShowInfoDialog = true;
  private int mmiMenuLevel = 0;
  private Map<Integer, Integer> levelSelIdxMap;
  private CIStateChangedCallBack.CIMenuUpdateListener ciMenuUpdateListener;
  //private TurnkeyCommDialog camScanCofirm;
  private Context mContext;

  public CIMainDialog(Context context) {
    this(context, R.style.nav_dialog);
  }

  public CIMainDialog(Context context, int theme) {
    super(context, theme);
//    componentID = NAV_COMP_ID_CI_DIALOG;
    levelSelIdxMap = new HashMap<Integer, Integer>();
    mCiMainDialog = this;
    mContext = context;
    inflater = LayoutInflater.from(context);
    initDialog(context);
  }

  public enum CIViewType {
    CI_DATA_TYPE_CAM_MENU, CI_DATA_TYPE_NO_CARD, CI_DATA_TYPE_MENU, CI_DATA_TYPE_ENQ
  }

  public enum CIPinCapsType {
    CI_PIN_CAPS_NONE, CI_PIN_CAPS_CAS_ONLY, CI_PIN_CAPS_CAS_AND_FTA, CI_PIN_CAPS_CAS_ONLY_CACHED, CI_PIN_CAPS_CAS_AND_FTA_CACHED
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MtkLog.d(TAG, "onCreate");
//    showChildView(mCIViewType);
  }

  @Override
  public void show() {
    super.show();
    showChildView(mCIViewType);
  }

  public boolean isCoExist(int componentID) {
    return false;
  }

  public static void resetTryCamScan() {
    MtkLog.d("NavCI", "resetTryCamScan() tryToCamScan is:" + tryToCamScan);
    if (tryToCamScan) {
      tryToCamScan = false;
    }

  }
  public static void setNeedShowInfoDialog(boolean isSet){
    needShowInfoDialog = isSet;
  }

  private void setWindowPosition() {
    WindowManager m = getWindow().getWindowManager();
    Display display = m.getDefaultDisplay();
    Window window = getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();
    int menuWidth = (int) (display.getWidth() * 0.52);
    int menuHeight = (int) (display.getHeight() * 0.56);
    lp.width = menuWidth;
    lp.height = menuHeight;
    lp.gravity = Gravity.CENTER;
    window.setAttributes(lp);
  }

  public void setNeedShowCamScan(boolean bNeed) {
    MtkLog.d(TAG, "setNeedShowCamScan:" + bNeed);
    bNeedShowCamScan = bNeed;
  }

  public void showChildView(CIViewType viewType) {
    MtkLog.v(TAG, "showChildView, viewType=" + viewType);
    mCIViewType = viewType;
    LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    switch (viewType) {
      case CI_DATA_TYPE_CAM_MENU:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiCamMenuLayout, layoutParams);
        mCiCamMenu = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_menu);
        mCiCamMenu.setOnKeyListener(mCardNameListener);
        int menuId = MtkTvCIBase.getCamPinCaps();
        MtkLog.d(TAG, "showChildView,menuID=" + menuId);
        if (menuId == CIPinCapsType.CI_PIN_CAPS_CAS_ONLY_CACHED.ordinal()
            || menuId == CIPinCapsType.CI_PIN_CAPS_CAS_AND_FTA_CACHED.ordinal()) {
          mCiPinCode = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_pin_code);
          mCiPinCode.setVisibility(View.VISIBLE);
          mCiPinCode.setOnKeyListener(mCardNameListener);
        }
        if (bNeedShowCamScan) {
          mCiCamScan = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_scan);
          mCiCamScan.setVisibility(View.VISIBLE);
          mCiCamScan.setOnKeyListener(mCardNameListener);
        }else if (TVContent.getInstance(mContext).isConfigVisible("g_misc__cam_profile_scan")) {
        	//NEED HANDLE
          MtkLog.d(TAG, "visible g_misc__cam_profile_scan");
          mCiCamScan = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_scan);
          mCiCamScan.setVisibility(View.VISIBLE);
          mCiCamScan.setOnKeyListener(mCardNameListener);
        }else {
          mCiCamScan = (TextView) mCiCamMenuLayout.findViewById(R.id.menu_ci_cam_scan);
          mCiCamScan.setVisibility(View.GONE);
        }
        break;
      case CI_DATA_TYPE_NO_CARD:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiNoCardLayout, layoutParams);
        mCiNoCard = (TextView) mCiNoCardLayout.findViewById(R.id.menu_ci_no_card);
        mCiNoCard.requestFocus();
        mCiNoCard.setOnKeyListener(mCardNameListener);
        break;
      case CI_DATA_TYPE_MENU:
        ciDialog.removeAllViews();
        ciDialog.addView(mCiMenuLayout, layoutParams);
        mCiMenuTitle = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_title);
        mCiMenuName = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_name);
        mCiMenuSubtitle = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_subtitle);
        mCiMenuBottom = (TextView) mCiMenuLayout.findViewById(R.id.menu_ci_main_bottom);
        mCiMenuList = (ListView) mCiMenuLayout.findViewById(R.id.menu_ci_main_list);
        mCiMenuList.setOnKeyListener(mMenuListKeyListener);
        break;
      case CI_DATA_TYPE_ENQ:
        MtkLog.d(TAG, "CI_DATA_TYPE_ENQ");
        MtkLog.d(TAG, "ciDialog:"+ciDialog);
        ciDialog.removeAllViews();
        ciDialog.addView(mCiEnqLayout, layoutParams);
        mCiEnqTitle = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_title);
        mCiEnqName = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_name);
        mCiEnqSubtitle = (TextView) mCiEnqLayout.findViewById(R.id.menu_ci_enq_subtitle);
        //NEED HANDLE
        mCiEnqInput = (PwdPincodeFragment) (((MediaPlayActivity) mContext).getFragmentManager()
            .findFragmentById(R.id.menu_ci_enq_input));
        mCiEnqInput.setResultListener(new ResultListener() {

          @Override
          public void done(String pinCode) {
            MtkLog.d(TAG, "answerEnquiry:"+pinCode);
            mCIState.answerEnquiry(1, pinCode);
          }
        });
        mCiEnqInput.setCancelBackListener(new CancelBackListener(){

			@Override
			public void cancel() {
				MtkLog.d(TAG, "mCiEnqInput keyback cancel");
		        mCIState.answerEnquiry(0, "");
		        isMmiItemBack = true;
		        mmiMenuLevel--;
			}
        });
        mCiEnqInput.requestFirstShowFcous();
        break;
      default:
        break;
    }
  }

  private void initDialog(Context context) {
    MtkLog.d(TAG, "initDialog");
    mCiCamMenuLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_cam_menu, null);
    mCiNoCardLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_no_card, null);
    mCiMenuLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_main, null);
    mCiEnqLayout = (LinearLayout) inflater.inflate(R.layout.menu_ci_enq, null);
    mCIState = CIStateChangedCallBack.getInstance(context);
	mCIState.setCIMainDilog(this);
    setContentView(R.layout.ci_dialog);
    setWindowPosition();
    ciDialog = (LinearLayout) findViewById(R.id.ci_dialog);
    ciMenuUpdateListener = new CIMenuUpdateListener() {

      @Override
      public void menuEnqClosed() {
        MtkLog.v(TAG, "menuEnqClosed");
        showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
        showNoCardInfo(mCIState.getCIHandle().getCamName());
        mCiNoCard.requestFocus();
      }

      @Override
      public void enqReceived(MtkTvCIMMIEnqBase enquiry) {
        MtkLog.d(TAG, "enqReceived,enquiry:" + enquiry);
        showChildView(CIViewType.CI_DATA_TYPE_ENQ);
        showCiEnqInfo(mCIState.getCIName(), "", enquiry.getText());
        showCiEnqInfo(mCIState.getCIHandle().getCamName(), "", enquiry.getText());
      }

      @Override
      public void menuReceived(MtkTvCIMMIMenuBase menu) {
        MtkLog.v(TAG, "menuReceived, menu=" + menu);

        showChildView(CIViewType.CI_DATA_TYPE_MENU);
        showCiMenuInfo(mCIState.getCIHandle().getCamName(), menu.getTitle(), menu.getSubtitle(),
            menu.getBottom(), menu.getItemList());
      }

      @Override
      public void ciRemoved() {
        MtkLog.d(TAG, "ciRemoved");
        showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
        showNoCardInfo(mContext.getString(R.string.menu_setup_ci_no_card));
        bNeedShowCamScan = false;
      }

      @Override
      public void ciCamScan(final int message) {
        MtkLog.d(TAG, "ciCamScan");
        //before cam scan confirm dialog is showing check CI-INFO dialog is showing
        if(message == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_REQUEST){//delay
        	MtkLog.d(TAG, "ciCamScan schedule delay...");
        	mHandler.postDelayed(new Runnable(){
				@Override
				public void run() {
					if(isShowing()){
	                	MtkLog.d(TAG, "before ciCamScan dismiss ci info dialog");
	                	dismiss();
	                }
	                camScanReqShow(message);
				}

        	}, 8000);
        }else{
        	if(isShowing()){
            	MtkLog.d(TAG, "before ciCamScan dismiss ci info dialog");
            	dismiss();
            }
            camScanReqShow(message);
        }

      }
    };
  }

  public void handleCIMessage(TvCallbackData data) {
   	needShowInfoDialog = true;

    CIStateChangedCallBack.getInstance(mContext).handleCiCallback(mContext, data,
        ciMenuUpdateListener);
    if(data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_CLOSE){
    	MtkLog.d(TAG, "handleCIMessage: close CI,not show dialog");
    	return;
    }
    if(data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_STARTED
    	||data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_CANCELED
    	||data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_REQUEST
    	||data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_ENDED){
    	MtkLog.d(TAG, "handleCIMessage: not deal with the profile search message");
    	needShowInfoDialog = false;
    }
	/*
    if (!isShowing() && needShowInfoDialog) {
    	//send finish livetvsettings app broadcast
    	Intent intent = new Intent("finish_live_tv_settings");
        mContext.sendBroadcast(intent);
    	ComponentsManager.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_CI_DIALOG);
    }
    */
  }

/*
  @Override
  public boolean KeyHandler(int keyCode, KeyEvent event, boolean fromNative) {
    if (mCIState.camUpgradeStatus()) {
      return true;
    }
    return super.KeyHandler(keyCode, event, fromNative);
  }

  @Override
  public boolean KeyHandler(int keyCode, KeyEvent event) {
    return KeyHandler(keyCode, event, false);
  }*/

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    return (mCIState.camUpgradeStatus()) ? true : super.dispatchKeyEvent(event);
  }

  private View.OnKeyListener mCardNameListener = new View.OnKeyListener() {

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      boolean isHandled = true;
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        MtkLog.v(TAG, "mCardNameListener, keyCode=" + keyCode);
        if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
          MtkLog.d(TAG, "mCIViewType:" + mCIViewType);
          if (mCIViewType == CIViewType.CI_DATA_TYPE_CAM_MENU) {
            if (v.getId() == R.id.menu_ci_cam_menu) {
              showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
              showNoCardInfo(CIStateChangedCallBack.getInstance(mContext).getCIName());
            } else if (v.getId() == R.id.menu_ci_cam_scan) {
              MtkLog.d(TAG, "start cam scan true");
              mCIState.getCIHandle().startCamScan(true);
            } else {
              MtkLog.d(TAG, "CIPinCodeDialog show");
              CIPinCodeDialog dialog = CIPinCodeDialog.getInstance(mContext);
              dialog.setCIStateChangedCallBack(mCIState);
              dialog.show();
            }
          } else {// has card
            MtkLog.d(TAG, "isCamActive:" + mCIState.isCamActive());
            if (mCIState.isCamActive() == true) {
              if (mCIState.getCIHandle().getMenuListID() != -1
                  || mCIState.getCIHandle().getEnqID() != -1) {
                mCIState.getCIHandle().setMMICloseDone();
              }
              mCIState.getCIHandle().enterMMI();
            }
          }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
          MtkLog.d(TAG, "key back");
          if (mCIViewType != CIViewType.CI_DATA_TYPE_CAM_MENU && mCiCamMenu != null) {//
            showChildView(CIViewType.CI_DATA_TYPE_CAM_MENU);
            mCiCamMenu.requestFocus();
            return true;
          } else {
            setCurrCIViewType(null);
          }
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
          MtkLog.d(TAG, "do nothing");
        } else if (keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_UP
            || keyCode == KeyMap.KEYCODE_MTKIR_STOP) {
            isHandled = false;
        }
      }
	  if (isHandled == false ){//&& MediaPlayActivity.getInstance() != null) {
        MtkLog.d(TAG, "isHandled == false ");
        dismiss();
        return true;//TurnkeyUiMainActivity.getInstance().KeyHandler(keyCode, event);
      }
      return false;
    }
  };
  // listener for cardListView
  private View.OnKeyListener mMenuListKeyListener = new View.OnKeyListener() {
    public boolean onKey(View v, int keyCode, KeyEvent event) {
      MtkLog.v(TAG, "mMenuListKeyListener, onKey, keyCode=" + keyCode);
      if (mCIState.camUpgradeStatus()) {
        MtkLog.v(TAG, "cam upgrading..., disable key process");
        return true;
      }
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        int position = mCiMenuList.getSelectedItemPosition();
        switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_CENTER:
          case KeyEvent.KEYCODE_ENTER:
            if (position < 0) {
              position = 0;
            }
            mCIState.selectMenuItem(position);
            levelSelIdxMap.put(mmiMenuLevel, mCiMenuList.getSelectedItemPosition());
            MtkLog.d("levelSelIdxMap", "enter pos--idx==" + mCiMenuList.getSelectedItemPosition()
                + ",level==" + mmiMenuLevel);
            mmiMenuLevel++;
            MtkLog.d(TAG, "mmiMenuLevel++:"+mmiMenuLevel);
            break;

          case KeyEvent.KEYCODE_BACK:
            MtkLog.d(TAG, "keycode back");
            isMmiItemBack = true;
            mmiMenuLevel--;
            MtkLog.d(TAG, "mmiMenuLevel--");
            mCIState.cancelCurrMenu();
            return true;
          case KeyEvent.KEYCODE_MENU:
            dismiss();
            return true;
        }
      }
      return false;
    }
  };
  // listener for enq
  private View.OnKeyListener mEnqInputKeyListener = new View.OnKeyListener() {

    public boolean onKey(View v, int keyCode, KeyEvent event) {
      if (event.getAction() == KeyEvent.ACTION_DOWN) {
        MtkLog.v(TAG, "mEnqInputKeyListener,onKey, keyCode=" + keyCode);
        switch (keyCode) {
          case KeyEvent.KEYCODE_DPAD_CENTER:
          case KeyEvent.KEYCODE_ENTER:
            MtkLog.d(TAG, "mEditStr:" + mEditStr);
            mCIState.answerEnquiry(1, mEditStr);
            break;

          case KeyEvent.KEYCODE_BACK:
            MtkLog.d(TAG, "keyback");
            mCIState.answerEnquiry(0, "");
            isMmiItemBack = true;
            mmiMenuLevel--;
            return true;
          default:
            break;
        }
      }
      return false;
    }
  };
  // numkey Listener
  NumberKeyListener numberKeyListener = new NumberKeyListener() {

    protected char[] getAcceptedChars() {
      return new char[] {
        'a'
      };
    }

    public int getInputType() {
      return 0;
    }
  };

  // for no card or card name
  public void showNoCardInfo(String cardName) {
    if (cardName == null) {
      cardName = "";
    }
    MtkLog.v(TAG, "showNoCardInfo, cardName=" + cardName);
    mmiMenuLevel = 0;
    levelSelIdxMap.clear();
    if (cardName == null || cardName.length() == 0) {
      // no card insert
      cardName = mContext.getString(R.string.menu_setup_ci_no_card);
    }
    mCiNoCard.setText(cardName.trim());
  }

  // for data_menu
  public void showCiMenuInfo(String cardTitle, String cardName, String cardSubtitle,
      String cardBottom, String[] cardListData) {
    if (cardTitle == null) {
      cardTitle = "";
    }
    if (cardName == null) {
      cardName = "";
    }
    if (cardSubtitle == null) {
      cardSubtitle = "";
    }
    if (cardBottom == null) {
      cardBottom = "";
    }
    MtkLog.v(TAG, "showCiMenuInfo, cardTitle=" + cardTitle + ",cardName=" + cardName
        + ",cardSubtitle=" + cardSubtitle + ",cardBottom=" + cardBottom);

    mCiMenuTitle.setText(cardTitle.trim());
    mCiMenuName.setText(cardName.trim());
    mCiMenuSubtitle.setText(cardSubtitle.trim());
    mCiMenuBottom.setText(cardBottom.trim());
    List<String> tempItemList = new ArrayList<String>();
    for (String s : cardListData) {
      if (!TextUtils.isEmpty(s)) {
        tempItemList.add(s);
      } else {
        MtkLog.d(TAG, "a empty item so needn't to show");
      }
    }
    mCIGroup = tempItemList.toArray(new String[0]);
    for (int i = 0; i < mCIGroup.length; i++) {
      MtkLog.d(TAG, "mCIGroup[" + i + "]=" + mCIGroup[i]);
    }
    mCiAdapter = new CIListAdapter(mContext);
    mCiAdapter.setCIGroup(mCIGroup);
    mCiMenuList.setAdapter(mCiAdapter);
    mCiAdapter.notifyDataSetChanged();
    mCiMenuList.setFocusable(true);
    mCiMenuList.requestFocus();
    MtkLog.d(TAG, "isMmiItemBack:"+isMmiItemBack+",levelSelIdxMap:"+levelSelIdxMap+",mmiMenuLevel:"+mmiMenuLevel);
    if (isMmiItemBack && levelSelIdxMap != null && levelSelIdxMap.size() != 0) {
      isMmiItemBack = false;
      int key = mmiMenuLevel-1;
      if(key <0) key =0;
      int idx = levelSelIdxMap.get(key);
      MtkLog.d("levelSelIdxMap", "idx==" + idx + ",level==" + (key));
      mCiMenuList.setSelection(levelSelIdxMap.get(key));
    }
  }

  public void camScanReqShow(int message) {
    MtkLog.d("NavCI", "camScanReqShow() message is:" + message);
    // MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE is send many times
    // so check is camScan = true to escape show many dialog
    if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE) {
      if ( tryToCamScan) {
        MtkLog.d("NavCI", "MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE camScanReqShowed needn't show again try:"
            + tryToCamScan);
        return;
      }
    }
    mCIState = CIStateChangedCallBack.getInstance(mContext);
    //camScanCofirm = new TurnkeyCommDialog(CIMainDialog.this.getContext(), 3);
    if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_WARNING) {
      bNeedShowCamScan = true;
    //  camScanCofirm.setMessage(mContext.getString(R.string.cam_scan_warning));
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_URGENT) {
      bNeedShowCamScan = true;
    //  camScanCofirm.setMessage(mContext.getString(R.string.cam_scan_urgent));
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_NOT_INIT) {
      bNeedShowCamScan = true;
   //   camScanCofirm.setMessage(mContext.getString(R.string.cam_scan_not_init));
    } else if (message == MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE) {
      bNeedShowCamScan = true;
   //   camScanCofirm.setMessage(mContext.getString(R.string.cam_scan_schedule));
    }else if(message == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_REQUEST){
    	bNeedShowCamScan = true;
   //     camScanCofirm.setMessage(mContext.getString(R.string.cam_scan_schedule));
    }

    /*camScanCofirm.setButtonYesName(mContext.getString(R.string.menu_dialog_confirm));
    camScanCofirm.setButtonNoName(mContext.getString(R.string.menu_dialog_cancel));
    camScanCofirm.show();
//    camScanCofirm.setPositon(-20, 70);
    camScanCofirm.setOnKeyListener(new DialogInterface.OnKeyListener() {
      public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        int action = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_BACK && action == KeyEvent.ACTION_DOWN) {
          MtkLog.d("NavCI", "camScanReqShow startcamScan false");
          mCIState.getCIHandle().startCamScan(false);
          camScanCofirm.dismiss();
          return true;
        }
        return false;
      }
    });*/

    View.OnKeyListener yesListener = new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            tryToCamScan = true;
            MtkLog.d("NavCI", "camScanReqShow startcamScan true");
            mCIState.getCIHandle().startCamScan(true);
            //camScanCofirm.dismiss();
            return true;
          }
        }
        return false;
      }
    };

    View.OnKeyListener noListener = new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            MtkLog.d("NavCI", "camScanReqShow startcamScan false");
            mCIState.getCIHandle().startCamScan(false);
          //  camScanCofirm.dismiss();
            return true;
          }
        }
        return false;
      }
    };

  //  camScanCofirm.getButtonNo().setOnKeyListener(noListener);
  //  camScanCofirm.getButtonYes().setOnKeyListener(yesListener);
  }

  // for data_enq
  private void showCiEnqInfo(String cardTitle, String cardName, String cardSubtitle) {
    MtkLog.d(TAG, "showCiEnqInfo:title->"+cardTitle+",cardName->"+cardName+",subtitle->"+cardSubtitle);
    if (cardTitle == null) {
      cardTitle = "";
    }
    if (cardName == null) {
      cardName = "";
    }
    if (cardSubtitle == null) {
      cardSubtitle = "";
    }
    MtkLog.v(TAG, "showCiMenuInfo, cardTitle=" + cardTitle + ",cardName=" + cardName
        + ",cardSubtitle=" + cardSubtitle);

    mHandler.removeMessages(1);
    mCiEnqTitle.setText(cardTitle.trim());
    mCiEnqName.setText(cardName.trim());
    mCiEnqSubtitle.setText(cardSubtitle.trim());
    mEditStr = "";
    mCurrentIndex = 0;
    mInputCharChange = false;
    if (mCIState.isBlindAns()) {
      mEnqType = 1;
      mPreEditStr = "_";
    } else {
      mEnqType = 0;
      mPreEditStr = "_";
    }
    mFirstShow = true;
    // reset text
    if ((mCIState.getAnsTextLen() & (byte) 0xf0) == 0) {
      length = mCIState.getAnsTextLen();
    } else {
      length = (byte) 0x0f;
    }
    MtkLog.d(TAG, "---------length------" + length);
  }

  private Handler mHandler = new Handler() {
    public void handleMessage(Message msg) {
      setPassword();
    }
  };

  // handler set password
  private void setPassword() {
    MtkLog.v(TAG, "setPassword,mEnqType:" + mEnqType);
    if (mEnqType == 1) {
      mInputCharChange = false;
      mEditStr = mPreEditStr;
      MtkLog.d(TAG, "mCurrentIndex:" + mCurrentIndex + ",mEditStr:" + mEditStr + ",length:"
          + length);
      if (mCurrentIndex <= mEditStr.length() - 1 && mEditStr.length() < length) {
        mCurrentIndex++;
        if (mCurrentIndex > mEditStr.length() - 1) {
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length - 1; i++) {
            tempChar[i] = '*';
          }
          tempChar[tempChar.length - 1] = '_';
        } else {
          tempChar = mEditStr.toCharArray();
          for (int i = 0; i < mEditStr.length(); i++) {
            tempChar[i] = '*';
          }
        }
      } else if (mCurrentIndex > mEditStr.length() - 1 && mEditStr.length() < length) {
        mCurrentIndex++;
        if (mCurrentIndex <= length - 1) {
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length - 1; i++) {
            tempChar[i] = '*';
          }
          tempChar[tempChar.length - 1] = '_';
        } else {
          mCurrentIndex = length - 1;
          mPreEditStr = mPreEditStr + "_";
          tempChar = mPreEditStr.toCharArray();
          for (int i = 0; i < tempChar.length; i++) {
            tempChar[i] = '*';
          }
        }
      } else {
        mCurrentIndex++;
        if (mCurrentIndex > length - 1) {
          mCurrentIndex = length - 1;
        }
        tempChar = mEditStr.toCharArray();
        for (int i = 0; i < mEditStr.length(); i++) {
          tempChar[i] = '*';
        }
      }
    }
  }

  public CIViewType getCurrCIViewType() {
    MtkLog.d(TAG, "getCurrCIViewType:" + mCIViewType);
    return mCIViewType;
  }

  public void setCurrCIViewType(CIViewType type) {
    MtkLog.d(TAG, "setCurrCIViewType:" + type);
    mCIViewType = type;
  }

  public void handlerMessage(int code){
      MtkLog.d(TAG, " handlerMessage code = " + code);

      if((code == 4 || code == 5 || code == 10 || code == 11)){
          if(this.isShowing()){
              this.dismiss();
          }
      }
  }

	@Override
	public void dismiss() {
		MtkLog.d(TAG, " dismiss cidialog");
		if(mCIState !=null){
			mCIState.setCIClose();
		}
		super.dismiss();
	}


}
