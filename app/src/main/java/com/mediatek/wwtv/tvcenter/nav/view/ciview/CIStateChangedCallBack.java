
package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIMenuBase;
import com.mediatek.twoworlds.tv.model.MtkTvCIMMIEnqBase;
import com.mediatek.twoworlds.tv.common.MtkTvCIMsgTypeBase;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;

//import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
//import com.mediatek.wwtv.tvcenter.nav.util.InputSourceManager;
//import com.mediatek.wwtv.tvcenter.nav.util.ComponentsManager;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.CIMainDialog.CIViewType;
//import com.mediatek.wwtv.tvcenter.nav.view.common.NavBasic;
//import com.mediatek.wwtv.tvcenter.util.GetCurrentTask;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.tvcenter.util.TvCallbackData;
import com.mediatek.wwtv.tvcenter.util.TvCallbackHandler;
import com.mediatek.wwtv.tvcenter.util.TvCallbackConst;


public class CIStateChangedCallBack {
  private String TAG = "MMPCI_CIStateChangedCallBack";

  private static CIStateChangedCallBack mCIState = null;
  private static Context mContext;
  boolean regStatus = false;
  // CI Data
  // default slot_id is 0, current support only one slot accroid to ci spec
  private int slot_id = 0;
  private String mCiName = null;
  private MtkTvCI mCi = null;
  private MtkTvCIMMIMenuBase menu = null;
  private MtkTvCIMMIEnqBase enquiry = null;
  // Handler
  private TvCallbackData mData = null;
  // cam upgrade status,
  // 0: not upgrade
  // 1: receive upgrade message
  // 2: press enter and upgrading
  private int camUpgrade = 0;
  // b_is_list_obj
  private boolean bListObj = false;

  private CIPinCodeDialog pincodedialog;
  private CIMainDialog mCIMainDialog;

  // check after pin code input reply type
  public enum CIPinCodeReplyType {
    CI_PIN_BAD_CODE, CI_PIN_CICAM_BUSY, CI_PIN_CODE_CORRECT, CI_PIN_CODE_UNCONFIRMED, CI_PIN_BLANK_NOT_REQUIRED, CI_PIN_CONTENT_SCRAMBLED
  }

  private CIStateChangedCallBack(Context context) {
    mContext = context;
  }

  public void setPinCodeDialog(CIPinCodeDialog dialog) {
    pincodedialog = dialog;
  }

  public void setCIMainDilog(CIMainDialog dialog){
  	mCIMainDialog = dialog;
  }

  public void setCIClose() {
    if (getCIHandle() != null) {
      mCi.setMMIClose();
    }
  }

  public static CIStateChangedCallBack getInstance(Context context) {
    if (null == mCIState) {
      mCIState = new CIStateChangedCallBack(context);
    }
    return mCIState;
  }

  private void camMenuShowRequest(Context megSrc, int msgType) {
//    String className = GetCurrentTask.getInstance(megSrc).getCurRunningClass();
//    MtkLog.e(TAG, className);

    // jump to CI Page
  }

