
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mediatek.MtkMediaPlayer.ABRpeatType;
import com.mediatek.twoworlds.tv.MtkTvVolCtrl;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo3DPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.DolbylogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.LastMemory;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.Util;

/**
 * {@link PopupWindow}
 *
 * @author hs_weihuiluo
 */
public class TextplayControlView extends ControlView
{
    private static final String TAG = "ControlView";
    private Context mContext;
    private View vControlView;
    private ImageView vPStatePlay;
    private ImageView vPStatePause;
    private TextView vPFileName;
    private TextView vPageNumber;
    private ProgressBar vMProgressBar;
    private ImageView mRepeatLogo;
    private ControlView.ControlPlayState mControlImp;
    private LogicManager mLogicManager;
    private RelativeLayout mPlayPauseLayout;
    private LinearLayout mControlbottom;
    // Added by Dan for fix bug DTV00376577
    private boolean mIsPlaying = true;
    private int mediaType;
    public TextplayControlView(View contentView)
    {
        super(contentView);
    }
    public TextplayControlView(Context context, int mediatype,
                               ControlView.ControlPlayState statecontrol, View contentView, int width,
                               int height)
    {
        super(contentView,width,height);
        mediaType = mediatype;
        vControlView = contentView;
        mControlImp = statecontrol;
        mContext = context;
        mLogicManager = LogicManager.getInstance(context);

        findCommonView();
        findTextPlayView();


        setOnDismissListener(mDismissListener);
    }

    private void setControlBottomLayoutVisible(int visibility)
    {
        if (mControlbottom != null)
        {
            mControlbottom.setVisibility(visibility);
        }
        MtkLog.i(TAG, "setControlBottomLayoutVisible:" + visibility+",mControlbottom != null==="+(mControlbottom != null));
    }

    public void hiddlen(int visibility)
    {
        setControlBottomLayoutVisible(visibility);
        //affectRepeatLogo(visibility);
    }

    private void affectRepeatLogo(int visibility)
    {
        MtkLog.i(TAG, "affectRepeatLogo:" + visibility);
    }

    private void setRepeatLogoVisible(int visibility)
    {
        if (null != mRepeatLogo)
        {
            mRepeatLogo.setVisibility(visibility);
        }
    }

    public boolean isShowed()
    {
        int isShowed = 0;
        if (vControlView != null)
        {
            MtkLog.i(TAG, "isShowed vControlView != null");
            if (mControlbottom != null && mControlbottom.getVisibility() == View.VISIBLE)
            {
                isShowed += 1;
            }

            if (mPlayPauseLayout != null && mPlayPauseLayout.getVisibility() == View.VISIBLE)
            {
                isShowed += 2;
            }
        }
        MtkLog.i(TAG, "isShowing:" + isShowed);
        if (isShowed > 0)
        {
            return true;
        } else
        {
            return false;
        }
    }

    private final OnDismissListener mDismissListener = new OnDismissListener()
    {

        @Override
        public void onDismiss()
        {
            // vControlView = null;
            // mContext = null;
        }
    };

    public void setRepeat(int type)
    {
        int casetype = mLogicManager.getRepeatModel(type);
        MtkLog.i(TAG, "casetype:" + casetype);
        switch (casetype)
        {
            case Const.REPEAT_ALL:
                setRepeatAll();
                break;
            case Const.REPEAT_NONE:
                setRepeatNone();
                break;
            case Const.REPEAT_ONE:
                setRepeatSingle();
                break;
            default:
                break;
        }
    }

    private void findCommonView()
    {
        mPlayPauseLayout = (RelativeLayout) vControlView.findViewById(R.id.mmp_pop_playstatus_layout);
        vPStatePlay = (ImageView) vControlView
                .findViewById(R.id.mmp_pop_playstateplay);
        vPStatePause = (ImageView) vControlView
                .findViewById(R.id.mmp_pop_playstatepause);
        vPFileName = (TextView) vControlView
                .findViewById(R.id.mmp_pop_filename_tv);

        mControlbottom = (LinearLayout) vControlView.findViewById(R.id.mmp_control_bottom);
    }

