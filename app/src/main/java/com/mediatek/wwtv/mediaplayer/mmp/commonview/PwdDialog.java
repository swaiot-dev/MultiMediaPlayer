package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import com.mediatek.wwtv.mediaplayer.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmp.multimedia.pwdListener;
import com.mediatek.wwtv.mediaplayer.mmp.util.DisplayUtil;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;

public class PwdDialog extends Dialog{

	protected TextView pwdInput;
	protected TextView pwdError;
	protected View mInfoView;

	private Context mContext;

	private pwdListener mListener;

    private static final String PWD_CHAR = "*";

    private String password ="";
    private String showPasswordStr ="";
	private String channalPwd = "";
	MtkTvPWDDialog mtkTvPwd=null;
	public PwdDialog(Context context, int theme) {
		super(context, theme);
	}

	public PwdDialog(Context context,pwdListener listener) {
		this(context, R.style.dialog);
		mContext = context;
		mListener = listener;
        mtkTvPwd = MtkTvPWDDialog.getInstance();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mmp_pwd_view);
		setLayoutParams();
		pwdInput = (TextView)findViewById(R.id.mmp_pwd_value);
		pwdError = (TextView)findViewById(R.id.mmp_pwd_error);
		mInfoView = findViewById(R.id.mmp_pwd_info);
	}



	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		switch (keyCode) {
		case KeyMap.KEYCODE_VOLUME_UP:
		case KeyMap.KEYCODE_VOLUME_DOWN:
			return true;
		default:
			break;
		}

		return super.dispatchKeyEvent(event);
	}


	private void setLayoutParams() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		Context context = getContext();

		lp.width = DisplayUtil.getWidthPixels(context, 0.35f);
		lp.height = DisplayUtil.getHeightPixels(context, 0.35f);

		getWindow().setAttributes(lp);
	}

    public String getInputString() {
        if (password != null) {
            return password;
        }
        return null;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		mInfoView.setVisibility(View.GONE);
	 	switch (keyCode) {
    	case KeyMap.KEYCODE_DPAD_CENTER:

    			String inputStr = getInputString();
    			if (null == inputStr || inputStr.length() == 0) {
//    				dismiss();
    				return true;
    			} else {
    				checkPassWord(inputStr);
    			}
    		return true;
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
    		if(password.length()<4 && pwdInput.getVisibility() == View.VISIBLE){
    			password=password + (keyCode - 7);
    			showPasswordStr=showPasswordStr+PWD_CHAR;
    			pwdInput.setText(showPasswordStr);
    		}
    		break;
    	case KeyMap.KEYCODE_BACK:
    		android.util.Log.i("TAG","keycode: back"+KeyMap.KEYCODE_BACK);
    		if(password.length() > 0){
    			password=password.substring(0,password.length() -1);
    			showPasswordStr=showPasswordStr.substring(0,showPasswordStr.length() -1);
    			pwdInput.setText(showPasswordStr);

    		}else{
    			mListener.setCancel();
    		}
    		return true;
    	default:
    		break;
    	}

		return super.onKeyDown(keyCode, event);
	}

	int mInputPwdErrorTimes = 0;

	private static final int INPUT_ERROR_TIMES = 3;

	public void checkPassWord(String pwd) {
	    boolean isPass = mtkTvPwd.checkPWD(pwd);
	    if (isPass){
	    	int pin = Integer.parseInt(pwd);
	    	mListener.setConfirm(pin);
	        mInputPwdErrorTimes = 0;
	    } else {
	    	mInputPwdErrorTimes++;
	        password ="";
	        showPasswordStr ="";
	       	pwdInput.setText(showPasswordStr);
	       	mInfoView.setVisibility(View.VISIBLE);
	    	pwdError.setText(mContext.getString(R.string.mmp_wrong_psw,String.valueOf(INPUT_ERROR_TIMES),String.valueOf(mInputPwdErrorTimes)));
	    	if (mInputPwdErrorTimes >= INPUT_ERROR_TIMES) {
	    		mListener.setCancel();
	    	}
	    }
	}

	private void reset(){
		password="";
		showPasswordStr="";
		pwdInput.setText(showPasswordStr);
	}

}
