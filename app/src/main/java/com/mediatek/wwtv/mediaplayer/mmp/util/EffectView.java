
package com.mediatek.wwtv.mediaplayer.mmp.util;

import com.mediatek.wwtv.mediaplayer.jni.PhotoRender;
import java.util.Random;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.Region.Op;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.DissolveData;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.PhotoUtil;
import android.os.Looper;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;
import android.graphics.Movie;

/**
 *
 * This class represents picture effect .
 *
 */
public class EffectView implements Runnable {
  private static final String TAG = "EFFECTVIEW";

  private int top;
  private int left;
  private int bottom;
  private int right;
  private int bmpW;
  private int bmpH;
  private int bmpPreW;
  private int bmpPreH;
  private int bmp_x;
  private int bmp_y;
  private int bmpPre_x;
  private int bmpPre_y;
  private int displayW = ((PhotoRender.is4KPanel() == true) ? 3840 : 1920);
  private int displayH = ((PhotoRender.is4KPanel() == true) ? 2160 : 1080);

  volatile Bitmap bmp;
  Bitmap bmpPre;

  int gifIndex;

  volatile Movie mGifMovie;
  // Bitmap[] bmpGif;

  int[] mDelayTime;

  Paint paint = new Paint();
  DissolveData dissolveData = null;

  int DSLV_WIDTH_UNIT = 160;
  int DSLV_HEIGHT_UNIT = 120;
  int total_frame = 4;
  int frame_index = 0;
  int DSLV_NUMBER = 160 * 120;// 160 * 120
  int index;
  int unit_end;
  int unit_width;
  int unit_height;
  int x_offset;
  int y_offset;
  int _x;
  int _y;
  int region_width;
  int region_height;
  private final Canvas myCanvas;
  private Bitmap drawBmp;
  private final HandlerThread mThread;

  private volatile int EffectDigreeNum = 0;
  private volatile int preEffectDigreeNum = 0;

  private ImagePlay playLisenter;

  private PhotoRender photoRender;

  public void setRender(PhotoRender render) {
    photoRender = render;
  }

  public interface ImagePlay {

    public void playDone();

    public void playError();
  }

  public void setPlayLisenter(ImagePlay play) {
    this.playLisenter = play;

  }

  public void recycleLastBitmap() {
    if (bmpPre != null && bmpPre != bmp) {
      bmpPre.recycle();
      bmpPre = null;
    }

  }

  public void setWindow(int dw, int dh) {

    displayW = dw;
    displayH = dh;
    Log.d(TAG, "setWindow dw = " + dw + " dh = " + dh);
    recycleBitmap(drawBmp);

    drawBmp = Bitmap.createBitmap(displayW, displayH, Bitmap.Config.ARGB_8888);
    myCanvas.setBitmap(drawBmp);
  }

  /**
   * Simple constructor to use when creating a effect view from code.
   *
   * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
   */
  public EffectView() {

    Log.d(TAG, "EffectView dw = " + displayW + " dh = " + displayH);
    // drawBmp = Bitmap.createBitmap(displayW,displayH,Bitmap.Config.ARGB_8888);
    myCanvas = new Canvas();
    mThread = new HandlerThread("DrawThread", Process.THREAD_PRIORITY_DEFAULT);
    mThread.start();
    eHandler = new MyHandler(mThread.getLooper());

  }

  public void clearScreen() {
    Log.i(TAG, "clearScreen:drawBmp:" + drawBmp + "--playLisenter:" + playLisenter);
    synchronized (this) {
      bmp = null;
      mGifMovie = null;
      myCanvas.clipRect(0, 0, displayW, displayH, Op.REPLACE);
      myCanvas.drawColor(Color.RED, Mode.CLEAR);
    }
    if (drawBmp != null) {
      playBitmap(drawBmp);
    }
    playLisenter.playDone();
    // int[] pixels=new int[displayW*displayH];
    // picjni.doMTPHOTO4KShow(2, pixels, displayW*displayH*4, 0, 0, displayW, displayH);
    // pixels=null;
  }

  private void recycleBitmap(Bitmap curBmp) {
    if (curBmp != null) {

      curBmp.recycle();
      System.gc();
      curBmp = null;
    }

  }

  private void recycleBitmapArray(Bitmap[] curBmps) {
    if (curBmps != null && curBmps.length > 0) {

      for (Bitmap bmp : curBmps) {
        recycleBitmap(bmp);

      }
    }

  }

  private void recycleMovie() {
    if (mGifMovie != null) {
      mGifMovie = null;
    }
  }

  /**
   * Set reource bitmap to use implements effect.
   * @param bitmap
   */
  public void setRes(PhotoUtil bitmap, int[] param) {
    if (null != bitmap) {
      setParams(param);
      setRes(bitmap);
    } else {
      bmp = null;
      mGifMovie = null;
    }
  }

  public void setRes(PhotoUtil bitmap) {
    if (null != bitmap) {
      if (null != bitmap.getMovie()) {
        mGifMovie = bitmap.getMovie();

        bmp = null;
      } else if (null != bitmap.getBitmap()) {
        mGifMovie = null;
        bmp = bitmap.getBitmap();
      }
    } else {

      bmp = null;
      mGifMovie = null;

    }
    if (null != bmp) {

      bmpW = (int) getBitmapWidth(bmp);
      bmpH = (int) getBitmapHeight(bmp);

      MtkLog.i(TAG, "bmpW = " + bmpW);
      MtkLog.i(TAG, "bmpH = " + bmpH);
    }
  }

  volatile int multiple = 1;

  /**
   * Set a base value to use zoom out or zoom in.
   * @param i
   */
  public void setMultiple(int i) {
    multiple = i;
  }

  volatile int preMultiple = 1;

  /**
   * Set a base value to use play effect.
   * @param i
   */
  public void setPreMultiple(int i) {
    preMultiple = i;
  }

  /**
   * Get a base value to use zoom out or zoom in.
   * @param i
   */
  public int getMultiple() {
    return multiple;
  }

  /**
   * Get a base value to use play effect.
   * @param i
   */
  public int getPreMultiple() {
    return preMultiple;
  }

  boolean flag = true;

  private float getBitmapWidth(Bitmap b) {
    float w = 0;
    if (b != null) {
      w = (b.getWidth() * getScale(b));
    }
    return w < 1.0f ? 1.0f : w;
  }

  /* lei add for think scale */
  private float getBitmapHeight(Bitmap b) {
    float h = 0;
    if (b != null) {
      h = (b.getHeight() * getScale(b));
    }
    return h < 1.0f ? 1.0f : h;
  }

  public void syncSetEffectResToRun(Bitmap bmpEffect, int[] param, int value) {
    synchronized (EffectView.this) {
      setEffectRes(bmpEffect, param);
      setType(value);
      run();
    }
  }

  /**
   * Set reource bitmap to use implements effect.
   * @param bitmap
   */
  public void setEffectRes(Bitmap bmpEffect, int[] param) {
    mGifMovie = null;

    if (null != bmpEffect) {
      setParams(param);
      setEffectRes(bmpEffect);
    }

  }

