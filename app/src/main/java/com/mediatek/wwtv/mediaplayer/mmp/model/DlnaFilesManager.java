
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.os.SystemProperties;
import android.util.Log;

import com.mediatek.dlna.object.DLNADevice;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoInfo;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAFile;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAManager;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileEvent;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.FileEventListener;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.Lists;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.util.MtkLog;

public class DlnaFilesManager extends FilesManager<FileAdapter> {
  private static final String TAG = "DlnaFilesManager";
  private static DlnaFilesManager sInstance;
  private final DLNAManager mOperator;

  // private Stack<String> mHistory;
  private String mTempParent;
  private AudioInfo mAudioInfo;
  private VideoInfo mFileInfo;

  protected DlnaFilesManager(Context context) {
    super(context);
    // mHistory = Lists.newStack();
    mOperator = DLNAManager.getInstance();
    mOperator.setOnFileEventListener(new FileEventListener() {
      @Override
      public void onFileLeft(FileEvent event) {
        MtkLog.i(TAG, "OnFileLeft");
        String currentDeviceName;
        LinkedList<DLNAFile> files = event.getFileList(getType());
        if (MediaMainActivity.mIsDlnaAutoTest) {
          Log.d(TAG, "onFileLeft size: " + files.size());
          for (DLNAFile file : files) {
            Log.d(TAG, "OnFileLeft : " + file.getPath());
          }
        } else {
          for (DLNAFile file : files) {
            MtkLog.d(TAG, "OnFileLeft : " + file.getPath());
          }
        }
        int sourceType = MultiFilesManager.getInstance(mContext)
            .getCurrentSourceType();
        if (MediaMainActivity.mIsDlnaAutoTest) {
          Log.d(TAG, "onFileLeft sourceType: " + sourceType);
        }
        if (sourceType == MultiFilesManager.SOURCE_ALL) {
          onFileFound(event);
        } else if (sourceType == MultiFilesManager.SOURCE_DLNA) {
          DLNADevice device = event.getLeftDevice();
          if (null != device) {
            String leftDeviceName = device.getName();
            MtkLog
                .i(TAG, " DLNA device  leave  :"
                    + leftDeviceName);
            synchronized (mFiles) {
              if (mFiles.size() > 0) {
                FileAdapter currentFile = mFiles.get(0);

                MtkLog.i(TAG, "  Current file  path  :"
                    + currentFile.getPath());

                if (currentFile.isDevice()) {
                  currentDeviceName = currentFile
                      .getDeviceName();
                } else {
                  currentDeviceName = mOperator
                      .getDevice(currentFile.getPath());
                }
                MtkLog.i(TAG, "  Current DLNA device  name  :"
                    + currentDeviceName);
                if (currentFile.getDeviceName().equals(
                    leftDeviceName)) {
                  // TODO leave
                  MtkLog.i(TAG, "goto  the root dic");
                  addFiles(files, REQUEST_DEVICE_LEFT);
                } else {
                  addFiles(files, REQUEST_BACK_DEVICE_LEFT);
                }
              } else {
                currentDeviceName = mOperator
                    .getDevice(getCurrentPath());
                DLNADevice device1 = event.getLeftDevice();
                if (currentDeviceName == null
                    || (null != device1 && currentDeviceName.equals(device1.getName()))) {
                  addFiles(files, REQUEST_DEVICE_LEFT);
                } else {
                  addFiles(files, REQUEST_BACK_DEVICE_LEFT);
                }
              }

            }
          }
        }
      }

      @Override
      public void onFileFound(FileEvent event) {
        MtkLog.i(TAG, "OnFileFound");
        LinkedList<DLNAFile> files = new LinkedList<DLNAFile>();
        LinkedList<DLNAFile> filestemp = new LinkedList<DLNAFile>();
        // files.addAll(event.getFileList(getType()));
        filestemp.addAll(event.getFileList(getType()));
        for (DLNAFile file : filestemp) {
//          if (MediaMainActivity.mIsDlnaAutoTest) {
//            Log.d(TAG, "onFileFound : " + file.getName());
//          } else {
//            MtkLog.d(TAG, "OnFileFound : " + file.getName());
//          }
          if (!files.contains(file)) {
            files.add(file);
          }
        }

        List<FileAdapter> wrapedFiles = wrapFiles(files);
        synchronized (mFiles) {
          mFiles.clear();
          mFiles.addAll(wrapedFiles);
          mFiles = Collections.synchronizedList(mFiles);
          sortFile();
          MtkLog.d(TAG, "OnFileFound mFiles size : " + mFiles.size());
          setChanged();
          notifyObservers(REQUEST_REFRESH);
        }
      }

      @Override
      public void onFileFailed(FileEvent event) {
        if (MediaMainActivity.mIsDlnaAutoTest) {
          Log.i(TAG, "onFileFailed");
        } else {
          MtkLog.i(TAG, "OnFileFailed");
        }
        setChanged();
        notifyObservers(REQUEST_REFRESH);
      }
    });

    mAudioInfo = AudioInfo.getInstance();
    mFileInfo = VideoInfo.getInstance();
  }

