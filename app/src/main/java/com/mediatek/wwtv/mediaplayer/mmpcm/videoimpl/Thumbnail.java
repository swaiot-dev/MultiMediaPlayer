
package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import com.mediatek.mmp.util.ThumbnailInfo;

import android.media.*;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video.Thumbnails;
import android.media.MediaDataSource;
import android.util.Log;

import com.google.android.exoplayer.upstream.FrameworkDataSource;
import com.google.android.exoplayer.upstream.FrameworkDataSource.StreamDataSource;
//import com.google.android.exoplayer.upstream.DLNAFrameworkDataSource;

import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;
import com.mediatek.wwtv.mediaplayer.mmpcm.MmpTool;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.netcm.dlna.DLNAEXODataSource;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.MtkMediaPlayer.DataSourceType;
import com.mediatek.MtkMediaPlayer.DivxDrmInfoType;
import com.mediatek.MtkMediaPlayer.PlayerSpeed;
import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.MtkMediaMetadataRetriever;
import com.mediatek.mmp.DLNAFrameworkDataSource;
import com.mediatek.mmp.MtkMediaPlayer;
import com.mediatek.wwtv.mediaplayer.util.Util;

public class Thumbnail extends Info {
  private static final String TAG = "Thumbnail";
  private UIMediaPlayer mtkPlayer;
  private Context mContext;
  private volatile boolean thumbLoadStart = false;
  private static Thumbnail vThumb = null;
  private volatile boolean cancelLoad = false;
  private final HandlerThread mHandlerThread;

  BlockingQueue<Integer> mQueue = new ArrayBlockingQueue(2);

  public static final int INIT = 0;
  public static final int RELEASE = 1;
  public static final int DLNASOURCE = 2;
  public static final int SAMBASOURCE = 3;
  public static final int SETQUEUE = 4;

  public static final int PREPARED = 1;
  public static final int ERROR = 2;
  public static final int EXCETPION = 3;
  public static final int MEDIAPLAYER = 4;
  public static final int ABORT = 5;
  private final MyHandler mHandler;
  private boolean mHasResetRigion;
  private MtkMediaMetadataRetriever retriever = null;
  public void setRestRigionFlag(boolean hasResetRigion) {
    mHasResetRigion = hasResetRigion;
  }

  public boolean hasResetRigion() {
    return mHasResetRigion;
  }

  public void setContext(Context context) {
    mContext = context;
  }

  public Context getContext() {
    return mContext;
  }

  public void resetHandlerSource() {
      mHandler.mSource = 0;
  }

  private class MyHandler extends Handler {

    public int mSource;
    BlockingQueue<Integer> mBlockQueue = null;
    private final MtkMediaPlayer.OnPreparedListener mtkOnPreparedListener
    = new MtkMediaPlayer.OnPreparedListener() {

      public void onPrepared(MtkMediaPlayer mp) {
        Log.i(TAG, "onPrepared Thumbnail");
        try {
          Log.i(TAG, "onPrepared put PREPARED");
          mBlockQueue.put(PREPARED);
        } catch (Exception e) {
          e.printStackTrace();
        }
        Log.i(TAG, "obj notify");
      }
    };

    private final MtkMediaPlayer.OnErrorListener
    mtkOnErrorListener = new MtkMediaPlayer.OnErrorListener() {

      public boolean onError(MtkMediaPlayer mp, int what, int extra) {
        try {
          Log.i(TAG, "onError put ERROR");
          mBlockQueue.put(ERROR);
        } catch (Exception e) {
          e.printStackTrace();
        }
        return true;
      }

    };

    //private UIMediaPlayer mtkPlayer;

    public UIMediaPlayer getPlayer() {
      return mtkPlayer;
    }