  public void setEffectRes(Bitmap bmpEffect) {
    mGifMovie = null;
    if (null != bmpEffect) {

      if (flag) {
        MtkLog.i(TAG, "1111111");
        bmp = bmpEffect;
        bmpPre = null;
        flag = false;
        if (bmp != null) {
          bmpPreW = (int) getBitmapWidth(bmp);
          bmpPreH = (int) getBitmapHeight(bmp);
        }
      } else {
        MtkLog.i(TAG, "2222222");
        bmpPre = bmp;
        bmp = bmpEffect;
        if (bmpPre != null) {
          bmpPreW = (int) getBitmapWidth(bmpPre);
          bmpPreH = (int) getBitmapHeight(bmpPre);
        }

        MtkLog.i(TAG, "bmpPreW = " + bmpPreW);
        MtkLog.i(TAG, "bmpPreH = " + bmpPreH);
      }
      if (bmp != null) {
        bmpW = (int) getBitmapWidth(bmp);
        bmpH = (int) getBitmapHeight(bmp);
      }

      MtkLog.i(TAG, "bmpW = " + bmpW);
      MtkLog.i(TAG, "bmpH = " + bmpH);
    }
  }

  private void setParams(int[] param) {
    setMultiple(param[0]);
    setPreMultiple(param[1]);
    setRotate(param[2]);
    setPreRotate(param[3]);
    flag = true;
    NotifySwitch = true;
  }

  /* lei add for think scale */
  private float getScale(Bitmap bitmap) {
    float scale = 1.0f;
    if (bitmap == null) {
      return scale;
    }
    float w = bitmap.getWidth();
    float h = bitmap.getHeight();
    float specialphoto = w / h;

    if (specialphoto >= SPCEIL_FILTER || specialphoto <= 1 / SPCEIL_FILTER) {

    } else if (w > 0 && h > 0) {
      float widthScale = Math.min(displayW / w, 1.0f);
      float heightScale = Math.min(displayH / h, 1.0f);
      scale = Math.min(widthScale, heightScale);
    }
    if (scale <= 0.0f) {
      scale = 1.0f;
    }
    return scale;
  }

  private void InitCoordinate1() {
    float scale = 1.0f;
    if (bmpW != 0) {

      bmp_x = (bmpW < displayW) ? (int) Math
          .ceil((displayW - bmpW * scale) / 2.0) : 0;
      bmp_y = (bmpH < displayH) ? (int) Math
          .ceil((displayH - bmpH * scale) / 2.0) : 0;
      MtkLog.i(TAG, "bmp_x = " + bmp_x);
      MtkLog.i(TAG, "bmp_y = " + bmp_y);
    }
  }

  private void InitCoordinate2() {
    float scale = 1.0f;

    if (bmpW != 0) {

      bmp_x = (bmpW < displayW) ? (int) Math
          .ceil((displayW - bmpW * scale) / 2.0) : 0;
      bmp_y = (bmpH < displayH) ? (int) Math
          .ceil((displayH - bmpH * scale) / 2.0) : 0;
      MtkLog.i(TAG, "bmp_x = " + bmp_x);
      MtkLog.i(TAG, "bmp_y = " + bmp_y);
    }

    if (bmpPreW != 0) {

      bmpPre_x = (bmpPreW < displayW) ? (int) Math
          .ceil((displayW - bmpPreW * scale) / 2.0) : 0;
      bmpPre_y = (bmpPreH < displayH) ? (int) Math
          .ceil((displayH - bmpPreH * scale) / 2.0) : 0;
    }
  }

  private void SideToMid(Canvas canvas) {
    MtkLog.i(TAG, "(EffectView onDraw())    side_to_mid>>>>>>>>>>");

    if (left2 >= right2) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(left, top, right, bottom);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
        canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);

        canvas.drawColor(Color.BLACK, Mode.CLEAR);

      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");