  private void addFiles(LinkedList<DLNAFile> files, int flag) {
    if (files == null) {
      return;
    }
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "addFiles REQUEST_LEFT files:" + files.size() + "  flag:" + flag);
    } else {
      MtkLog.d(TAG, "addFiles REQUEST_LEFT files:" + files.size() + "  flag:" + flag);
    }
    if (flag == REQUEST_BACK_DEVICE_LEFT) {
      List<FileAdapter> wrapedFiles = wrapFiles(files);
      synchronized (mDevices) {
        mDevices.clear();
        mDevices.addAll(wrapedFiles);

        setChanged();
        notifyObservers(REQUEST_BACK_DEVICE_LEFT);
      }

    } else if (flag == REQUEST_DEVICE_LEFT) {
      mOperator.setCurrentParserPath("/");
      mHistory.clear();
      List<FileAdapter> wrapedFiles = wrapFiles(files);
      synchronized (mFiles) {
        mFiles.clear();
        mFiles.addAll(wrapedFiles);

        setChanged();
        notifyObservers(REQUEST_DEVICE_LEFT);
      }

    }
  }

  private int getType() {
    int type = FileEvent.FILTER_TYPE_ALL;
    switch (mContentType) {
      case CONTENT_PHOTO:
        type = FileEvent.FILTER_TYPE_IMAGE;
        break;
      case CONTENT_AUDIO:
        type = FileEvent.FILTER_TYPE_AUDIO;
        break;
      case CONTENT_VIDEO:
        type = FileEvent.FILTER_TYPE_VIDEO;
        break;
      case CONTENT_TEXT:
        type = FileEvent.FILTER_TYPE_TEXT;
        break;
      default:
        break;
    }
    return type;
  }

  public static DlnaFilesManager getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new DlnaFilesManager(context);
    }

    return sInstance;
  }

  @Override
  public void setCurrentPath(String path) {
    MtkLog.i(TAG, "path:" + path + " mParentPath:" + mParentPath);
    mTempParent = mParentPath;

    super.setCurrentPath(path);
  }

  public List<FileAdapter> getDevices() {
    synchronized (mDevices) {
      return mDevices;
    }
  }

  @Override
  public List<FileAdapter> listAllFiles(String path) {
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "listAllFiles Path : " + path + "  ParentPath : " + mTempParent);
    } else {
      MtkLog.i(TAG, "listAllFiles start");
      MtkLog.d(TAG, "List Path : " + path);
      MtkLog.d(TAG, "Parent Path : " + mTempParent);
    }

    synchronized (mFiles) {
      mFiles.clear();
      try {
        String name = retriveName(path);
        if (MediaMainActivity.mIsDlnaAutoTest) {
          Log.d(TAG, "listAllFiles Path : " + path + "  ParentPath : " + mTempParent);
        } else {
          MtkLog.d(TAG, "listAllFiles Path : " + path + "  ParentPath : " + mTempParent);
        }
        if (path != null && path.equals(mTempParent)) {
          mOperator.parseDLNAFile(path, name, false);
          if (!mHistory.empty()) {
            String popVal = mHistory.pop();
            MtkLog.d(TAG, "listAllFiles mHistory pop  : " + popVal);
          } else {
            mHistory.push(name);
            MtkLog.d(TAG, "listAllFiles mHistory is empty~~ ");

          }
        } else {
          MtkLog.i(TAG, " listAllFiles Into ");
          mOperator.parseDLNAFile(path, name, true);
          String absolute = retriveHistoryStack();

          MtkLog.d(TAG, "listAllFiles   absolute  : " + absolute);
          if (path != null && path.equals(absolute)) {

            MtkLog.d(TAG, "listAllFiles need not  push  : " + name);

          } else if (name != null) {
            mHistory.push(name);

            MtkLog.d(TAG, "listAllFiles mHistory push  : " + name);
          }

          mRootPath = "";
        }
      } catch (ExceptionInInitializerError e) {
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
      return mFiles;
    }
  }

  @Override
  protected FileAdapter newWrapFile(Object originalFile) {
    DLNAFile file = (DLNAFile) originalFile;
    if (file.getContent() != null
        && !(file.getContent().getParentId().equals(mOperator.getObjectId()))) {
      return null;
    }
    String name = file.getName();
    String absolutePath = "";
    if (file.isDevice()) {
      absolutePath = "/" + name;
    } else {
      absolutePath = retriveAbsolutePath(name);
    }

    DlnaFileAdapter tempAdapter = new DlnaFileAdapter(file, absolutePath, mAudioInfo, mFileInfo);
    if (tempAdapter.isTypeUnknown()) {
      return null;
    } else {
      return tempAdapter;
    }
  }

  private String retriveHistoryStack() {
    String absolute;
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < mHistory.size(); i++) {
      builder.append("/").append(mHistory.get(i));
    }

    absolute = builder.toString();
