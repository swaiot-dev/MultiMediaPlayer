
package com.mediatek.wwtv.mediaplayer.mmp.util;

public interface iDmrListener {
  public void notifyNewEvent(int state);

  public void notifyNewEventWithParam(int state, int param);

  public long getProgress();

  public long getDuration();

}
