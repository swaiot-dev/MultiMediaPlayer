package com.mediatek.wwtv.tvcenter.nav.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Process;
import android.os.SystemProperties;
import android.util.SparseArray;

import com.mediatek.wwtv.util.MtkLog;

public class ComponentStatusListener {
    private static final String TAG = "ComponentStatusListener";
    private static final int STATUS_CHANGED = 1;

    private static ComponentStatusListener mCSListener = null;
    private static int mParam1 = 0;

    private Handler mHandler = null;
    private HandlerThread mThread = null;
    private SparseArray<List<ICStatusListener>> mRigister;//local variables

    //please add your status ID here
    public static final int NAV_COMPONENT_HIDE  = 1;//its value is component id
    public static final int NAV_COMPONENT_SHOW  = 2;//its value is component id
    public static final int NAV_RESUME          = 3;//its value is 0
    public static final int NAV_PAUSE           = 4;//its value is 0
    public static final int NAV_KEY_OCCUR       = 5;//its value is key code
    public static final int NAV_ENTER_LANCHER   = 6;//its value is 0
    public static final int NAV_ENTER_MMP       = 7;//its value is 0
    public static final int NAV_ENTER_STANDBY   = 8;//its value is 0
    public static final int NAV_INPUT_SELECT    = 9;//its value is 0
    public static final int NAV_CHANNEL_CHANGED = 10;//its value is 0(success) or -1(fail);
    public static final int NAV_CONTENT_ALLOWED = 12;//its value is 0
    public static final int NAV_CONTENT_BLOCKED = 13;
    //end

    private ComponentStatusListener(){
        mRigister = new SparseArray<List<ICStatusListener>>();
        mThread = new HandlerThread(TAG, Process.THREAD_PRIORITY_DEFAULT);
        mThread.start();

        mHandler = new InternalHandler(this);
    }

    public static synchronized ComponentStatusListener getInstance() {
        if(mCSListener == null){
            mCSListener = new ComponentStatusListener();
        }

        return mCSListener;
    }

    public void updateStatus(int statusID, int value){
        Message msg = Message.obtain();
        msg.what = STATUS_CHANGED;
        msg.arg1 = statusID;
        msg.arg2 = value;

        MtkLog.d(TAG, "updateStatus, statusID=" + statusID + ",value=" + value);
        mHandler.sendMessage(msg);
    }

    public boolean addListener(int statusID, ICStatusListener listener){
        List<ICStatusListener> handlers = mRigister.get(statusID);

        MtkLog.d(TAG, "ComponentStatusListener, key=" + statusID);
        if(handlers != null){
            for(ICStatusListener handler : handlers){
                if(listener == handler){
                    MtkLog.d(TAG, "addListener, already existed");
                    return false;//already existed
                }
            }
        }
        else{
            MtkLog.d(TAG, "ComponentStatusListener, new ArrayList");
            handlers = new ArrayList<ICStatusListener>();
            mRigister.append(statusID, handlers);
        }

        return handlers.add(listener);
    }

    public boolean removeListener(ICStatusListener listener){
        int size = mRigister.size();

        for(int i = 0; i < size; i++){
            List<ICStatusListener> handlers = mRigister.valueAt(i);
            handlers.remove(listener);
        }

        return true;
    }

    public boolean removeAll(){
        synchronized(ComponentStatusListener.class){
            mRigister.clear();
            mThread.quit();

            mThread = null;
            mCSListener = null;
        }
        return true;
    }

    public static int getParam1(){
        return mParam1;
    }

    public static void setParam1(int param){
        synchronized(ComponentStatusListener.class){
            mParam1 = param;
        }
    }

    public interface ICStatusListener{
        public void updateComponentStatus(int statusID, int value);
    }

    /**
     * InternalHandler
     */
    private static class InternalHandler extends Handler{
        private final WeakReference<ComponentStatusListener> mDialog;

        public InternalHandler(ComponentStatusListener dialog){
            mDialog = new WeakReference<ComponentStatusListener>(dialog);
        }

        public void handleMessage(Message msg){
            MtkLog.d(TAG, "[InternalHandler] handlerMessage occur~");
            if(mDialog.get() == null){
                return ;
            }

            switch(msg.what){
                case STATUS_CHANGED:
                    synchronized(ComponentStatusListener.class){
                        List<ICStatusListener> handlers = mDialog.get().mRigister.get(msg.arg1);
                        if(handlers != null){
                            for(ICStatusListener listener : handlers){
                                listener.updateComponentStatus(msg.arg1, msg.arg2);
                            }
                        }
                        if((msg.arg1 == NAV_ENTER_STANDBY) && (msg.arg2 == -1)){
                            SystemProperties.set("debug.mtk.tkui.suspend", "1");
                        }
                    }
                    break;
            }
        }
    }
}
