
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Selection;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.PinDialogFragment.ResultListener;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayPvrActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.pwdListener;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import android.widget.Toast;

public class CIPinCodeDialog extends Dialog {

  private static final String TAG = "CIPinCodeDialog";
  private final Context mContext;
  private TextView mTitle;
  private PinDialogFragment pinDialogFragment;
  // private CIStateChangedCallBack mCIState;
  private final String realPwd = "";
  private String showPwd;
  private final int PIN_CODE_LEN = 4;
  private static CIPinCodeDialog mDialog;
  private LinearLayout dialogLayout;
  private boolean isKeyShowDialog = false;

  private pwdListener mListener;

  public CIPinCodeDialog(Context context) {
    super(context, R.style.Theme_TurnkeyCommDialog);
    mContext = context;
    mDialog = this;
  }

  Toast mToast = null;

  public CIPinCodeDialog(Context context, pwdListener listener) {
    super(context, R.style.Theme_TurnkeyCommDialog);
    mContext = context;
    mDialog = this;
    mListener = listener;
    mToast = Toast.makeText(mContext, R.string.menu_setup_ci_pin_code_incorrect_tip, 1);
    // mToast.setDuration(Toast.LENGTH_SHORT);
  }

  private int resid = R.string.menu_setup_ci_pin_code_input_tip;

  public void setTitleName(int id) {
    resid = id;
    if (mTitle != null) {
      mTitle.setText(id);
    }
  }

  boolean isPinDialog = false;

  public void setType(boolean isPin) {
    isPinDialog = isPin;
    if (isPinDialog) {
      resid = R.string.menu_setup_ci_pin_code;
    } else {
      resid = R.string.menu_setup_ci_pin_code_input_tip;
      if (mtkTvPwd == null) {
        mtkTvPwd = MtkTvPWDDialog.getInstance();
      }
    }
    if (mTitle != null) {
      mTitle.setText(resid);
    }
  }

  public static CIPinCodeDialog getInstance(Context context) {
    if (mDialog == null) {
      mDialog = new CIPinCodeDialog(context);
    }
    return mDialog;
  }

  // public void setCIStateChangedCallBack(CIStateChangedCallBack state) {
  // mCIState = state;
  // mCIState.setPinCodeDialog(this);
  // }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    MtkLog.d(TAG, "onCreate");
    setContentView(getLayoutInflater().inflate(
        R.layout.menu_ci_pin_code_dialog, null));
    setWindowPosition();
    dialogLayout = (LinearLayout) findViewById(R.id.pin_code_dialog);
    mTitle = (TextView) findViewById(R.id.ci_input_pin_code_title);
    if (mContext instanceof VideoPlayActivity) {
      pinDialogFragment = (PinDialogFragment) (((VideoPlayActivity) mContext)
          .getFragmentManager()
          .findFragmentById(R.id.ci_input_pin_code_num));
    } else {
      pinDialogFragment = (PinDialogFragment) (((VideoPlayPvrActivity) mContext)
          .getFragmentManager()
          .findFragmentById(R.id.ci_input_pin_code_num));
    }

    pinDialogFragment.setResultListener(new ResultListener() {

      @Override
      public void done(String pinCode) {

        if (!isPinDialog) {
          int ret = checkPassWord(pinCode);
          MtkLog.i(TAG, "checkPassWord ret:" + ret);
          if (ret == 0) {
            mToast.setText(R.string.ci_menu_pin_correct);
            mToast.show();
            confirmed(pinCode);
          } else if (ret >= 3) {
            mToast.setText(R.string.ci_menu_pin_outoftry);
            mToast.show();
            cancelPlay();
          } else {
            mToast.setText(R.string.menu_setup_ci_pin_code_incorrect_tip);
            mToast.show();
            reDoInput();
          }
        }
        // MtkTvCI ci = mCIState.getCIHandle();
        // if (ci != null) {
        // int ret = ci.setCamPinCode(pinCode);
        // dismiss();
        // }
      }

      @Override
      public void cancel() {
        // TODO Auto-generated method stub
        mListener.setCancel();

      }
    });
    mTitle.setText(resid);
  }

  MtkTvPWDDialog mtkTvPwd = null;

  private static final int INPUT_ERROR_TIMES = 3;
  private static int mInputPwdErrorTimes = 0;

  public int checkPassWord(String pwd) {

    boolean isPass = mtkTvPwd.checkPWD(pwd);
    if (isPass) {
      mInputPwdErrorTimes = 0;
    } else {
      mInputPwdErrorTimes++;
    }
    return mInputPwdErrorTimes;
  }

  public void confirmed(String pwd) {
    // CIPinCodeDialog.this.hide();
    int pin = Integer.parseInt(pwd);
    mListener.setConfirm(pin);
    //DTV00709360
    pinDialogFragment.resetPinInput();
  }

  public void reDoInput() {
    pinDialogFragment.resetPinInput();
  }

  public void cancelPlay() {
    mInputPwdErrorTimes = 0;
    mListener.setCancel();
  }

  @Override
  public void show() {
    MtkLog.d(TAG, "show");
    isKeyShowDialog = true;
    super.show();
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    MtkLog.d(TAG, "dispatchKeyEvent");
    if (isKeyShowDialog) {
      isKeyShowDialog = false;
      return true;
    }
    return super.dispatchKeyEvent(event);
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
}
