
package com.mediatek.wwtv.mediaplayer.mmpcm.audio;

import android.content.Context;

public interface IPlayback {
  /**
   * Set data source to mtkpalyer by sepcified path.
   *
   * @param path
   * @return return true success , return false fail
   */
  boolean setDataSource(String path);

  /**
   * Play a audio
   *
   * @throws IllegalStateException
   */
  void play();

  /**
   * Play next audio
   *
   * @throws IllegalStateException
   */
  void playNext();

  /**
   * Play previous audio
   *
   * @throws IllegalStateException
   */
  void playPrevious();

  /**
   * Pause audio
   *
   * @throws IllegalStateException
   */
  void pause();

  /**
   * Stop audio
   *
   * @throws IllegalStateException
   */
  void stop();

  /**
   * Release audio
   *
   * @throws IllegalStateException
   */
  void release();

//  /**
//   * Fast forword for auido
//   * @throws NotSupportException
//   * @throws IllegalStateException
//   */
//  void fastForward();

  void setNormalSpeed();

//  /**
//   * Fast fewind for auido.
//   * @throws NotSupportException
//   * @throws IllegalStateException
//   */
//  void fastRewind();

  int getSpeed();

  void setSpeed(int speed);

  boolean isPlaying();

  int getPlayStatus();

  int getPlayMode();

  void setPlayMode(int playMode);

  int getPlaybackProgress();

  int getCurrentBytePosition();

  /**
   * get total playback time
   *
   * @return
   */
  int getTotalPlaybackTime();

  /**
   * Seek to certain time
   *
   * @param time
   * @return return true if success, return false if fail
   */
  void seekToCertainTime(long time);

  /**
   * Get sample rate
   * @return return  sample rate
   */
  int getSampleRate();

  /**
   * Get bit rate
   * @return
   */
  int getBitRate();

  String getFilePath();

  /**
   *  Get audio codec
   * @return return  audio codec
   */
  String getAudioCodec();

  /**
   * Get file size
   *
   * @return file size
   */
  public long getFileSize();

  /**
   * Get music title
   *
   * @return music title
   */
  public String getMusicTitle();

  /**
   * Get music artist
   *
   * @return music artist
   */
  public String getMusicArtist();

  /**
   * Get music album
   *
   * @return music album
   */
  public String getMusicAlbum();

  /**
   * Get music genre
   *
   * @return music genre
   */
  public String getMusicGenre();

  /**
   * Get music year
   *
   * @return music year
   */
  public String getMusicYear();

  void registerAudioSeekCompletionListener(Object seekCompletionListener);

  void unregisterAudioSeekCompletionListener();

  void registerAudioCompletionListener(Object completionListener);

  void unregisterAudioCompletionListener();

  void registerAudioPreparedListener(Object newPreparedListener);

  void unregisterAudioPreparedListener();

  void registerAudioErrorListener(Object newErrorListener);

  void unregisterAudioErrorListener();

  public void setContext(Context context);

  /**
   * register audio durationUpdate listener
   *
   * @param updateListener
   */
  public void registerAudioDurationUpdateListener(Object updateListener);

  public void unregisterAudioDurationUpdateListener();

  public void registerInfoListener(Object replayListener);

  public void unregisterInfoListener();

  public boolean isSeekable();
}
