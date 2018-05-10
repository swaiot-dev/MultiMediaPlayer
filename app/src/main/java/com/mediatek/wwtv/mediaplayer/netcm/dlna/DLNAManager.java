
package com.mediatek.wwtv.mediaplayer.netcm.dlna;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Stack;

import com.mediatek.dlna.DeviceEventListener;
import com.mediatek.dlna.DigitalMediaPlayer;
import com.mediatek.dlna.FoundDeviceEvent;
import com.mediatek.dlna.LeftDeviceEvent;
import com.mediatek.dlna.object.Content;
import com.mediatek.dlna.object.ContentEventListener;
import com.mediatek.dlna.object.ContentType;
import com.mediatek.dlna.object.DLNADevice;
import com.mediatek.dlna.object.DLNADeviceType;
import com.mediatek.dlna.object.FailedContentEvent;
import com.mediatek.dlna.object.MediaServer;
import com.mediatek.dlna.object.NormalContentEvent;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.netcm.util.NetLog;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;

import android.util.Log;

/**
 * This Class find content on digital media servers (DMS) and provide
 * playback, rendering and get input stream capabilites.
 *
 */
public class DLNAManager implements DeviceEventListener, ContentEventListener {
  public static final int FILE_TYPE_UNKNOW = 0;
  public static final int FILE_TYPE_DIRECTORY = 1;
  public static final int FILE_TYPE_VIDEO = 2;
  public static final int FILE_TYPE_AUDIO = 3;
  public static final int FILE_TYPE_IMAGE = 4;
  public static final int FILE_TYPE_TEXT = 5;
  public static final int FILE_TYPE_DEVICE = 6;

  public static final boolean CD_CHILD = true;
  public static final boolean CD_PARENT = false;
  public boolean isStartOnce = false;

  private static final boolean localLOGV = true;
  private final String TAG = "CM_DLNAManager";
  private final int CONTENT_PARENT = 0;
  private final int CONTENT_CHILD = 1;
  private final int CONTENT_UNKNOW = 2;
  private static DLNAManager mDLNAManager = null;
  private MediaServer server = null;
  private DigitalMediaPlayer player;
  private final int BROWSE_REQUEST = 30;
  private int REQUEST_MATCH_NUM = 0;
  private int mTotalNum = 0;
  private String currentID;
  private static LinkedList<DLNAFile> mListDLNADeviceName = new LinkedList<DLNAFile>();
  private static Hashtable<String, DLNADevice> mNameDeviceTable
  = new Hashtable<String, DLNADevice>();
  private static LinkedList<DLNAFile> mListContentName = new LinkedList<DLNAFile>();
  private static Hashtable<String, Content> mNameContentTable = new Hashtable<String, Content>();
  private static Hashtable<String, Content> mSubTitleContentTable
  = new Hashtable<String, Content>();
  private final Stack<DLNATempFile> mTraceFile = new Stack<DLNATempFile>();
  private final String mRoot = "/";
  private String mCurPath = mRoot;
  private FileEventListener listener;
  private long mBrowseHandle = -1L;

  static {
    mDLNAManager = new DLNAManager();
  }

  private DLNAManager() {
    if (localLOGV) {
      Log.d(TAG, "DLNAManager create here!");
    }
  }

  /**
   * Create a new DLNAManager instance.
   * Applications will use for DLNA operation.
   *
   */
  public static DLNAManager getInstance() {
    if (mDLNAManager == null) {
      mDLNAManager = new DLNAManager();
    }
    return mDLNAManager;
  }

  /**
   * Inner class.
   *
   */
  class DLNATempFile {
    public static final int TYPE_DEVICE = 0;
    public static final int TYPE_CONTENT = 1;
    public static final int TYPE_UNKNOW = 2;
    private int fileType = TYPE_UNKNOW;
    private DLNADevice device = null;
    private Content content = null;

    public DLNATempFile(DLNADevice device) {
      this.device = device;
      this.fileType = TYPE_DEVICE;
    }

    public DLNATempFile(Content content) {
      this.content = content;
      this.fileType = TYPE_CONTENT;
    }

    public int getType() {
      return fileType;
    }

    public DLNADevice getDevice() {
      return this.device;
    }

