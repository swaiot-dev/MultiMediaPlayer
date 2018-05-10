package com.mediatek.wwtv.mediaplayer.setting.presenter;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class SettingItem {
	
	private String mTitle;
	
	private Drawable mIcon;
	
	private Intent mIntent;
	
	private boolean isCircle = false;

	public SettingItem(){
		
	}
	
	public SettingItem(String title,Drawable icon){
		mTitle = title;
		mIcon = icon;
	}
	
	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public Drawable getmIcon() {
		return mIcon;
	}

	public void setmIcon(Drawable mIcon) {
		this.mIcon = mIcon;
	}

	public Intent getIntent() {
		return mIntent;
	}

	public void setIntent(Intent mIntent) {
		this.mIntent = mIntent;
	}

	public boolean isCircle() {
		return isCircle;
	}

	public void setCircle(boolean isCircle) {
		this.isCircle = isCircle;
	}
	
	
}
