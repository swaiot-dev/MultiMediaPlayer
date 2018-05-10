
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbException;
import android.content.Context;
import android.util.Log;

import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioInfo;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoInfo;
import com.mediatek.wwtv.mediaplayer.netcm.samba.LoginInfo;
import com.mediatek.wwtv.mediaplayer.netcm.samba.SambaManager;

public class SmbFilesManager extends FilesManager<FileAdapter> {
  private static final String TAG = "SmbFilesManager";

  private static SmbFilesManager sInstance;
  private final SambaManager mOperator;
  private int mLoginCount;
  private AudioInfo mAudioInfo;
  private VideoInfo mFileInfo;

  private SmbFilesManager(Context context) {
    super(context);

    mOperator = SambaManager.getInstance();
    mOperator.initSamba();

    mAudioInfo = AudioInfo.getInstance();
    mFileInfo = VideoInfo.getInstance();
  }

  public static SmbFilesManager getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new SmbFilesManager(context);
    }

    return sInstance;
  }

  public boolean login(String path, String userName, String userPwd) {
    boolean success = false;
    try {
      SambaManager.login(path, userName, userPwd);
      success = true;
    } catch (SmbAuthException e) {
      MtkLog.w(TAG, "Login SmbAuthException!!");
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (SmbException e) {
      e.printStackTrace();
    }

    return success;
  }

  public boolean login(String path, String domain, String userName, String userPwd) {
    boolean success = false;
    try {
      SambaManager.login(path, domain, userName, userPwd);
      success = true;
    } catch (SmbAuthException e) {
      MtkLog.w(TAG, "Login SmbAuthException!!");
      e.printStackTrace();
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (SmbException e) {
      e.printStackTrace();
    }

    return success;
  }

  private boolean isSmbRoute(String path) {
    if (path == null) {
      return false;
    }

    if (path.equals("smb://")) {
      return true;
    }

    return false;
  }

  @Override
  public List<FileAdapter> listAllFiles(String path) {
    if (MediaMainActivity.mIsSambaAutoTest) {
      Log.d(TAG, "------listAllFiles Files : " + path + "  mContentType:" + mContentType);
    } else {
      MtkLog.d(TAG, "------listAllFiles Files : " + path + "  mContentType:" + mContentType);
    }

    try {
      mFiles.clear();
      int type = SambaManager.TYPE_ALL;

      switch (mContentType) {
        case CONTENT_PHOTO:
          type = SambaManager.TYPE_IMAGE;
          break;
        case CONTENT_AUDIO:
          type = SambaManager.TYPE_AUDIO;
          break;
        case CONTENT_VIDEO:
          type = SambaManager.TYPE_VIDEO;
          break;
        case CONTENT_TEXT:
          type = SambaManager.TYPE_TEXT;
          break;
        case CONTENT_THRDPHOTO:
          type = SambaManager.TYPE_THRIMAGE;
          break;
        default:
          break;
      }
      if (MediaMainActivity.mIsSambaAutoTest) {
        Log.d(TAG, "------listAllFiles getSmbFileList11 : ");
      } else {
        MtkLog.d(TAG, "------listAllFiles getSmbFileList11 : ");
      }
      LinkedList<String> filePaths = mOperator.getSmbFileList(path, type);
      if (MediaMainActivity.mIsSambaAutoTest) {
        Log.d(TAG, "------listAllFiles getSmbFileList22 : ");
      } else {
        MtkLog.d(TAG, "------listAllFiles getSmbFileList22 : ");
      }
      filePaths = mOperator.sortByName(filePaths);
      if (MediaMainActivity.mIsSambaAutoTest) {
        Log.d(TAG, "------listAllFiles getSmbFileList33 : ");
      } else {
        MtkLog.d(TAG, "------listAllFiles getSmbFileList33 : ");
      }
      mFiles = wrapFiles(filePaths);
      if (MediaMainActivity.mIsSambaAutoTest) {
        Log.d(TAG, "------listAllFiles getSmbFileList44 : ");
      } else {
        MtkLog.d(TAG, "------listAllFiles getSmbFileList44 : ");
      }
      logFiles(TAG);

    } catch (Exception e) {
      e.printStackTrace();
      if (MediaMainActivity.mIsSambaAutoTest) {
        Log.d(TAG, "Try to connect to login : " + mLoginCount);
      } else {
        MtkLog.d(TAG, "Try to connect to login : " + mLoginCount);
      }
      if (mLoginCount == 0) {
        if (MediaMainActivity.mIsSambaAutoTest) {
          Log.d(TAG, "Try to connect to login : ?  GUEST  '' ");
        } else {
          MtkLog.d(TAG, "Try to connect to login : ?  GUEST  '' ");
        }
        if (path != null && !path.equalsIgnoreCase("smb://")
            && !path.equalsIgnoreCase("smb://GCN/")) {
          LoginInfo loginInfo = mOperator.getServerInfo(path);
          if (loginInfo != null) {//guest or other user
            mLoginCount = 0;
            if (e instanceof SmbAuthException) {
              setChanged();
              notifyObservers(REQUEST_LOGIN);
              if (MediaMainActivity.mIsSambaAutoTest) {
                Log.d(TAG, " --- 00User name and Password is not correct. ");
              } else {
                MtkLog.d(TAG, " --- 00User name and Password is not correct. ");
              }
            } else {
              Log.d(TAG, " --- other exception. no need show pwd. ");
            }
          } else {
            login(path, "?", "GUEST", "");
            mLoginCount++;
            listAllFiles(path);
          }
//        } else {
//          login(path, "?", "GUEST", "");
//          mLoginCount++;
//          listAllFiles(path);
        }
      } else if (mLoginCount == 1) {
        if (MediaMainActivity.mIsSambaAutoTest) {
          Log.d(TAG, "Try to connect to login : null null null");
        } else {
          MtkLog.d(TAG, "Try to connect to login : null null null");
        }
        LoginInfo loginInfo = mOperator.removePwd(path);
        mLoginCount = 0;
        if (e instanceof SmbAuthException) {
          setChanged();
          notifyObservers(REQUEST_LOGIN);
          if (MediaMainActivity.mIsSambaAutoTest) {
            Log.d(TAG, " --- 00User name and Password is not correct. ");
          } else {
            MtkLog.d(TAG, " --- 00User name and Password is not correct. ");
          }
        } else {
          Log.d(TAG, " --- other exception. no need show pwd. ");
        }
//        login(path, null, null, null);
//        mLoginCount++;
//        listAllFiles(path);
      }
//      else if (mLoginCount == 2) {
//
//        mLoginCount = 0;
//        if (e instanceof SmbAuthException) {
//          if (!isSmbRoute(path)) {
//            setChanged();
//            notifyObservers(REQUEST_LOGIN);
//            if (MediaMainActivity.mIsSambaAutoTest) {
//              Log.d(TAG, " --- User name and Password is not correct. ");
//            } else {
//              MtkLog.d(TAG, " --- User name and Password is not correct. ");
//            }
//          } else {
//            Log.d(TAG, " --- is root path, no need show pwd. ");
//          }
//        } else {
//          Log.d(TAG, " --- other exception. no need show pwd. ");
//        }
//      }
    }
    System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    sortFile();
    return mFiles;
  }

  @Override
  protected String retriveParentPath(String path) {
    String parent;
    String newPath = path.substring(0, path.length() - 1);

    if (newPath.indexOf("/") + 1 == newPath.lastIndexOf("/")) {
      parent = newPath.substring(0, newPath.lastIndexOf("/") + 1);
    } else {
      parent = super.retriveParentPath(newPath) + "/";
    }

    MtkLog.d(TAG, "RetriveParentPath : " + parent);
    return parent;
  }

  @Override
  protected FileAdapter newWrapFile(Object originalFile) {
    String path = (String) originalFile;
    boolean isDir = false, isFile = false;
    long fileLength = 0L;
    try {
      isDir = SambaManager.getInstance().isDir(path);
      MtkLog.d(TAG, "newWrapFile  path: " + path + "  isDir:" + isDir);
//      isFile = SambaManager.getInstance().isFile(path);
      if (!isDir) {
        fileLength = SambaManager.getInstance().size(path);
      }
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (SmbException e) {
      e.printStackTrace();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return new SmbFileAdapter(path, mAudioInfo, mFileInfo, isDir, !isDir, fileLength);
  }

  public interface AuthListener {
    void onNeedAuth();
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
    if (mAudioInfo != null) {
      mAudioInfo.destroyInfo();
      mAudioInfo = null;
    }
    if (mFileInfo != null) {
      mFileInfo.destroyInfo();
      mFileInfo = null;
    }
    sInstance = null;
  }

  protected void sortFile() {
    MtkLog.i("sort", "sortFile");
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
