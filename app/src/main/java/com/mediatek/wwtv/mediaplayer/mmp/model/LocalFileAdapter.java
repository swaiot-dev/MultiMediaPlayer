
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import java.io.FileInputStream;

import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.AudioFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.PhotoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;
import com.mediatek.wwtv.util.MtkLog;

public class LocalFileAdapter extends FileAdapter {
  private static final String TAG = "LocalFileAdapter";
  private final MtkFile mFile;
  private String mName;
  private static UsbFileOperater sOperator = UsbFileOperater.getInstance();
  private String mLabel;

  public LocalFileAdapter(MtkFile file) {
    mFile = file;
  }

  public LocalFileAdapter(String path) {
    mFile = new MtkFile(path);
  }

  public LocalFileAdapter(String path, String name) {
    mFile = new MtkFile(path);
    mName = name;
  }

  public LocalFileAdapter(String path, String name, String label) {
    mFile = new MtkFile(path);
    mName = name;
    mLabel = label;
  }

  public LocalFileAdapter(File file) {
    mFile = new MtkFile(file);
  }

  @Override
  public void stopDecode() {
    if (null != mFile && mFile instanceof PhotoFile) {
      MtkLog.i(TAG, " Bitmap  stopDecode --------------");
      ((PhotoFile) mFile).stopDecode();
    }
  }

  @Override
  public boolean isPhotoFile() {
    return mFile.isPhotoFile();
  }

  @Override
  public boolean isAudioFile() {
    return mFile.isAudioFile();
  }

  @Override
  public boolean isVideoFile() {
    return mFile.isVideoFile();
  }

  @Override
  public boolean isTextFile() {
    return mFile.isTextFile();
  }

  // add by sky luojie begin
  public boolean isMoreItem() {
    return true;
  }
  // add by sky luojie end

  @Override
  public String getSize() {
    return mFile.getSize();
  }

  @Override
  public String getTextSize() {
    return getTextSize(mFile.getFileSize());
  }

  @Override
  public String getAbsolutePath() {
    return mFile.getAbsolutePath();
  }

  @Override
  public String getPath() {
    return mFile.getPath();
  }

  @Override
  public String getDeviceName() {
    if (mName != null && mName.length() > 0) {
      return mName;
    }

    return mFile.getName();

  }

  @Override
  public String getName() {

    if (mLabel != null && mLabel.length() > 0) {
      return mLabel;
    }

    if (mFile != null) {
        String path = mFile.getPath();
        String rootPath = mFile.getParent();
        if (path != null && path.length()>0) {
            MtkLog.i(TAG, " path:" + path + " rootPath:" + rootPath);
            if (rootPath != null && rootPath.equals("/storage")) {
                int rootIndex = path.indexOf("/storage/");
                if (rootIndex >= 0) {
                    path = path.substring(rootIndex + 9, path.length());
                    return path;
                }
            } else {
                if (mName != null && mName.length()>0) {
                    return mName;
                }
            }
        }
    } else {
        if (mName != null && mName.length()>0) {
            return mName;
        }
    }

    return mFile.getName();
  }

  public String getFileName() {
    if (null != mFile){
        return mFile.getName();
    } else {
        return getDeviceName();
    }
  }

  @Override
  public Bitmap getThumbnail(int width, int height, boolean isThumbnail) {
    if ((isPhotoFile() || isThrdPhotoFile()) && !isValidSizePhoto()) {
      return null;
    }
    return mFile.getThumbnail(width, height, isThumbnail);
  }

  @Override
  public boolean isDirectory() {
    return mFile.isDirectory();
  }

  @Override
  public boolean isFile() {
    return mFile.isFile();
  }

  @Override
  public long lastModified() {
    return mFile.lastModified();
  }

  @Override
  public void stopThumbnail() {

    mFile.stopThumbnail();
  }

  @Override
  public long length() {
    return mFile.length();
  }

  @Override
  public boolean delete() {
    try {
      sOperator.addFileToDeleteList(mFile);
    } catch (IOException e) {
      e.printStackTrace();
    }
    sOperator.deleteFiles();

    return true;
  }

  @Override
  public String getInfo() {
    return getInfoByCache(false);
  }

  private String getInfoByCache(boolean isCache) {
    String info = "";
    MetaData data = null;
    if (isPhotoFile() || isThrdPhotoFile()) {
      if (isValidSizePhoto()) {
        info = assemblyInfos(getName(), getResolution(), getSize());

      } else {
        info = assemblyInfos(getName(), "", getSize());

      }

    } else if (isAudioFile() && (mFile instanceof AudioFile)) {
      AudioFile file = (AudioFile) mFile;
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else if(file != null) {
        data = file.getMetaDataInfo();
      }

      if (null == data) {

        info = assemblyInfos(getName(), "", "", getSize());
      } else {
        //add by y.wan for bug 93597 start 2018/8/10
        //String title = data.getTitle();
        String title = getName();
        //add by y.wan for  end 2018/8/10

        if (title == null || title.length() <= 0) {
          title = getName();
        }
        info = assemblyInfos(title, data.getAlbum(), data.getGenre(), data
            .getYear(), getSize());
      }

    } else if (isVideoFile() && (mFile instanceof VideoFile)) {
      VideoFile file = (VideoFile) mFile;
      if (isCache) {
        data = Info.getCacheMetaData();
        if (null == data) {
          return null;
        }
      } else if (file != null) {
        data = file.getMetaDataInfo();
      }

      if (null == data) {

        info = assemblyInfos(getName(), "", "", getSize());
      } else {

        String title = data.getTitle();
        if (title == null || title.length() <= 0) {
          title = getName();
        }
        int dur = data.getDuration();
        MtkLog.i(TAG, "$$$$$$$$$$$$time = " + dur);
        if (getName() != null && getName().toLowerCase().endsWith(".pvr")) {
          info = assemblyInfos(title, data.getYear(), "", getSize());
        } else {
          info = assemblyInfos(title, data.getYear(), setTime(dur), getSize());
        }

      }

    } else if (isTextFile()) {
      info = assemblyInfos(getName(), getLastModified(), getTextSize());
    }

    return info;
  }

  @Override
  public String getPreviewBuf() {
    MtkLog.d(TAG, "getPreviewBuf getAbsolutePath =" + getAbsolutePath());
    try {
      FileInputStream fileIS = new FileInputStream(getAbsolutePath());
      return super.getPreviewBuf(fileIS);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return "";

  }

  @Override
  public String getResolution() {
    // String resolution = null;
    // if (isPhotoFile()) {
    // resolution = mFile.getResolution();
    // }

    return mFile.getResolution();
  }

  @Override
  public String getSuffix() {
    return "";
  }

  @Override
  public String getCacheInfo() {

    return getInfoByCache(true);
  }
}
