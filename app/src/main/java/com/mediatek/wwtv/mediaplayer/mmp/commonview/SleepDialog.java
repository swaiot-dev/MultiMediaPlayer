package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import java.util.ArrayList;
import java.util.List;

import com.mediatek.wwtv.mediaplayer.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmp.util.DisplayUtil;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvTime;

public class SleepDialog extends Dialog{

	private TextView mSleepTimeShow;
	private Context mContext;

	private String TAG="SLEEPDIALOG";
	private List<String> mArray = new ArrayList<String>();
	private String mSleepOff = "Sleep Off";

	public SleepDialog(Context context, int theme) {
		super(context, theme);
	}

	public SleepDialog(Context context) {
		this(context, R.style.dialog);
		mContext = context;
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.sleep_view);
//		String[] tmp = mContext.getResources().getStringArray(R.array.menu_setup_sleep_timer_array);
//		for(String str:tmp){
//			mArray.add(str);
//		}
		mTime = MtkTvTime.getInstance();
		mSleepTimeShow = (TextView)findViewById(R.id.sleep_tv);
		setLayoutParams();
	}



	private boolean mFromOut = true;
	//API
	private MtkTvTime mTime;
	public void updateValue(boolean isFromOut){
		Log.i(TAG,"mSleepDialog  updateValue");
		mFromOut = isFromOut;
		updateView();
	}


	private void adjustTime(int times) {
		// TODO Auto-generated method stub
		int minutes = times / 60;

		MtkLog.d(TAG, "times=" + times + ", minutes=" + minutes);
		mSleepTimeShow.setText("Sleep:" + (minutes+1) + " Minutes");
	}

	private void updateView() {
		// TODO Auto-generated method stub
		if(mFromOut){
			int times = mTime.getSleepTimerRemainingTime();
			if(times > 0){
				adjustTime(times);
			}else{
				mSleepTimeShow.setText(mSleepOff);
			}
		}else{
			int sleep = mTime.getSleepTimer();
			if(sleep == 0){
				mSleepTimeShow.setText(mSleepOff);
			}
			else{
				mSleepTimeShow.setText("Sleep:" + (sleep / 60) + " Minutes");
			}
		}
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
		Window win = getWindow();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		Context context = getContext();
		win.setGravity(Gravity.BOTTOM | Gravity.LEFT);
		lp.x = 100;
		lp.y = 100;
		lp.width = 300;
		lp.height = 50;

		win.setAttributes(lp);
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		Log.i(TAG,"keyCode :"+keyCode);
        //temp solution
        if(keyCode == KeyEvent.KEYCODE_FUNCTION){
            keyCode = event.getScanCode();
        }
        Log.i(TAG,"keyCode later :"+keyCode);
	 	switch (keyCode) {
    	case KeyMap.KEYCODE_BACK:
    		this.dismiss();
    		return true;
    	case KeyMap.KEYCODE_MTKIR_SLEEP:
    		mFromOut = false;
    		updateView();
    		break;
    	default:
    		break;
    	}

		return super.onKeyDown(keyCode, event);
	}





}
