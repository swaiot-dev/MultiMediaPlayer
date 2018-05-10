package com.mediatek.wwtv.mediaplayer.mmp.commonview;
import com.mediatek.wwtv.mediaplayer.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.AudioConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import com.mediatek.wwtv.mediaplayer.mmpcm.photoimpl.ConstPhoto;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.VideoConst;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo3DPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.mmp.util.DivxDisplayInfo;
import com.mediatek.wwtv.mediaplayer.mmp.util.DivxUtil;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.mmp.util.DivxPositionInfo;

/**
 * {@link PopupWindow}
 *
 * @author hs_jinbenwang
 *
 */
public class MeteDataView extends PopupWindow {

	private static final String TAG = "MeteDatalView";

	private Context mContext;


	private TextView mRateName;

	private TextView mTitleName;

	private TextView mChapterName;

	private TextView mAudioName;

	private TextView mSubtitleName;

	private TextView mRateContent;

	private TextView mTitleContent;

	private TextView mChapterContent;

	private TextView mAudioContent;

	private TextView mSubtitleContent;



	private LogicManager mLogicManager;

	private View mView ;


	public MeteDataView(View contentView) {
		super(contentView);
	}

	public MeteDataView(Context context, View contentView, int width,
			int height) {
		super(contentView, width, height);
		mView = contentView;
		mContext = context;
		mLogicManager = LogicManager.getInstance(context);
		initView();
	}
	//add by shuming for fix CR: DTV00407914

	private void initView() {
		// TODO Auto-generated method stub
		mRateName = (TextView)mView.findViewById(R.id.ratename);
		mRateContent = (TextView)mView.findViewById(R.id.ratecontent);
		mTitleName = (TextView)mView.findViewById(R.id.titlename);
		mTitleContent = (TextView)mView.findViewById(R.id.titlecontent);
		mChapterName = (TextView)mView.findViewById(R.id.chaptername);
		mChapterContent = (TextView)mView.findViewById(R.id.chaptercontent);
		mAudioName = (TextView)mView.findViewById(R.id.audioname);
		mAudioContent = (TextView)mView.findViewById(R.id.audiocontent);
		mSubtitleName = (TextView)mView.findViewById(R.id.subtitlename);
		mSubtitleContent = (TextView)mView.findViewById(R.id.subtitlecontent);
	}

	public void hiddlen(int visibility) {
		if(!DivxUtil.isDivxFormatFile(mContext)){
			mView.setVisibility(View.GONE);
		}else{
			mView.setVisibility(visibility);
			if(View.VISIBLE == visibility && null != mContext ){
				getAllContent();
			}
		}
	}

	public boolean isShowed(){
		boolean isShowed = false;
		if(mView != null && mView.getVisibility() == View.VISIBLE){
			isShowed = true;
		}
		MtkLog.i(TAG, "isShowed:"+isShowed);
		return isShowed;
	}

