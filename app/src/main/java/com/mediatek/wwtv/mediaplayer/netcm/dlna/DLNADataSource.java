
package com.mediatek.wwtv.mediaplayer.netcm.dlna;

import java.io.InputStream;
import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

import com.mediatek.dlna.object.Content;
import com.mediatek.dlna.object.ContentInputStream;

/**
 * This Class is use to download the content resource.
 *
 */
public class DLNADataSource implements Parcelable, Serializable {

  private static final String mDlna = "DLNA_PULL";
  private static final String TAG = "DLNADataSource";
  private final Content mContent;

  public DLNADataSource(Content content) {
    this.mContent = content;
  }

  public InputStream newInputStream() {
    return new DLNAInputStream(mContent);
  }

  public ContentInputStream newContentInputStream() {
    return new ContentInputStream(mContent);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    mContent.writeToParcel(dest, flags);
  }

  public Content getContent() {
    return mContent;
  }

  public static final Parcelable.Creator<DLNADataSource> CREATOR =
      new Parcelable.Creator<DLNADataSource>() {
        @Override
        public DLNADataSource createFromParcel(Parcel source) {
          Content content = Content.CREATOR.createFromParcel(source);
          DLNADataSource dlnaSource = new DLNADataSource(content);
          return dlnaSource;
        }

        @Override
        public DLNADataSource[] newArray(int size) {
          return new DLNADataSource[size];
        }

      };
}
