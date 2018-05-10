
package com.mediatek.wwtv.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.os.SystemProperties;
import android.provider.Settings;

public class BootBroadcastReceiver extends BroadcastReceiver {

	@Override
  	public void onReceive(Context context, Intent intent) {
    	if (intent.getAction().compareTo("android.mediatek.intent.logcattousb") == 0) {
        	MtkLog.logOnFlag = intent.getBooleanExtra("status", false);
    	}
  	}
}
