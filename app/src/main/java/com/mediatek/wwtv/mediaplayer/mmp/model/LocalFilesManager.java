
package com.mediatek.wwtv.mediaplayer.mmp.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.AudioFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.PhotoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.TextFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.ThrDPhotoFile;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.UsbFileOperater;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.VideoFile;

public class LocalFilesManager extends FilesManager<FileAdapter> {
  private static final String TAG = "LocalFilesManager";

  private static LocalFilesManager sInstance;
  private final UsbFileOperater mOperator;

  private LocalFilesManager(Context context) {
    super(context);

    mOperator = UsbFileOperater.getInstance();
    setSortType(SORT_BY_NAME);
  }

  public static LocalFilesManager getInstance(Context context) {
    if (sInstance == null) {
      sInstance = new LocalFilesManager(context);
    }

    return sInstance;
  }

  @Override
  public void setSortType(int sortType) {
    switch (sortType) {
      case SORT_BY_NAME:
        mSortType = FileConst.SORT_NAME;
        break;
      case SORT_BY_DATE:
        mSortType = FileConst.SORT_DATE;
        break;
      case SORT_BY_ALBUM:
        mSortType = FileConst.SORT_ALBUM;
        break;
      case SORT_BY_ARTIST:
        mSortType = FileConst.SORT_ARTIST;
        break;
      case SORT_BY_GENRE:
        mSortType = FileConst.SORT_GENRE;
        break;
      case SORT_BY_TYPE:
        mSortType = FileConst.SORT_TYPE;
        break;
      default:
        break;
    }

  }

  @Override
  public List<FileAdapter> listAllFiles(String path) {
    if(path == null) return new ArrayList<FileAdapter>();
    File dir = new File(path);
    int filter = 0;
    switch (mContentType) {
      case CONTENT_PHOTO:
        filter = FileConst.MMP_FF_PHOTO;
        PhotoFile.openFileInfo(mContext);
        break;
      case CONTENT_AUDIO:
        filter = FileConst.MMP_FF_AUDIO;
        AudioFile.openFileInfo(mContext);
        break;
      case CONTENT_VIDEO:
        filter = FileConst.MMP_FF_VIDEO;
        VideoFile.openFileInfo(mContext);
        break;
      case CONTENT_TEXT:
        filter = FileConst.MMP_FF_TEXT;
        break;
      case CONTENT_THRDPHOTO:
        filter = FileConst.MMP_FF_THRDPHOTO;
        ThrDPhotoFile.openFileInfo(mContext);
        break;
      default:
        break;
    }

    List<MtkFile> originalFiles = mOperator.listFilterFiles(filter, dir,
        mSortType);
    mFiles.clear();
    mFiles.addAll(wrapFiles(originalFiles));
    logFiles(TAG);

    return mFiles;
  }

  public List<FileAdapter> listAllFiles(String path, int Type) {
    File dir = new File(mRootPath);

    List<MtkFile> originalFiles = mOperator.listRecursive(dir,
        mSortType, true);

    mFiles = wrapFiles(originalFiles);
    logFiles(TAG);

    return mFiles;
  }

  @Override
  public List<FileAdapter> listRecursiveFiles(int contentType) {
    List<MtkFile> originalFiles = null;
    if (null == mRootPath) {
      return null;
    }
    File dir = new File(mRootPath);

    switch (contentType) {
      case CONTENT_PHOTO:
        originalFiles = mOperator.listRecursivePhoto(dir, mSortType);
        PhotoFile.openFileInfo(mContext);
        break;
      case CONTENT_AUDIO:
        originalFiles = mOperator.listRecursiveAudio(dir, mSortType);
        AudioFile.openFileInfo(mContext);
        break;
      case CONTENT_VIDEO:
        originalFiles = mOperator.listRecursiveVideo(dir, mSortType);
        VideoFile.openFileInfo(mContext);
        break;
      case CONTENT_TEXT:
        originalFiles = mOperator.listRecursiveText(dir, mSortType);
        TextFile.openFileInfo(mContext);
        break;
      case CONTENT_THRDPHOTO:
        originalFiles = mOperator.listRecursiveThrdPhoto(dir, mSortType);
        ThrDPhotoFile.openFileInfo(mContext);
        break;
      default:
        break;
    }
    // mFiles.clear();
    mFiles = wrapFiles(originalFiles);

    return mFiles;
  }