  public void handleCiCallback(Context megSrc, TvCallbackData data, CIMenuUpdateListener listener) {
    MtkLog.d(TAG, "handleCiCallback, " + data.param2);

    try {
      if (data.param1 != slot_id) {
        slot_id = data.param1;
      }
      switch (data.param2) {
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_INSERT:// 0
          if (data.param1 != slot_id) {
            slot_id = data.param1;
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_NAME:// 1
        	//TEMP CODE NEED HANDLE
          if (mCIMainDialog != null) {
            mCIMainDialog.showChildView(CIViewType.CI_DATA_TYPE_NO_CARD);
            mCIMainDialog.showNoCardInfo(CIStateChangedCallBack.getInstance(mContext).getCIName());
            if (!mCIMainDialog.isShowing()) {
              mCIMainDialog.show();//.getInstance().showNavComponent(NavBasic.NAV_COMP_ID_CI_DIALOG);
            }
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_CARD_REMOVE:// 2
          slot_id = 0;
          menu = null;
          enquiry = null;
          MtkLog.d(TAG, "when card remove set upgrade to 0");
          camUpgrade = 0;//when remove set to 0
          if (listener != null) {
            listener.ciRemoved();
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_ENQUIRY:// 3
          enquiry = (MtkTvCIMMIEnqBase) (data.paramObj2);
          CIMainDialog.resetTryCamScan();
          if (listener != null) {
            listener.enqReceived(enquiry);
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_MENU:// 4
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_LIST:// 5
        	 /*
	          CIMainDialog mCiMainDialog = (CIMainDialog) ComponentsManager.getInstance()
	          .getComponentById(NavBasic.NAV_COMP_ID_CI_DIALOG);
	          if (mCiMainDialog == null || !mCiMainDialog.isVisible()) {
	            CIMainDialog.setNeedShowInfoDialog(false);
	            break;
	          }
        	  */
          if (data.param2 == MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_LIST) {
            bListObj = true;
          } else {
            bListObj = false;
          }
          menu = (MtkTvCIMMIMenuBase) (data.paramObj1);
          MtkLog.d(TAG, "scube, menu=" + (MtkTvCIMMIMenuBase) (data.paramObj1));
          MtkLog.d(TAG, "listener:" + listener);
          if (listener != null) {
            listener.menuReceived(menu);
          }
          if (bListObj) {
            // here can send upgrade progress
            if (menu.getTitle() != null && menu.getTitle().contains("Upgrade")
                && menu.getTitle().contains("Test")) {
              MtkLog.d(TAG, "CI upgrade begin to send upgrade progress");
              TvCallbackData tdata = new TvCallbackData();
              tdata.param2 = MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE_PROGRESS;
              handleCiCallback(megSrc, tdata, listener);
            } else {
              MtkLog.d(TAG, "CI menu title ==" + menu.getTitle());
            }
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_MMI_CLOSE:// 6
          if(data.param1 != slot_id){
            MtkLog.d(TAG, "MTKTV_CI_NFY_COND_MMI_CLOSE, " + data.param1 + "," + slot_id);
            CIMainDialog.setNeedShowInfoDialog(false);
            return ;//DTV00612468
          }
          if (getCIHandle() != null) {
            mCi.setMMICloseDone();
            if (listener != null) {
              listener.menuEnqClosed();
            }
            menu = null;
            enquiry = null;
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_WARNING:// 11
        case MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_URGENT:// 12
        case MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_NOT_INIT:// 13
        case MtkTvCIMsgTypeBase.MTKTV_CI_CAM_SCAN_ENQ_SCHEDULE:// 14
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PROFILE_SEARCH_REQUEST:// 18
          listener.ciCamScan(data.param2);
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE:
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE_PROGRESS:
          if (MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE == data.param2) {
            MtkLog.d(TAG, "reday to upgrade");
            camUpgrade = 1;
          } else {
            MtkLog.d(TAG, "upgrade progressing");
            camUpgrade = 2;
          }
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE_COMPLETE:
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_FIRMWARE_UPGRADE_ERROR:
          camUpgrade = 0;
          break;
        case MtkTvCIMsgTypeBase.MTKTV_CI_NFY_COND_PIN_REPLY:// 15
          checkReplyValue(data.param3);
          break;
        default:
          break;
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public boolean camUpgradeStatus() {
    return (camUpgrade == 2);
  }

  /**
   * this method is used to get CI handle
   *
   * @return instance of MtkTvCI
   */
  public MtkTvCI getCIHandle() {
    if (slot_id == -1) {
      MtkLog.d(TAG, "getCIHandle, null");
      mCi = null;
    } else {
      mCi = MtkTvCI.getInstance(slot_id);
    }
    return mCi;
  }

  /**
   * this method is used to get cam name
   *
   * @return
   */
  public String getCIName() {
    if (getCIHandle() != null) {
      mCiName = mCi.getCamName();
    } else {
      mCiName = "";
    }

    MtkLog.d(TAG, "getCIName, name=" + mCiName);

    return mCiName;
  }

  /**
   * this method is used to get MMIMenu
   *
   * @param menu
   */
  public MtkTvCIMMIMenuBase getMtkTvCIMMIMenu() {
    MtkLog.d(TAG, "getMtkTvCIMMIMenuBase, menu=" + menu);

    return this.menu;
  }

  /**
   * this method is used to get MMIEnq
   *
   * @param enquiry
   */
  public MtkTvCIMMIEnqBase getMtkTvCIMMIEnq() {
    MtkLog.d(TAG, "getMtkTvCIMMIEnqBase, enquiry=" + enquiry);

    return this.enquiry;
  }

  /**
   * this method is used to select menu item
   *
   * @param num
   */
  public void selectMenuItem(int num) {
    MtkLog.d(TAG, "selectMenuItem, num=" + num);

    if (null != getCIHandle()) {
      mCi.setMenuAnswer(menu.getMMIId(), bListObj ? 0 : (num + 1));

      if (1 == camUpgrade) {
        camUpgrade = 2;
      }
    }
  }

  /**
   * this method is used to answer enquiry
   *
   * @param bAnswer
   * @param data
   */
  public void answerEnquiry(int bAnswer, String data) {
    if (null != getCIHandle()) {
      mCi.setEnqAnswer(getMtkTvCIMMIEnq().getMMIId(), bAnswer, data);
    }
  }

  /**
   * this method is used to get Ans Len
   *
   * @return
   */
  public byte getAnsTextLen() {
    if (enquiry == null) {
      return -1;
    }

    MtkLog.d(TAG, "getAnsTextLen, enquiry=" + enquiry);

    return enquiry.getAnsTextLen();
  }

  /**
   * this method is used to check blindans
   *
   * @return
   */
  public boolean isBlindAns() {
    if (enquiry == null) {
      return false;
    }

    MtkLog.d(TAG, "isBlindAns, enquiry=" + enquiry);

    return enquiry.getBlindAns();
  }

  /**
   * this method is used to cancel curr Menu
   */
  public int cancelCurrMenu() {
    MtkLog.d(TAG, "cancelCurrMenu, menu=" + menu);

    if (null != getCIHandle()) {
      return mCi.setMenuAnswer(menu.getMMIId(), 0);
    }
    return 0;
  }

  /**
   * this method is used to check whether cam is active or not
   *
   * @return
   */
  public boolean isCamActive() {
    boolean active = false;
    if (null != mCi) {
      active = mCi.getSlotActive();
    }
    return active;
  }

  public TvCallbackData getReqShowData() {
    MtkLog.d(TAG, "getReqShowData");
    return mData;
  }

  public void setReqShowData(TvCallbackData data) {
    MtkLog.d(TAG, "setReqShowData");
    if (mData == null) {
      mData = new TvCallbackData();
    }

    mData = data;
  }

  private void checkReplyValue(int ret) {
    CIPinCodeReplyType type = CIPinCodeReplyType.values()[ret];
    MtkLog.d(TAG, "CIPinCodeReplyType is " + type);
    switch (type) {
      case CI_PIN_CODE_CORRECT:
        if (pincodedialog != null) {
          pincodedialog.dismiss();
        }
		//mmp handled
       //CIMainDialog.setNeedShowInfoDialog(false);
       //start
        mCIMainDialog.hide();
		mCIMainDialog.cancel();
		//end
        Toast.makeText(mContext, "correct!", Toast.LENGTH_LONG).show();

        break;
      case CI_PIN_CODE_UNCONFIRMED:
      case CI_PIN_CONTENT_SCRAMBLED:
      case CI_PIN_CICAM_BUSY:
        MtkLog.d(TAG, "these 3 type do nothing");
        Toast.makeText(mContext, "some invalid type", Toast.LENGTH_LONG).show();
        break;
      case CI_PIN_BAD_CODE:
      case CI_PIN_BLANK_NOT_REQUIRED:
        Toast.makeText(mContext, mContext.getString(R.string.menu_setup_ci_pin_code_incorrect_tip),
            Toast.LENGTH_LONG).show();
        break;
    }
  }

  // Interfaces
  public static interface CIMenuUpdateListener {
    void enqReceived(MtkTvCIMMIEnqBase enquiry);

    void menuReceived(MtkTvCIMMIMenuBase menu);

    void menuEnqClosed();

    void ciRemoved();

    void ciCamScan(int message);

  }
}
