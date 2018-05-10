package com.mediatek.wwtv.mediaplayer.mmpcm.device;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

import com.mediatek.dm.Device;
import com.mediatek.dm.DeviceManager;
import com.mediatek.dm.DeviceManagerEvent;
import com.mediatek.dm.DeviceManagerListener;
import com.mediatek.dm.MountPoint;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity.MyDevListener;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;

/**
 *
 *This class represents manager local device.
 *
 */
public class DevManager {
    private static DevManager dman = null;
    private final DeviceManager dm;
    private final ArrayList<DevListener> onDevListener;
    static private final String TAG = "DevManager";
    /** the isMounted is used about refresh listView in MtkFilesBaseListActivity after power off to on. */
    private boolean isMounted = false;

    private DevManager(){
        onDevListener = new ArrayList<DevListener>();
        dm = DeviceManager.getInstance();
        dm.addListener(dmListener);
    }
    /**
     * Get device manager instance.
     * @return
     */
    public static DevManager getInstance(){
        if (dman == null) {
            synchronized (DevManager.class) {
               // if (dman == null) {
                    dman = new DevManager();
                //}
            }
        }
        return dman;
    }
    /**
     * Get mount point count.
     * @return
     */
    public int getMountCount(){
        return dm.getMountPointCount();
    }
    /**
     * Get mount point list.
     * @return
     */
    public ArrayList<MountPoint> getMountList(){
        return dm.getMountPointList();
    }
    /**
     * Get mount point info by specified path.
     * @param path
     * @return
     */
    public MountPoint getPointInfo(String path){
        return dm.getMountPoint(path);
    }
    /**
     * Add a device notify listenr.
     * @param devListener
     */
    public void addDevListener(DevListener devListener){
//      Util.LogResRelease("DevManager addDevListener devListener:" + devListener);
        onDevListener.add(devListener);
    }

    /**
     * Remove a device listener by specified device listener.
     * @param devListener
     */
    public void removeDevListener(DevListener devListener){
//      Util.LogResRelease("DevManager removeDevListener devListener:" + devListener);
        onDevListener.remove(devListener);
    }

    public void removeDevListeners(){
      Util.LogResRelease("DevManager removeDevListeners");
      if (VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
//        Util.LogResRelease("DevManager removeDevListeners is pip:" + onDevListener.size());
        for (int i = 0; i < onDevListener.size(); i++) {
          if (!(onDevListener.get(i) instanceof MyDevListener)) {
            onDevListener.remove(i);
            i--;
          }
        }
//        Util.LogResRelease("DevManager removeDevListeners is pip t: " + onDevListener.size());
      } else {
        onDevListener.clear();
      }
    }
	/**
	 * Mount a iso file by specified iso file path.
	 * @param isoFilePath
	 */
    public void mountISOFile( String isoFilePath)
    {
    	dm.mountISO(isoFilePath);
    }
    /**
     * Unmount a iso file by specified iso file path.
     * @param isoMountPath
     */
    public void umoutISOFile(String isoMountPath)
    {
    	dm.umountISO(isoMountPath);
    }
    /**
     * Check the device whether is virtual device by specified path.
     * @param isMountPath
     * @return
     */
    public boolean isVirtualDev(String isMountPath)
    {
    	return dm.isVirtualDevice(isMountPath);
    }
    //end ISO
    /**
     * Unmount device by specified mount point
     * @param mountPoint
     */
    public void unMountDevice(MountPoint mountPoint){
    	unMountDevice(getDeviceName(mountPoint));
    }


    private void unMountDevice(String devName){
    	if (devName != null){
    		dm.umountDevice(devName);
    	}

    }

    private String getDeviceName(MountPoint mountPoint){
    	Device dv = dm.getParentDevice(mountPoint);
    	return dv != null ? dv.mDeviceName : null;
    }
    /**
     * destroy device manager and remove devices listenr.
     */
    public void destroy(){
    	Util.LogResRelease("DevManager Destroy");
    	if (VideoPlayActivity.getInstance() != null
    	    && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
//    	  Util.LogResRelease("DevManager Destroy is pip");
//    	  Util.LogResRelease("DevManager destroy is pip:" + onDevListener.size());
    	  for (int i = 0; i < onDevListener.size(); i++) {
    	    if (!(onDevListener.get(i) instanceof MyDevListener)) {
    	      onDevListener.remove(i);
    	      i--;
    	    }
        }
//    	  Util.LogResRelease("DevManager destroy is piptt:" + onDevListener.size());
    	} else {
//    	  Util.LogResRelease("DevManager Destroy is not pip");
    	  dm.removeListener(dmListener);
    	  onDevListener.clear();
    	  dman = null;
    	}
    }

   private final DeviceManagerListener dmListener = new DeviceManagerListener(){

        public void onEvent(DeviceManagerEvent arg0) {
            MtkLog.d(TAG, "DevManager-->DeviceManagerListener-->onEvent");
            if (arg0.getType() == DeviceManagerEvent.mounted) {
                MtkLog.d(TAG, "OnEvent-->mounted");
                isMounted = true;
            }

            if(onDevListener.size() > 0){
                Iterator<DevListener> it = onDevListener.iterator();

                while(it.hasNext()){
                    DevListener lis = it.next();
                    lis.onEvent(arg0);
                }
            }
        }
    };

    public void setMount(boolean mounted) {
        this.isMounted = mounted;
    }

    public boolean getMount() {
        return isMounted;
    }
}
