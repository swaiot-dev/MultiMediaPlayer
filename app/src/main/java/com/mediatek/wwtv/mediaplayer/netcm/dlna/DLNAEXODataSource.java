package com.mediatek.wwtv.mediaplayer.netcm.dlna;

import com.google.android.exoplayer.upstream.FrameworkDataSource;
import com.google.android.exoplayer.upstream.FrameworkDataSource.StreamDataSource;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;

import java.io.InputStream;

public class DLNAEXODataSource implements StreamDataSource{

  private UIMediaPlayer mUiMediaPlayer;

  public DLNAEXODataSource() {

  }

  public DLNAEXODataSource(UIMediaPlayer player) {
    mUiMediaPlayer = player;
  }

  /**
   * Returns whether the dataSource is seekable.
   *
   * @return true if seekable, false if not
   */
  public boolean isSeekable() {
    if (mUiMediaPlayer != null) {
      return mUiMediaPlayer.isSeekable();
    }
    return false;
  }

  /**
   * Returns the size of the media file.
   *
   * @return file size
   */
  public long getSize() {
    if (mUiMediaPlayer != null) {
      return mUiMediaPlayer.getSourceSize();
    }
    return 0;
  }

  /**
   * Returns an {@link InputStream} to read data. Notice that a newly {@link InputStream} will be
   * opend once this function is called. Remember to close the {@link InputStream} no longer used.
   *
   * @return an {@link InputStream}
   */
  public InputStream newInputStream() {
    if (mUiMediaPlayer != null) {
      return mUiMediaPlayer.newInputStream();
    }
    return null;
  }

}
