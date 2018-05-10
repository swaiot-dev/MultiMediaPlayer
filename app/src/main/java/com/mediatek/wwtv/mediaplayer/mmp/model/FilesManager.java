
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;

import android.content.Context;
import android.util.Log;

import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.Lists;

public abstract class FilesManager<T extends FileAdapter> extends Observable {
  private static final String TAG = "FilesManager";

  public static final int SORT_BY_NAME = 0;
  public static final int SORT_BY_DATE = 1;
  public static final int SORT_BY_ALBUM = 2;
  public static final int SORT_BY_ARTIST = 3;
  public static final int SORT_BY_GENRE = 4;
  public static final int SORT_BY_TYPE = 5;

  public static final int CONTENT_ALL = -1;
  public static final int CONTENT_VIDEO = 0;
  public static final int CONTENT_PHOTO = 1;
  public static final int CONTENT_AUDIO = 2;
  public static final int CONTENT_TEXT = 3;
  public static final int CONTENT_THRDPHOTO = 4;

  public static final int REQUEST_REFRESH = 1;
  public static final int REQUEST_LOGIN = 2;
  public static final int REQUEST_BACK_TO_ROOT = 3;

  public static final int REQUEST_BACK_DEVICE_LEFT = 4;
  public static final int REQUEST_DEVICE_LEFT = 5;
  public static final int REQUEST_SUB_DIRECTORY = 6;
  public static final int REQUEST_MMP_MENU = 7;
  public static final int REQUEST_SOURCE_CHANGED = 8;
  public static final int REQUEST_BACK_TO_NO_DEVICES = 9;//add by yangxiong for solving "show no device after usb is ejecting"

  public static final int INVALID_INDEX = -1;

  protected Context mContext;

  protected List<T> mFiles;
  protected List<T> mVideoFiles;
  protected List<T> mAudioFiles;
  protected List<T> mPhotoFiles;
  protected List<T> mDevices;

  protected String mRootPath;
  protected String mCurrentPath;
  protected String mParentPath;

  protected int mContentType;
  protected int mSortType;

  protected boolean mReturnParentRefresh;
  protected int mPositionInParent;
  private final Stack<Integer> mOpenedHistory;
  public Stack<String> mHistory;// = Lists.newStack();

  protected FilesManager(Context context) {
    mContext = context;

    mFiles = Lists.newArrayList();
    mVideoFiles = Lists.newArrayList();
    mAudioFiles = Lists.newArrayList();
    mPhotoFiles = Lists.newArrayList();
    mDevices = Lists.newArrayList();

    mRootPath = null;
    mCurrentPath = null;
    mParentPath = null;

    mContentType = CONTENT_ALL;
    mSortType = SORT_BY_NAME;

    mPositionInParent = INVALID_INDEX;
    mOpenedHistory = Lists.newStack();
    mHistory = Lists.newStack();
  }

  public List<T> getCurrentFiles() {
    synchronized (mFiles) {
      return mFiles;
    }
  }

  public void setRootPath(String path) {
    mRootPath = path;
    MtkLog.d(TAG, "Root Path : " + mRootPath);
  }

  public String getRootPath() {
    return mRootPath;
  }

  public void setCurrentPath(String path) {
    mCurrentPath = path;

    if (mCurrentPath == null || mCurrentPath.length() <= 0
        || mCurrentPath.equals(mRootPath)) {
      mCurrentPath = mRootPath;
      mParentPath = null;
    } else {
      mParentPath = retriveParentPath(path);
    }

    MtkLog.d(TAG, "Current Path : " + mCurrentPath);
    MtkLog.d(TAG, "Parent Path : " + mParentPath);
  }

  public String getCurrentPath() {
    return mCurrentPath;
  }

  public String getParentPath() {
    return mParentPath;
  }

  public String getBackPath(String curPath) {
    String path = curPath;

    if (path == null)
      return null;

    int index = path.lastIndexOf("/");
    if (index > 0)
    {
      path = path.substring(0, index);
    }
    return path;
  }

  public String getBackPath()
  {
    String path = getCurrentPath();

    if ("/mnt".equals(path))
    {
      return null;
    }

    if (mRootPath.equals(path) || "/mnt/usb".equals(path))
    {
      return "/mnt";
    }
    int index = path.lastIndexOf("/");
    if (index > 0)
    {
      path = path.substring(0, index);
    }
    return path;
  }

  public void setContentType(int contenType) {
    MtkLog.d(TAG, "Content Type : " + contenType);
    if (contenType > INVALID_INDEX) {
      mContentType = contenType;
      // MediaMainActivity.mSelection=mContentType;
    }
  }

  public int getContentType() {
    return mContentType;
  }

  public int getContentType(String path) {
    return CONTENT_ALL;
  }

  public void setSortType(int sortType) {
    mSortType = sortType;
    MtkLog.d(TAG, "Sort Type : " + mSortType);
  }