  // add by sky luojie begin
  public void setCancelListRecursive(boolean cancelListRecursive) {
    if(mOperator != null) {
      mOperator.setCancelListRecursive(cancelListRecursive);
    }
  }

  public void listRecursiveFiles(List<FileAdapter> list, int contentType) {
    if (null == mRootPath) {
      return;
    }
    File dir = new File(mRootPath);

    switch (contentType) {
      case CONTENT_PHOTO:
        mOperator.listRecursivePhoto(list, dir);
        PhotoFile.openFileInfo(mContext);
        break;
      case CONTENT_AUDIO:
        mOperator.listRecursiveAudio(list, dir);
        AudioFile.openFileInfo(mContext);
        break;
      case CONTENT_VIDEO:
        mOperator.listRecursiveVideo(list, dir);
        VideoFile.openFileInfo(mContext);
        break;
        /*
      case CONTENT_TEXT:
        originalFiles = mOperator.listRecursiveText(dir, mSortType);
        TextFile.openFileInfo(mContext);
        break;
      case CONTENT_THRDPHOTO:
        originalFiles = mOperator.listRecursiveThrdPhoto(dir, mSortType);
        ThrDPhotoFile.openFileInfo(mContext);
        break;
        */
      default:
        break;
    }
    // mFiles.clear();
    mFiles = list;
  }

  public List<FileAdapter> listRecursiveFiles(String path, int contentType, int maxFileCount, boolean exceptPVR) {
    List<MtkFile> originalFiles = null;
    File dir = new File(path);
    switch (contentType) {
      case CONTENT_PHOTO:
        originalFiles = mOperator.listRecursivePhoto(dir, mSortType, maxFileCount);
        PhotoFile.openFileInfo(mContext);
        break;
      case CONTENT_AUDIO:
        originalFiles = mOperator.listRecursiveAudio(dir, mSortType, maxFileCount);
        AudioFile.openFileInfo(mContext);
        break;
      case CONTENT_VIDEO:
        originalFiles = mOperator.listRecursiveVideo(dir, mSortType, maxFileCount, exceptPVR);
        VideoFile.openFileInfo(mContext);
        break;
      default:
        break;
    }
    return wrapFiles(originalFiles);
  }

  public List<FileAdapter> listRecursivePVRFiles(int maxFileCount) {
    List<MtkFile> originalFiles = null;
    if (null == mRootPath) {
      return null;
    }
    String dirPath1 = mRootPath + File.pathSeparator + "PVR";
    String dirPath2 = mRootPath + File.pathSeparator + "pvr";
    File dir = new File(dirPath1);
    if(!dir.exists()) {
        dir = new File(dirPath2);
    }
    if(!dir.exists()) {
      return null;
    }

    originalFiles = mOperator.listRecursiveVideo(dir, mSortType, maxFileCount, false);
    mFiles = wrapFiles(originalFiles);

    return mFiles;
  }

  public MtkFile[] listVideo(String pathDir) {
    if(pathDir == null || "".equals(pathDir)) return null;
    return mOperator.listVideo(new File(pathDir));
  }
  // add by sky luojie end

  @Override
  protected FileAdapter newWrapFile(Object originalFile) {
    return new LocalFileAdapter((MtkFile) originalFile);
  }

  @Override
  public void destroy() {
    destroyManager();
  }

  @Override
  public void destroyManager() {
    // TODO Auto-generated method stub
    sInstance = null;
  }

  // SKY luojie add 20171219 for add choose menu begin
  public static FileAdapter createVideoFileAdapterByPath(String path) {
    VideoFile file = new VideoFile(new MtkFile(new File(path)));
    return new LocalFileAdapter(file);
  }

  public static FileAdapter createAudioFileAdapterByPath(String path) {
    AudioFile file = new AudioFile(new MtkFile(new File(path)));
    return new LocalFileAdapter(file);
  }

  public static FileAdapter createPhotoFileAdapterByPath(String path) {
    PhotoFile file = new PhotoFile(new MtkFile(new File(path)));
    return new LocalFileAdapter(file);
  }
  // SKY luojie add 20171219 for add choose menu end
}
