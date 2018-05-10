package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.DisplayUtil;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util.iDrmlistener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.view.View.OnKeyListener;

public class DrmDialog extends Dialog implements OnKeyListener{
	protected Button mOk;
	protected Button mCancel;
	protected TextView mDrmInfo;
	protected TextView mDrmContinueCheck;
	protected View mDrmCancelLayout;
	protected View mDrmContinueLayout;

	private int mLimitCount;
	private int mUsedCount;

	private Context mContext;

	private iDrmlistener mDrmListener;

	private int mIndex;


	public DrmDialog(Context context, int theme) {
		super(context, theme);
	}

	public DrmDialog(Context context) {
		this(context, R.style.dialog);
	}
	public DrmDialog(Context context,int limit ,int usercount,iDrmlistener listener,int index) {
		this(context, R.style.dialog);
		mLimitCount = limit;
		mUsedCount  = usercount;
		mContext = context;
		mDrmListener = listener;
		mIndex = index;
	}


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mmp_drm);

		setLayoutParams();

		mOk = (Button)findViewById(R.id.mmp_drm_btn_ok);
		mOk.setOnKeyListener(this);
		mCancel = (Button)findViewById(R.id.mmp_drm_btn_cancel);
		mCancel.setOnKeyListener(this);
		mDrmInfo = (TextView)findViewById(R.id.mmp_drm_info);
		mDrmCancelLayout = findViewById(R.id.mmp_drm_cancel_layout);
		mDrmContinueLayout = findViewById(R.id.mmp_drm_continue_layout);
		mDrmInfo.setText(mContext.getString(R.string.mmp_drm_register));
		mDrmContinueCheck = (TextView)findViewById(R.id.mmp_drm_expiredorcontinue);
		mDrmContinueCheck.setText(mContext.getString(R.string.mmp_drm_register_used,String.valueOf(2),String.valueOf(3)));

		setVisible();

	}



	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyMap.KEYCODE_BACK){
			mDrmListener.listenTo(false,false,mIndex);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void setVisible() {
		// TODO Auto-generated method stub
		if(-1 == mLimitCount && -1==mUsedCount){
			mDrmInfo.setText(R.string.mmp_drm_register);
			mDrmCancelLayout.setVisibility(View.GONE);
			mDrmContinueLayout.setVisibility(View.GONE);
		}else if(mLimitCount == mUsedCount){
			mDrmInfo.setText(mContext.getString(R.string.mmp_drm_register_used,String.valueOf(mUsedCount),String.valueOf(mLimitCount)));
			mDrmContinueCheck.setText(R.string.mmp_drm_register_expired);
			mDrmCancelLayout.setVisibility(View.GONE);
		}else{
			mDrmInfo.setText(mContext.getString(R.string.mmp_drm_register_used,String.valueOf(mUsedCount),String.valueOf(mLimitCount)));
			mDrmContinueCheck.setText(R.string.mmp_drm_register_continue);
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
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		Context context = getContext();

		lp.width = DisplayUtil.getWidthPixels(context, 0.35f);
		lp.height = DisplayUtil.getHeightPixels(context, 0.35f);

		getWindow().setAttributes(lp);
	}

	@Override
	public boolean onKey(View view, int code, KeyEvent event) {
		// TODO Auto-generated method stub
		MtkLog.i("drmdialog","code:"+code+"keyCode:"+event.getKeyCode()+" ENTER:"+KeyEvent.KEYCODE_DPAD_CENTER);
		if(KeyEvent.KEYCODE_DPAD_CENTER != event.getKeyCode()){
			return false;
		}
		switch(view.getId()){
		case R.id.mmp_drm_btn_ok:
			MtkLog.i("drmdialog","mmp_drm_btn_ok");
			if(mLimitCount > mUsedCount){
				mDrmListener.listenTo(true,true,mIndex);
			}else{
				mDrmListener.listenTo(true,false,mIndex);
			}
			break;
		case R.id.mmp_drm_btn_cancel:
			MtkLog.i("drmdialog","mmp_drm_btn_cancel");
			mDrmListener.listenTo(false,false,mIndex);
			break;
		}
		return true;
	}

}
