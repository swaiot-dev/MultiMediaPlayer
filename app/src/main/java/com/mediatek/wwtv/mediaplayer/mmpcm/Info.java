package com.mediatek.wwtv.mediaplayer.mmpcm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jcifs.smb.SmbException;
import android.util.Log;
import com.mediatek.wwtv.mediaplayer.mmpcm.UIMediaPlayer;
import android.media.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.MediaMetadataRetriever;
import com.mediatek.MtkMediaMetadataRetriever;
import com.mediatek.wwtv.mediaplayer.util.Util;

/*
 *
 * get media Metedata base class
 */
public class Info {
    private static final String TAG ="Info";
    protected String mFilePath;
    protected int mSrcType;
    protected static MetaData mCacheMetaData = null;

    public static MetaData getCacheMetaData(){
        return mCacheMetaData;
    }

    public MetaData getMediaInfo(String path){
           MetaData mMetaInfo = new MetaData();
           MtkMediaMetadataRetriever retriever = new MtkMediaMetadataRetriever();
           try{
              // retriever.setDataSource(path);
               String KEY_RETRIEVER_PLAYER = "X-tv-Retriever-Player";
               String KEY_THUMBNAIL_PATH = "X-tv-Thumbnail-Path";

               String VALUE_RETRIEVER_PLAYER = "CMPB_PLAYER";
               String VALUE_THUMBNAIL_PATH = "THRD_USB";
               Map<String, String> Headers_t=new HashMap<String, String>();

               Headers_t = new HashMap<String,String>();


               //Use CMPB for MetaDataInfo, old style
               Headers_t.put(KEY_RETRIEVER_PLAYER,VALUE_RETRIEVER_PLAYER);

                //MetadataRetriever exoPlayer same as AOSP
                if (!Util.isUseExoPlayer())
                {
                    retriever.setPlayerType(UIMediaPlayer.PLAYER_ID_CMPB_PLAYER);
                }

                retriever.setDataSource(path);

           }catch(Exception e){

               retriever.release();

               Log.d(TAG,"setdataSource fail ~");


               return null;
           }

           String mtitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
               //cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
           //for 1401  no MediaMetadataRetriever.METADATA_KEY_DIRECTOR params and MediaMetadataRetriever.METADATA_KEY_COPYRIGHT;
           String mdirector = null;//retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DIRECTOR);
           String mcopyright = null;//retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COPYRIGHT);

           String myear = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
               //cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
           String mgenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);

           String martist =  retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
               //cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ARTIST));

           String malbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
           //cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.ALBUM));

           String mbitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
           Log.e("mbitrate", "mbitrate:"+"null:"+mbitrate+"_re:"+mgenre+"_mdirector:"+mdirector+"_mcopyright:"+mcopyright);

           String mdur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

           int dur = 0;
          try{
              dur = Integer.valueOf(mdur);

          }catch(Exception ex){
               Log.d(TAG,"duration to int error~~");
          }

           int mbitratet= 200;
           try{
               mbitratet = Integer.valueOf(mbitrate);

           }catch(Exception ex){
                Log.d(TAG,"mbitrate to int error~~");
           }

           retriever.release();

           mMetaInfo.setMetaData(mtitle, mdirector, mcopyright, myear,
                   mgenre, martist, malbum, dur,
                   mbitratet);
           Log.e(TAG, "video myear:"+myear+"_mtitle:"+mtitle+"_martist:"+martist
                   +"_malbum:"+malbum+"_mgenre:"+mgenre);

           //int videoID = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));

           return mMetaInfo;
          }

    public Bitmap getAudioBmp(String path){
         MediaMetadataRetriever retriever = new MediaMetadataRetriever();
          try{
               retriever.setDataSource(path);

           }catch(Exception e){
                   retriever.release();
               return null;
           }
         byte[] bit = retriever.getEmbeddedPicture();
         Bitmap bmp = null;
         if(bit != null){
            int len = bit.length;
            bmp = BitmapFactory.decodeByteArray(bit,0,len);
         }else{
             retriever.release();
            Log.d(TAG,"get bit = null");
            return null;
         }
         retriever.release();
         Log.d(TAG,"getAudiobmp:"+bmp);
         return bmp;

    }
}
