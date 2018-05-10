
package com.mediatek.wwtv.mediaplayer.mmp.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.MtkFile;

/**
 * Author SKY205711
 * Date   2018/1/24
 * Description: This is NoDataItemFileAdapter
 */
public class NoDataItemFileAdapter extends LocalFileAdapter {
  private static final String TAG = "NoDataItemFileAdapter";
  private String mName;
  private Drawable mDrawable;

  public NoDataItemFileAdapter(MtkFile file) {
    super(file);
  }

  public void setName(String name) {
    this.mName = name;
  }

  public Drawable getDrawable() {
    return mDrawable;
  }

  public void setDrawable(Drawable drawable) {
    this.mDrawable = drawable;
  }

  @Override
  public void stopDecode() {
  }

  @Override
  public boolean isPhotoFile() {
    return false;
  }

  @Override
  public boolean isAudioFile() {
    return false;
  }

  @Override
  public boolean isVideoFile() {
    return false;
  }

  @Override
  public boolean isTextFile() {
    return false;
  }

  public boolean isNoDataItem() {
    return true;
  }

  @Override
  public String getSize() {
    return "0B";
  }

  @Override
  public String getTextSize() {
    return "0B";
  }

  @Override
  public String getAbsolutePath() {
    return "";
  }

  @Override
  public String getPath() {
    return "NoDataItemPath" + System.currentTimeMillis();
  }

  @Override
  public String getDeviceName() {
    if (mName != null && mName.length() > 0) {
      return mName;
    }
    return "";
  }

  @Override
  public String getName() {
    return mName;
  }

  @Override
  public Bitmap getThumbnail(int width, int height, boolean isThumbnail) {
    if(mDrawable == null) return null;
    return ((BitmapDrawable)mDrawable).getBitmap();
  }

  @Override
  public boolean isDirectory() {
    return false;
  }

  @Override
  public boolean isFile() {
    return false;
  }

  @Override
  public long lastModified() {
    return System.currentTimeMillis();
  }

  @Override
  public void stopThumbnail() {

  }

  @Override
  public long length() {
    return 0l;
  }

  @Override
  public boolean delete() {
    return true;
  }

  @Override
  public String getInfo() {
    return "";
  }

  @Override
  public String getPreviewBuf() {
    return "";
  }

  @Override
  public String getResolution() {
    return "";
  }

  @Override
  public String getSuffix() {
    return "";
  }

  @Override
  public String getCacheInfo() {
    return "";
  }
}