    public MyHandler(Looper looper) {
      super(looper);
    }


    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case INIT:
          Log.i(TAG, "handleMessage INIT");
          mSource = -1;
          if (mtkPlayer != null) {
            mtkPlayer.setOnPreparedListener(null);
            mtkPlayer.setOnErrorListener(null);
            mtkPlayer.release();
            mtkPlayer = null;
          }
          break;
        case RELEASE:
          Log.i(TAG, "handleMessage RELEASE");
          if (mtkPlayer != null) {
            mtkPlayer.reset();
          }
          try {
            mBlockQueue.put(ABORT);
          } catch (Exception e) {
            Log.i(TAG, "handleMessage RELEASE put Error Exception");
          }
          break;
        case DLNASOURCE:
        case SAMBASOURCE:
          Log.i(TAG, "msg.what:" + msg.what);

          try {
            //Switch
            if (mSource != msg.what) {
              if (mtkPlayer != null) {
                  Log.i(TAG, "Issue Handle ");
                  //For USB case, need release
                  mtkPlayer.stop();
                  mtkPlayer.release();
              }
              mSource = msg.what;
              mtkPlayer = new UIMediaPlayer(mSource);
            }
            //Dlna case, reuse Player
            if (mtkPlayer != null) {
              mtkPlayer.reset();
            }

            mBlockQueue.put(MEDIAPLAYER);
            mFilePath = (String) msg.obj;

            mtkPlayer.setOnPreparedListener(mtkOnPreparedListener);
            mtkPlayer.setOnErrorListener(mtkOnErrorListener);
            mtkPlayer.setDataSource(mFilePath);
            Log.i(TAG, "Thread name: "
                + Thread.currentThread().getName());
            mtkPlayer.prepare();
            Log.d(TAG, "prepare done ");
          } catch (Exception e) {
            e.printStackTrace();
            try {
              mBlockQueue.put(EXCETPION);
            } catch (Exception e2) {
              e2.printStackTrace();
            }
          }
          break;
        case SETQUEUE:
          Log.i(TAG, "handleMessage SETQUEUE ");
          mBlockQueue = (ArrayBlockingQueue) msg.obj;
          break;
      }

    }
  }

  private Thumbnail() {
    mSrcType = FileConst.SRC_USB;

    mHandlerThread = new HandlerThread("Thumbnail", Process.THREAD_PRIORITY_LOWEST);
    mHandlerThread.start();

    mHandler = new MyHandler(mHandlerThread.getLooper());
    Message msg = mHandler.obtainMessage(SETQUEUE, mQueue);
    mHandler.sendMessage(msg);

  }

  public static synchronized Thumbnail getInstance() {
    if (vThumb == null) {
      vThumb = new Thumbnail();
    }
    return vThumb;
  }

  /**
   * get video thumbnail bitmap
   */
  public Bitmap getVideoThumbnail(int srcType, String filepath, int width,
      int height) throws IllegalArgumentException {
    Log.d(TAG, "filepath = " + filepath);

    if (filepath == null) {
      throw new IllegalArgumentException("empty filepath!");
    }

    mSrcType = srcType;

    //Local
    if (srcType == FileConst.SRC_USB)
    {
        if (srcType != mSrcType)
        {
          Log.i(TAG, "sendEmptyMessage INIT");
          mHandler.sendEmptyMessage(INIT);
        }

        mCacheMetaData = getMediaInfo(filepath);

        return getVideoThumbnailLocal(filepath, width, height,
          Thumbnails.MINI_KIND);
    }

    //ExoPlayer && Network
    if (Util.isUseExoPlayer() && srcType != FileConst.SRC_USB)
    {
        return getVideoThumbnaiExoPlayerNetwork(filepath, width, height,
          Thumbnails.MINI_KIND);
    }

    //CmpbPlayer && Network
    return getVideoThumbnaiCmpbPlayerNetwork(filepath, width, height,
      Thumbnails.MINI_KIND);

  }

  private void loadDone() {
    Log.d(TAG, "begin Thumbnail loadDone ");
    synchronized(this) {
        try {
          thumbLoadStart = false;
          notifyAll();
        } catch (Exception ex) {
          ex.printStackTrace();
        }
    }
    Log.d(TAG, "end Thumbnail loadDone ");
  }


