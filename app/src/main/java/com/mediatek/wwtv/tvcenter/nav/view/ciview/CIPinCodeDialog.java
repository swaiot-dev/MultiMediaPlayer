package com.mediatek.wwtv.tvcenter.nav.view.ciview;

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
//import com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity;
import com.mediatek.wwtv.tvcenter.nav.view.ciview.PinDialogFragment.ResultListener;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
//import com.mediatek.wwtv.tvcenter.util.ScreenConstant;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;

public class CIPinCodeDialog extends Dialog {

	private static final String TAG = "CIPinCodeDialog";
	private Context mContext;
	private TextView mTitle;
	private PinDialogFragment pinDialogFragment;
	private CIStateChangedCallBack mCIState;
	private String realPwd = "";
	private String showPwd;
	private final int PIN_CODE_LEN = 4;
	private static CIPinCodeDialog mDialog;
	private LinearLayout dialogLayout;
	private boolean isKeyShowDialog = false;

	private CIPinCodeDialog(Context context) {
		super(context, R.style.Theme_TurnkeyCommDialog);
		mContext = context;
		mDialog = this;
	}

	public static CIPinCodeDialog getInstance(Context context) {
		if (mDialog == null) {
			mDialog = new CIPinCodeDialog(context);
		}
		return mDialog;
	}

	public void setCIStateChangedCallBack(CIStateChangedCallBack state) {
		mCIState = state;
		mCIState.setPinCodeDialog(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MtkLog.d(TAG, "onCreate");
		setContentView(getLayoutInflater().inflate(
				R.layout.menu_ci_pin_code_dialog, null));
		setWindowPosition();
		dialogLayout = (LinearLayout) findViewById(R.id.pin_code_dialog);
		mTitle = (TextView) findViewById(R.id.ci_input_pin_code_title);
		pinDialogFragment = (PinDialogFragment) (((MediaPlayActivity)mContext)
				.getFragmentManager()
				.findFragmentById(R.id.ci_input_pin_code_num));
		pinDialogFragment.setResultListener(new ResultListener() {

			@Override
			public void done(String pinCode) {
				MtkTvCI ci = mCIState.getCIHandle();
				if (ci != null) {
					int ret = ci.setCamPinCode(pinCode);
					dismiss();
				}
			}
		});
		mTitle.setText(R.string.menu_setup_ci_pin_code_input_tip);
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