  public int getSortType() {
    return mSortType;
  }

  public void setPositionInParent(int position) {
    if (position > INVALID_INDEX) {
      mPositionInParent = position;
    } else {
      mPositionInParent = 0;
    }
    MtkLog.d(TAG, "set Current Selection : " + mPositionInParent);
  }

  public int getPositionInParent() {
    MtkLog.d(TAG, "get Current Selection : " + mPositionInParent);
    return mPositionInParent;
  }

  public void setRefresh(boolean refresh) {
    mReturnParentRefresh = refresh;
  }

  public boolean isRefresh() {
    return mReturnParentRefresh;
  }

  public int getFilesCount() {
//    MtkLog.d(TAG, "Files Count : " + mFiles.size());
    return mFiles.size();
  }

  public T getFile(int postion) {
    if (postion >= 0 && postion < mFiles.size()) {
      return mFiles.get(postion);
    }

    return null;
  }

  public void pushOpenedHistory(int position) {
    mOpenedHistory.push(position);
  }

  public int popOpenedHistoryRoot() {
    while (mOpenedHistory.size() > 1) {
      mOpenedHistory.pop();
    }

    return popOpenedHistory();
  }

  public int popOpenedHistory() {
    if (mOpenedHistory.size() == 0) {
      return 0;
    }

    return mOpenedHistory.pop();
  }

  public boolean canPaste(String file) {
    if (mCurrentPath == null) {
      return false;
    }
    return !mCurrentPath.equals(retriveParentPath(file));
  }

  public boolean isInSameFolder(String path1, String path2) {
    return retriveParentPath(path1).equals(retriveParentPath(path2));
  }

  protected String retriveParentPath(String path) {
    int index = path.lastIndexOf("/");
    String name = path.substring(index + 1, path.length());
    String parent = path.substring(0, index);
    /*
     * String parent = ""; if(mHistory.size() == 0){ return parent; }else{ StringBuilder tmp = new
     * StringBuilder("/"+mHistory.get(0)); StringBuilder current = new
     * StringBuilder("/"+mHistory.get(0)); int i = 1; for(;i < mHistory.size();i++){
     * current.append("/").append(mHistory.get(i));
     * MtkLog.i(TAG,"retriveParentPath path:"+path+"--current:"+current);
     * if(current.toString().equals(path)){ return tmp.toString(); }
     * tmp.append("/").append(mHistory.get(i)); } if(path.equals(current.toString())&&i==1){ return
     * ""; } if(path.contains(current)){ return current.toString(); } }/* /* int size =
     * mFiles.size(); if(size>0){ if(mFiles.get(size-1).getAbsolutePath().contains(path)){
     * if(mHistory.size() > 1){ StringBuilder tmp = new StringBuilder("/"+mHistory.get(0));
     * StringBuilder current = new StringBuilder("/"+mHistory.get(0)+"/"+mHistory.get(1)); for(int i
     * = 0;i < mHistory.size();i++){ if(current.equals(path)){ return tmp.toString(); } } }else{
     * return ""; } } }else{ boolean isExsit = true; int i = 0; String name = ""; String
     * absolutename = ""; while(isExsit){ for(;i<mFiles.size();i++){ name = mFiles.get(i).getName();
     * absolutename = mFiles.get(i).getAbsolutePath();
     * Log.i(TAG,"----retriveParentPath name:"+name+"  absolutename:"+absolutename);
     * if(filename.equals(name)&&path.equals(absolutename)){ isExsit = false; break; } }
     * if(isExsit){ index = parnet.lastIndexOf('/'); if(index > 0){ filename =
     * parnet.substring(index+1)+"/"+filename; parnet = parnet.substring(0, index); i = 0;
     * Log.i(TAG,"----retriveParentPath filename:"+filename+"  parnet:"+parnet); }else{
     * Log.i(TAG,"----retriveParentPath index <=0"); isExsit = false; } } } } MtkLog.d(TAG,
     * "RetriveParentPath : " + parnet);
     */
    return parent;
  }

  protected List<T> wrapFiles(List<?> originalFiles) {
    List<T> wrapedFiles = Lists.newArrayList();
    if (originalFiles != null) {
      for (Object file : originalFiles) {
        T wrapedFile = newWrapFile(file);
        if (null != wrapedFile) {
          wrapedFiles.add(wrapedFile);
        }
      }
    }

    return wrapedFiles;
  }

  protected T newWrapFile(Object originalFile) {
    return null;
  }

  public abstract List<T> listAllFiles(String path);

  protected void logFiles(String tag) {
//    logFiles(tag, mFiles);
  }

  protected void logFiles(String tag, List<T> files) {
    if (files != null) {
      for (FileAdapter file : files) {
        MtkLog.d(tag, "File : " + file.getAbsolutePath());
      }
    }
  }

  public abstract List<T> listRecursiveFiles(int contentType);

  public abstract void destroy();

  public abstract void destroyManager();
}