        canvas.clipRect(left, top, right, bottom);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
          // canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
          // canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);
        }
        canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2,
            displayH / 2);
        canvas.drawColor(Color.BLACK, Mode.CLEAR);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }

    }
  }

  private void SideToMide_Out(Canvas canvas) {

    if (left2 >= right2) {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    } else {
      canvas.clipRect(left, top, right, bottom);
      canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    }
  }

  private void MidToSize(Canvas canvas) {
    MtkLog.i(TAG, "(EffectView onDraw())    mid_to_side>>>>>>>>>>");
    if ((left2 <= 0) || (right2 > displayW)) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
          // canvas.clipRect(0, 0, displayW, displayH, Op.REPLACE);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }
        canvas.clipRect(0, 0, displayW, displayH, Op.REPLACE);

        // add by shuming for fix bug
        // paint.setAntiAlias(true);
        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2,
            displayH / 2);

        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);

        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top, right2, bottom, Op.REPLACE);

        canvas.drawColor(Color.BLACK, Mode.CLEAR);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);

      }

    }
  }

  private void MidToSize_Out(Canvas canvas) {
    MtkLog.i(TAG, "(EffectView onDraw())    mid_to_side_out>>>>>>>>>>");
    if ((left2 <= 0) || (right2 > displayW)) {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      canvas.clipRect(left, top, right, bottom);
      canvas.clipRect(left2, top, right2, bottom, Op.DIFFERENCE);

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    }
  }

  @SuppressWarnings("unused")
  private void BoxIn_back(Canvas canvas) {
    MtkLog.i(TAG, "box_in>>>>>>>>>>");
    if (left2 >= right2 || top2 >= bottom2) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.DIFFERENCE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.DIFFERENCE);
        // canvas.drawBitmap(bmp, bmp_x, bmp_y, paint);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
          // canvas.clipRect(0, 0, displayW, displayH, Op.XOR);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }

        canvas.clipRect(0, 0, displayW, displayH, Op.XOR);

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }
    }
  }

  @SuppressWarnings("unused")
  private void BoxOut_back(Canvas canvas) {
    MtkLog.i(TAG, "box_out>>>>>>>>>>");
    if (left2 <= 0 || right2 >= displayW) {
      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);

        }
        canvas.clipRect(0, 0, displayW, displayH, Op.XOR);

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }
    }
  }

  private void BoxIn(Canvas canvas) {
    MtkLog.i(TAG, "box_in>>>>>>>>>>");
    if (left2 >= right2 || top2 >= bottom2) {
      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);

        canvas.drawColor(Color.BLACK, Mode.CLEAR);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");

        canvas.clipRect(0, 0, right, bottom);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2,
            displayH / 2);
        canvas.drawColor(Color.BLACK, Mode.CLEAR);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);

      }
    }
  }

  private void BoxOut(Canvas canvas) {
    MtkLog.i(TAG, "box_out>>>>>>>>>>");
    if (left2 <= 0 || right2 >= displayW) {
      canvas.clipRect(0, 0, right, bottom);
      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {
      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }
        canvas.clipRect(0, 0, displayW, displayH, Op.REPLACE);
        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2,
            displayH / 2);

        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);

        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top2, right2, bottom2, Op.REPLACE);
        canvas.drawColor(Color.BLACK, Mode.CLEAR);
        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
      }
    }
  }

  private void WipeRight(Canvas canvas) {
    MtkLog.i(TAG, "wipe_right>>>>>>>>>> right2 =" + right2);

    if (right2 >= displayW) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top, right2, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top, right2, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);

        // add by shuming for fix CR:DTV00415287
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }

        canvas.clipRect(right2, 0, displayW, displayH, Op.REPLACE);
        MtkLog.i(TAG, "-fuck--displayW= " + displayW + "displayH= " + displayH);
        // add by shuming for fix bug

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2,
            displayH / 2);

        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }
    }
  }

  private void WipeLeft(Canvas canvas) {
    MtkLog.i(TAG, "wipe_left>>>>>>>>>>");
    if (left2 <= 0) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top, right, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left2, top, right, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }
        canvas.clipRect(0, 0, left2, displayH, Op.REPLACE);
        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }
    }
  }

  private void WipeTop(Canvas canvas) {
    MtkLog.i(TAG, "wipe_top>>>>>>>>>>");
    if (top2 <= 0) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top2, right, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top2, right, bottom, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }

        canvas.clipRect(0, 0, displayW, top2, Op.REPLACE);
        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);

        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
        MtkLog.i(TAG, "bmpPreW = " + bmpPreW);
      }
    }
  }

  private void WipeBottom(Canvas canvas) {
    MtkLog.i(TAG, "wipe_bottom>>>>>>>>>>");
    if (bottom2 >= displayH) {

      canvas.drawBitmap(bmp, mDisplayMatrix, paint);
    } else {

      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
      if (bmpPre == null) {
        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top, right, bottom2, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      } else {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");
        canvas.clipRect(0, 0, right, bottom);
        canvas.clipRect(left, top, right, bottom2, Op.REPLACE);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);

        MtkLog.i(TAG, "bmp_x = " + bmp_x);
        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }

        canvas.clipRect(0, bottom2, displayW, displayH, Op.REPLACE);

        // add by shuming for fix bug
        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);
        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        MtkLog.i(TAG, "bmpPre_x = " + bmpPre_x);
      }
    }
  }

  private void Dissolve(Canvas canvas) {
    MtkLog.i(TAG, "bmpPre if null, excute 1  frame_index" + frame_index);

    if (null == dissolveData) {
      dissolveData = new DissolveData();
    }

    unit_end = DSLV_NUMBER / total_frame;
    index = unit_end * frame_index;
    unit_end = unit_end + index;

    if (bmpPre == null) {
      if (frame_index < total_frame) {

        MtkLog.i(TAG, "1111111~~~~~~~~~~");
        canvas.save();
        for (index = 0; index < unit_end; index += 2) {

          int num = dissolveData.getNum(index);
          _x = num % DSLV_WIDTH_UNIT;
          _y = num / DSLV_WIDTH_UNIT;

          _x *= unit_width;
          _y *= unit_height;

          _x += x_offset;
          _y += y_offset;

          region_width = unit_width;
          region_height = unit_height;

          if (_x < 0) {
            region_width += _x;
          } else {
            region_width = (_x + region_width < displayW) ? region_width
                : displayW - _x;
          }

          if (_y < 0) {
            region_height += _y;
          } else {
            region_height = (_y + region_height < displayH) ? region_height
                : displayH - _y;
          }
          canvas.clipRect(_x, _y, _x + region_width, _y
              + region_height, Op.REPLACE);

          canvas.drawBitmap(bmp, mDisplayMatrix, paint);

          if (region_width <= 0 || region_height <= 0) {
            continue;
          }
        }
        canvas.restore();
      } else {

        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        MtkLog.i(TAG, "dissolve frame number= " + frame_index);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      }
      frame_index++;

    } else {
      if (frame_index < total_frame) {
        MtkLog.i(TAG, "22222222~~~~~~~~~~~");

        if (preMultiple > 1.0f) {
          getProperZoomMatrix(bmpPre, mDisplayPreMatrix,
              preMultiple);
        } else {
          getProperBaseMatrix(bmpPre, mDisplayPreMatrix);
        }

        mDisplayPreMatrix.postRotate(preEffectDigreeNum, displayW / 2, displayH / 2);

        canvas.drawBitmap(bmpPre, mDisplayPreMatrix, paint);
        canvas.save();

        for (index = 0; index < unit_end; index += 2) {

          int num = dissolveData.getNum(index);
          _x = num % DSLV_WIDTH_UNIT;
          _y = num / DSLV_WIDTH_UNIT;

          _x *= unit_width;
          _y *= unit_height;

          _x += x_offset;
          _y += y_offset;

          region_width = unit_width;
          region_height = unit_height;

          if (_x < 0) {
            region_width += _x;
          } else {
            region_width = (_x + region_width < displayW) ? region_width
                : displayW - _x;
          }

          if (_y < 0) {
            region_height += _y;
          } else {
            region_height = (_y + region_height < displayH) ? region_height
                : displayH - _y;
          }

          canvas.clipRect(_x, _y, _x + region_width, _y
              + region_height, Op.REPLACE);

          canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

          canvas.drawBitmap(bmp, mDisplayMatrix, null);

          if (region_width <= 0 || region_height <= 0) {
            continue;
          }

        }

        canvas.restore();

      } else {

        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        MtkLog.i(TAG, "dissolve frame number= " + frame_index);

        canvas.drawBitmap(bmp, mDisplayMatrix, paint);
      }
      frame_index++;

    }
  }

  private void ZoomIn(Canvas canvas) {
    MtkLog.i(TAG, "Zoom int == " + multiple);
    MtkLog.i(TAG, "ZoomIn");
    getProperZoomMatrix(bmp, mDisplayMatrix, multiple);
        paint.setFilterBitmap(true);
    canvas.drawBitmap(bmp, mDisplayMatrix, paint);

  }

  private void ZoomOut(Canvas canvas) {
    if (bmp == null) {
      return;
    }

    MtkLog.i(TAG, "Zoom Out == " + multiple);
    getProperZoomMatrix(bmp, mDisplayMatrix, multiple);
    mDisplayMatrix.postRotate(EffectDigreeNum, displayW / 2, displayH / 2);
        paint.setFilterBitmap(true);
    canvas.drawBitmap(bmp, mDisplayMatrix, paint);

  }
   public void setmZoomScale() {//add by yx for reset photo size when pause
        mZoomScale = 1.0f;
    }
  // begin by yangxiong for fix MTK the function of photo moving
  public boolean isBeyongScreen(){
      if (bmp == null) {
        return false;
      }

      float viewWidth = displayW;
      float viewHeight = displayH;

      float w = bmp.getWidth();
      float h = bmp.getHeight();
      MtkLog.i("yangxiong", "isBeyongScreen,viewWidth ==" + viewWidth +",viewHeight==" + viewHeight);
      MtkLog.i("yangxiong", "isBeyongScreen,w ==" + w +",h==" + h+",mZoomScale == "+mZoomScale);
      if (w * mZoomScale > viewWidth || h * mZoomScale > viewHeight) return true;//add by yangxiong for solving "photo can not auto play if h=viewHeight"
      return false;
  }
 // end by yangxiong for fix MTK the function of photo moving

  float mMoveTranslateX = 0;
  float mMoveTranslateY = 0;
  float mZoomScale = 1.0f;
  float mStepMoveTranslate = 200;

  private void MoveTranslate(Canvas canvas, int moveDirection) {
      if (bmp == null) {
        return;
      }

      float viewWidth = displayW;
      float viewHeight = displayH;

      float w = bmp.getWidth();
      float h = bmp.getHeight();

      MtkLog.i(TAG, "MoveTranslate,w ==" + w +",h==" + h);
      MtkLog.i(TAG, "MoveTranslate,viewWidth ==" + viewWidth +",viewHeight==" + viewHeight);
      MtkLog.i(TAG, "MoveTranslate,moveDirection ==" + moveDirection);
      MtkLog.i(TAG, "MoveTranslate,mMoveTranslateX =="+ mMoveTranslateX+",mMoveTranslateY=="+mMoveTranslateY);

      if (moveDirection == ConstPhoto.MOVE_RIGHT){
          float photoBeyondScreen = (w * mZoomScale - viewWidth) / 2F - mMoveTranslateX;
          MtkLog.i(TAG, "MOVE_RIGHT,photoBeyondScreen =="+ photoBeyondScreen);

          if (photoBeyondScreen >= mStepMoveTranslate){
            mMoveTranslateX = mMoveTranslateX + mStepMoveTranslate;
          } else if (photoBeyondScreen > 0 && photoBeyondScreen < mStepMoveTranslate){
            mMoveTranslateX = mMoveTranslateX + photoBeyondScreen;
          }
      } else if (moveDirection == ConstPhoto.MOVE_LEFT){
          float photoBeyondScreen = (w * mZoomScale - viewWidth) / 2F + mMoveTranslateX;
          MtkLog.i(TAG, "MOVE_LEFT,photoBeyondScreen =="+ photoBeyondScreen);

          if (photoBeyondScreen >= mStepMoveTranslate){
            mMoveTranslateX = mMoveTranslateX - mStepMoveTranslate;
          } else if (photoBeyondScreen > 0 && photoBeyondScreen < mStepMoveTranslate){
            mMoveTranslateX = mMoveTranslateX - photoBeyondScreen;
          }
      } else if (moveDirection == ConstPhoto.MOVE_UP){
          float photoBeyondScreen = (h * mZoomScale - viewHeight) / 2F + mMoveTranslateY;
          MtkLog.i(TAG, "MOVE_UP,photoBeyondScreen =="+ photoBeyondScreen);

          if (photoBeyondScreen >= mStepMoveTranslate){
            mMoveTranslateY = mMoveTranslateY - mStepMoveTranslate;
          } else if (photoBeyondScreen > 0 && photoBeyondScreen < mStepMoveTranslate){
            mMoveTranslateY = mMoveTranslateY - photoBeyondScreen;
          }
      } else if (moveDirection == ConstPhoto.MOVE_DOWN){
          float photoBeyondScreen = (h * mZoomScale - viewHeight) / 2F - mMoveTranslateY;
          MtkLog.i(TAG, "MOVE_DOWN,photoBeyondScreen =="+ photoBeyondScreen);

          if (photoBeyondScreen >= mStepMoveTranslate){
            mMoveTranslateY = mMoveTranslateY + mStepMoveTranslate;
          } else if (photoBeyondScreen > 0 && photoBeyondScreen < mStepMoveTranslate){
            mMoveTranslateY = mMoveTranslateY + photoBeyondScreen;
          }
      }

      mDisplayMatrix.reset();

      MtkLog.i(TAG, "MoveTranslate,mZoomScale =="+ mZoomScale);
      if (mZoomScale > 0.0f) {
          mDisplayMatrix.postScale(mZoomScale, mZoomScale);
      }

      MtkLog.i(TAG, "MoveTranslate,22 mMoveTranslateX ==" + mMoveTranslateX + ",mMoveTranslateY=="+mMoveTranslateY);

      mDisplayMatrix.postTranslate(
        (viewWidth - w * mZoomScale) / 2F + mMoveTranslateX,
        (viewHeight - h * mZoomScale) / 2F + mMoveTranslateY);

      MtkLog.i(TAG, "MoveTranslate,EffectDigreeNum=="+ EffectDigreeNum);
      mDisplayMatrix.postRotate(EffectDigreeNum, displayW / 2, displayH / 2);

      paint.setFilterBitmap(true);
      canvas.drawBitmap(bmp, mDisplayMatrix, paint);

  }

  long movieStart = 0;

  private synchronized void Default(Canvas canvas) {
    MtkLog.i(TAG, "Default+++++++++++++++++++");

    if (null != mGifMovie) {
      if (isInterrupted) {
        if (null != mListener) {
          mListener.drawEnd();
        }
        return;
      }
      long curTime = android.os.SystemClock.uptimeMillis();
      if (movieStart == 0) {
        movieStart = curTime;

      }
      int dur = mGifMovie.duration() == 0 ? 100 : mGifMovie.duration();

      int relTime = (int) ((curTime - movieStart) % dur);

      int x = (canvas.getWidth() - mGifMovie.width() * multiple) / (2 * multiple);
      int y = (canvas.getHeight() - mGifMovie.height() * multiple) / (2 * multiple);
      mGifMovie.setTime(relTime);
      try {
        canvas.save();
        Matrix mx = canvas.getMatrix();
        mx.postScale(multiple, multiple);
        mx.postRotate(EffectDigreeNum, canvas.getWidth() / 2, canvas.getHeight() / 2);
        canvas.setMatrix(mx);

        Log.d(TAG, "dur = " + dur + "relTime = " + relTime + " x =" + x + " y = " + y);
        Log.d(
            TAG,
            " canvas.getWidth() =" + canvas.getWidth() + " canvas.getHeight() = "
                + canvas.getHeight());
        mGifMovie.draw(canvas, x, y);
        canvas.restore();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      if ((curTime - movieStart) >= dur && NotifySwitch) {
        NotifySwitch = false;
        playLisenter.playDone();

      }
      if (false == isInterrupted) {
        eHandler.sendEmptyMessage(ConstPhoto.PLAYTOEND);
      }
    } else if (null != bmp) {

      MtkLog.i(TAG, "bmp:" + bmp);
      canvas.drawBitmap(bmp, mDisplayMatrix, paint);

    } else {
      canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

      System.out.println(" Image decode failure! ");
    }

  }

  private void DrawBlack(Canvas canvas) {
    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
  }

  /**
   * Implement this to do your drawing.
   *
   * @param canvas the canvas on which the background will be drawn
   * Modified by Lei for add Scale Matrix when Draw bitmap
  * CR TV00399654
   */
  public synchronized void onDraw(Canvas canvas) {
    MtkLog.i(TAG, "onDraw type = " + type);
    canvas.clipRect(0, 0, displayW, displayH, Op.REPLACE);
    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
    if (bmp != null) {
      getProperBaseMatrix(bmp, mDisplayMatrix);
    } else if (mGifMovie == null) {
      return;
    }

    switch (type) {

      case ConstPhoto.side_to_mid:
        SideToMid(canvas);
        break;
      case ConstPhoto.mid_to_side:
        MidToSize(canvas);
        break;
      case ConstPhoto.side_to_mid_out:
        SideToMide_Out(canvas);
        break;
      case ConstPhoto.mid_to_side_out:
        MidToSize_Out(canvas);
        break;
      case ConstPhoto.box_in:
        BoxIn(canvas);
        break;
      case ConstPhoto.box_out:
        BoxOut(canvas);
        break;
      case ConstPhoto.wipe_right:
        WipeRight(canvas);
        break;
      case ConstPhoto.wipe_left:
        WipeLeft(canvas);
        break;
      case ConstPhoto.wipe_top:
        WipeTop(canvas);
        break;
      case ConstPhoto.wipe_bottom:
        WipeBottom(canvas);
        break;
      case ConstPhoto.dissolve:
        Dissolve(canvas);
        break;

      case ConstPhoto.ZOOMOUT:
      case ConstPhoto.ROTATE_R:
        ZoomOut(canvas);
        break;

      case ConstPhoto.ROTATE_PHOTO:
        RotatePhoto(canvas);
        break;

      case ConstPhoto.ZOOMIN:
        ZoomIn(canvas);
        break;

      case ConstPhoto.MOVE_RIGHT:
      case ConstPhoto.MOVE_LEFT:
      case ConstPhoto.MOVE_DOWN:
      case ConstPhoto.MOVE_UP:
        MoveTranslate(canvas, type);
        break;

      case ConstPhoto.DEFAULT:
        Default(canvas);
        break;
      case ConstPhoto.DRAWBLACK:
        DrawBlack(canvas);
        break;
      default:
        break;
    }
    MtkLog.i(TAG, "playBitmap time begin = " + System.currentTimeMillis());

    playBitmap(drawBmp);
    MtkLog.i(TAG, "playBitmap time  end = " + System.currentTimeMillis());

  }

  private synchronized void playBitmap(Bitmap bitmap) {
    MtkLog.i(TAG, "count" + bitmap.getByteCount() + ":---:" + bitmap.getAllocationByteCount());
    if (isInterrupted) {
      if (null != mListener) {
        mListener.drawEnd();
      }
      return;
    }
    MtkLog.i(TAG, "bitmap:" + bitmap + " mmGifMovie:" + mGifMovie);

    // pixels=null;

    long nativeBitmap = 0;
    if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
      try {
        Log.i(TAG, "mmmmm test");

        Field fileds = bitmap.getClass().getDeclaredField("mNativePtr");
        fileds.setAccessible(true);
        nativeBitmap = fileds.getLong(bitmap);// (long)fileds.get(bitmap);
        Log.i(TAG, "reflect field:nativeBitmap:" + nativeBitmap);

        Log.i(TAG, "getNativeBitmap():" + nativeBitmap);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    } else {
      try {
        Log.i(TAG, "lllll test");
        Field fileds = Bitmap.class.getDeclaredField("mNativeBitmap");
        fileds.setAccessible(true);
        nativeBitmap = (long) fileds.get(bitmap);
      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }

    Config config = bitmap.getConfig();
    MtkLog.i(TAG, "config.value:" + config.ordinal());
    // bitmap.setConfig(Config.ARGB_8888);

    MtkLog.i(TAG, "config.value:" + config.ordinal());
    // int nativeBitmap = (int)bitmap.mNativeBitmap;
    Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    int dst_w = bitmap.getWidth(), dst_h = bitmap.getHeight();

    if (dst_w > displayW || dst_h > displayH)
    {
      if ((1000 * dst_w / displayW) > (1000 * dst_h / displayH))
      {
        dst_h = (dst_h * (1000 * displayW / dst_w)) / 1000;
        dst_w = displayW;
      }
      else
      {
        dst_w = (dst_w * (1000 * displayH / dst_h)) / 1000;
        dst_h = displayH;
      }
    }

    Rect dstRect = new Rect((displayW - dst_w) / 2, (displayH - dst_h) / 2, dst_w
        + (displayW - dst_w) / 2, dst_h + (displayH - dst_h) / 2);
    MtkLog.i(TAG,
        "start to render, bitmap width()" + bitmap.getWidth() + "--height:" + bitmap.getHeight());
    MtkLog.i(TAG, "start to render, photoRender=" + photoRender + "--nativeBitmap:" + nativeBitmap
        + "--(should be long)nativeBitmap int:" + ((int) nativeBitmap));
    int result = photoRender.renderPhotoOnVdp(srcRect, dstRect, nativeBitmap, 0);
    Log.i(TAG, "result :" + result);
    if (photoRender == null || result != 0) {
      Log.d(TAG, "photoRender = " + photoRender);
      MtkLog.i(TAG, "render fail");
      playLisenter.playError();
    }
    MtkLog.i(TAG, "render ok");
    /**/
  }

  private static int mTempType;
  private static int type;
  private int right2;
  private int left2;
  private int top2;
  private int bottom2;
  private boolean mIsSwitchingEffect;

  private void InitSideToMid() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    left2 = 0;
    right2 = displayW;

    InitCoordinate2();

  }

  private void InitMidToSide() {
    if (displayW % 2 == 0) {
      left = displayW / 2;
      top = 0;
      right = displayW / 2;
      bottom = displayH;
    } else {
      left = (displayW - 1) / 2;
      top = 0;
      right = (displayW - 1) / 2;
      bottom = displayH;
    }
    if (displayW % 2 == 0) {
      left2 = displayW / 2;
      right2 = displayW / 2;

    } else {
      left2 = (displayW - 1) / 2;
      right2 = (displayW - 1) / 2;
    }

    InitCoordinate2();
  }

  private void InitSideToMid_Out() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    left2 = 0;
    right2 = displayW;

    InitCoordinate1();
  }

  private void InitMidToSide_Out() {

    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    if (displayW % 2 == 0) {
      left2 = displayW / 2;
      right2 = displayW / 2;

    } else {
      left2 = (displayW - 1) / 2;
      right2 = (displayW - 1) / 2;
    }
    InitCoordinate1();
  }

  private void InitBoxIn() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    left2 = 0;
    top2 = 0;
    right2 = displayW;
    bottom2 = displayH;

    InitCoordinate2();
  }

  private void InitBoxOut() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    left2 = (int) Math.ceil(displayW / 2);
    top2 = (int) Math.ceil(displayH / 2);
    right2 = (int) Math.ceil(displayW / 2);
    bottom2 = (int) Math.ceil(displayH / 2);

    InitCoordinate2();
  }

  private void InitWipeRight() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;
    right2 = 0;

    InitCoordinate2();
  }

  private void InitWipeLeft() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;
    left2 = displayW;

    InitCoordinate2();
  }

  private void InitWipeTop() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;
    top2 = displayH;

    InitCoordinate2();
  }

  private void InitWipeBottom() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;
    bottom2 = 0;

    InitCoordinate2();
  }

  private void InitDissolve() {

    frame_index = 0;

    unit_width = (displayW + DSLV_WIDTH_UNIT - 1) / DSLV_WIDTH_UNIT;
    unit_height = (displayH + DSLV_HEIGHT_UNIT - 1) / DSLV_HEIGHT_UNIT;

    MtkLog.i(TAG, "unit_width = " + unit_width);
    MtkLog.i(TAG, "unit_height = " + unit_height);

    x_offset = (displayW - unit_width * DSLV_WIDTH_UNIT) / 2;
    y_offset = (displayH - unit_height * DSLV_HEIGHT_UNIT) / 2;

    MtkLog.i(TAG, "x_offset = " + x_offset);
    MtkLog.i(TAG, "y_offset = " + y_offset);

    if (x_offset < 0) {
      x_offset = 0;
    }

    if (y_offset < 0) {
      y_offset = 0;
    }

    InitCoordinate2();
  }

  private void InitDefault() {
    left = 0;
    top = 0;
    right = displayW;
    bottom = displayH;

    InitCoordinate1();
  }

  static boolean randomEffectValue;

  /**
   * Set effect type to use playback photo.
   * @param i
   */
  public void setType(int i) {

    if (i == ConstPhoto.ROTATE_R || i == ConstPhoto.ZOOMOUT

        || i == ConstPhoto.DRAWBLACK || i == ConstPhoto.ROTATE_PHOTO
        || i == ConstPhoto.MOVE_RIGHT
        || i == ConstPhoto.MOVE_LEFT
        || i == ConstPhoto.MOVE_UP
        || i == ConstPhoto.MOVE_DOWN) {//add by yangxiong for solving for "photo cant auto play next && the position is already changed and so need to reset"

      mIsSwitchingEffect = false;
      type = i;
      MtkLog.i(TAG, "set type = " + type);
      return;
    }

    mIsSwitchingEffect = true;
    mTempType = i;
    MtkLog.i(TAG, "before get random: type = " + mTempType);

    randomEffectValue = false;
    if (getEffectValue() == ConstPhoto.RADNOM) {
      mTempType = getRandomNum();

      MtkLog.i(TAG, "get random number after type = " + mTempType);

      randomEffectValue = true;
    }

  }

  public int getDisX() {
    int disX = 0;

    switch (type) {
      case ConstPhoto.wipe_right:
      case ConstPhoto.wipe_left:
        disX = (int) Math.ceil(displayW / 8.0);
        // return disX;
        break;
      case ConstPhoto.mid_to_side:
      case ConstPhoto.side_to_mid:
      case ConstPhoto.side_to_mid_out:
      case ConstPhoto.mid_to_side_out:
      case ConstPhoto.box_in:
      case ConstPhoto.box_out:
        disX = (int) Math
            .ceil(displayW / 8.0);
        // return disX;
        break;
      default:
        disX = 0;
        break;
    }
    MtkLog.i(TAG, "getDisX type:" + type + " disX = " + disX);
    return disX;
  }

  public int getDisY() {
    int disY = 0;
    switch (type) {
      case ConstPhoto.wipe_top:
      case ConstPhoto.wipe_bottom:
        disY = (int) Math.ceil(displayH / 8.0);
        // return disY;
        break;
      case ConstPhoto.box_in:
      case ConstPhoto.box_out:
        disY = (int) Math
            .ceil(displayH / 8.0);
        // return disY;
        break;

      default:
        disY = 0;
        break;
    }
    MtkLog.i(TAG, "getDisY type:" + type + " disY  = " + disY);
    return disY;
  }

  /**
   * execute to effect for photo.
   *
   */
  @Override
  public void run() {

    synchronized (EffectView.this) {
      MtkLog.i(TAG, "(EffectView run()) run is doing");

      removeMessage();
      Message message = new Message();

      if (mIsSwitchingEffect) {
        type = mTempType;
      } else {
        mIsSwitchingEffect = true;
      }

      if (null != mGifMovie) {
        InitDefault();
        type = ConstPhoto.DEFAULT;
        message.what = ConstPhoto.PLAYTOEND;
        MtkLog.i(TAG, "(EffectView run())  send message GIF_ANIM>>>>>>>>>>>>>");
        eHandler.sendMessage(message);
        return;
      }

      MtkLog.i(TAG, "(EffectView run()) type = " + type);

      eHandler.removeMessages(type);
      switch (type) {
        case ConstPhoto.side_to_mid:
          InitSideToMid();
          message.what = ConstPhoto.side_to_mid;
          MtkLog.i(TAG, "(EffectView run())  send message side_to_mid>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.mid_to_side:
          InitMidToSide();
          message.what = ConstPhoto.mid_to_side;
          MtkLog.i(TAG, "(EffectView run())  send message mid_to_side>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.side_to_mid_out:
          InitSideToMid_Out();
          message.what = ConstPhoto.side_to_mid_out;
          MtkLog.i(TAG, "(EffectView run())  send message side_to_mid>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.mid_to_side_out:
          InitMidToSide_Out();
          message.what = ConstPhoto.mid_to_side_out;
          MtkLog.i(TAG, "(EffectView run())  send message mid_to_side>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.box_in:
          InitBoxIn();
          message.what = ConstPhoto.box_in;
          MtkLog.i(TAG, "(EffectView run())  send message box_in>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.box_out:
          InitBoxOut();
          message.what = ConstPhoto.box_out;
          MtkLog.i(TAG, "(EffectView run())  send message box_out>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.wipe_right:
          InitWipeRight();
          message.what = ConstPhoto.wipe_right;
          MtkLog.i(TAG, "(EffectView run())  send message wipe_right>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.wipe_left:
          InitWipeLeft();
          message.what = ConstPhoto.wipe_left;
          MtkLog.i(TAG, "(EffectView run())  send message wipe_left>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.wipe_top:
          InitWipeTop();
          message.what = ConstPhoto.wipe_top;
          MtkLog.i(TAG, "(EffectView run())  send message wipe_top>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.wipe_bottom:

          InitWipeBottom();
          message.what = ConstPhoto.wipe_bottom;
          MtkLog.i(TAG, "(EffectView run())  send message wipe_bottom>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.dissolve:
          InitDissolve();
          message.what = ConstPhoto.dissolve;
          MtkLog.i(TAG, "(EffectView run())  send message dissolve>>>>>>>>>>>>>");
          eHandler.sendMessage(message);
          break;
        case ConstPhoto.DEFAULT:
          InitDefault();
          eHandler.sendEmptyMessage(ConstPhoto.DEFAULT);
          break;

        case ConstPhoto.ZOOMOUT:
        case ConstPhoto.ROTATE_R:
        case ConstPhoto.ZOOMIN:
        case ConstPhoto.DRAWBLACK:
        case ConstPhoto.ROTATE_PHOTO:
            mMoveTranslateX = 0;
            mMoveTranslateY = 0;
        case ConstPhoto.MOVE_RIGHT:
        case ConstPhoto.MOVE_LEFT:
        case ConstPhoto.MOVE_UP:
        case ConstPhoto.MOVE_DOWN:

          onDraw(myCanvas);
          break;

      }

    }

  }

  MyHandler eHandler;

  class MyHandler extends Handler {

    public MyHandler() {
      super();
    }

    public MyHandler(Looper looper) {
      super(looper);

    }

    @Override
    public void handleMessage(Message msg) {
      MtkLog.i(TAG, "(EffectView eHandler()) isInterrupted = "
          + isInterrupted + " msg.what = " + msg.what + " type = " + type);
      synchronized (EffectView.this) {

        // not interrupted status,only gif flow what != type.
        if (!isInterrupted && msg.what != type) {

          if (msg.what != ConstPhoto.PLAYTOEND && type != ConstPhoto.DEFAULT) {
            return;
          }
        }

        // interrupted status only allow PLAYTOEND handle.
        if (isInterrupted && msg.what != ConstPhoto.PLAYTOEND) {
          return;
        }

        switch (msg.what) {
          case ConstPhoto.side_to_mid:
            MtkLog.i(TAG, "(EffectView eHandler()) case side_to_mid~~~~~~~~~~left2 = " +
                left2 + " right2 = " + right2);
            if (left2 < right2) {

              left2 += getDisX();
              right2 -= getDisX();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.side_to_mid, 30);
              eHandler.sendEmptyMessage(ConstPhoto.side_to_mid);
            } else {
              eHandler.removeMessages(ConstPhoto.side_to_mid);
              preEffectDigreeNum = 0;
              preMultiple = 1;
              if (playLisenter != null) {
                playLisenter.playDone();

              }
            }
            break;
          case ConstPhoto.mid_to_side:
            MtkLog.i(TAG, "(EffectView eHandler()) case mid_to_side~~~~~~~~~~left2 = "
                + left2 + "right2 = " + right2 + " displayW = " + displayW);

            if ((left2 >= 0) || (right2 < displayW)) {
              left2 -= getDisX();
              right2 += getDisX();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.mid_to_side,30);
              eHandler.sendEmptyMessage(ConstPhoto.mid_to_side);
            } else {
              eHandler.removeMessages(ConstPhoto.mid_to_side);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }
            }
            break;
          case ConstPhoto.side_to_mid_out:
            MtkLog.i(TAG, "(EffectView eHandler()) case side_to_mid_out~~~~~~~~~~left2 = "
                + left2 + " right2 = " + right2);

            if (left2 < right2) {

              left2 += getDisX();
              right2 -= getDisX();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.side_to_mid_out,30);
              eHandler.sendEmptyMessage(ConstPhoto.side_to_mid_out);
            } else {
              removeMessages(ConstPhoto.side_to_mid_out);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }
            }
            break;
          case ConstPhoto.mid_to_side_out:
            MtkLog.i(TAG, "(EffectView eHandler()) case mid_to_side_out~~~~~~~~~~left2 = "
                + left2 + " right2 = " + right2 + " displayW = " + displayW);

            if ((left2 > 0) || (right2 < displayW)) {
              left2 -= getDisX();
              right2 += getDisX();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.mid_to_side_out,30);
              eHandler.sendEmptyMessage(ConstPhoto.mid_to_side_out);
            } else {
              eHandler.removeMessages(ConstPhoto.mid_to_side_out);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }
            }
            break;
          case ConstPhoto.box_in:
            MtkLog.i(TAG, "eHandler() case box_in~~~~~~~~~~+ left2 = "
                + left2 + " right2 = " + right2 + " top2 = "
                + top2 + " bottom2 = " + bottom2);
            if ((left2 < right2) || (top2 < bottom2)) {

              left2 += getDisX();
              top2 += getDisY();
              right2 -= getDisX();
              bottom2 -= getDisY();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.box_in,30);
              eHandler.sendEmptyMessage(ConstPhoto.box_in);
            } else {
              eHandler.removeMessages(ConstPhoto.box_in);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }

            }
            break;
          case ConstPhoto.box_out:
            MtkLog.i(TAG, "eHandler() case box_out~~~~~~~~~~left2 = "
                + left2 + " right2 = " + right2 + "displayW = " + displayW);
            if ((left2 >= 0) || (right2 < displayW)) {
              left2 -= getDisX();
              top2 -= getDisY();
              right2 += getDisX();
              bottom2 += getDisY();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.box_out,30);
              eHandler.sendEmptyMessage(ConstPhoto.box_out);
            } else {

              eHandler.removeMessages(ConstPhoto.box_out);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }

            }
            break;
          case ConstPhoto.wipe_right:
            MtkLog.i(TAG, "eHandler() case wipe_right~~~~~~~~~~right2 = " + right2
                + " displayW = " + displayW);
            if (right2 <= displayW) {
              right2 += getDisX();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.wipe_right,30);
              eHandler.sendEmptyMessage(ConstPhoto.wipe_right);
            } else {
              eHandler.removeMessages(ConstPhoto.wipe_right);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }
            }
            break;
          case ConstPhoto.wipe_left:
            MtkLog.i(TAG, "eHandler() case wipe_left~~~~~~~~~~left2 = " + left2);
            if (left2 >= 0) {
              left2 -= getDisX();
              onDraw(myCanvas);
              eHandler.sendEmptyMessage(ConstPhoto.wipe_left);
            } else {
              eHandler.removeMessages(ConstPhoto.wipe_left);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }

            }
            break;
          case ConstPhoto.wipe_top:
            MtkLog.i(TAG, "eHandler() case wipe_top~~~~~~~~~~ top2 = "
                + top2);
            if (top2 > 0) {
              top2 -= getDisY();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.wipe_top,30);
              eHandler.sendEmptyMessage(ConstPhoto.wipe_top);
            } else {

              eHandler.removeMessages(ConstPhoto.wipe_top);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }

            }
            break;
          case ConstPhoto.wipe_bottom:
            MtkLog.i(TAG, "eHandler() case wipe_bottom~~~~~~~~~~bottom2 = "
                + bottom2 + " displayH =" + displayH);
            if (bottom2 <= displayH) {
              bottom2 += getDisY();
              onDraw(myCanvas);
              // eHandler.sendEmptyMessageDelayed(ConstPhoto.wipe_bottom,30);
              eHandler.sendEmptyMessage(ConstPhoto.wipe_bottom);
            } else {
              eHandler.removeMessages(ConstPhoto.wipe_bottom);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();
              }
            }
            break;
          case ConstPhoto.dissolve:
            MtkLog.i(TAG, "eHandler() case dissolve~~~~~~~~~~~~frame_index = "
                + frame_index + " total_frame = " + total_frame);
            if (frame_index <= total_frame) {
              onDraw(myCanvas);
              eHandler.sendEmptyMessage(ConstPhoto.dissolve);
            } else {

              MtkLog.i(TAG, "remove \"dissolve\" message");

              eHandler.removeMessages(ConstPhoto.dissolve);
              preEffectDigreeNum = 0;
              preMultiple = 1;

              if (playLisenter != null) {
                playLisenter.playDone();

              }

            }
            break;
          case ConstPhoto.DEFAULT:
            MtkLog.i(TAG, "eHandler() case DEFAULT~~~~~~~~~~~");
            onDraw(myCanvas);
            eHandler.removeMessages(ConstPhoto.DEFAULT);
            preEffectDigreeNum = 0;
            preMultiple = 1;

            if (playLisenter != null) {
              playLisenter.playDone();

            }
            break;
          case ConstPhoto.PLAYTOEND:
            MtkLog.i(TAG, "eHandler() case PLAYTOEND~~~~~~~~~~~~~~~");
            if (false == isInterrupted) {
              onDraw(myCanvas);
            } else {
              if (null != mListener) {
                iCompleteListener listener = mListener;
                listener.drawEnd();
                bmp = null;
                mGifMovie = null;
                mListener = null;
              }
              eHandler.removeMessages(ConstPhoto.PLAYTOEND);
            }
            break;
          default:
            break;
        }
      }
    }

  };

  /**
   * Get current plays effect value for photo
   * @return for examples
   */
  public int getEffectValue() {
    if (randomEffectValue == true) {
      return ConstPhoto.RADNOM;
    }

    return mTempType;
  }

  int temp = -1;

  /**
   * Returns a new pseudo-random int value which is uniformly distributed between
   * 0 (inclusively) and the value of n (exclusively).
   * @return
   */
  public int getRandomNum() {
    Random random = new Random();
    int a;

    do {

      a = random.nextInt(ConstPhoto.EFFECT_END)
          % (ConstPhoto.EFFECT_END - ConstPhoto.EFFECT_START + 1)
          + ConstPhoto.EFFECT_START;

    } while (a == temp);

    temp = a;

    MtkLog.i(TAG, "randomNumber = " + a);

    return a;
  }

  // This is the final matrix which is computed as the concatentation
  // of the base matrix and the supplementary matrix.
  private final Matrix mDisplayMatrix = new Matrix();

  private final Matrix mDisplayPreMatrix = new Matrix();

  // Setup the base matrix so that the image is centered and scaled properly.
  private void getProperBaseMatrix(Bitmap bitmap, Matrix matrix) {

    float viewWidth = displayW;// getWidth();
    float viewHeight = displayH;// getHeight();

    float w = bitmap.getWidth();
    float h = bitmap.getHeight();
    matrix.reset();

    float widthScale = Math.min(viewWidth / w, 1.0f);
    float heightScale = Math.min(viewHeight / h, 1.0f);
    float scale = Math.min(widthScale, heightScale);

    float specialScale = w / h;

    if (specialScale >= SPCEIL_FILTER || specialScale <= 1 / SPCEIL_FILTER) {

    } else if (scale > 0.0f) {
      matrix.postScale(scale, scale);
    } else {
      scale = 1.0f;
    }
    matrix.postTranslate(
        (viewWidth - w * scale) / 2F,
        (viewHeight - h * scale) / 2F);
  }