    public Content getContent() {
      return this.content;
    }

    public String getName() {
      int type = this.getType();
      if (type == TYPE_DEVICE)
        return this.getDevice().getName();
      else if (type == TYPE_CONTENT)
        return this.getContent().getTitle();
      return null;
    }

    public boolean equals(DLNATempFile fileIns) {
      int type = this.getType();
      if (type != fileIns.getType())
        return false;
      if (type == TYPE_DEVICE) {
        if (this.getDevice() == null || this.getDevice().getName() == null)
          return false;
        if (this.getDevice().getName().equals(fileIns.getDevice().getName()))
          return true;
      } else if (type == TYPE_CONTENT) {
        if (this.getContent() == null || this.getContent().getTitle() == null)
          return false;
        if (this.getContent().getTitle().equals(fileIns.getContent()
            .getTitle()))
          return true;
      }
      return false;
    }
  }

  /**
   * Get the name of the latest parsed path
   *
   * @return the name of the latest parsed path
   */
  public String getParsedPath() {
    return mCurPath;
  }

  public void setCurrentParserPath(String path) {
    mCurPath = path;
  }

  private void constructCurPath(String fileName, boolean addFlag) {
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.i(TAG, "constructCurPath:fileName:" + fileName + "  addFlag:" + addFlag
          + "  mCurPath:" + mCurPath);
    } else {
      MtkLog.i(TAG, "constructCurPath:fileName:" + fileName + "  addFlag:" + addFlag
          + "  mCurPath:" + mCurPath);
    }
    if (addFlag == true) {//enter sub path
      if (mCurPath.endsWith("/") == true) {
        mCurPath += fileName;
        return;
      }
      mCurPath += ("/" + fileName);
      MtkLog.i(TAG, "constructCurPath:if mCurPath:" + mCurPath);
      return;
    } else {//back current path to last
      // String name ="";
      // if(fileName.endsWith("/"))
      // name = fileName.substring(0, fileName.lastIndexOf("/"));
      // mCurPath = mCurPath.substring(0,
      // mCurPath.lastIndexOf(name)+name.length());
      mCurPath = mCurPath.substring(0, mCurPath.lastIndexOf(fileName));
      MtkLog.i(TAG, "constructCurPath:else mCurPath:" + mCurPath);
      return;
    }
  }

  private boolean isDevice(String fileName) {
    if (fileName == null)
      return false;

    int total = mListDLNADeviceName.size();
//    if (localLOGV)
//      NetLog.d(TAG, "---mListDLNADeviceName total:" + total
//          + "  fileName:" + fileName);
    for (int i = 0; i < mListDLNADeviceName.size(); i++) {
      DLNAFile tempFile = mListDLNADeviceName.get(i);
      String name = tempFile.getName();
//      NetLog.d(TAG, "device name: " + name);
      if (name != null && name.equals(fileName)) {
        return true;
      }
    }

    return false;
  }

  private int checkContent(String path) {
    String fileName = retriveName(path);
    MtkLog.i(TAG, "checkContent:fileName:" + fileName);
    if (fileName == null) {
      return CONTENT_UNKNOW;
    }

    if (path.compareTo(mCurPath) > 0) {
      return CONTENT_CHILD;
    } else {
      return CONTENT_PARENT;
    }

    /*
     * MtkLog.i(TAG,"checkContent:mCurPath:"+mCurPath);
     * MtkLog.i(TAG,"checkContent:mTraceFile.peek().getName():"+mTraceFile.peek().getName()); if
     * (mTraceFile.peek().getName() != null && fileName.equals(mTraceFile.peek().getName())) {
     * return CONTENT_PARENT; } int total = mListContentName.size(); for (int i = 0; i < total; i++)
     * { String name = mListContentName.get(i).getName(); MtkLog.i(TAG,"checkContent:name:"+name);
     * if (name != null && name.equals(fileName)) { return CONTENT_CHILD; } } return CONTENT_UNKNOW;
     */
  }

  private void parseRootDirectory() {
    if (localLOGV)
      NetLog.d(TAG, "parseRootDirectory isStartOnce:" + isStartOnce);
    if (!isStartOnce) {
      mCurPath = mRoot;
      if (!Util.mIsEnterPip) {
        mListDLNADeviceName.clear();
        mNameDeviceTable.clear();
      }
      if (localLOGV)
        NetLog.d(TAG, "parseRootDirectory start call DigitalMediaPlayer.getInstance()");
      player = DigitalMediaPlayer.getInstance();
      player.setDeviceEventListener(mDLNAManager);

      // player.stop();
      int result = player.start();
      if (localLOGV) {
        NetLog.d(TAG, "parseRootDirectory player.start():" + result);
      }
      // new Exception().printStackTrace();
      isStartOnce = true;
      if (Util.mIsEnterPip) {
        notifyDirect();
      }
    } else {
      notifyDirect();
    }
  }

  private void parseDevice(String fileName) {
    if (!mCurPath.equals(mRoot)) {
      mCurPath = mRoot;
    }
    constructCurPath(fileName, true);
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "parseDevice fileName:" + fileName);
    } else {
      MtkLog.i(TAG, "parseDevice fileName:" + fileName);
    }
    DLNADevice device = mNameDeviceTable.get(fileName);
    if (null == device || (device.getType() != DLNADeviceType.DigitalMediaServer)
        && (device.getType() != DLNADeviceType.MobileDigitalMediaServer)) {
      return;
    }
    DLNATempFile fileins = new DLNATempFile(device);
    mTraceFile.push(fileins);
    mListContentName.clear();

    // mNameContentTable.clear();
    server = (MediaServer) device;
    server.setActionEventListener(mDLNAManager);

    long startTime = System.currentTimeMillis();
    if (localLOGV) {
      NetLog.v(TAG, "parseDevice start 1 browse : " + startTime);
    }

    REQUEST_MATCH_NUM = 0;
    currentID = MediaServer.OBJECT_ID;
    mBrowseHandle = server.browse(MediaServer.OBJECT_ID,
        MediaServer.BrowseFlag.BrowseDirectChildren, REQUEST_MATCH_NUM,
        BROWSE_REQUEST, MediaServer.FILTER_BASIC_INFO,
        MediaServer.SORT_ASCENDING_CRITERIA_TITLE);

    if (localLOGV) {
      NetLog.v(TAG, "parseDevice end 1 browse : " + System.currentTimeMillis()
          + "    " + (System.currentTimeMillis() - startTime));
    }
  }

  private void parseContent(String path, String fileName) throws Exception {
    // String fileName = retriveName(path);
    int contentType = checkContent(path);
    Content content = null;
    if (localLOGV) {
      NetLog.d(TAG, "parseContent file name: " + fileName + "  contentType:" + contentType);
    }
    if (contentType == CONTENT_UNKNOW) {
      // throw new Exception();
    } else if (contentType == CONTENT_CHILD) {
      constructCurPath(fileName, true);
      content = mNameContentTable.get(fileName);
      DLNATempFile fileins = new DLNATempFile(content);
      mTraceFile.push(fileins);
    } else {
      content = mTraceFile.peek().getContent();
    }

    mListContentName.clear();
    // mNameContentTable.clear();
    if (content == null) {
      return;
    }
    server = content.getServer();
    server.setActionEventListener(mDLNAManager);
    long startTime = System.currentTimeMillis();
    if (localLOGV) {
      NetLog.v(TAG, "parseContent start 2 browse : " + startTime);
    }

    REQUEST_MATCH_NUM = 0;
    currentID = content.getObjectId();
    mBrowseHandle = server.browse(content.getObjectId(),
        MediaServer.BrowseFlag.BrowseDirectChildren, REQUEST_MATCH_NUM,
        BROWSE_REQUEST, MediaServer.FILTER_BASIC_INFO,
        MediaServer.SORT_ASCENDING_CRITERIA_TITLE);

    if (localLOGV) {
      NetLog.v(TAG, "parseContent end 2 browse : " + System.currentTimeMillis()
          + "    " + (System.currentTimeMillis() - startTime));
    }
  }

  /**
   * Get the file list of the input path
   *
   * @param fileName    the name of the file waiting to be parsed
   * @param walkType    the type of walk routine, application can
   *                    use CD_CHILD and CD_PARENT.
   * @return
   * @throws Exception
   *             Invalid input file name
   */
  public void parseDLNAFile(String path, String fileName, boolean walkType)
      throws Exception {
    if (localLOGV) {
      NetLog.d(TAG, "[parseDLNAFile]path:" + path
          + "  fileName:" + fileName + "  walkType:" + walkType);
    }
    // String fileName = retriveName(path);
    cancelNotify();

    if (walkType == CD_CHILD) {

      if (fileName == null || fileName.equals(mRoot)) {
        if (localLOGV) {
          NetLog.d(TAG, "[parseDLNAFile][root][fileName]:" + fileName);
        }
        parseRootDirectory();
        return;
      }

      REQUEST_MATCH_NUM = 0;
      if (isDevice(fileName) == true) {
        // device
        if (localLOGV) {
          NetLog.d(TAG, "[parseDLNAFile][device][fileName]:" + fileName);
        }
        parseDevice(fileName);
        return;

      } else {
        // content
        if (localLOGV) {
          NetLog.d(TAG, "[parseDLNAFile][content][fileName]:"
              + fileName);
        }
        parseContent(path, fileName);
        return;
      }
    } else {
      try {
        if (!mTraceFile.empty()) {
          DLNATempFile dlnaFile = mTraceFile.pop();
          constructCurPath(dlnaFile.getName(), false);
          if (dlnaFile.getType() == DLNATempFile.TYPE_DEVICE) {
            if (localLOGV)
              NetLog.d(TAG, "[parseParent] pop device, next parse root");
            mCurPath = mRoot;
            // player.stop();

            parseRootDirectory();
            return;
          } else {
            dlnaFile = mTraceFile.peek();
            String name = dlnaFile.getName();
            if (dlnaFile.getType() == DLNATempFile.TYPE_DEVICE) {
              constructCurPath(dlnaFile.getName(), false);
              mTraceFile.pop();
              parseDevice(name);

            } else {
              parseContent(path, fileName);
            }
          }
        } else {
          if (localLOGV)
            NetLog.d(TAG, "[parseParent] stack is empty, next parse root");
          // player.stop();
          parseRootDirectory();
        }
      } catch (Exception e) {
        throw new Exception(e);
      }
    }
  }

  /**
   * Get the file list of current parent path
   *
   * @param
   * @return
   * @throws Exception
   */
  // public void parseParent() throws Exception {
  // try {
  // if (mTraceFile.empty() == false) {
  // DLNATempFile dlnaFile = mTraceFile.pop();
  // constructCurPath(dlnaFile.getName(), false);
  // if (dlnaFile.getType() == DLNATempFile.TYPE_DEVICE) {
  // Log.d(TAG, "[parseParent] pop device, next parse root");
  // mCurPath = mRoot;
  // player.stop();
  // parseDLNAFile(null);
  // } else {
  // dlnaFile = mTraceFile.peek();
  // String name = dlnaFile.getName();
  // if (dlnaFile.getType() == DLNATempFile.TYPE_DEVICE) {
  // Log.d(TAG,
  // "[parseParent] pop content, next parse device");
  // constructCurPath(dlnaFile.getName(), false);
  // mTraceFile.pop();
  // parseDLNAFile(name);
  // } else {
  // Log
  // .d(TAG,
  // "[parseParent] pop content, next parse content");
  // parseDLNAFile(name);
  // }
  // }
  // } else {
  // Log.d(TAG, "[parseParent] stack is empty, next parse root");
  // player.stop();
  // parseDLNAFile(null);
  // }
  // } catch (Exception e) {
  // throw new Exception(e);
  // }
  // }

  /**
   * Judging whether the lasted parsed path is the root.
   *
   * @return Return true if the path is the root, else return false
   */
  public boolean isRoot() {
    if (mCurPath.equals(mRoot))
      return true;
    return false;
  }

  /**
   * Destroy options.
   */
  // fix cr DTV00416665
  public void destroy(boolean isDLNAInPip) {
    stopDlna(isDLNAInPip);
    /*
     * if (localLOGV) NetLog.d(TAG, "destroy dlna Manager structure."); mListDLNADeviceName.clear();
     * mNameDeviceTable.clear(); mListContentName.clear(); //mNameContentTable.clear();
     * mTraceFile.clear(); if(player != null) { new Exception().printStackTrace(); // check
     * player.stop(); } isStartOnce = false; mDLNAManager = null; mCurPath = "/"; listener = null;
     */
  }

  /**
   * Destroy options.
   */
  public void stopDlna(boolean isDLNAInPip) {
    if (localLOGV) {
      NetLog.d(TAG, "stopDlna destroy dlna Manager structure.");
    }
//    Util.LogResRelease("DLNAMANAGER stopDlna");
    cancelNotify();
    mListContentName.clear();
    mTraceFile.clear();
    if (player != null) {
      if (isDLNAInPip) {
        if (localLOGV) {
          NetLog.d(TAG, "isDLNAInPip no need player.stop():");
        }
      } else {
        mListDLNADeviceName.clear();
        mNameDeviceTable.clear();
        int result = player.stop();
        if (localLOGV) {
          NetLog.d(TAG, "stopDlna destroy player.stop():" + result);
        }
      }
    }

    isStartOnce = false;
    mDLNAManager = null;
    mCurPath = "/";
    listener = null;
    deleteDlnaFile("goldendms");
  }

  public static int stoDMP() {
    return DigitalMediaPlayer.getInstance().stop();
  }

  /**
   * Get the file type.
   *
   * @param fileName
   *            The name of the file waiting to be parsed.
   * @return DLNAManager.FILE_TYPE_DEVICE; DLNAManager.FILE_TYPE_DIRECTORY;
   *         DLNAManager.FILE_TYPE_VIDEO; DLNAManager.FILE_TYPE_AUDIO;
   *         DLNAManager.FILE_TYPE_IMAGE; DLNAManager.FILE_TYPE_TEXT;
   *         DLNAManager.FILE_TYPE_UNKNOW;
   */
  public int getType(String fileName) {
    if (isDevice(fileName))
      return FILE_TYPE_DEVICE;
    else {
      Content cnt = mNameContentTable.get(fileName);
      if (cnt == null) {
        return FILE_TYPE_UNKNOW;
      }

      if (cnt.isDirectory()){
        return FILE_TYPE_DIRECTORY;
      }
      ContentType type = cnt.getType();
      switch (type) {
        case Video:
          return FILE_TYPE_VIDEO;
        case Audio:
          return FILE_TYPE_AUDIO;
        case Photo:
          return FILE_TYPE_IMAGE;
        case Playlist:
          return FILE_TYPE_TEXT;
      }
    }
    return FILE_TYPE_UNKNOW;
  }

  private void notifyDirect() {
    // FoundDeviceEvent event = null;
    if (listener != null) {
      listener.onFileFound(new FileEvent((FoundDeviceEvent) null, mListDLNADeviceName));
    }
  }

  /**
   * Call back function, implement for com.mediatek.dlna.jar.
   * Application should not use this API.
   *
   * @return.
   */
  public void notifyDeviceFound(FoundDeviceEvent event) {
    DLNADevice device = event.getDevice();
    String name = device.getName();
    if (localLOGV)
      NetLog.d(TAG, "notifyDeviceFound name:" + name);

    DLNAFile devicefile = new DLNAFile(device, mRoot, mRoot);

    if (localLOGV)
      NetLog.d(TAG, "notifyDeviceFound add device to device list name:" + name);
    if (MediaMainActivity.mIsDlnaAutoTest) {
      if (name.toLowerCase().startsWith("goldendms")) {
        mListDLNADeviceName.add(devicefile);
        mNameDeviceTable.put(name, device);
      }
    } else {
      mListDLNADeviceName.add(devicefile);
      mNameDeviceTable.put(name, device);
    }
    if (localLOGV) {
      NetLog.d(TAG, "notifyDeviceFound add device mCurPath:" + mCurPath);
    }
    createDlnaFile(name);
    if (listener != null && (mCurPath == null || mCurPath.equals(mRoot))) {
      listener.onFileFound(new FileEvent(event, mListDLNADeviceName));
    }
  }

  /**
   * Call back function, implement for com.mediatek.dlna.jar.
   * Application should not use this API.
   *
   * @return.
   */
  public void notifyDeviceLeft(LeftDeviceEvent event) {
    DLNADevice device = event.getDevice();
    String name = device.getName();
    if (localLOGV)
      NetLog.d(TAG, "notifyDeviceLeft name:" + name + "  mCurPath:" + mCurPath);

    int number = mListDLNADeviceName.size();
    if (localLOGV)
      NetLog.d(TAG, "notifyDeviceLeft name:" + name + "  number:" + number);
    for (int i = 0; i < mListDLNADeviceName.size(); i++) {
      DLNAFile tempDevice = mListDLNADeviceName.get(i);
      if (device.equals(tempDevice.getDevice())) {
        mListDLNADeviceName.remove(tempDevice);
        break;
      }
    }

    mNameDeviceTable.remove(name);
    deleteDlnaFile(name);
    if (listener != null && (mCurPath == null || mCurPath.equals(mRoot)
        || mCurPath.contains(name))) {
      listener.onFileLeft(new FileEvent(event, mListDLNADeviceName));
    }
  }

  /**
   * Call back function, implement for com.mediatek.dlna.jar.
   * Application should not use this API.
   *
   * @return.
   */
  public void notifyContentSuccessful(NormalContentEvent event) {

    if (localLOGV)
      NetLog.v(TAG, " notifyContentSuccessful : "
          + System.currentTimeMillis() + "  event:" + event
          + "  mCurPath:" + mCurPath);

    if (event == null) {
      if (listener != null) {
        listener.onFileFound(new FileEvent(event, mListContentName));
      }
      return;
    }
    if (event.getTotalMatches() <= 0) {
      if (listener != null) {
        listener.onFileFound(new FileEvent(event, mListContentName));
      }
      return;
    }
    int listnum = event.getList().size();
    if (localLOGV)
      NetLog.v(TAG, "notifyContentSuccessful Last index is : " + REQUEST_MATCH_NUM
          + ", List num is: " + listnum);

//    if (listnum <= 0) {
//      if (localLOGV)
//        NetLog.v(TAG, " List num is 0, return!");
//      if (listener != null) {
//        listener.onFileFound(new FileEvent(event, mListContentName));
//      }
//      return;
//    }
    if (listnum <= 0) {
      REQUEST_MATCH_NUM = REQUEST_MATCH_NUM + 1;// BROWSE_REQUEST
    } else {
      REQUEST_MATCH_NUM = REQUEST_MATCH_NUM + listnum;// BROWSE_REQUEST
    }
    ArrayList<Content> items = event.getList();
    for (int i = 0; i < listnum; i++) {
      Content cnt = items.get(i);
      String parent = null;
      String cname = cnt.getTitle();
      String tempParent = mCurPath;

      int index = tempParent.lastIndexOf("/");
      if (index == 0) {
        parent = mCurPath.substring(1, mCurPath.length());
      } else {
        parent = mCurPath.substring(index + 1, mCurPath.length());
      }

      StringBuffer pathBuf = new StringBuffer();
      pathBuf.append(mCurPath).append('/').append(cname);

      DLNAFile contentfile = new DLNAFile(cnt, pathBuf.toString(), parent);

      if (contentfile.isSubTitleFile()) {
        mSubTitleContentTable.put(cname, cnt);

      } else {

        mListContentName.add(contentfile);

        mNameContentTable.put(cname, cnt);
      }
    }
    // LinkedList<DLNAFile> cntList = new LinkedList<DLNAFile>();
    // int size = mNameContentTable.size();
    // for (int i = 0; i < size; i++)
    // cntList.add(mNameContentTable.get(mListContentName.get(i)));
    if (listnum > 0 && listener != null) {
      listener.onFileFound(new FileEvent(event, mListContentName));
    }

    if (localLOGV)
      NetLog.v(TAG, " notifyContentSuccessful end : "
          + System.currentTimeMillis());

    int total = event.getTotalMatches();
    mTotalNum = total;
    if (localLOGV)
      NetLog.v(TAG, "notifyContentSuccessful Current index is : " + REQUEST_MATCH_NUM
          + ", Total is: " + total);
    if (REQUEST_MATCH_NUM < total) {
      long startTime = System.currentTimeMillis();
      if (localLOGV) {
        NetLog.v(TAG, "notifyContentSuccessful start 3 browse : " + startTime);
      }
      mBrowseHandle = server.browse(currentID,
          MediaServer.BrowseFlag.BrowseDirectChildren,
          REQUEST_MATCH_NUM, BROWSE_REQUEST,
          MediaServer.FILTER_BASIC_INFO,
          MediaServer.SORT_ASCENDING_CRITERIA_TITLE);
      if (localLOGV) {
        NetLog.v(TAG, "notifyContentSuccessful end 3 browse : " + System.currentTimeMillis()
            + "    " + (System.currentTimeMillis() - startTime));
      }
    }

  }

  public ArrayList<String> getSubTitleList(String nameSuffix) {
    String fileName = getFileName(nameSuffix);
    Content cnt = mNameContentTable.get(fileName);
    if (cnt == null) {
      return null;
    }
    ArrayList<String> subTitleList = new ArrayList<String>();

    try {
      for (String name : mSubTitleContentTable.keySet()) {
        String tmpName = name.toLowerCase();
        if (tmpName.startsWith(fileName)) {

          Content tmpCnt = mSubTitleContentTable.get(name);

          String tmpVideoUri = cnt.getResUri();
          String tmpSubTitleUri = tmpCnt.getResUri();
          Log.d(TAG, " getSubTitleList tmpSubTitleUri = " + tmpSubTitleUri);

          tmpVideoUri = tmpVideoUri.substring(0, tmpVideoUri.lastIndexOf('/'));
          tmpSubTitleUri = tmpSubTitleUri.substring(0, tmpSubTitleUri.lastIndexOf('/'));

          Log.d(TAG,
              " getSubTitleList tmpVideoUri =" + tmpVideoUri + " tmpSubTitleUri = "
                  + tmpSubTitleUri
                  + " cnt.getMimeType() = " + cnt.getMimeType() + " tmpCnt.getMimeType() = "
                  + tmpCnt.getMimeType());

          if (tmpVideoUri.equals(tmpSubTitleUri)
              && (cnt.getMimeType()).equals(tmpCnt.getMimeType())) {
            subTitleList.add(tmpCnt.getResUri());

          }

        }

      }
      Log.d(TAG, " getSubTitleList mSubTitleContentTable size = " + mSubTitleContentTable.size());
      Log.d(TAG, " getSubTitleList size = " + subTitleList.size() + "filename = " + fileName
          + "  uri =" + cnt.getResUri());

      if (subTitleList.size() > 0) {
        for (String uri : subTitleList) {
          Log.d(TAG, " getSubTitleList uri = " + uri);

        }
      }
    } catch (Exception ex) {
      Log.d(TAG, " getSubTitleList Exception");

      ex.printStackTrace();
    }

    return subTitleList;

  }

  private String getFileName(String nameSuffix) {
    String path = nameSuffix;
    int index = path.lastIndexOf(".");
    if (index > 0) {
      path = path.substring(0, index);
    }
    return path;
  }

  /**
   * Call back function, implement for com.mediatek.dlna.jar.
   * Application should not use this API.
   *
   * @return.
   */
  private void cancelNotify() {
    if (localLOGV) {
      NetLog.d(TAG, "cancelNotify mBrowseHandle:" + mBrowseHandle + "  REQUEST_MATCH_NUM:"
          + REQUEST_MATCH_NUM + "  mTotalNum:" + mTotalNum);
    }
//    if (mBrowseHandle > 0 && REQUEST_MATCH_NUM > BROWSE_REQUEST &&
//        REQUEST_MATCH_NUM < mTotalNum) {
    if (mBrowseHandle != -1 && server != null) {
      server.setActionEventListener(null);
      server.cancel(mBrowseHandle);
      server = null;
      mBrowseHandle = -1;
    }
//    }
  }

  /**
   * Call back function, implement for com.mediatek.dlna.jar.
   * Application should not use this API.
   *
   * @return.
   */
  public void notifyContentFailed(FailedContentEvent event) {
    if (localLOGV)
      NetLog.d(TAG, "notifyContentFailed event:" + event + "  mCurPath:" + mCurPath);
    if (listener != null) {
      listener.onFileFailed(new FileEvent(event));
    }
  }

  /**
   * Set listener for DLNA manager.
   * Application should call this at first.
   *
   * @return.
   */
  public void setOnFileEventListener(FileEventListener listener) {
    this.listener = listener;
  }

  // class DLNADataSource {
  // Content content;
  // public DLNADataSource(Content content) {
  // this.content = content;
  // }
  // public java.io.InputStream newInputStream() {
  // return new com.mediatek.dlna.object.ContentInputStream(content);
  // }
  // }

  /**
   * Get the input stream of the input file.
   *
   * @return The InputStream of the input file.
   */
  public InputStream getFileStream(String name) {
    String fileName = getFileName(name);
    if (localLOGV) {
      NetLog.d(TAG, "getFileStream fileName:" + fileName);
    }
    Content cnt = mNameContentTable.get(fileName);
    if (cnt == null)
      return null;
    return new DLNAInputStream(cnt);
  }

  /**
   * Get the DLNA content suffix  from filename.
   *
   * @return The InputStream of the input file.
   */
  public String getDLNAFileSuffix(String name) {
    String fileName = getFileName(name);
    String suffix = "";
    Content cnt = mNameContentTable.get(fileName);
    if (cnt != null) {
      DLNAFile file = new DLNAFile(cnt, null, null);
      suffix = file.getSuffix();
    }
    if (localLOGV) {
      NetLog.d(TAG, "getDLNAFileSuffix suffix:" + suffix);
    }
    return suffix;

  }

  /**
   * Get the data source of the input file.
   *
   * @param fileName
   * @return The DLNADataSource of the input file which provide method to
   *         create corresponding instance of class InputStream.
   */
  public DLNADataSource getDLNADataSource(String name) {
    String fileName = getFileName(name);
    if (localLOGV) {
      NetLog.d(TAG, "getDLNADataSource fileName:" + fileName);
    }
    Content cnt = mNameContentTable.get(fileName);
    if (cnt == null) {
      return null;
    }
    return new DLNADataSource(cnt);
  }

  public DLNADataSource getDLNADataSource(Content cnt) {
    if (cnt == null){
      return null;
    }
    return new DLNADataSource(cnt);
  }

  public String getDevice(String path) {
    if (path == null || path.length() <= 1) {
      return null;
    }
    int end = path.indexOf(0x2f, 1);
    if (end <= 0) {
      return path.substring(1);
    }
    return path.substring(1, end);
  }

  public String getObjectId() {
//    if (localLOGV) {
//      NetLog.d(TAG, "getObjectId currentID:" + currentID);
//    }
    return currentID;
  }

  private String retriveName(String path) {
    if (path == null) {
      return null;
    }

    String name = path.substring(path.lastIndexOf("/") + 1, path.length());
    MtkLog.d(TAG, "Dlnamanager RetriveName : " + name);

    return name;
  }

  public void resetDlna() {
    mCurPath = mRoot;
    mListDLNADeviceName.clear();
    mNameDeviceTable.clear();

    if (player != null) {
      // new Exception("stopdlna").printStackTrace();
      int result = player.stop();
      if (localLOGV) {
        NetLog.d(TAG, "resetDlna player.stop result:" + result);
      }
    }

    if (player != null) {
      // new Exception("startdlna").printStackTrace();
      int result = player.start();
      if (localLOGV) {
        NetLog.d(TAG, "resetDlna player.start result:" + result);
      }
    }
    isStartOnce = true;

  }

  public void createDlnaFile(String name) {
    try {
      File f = new File("/tmp/mtk/dlna_success.txt");
      if (!f.exists() && name != null && name.toLowerCase().startsWith("goldendms")) {
        f.createNewFile();
      }
    } catch (Exception e) {
      if (localLOGV) {
        NetLog.d(TAG, "notifyDeviceFound create file exception:");
      }
    }
  }

  public void deleteDlnaFile(String name) {
    try {
      File f = new File("/tmp/mtk/dlna_success.txt");
      if (f.exists() && name != null && name.toLowerCase().startsWith("goldendms")) {
        f.delete();
      }
    } catch (Exception e) {
      if (localLOGV) {
        NetLog.d(TAG, "notifyDeviceFound delete file exception:");
      }
    }
  }

}