//    if (MediaMainActivity.mIsDlnaAutoTest) {
//      Log.d(TAG, "retriveHistoryStack : " + absolute);
//    } else {
//      MtkLog.d(TAG, "retriveHistoryStack : " + absolute);
//    }

    return absolute;

  }

  private String retriveAbsolutePath(String name) {
    String absolute = retriveHistoryStack() + "/" + name;
//    if (MediaMainActivity.mIsDlnaAutoTest) {
//      Log.d(TAG, "retriveAbsolutePath : " + absolute);
//    } else {
//      MtkLog.d(TAG, "RetriveAbsolutePath : " + absolute);
//    }

    return absolute;
  }

  private String retriveName(String path) {
    if (path == null) {
      return null;
    }
    /*
     * boolean isExsit = true; int index = path.lastIndexOf("/"); String name =
     * path.substring(index+ 1, path.length()); String parent = path.substring(0,index);
     * MtkLog.d(TAG, "retriveName mParentPath : " + mParentPath); MtkLog.d(TAG,
     * "retriveName name : " + name+" parent:"+parent); while(isExsit&&index > 0){
     * if(parent.equals(mParentPath)){ isExsit = false; }else{ index = parent.lastIndexOf("/"); name
     * = parent.substring(index+1)+"/"+name; parent = parent.substring(0,index); } MtkLog.d(TAG,
     * "retriveName name : " + name+" parent:"+parent); }
     */
    String parent = retriveParentPath(path);
    String name = path.substring(parent.length() + 1);
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "RetriveName : " + name);
    } else {
      MtkLog.d(TAG, "RetriveName : " + name);
    }

    return name;
  }

  @Override
  protected String retriveParentPath(String path) {
    String parent = "";
    if (mHistory.size() == 0) {
      return parent;
    } else {
      StringBuilder tmp = new StringBuilder("/" + mHistory.get(0));
      StringBuilder current = new StringBuilder("/" + mHistory.get(0));
      int i = 1;
      for (; i < mHistory.size(); i++) {
        current.append("/").append(mHistory.get(i));
        if (MediaMainActivity.mIsDlnaAutoTest) {
          Log.d(TAG, "retriveParentPath : " + path + "--current:" + current);
        } else {
          MtkLog.i(TAG, "retriveParentPath path:" + path + "--current:" + current);
        }
        if (current.toString().equals(path)) {
          return tmp.toString();
        }
        tmp.append("/").append(mHistory.get(i));
      }
      if (path.equals(current.toString()) && i == 1) {
        return "";
      }
      if (path.contains(current)) {
        return current.toString();
      }
    }
    return parent;
  }

  public void clearHistory() {
    mHistory.clear();

    MtkLog.d(TAG, "clearHistory ~ ");
  }

  @Override
  public void destroy() {
    destroyManager();
  }

  @Override
  public List<FileAdapter> listRecursiveFiles(int contentType) {
    return null;
  }

  @Override
  public void destroyManager() {
    try {
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.i(TAG, "destroyManager!!");
      } else {
        MtkLog.i(TAG, "destroyManager!!");
      }
      if (!LogicManager.getInstance(mContext).isMMPLocalSource()
          && VideoPlayActivity.getInstance() != null
          && VideoPlayActivity.getInstance().isInPictureInPictureMode()) {
        MtkLog.d(TAG, "destroy is pip, no need stop dlna");
        mOperator.destroy(true);
      } else {
        mOperator.destroy(false);
      }
      if (mAudioInfo != null) {
        mAudioInfo.destroyInfo();
        mAudioInfo = null;
      }
      if (mFileInfo != null) {
        mFileInfo.destroyInfo();
        mFileInfo = null;
      }
      sInstance = null;
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  protected void sortFile() {
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.i(TAG, "sortFile:" + MultiFilesManager.hasInstance());
    } else {
      MtkLog.i(TAG, "dlna sortFile:" + MultiFilesManager.hasInstance());
    }
    if (!MultiFilesManager.hasInstance()) {
      new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getName", null);
      return;
    }
    int type = MultiFilesManager.getInstance(mContext).getSortType();
    if (FilesManager.SORT_BY_NAME == type) {
      new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getName", null);
    } else if (FilesManager.SORT_BY_DATE == type) {
      int source = MultiFilesManager.getInstance(mContext).getCurrentSourceType();
      if (MultiFilesManager.SOURCE_DLNA != source) {
        new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getSize", null);
      }
    } else if (FilesManager.SORT_BY_TYPE == type) {
      new com.mediatek.wwtv.mediaplayer.mmp.util.SortList().sort(mFiles, "getSuffix", "getName");
    }

  }

}
