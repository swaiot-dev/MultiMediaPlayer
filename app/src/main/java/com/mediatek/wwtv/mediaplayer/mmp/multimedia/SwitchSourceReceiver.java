package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;

/**
 * Created by heyzqt on 5/15/2018.
 */

public class SwitchSourceReceiver extends BroadcastReceiver {

	public static final String SKY_ACTION_SOURCE_CHANGE = "skyworth.intent.action.SOURCE_CHANGE";

	private static final String TAG = "ChangeSourceBroadcast";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive: ");
		String action = intent.getAction();
		if (SKY_ACTION_SOURCE_CHANGE.equals(action)) {
			Log.i(TAG, "onReceive: SKY_ACTION_SOURCE_CHANGE broadcast receiver");
			LogicManager.getInstance(context).finishVideo();
			LogicManager.getInstance(context).stopAudio();
		}
	}
}
