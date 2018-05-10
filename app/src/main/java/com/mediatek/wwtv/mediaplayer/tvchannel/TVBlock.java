package com.mediatek.wwtv.mediaplayer.tvchannel;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.USBBroadcastReceiver;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import static android.provider.ContactsContract.Intents.Insert.ACTION;

public class TVBlock implements Observer {
    private static final String TAG = "TVBlock";

    public static final String ACTION_CHANNEL_SERVICE = "com.skyworth.mediachannel.CHANNEL_SERVICE";
    public static final String CHANNEL_SERVICE_PACKAGE = "com.skyworth.mediachannel";
    public static final String CHANNEL_SERVICE_CLASS = "com.skyworth.mediachannel.TVBlockService";

    private static TVBlock instance;
    private Context mContext;

    private MultiFilesManager mFilesManager;
    private boolean mIsAddingProgram = false;

    private ProgramFile mUSBProgramFile;
    private ProgramFile mPVRProgramFile;

    private boolean mIsConnected = false;

    private ChannelManager mChannelManager;

    private TVBlock() {

    }

    public static TVBlock getInstance() {
        if(instance == null) {
            instance = new TVBlock();
        }
        return instance;
    }

    public void init(Context context) {
        // indicate already inited
        if(mContext != null) return;
        if(context != null) {
            mContext = context;
        } else {
            Log.d(TAG, "init failed, context == null !");
            return;
        }
        setupFilesManager(mContext);
        mFilesManager.addObserver(this);
    }

    public void attemptAddChannelDelay(long delay) {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                attemptAddChannel();
            }
        };
        timer.schedule(task, delay);
    }

    public void attemptAddChannel() {
        if(mIsAddingProgram) {
            Log.i(TAG, "attempAddChannel is adding program yet!");
            return;
        }
        new AddChannelAsyncTask().execute();
    }

    private void initUSBBroadcast(){
        IntentFilter filter = new IntentFilter();
        USBBroadcastReceiver usbBroadcastReceiver = new USBBroadcastReceiver();
        filter.addAction(ACTION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(usbBroadcastReceiver, filter);
    }

    public static void deleteThumbnailFile() {
        USBMediaFileManager.deleteThumbnail();
    }

    protected void setupFilesManager(Context context) {
        SaveValue pref = SaveValue.getInstance(context);
        boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false : true;
        boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false : true;
        mFilesManager = MultiFilesManager.getInstance(context, smbAvailable, dlnaAvailable);
        mFilesManager.getLocalDevices();
    }

    public void refreshLocalDevice() {
        if (mFilesManager == null) {
            setupFilesManager(mContext);
        }
        mFilesManager.addObserver(this);
        mFilesManager.refreshLocalDevice();
    }

    @Override
    public void update(Observable o, Object data) {
        final int request = (Integer) data;
        //Log.d(TAG, "update " + " request =" + request);
        switch (request) {
            case FilesManager.REQUEST_REFRESH: {
                break;
            }
            case FilesManager.REQUEST_SUB_DIRECTORY: {
                Log.d(TAG, "update REQUEST_SUB_DIRECTORY ");
                attemptAddChannel();
                break;
            }
            default:
                break;
        }
    }

    class AddChannelAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsAddingProgram = true;

            if (mFilesManager == null) {
                setupFilesManager(mContext);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String path = mFilesManager.getFirstDeviceMountPointPath();
            Log.d(TAG, "AddChannelAsyncTask path:" + path);

            if (path == null) return null;

            mUSBProgramFile = USBMediaFileManager.getUSBCardImageFilePath(
                    path, mContext, mFilesManager);
            //Remove PVR function
            //mPVRProgramFile = USBMediaFileManager.getPVRCardImageFilePath(
            //        path + "/PVR",  mContext, mFilesManager);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mUSBProgramFile != null || mPVRProgramFile != null) {
                if(mIsConnected) {
                    if(mChannelManager != null) {
                        Log.d(TAG, "AddChannelAsyncTask is Connected add program directly");
                        addProgramToRemote();
                    }
                } else {
                    bindService();
                }
            } else {
                Log.d(TAG, "AddChannelAsyncTask no Programs to add");
            }
            mIsAddingProgram = false;
        }
    }

    private void addProgramToRemote() {
        if(mChannelManager == null) return;
        if (mUSBProgramFile == null && mPVRProgramFile == null) return;
        List<ProgramFile> programs = new ArrayList<>();
        if(mUSBProgramFile != null) {
            programs.add(mUSBProgramFile);
        }
        if(mPVRProgramFile != null) {
            programs.add(mPVRProgramFile);
        }
        try {
            mChannelManager.addPrograms(programs);
        } catch (RemoteException e) {
            Log.e(TAG, "mChannelManager add programs failed");
            e.printStackTrace();
        }
        mUSBProgramFile = null;
        mPVRProgramFile = null;
    }

    private ServiceConnection mConn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected ......");
            mIsConnected = true;
            mChannelManager = ChannelManager.Stub.asInterface(service);

            try {
                mChannelManager.registerCallback(mCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            addProgramToRemote();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected ");
            mChannelManager = null;
            mIsConnected = false;
        }
    };

    private void bindService() {
        Log.d(TAG, "bindService ......");
        Intent intent = new Intent();
        intent.setClassName(CHANNEL_SERVICE_PACKAGE, CHANNEL_SERVICE_CLASS);
        intent.setAction(ACTION_CHANNEL_SERVICE);
        mContext.getApplicationContext().bindServiceAsUser(intent, mConn, Context.BIND_AUTO_CREATE, UserHandle.CURRENT);
    }

    private void stopService() {
        Log.d(TAG, "stopService ......");
        Intent intent = new Intent();
        intent.setClassName(CHANNEL_SERVICE_PACKAGE, CHANNEL_SERVICE_CLASS);
        intent.setAction(ACTION_CHANNEL_SERVICE);
        mContext.getApplicationContext().stopService(intent);
    }

    private AddCallBack mCallBack = new AddCallBack.Stub() {

        public void finishedAddProgram(boolean success) {
            Log.d(TAG, "mCallBack finishedAddProgram success:" + success);

            mUSBProgramFile = null;
            mPVRProgramFile = null;
            //stopService();
        }
    };
}