	public void getAllContent() {
		// TODO Auto-generated method stub
		DivxPositionInfo positioninfo = DivxUtil.getDivxPositionInfo(mContext);
		if(positioninfo != null){
			DivxDisplayInfo displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.EDITION_NAME);

			if(displayInfo != null && displayInfo.ps_utf_name!=null && !"".equals(displayInfo.ps_utf_name)){
				setTitleContent(displayInfo.ps_utf_name);
				displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.EDITION_RATE);
				if(null != displayInfo){
					setRateContent(displayInfo.ps_utf_name);
				}
			}else{
				displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.TITLE_NAME);
				if(displayInfo != null){
					setTitleContent(displayInfo.ps_utf_name);
					displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.TITLE_NAME);
					if(null != displayInfo ){
						setRateContent(displayInfo.ps_utf_name);
					}
				}
			}

			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.CHAPTER_NAME);
			if(null != displayInfo ){
				setChapterContent(displayInfo.ps_utf_name);
			}

			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_FORMAT);
			String AudioStr = "";
			if(null != displayInfo ){
				AudioStr += displayInfo.ps_utf_name+";";
			}
			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_CHANNEL_CONF);
			if(null != displayInfo ){
				AudioStr += displayInfo.ps_utf_name+";";
			}
			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_LANGUAGE);
			if(null != displayInfo ){
				AudioStr += transferLan(displayInfo.ps_char_name)+";";
			}
			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_NAME);
			if(null != displayInfo ){
				AudioStr += displayInfo.ps_utf_name;
			}
			setAudioContent(AudioStr);


			String subtitleStr = "";
			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.SUBTITLE_TRACK_LANGUAGE);
			if(null != displayInfo ){
				subtitleStr += transferLan(displayInfo.ps_char_name)+";";
			}
			displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.SUBTITLE_TRACK_NAME);
			if(null != displayInfo ){
				subtitleStr += displayInfo.ps_utf_name+";";
			}
			setSubTitleContent(subtitleStr);

		}

	}

	public void updateAudioTrack(){
		DivxDisplayInfo displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_FORMAT);
		String AudioStr = "";
		if(null != displayInfo ){
			AudioStr += displayInfo.ps_utf_name+";";
		}
		displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_CHANNEL_CONF);
		if(null != displayInfo ){
			AudioStr += displayInfo.ps_utf_name+";";
		}
		displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_LANGUAGE);
		if(null != displayInfo ){
			AudioStr += transferLan(displayInfo.ps_char_name)+";";
		}
		displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.AUDIO_TRACK_NAME);
		if(null != displayInfo ){
			AudioStr += displayInfo.ps_utf_name;
		}
		setAudioContent(AudioStr);
	}

	public void updateSubtitle(){
		String subtitleStr = "";
		DivxDisplayInfo displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.SUBTITLE_TRACK_LANGUAGE);
		if(null != displayInfo ){
			subtitleStr += transferLan(displayInfo.ps_char_name)+";";
		}
		displayInfo =	 DivxUtil.getDivxDisplayInfo(mContext,DivxUtil.SUBTITLE_TRACK_NAME);
		if(null != displayInfo ){
			subtitleStr += displayInfo.ps_utf_name+";";
		}
		setSubTitleContent(subtitleStr);
	}

	private static String  transferLan(String  p_str)	{
		 String pws_meta_data = "";
	    if(p_str == null ){
	        return pws_meta_data;
	    }

	    Log.i(TAG,"p_str:"+p_str);


	    if (p_str.equals("chi") ){
	        pws_meta_data = "Chinese";
	    }
	    else if (p_str.equals("eng") )
	    {
	        pws_meta_data = "English";
	    }
	    else if (p_str.equals("fre") )
	    {
	        pws_meta_data = "French";
	    }
	    else if (p_str.equals("ger") )
	    {
	        pws_meta_data = "German";
	    }
	    else if (p_str.equals("ita") )
	    {
	        pws_meta_data = "Italian";
	    }
	    else if (p_str.equals("jpn") )
	    {
	        pws_meta_data = "Japanese";
	    }
	    else if (p_str.equals("kor") )
	    {
	        pws_meta_data = "Korean";
	    }
	    else if (p_str.equals( "nor") )
	    {
	        pws_meta_data = "Norwegian";
	    }
	    else if (p_str.equals( "pol") )
	    {
	        pws_meta_data = "Polish";
	    }
	    else if (p_str.equals( "por") )
	    {
	        pws_meta_data = "Portuguese";
	    }
	    else if (p_str.equals("rus") )
	    {
	        pws_meta_data = "Russian";
	    }
	    else if (p_str.equals( "slk") )
	    {
	        pws_meta_data = "Slovak";
	    }
	    else if (p_str.equals("spa") )
	    {
	        pws_meta_data = "Spanish";
	    }
	    else if (p_str.equals( "und") )
	    {
	        pws_meta_data = "Undefined";
	    }
	    else
	    {
	        pws_meta_data = "Undefined";
	    }

	    return pws_meta_data;
	}


	public void setTitleContent(String content){
		mTitleContent.setText(content);
	}
	public void setRateContent(String content){
		mRateContent.setText(content);

	}
	public void setChapterContent(String content){
		mChapterContent.setText(content);
	}
	public void setAudioContent(String content){
		mAudioContent.setText(content);
	}
	public void setSubTitleContent(String content){
		mSubtitleContent.setText(content);
	}
}
