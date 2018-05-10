package com.mediatek.wwtv.tvcenter.menu.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.text.TextUtils;


import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.wwtv.tvcenter.util.Util;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import java.util.ArrayList;

public class TVContent {
	private static TVContent instance;
    public static final int VSH_SRC_TAG3D_2D  = 0;
    public static final int VSH_SRC_TAG3D_MVC = 1;      // MVC = Multi-View Codec
    public static final int VSH_SRC_TAG3D_FP  = 2;      // FP = Frame Packing
    public static final int VSH_SRC_TAG3D_FS  = 3;     // FS = Frame Sequential
    public static final int VSH_SRC_TAG3D_TB  = 4;       // TB = Top-and-Bottom
    public static final int VSH_SRC_TAG3D_SBS = 5;      // SBS = Side-by-Side
    public static final int VSH_SRC_TAG3D_REALD = 6;    //
    public static final int VSH_SRC_TAG3D_SENSIO = 7;   //
    public static final int VSH_SRC_TAG3D_LA = 8;      // LA = Line Alternative
    public static final int VSH_SRC_TAG3D_TTDO = 9;     // TTD only. It is 2D mode
    public static final int VSH_SRC_TAG3D_NOT_SUPPORT = 10;

	private MtkTvConfig mTvConfig;

	private boolean dumy = false;

	private int mScanMode = 0;

	private Context mContext;

	private String TAG = "TVContent";




	protected TVContent(Context context) {
		mContext = context;
		init();
	}

	private void init() {

		mTvConfig = MtkTvConfig.getInstance();

	}

	static public synchronized TVContent getInstance(Context context) {
		if (instance == null) {
			instance = new TVContent(context);
		}
		return instance;
	}








	public boolean isConfigVisible(String cfgid){
		boolean flag = mTvConfig.isConfigVisible(cfgid) == MtkTvConfigType.CFGR_VISIBLE ? true
				: false;
		return flag;
	}

}
