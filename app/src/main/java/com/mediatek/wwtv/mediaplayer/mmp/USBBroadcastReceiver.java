package com.mediatek.wwtv.mediaplayer.mmp;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.tvchannel.TVBlock;

import java.util.List;

/**
 * Created by yangxiong on 2017/3/13/013.
 */

public class USBBroadcastReceiver extends BroadcastReceiver {
    private final static String TAG = "USBBroadcastReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if(intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
            new StopAudioAsyncTask(context, intent).execute();
        }
        checkTVBlockChannel(context, intent);
    }

    private void checkTVBlockChannel(Context context, Intent intent) {
        String action = intent.getAction();
        if (action !=null) {
            Log.d(TAG, "onReceive action:" + action);
            if (action.equals(Intent.ACTION_SHUTDOWN)
                    || action.equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                TVBlock.deleteThumbnailFile();
            }
            if (action.equals(Intent.ACTION_BOOT_COMPLETED)
                    || action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                final TVBlock tvBlock = TVBlock.getInstance();
                tvBlock.init(context);
                tvBlock.refreshLocalDevice();
                tvBlock.attemptAddChannelDelay(2000L);
            }
        }
    }

    public static boolean isBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    class StopAudioAsyncTask extends AsyncTask<Void, Void, Void> {

        Context mContext;
        Intent mIntent;

        public StopAudioAsyncTask(Context context, Intent intent) {
            mContext = context;
            mIntent = intent;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ActivityManager activityManager=(ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
            String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getShortClassName();
            Log.d("yangixong", "runningActivity=" + runningActivity);
            if ( (runningActivity.equals(".mmp.MediaMainActivity")) &&
                    isBackground(mContext) && mIntent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                LogicManager.getInstance(mContext).stopAudio();
            }
            return null;
        }
    }
}