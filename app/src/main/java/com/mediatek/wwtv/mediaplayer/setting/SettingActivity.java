package com.mediatek.wwtv.mediaplayer.setting;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import com.mediatek.wwtv.mediaplayer.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.mediaplayer.setting.base.BaseSettingsActivity;
import com.mediatek.wwtv.mediaplayer.setting.base.SpecialOptionDealer;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.DynamicOptionFrag;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.ProgressBarFrag;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.MessageType;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.mediaplayer.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.mediaplayer.setting.widget.view.MjcDemoDialog;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.Util;
import android.os.SystemProperties;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;

public class SettingActivity extends BaseSettingsActivity implements SpecialOptionDealer {

	public static final String TAG = "SettingActivity";
	private String ItemName;

	private Context mContext;
	private int autoTimeOut = 0;
	private MtkTvAppTVBase appTV;
    private String[] hdr;
	LiveTVDialog autoAdjustDialog;
	CommonIntegration mCommonIntegration ;
	MmpApp mApp;
	private static SettingActivity mInstance =  null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mContext = this;
        MtkLog.d(TAG, "onCreate SettingActivity: ");
		mCurrAction = new Action("Menu Video", "Menu Video",
				Action.DataType.TOPVIEW);
		appTV = new MtkTvAppTVBase();
		mCommonIntegration = CommonIntegration.getInstance();
		mSpecialOptionDealer = this;
		mApp =(MmpApp) getApplication();
		mApp.add(this);
		super.onCreate(savedInstanceState);
		mInstance = this;
	}

	public static SettingActivity getInstance(){
		return mInstance;
	}

	protected void onStop() {
		super.onStop();
		mApp.remove(this);
	}

	@Override
	protected Object getInitialDataType() {
		return DataType.TOPVIEW;
	}

	@Override
	protected void updateView() {
	    int iconResId = R.drawable.play_logo;
        if (Util.mIsDolbyVision){
            iconResId = R.drawable.dolbyvision_horizontal;
        }
		switch ((Action.DataType) mState) {
		case TOPVIEW:
		case OPTIONVIEW:
		case SWICHOPTIONVIEW:
		case EFFECTOPTIONVIEW:
		case HAVESUBCHILD:
			//for 3d mode 's special
			if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_3D_MODE)){
				DynamicOptionFrag frag = new DynamicOptionFrag();
				frag.setAction(mCurrAction);
				mActionFragment = frag;
				setViewWithActionFragment(
						(mCurrAction == null ? mContext.getResources().getString(R.string.mmp_main_menuvideo)
								: mCurrAction.getTitle()),
						(mParentAction == null ? mContext.getResources().getString(
								R.string.menu_header_name) : mParentAction
								.getTitle()), "", R.drawable.play_logo);
			}else{
				refreshActionList();
				setView((mCurrAction == null ? mContext.getResources().getString(R.string.mmp_main_menuvideo)
						: mCurrAction.getTitle()),
						(mParentAction == null ? mContext.getResources().getString(
								R.string.menu_header_name) : mParentAction
								.getTitle()), "", R.drawable.play_logo);
			}
			break;
		case DIALOGPOP:
			MtkLog.d(TAG, "mCurrAction.mItemID=="+mCurrAction.mItemID);
    		break;
		case PROGRESSBAR:
		case POSITIONVIEW:
			ProgressBarFrag frag = new ProgressBarFrag();
			frag.setAction(mCurrAction);
			mActionFragment = frag;
			setViewWithActionFragment(
					(mCurrAction == null ? mContext.getResources().getString(R.string.mmp_main_menuvideo)
							: mCurrAction.getTitle()),
					(mParentAction == null ? mContext.getResources().getString(
							R.string.menu_header_name) : mParentAction
							.getTitle()), "", R.drawable.play_logo);
			break;
		case LASTVIEW:
			break;
		default:
			break;
		}

	}



	@Override
	protected void refreshActionList() {
		mActions.clear();
    	switch((Action.DataType)mState){
    	//handle havesubchild view special
    	case HAVESUBCHILD:
    		if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_COLOR_TEMPERATURE)){
    			loadColorTempretureData();
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_ADVANCED_VIDEO)){
    			loadAdvancedData();
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else if(mCurrAction.mItemID.equals(MenuConfigManager.VIDEO_3D)){
    			load3DData();
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}else{
    			mActions.addAll(mCurrAction.mSubChildGroup);
    		}
    		break;
    	default:
    		super.refreshActionList();
    	}

	}

	@Override
	protected void setActionsForTopView() {
		this.loadDataVideo(mCurrAction);
		mActions.addAll(mCurrAction.mSubChildGroup);
	}



	private OnDismissListener mListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			mContent.setVisibility(View.VISIBLE);
		}

	};

	@Override
	public void onActionClicked(Action action) {
	    MtkLog.d(TAG,"onActionClicked,action.mItemID=="+action.mItemID);
		if(action.mItemID.equals(MenuConfigManager.DEMO)){//for mjc demo
			MjcDemoDialog mjcDemoDialog = new MjcDemoDialog(this);
			mjcDemoDialog.setOnDismissListener(mListener);
			mjcDemoDialog.show();
			mContent.setVisibility(View.GONE);
		} else if (action.mItemID.equals(MenuConfigManager.RESET_SETTING)){//for reset setting of dolby vision
            mTV.setConfigValue(MenuConfigManager.RESET_SETTING, 0);
            Toast.makeText(this, R.string.menu_video_dovi_mode_reset, Toast.LENGTH_SHORT).show();
            setState(getInitialDataType(), true);
		}else if(action.mDataType == Action.DataType.DIALOGPOP){
			if(action.mItemID.equals(MenuConfigManager.AUTO_ADJUST)){
    			autoAdjustInfo(mContext.getString(R.string.menu_video_auto_adjust_info));
		    }else{
		    	autoAdjustInfo(mContext.getString(R.string.menu_video_auto_color_info));
		    }
			Message message = new Message();
			message.obj = action;
    		if (action.mItemID.equals(MenuConfigManager.AUTO_ADJUST)) {
			    appTV.setAutoClockPhasePostion(mCommonIntegration.getCurrentFocus());
			    message.what = MessageType.MESSAGE_AUTOADJUST;
			} else if (action.mItemID.equals(MenuConfigManager.FV_AUTOCOLOR)) {
                appTV.setAutoColor(mCommonIntegration.getCurrentFocus());
                message.what = MessageType.MESSAGE_AUTOCOLOR;
			} else {
			    appTV.setAutoClockPhasePostion(mCommonIntegration.getCurrentFocus());
			    message.what = MessageType.MESSAGE_AUTOADJUST;
			}
    		mHandler.sendMessageDelayed(message,
                    MessageType.delayMillis6);
		}else{
			super.onActionClicked(action);
		}
	}

	@Override
	protected void setProperty(boolean enable) {

	}

	protected void setViewWithActionFragment(String title, String breadcrumb,
			String description, int iconResId) {
		mContentFragment = ContentFragment.newInstance(title, breadcrumb,
				description, iconResId,
				getResources().getColor(R.color.icon_background));
		setContentAndActionFragments(mContentFragment, mActionFragment);
	}

	private void autoAdjustInfo(String mShowMessage) {
		MtkLog.d(TAG, "autoAdjustInfo");
		autoAdjustDialog = new LiveTVDialog(this, 5);
		autoAdjustDialog.setMessage(mShowMessage);
		autoAdjustDialog.show();
		autoAdjustDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				return true;
			}
		});
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MessageType.MESSAGE_AUTOADJUST:
			case MessageType.MESSAGE_AUTOCOLOR:
				Action mobj = (Action)msg.obj;
			    MtkLog.d(TAG, "MESSAGE_AUTOADJUST fro id:"+mobj.mItemID);
			    boolean flag = true;
			    autoTimeOut++;
			    if (mobj.mItemID.equals(MenuConfigManager.FV_AUTOPHASE)) {
			        flag =appTV.AutoClockPhasePostionCondSuccess(mIntegration.getCurrentFocus());
                }else {
                    flag =appTV.AutoColorCondSuccess(mIntegration.getCurrentFocus());
                }
			    if (flag || autoTimeOut >= 5) {
			        autoTimeOut = 0;
			        autoAdjustDialog.dismiss();
	                if (mobj.mItemID.equals(MenuConfigManager.AUTO_ADJUST)){
	                	loadVGA(mVideoVga);
	                	ActionFragment actFrag = (ActionFragment) mActionFragment;
	                	mActions.clear();
	                	mActions.addAll(mVideoVga.mSubChildGroup);
	                	((ActionAdapter) actFrag.getAdapter()).setActions(mActions);
	                }
	                mHandler.removeMessages(MessageType.MESSAGE_AUTOADJUST);
	                mHandler.removeMessages(MessageType.MESSAGE_AUTOCOLOR);
                }else {
                	Message message = new Message();
                	message.what = MessageType.MESSAGE_AUTOADJUST;
                	message.obj = mobj;
                    sendMessageDelayed(message,
                            MessageType.delayMillis6);
                }
			        break;
			default:
				break;
			}
		}
	};

	/**
	 * load data for video
	 */
	private String[] pictureMode;
    private String[] doViRestore;
	private String[] doViNoti;
	private String[] gamma;
	private String[] colorTemperature2;
	private String[] dnr;
	private String[] videoMpegNr;
	private String[] luma;
	private String[] fleshTone;
	private String[] videoDiFilm;
	private String[] blueStretch;
	private String[] gameMode;
	private String[] pqsplitdemoMode;
	private String[] vgaMode;
	private String[] hdmiMode;
	private String[] m3DModeArr;
	private String[] m3DModeStrArr;
	private String[] mBlackBar;
	private String[] mSuperResolution;
	String[] m3DNavArr;
	String[] m3D2DArr;
	String[] m3DImgSafetyArr;
	String[] m3DLrSwitchArr;
	private String[] blueMute;
	private String[] MSI;
	private String[] Ginga;
	// effect
	private String[] videoEffect;
	// mjc mode
	private String[] videoMjcMode;

	private Action mColorTemperature;
	private Action mColorTemperature2;
	private Action mAdvancedVideo;
	private Action mPictureMode;
	private Action mVideoVga;
	private Action m3DItem;
	private Action mAutoView;

	public void loadDataVideo(Action menuVideo) {
        if (Util.mIsDolbyVision) {
            pictureMode = mContext.getResources().getStringArray(
                    R.array.picture_effect_array_dovi);
    	    doViRestore = mContext.getResources().getStringArray(
                R.array.dolby_vision_restore);
        } else {
            pictureMode = mContext.getResources().getStringArray(
				R.array.picture_effect_array);
        }

        doViNoti = mContext.getResources().getStringArray(
            R.array.dolby_vision_notification);
		gamma = mContext.getResources().getStringArray(
				R.array.menu_video_gamma_array);
		colorTemperature2 = mContext.getResources().getStringArray(
				R.array.menu_video_color_temperature2_array);
		dnr = mContext.getResources().getStringArray(
				R.array.menu_video_dnr_array);
		videoMpegNr = mContext.getResources().getStringArray(
				R.array.menu_video_mpeg_nr_array);
		videoDiFilm = mContext.getResources().getStringArray(
				R.array.menu_video_di_film_mode_array);
		luma = mContext.getResources().getStringArray(
				R.array.menu_video_luma_array);
//		if (mTV.isCNRegion()) {
//			fleshTone = mContext.getResources().getStringArray(
//					R.array.menu_video_flesh_tone_array);
//		} else {
			fleshTone = mContext.getResources().getStringArray(
					R.array.menu_video_flesh_tone_us_array);
//		}
		mBlackBar = mContext.getResources().getStringArray(
				R.array.menu_video_black_bar_detection_array);

		mSuperResolution = mContext.getResources().getStringArray(
				R.array.menu_video_super_resolution_array);

		blueStretch = mContext.getResources().getStringArray(
				R.array.menu_video_blue_stretch_array);
		gameMode = mContext.getResources().getStringArray(
				R.array.menu_video_game_mode_array);
		blueMute = mContext.getResources().getStringArray(
				R.array.menu_setup_blue_mute_array);
		pqsplitdemoMode = mContext.getResources().getStringArray(
				R.array.menu_video_pq_split_mode_array);
		vgaMode = mContext.getResources().getStringArray(
				R.array.menu_video_vga_mode_array);
		hdmiMode = mContext.getResources().getStringArray(
				R.array.menu_video_hdmi_mode_array);
		// effect
		videoEffect = mContext.getResources().getStringArray(
				R.array.menu_video_mjc_effect_array);
		// demo partition
		/*
		 * videoDemoPa = mContext.getResources().getStringArray(
		 * R.array.menu_video_mjc_demo_partition_array);
		 */
		// Mjc Mode
		videoMjcMode = mContext.getResources().getStringArray(
				R.array.menu_video_mjc_demo_partition_array);
		// 3d
		m3DModeArr = mContext.getResources().getStringArray(
				R.array.menu_video_3d_mode_array);
		// 3d navigation
		m3DNavArr = mContext.getResources().getStringArray(
				R.array.menu_video_3d_nav_array);
		m3D2DArr = mContext.getResources().getStringArray(
				R.array.menu_video_3d_3t2switch_array);
		m3DImgSafetyArr = mContext.getResources().getStringArray(
				R.array.menu_video_3d_image_safety_array);
		m3DLrSwitchArr = mContext.getResources().getStringArray(
				R.array.menu_video_3d_lrswitch_array);

        // HDR
        hdr = mContext.getResources().getStringArray(
                R.array.menu_video_hdr_array);

		List<Action> mVideoDataGroup = new ArrayList<Action>();
		ItemName = MenuConfigManager.PICTURE_MODE;
        int cur = mTV.getConfigValue(MenuConfigManager.PICTURE_MODE);
		int initValue = mConfigManager.getDefault(ItemName) - mConfigManager.getMin(ItemName);
		MtkLog.d(TAG, "PICTURE_MODE cur: " + cur);
		if (cur == 5) {
		    initValue = 0;
		} else if (cur == 6){
		    initValue = 1;
        }
		mPictureMode = new Action(ItemName,
				mContext.getString(R.string.menu_video_picture_mode),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				initValue, pictureMode,
				MenuConfigManager.STEP_VALUE, Action.DataType.EFFECTOPTIONVIEW);

		mPictureMode.mEffectGroup = new ArrayList<Action>();
		mVideoDataGroup.add(mPictureMode);
		mPictureMode.setUserDefined(0);
		
        mAutoView = new Action(MenuConfigManager.AUTO_VIEW,mContext.getString(R.string.menu_video_auto_view),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				mConfigManager.getDefault(MenuConfigManager.AUTO_VIEW),blueMute,
				MenuConfigManager.STEP_VALUE,Action.DataType.OPTIONVIEW);
		if (isAutoView()){
			mVideoDataGroup.add(mAutoView);
		}

		//hide Dolby Vision Notification function
//        ItemName = MenuConfigManager.NOTIFY_SWITCH;
//        Action notifySwitch = new Action(ItemName,
//                mContext.getString(R.string.menu_video_notification),
//                MenuConfigManager.INVALID_VALUE,
//                MenuConfigManager.INVALID_VALUE,
//                mTV.getConfigValue(MenuConfigManager.NOTIFY_SWITCH), doViNoti,
//                MenuConfigManager.STEP_VALUE,
//                Action.DataType.SWICHOPTIONVIEW);
//        mVideoDataGroup.add(notifySwitch);

        if (Util.mIsDolbyVision){
            // reset setting
			ItemName = MenuConfigManager.RESET_SETTING;
			Action resetSetting = new Action(ItemName,
					mContext.getString(R.string.menu_video_restore),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE, null,
					MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
            mVideoDataGroup.add(resetSetting);
        }
		ItemName = MenuConfigManager.BACKLIGHT;
		Action mBackLight = new Action(ItemName,
				mContext.getString(R.string.menu_video_backlight),
				mConfigManager.getMin(ItemName),
				mConfigManager.getMax(ItemName),
				mConfigManager.getDefault(ItemName), null,
				MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
		if (MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.AUTO_VIEW)==1){
			mBackLight.setEnabled(false);
		}else {
			mBackLight.setEnabled(true);
			MenuConfigManager.getInstance(mContext).setValue(MenuConfigManager.BACKLIGHT,
					MenuConfigManager.getInstance(mContext).getDefault(MenuConfigManager.BACKLIGHT),
					mBackLight);
		}
		mVideoDataGroup.add(mBackLight);

		ItemName = MenuConfigManager.BRIGHTNESS;
		Action mBrightness = new Action(ItemName,
				mContext.getString(R.string.menu_video_brighttness),
				mConfigManager.getMin(ItemName),
				mConfigManager.getMax(ItemName),
				mConfigManager.getDefault(ItemName), null,
				MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
		mBrightness.mParent = mPictureMode;
		mVideoDataGroup.add(mBrightness);

		ItemName = MenuConfigManager.CONTRAST;
		Action mContrast = new Action(ItemName,
				mContext.getString(R.string.menu_video_contrast),
				mConfigManager.getMin(ItemName),
				mConfigManager.getMax(ItemName),
				mConfigManager.getDefault(ItemName), null,
				MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
		mContrast.mParent = mPictureMode;
		mVideoDataGroup.add(mContrast);

		ItemName = MenuConfigManager.SATURATION;
		Action mSaturation = new Action(ItemName,
				mContext.getString(R.string.menu_video_saturation),
				mConfigManager.getMin(ItemName),
				mConfigManager.getMax(ItemName),
				mConfigManager.getDefault(ItemName), null,
				MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
		mSaturation.mParent = mPictureMode;

		ItemName = MenuConfigManager.HUE;
		Action mHue = new Action(ItemName,
				mContext.getString(R.string.menu_video_hue),
				mConfigManager.getMin(ItemName),
				mConfigManager.getMax(ItemName),
				mConfigManager.getDefault(ItemName), null,
				MenuConfigManager.STEP_VALUE, Action.DataType.POSITIONVIEW);
		mHue.mParent = mPictureMode;

		// when not VGA,show definition,when VGA,not show
		Action mSharpness = null;
		if (!mTV.isCurrentSourceVGA()) {
			mVideoDataGroup.add(mSaturation);
			mVideoDataGroup.add(mHue);
			ItemName = MenuConfigManager.SHARPNESS;
			mSharpness = new Action(ItemName,
					mContext.getString(R.string.menu_video_sharpness),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mSharpness.mParent = mPictureMode;
			int index = 5;
			if (CommonIntegration.getInstance().getCurrentFocus()
					.equalsIgnoreCase("sub")) {
				index = 5;
			} else {
				mVideoDataGroup.add(mSharpness);
				index = 6;
			}

			for (int i = 1; i <= index; i++) {
				mPictureMode.mEffectGroup.add(mVideoDataGroup.get(i));
			}
		} else {
			int index = 3;
			// fix 00621892 add this 2 item not only for US
			mVideoDataGroup.add(mSaturation);
			mVideoDataGroup.add(mHue);
			index = 5;
			for (int i = 1; i <= index; i++) {
				mPictureMode.mEffectGroup.add(mVideoDataGroup.get(i));
			}
		}
		if (CommonIntegration.getInstance().getCurrentFocus()
				.equalsIgnoreCase("sub")
				&& mTV.isCurrentSourceTv()) {

			if (mVideoDataGroup.contains(mBackLight)) {
				mVideoDataGroup.remove(mBackLight);
			}
			if (mVideoDataGroup.contains(mSharpness)) {
				mVideoDataGroup.remove(mSharpness);
			}
			if (mPictureMode.mEffectGroup.contains(mBackLight)) {
				mPictureMode.mEffectGroup.remove(mBackLight);
			}
			if (mPictureMode.mEffectGroup.contains(mSharpness)) {
				mPictureMode.mEffectGroup.remove(mSharpness);
			}

		} else {
			mAdvancedVideo = new Action(MenuConfigManager.VIDEO_ADVANCED_VIDEO,
					mContext.getString(R.string.menu_video_advancedvideo),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE, null,
					MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
			mAdvancedVideo.mSubChildGroup = new ArrayList<Action>();
			if (CommonIntegration.getInstance().getCurrentFocus()
					.equalsIgnoreCase("sub")
					&& !mTV.isCurrentSourceTv()) {

			} else {
				//hide Gamma function
//				ItemName = MenuConfigManager.GAMMA;
//				mVideoDataGroup.add(new Action(ItemName, mContext
//						.getString(R.string.menu_video_gamma),
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE, mConfigManager
//								.getDefault(ItemName), gamma,
//						MenuConfigManager.STEP_VALUE,
//						Action.DataType.OPTIONVIEW));
				// del layer color temperature
//				mColorTemperature = new Action(
//						MenuConfigManager.VIDEO_COLOR_TEMPERATURE,
//						mContext.getString(R.string.menu_video_color_temperature),
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE, null,
//						MenuConfigManager.STEP_VALUE,
//						Action.DataType.HAVESUBCHILD);
//				mColorTemperature.mSubChildGroup = new ArrayList<Action>();
				ItemName = MenuConfigManager.COLOR_TEMPERATURE;
				int mInitValue;
				int value = mConfigManager.getDefault(ItemName)
						- mConfigManager.getMin(ItemName);
				switch (value){
					case 0:
						mInitValue = 1;
						break;
					case 1:
						mInitValue = 0;
						break;
					case 2:
						mInitValue = 1;
						break;
					case 3:
						mInitValue = 2;
						break;
					default:
						mInitValue = 1;
						break;
				}
				mColorTemperature = new Action(ItemName,
						mContext.getString(R.string.menu_video_color_temperature),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mInitValue,
						colorTemperature2, MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mVideoDataGroup.add(mColorTemperature);
				mColorTemperature.setmParentGroup(mVideoDataGroup);
                if (Util.mIsDolbyVision) {
                    mColorTemperature.setEnabled(false);
                }

			}
//			// loadAdvancedData();
//			if (CommonIntegration.getInstance().getCurrentFocus()
//					.equalsIgnoreCase("sub")) {
//
//				if (mTV.isCurrentSourceVGA() && mTV.isUSRegion()) {
//					mVideoDataGroup.add(mAdvancedVideo);
//					mAdvancedVideo.setmParentGroup(mVideoDataGroup);
//				}
//				if (mTV.isCurrentSourceTv()) {
//					mVideoDataGroup.add(mAdvancedVideo);
//					mAdvancedVideo.setmParentGroup(mVideoDataGroup);
//				}
//			} else {
//				mVideoDataGroup.add(mAdvancedVideo);
//				mAdvancedVideo.setmParentGroup(mVideoDataGroup);
//			}
			// only VGA have fix CR DTV00580520 & CR DTV00580679
			if (mTV.isCurrentSourceVGA()) {
				List<Action> mAdjustDataGroup = new ArrayList<Action>();
				// VGA
				ItemName = MenuConfigManager.VGA;
				mVideoVga = new Action(ItemName,
						mContext.getString(R.string.menu_video_vga),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.HAVESUBCHILD);
				mVideoVga.mSubChildGroup = mAdjustDataGroup;

				// AUTO ADJUST
				ItemName = MenuConfigManager.AUTO_ADJUST;
				Action mVideoAutoAdjust = new Action(ItemName,
						mContext.getString(R.string.menu_video_auto_adjust),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.DIALOGPOP);
				mAdjustDataGroup.add(mVideoAutoAdjust);
				mVideoAutoAdjust.hasRealChild = false;
				mVideoAutoAdjust.mSubChildGroup = new ArrayList<Action>();
				mVideoAutoAdjust.setmParentGroup(mVideoVga.mSubChildGroup);

				// H POSITION
				ItemName = MenuConfigManager.HPOSITION;
				Action mVideoAutoHPosition = new Action(ItemName,
						mContext.getString(R.string.menu_video_hposition),
						mConfigManager.getMin(ItemName),
						mConfigManager.getMax(ItemName),
						mConfigManager.getDefault(ItemName), null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.PROGRESSBAR);
				mAdjustDataGroup.add(mVideoAutoHPosition);
				mVideoAutoHPosition.setmParentGroup(mVideoVga.mSubChildGroup);

				// V POSITION
				ItemName = MenuConfigManager.VPOSITION;
				Action mVideoAutoVPosition = new Action(ItemName,
						mContext.getString(R.string.menu_video_vposition),
						mConfigManager.getMin(ItemName),
						mConfigManager.getMax(ItemName),
						mConfigManager.getDefault(ItemName), null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.PROGRESSBAR);
				mAdjustDataGroup.add(mVideoAutoVPosition);
				mVideoAutoVPosition.setmParentGroup(mVideoVga.mSubChildGroup);

				// PHASE
				ItemName = MenuConfigManager.PHASE;
				Action mVideoAutoVPhase = new Action(ItemName,
						mContext.getString(R.string.menu_video_phase),
						mConfigManager.getMin(ItemName),
						mConfigManager.getMax(ItemName),
						mConfigManager.getDefault(ItemName), null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.PROGRESSBAR);
				mAdjustDataGroup.add(mVideoAutoVPhase);
				mVideoAutoVPhase.setmParentGroup(mVideoVga.mSubChildGroup);

				// CLOCK
				ItemName = MenuConfigManager.CLOCK;
				Action mVideoAutoClock = new Action(ItemName,
						mContext.getString(R.string.menu_video_clock),
						mConfigManager.getMin(ItemName),
						mConfigManager.getMax(ItemName),
						mConfigManager.getDefault(ItemName), null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.PROGRESSBAR);
				mAdjustDataGroup.add(mVideoAutoClock);
				mVideoAutoClock.setmParentGroup(mVideoVga.mSubChildGroup);

				mVideoDataGroup.add(mVideoVga);
				// fix CR DTV00581901
				if (mTV.iCurrentInputSourceHasSignal()) {
					mVideoVga.setEnabled(true);
				} else {
					mVideoVga.setEnabled(false);
				}
				mVideoVga.setmParentGroup(mVideoDataGroup);
			}
			// if not 3d panel don't show menu/video/3D
			if (mTV.isConfigVisible(MenuConfigManager.VIDEO_3D)) {
				// to do 3D fix CR DTV00580522
				m3DItem = new Action(MenuConfigManager.VIDEO_3D,
						mContext.getString(R.string.menu_video_3d),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, null,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.HAVESUBCHILD);
				m3DItem.mSubChildGroup = new ArrayList<Action>();
				mVideoDataGroup.add(m3DItem);
				m3DItem.setmParentGroup(mVideoDataGroup);

                if (Util.mIsUseEXOPlayer) {
                    m3DItem.setEnabled(false);
                }
				MtkLog.d(TAG, "this is 3d panel");
			} else {
				MtkLog.d(TAG, "this is not 3d panel so needn't 3D item");
			}

			if (CommonIntegration.getInstance().getCurrentFocus()
					.equalsIgnoreCase("sub")) {
				if (mTV.isCurrentSourceVGA() && mTV.isUSRegion()) {
					ItemName = MenuConfigManager.VGA_MODE;
					Action mVideoVgaMode = new Action(ItemName,
							mContext.getString(R.string.menu_video_vga_mode),
							MenuConfigManager.INVALID_VALUE,
							MenuConfigManager.INVALID_VALUE,
							mConfigManager.getDefault(ItemName), vgaMode,
							MenuConfigManager.STEP_VALUE,
							Action.DataType.OPTIONVIEW);
					mVideoDataGroup.add(mVideoVgaMode);
				/*ItemName = MenuConfigManager.BLACK_BAR_DETECTION;
				Action blackbarItem = new Action(ItemName,
						mContext.getString(R.string.menu_video_black_bar),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), mBlackBar,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(blackbarItem);*/ //by dcc
				}
				if(mTV.isCurrentSourceVGA() && mTV.isEURegion()){
					ItemName = MenuConfigManager.VGA_MODE;
					Action mVideoVgaMode = new Action(ItemName,
							mContext.getString(R.string.menu_video_vga_mode),
							MenuConfigManager.INVALID_VALUE,
							MenuConfigManager.INVALID_VALUE,
							mConfigManager.getDefault(ItemName), vgaMode,
							MenuConfigManager.STEP_VALUE,
							Action.DataType.OPTIONVIEW);
					mVideoDataGroup.add(mVideoVgaMode);
				}
				if (!mTV.isSARegion() && !mTV.isCurrentSourceTv()) {

				}
			}else {
				ItemName = MenuConfigManager.DNR;
				Action dnrItem = new Action(ItemName,
						mContext.getString(R.string.menu_video_dnr),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName)
								- mConfigManager.getMin(ItemName), dnr,
						MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

				ItemName = MenuConfigManager.MPEG_NR;
				Action mpegItem = new Action(ItemName,
						mContext.getString(R.string.menu_video_mpeg_nr),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName)
								- mConfigManager.getMin(ItemName), videoMpegNr,
						MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
				if (CommonIntegration.getInstance().getCurrentFocus()
						.equalsIgnoreCase("sub")) {
					dnrItem.setEnabled(false);
				}
				if (!mTV.isCurrentSourceVGA()) {
					mVideoDataGroup.add(dnrItem);
					mVideoDataGroup.add(mpegItem);
				}

				ItemName = MenuConfigManager.MJC;
				Action mVideoMjc = new Action(ItemName,
						mContext.getString(R.string.menu_video_mjc),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, null,
						MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
				mVideoMjc.mSubChildGroup = new ArrayList<Action>();
//				mVideoDataGroup.add(mVideoMjc);
				if(isUHD()){
					mVideoDataGroup.add(mVideoMjc);
				}



				// effect
				ItemName = MenuConfigManager.EFFECT;
				Action VideoEffect = new Action(ItemName,
						mContext.getString(R.string.menu_video_mjc_effect),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), videoEffect,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.SWICHOPTIONVIEW);

				mVideoMjc.mSubChildGroup.add(VideoEffect);
				VideoEffect.setmParent(mVideoMjc);
//
//			  //demo partition ItemName = MenuConfigManager.DEMO_PARTITION;
//				Action VideoDemoPa = new Action(ItemName, mContext
//						.getString(R.string.menu_video_mjc_demo_partition),
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE, mConfigManager
//						.getDefault(ItemName) - mConfigManager.getMin(ItemName),
//						videoDemoPa, MenuConfigManager.STEP_VALUE,
//						Action.DataType.OPTIONVIEW);
//				mVideoMjc.mSubChildGroup.add(VideoDemoPa);
//				VideoDemoPa.setmParent(mVideoMjc);

//				//mode
//				ItemName = MenuConfigManager.DEMO_PARTITION;
//				Action VideoMjcMode = new Action(ItemName,
//						mContext.getString(R.string.menu_video_mjc_demo_partition),
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE,
//						mConfigManager.getDefault(ItemName), videoMjcMode,
//						MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
//				mVideoMjc.mSubChildGroup.add(VideoMjcMode);
//				VideoMjcMode.setmParent(mVideoMjc);
//
//				// demo
//				ItemName = MenuConfigManager.DEMO;
//				Action VideoDemo = new Action(ItemName,
//						mContext.getString(R.string.menu_video_mjc_demo),
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE,
//						MenuConfigManager.INVALID_VALUE, null,
//						MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
//				VideoDemo.hasRealChild = false;
//				VideoDemo.mSubChildGroup = new ArrayList<Action>();
//				VideoDemo.setmParent(mVideoMjc);
//				mVideoMjc.mSubChildGroup.add(VideoDemo);
//				if (mTV.isCNRegion()) {

					ItemName = MenuConfigManager.PQ_SPLIT_SCREEN_DEMO_MODE;
					Action pqsplitdemo = new Action(ItemName,
							mContext.getString(R.string.menu_video_pq_split_mode),
							MenuConfigManager.INVALID_VALUE,
							MenuConfigManager.INVALID_VALUE,
							mConfigManager.getDefault(ItemName)
									- mConfigManager.getMin(ItemName),
							pqsplitdemoMode, MenuConfigManager.STEP_VALUE,
							Action.DataType.OPTIONVIEW);
					mVideoMjc.mSubChildGroup.add(pqsplitdemo);
//				}

				// HDR
				ItemName = MenuConfigManager.VIDEO_HDR;
				Action mvideohdr = new Action(ItemName,
						mContext.getString(R.string.menu_video_hdr),
						mConfigManager.INVALID_VALUE,
						mConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), hdr,
						MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
//				if(mTV.isConfigVisible(MenuConfigManager.VIDEO_HDR)){
//					mVideoDataGroup.add(mvideohdr);
//				}
				if(isUHD()){
					mVideoDataGroup.add(mvideohdr);
				}


			}

		}
		menuVideo.mSubChildGroup = mVideoDataGroup;

	}

	private boolean isUHD(){
		if(Build.MODEL.contains("uhd")){
			return true;
		}else {
			return false;
		}
	}

	/**
	 * load color tempreture data
	 */
	public void loadColorTempretureData() {

		if (mColorTemperature.mSubChildGroup != null
				&& mColorTemperature.mSubChildGroup.size() > 0) {
			mColorTemperature.mSubChildGroup.clear();
		}

		if (mColorTemperature.mSubChildGroup != null) {
			ItemName = MenuConfigManager.COLOR_TEMPERATURE;
			
			int mInitValue;
			int value = mConfigManager.getDefault(ItemName)
					- mConfigManager.getMin(ItemName);
			switch (value){
				case 0:
					mInitValue = 1;
					break;
				case 1:
					mInitValue = 0;
					break;
				case 2:
					mInitValue = 1;
					break;
				case 3:
					mInitValue = 2;
					break;
				default:
					mInitValue = 1;
					break;
			}
			
			mColorTemperature2 = new Action(ItemName,
					mContext.getString(R.string.menu_video_color_temperature),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mInitValue,
					colorTemperature2, MenuConfigManager.STEP_VALUE,
					Action.DataType.OPTIONVIEW);
			mColorTemperature.mSubChildGroup.add(mColorTemperature2);

			/*ItemName = MenuConfigManager.COLOR_G_R;
			Action COLOR_G_R = new Action(ItemName,
					mContext.getString(R.string.menu_video_color_g_red),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);

			ItemName = MenuConfigManager.COLOR_G_G;
			Action COLOR_G_G = new Action(ItemName,
					mContext.getString(R.string.menu_video_color_g_green),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);

			ItemName = MenuConfigManager.COLOR_G_B;
			Action COLOR_G_B = new Action(ItemName,
					mContext.getString(R.string.menu_video_color_g_blue),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mColorTemperature.mSubChildGroup.add(COLOR_G_R);
			mColorTemperature.mSubChildGroup.add(COLOR_G_G);
			mColorTemperature.mSubChildGroup.add(COLOR_G_B);

			// mColorTemperature2.mNoEffectIndex = 0;
			// when change value of down,model change as user
			mColorTemperature2.setUserDefined(0);
			mColorTemperature2.mEffectGroup = new ArrayList<Action>();

			COLOR_G_R.mParentGroup = mColorTemperature2.mEffectGroup;
			COLOR_G_G.mParentGroup = mColorTemperature2.mEffectGroup;
			COLOR_G_B.mParentGroup = mColorTemperature2.mEffectGroup;

			mColorTemperature2.mEffectGroup.add(COLOR_G_R);
			mColorTemperature2.mEffectGroup.add(COLOR_G_G);
			mColorTemperature2.mEffectGroup.add(COLOR_G_B);

			COLOR_G_R.mParent = mColorTemperature2;
			COLOR_G_G.mParent = mColorTemperature2;
			COLOR_G_B.mParent = mColorTemperature2;*/
		}

	}
//add by yangxiong for solving "the advance ui of livetv settings is same as the mmp picture setting"
	/**
	 * Load Advanced Data
	 */
	public void loadAdvancedData() {

		if (CommonIntegration.getInstance().getCurrentFocus()
				.equalsIgnoreCase("sub")) {
			if (mTV.isCurrentSourceVGA() && mTV.isUSRegion()) {
				mAdvancedVideo.mSubChildGroup.clear();
				ItemName = MenuConfigManager.VGA_MODE;
				Action mVideoVgaMode = new Action(ItemName,
						mContext.getString(R.string.menu_video_vga_mode),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), vgaMode,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(mVideoVgaMode);
				/*ItemName = MenuConfigManager.BLACK_BAR_DETECTION;
				Action blackbarItem = new Action(ItemName,
						mContext.getString(R.string.menu_video_black_bar),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), mBlackBar,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(blackbarItem);*/ //by dcc
				return;
			}
			if(mTV.isCurrentSourceVGA() && mTV.isEURegion()){
				mAdvancedVideo.mSubChildGroup.clear();
				ItemName = MenuConfigManager.VGA_MODE;
				Action mVideoVgaMode = new Action(ItemName,
						mContext.getString(R.string.menu_video_vga_mode),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), vgaMode,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(mVideoVgaMode);
				return;
			}
			if (!mTV.isSARegion() && !mTV.isCurrentSourceTv()) {
				return;
			}
		}
		mAdvancedVideo.mSubChildGroup.clear();
		// when VGA,Advanced video only have VGA model,when other source,not
		// show this item
		ItemName = MenuConfigManager.DNR;
		Action dnrItem = new Action(ItemName,
				mContext.getString(R.string.menu_video_dnr),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				mConfigManager.getDefault(ItemName)
						- mConfigManager.getMin(ItemName), dnr,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

		ItemName = MenuConfigManager.MPEG_NR;
		Action mpegItem = new Action(ItemName,
				mContext.getString(R.string.menu_video_mpeg_nr),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				mConfigManager.getDefault(ItemName)
						- mConfigManager.getMin(ItemName), videoMpegNr,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

		ItemName = MenuConfigManager.ADAPTIVE_LUMA_CONTROL;
		Action adaptLumaItem = new Action(ItemName,
				mContext.getString(R.string.menu_video_luma),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				mConfigManager.getDefault(ItemName)
						- mConfigManager.getMin(ItemName), luma,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

		ItemName = MenuConfigManager.FLESH_TONE;
		Action fleshToneItem = new Action(ItemName,
				mContext.getString(R.string.menu_video_flesh_tone),
				MenuConfigManager.INVALID_VALUE,
				MenuConfigManager.INVALID_VALUE,
				mConfigManager.getDefault(ItemName)
						- mConfigManager.getMin(ItemName), fleshTone,
				MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

		//	if (mTV.isSARegion()) {
		if (CommonIntegration.getInstance().getCurrentFocus()
				.equalsIgnoreCase("sub")) {
			dnrItem.setEnabled(false);
			adaptLumaItem.setEnabled(false);
		}
		if (!mTV.isCurrentSourceVGA()) {
			mAdvancedVideo.mSubChildGroup.add(dnrItem);
			mAdvancedVideo.mSubChildGroup.add(mpegItem);
			mAdvancedVideo.mSubChildGroup.add(fleshToneItem);
			mAdvancedVideo.mSubChildGroup.add(adaptLumaItem);
		}


			ItemName = MenuConfigManager.BLUE_MUTE;
			Action bluemuteItem = new Action(ItemName,
					mContext.getString(R.string.menu_setup_bluemute),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), blueMute,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			mAdvancedVideo.mSubChildGroup.add(bluemuteItem);
		//}
		// for EU
		/*if (mTV.isEURegion() || mTV.isUSRegion()||mTV.isCNRegion()) {

			if (mTV.isCurrentSourceVGA() && mTV.isEURegion()) {
				mAdvancedVideo.mSubChildGroup.clear();*/
				ItemName = MenuConfigManager.VGA_MODE;
				Action mVideoVgaMode = new Action(ItemName,
						mContext.getString(R.string.menu_video_vga_mode),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), vgaMode,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
		if (mTV.isCurrentSourceVGA()) {


			mAdvancedVideo.mSubChildGroup.add(mVideoVgaMode);
			//		return;
		}
			/*mAdvancedVideo.mSubChildGroup.add(dnrItem);
			ItemName = MenuConfigManager.MPEG_NR;
			Action mpegItem = new Action(ItemName,
					mContext.getString(R.string.menu_video_mpeg_nr),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName)
							- mConfigManager.getMin(ItemName), videoMpegNr,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
             */
			ItemName = MenuConfigManager.DI_FILM_MODE;
			Action diFilmItem = new Action(ItemName,
					mContext.getString(R.string.menu_video_di_film_mode),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), videoDiFilm,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

			ItemName = MenuConfigManager.BLUE_STRETCH;
			Action blueStrechItem = new Action(ItemName,
					mContext.getString(R.string.menu_video_blue_stretch),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mTV.getConfigValue(ItemName), blueStretch,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

			ItemName = MenuConfigManager.GAME_MODE;
			Action mVideoGameMode = new Action(ItemName,
					mContext.getString(R.string.menu_video_wme_mode),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName)
							- mConfigManager.getMin(ItemName), gameMode,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.SWICHOPTIONVIEW);

//			// only when TV model,game model can use
//			MtkTvChannelInfoBase channelInfoBase = CommonIntegration
//					.getInstance().getCurChInfo();
			if (mTV.isConfigEnabled(MenuConfigManager.GAME_MODE)) {
				mVideoGameMode.setEnabled(true);
			} else {
				mVideoGameMode.setEnabled(false);
			}

		//	mAdvancedVideo.mSubChildGroup.add(mpegItem);
		//	mAdvancedVideo.mSubChildGroup.add(adaptLumaItem);
		//	mAdvancedVideo.mSubChildGroup.add(fleshToneItem);
		if (!mTV.isCurrentSourceVGA()) {
			mAdvancedVideo.mSubChildGroup.add(diFilmItem);
		}
		mAdvancedVideo.mSubChildGroup.add(blueStrechItem);
		mAdvancedVideo.mSubChildGroup.add(mVideoGameMode);
		if (mTV.isCNRegion()) {

				ItemName = MenuConfigManager.PQ_SPLIT_SCREEN_DEMO_MODE;
				Action pqsplitdemo = new Action(ItemName,
						mContext.getString(R.string.menu_video_pq_split_mode),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName)
								- mConfigManager.getMin(ItemName),
						pqsplitdemoMode, MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(pqsplitdemo);
			}
			// MJC
			ItemName = MenuConfigManager.MJC;
			Action mVideoMjc = new Action(ItemName,
					mContext.getString(R.string.menu_video_mjc),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE, null,
					MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
			mVideoMjc.mSubChildGroup = new ArrayList<Action>();
			//mVideoMjc.setmParentGroup(mAdvancedVideo.mSubChildGroup);

			// effect
			ItemName = MenuConfigManager.EFFECT;
			Action VideoEffect = new Action(ItemName,
					mContext.getString(R.string.menu_video_mjc_effect),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), videoEffect,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.SWICHOPTIONVIEW);

			mVideoMjc.mSubChildGroup.add(VideoEffect);
			VideoEffect.setmParent(mVideoMjc);

			/*
			 * //demo partition ItemName = MenuConfigManager.DEMO_PARTITION;
			 * Action VideoDemoPa = new Action(ItemName, mContext
			 * .getString(R.string.menu_video_mjc_demo_partition),
			 * MenuConfigManager.INVALID_VALUE,
			 * MenuConfigManager.INVALID_VALUE,mConfigManager
			 * .getDefault(ItemName) - mConfigManager.getMin(ItemName),
			 * videoDemoPa, MenuConfigManager.STEP_VALUE,
			 * Action.DataType.OPTIONVIEW);
			 * mVideoMjc.mSubChildGroup.add(VideoDemoPa);
			 * VideoDemoPa.setmParent(mVideoMjc);
			 */

			// mode
			ItemName = MenuConfigManager.DEMO_PARTITION;
			Action VideoMjcMode = new Action(ItemName,
					mContext.getString(R.string.menu_video_mjc_demo_partition),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), videoMjcMode,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			mVideoMjc.mSubChildGroup.add(VideoMjcMode);
			VideoMjcMode.setmParent(mVideoMjc);

			// demo
			ItemName = MenuConfigManager.DEMO;
			Action VideoDemo = new Action(ItemName,
					mContext.getString(R.string.menu_video_mjc_demo),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE, null,
					MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);
			VideoDemo.hasRealChild = false;
			VideoDemo.mSubChildGroup = new ArrayList<Action>();
			VideoDemo.setmParent(mVideoMjc);
			mVideoMjc.mSubChildGroup.add(VideoDemo);

			VideoEffect.mEffectGroup = new ArrayList<Action>();
			VideoEffect.mEffectGroup.add(VideoMjcMode);
			VideoEffect.mEffectGroup.add(VideoDemo);
			VideoEffect.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
			// off,not available
			VideoEffect.mSwitchHashMap.put(0, new Boolean[] { false, false });
			// low,available
			VideoEffect.mSwitchHashMap.put(1, new Boolean[] { true, true });
			// middle,available
			VideoEffect.mSwitchHashMap.put(2, new Boolean[] { true, true });
			// high,available
			VideoEffect.mSwitchHashMap.put(3, new Boolean[] { true, true });

			// off,not available
			if (VideoEffect.mInitValue == 0) {
				VideoMjcMode.setEnabled(false);
				VideoDemo.setEnabled(false);
			} else {
				VideoMjcMode.setEnabled(true);
				VideoDemo.setEnabled(true);
			}

			//mAdvancedVideo.mSubChildGroup.add(mVideoMjc);

			//ItemName = MenuConfigManager.VGA_MODE;
			/*Action mVideoVgaMode = new Action(ItemName,
					mContext.getString(R.string.menu_video_vga_mode),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), vgaMode,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.SWICHOPTIONVIEW);*/
		if (mTV.isCurrentSourceVGA()/* && mTV.isUSRegion()*/) {
			//	mAdvancedVideo.mSubChildGroup.add(mVideoVgaMode);
				mVideoVgaMode.setmParentGroup(mAdvancedVideo.mSubChildGroup);

				mVideoVgaMode.mEffectGroup = new ArrayList<Action>();
				mVideoVgaMode.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
				mVideoVgaMode.mEffectGroup.add(dnrItem);
				mVideoVgaMode.mEffectGroup.add(mpegItem);
				mVideoVgaMode.mEffectGroup.add(adaptLumaItem);
				mVideoVgaMode.mEffectGroup.add(fleshToneItem);
				mVideoVgaMode.mEffectGroup.add(blueStrechItem);

				mVideoVgaMode.mSwitchHashMap.put(1, new Boolean[] { true, true,
						true, true, true });
				mVideoVgaMode.mSwitchHashMap.put(0, new Boolean[] { false,
						false, false, false, false });
				if (mVideoVgaMode.mInitValue == 0) {
					dnrItem.setEnabled(false);
					mpegItem.setEnabled(false);
					adaptLumaItem.setEnabled(false);
					fleshToneItem.setEnabled(false);
					blueStrechItem.setEnabled(false);
				} else {
					dnrItem.setEnabled(true);
					mpegItem.setEnabled(true);
					adaptLumaItem.setEnabled(true);
					fleshToneItem.setEnabled(true);
					blueStrechItem.setEnabled(true);
				}
			}

			mVideoGameMode.mEffectGroup = new ArrayList<Action>();
			mVideoGameMode.mEffectGroup.add(diFilmItem);
			mVideoGameMode.mEffectGroup.add(mVideoMjc);

			mVideoGameMode.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
			mVideoGameMode.mSwitchHashMap.put(0, new Boolean[] { true, true });
			mVideoGameMode.mSwitchHashMap
					.put(1, new Boolean[] { false, false });
			if (mVideoGameMode.mInitValue == 0) {
				mVideoMjc.setEnabled(true);
				if (CommonIntegration.getInstance().isPipOrPopState()) {
					mVideoMjc.setEnabled(false);
				} else {
					mVideoMjc.setEnabled(true);
				}
			} else {
				mVideoMjc.setEnabled(false);
			}
			if (mTV.isConfigEnabled(MenuConfigManager.DI_FILM_MODE)) {
				diFilmItem.setEnabled(true);
			} else {
				diFilmItem.setEnabled(false);
			}
		//	if (mTV.isUSRegion()) {
				/*ItemName = MenuConfigManager.BLACK_BAR_DETECTION;
				Action blackbarItem = new Action(ItemName,
						mContext.getString(R.string.menu_video_black_bar),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE,
						mConfigManager.getDefault(ItemName), mBlackBar,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(blackbarItem);*/ //by dcc
		//	}
		// if (!mTV.isCNRegion()) {
			ItemName = MenuConfigManager.SUPER_RESOLUTION;
			Action superResoItem = new Action(ItemName,
					mContext.getString(R.string.menu_video_super_resolution),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), mSuperResolution,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			mAdvancedVideo.mSubChildGroup.add(superResoItem);

			if (mTV.isEURegion() && mTV.isCurrentSourceVGA()) {
				ItemName = MenuConfigManager.GRAPHIC;
				Action graphic = new Action(
					ItemName,
						mContext.getString(R.string.menu_video_super_resolution),
						MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, mConfigManager
								.getDefault(ItemName), mSuperResolution,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.OPTIONVIEW);
				mAdvancedVideo.mSubChildGroup.add(graphic);
			}

			// HDMI Mode
			/*ItemName = MenuConfigManager.HDMI_MODE;
			Action hdmiItem = new Action(ItemName,
					mContext.getString(R.string.menu_video_hdmi_mode),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), hdmiMode,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.SWICHOPTIONVIEW);

			hdmiItem.mEffectGroup = new ArrayList<Action>();
			hdmiItem.mEffectGroup.add(dnrItem);
			hdmiItem.mEffectGroup.add(mpegItem);
			hdmiItem.mEffectGroup.add(adaptLumaItem);
			hdmiItem.mEffectGroup.add(fleshToneItem);
			hdmiItem.mEffectGroup.add(diFilmItem);
			hdmiItem.mEffectGroup.add(blueStrechItem);
			hdmiItem.mEffectGroup.add(mVideoGameMode);
			hdmiItem.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
			hdmiItem.mSwitchHashMap.put(0, new Boolean[] { true, true, true,
					true, false, true, true });
			hdmiItem.mSwitchHashMap.put(1, new Boolean[] { false, false, false,
					false, false, false, false });
			hdmiItem.mSwitchHashMap.put(2, new Boolean[] { true, true, true,
					true, false, true, true });

            // blue light
            ItemName = MenuConfigManager.BLUE_LIGHT;
            Action mBluelight = new Action(ItemName,
                    mContext.getString(R.string.menu_video_blue_light),
                    mConfigManager.getMin(ItemName),
                    mConfigManager.getMax(ItemName),
                    mConfigManager.getDefault(ItemName), null,
                    MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
            if (!mTV.isCNRegion()){
                mAdvancedVideo.mSubChildGroup.add(mBluelight);
            }

            // HDR
            ItemName = MenuConfigManager.VIDEO_HDR;
            Action mvideohdr = new Action(ItemName,
                    mContext.getString(R.string.menu_video_hdr),
                    mConfigManager.INVALID_VALUE,
                    mConfigManager.INVALID_VALUE,
                    mConfigManager.getDefault(ItemName), hdr,
                    MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
            if(mTV.isConfigVisible(MenuConfigManager.VIDEO_HDR)){
                mAdvancedVideo.mSubChildGroup.add(mvideohdr);
            }

			if (mTV.isCurrentSourceHDMI()) {
				mAdvancedVideo.mSubChildGroup.add(hdmiItem);
				// diFilmItem.setEnable(false);
				*//* DTV00584203 *//*
				if (mConfigManager.getDefault(MenuConfigManager.HDMI_MODE) != 1) {
					dnrItem.setEnabled(true);
					mpegItem.setEnabled(true);
					adaptLumaItem.setEnabled(true);
					fleshToneItem.setEnabled(true);
					blueStrechItem.setEnabled(true);
				} else {
					dnrItem.setEnabled(false);
					mpegItem.setEnabled(false);
					adaptLumaItem.setEnabled(false);
					fleshToneItem.setEnabled(false);
					blueStrechItem.setEnabled(false);
					// mVideoGameMode.setEnable(false);
				}
				if (mTV.iCurrentInputSourceHasSignal()) {
				} else {
					// mVideoGameMode.setEnable(false);
				}
			}*/ //by dcc
		//	}
	}
//end by yangxiong for solving "the advance ui of livetv settings is same as the mmp picture setting"
	/**
	 * Load 3D Data
	 */
	public void load3DData() {
		if (m3DItem.mSubChildGroup != null && m3DItem.mSubChildGroup.size() > 0) {
			m3DItem.mSubChildGroup.clear();
		}

		if (m3DItem.mSubChildGroup != null) {
			// 3d mode
			ItemName = MenuConfigManager.VIDEO_3D_MODE;
			Action m3DMode = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_mode),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), m3DModeArr,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.SWICHOPTIONVIEW);
			m3DItem.mSubChildGroup.add(m3DMode);
			m3DMode.mParent = m3DItem;
			m3DMode.mEffectGroup = new ArrayList<Action>();
			m3DMode.mSwitchHashMap = new HashMap<Integer, Boolean[]>();

			// 3d navigation
			ItemName = MenuConfigManager.VIDEO_3D_NAV;
			Action m3DNav = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_nav),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), m3DNavArr,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			m3DItem.mSubChildGroup.add(m3DNav);
			m3DNav.mParent = m3DItem;

			// 3d-2d
			ItemName = MenuConfigManager.VIDEO_3D_3T2;
			Action m3D2D = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_3t2),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), m3D2DArr,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			m3DItem.mSubChildGroup.add(m3D2D);
			m3D2D.mParent = m3DItem;

			// depth of field
			ItemName = MenuConfigManager.VIDEO_3D_FIELD;
			Action m3DDepthField = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_depth_field),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			m3DItem.mSubChildGroup.add(m3DDepthField);
			m3DDepthField.mParent = m3DItem;

			// protrude
			ItemName = MenuConfigManager.VIDEO_3D_PROTRUDE;
			Action m3DProtrude = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_protrude),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			m3DItem.mSubChildGroup.add(m3DProtrude);
			m3DProtrude.mParent = m3DItem;

			// distance to TV
			ItemName = MenuConfigManager.VIDEO_3D_DISTANCE;
			Action m3DDistance = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_distance),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			m3DItem.mSubChildGroup.add(m3DDistance);
			m3DDistance.mParent = m3DItem;

			// image safety
			ItemName = MenuConfigManager.VIDEO_3D_IMG_SFTY;
			Action m3DImgSafety = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_image_safety),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), m3DImgSafetyArr,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			m3DItem.mSubChildGroup.add(m3DImgSafety);
			m3DImgSafety.mParent = m3DItem;

			// L-R switch
			ItemName = MenuConfigManager.VIDEO_3D_LF;
			Action m3DLrSwitch = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_leftright),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					mConfigManager.getDefault(ItemName), m3DLrSwitchArr,
					MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
			m3DItem.mSubChildGroup.add(m3DLrSwitch);
			m3DLrSwitch.mParent = m3DItem;

			// // OSD Depth
			ItemName = MenuConfigManager.VIDEO_3D_OSD_DEPTH;
			Action m3DOsdDepth = new Action(ItemName,
					mContext.getString(R.string.menu_video_3d_osd),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			m3DItem.mSubChildGroup.add(m3DOsdDepth);
			m3DOsdDepth.mParent = m3DItem;

			m3DMode.mEffectGroup.add(m3DNav);
			m3DMode.mEffectGroup.add(m3D2D);
			m3DMode.mEffectGroup.add(m3DDepthField);
			m3DMode.mEffectGroup.add(m3DProtrude);
			m3DMode.mEffectGroup.add(m3DDistance);
			m3DMode.mEffectGroup.add(m3DImgSafety);
			m3DMode.mEffectGroup.add(m3DLrSwitch);
			m3DMode.mEffectGroup.add(m3DOsdDepth);
			ArrayList<Boolean> m3DConfigList = mConfigManager.get3DConfig();
			boolean m3DModeFlag = m3DConfigList.get(0);
			boolean m3DNavFlag = m3DConfigList.get(1);
			boolean m3D2DFlag = m3DConfigList.get(2);
			boolean m3DDepthFieldFlag = m3DConfigList.get(3);
			boolean m3DProtrudeFlag = m3DConfigList.get(4);
			boolean m3DDistanceFlag = m3DConfigList.get(5);
			boolean m3DImgSafetyFlag = m3DConfigList.get(6);
			boolean m3DLrSwitchFlag = m3DConfigList.get(7);
			boolean m3DOsdDepthFlag = m3DConfigList.get(8);

            if (!m3DOsdDepthFlag){
                m3DItem.mSubChildGroup.remove(m3DOsdDepth);
            }

			m3DMode.setEnabled(m3DModeFlag);
			m3DNav.setEnabled(m3DNavFlag);
			m3D2D.setEnabled(m3D2DFlag);
			m3DDepthField.setEnabled(m3DDepthFieldFlag);
			m3DProtrude.setEnabled(m3DProtrudeFlag);
			m3DDistance.setEnabled(m3DDistanceFlag);
			m3DImgSafety.setEnabled(m3DImgSafetyFlag);
			m3DLrSwitch.setEnabled(m3DLrSwitchFlag);
			m3DOsdDepth.setEnabled(m3DOsdDepthFlag);
		}
	}

	public void loadVGA(Action videoVga) {
		List<Action> mAdjustDataGroup =  videoVga.mSubChildGroup ;
		if (mAdjustDataGroup != null && mAdjustDataGroup.size() > 0) {
			mAdjustDataGroup.clear();
		}
		if (mAdjustDataGroup != null) {
			ItemName = MenuConfigManager.AUTO_ADJUST;
			Action mVideoAutoAdjust = new Action(ItemName,
					mContext.getString(R.string.menu_video_auto_adjust),
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE,
					MenuConfigManager.INVALID_VALUE, null,
					MenuConfigManager.STEP_VALUE,
					Action.DataType.HAVESUBCHILD);
			mAdjustDataGroup.add(mVideoAutoAdjust);
			mVideoAutoAdjust.mSubChildGroup = new ArrayList<Action>();

			// H POSITION
			ItemName = MenuConfigManager.HPOSITION;
			Action mVideoAutoHPosition = new Action(ItemName,
					mContext.getString(R.string.menu_video_hposition),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mAdjustDataGroup.add(mVideoAutoHPosition);

			// V POSITION
			ItemName = MenuConfigManager.VPOSITION;
			Action mVideoAutoVPosition = new Action(ItemName,
					mContext.getString(R.string.menu_video_vposition),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mAdjustDataGroup.add(mVideoAutoVPosition);

			// PHASE
			ItemName = MenuConfigManager.PHASE;
			Action mVideoAutoVPhase = new Action(ItemName,
					mContext.getString(R.string.menu_video_phase),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mAdjustDataGroup.add(mVideoAutoVPhase);

			// CLOCK
			ItemName = MenuConfigManager.CLOCK;
			Action mVideoAutoClock = new Action(ItemName,
					mContext.getString(R.string.menu_video_clock),
					mConfigManager.getMin(ItemName),
					mConfigManager.getMax(ItemName),
					mConfigManager.getDefault(ItemName), null,
					MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);
			mAdjustDataGroup.add(mVideoAutoClock);
		}
	}

	private boolean isAutoView()
	{
		return SystemProperties.get("persist.sys.config.auto_view").equals("1") ? true : false; //modified by dzh for changing the property
	}

	/**
	 * deal special item
	 */
	@Override
	public void specialOptionClick(Action currAction) {
		if(currAction.mItemID.equals(MenuConfigManager.VIDEO_3D_NAV)){
			 if (currAction.mInitValue == 2) {
				 mConfigManager.setValue(MenuConfigManager.VIDEO_3D_MODE, 1, currAction);
	         } else {
	        	 mConfigManager.setValue(MenuConfigManager.VIDEO_3D_MODE, 0, currAction);
	         }
		}
	}

}
