
package com.mediatek.wwtv.mediaplayer.mmp.util;

public class DivxInfo {
  private boolean tEnable = false;
  private boolean eEnable = false;
  private boolean cEnable = false;
  private int tNum = -1;
  private int eNum = -1;
  private int cNum = -1;

  public void setTNum(int num) {
    tNum = num;
    if (tNum > 0) {
      tEnable = true;
    }
  }

  public int getENum() {
    return eNum;
  }

  public int getCNum() {
    return cNum;
  }

  public void setENum(int num) {
    eNum = num;
    if (eNum > 0) {
      eEnable = true;
    }
  }

  public void setCNum(int num) {
    cNum = num;
    if (cNum > 0) {
      cEnable = true;
    }
  }

  public boolean tEnable() {
    return tEnable;
  }

  public boolean eEnable() {
    return eEnable;
  }

  public boolean cEnable() {
    return cEnable;
  }

}