private Bitmap getVideoThumbnaiExoPlayerNetwork(String videoPath,
                                    int width,
                                    int height,
                                    int kind)
{

    Bitmap bitmap = null;
    MtkMediaMetadataRetriever retriever = new MtkMediaMetadataRetriever();
    try
    {
       if (Util.isUseExoPlayer() && mSrcType != FileConst.SRC_USB)
       {
           if(mtkPlayer == null)
           {
               mtkPlayer = new UIMediaPlayer(mSrcType);
           }

           mtkPlayer.setFilePath(videoPath);
           DLNAEXODataSource tempSource = new DLNAEXODataSource(mtkPlayer);

           DLNAFrameworkDataSource frameworkDataSource = new DLNAFrameworkDataSource(tempSource, null);

           MediaDataSource mediaDataSource = (MediaDataSource)frameworkDataSource.getDataSourceForNativeExtractor();

           if (mediaDataSource != null)
           {
               //Log.i(TAG, "APP setDataSource:+:");
               retriever.setDataSource(mediaDataSource);
               //Log.i(TAG, "APP setDataSource:-");

               //Log.i(TAG, "getFrameAtTime:+:");
               bitmap = retriever.getFrameAtTime(-1);
               //Log.i(TAG, "getFrameAtTime:-");
               if(null == bitmap)
               {
                   //Log.i(TAG, "getFrameAtTime0:+:");
                   bitmap = retriever.getFrameAtTime(0);
                   //Log.i(TAG, "getFrameAtTime0:-");
               }
           }
        }
    }
    catch (IllegalArgumentException ex)
    {
        // Assume file is a corrupt video file
        Log.i(TAG, "IllegalArgumentException");
    }
    catch (RuntimeException ex)
    {
        // Assume file is a corrupt video file.
        Log.i(TAG, "RuntimeException");
    }
    finally
    {
       try {
         //Log.i(TAG, "release:+");
         retriever.release();
         //Log.i(TAG, "release:-");
       }
       catch (RuntimeException ex)
       {
         // Ignore failures while cleaning up.
         Log.i(TAG, "RuntimeException ex");
       }
    }
    //Log.i(TAG, "Thumbnail getThumbnailUtil bitmap:" + bitmap);
    if (bitmap == null)
    {
      return null;
    }

    bitmap = extractThumbnail(bitmap, width, height,
        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    return bitmap;

  }


private Bitmap getVideoThumbnaiCmpbPlayerNetwork(String videoPath,
                                                int width,
                                                int height,
                                                int kind)
{
    String tmpStr = videoPath;
    boolean isMp4 = false;
    tmpStr= tmpStr.toLowerCase();

    //For mp4 thumbnail crash issue
    //Mp4 Multi-instance Issue
    /*
    if(tmpStr.endsWith(".mp4"))
    {
       isMp4 = true;

       loadDone();
       return null;
    }
   */
    mQueue.clear();

    synchronized (this) {
      cancelLoad = false;
      thumbLoadStart = true;
    }

    Message msg = mHandler.obtainMessage(mSrcType, 0, 0, videoPath);
    mHandler.sendMessage(msg);

    //Poll out MediaPlayer
    int mediaExsit = -1;
    try {
      Integer state = mQueue.poll(1,TimeUnit.SECONDS);
      if (state == null) {
        Log.i(TAG, "mediaExsit mQueue state timeout");
      } else {
        mediaExsit = state;
      }
      Log.i(TAG, "mediaExsit ==" + mediaExsit);
    } catch (Exception e) {
      Log.i(TAG, "mediaExsit take Exception");
      loadDone();
      return null;
    }

    if (MEDIAPLAYER == mediaExsit) {
      //Log.i(TAG, "get mtkPlayer before ");
      mtkPlayer = mHandler.getPlayer();
      //Log.i(TAG, "get mtkPlayer ==" + mtkPlayer);
    } else {
      Log.i(TAG, "get mediaExsit != MEDIAPLAYER ");
      loadDone();
      return null;
    }

    //Poll out state, if not PREPARED, return
    int Situration = -1;
    try {

      Integer state1 = mQueue.poll(5,TimeUnit.SECONDS);
      if (state1 == null) {
        Log.i(TAG, "mediaExsit mQueue state1 timeout");
      } else {
        Situration = state1;
      }
      Log.i(TAG, "Situration take after:Situration == " + Situration);
    } catch (InterruptedException e) {
      // e.printStackTrace();
      Log.i(TAG, "Situration take Exception");
      loadDone();
      return null;
    }

    if (PREPARED != Situration) {
      Log.i(TAG, "Situration !==PREPARED,Issue Handle ");
      //If not PREPARED, just return
      loadDone();
      return null;
    }

    ThumbnailInfo thInfo = new ThumbnailInfo(ThumbnailInfo.RGB_D565,
        VideoConst.THUMBNAIL_WIDTH, VideoConst.THUMBNAIL_HEIGTH);
    byte[] thBuffer = null;

    synchronized (this) {
        if (!cancelLoad) {
          Log.d(TAG, "start getThumbnailInfo ");
          thBuffer = mtkPlayer.getThumbnailInfo(thInfo);
          Log.d(TAG, "end getThumbnailInfo  ");
        } else {
          Log.d(TAG, "cancelLoad == true ");
        }
    }

   Log.d(TAG, " thBuffer = "+ thBuffer);

   //Needn't stop for reuse next file
   //Next File will reset

   if (thBuffer == null)
   {
       loadDone();
       return null;
   }
   else
   {
       Bitmap bitmap = Bitmap.createBitmap(VideoConst.THUMBNAIL_WIDTH,
           VideoConst.THUMBNAIL_HEIGTH, Bitmap.Config.RGB_565);

       ByteBuffer buffer = ByteBuffer.wrap(thBuffer);
       bitmap.copyPixelsFromBuffer(buffer);

       int bitWidth = bitmap.getWidth();
       int bitHeight = bitmap.getHeight() - 2;
       float scaleWidth = width / (float) bitWidth;
       float scaleHeight = height / (float) bitHeight;
       Matrix matrix = new Matrix();
       matrix.postScale(scaleWidth, scaleHeight);
       bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitWidth, bitHeight,
           matrix, true);
       loadDone();
       return bitmap;
   }

}

  private Bitmap getVideoThumbnailLocal(String videoPath, int width, int height,
      int kind) {

    Bitmap bitmap = null;
    retriever = new MtkMediaMetadataRetriever();
    try
   {
        //MetadataRetriever exoPlayer same as AOSP
        //Cmpb need setPlayerType
        if (!Util.isUseExoPlayer())
        {
            retriever.setPlayerType(UIMediaPlayer.PLAYER_ID_CMPB_PLAYER);
        }

        Log.i(TAG, "mediaDataSource is null, Local path");
        retriever.setDataSource(videoPath);
        bitmap = retriever.getFrameAtTime(-1);
        if(null == bitmap)
        {
            //Log.i(TAG, "getFrameAtTime0:+:");
            bitmap = retriever.getFrameAtTime(0);
            //Log.i(TAG, "getFrameAtTime0:-");
        }

    }
    catch (IllegalArgumentException ex)
    {
      // Assume file is a corrupt video file
      Log.i(TAG, "IllegalArgumentException");
    }
    catch (RuntimeException ex)
    {
      // Assume file is a corrupt video file.
      Log.i(TAG, "RuntimeException");
    }
    finally
    {
       try {
        //Log.i(TAG, "release:+");
         retriever.release();
        //Log.i(TAG, "release:-");
       } catch (RuntimeException ex) {
         // Ignore failures while cleaning up.
         Log.i(TAG, "RuntimeException ex");
       }
    }
    MtkLog.i(TAG, "Thumbnail getThumbnailUtil bitmap:" + bitmap);
    if (bitmap == null) {
      return null;
    }
    bitmap = extractThumbnail(bitmap, width, height,
        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

    return bitmap;

  }

  private Bitmap extractThumbnail(Bitmap source, int width, int height,
      int options) {
    if (source == null) {
      return null;
    }

    float scale;
    if (source.getWidth() < source.getHeight()) {
      scale = width / (float) source.getWidth();
    } else {
      scale = height / (float) source.getHeight();
    }
    Matrix matrix = new Matrix();
    matrix.setScale(scale, scale);
    Bitmap thumbnail = transform(matrix, source, width, height,
        0x1 | options);
    MtkLog.i(TAG, "Thumbnail extractThumbnail thumbnail:"
        + thumbnail);
    return thumbnail;
  }

  private Bitmap transform(Matrix scaler, Bitmap source, int targetWidth,
      int targetHeight, int options) {
    boolean scaleUp = (options & 0x1) != 0;
    boolean recycle = (options & ThumbnailUtils.OPTIONS_RECYCLE_INPUT) != 0;

    int deltaX = source.getWidth() - targetWidth;
    int deltaY = source.getHeight() - targetHeight;
    if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
      /*
       * In such case the bitmap is smaller, at least in one dimension, than the target. Transform
       * it by placing as much of the image as possible into the target and leaving the top/bottom
       * or left/right (or both) black.
       */
      Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
          Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(b2);

      int deltaXHalf = Math.max(0, deltaX / 2);
      int deltaYHalf = Math.max(0, deltaY / 2);
      Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
          + Math.min(targetWidth, source.getWidth()), deltaYHalf
          + Math.min(targetHeight, source.getHeight()));
      int dstX = (targetWidth - src.width()) / 2;
      int dstY = (targetHeight - src.height()) / 2;
      Rect dst = new Rect(dstX, dstY, targetWidth - dstX, targetHeight
          - dstY);
      c.drawBitmap(source, src, dst, null);
      if (recycle) {
        source.recycle();
      }
      c.setBitmap(null);
      return b2;
    }
    float bitmapWidthF = source.getWidth();
    float bitmapHeightF = source.getHeight();

    float bitmapAspect = bitmapWidthF / bitmapHeightF;
    float viewAspect = (float) targetWidth / targetHeight;

    if (bitmapAspect > viewAspect) {
      float scale = targetHeight / bitmapHeightF;
      if (scale < .9F || scale > 1F) {
        scaler.setScale(scale, scale);
      } else {
        scaler = null;
      }
    } else {
      float scale = targetWidth / bitmapWidthF;
      if (scale < .9F || scale > 1F) {
        scaler.setScale(scale, scale);
      } else {
        scaler = null;
      }
    }

    Bitmap b1;
    if (scaler != null) {
      // used for minithumb and crop, so we want to filter here.
      b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
          source.getHeight(), scaler, true);
    } else {
      b1 = source;
    }

    if (recycle && b1 != source) {
      source.recycle();
    }

    int dx1 = Math.max(0, b1.getWidth() - targetWidth);
    int dy1 = Math.max(0, b1.getHeight() - targetHeight);

    Bitmap b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
        targetHeight);

    if (b2 != b1) {
      if (recycle || b1 != source) {
        b1.recycle();
      }
    }

    return b2;
  }

  /**
   * stop thumbnail
   */
  public void stopThumbnail() {
    Log.d(TAG, "stopThumbnail call ");

    synchronized(this) {
        while (thumbLoadStart) {
          try {
            Log.d(TAG, "stopThumbnail  cancelLoad =  " + cancelLoad);
            cancelLoad = true;
            if (mHandler != null) {
              Log.i(TAG, "sendEmptyMessage RELEASE");
              mHandler.sendEmptyMessage(RELEASE);
            } else {
              if (mtkPlayer != null) {
                mtkPlayer.reset();
              }
            }

            if (thumbLoadStart) {
              wait();
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        try {
            //Log.i(TAG, "release:+");
            if (null != retriever){
                retriever.release();
            }
            //Log.i(TAG, "release:-");
        } catch (RuntimeException ex) {
            // Ignore failures while cleaning up.
            Log.i(TAG, "RuntimeException ex");
        }
    }

    Log.d(TAG, "stopThumbnail done");
  }

  public boolean isLoadThumanil() {
    return thumbLoadStart;
  }

  public synchronized void reset() {
    if (mtkPlayer != null) {
      mtkPlayer.reset();
    }
  }

}
