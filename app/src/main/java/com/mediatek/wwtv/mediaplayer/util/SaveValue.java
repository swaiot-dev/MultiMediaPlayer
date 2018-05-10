package com.mediatek.wwtv.mediaplayer.util;



import java.io.IOException;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mediatek.wwtv.util.MtkLog;


public class SaveValue {
	 private static  Object syncRoot = new Object();
    private static final String TAG = "SaveValue";
    private SharedPreferences mSharedPreferences;
    Context mContext;

    private static String SP="com.mediatek.ui_preference";

    public static SaveValue save_data;

    private SaveValue(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static synchronized SaveValue getInstance(Context context) {
		if (save_data == null) {
			save_data = new SaveValue(context);
		}
	     return save_data;

    }

    public void saveValue(String name, int value) {
    	Log.i(TAG,"name:"+name+"--value:"+value);
        mSharedPreferences.edit().putInt(name, value).commit();
        flushMedia();
    }
    public void saveStrValue(String name, String value) {
        mSharedPreferences.edit().putString(name, value).commit();
        flushMedia();
    }
    public void saveBooleanValue(String name, boolean value){
    	mSharedPreferences.edit().putBoolean(name, value).commit();
    	flushMedia();
    }

	public int readValue(String id) {
		int value = 0;

//		if (id.equals(MenuConfigManager.DPMS)
//				|| id.equals(MenuConfigManager.AUTO_SYNC)) {
//			value = mSharedPreferences.getInt(id, 1);
//        } else {
            value = mSharedPreferences.getInt(id, 0);
//        }
        return value;
    }
    public String readStrValue(String id){
    	String value = null;
//    	if (id.equals(MenuConfigManager.TIMER1)||id.equals(MenuConfigManager.TIMER2)) {
//    		value = mSharedPreferences.getString(id, "00:00:00");
//		}else
		    if(id.equals("password")){
			value = mSharedPreferences.getString(id, "1234");
		}else {
			value = mSharedPreferences.getString(id, "0");
		}
    	return value;
    }
    public boolean readBooleanValue(String id){
    	Boolean value = false;
    	value = mSharedPreferences.getBoolean(id, false);
    	return value;
    }

    public void flushMedia() {
        try {
            Runtime.getRuntime().exec("sync");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