    private void findTextPlayView()
    {
        mRepeatLogo = (ImageView) vControlView.findViewById(R.id.mmp_text_mode);
        vPageNumber = (TextView) vControlView.findViewById(R.id.mmp_text_page_number);
        vMProgressBar = (ProgressBar)vControlView.findViewById(R.id.mmp_pop_text_progress); 
        setRepeat(Const.FILTER_TEXT);
    }

    /**
     * fix reStart play 2D,3D photo , music and video repeatOne reserves problem
     */
    public void setRepeatVisibility(int type)
    {
        setRepeat(mediaType);
    }

    public void setMediaPlayState()
    {

    }

    public void onCapture()
    {
        try
        {
            mControlImp.pause();
            mIsPlaying = false;
        } catch (Exception e)
        {
            MtkLog.i(TAG, e.getMessage());
        }
    }

    public void setSlow()
    {

    }

    public boolean isPlaying()
    {
        switch (mediaType)
        {
            case MultiMediaConstant.VIDEO:
                return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_STARTED;
            case MultiMediaConstant.AUDIO:
                return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_STARTED;
            default:
                break;
        }
        return mIsPlaying;
    }

    public boolean isPause()
    {
        switch (mediaType)
        {
            case MultiMediaConstant.VIDEO:
                return mLogicManager.getVideoPlayStatus() == VideoConst.PLAY_STATUS_PAUSED;
            case MultiMediaConstant.AUDIO:
                return mLogicManager.getAudioStatus() == AudioConst.PLAY_STATUS_PAUSED;
            default:
                break;
        }
        return !mIsPlaying;
    }

    public void setFileName(String name)
    {
        vPFileName.setText(name);
    }

    public void setFilePosition(String pagesize)
    {
        MtkLog.d(TAG, "TextplayContolView setFilePosition  pagesize==" + pagesize);
    }

    public void setRepeatAll()
    {
        MtkLog.i(TAG, "setRepeatNone()");
    }

    public void setRepeatSingle()
    {
        MtkLog.i(TAG, "setRepeatNone()");
    }

    public void setRepeatNone()
    {
        MtkLog.i(TAG, "setRepeatNone()");
    }

    public void setProgressMax(int max)
    {
        MtkLog.d(TAG, "setProgressMax max:" + max + "  " + Log.getStackTraceString(new Throwable()));
        vMProgressBar.setMax(max);
    }

    public int getProgressMax()
    {
        return vMProgressBar.getMax();
    }

    public int getCurrentProgress()
    {
        return vMProgressBar.getProgress();
    }

    public void setProgress(int progress)
    {
        MtkLog.d(TAG, "setProgress progress:" + progress
                + "  " + Log.getStackTraceString(new Throwable()));
        vMProgressBar.setProgress(progress);
    }

    public void setInforbarStop()
    {
        vMProgressBar.setProgress(0);
    }

    private void setVMProgressBarVisible(int visible)
    {
        if (null != vMProgressBar)
        {
            vMProgressBar.setVisibility(visible);
        }
    }

    public void hideProgress()
    {
        setVMProgressBarVisible(View.INVISIBLE);
    }

    public void showProgress()
    {
        setVMProgressBarVisible(View.VISIBLE);
    }
    
    //
    public void pause()
    {
        
    }
    
    public void play()
    {
        
    }
    
    public void setMute(boolean isMute)
    {
        
    }

    public void setCurrentVolume(int volume)
    {
        
    }

    public void setShuffleVisble(int visibility)
    {
        
    }

    public void showPausePlayIcon(boolean isPlaying)
    {
        
    }

    public void setZoomSize()
    {
        
    }

    public void initRepeatAB()
    {
        
    }

    public void reinitSubtitle(short number)
    {
        
    }

    public void initVideoTrackNumber()
    {
        
    }

    public void setPhotoTimeType(String type)
    {
        MtkLog.i(TAG,"setPhotoTimeType == "+type);
        vPageNumber.setText(type);
        try
        {
            String curPage = type.split("/")[0];
            String countPage = type.split("/")[1];
            int curPageNum = Integer.valueOf(curPage).intValue();
            int countPageNum = Integer.valueOf(countPage).intValue();
            setProgress(curPageNum*100/countPageNum); 
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    //
}
