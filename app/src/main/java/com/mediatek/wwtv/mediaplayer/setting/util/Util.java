/**
 * @Description: TODO()
 *
 */
package com.mediatek.wwtv.mediaplayer.setting.util;

import android.util.Log;
import android.view.KeyEvent;

import com.mediatek.wwtv.util.MtkLog;

/**
 *
 */
public class Util {

	public interface iDrmlistener{
		public void listenTo(boolean isSure,boolean isContinue,int index);
	};

	private final static boolean AP_DEBUG = true;
	public final static boolean DEBUG = MtkLog.logOnFlag && AP_DEBUG;
	private final static boolean STACK_DEBUG = DEBUG&&false;

	private final static String TAG = "Util_Log";

	/**
	 * true: means DVBS is developing, the code only for UI and method debug.
	 * false: call TV-API directly.
	 */
	public final static boolean DVBS_DEV_ING = false;
	public static void showDLog(String string) {
		if (DEBUG)
			Log.d(TAG, string);
	}

	public static void showDLog(String tag, String string) {
		if (DEBUG)
			Log.d(tag, string);
	}

	public static void showELog(String string) {
		if (DEBUG)
			Log.e(TAG, string);
	}

	public static void showELog(String tag, String string) {
		if (DEBUG)
			Log.e(tag, string);
	}
	public static String mapKeyCodeToStr(int keyCode){
		String _mStr = "";
		char _ch;
		switch (keyCode) {
		case KeyEvent.KEYCODE_0:
			_ch = '0';
			_mStr = "0";
			break;
		case KeyEvent.KEYCODE_1:
			_ch = '1';
			_mStr = "1";
			break;
		case KeyEvent.KEYCODE_2:
			_ch = '2';
			_mStr = "2";
			break;
		case KeyEvent.KEYCODE_3:
			_ch = '3';
			_mStr = "3";
			break;
		case KeyEvent.KEYCODE_4:
			_ch = '4';
			_mStr = "4";
			break;
		case KeyEvent.KEYCODE_5:
			_ch = '5';
			_mStr = "5";
			break;
		case KeyEvent.KEYCODE_6:
			_ch = '6';
			_mStr = "6";
			break;
		case KeyEvent.KEYCODE_7:
			_ch = '7';
			_mStr = "7";
			break;
		case KeyEvent.KEYCODE_8:
			_ch = '8';
			_mStr = "8";
			break;
		case KeyEvent.KEYCODE_9:
			_ch = '9';
			_mStr = "9";
			break;
		default:
			break;
		}

		return _mStr;
	}

	public static void printStackTrace() {
		if (STACK_DEBUG) {
			Throwable tr = new Throwable();
			Log.getStackTraceString(tr);
			tr.printStackTrace();
		}
	}
}
