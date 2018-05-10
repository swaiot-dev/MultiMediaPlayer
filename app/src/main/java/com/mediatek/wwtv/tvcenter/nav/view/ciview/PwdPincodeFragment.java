package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;


public class PwdPincodeFragment extends Fragment{
	private static final String TAG = "MMPCI_PwdPincodeFragment";
	private static boolean DEBUG = true;

	private Context context;
	TextView pwdValue1 ;
	TextView pwdValue2 ;
	TextView pwdValue3 ;
	TextView pwdValue4 ;
	String realPwd="";
	final String PWD_CHAR ="*";
	final int MAXINPUTS = 4;
	int currentFocusViewIndex = 0;
	TextView[] pwdInputViews = new TextView[4];

	public interface ResultListener {
		void done(String pinCode);
	}
	//when press back key
	public interface CancelBackListener {
		void cancel();
	}

	private ResultListener mListener;
	private CancelBackListener mCancelListener;

	public void requestFirstShowFcous(){
		pwdInputViews[0].requestFocus();
	}

	private final Handler mHandler = new Handler();

	public PwdPincodeFragment() {
		super();
	}

	public void setResultListener(ResultListener listener) {
		this.mListener = listener;
	}

	public void setCancelBackListener(CancelBackListener listener) {
		this.mCancelListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		MtkLog.d(TAG, "onCreateView");
		final View v = inflater.inflate(R.layout.ci_pwd_pincode_fragment,
				container, false);
		initViews(v);
		return v;
	}

	private void initViews(View v){
		pwdValue1 = (TextView)v.findViewById(R.id.first);
		pwdValue2 = (TextView)v.findViewById(R.id.second);
		pwdValue3 = (TextView)v.findViewById(R.id.third);
		pwdValue4 = (TextView)v.findViewById(R.id.fourth);
		pwdInputViews[0] = pwdValue1;
		pwdInputViews[1] = pwdValue2;
		pwdInputViews[2] = pwdValue3;
		pwdInputViews[3] = pwdValue4;
		pwdValue1.setOnKeyListener(pwdKeyListener);
		pwdValue2.setOnKeyListener(pwdKeyListener);
		pwdValue3.setOnKeyListener(pwdKeyListener);
		pwdValue4.setOnKeyListener(pwdKeyListener);

	}

	View.OnKeyListener pwdKeyListener = new View.OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN){
				switch(keyCode){
				case KeyEvent.KEYCODE_0:
	        	case KeyEvent.KEYCODE_1:
	        	case KeyEvent.KEYCODE_2:
	        	case KeyEvent.KEYCODE_3:
	        	case KeyEvent.KEYCODE_4:
	        	case KeyEvent.KEYCODE_5:
	        	case KeyEvent.KEYCODE_6:
	        	case KeyEvent.KEYCODE_7:
	        	case KeyEvent.KEYCODE_8:
	        	case KeyEvent.KEYCODE_9:
	        		if(currentFocusViewIndex < MAXINPUTS){
	        			pwdInputViews[currentFocusViewIndex].setText(PWD_CHAR);
		        		currentFocusViewIndex ++;
	        			realPwd +=""+(keyCode-7);
	        			if(currentFocusViewIndex < MAXINPUTS){
	        				pwdInputViews[currentFocusViewIndex].requestFocus();
	        			}
	        		}
	        		MtkLog.d(TAG, "realPwd=="+realPwd);
	        		return true;
	        	case KeyEvent.KEYCODE_DPAD_CENTER:
	            case KeyEvent.KEYCODE_ENTER:
	            	if(currentFocusViewIndex == MAXINPUTS){//now is select the last one
	            		done(realPwd);
	            	}
	            	return true;
	            case KeyEvent.KEYCODE_BACK:
	            	if(cancelback())return true;
	            	break;
	            case KeyEvent.KEYCODE_DPAD_UP:
	            case KeyEvent.KEYCODE_DPAD_DOWN:
	            case KeyEvent.KEYCODE_DPAD_LEFT:
	            case KeyEvent.KEYCODE_DPAD_RIGHT:
	            case KeyEvent.KEYCODE_CHANNEL_UP:
	            case KeyEvent.KEYCODE_CHANNEL_DOWN:
	            	return true;
	            default:
	            	break;
				}
				return false;
			}else{
				return false;
			}
		}

	};

	private void done(String pin) {
		if (mListener != null) {
			mListener.done(pin);
		}
		resetPinInput();
	}

	private boolean cancelback() {
		resetPinInput();
		if (mCancelListener != null) {
			mCancelListener.cancel();
			return true;
		}else{
			return false;
		}
	}

	private void resetPinInput(){
		realPwd ="";
		currentFocusViewIndex = 0;
		for(TextView tv:pwdInputViews){
			tv.setText("");
		}
	}
}
