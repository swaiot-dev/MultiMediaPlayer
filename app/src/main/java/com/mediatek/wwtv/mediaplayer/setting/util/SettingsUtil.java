package com.mediatek.wwtv.mediaplayer.setting.util;

import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

/**
 *
 * @author sin_biaoqinggao
 *
 */
public class SettingsUtil {

	public static  int SCREEN_WIDTH ;
	public static  int SCREEN_HEIGHT ;
	public static final String ACTION_PREPARE_SHUTDOWN = "android.mtk.intent.action.ACTION_PREPARE_SHUTDOWN";

	public static final String TAG = "SettingsUtil";
	public static String OPTIONSPLITER = "#";

    public static final int SVCTX_NTFY_CODE_SIGNAL_LOCKED = 4;
    public static final int SVCTX_NTFY_CODE_SIGNAL_LOSS = 5;
	/**
	 * get the real CfgId and the Value to set
	 * @param newId
	 * @return
	 */
	public static String[] getRealIdAndValue(String newId){
		String[] idValue = newId.split("#");
		if(idValue != null && idValue.length == 2){
			return idValue;
		}else{
			Log.e(TAG,"something error,please check your newId:"+newId);
		}
		return null;
	}

}
