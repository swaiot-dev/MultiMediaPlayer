/**
 * @Description: TODO()
 *
 */
package com.mediatek.wwtv.tvcenter.util;

import android.util.Log;
import com.mediatek.wwtv.util.KeyMap;

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
		case KeyMap.KEYCODE_0:
			_ch = '0';
			_mStr = "0";
			break;
		case KeyMap.KEYCODE_1:
			_ch = '1';
			_mStr = "1";
			break;
		case KeyMap.KEYCODE_2:
			_ch = '2';
			_mStr = "2";
			break;
		case KeyMap.KEYCODE_3:
			_ch = '3';
			_mStr = "3";
			break;
		case KeyMap.KEYCODE_4:
			_ch = '4';
			_mStr = "4";
			break;
		case KeyMap.KEYCODE_5:
			_ch = '5';
			_mStr = "5";
			break;
		case KeyMap.KEYCODE_6:
			_ch = '6';
			_mStr = "6";
			break;
		case KeyMap.KEYCODE_7:
			_ch = '7';
			_mStr = "7";
			break;
		case KeyMap.KEYCODE_8:
			_ch = '8';
			_mStr = "8";
			break;
		case KeyMap.KEYCODE_9:
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
	public static byte [] stringToByte(String s){
		byte[] b = new byte[3];
		if (s != null && s.length() == 5) {
			byte[] bytes = s.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				System.out.println(i + "  = "
						+ Integer.toBinaryString(bytes[i] - 48));
			}
			b[0] = (byte) (((bytes[0] - 48) * 16) | (bytes[1] - 48));
			b[1] = (byte) (((bytes[2] - 48) * 16) | (bytes[3] - 48));
			b[2] = (byte) (((bytes[4] - 48) * 16) | (0x0F));
		}
		return b;
	}
}