//  private final float SCALE_RATE = 0.6f;
  private final float SPCEIL_FILTER = 800f;

  private void getProperZoomMatrix(Bitmap bitmap, Matrix matrix, float s) {
    float viewWidth = displayW;// getWidth();
    float viewHeight = displayH;// getHeight();
    MtkLog.d(TAG, "getProperZoomMatrix viewHeight:" + viewHeight + "  viewWidth:" + viewWidth
        + "   s:" + s);
    float w = bitmap.getWidth();
    float h = bitmap.getHeight();
    MtkLog.d(TAG, "getProperZoomMatrix bitmap h:" + h + "  w:" + w);
    matrix.reset();

    float specialphoto = w / h;
    MtkLog.d(TAG, "getProperZoomMatrix specialphoto w / h:" + specialphoto);
    float scale = s;

    if ((int) s == 1) {
      float widthScale = Math.min(viewWidth / w, s);
      float heightScale = Math.min(viewHeight / h, s);
      MtkLog.d(TAG, "getProperZoomMatrix viewWidth / w:" + viewWidth / w
          + "  viewHeight / h:" + viewHeight / h);
      scale = Math.min(widthScale, heightScale);
      if (specialphoto >= SPCEIL_FILTER || specialphoto <= 1 / SPCEIL_FILTER) {
        scale = 1.0f;
      }
    } else if (s > 1.0f) {
      scale = (float) Math.sqrt(scale);
      MtkLog.d(TAG, "getProperZoomMatrix scale:" + scale);
    }

    if (scale > 0.0f) {
      matrix.postScale(scale, scale);
    } else {
      scale = 1.0f;
    }

    mZoomScale = scale;

    matrix.postTranslate(
        (viewWidth - w * scale) / 2F,
        (viewHeight - h * scale) / 2F);
  }

  public int getRotate() {
    return EffectDigreeNum;
  }

  public void setRotate(int digree) {

    EffectDigreeNum = digree;
    MtkLog.i(TAG, "setRotate:" + digree);
  }

  public int getPreRotate() {
    return preEffectDigreeNum;
  }

  public void setPreRotate(int digree) {
    MtkLog.i(TAG, "setPreRotateGigree digree = " + digree);
    preEffectDigreeNum = digree;

  }

  public void RotatePhoto(Canvas canvas) {
    MtkLog.i(TAG, "Rotate Photo use Matrix method!");
    getProperZoomMatrix(bmp, mDisplayMatrix, multiple);
    mDisplayMatrix.postRotate(EffectDigreeNum, displayW / 2, displayH / 2);
    canvas.drawBitmap(bmp, mDisplayMatrix, paint);
  }

  public void bitmapRecycle() {
    if (bmp != null) {
      setInterrupted(true);
    }
    recycleBitmap(bmp);
    recycleBitmap(bmpPre);
    recycleBitmap(drawBmp);
    recycleMovie();

  }

  /**
   * Remove handler message
   */
  public void removeMessage() {

    eHandler.removeMessages(ConstPhoto.side_to_mid);
    eHandler.removeMessages(ConstPhoto.mid_to_side);
    eHandler.removeMessages(ConstPhoto.side_to_mid_out);
    eHandler.removeMessages(ConstPhoto.mid_to_side_out);
    eHandler.removeMessages(ConstPhoto.dissolve);
    eHandler.removeMessages(ConstPhoto.wipe_right);
    eHandler.removeMessages(ConstPhoto.wipe_left);
    eHandler.removeMessages(ConstPhoto.wipe_top);
    eHandler.removeMessages(ConstPhoto.wipe_bottom);
    eHandler.removeMessages(ConstPhoto.box_in);
    eHandler.removeMessages(ConstPhoto.box_out);
    eHandler.removeMessages(ConstPhoto.side_to_mid);
    eHandler.removeMessages(ConstPhoto.PLAYTOEND);

  }

  private boolean isInterrupted = false;
  private boolean NotifySwitch = true;

  /*
   * when true,mean to finsh photoplay
   */
  public void setInterrupted(boolean isInterrupt) {
    isInterrupted = isInterrupt;
    if (isInterrupt) {
      synchronized (this) {
        removeMessage();
        if (mListener != null) {
          if (bmp != null || mGifMovie != null) {
            eHandler.sendEmptyMessage(ConstPhoto.PLAYTOEND);
          } else {
            iCompleteListener listener = mListener;
            listener.drawEnd();
            mListener = null;
          }
        }
      }
    }
  }

  /*
   * this called when photo loaddone To sure playDone() only called one time
   */
  public void setNotifyOnce() {
    NotifySwitch = false;
  }

  public iCompleteListener mListener = null;

  public void setCompleteListener(iCompleteListener listener) {
    mListener = listener;
  }

  public interface iCompleteListener {
    public void drawEnd();
  }

}
