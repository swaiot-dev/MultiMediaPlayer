package com.mediatek.wwtv.mediaplayer.setting.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.text.TextUtils;

import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;

import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;

import com.mediatek.twoworlds.tv.MtkTvATSCRating;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
//import com.mediatek.twoworlds.tv.MtkTvChannelListBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.twoworlds.tv.MtkTvISDBRating;
import com.mediatek.twoworlds.tv.MtkTvDVBRating;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvTime;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.MtkTvUtilBase;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;



import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPInfo;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPPara;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfo;
import com.mediatek.twoworlds.tv.model.MtkTvOpenVCHIPSettingInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvUSTvRatingSettingInfo;
import com.mediatek.twoworlds.tv.model.MtkTvUSTvRatingSettingInfoBase;

import com.mediatek.twoworlds.tv.MtkTvMultiMediaBase;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;

import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.mediaplayer.setting.util.MarketRegionInfo;
import com.mediatek.wwtv.mediaplayer.setting.util.TvCallbackConst;
import com.mediatek.wwtv.mediaplayer.setting.util.TvCallbackHandler;
import com.mediatek.wwtv.mediaplayer.setting.util.Util;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import android.media.tv.TvInputManager;
import android.media.tv.TvContentRating;
import java.util.ArrayList;

public class TVContent {
	private static TVContent instance;
    public static final int VSH_SRC_TAG3D_2D  = 0;
    public static final int VSH_SRC_TAG3D_MVC = 1;      // MVC = Multi-View Codec
    public static final int VSH_SRC_TAG3D_FP  = 2;      // FP = Frame Packing
    public static final int VSH_SRC_TAG3D_FS  = 3;     // FS = Frame Sequential
    public static final int VSH_SRC_TAG3D_TB  = 4;       // TB = Top-and-Bottom
    public static final int VSH_SRC_TAG3D_SBS = 5;      // SBS = Side-by-Side
    public static final int VSH_SRC_TAG3D_REALD = 6;    //
    public static final int VSH_SRC_TAG3D_SENSIO = 7;   //
    public static final int VSH_SRC_TAG3D_LA = 8;      // LA = Line Alternative
    public static final int VSH_SRC_TAG3D_TTDO = 9;     // TTD only. It is 2D mode
    public static final int VSH_SRC_TAG3D_NOT_SUPPORT = 10;
	private MtkTvConfig mTvConfig;
	private MtkTvATSCRating mTvRatingSettingInfo;
	private MtkTvOpenVCHIPSettingInfoBase mOpenVCHIPSettingInfoBase;
	private MtkTvOpenVCHIPPara para;
	private MtkTvCI mCIBase;
	private MtkTvMultiMediaBase mMtkTvMultiMediaBase;
	private TvCallbackHandler mCallbackHandler;
//	private InputSourceManager mSourceManager;
	private boolean dumy = false;
	private HashMap<String, Integer> dumyData = new HashMap<String, Integer>();
	private int region = MarketRegionInfo.REGION_CN;
	private SaveValue saveV;
	private Context mContext;
	private String TAG = "TVContent";

	//add by sin_biaoqinggao
	//last input source's Name;
	private String lastInputSourceName ="" ;
	//current input source's Name;
	private String currInputSourceName ="";

	public final static String SAT_BRDCSTER = MtkTvConfigTypeBase.CFG_BS_BS_SAT_BRDCSTER; // DVBS OP




	protected TVContent(Context context) {
		mContext = context;
		saveV = SaveValue.getInstance(context);
		init();
	}

	private void init() {
		dumyData.clear();
		mTvConfig = MtkTvConfig.getInstance();
		mCIBase = MtkTvCI.getInstance(0);
		mTvRatingSettingInfo = MtkTvATSCRating.getInstance();
		mCallbackHandler = TvCallbackHandler.getInstance();
		region = MarketRegionInfo.getCurrentMarketRegion();
		// region = MarketRegionInfo.REGION_SA;//
		mMtkTvMultiMediaBase = new MtkTvMultiMediaBase();
	}

	static public synchronized TVContent getInstance(Context context) {
		if (instance == null) {
			instance = new TVContent(context);
		}
		return instance;
	}

//	public InputSourceManager getSourceManager() {
//		if (mSourceManager == null) {
//			mSourceManager = InputSourceManager.getInstance();
//		}
//		return mSourceManager;
//	}

	public String getSysVersion(int eType, String sVersion) {
		String version = MtkTvUtil.getInstance().getSysVersion(eType, sVersion);
		MtkLog.d(TAG, "getSysVersion" + version);
		return version;
	}

	/*
	 * US Rating function
	 */
	public MtkTvATSCRating getATSCRating() {
		if (mTvRatingSettingInfo == null) {
			mTvRatingSettingInfo = MtkTvATSCRating.getInstance();
		}
		return mTvRatingSettingInfo;
	}

	public MtkTvISDBRating getIsdbRating() {
		return MtkTvISDBRating.getInstance();
	}

	public MtkTvDVBRating getDVBRating() {
		return MtkTvDVBRating.getInstance();
	}

	public MtkTvUSTvRatingSettingInfo getUsRating() {
		return new MtkTvUSTvRatingSettingInfo();
	}



	public int getDVBTIFRatingPlus(){
		TvInputManager mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
		List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
		if(dvbRatings.size() > 0){
			List<Integer> ageList = new ArrayList<Integer>();
			for(TvContentRating rating :dvbRatings){
				String ratingName = rating.getMainRating();
				MtkLog.d(TAG, "getDVBTIFRating:ratingName =="+ratingName);
				int pos = ratingName.indexOf("_");
				if(pos != -1){
					ageList.add(Integer.parseInt(ratingName.substring(pos+1)));
				}else{
					return 2;
				}
			}
			int[] ageArray  = new int[ageList.size()];
			for(int i=0;i<ageList.size();i++){
				ageArray[i] = ageList.get(i);
			}
			Arrays.sort(ageArray);
			return ageArray[0];
		}else{
			MtkLog.d(TAG, "getDVBTIFRating:no ratings getted");
			return 2 ;
		}

	}
	/**
	 * tif tv content rating
	 * @param age
	 */
	public void genereateDVBTIFRatingPlus(int age) {
		String rate = null ;
		TvInputManager mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
		if(age == 0){
			List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
			if(dvbRatings.size() > 0){
				for(TvContentRating rating:dvbRatings){
					mTvInputManager.removeBlockedRating(rating);
				}
			}
			MtkLog.d(TAG, "remove ratings to none");
			return;

		}else{
			List<TvContentRating> dvbRatings = mTvInputManager.getBlockedRatings();
			if(dvbRatings.size() > 0){
				for(int i=age-1; i>=3; i--){
					rate = String.valueOf(i);
					TvContentRating oldRating = TvContentRating.createRating(RatingConst.RATING_DOMAIN,RatingConst.RATING_SYS_DVB_TV,"DVB_"+rate);
					if(dvbRatings.contains(oldRating)){
						MtkLog.d(TAG, "remove oldRating.flattenToString=="+oldRating.flattenToString());
						mTvInputManager.removeBlockedRating(oldRating);
					}
				}
			}
			for(int i = age;i <= 18;i ++){
				rate = String.valueOf(i);
				TvContentRating newRating = TvContentRating.createRating(RatingConst.RATING_DOMAIN,RatingConst.RATING_SYS_DVB_TV,"DVB_"+rate);
				MtkLog.d(TAG, "newRating.flattenToString=="+newRating.flattenToString());
				if(!dvbRatings.contains(newRating)){
					mTvInputManager.addBlockedRating(newRating);
				}
			}
			for(TvContentRating xRating:mTvInputManager.getBlockedRatings()){
				MtkLog.d(TAG, "xRating.String=="+xRating.flattenToString());
			}
		}
	}

	public int getRatingEnable() {
		if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
			TvInputManager mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
			boolean ret = mTvInputManager.isParentalControlsEnabled();
			Log.d("TVContent", "TIF.isParentalControlsEnabled():"+ret);
			return  ret? 1 : 0;
		}else{
			Log.d("TVContent", "mTvRatingSettingInfo.getRatingEnable():"
					+ mTvRatingSettingInfo.getRatingEnable());
			return mTvRatingSettingInfo.getRatingEnable() ? 1 : 0;
		}

	}

	public void setRatingEnable(boolean isRatingEnable) {
		if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_TIF_RATING)){
			TvInputManager mTvInputManager = (TvInputManager) mContext.getSystemService(Context.TV_INPUT_SERVICE);
			mTvInputManager.setParentalControlsEnabled(isRatingEnable);
			Log.d("TVContent", "TIF.setParentalControlsEnabled():"+isRatingEnable);
		}else{
			mTvRatingSettingInfo.setRatingEnable(isRatingEnable);
		}

	}

	public int getBlockUnrated() {
		return mTvRatingSettingInfo.getBlockUnrated() ? 1 : 0;
	}

	public void setBlockUnrated(boolean isBlockUnrated) {
		mTvRatingSettingInfo.setBlockUnrated(isBlockUnrated);
	}

	public MtkTvOpenVCHIPInfoBase getOpenVchip() {
		if (para == null) {
			para = new MtkTvOpenVCHIPPara();
		}
		return mTvRatingSettingInfo.getOpenVCHIPInfo(para);
	}

	public MtkTvOpenVCHIPPara getOpenVCHIPPara() {
		if (para == null) {
			para = new MtkTvOpenVCHIPPara();
		}
		return para;
	}

	public MtkTvOpenVCHIPSettingInfoBase getOpenVchipSetting() {
		if (mOpenVCHIPSettingInfoBase == null) {
			mOpenVCHIPSettingInfoBase = mTvRatingSettingInfo
					.getOpenVCHIPSettingInfo();
			;
		}
		return mOpenVCHIPSettingInfoBase;
	}

	public void setOpenVChipSetting(int regionIndex, int dimIndex, int levIndex) {
		MtkTvOpenVCHIPSettingInfoBase info = getOpenVchipSetting();
		byte[] block = info.getLvlBlockData();
		int iniValue = block[levIndex];
		for (int i = 0; i < block.length; i++) {
			if (iniValue == 0) {
				if (i > levIndex) {
					block[i] = 1;
				}
				block[levIndex] = 1;
			} else if (iniValue == 1) {
				if (i < levIndex) {
					block[i] = 0;
				}
				block[levIndex] = 0;
			}
			MtkLog.d(TAG, "block[i]:" + block[i] + "---i:" + i);
		}
		info.setRegionIndex(regionIndex);
		info.setDimIndex(dimIndex);
		info.setLvlBlockData(block);
		mTvRatingSettingInfo.setOpenVCHIPSettingInfo(info);
	}

	/*
	 * Region
	 */
	public boolean isUSRegion() {
		return region == MarketRegionInfo.REGION_US;
	}

	public boolean isSARegion() {
		return region == MarketRegionInfo.REGION_SA;
	}

	public boolean isEURegion() {
		return region == MarketRegionInfo.REGION_EU;
	}

	public boolean isCNRegion() {
		return region == MarketRegionInfo.REGION_CN;
	}

	public int getRegion() {
		return region;
	}

	public void removeCallBackListener(Handler listerner) {
		mCallbackHandler.removeCallBackListener(
				TvCallbackConst.MSG_CB_SCAN_NOTIFY, listerner);
	}

	/*
	 * notify
	 */
	public boolean addSingleLevelCallBackListener(Handler listerner) {
		return mCallbackHandler.addCallBackListener(
				TvCallbackConst.MSG_CB_SVCTX_NOTIFY, listerner);
	}

	public void removeSingleLevelCallBackListener(Handler listerner) {
		mCallbackHandler.removeCallBackListener(
				TvCallbackConst.MSG_CB_SVCTX_NOTIFY, listerner);
	}

	/*
	 * notify
	 */
	public boolean addConfigCallBackListener(Handler listerner) {
		return mCallbackHandler.addCallBackListener(
				TvCallbackConst.MSG_CB_CONFIG, listerner);
	}















	/*
	 * ACFG Function
	 */
	public int getMinValue(String cfgId) {
		if (dumy) {
			return 0;
		} else {
			int value = mTvConfig.getMinMaxConfigValue(cfgId);
			return MtkTvConfig.getMinValue(value);
		}
	}

	public int getMaxValue(String cfgId) {
		if (dumy) {
			return 100;
		} else {
			int value = mTvConfig.getMinMaxConfigValue(cfgId);
			MtkLog.d("TVContent", "value:" + value);
			return MtkTvConfig.getMaxValue(value);
		}
	}

	public int getConfigValue(String cfgId) {
		MtkLog.d("TVContent",
				"getConfigValue(cfgId):" + mTvConfig.getConfigValue(cfgId)
						+ "cfgId:" + cfgId);
		return mTvConfig.getConfigValue(cfgId);
	}

	public String getConfigString(String cfgId) {
		return mTvConfig.getConfigString(cfgId);
	}

	public void setConfigValue(String cfgId, int value) {
		MtkLog.d("TVContent", "setConfigValue cfgId:" + cfgId + "----value:"
				+ value);
		if (dumy) {
			dumyData.put(cfgId, value);
		} else {
			if (cfgId.equalsIgnoreCase(MtkTvConfigType.CFG_VIDEO_VID_MJC_DEMO)) {
				mTvConfig.setConfigValue(cfgId, value, 1);
			} else {
				mTvConfig.setConfigValue(cfgId, value);
			}
		}
	}

	public void setConfigValue(String cfgId, int value, boolean isUpate) {
		MtkLog.d("TVContent", "setConfigValue cfgId:" + cfgId + "----value:"
				+ value);
		if (dumy) {
			dumyData.put(cfgId, value);
		} else {
			int update = 0;
			if (isUpate) {
				update = 1;
			}
			mTvConfig.setConfigValue(cfgId, value, update);
		}
	}

	public boolean isConfigEnabled(String cfgId) {
		return mTvConfig.isConfigEnabled(cfgId) == MtkTvConfigType.CFGR_ENABLE ? true
				: false;
	}

	public String getCurrentInputSourceName() {
		// String sourcename = "tv";fix CR DTV00580677 & DTV00581180 &
		// DTV00581188
		String sourcename = "";
		return sourcename;
	}

	public boolean isCurrentSourceTv() {
		String sourceName = "";
		return sourceName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_TV);
	}

	public boolean isCurrentSourceVGA() {
		String sourceName = "";
		return sourceName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_VGA);
	}

	public boolean isCurrentSourceHDMI() {
		String sourceName = "";
		if (TextUtils.isEmpty(sourceName)) {
			return false;
		}
		if (sourceName.contains("HDMI")) {
			sourceName = "hdmi";
		}

		return sourceName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_HDMI);
	}

	// fix CR DTV00580462
	public boolean iCurrentInputSourceHasSignal() {
		return isSignalLoss() ? false : true;
	}

	public boolean isCurrentSourceComponent() {
		String sourceName = "";
		return sourceName
				.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_COMPONENT);
	}

	public boolean isCurrentSourceComposite() {
		String sourceName = "";
		return sourceName
				.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_COMPOSITE);
	}


	public int getCurrentTunerMode() {
		return getConfigValue(MtkTvConfigType.CFG_BS_BS_SRC);
	}


	public boolean isHaveScreenMode() {
		boolean flag = isConfigVisible(MenuConfigManager.SCREEN_MODE);
		MtkLog.d("TVContent", "isHaveScreenMode flag:" + flag);
		return flag;
	}

	/**
	 * check is config visible
	 * @return
	 */
	public boolean isConfigVisible(String cfgid){
		boolean flag = mTvConfig.isConfigVisible(cfgid) == MtkTvConfigType.CFGR_VISIBLE ? true
				: false;
		return flag;
	}

	public boolean isFilmModeEnabled() {
		if (isConfigEnabled(MenuConfigManager.GAME_MODE)) {
			return false;
		}
		return false;
	}

	/**
	 * Get current signal level
	 *
	 * @return true(no signal)/false(with signal)
	 */
	public boolean isSignalLoss() {

		boolean hasSignal = false;
		hasSignal = MtkTvBroadcast.getInstance().isSignalLoss();

		MtkLog.d("TVContent", "isSignalLoss()?," + hasSignal);
		return hasSignal;
	}

	/**
	 * Get current signal level
	 *
	 * @return 0-100
	 */
	public int getSignalLevel() {

		MtkLog.d("TVContent", "Enter getSignalLevel\n");

		return MtkTvBroadcast.getInstance().getSignalLevel();
	}

	/**
	 * Get current signal Quality
	 *
	 * @return 0-100
	 */
	public int getSignalQuality() {

		MtkLog.d("TVContent", "Enter getSignalQuality\n");
		return MtkTvBroadcast.getInstance().getSignalQuality();
	}

	public void updatePowerOn(String cfgID, int enable, String date) {
		int daySec = onTimeModified(date);
		int timerValue = ((((((enable)) & 0x01) << 31) & 0x80000000) | ((daySec) & 0x0001ffff));
		MtkLog.d("TVContent", "timerValue:" + timerValue + "cfgID:" + cfgID);
		mTvConfig.setConfigValue(cfgID, timerValue);
	}

	public void updatePowerOff(String cfgID, int enable, String date) {
		int daySec = onTimeModified(date);
		int timerValue = ((((((enable)) & 0x01) << 31) & 0x80000000) | ((daySec) & 0x0001ffff));
		MtkLog.d("TVContent", "timerValue:" + timerValue + "cfgID:" + cfgID);
		mTvConfig.setConfigValue(cfgID, timerValue);
	}

	public int onTimeModified(String time) {
		int hour = Integer.parseInt(time.substring(0, 2));
		int minute = Integer.parseInt(time.substring(3, 5));
		int second = Integer.parseInt(time.substring(6));
		return hour * 3600 + minute * 60 + second;
	}

	public void setTimeInterval(int value) {
		mTvConfig.setConfigValue(MenuConfigManager.PARENTAL_CFG_RATING_BL_TYPE,
				value);
	}

	public void setTimeIntervalTime(String cfgID, String date) {
		int daySec = onTimeModified(date);
		mTvConfig.setConfigValue(cfgID, daySec * 1000);
	}

	/*
	 * true is right,false is left
	 */
	public int setSleepTimer(boolean direction) {
		MtkLog.d("TVContent", "direction:" + direction);
		int valueIndex = 0;
		int leftmill = getSleepTimerRemaining();
		int mill = MtkTvTime.getInstance().getSleepTimer(direction);
		if (leftmill > 0) {
			int minute = leftmill / 60;
			if (minute > 0 && minute < 1) {
				valueIndex = direction ? 1 : 8;
			} else if (minute >= 1 && minute < 9) {
				valueIndex = direction ? 1 : 0;
			} else if (minute >= 9 && minute < 11) {
				valueIndex = direction ? 2 : 0;
			} else if (minute >= 11 && minute < 19) {
				valueIndex = direction ? 2 : 1;
			} else if (minute >= 19 && minute < 21) {
				valueIndex = direction ? 3 : 1;
			} else if (minute >= 21 && minute < 29) {
				valueIndex = direction ? 3 : 2;
			} else if (minute >= 29 && minute < 31) {
				valueIndex = direction ? 4 : 2;
			} else if (minute >= 31 && minute < 39) {
				valueIndex = direction ? 4 : 3;
			} else if (minute >= 39 && minute < 41) {
				valueIndex = direction ? 5 : 3;
			} else if (minute >= 41 && minute < 49) {
				valueIndex = direction ? 5 : 4;
			} else if (minute >= 49 && minute < 51) {
				valueIndex = direction ? 6 : 4;
			} else if (minute >= 51 && minute < 59) {
				valueIndex = direction ? 6 : 5;
			} else if (minute >= 59 && minute < 61) {
				valueIndex = direction ? 7 : 5;
			} else if (minute >= 61 && minute < 89) {
				valueIndex = direction ? 7 : 6;
			} else if (minute >= 89 && minute < 91) {
				valueIndex = direction ? 8 : 6;
			} else if (minute >= 91 && minute < 119) {
				valueIndex = direction ? 8 : 7;
			} else if (minute >= 119 && minute <= 120) {
				valueIndex = direction ? 0 : 7;
			}
			MtkLog.d("TVContent", "minute:" + minute + "valueIndex:"
					+ valueIndex);
		} else {
			switch (mill / 60) {
			case 10:
				valueIndex = 1;
				break;
			case 20:
				valueIndex = 2;
				break;
			case 30:
				valueIndex = 3;
				break;
			case 40:
				valueIndex = 4;
				break;
			case 50:
				valueIndex = 5;
				break;
			case 60:
				valueIndex = 6;
				break;
			case 90:
				valueIndex = 7;
				break;
			case 120:
				valueIndex = 8;
				break;
			default:
				valueIndex = 0;
				break;
			}
		}
		return valueIndex;
	}

	/*
	 * true is right,false is left
	 */
	public int getSleepTimerRemaining() {
		return MtkTvTime.getInstance().getSleepTimerRemainingTime();
	}

	public void resetConfigValues() {
		mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_FACTORY_RESET_ALL);
		// mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_AUDIO_ITEMS);
		// mTvConfig.resetConfigValues(MtkTvConfigType.CFGU_SCREEN_ITEMS);
	}

	public void resetPub() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				MtkTvUtil.getInstance().resetPub();
			}
		}).start();
//		TimeShiftManager.forceSetToDefault(); temp for biaoqinggao
	}

	public void resetPri() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				MtkTvUtil.getInstance().resetPri();
			}
		}).start();
//		TimeShiftManager.forceSetToDefault(); temp for biaoqinggao
	}

	public void resetFac() {
		//cr00615358 as same as 00614154 which has fixed by sin_xinsheng
		//so needn't fix it again(sin_biaoqinggao)
		new Thread(new Runnable() {

			@Override
			public void run() {
				MtkTvUtil.getInstance().resetFac();
			}
		}).start();
//		TimeShiftManager.forceSetToDefault(); temp for biaoqinggao
	}





/*
	public boolean isTshitRunning() {
		// Fix CR DTV00583447
		boolean running = TimeShiftManager.getInstance() == null?false:TimeShiftManager.getInstance().tshiftIsRunning();
		return running;
	}
*/
	public int getDefaultNetWorkID() {
		// TODO Auto-generated method stub
		return 104000;
	}

	public int updateCIKey() {
		return mCIBase.updateCIKey();
	}

	public int eraseCIKey() {
		return mCIBase.eraseCIKey();
	}

	public String getCIKeyinfo() {
		return mCIBase.getCIKeyinfo();
	}

	public boolean isShowCountryRegion() {
		String country = mTvConfig.getCountry();
		MtkLog.v(TAG, "isShowCountryRegion country*****************" + country);
		boolean isShow = false;
		if (country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS)
				|| country
						.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ESP)
				|| country.equalsIgnoreCase(MtkTvConfigType.S639_CFG_LANG_POR)) {
			if (country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_ESP)) {
				if (getConfigValue(MtkTvConfigType.CFG_TIME_TZ_SYNC_WITH_TS) == 1) {
					isShow = true;
				}
			} else {
				isShow = true;
			}
		}
		MtkLog.v(TAG, "isShowCountryRegion*****************" + isShow);
		return isShow;
	}

	public boolean isAusCountry() {
		String country = mTvConfig.getCountry();
		if (isEURegion()
				&& country.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_AUS)) {
			return true;
		}
		return false;
	}

	public boolean isNorCountry() {
		String country = mTvConfig.getCountry();
		if (isEURegion()
				&& country
						.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_NOR)) {
			return true;
		}
		return false;
	}

	// is france
	public boolean isFraCountry() {
		String country = mTvConfig.getCountry();
		if (isEURegion()
				&& country
						.equalsIgnoreCase(MtkTvConfigType.S3166_CFG_COUNT_FRA)) {
			return true;
		}
		return false;
	}

	public int getWowEnable() {
		return 0;
	}
	/*
	public void setWowEnable(int index) {
		MtkLog.v(TAG, "setWowEnable*****************" + index);
		saveV.saveValue(MenuConfigManager.SETUP_WOW, index);
		if (index == 0) {
			MtkNetworkManager.getInstance().setEnableWoWL(false);
		} else {
			MtkNetworkManager.getInstance().setEnableWoWL(true);
		}
	}

	public void setWolEnable(int index) {
		MtkLog.v(TAG, "setWolEnable*****************" + index);

		if (index == 0) {
			MtkNetworkManager.getInstance().setEnableWol(false);
		} else {
			MtkNetworkManager.getInstance().setEnableWol(true);
		}
	}

	public int getWolEnable() {
	    boolean enable = MtkNetworkManager.getInstance().isEnableWol();
	    MtkLog.v(TAG, "getWowEnable*****************"+enable);
	    return enable ? 1 : 0;
	}

	*/

	public static int snapshotID = -1;
	public static int dvbsLastOP = -1;
	public static int mDvbsSatSnapShotId = -1;

//	public static void createChanneListSnapshot() {
//		releaseChanneListSnapshot();
//		if (CommonIntegration.getInstance().getAllChannelLength() > 0) {
//			snapshotID = MtkTvChannelListBase.createSnapshot(CommonIntegration.getInstance().getSvl());
//		}
//		Util.showDLog("createChanneListSnapshot(),snapshotID:" + snapshotID);
//	}

//	public static void restoreChanneListSnapshot() {
//		Util.showDLog("restoreChanneListSnapshot(),snapshotID:" + snapshotID);
//		Util.printStackTrace();
//		if (snapshotID != -1) {
//			int result = -1;
//			result = MtkTvChannelListBase.restoreSnapshot(snapshotID);
//			Util.showDLog("restoreChanneListSnapshot(),result:" + result);
//		}
//	}

//	public static void releaseChanneListSnapshot() {
//		Util.showDLog("releaseChanneListSnapshot(),snapshotID:" + snapshotID);
//		if (snapshotID != -1) {
//			int result = -1;
//			result = MtkTvChannelListBase.freeSnapshot(snapshotID);
//			Util.showDLog("releaseChanneListSnapshot(),result:" + result);
//			snapshotID=-1;
//		}
//	}



	public static void freeBackupDVBSOP() {
		Util.showDLog("freeBackupDVBSOP(),dvbsLastOP:" + dvbsLastOP);
		dvbsLastOP = -1;
	}

	public static void backUpDVBSsatellites() {
		Util.showDLog("backUpDVBSsatellites()=");
		freeBachUpDVBSsatellites();
		int svlID = CommonIntegration.getInstance().getSvl();
		mDvbsSatSnapShotId = MtkTvDvbsConfigBase.createSatlSnapshot(svlID);
		Util.showDLog("backUpDVBSsatellites()=" + mDvbsSatSnapShotId);
	}

	public static void restoreDVBSsatellites() {
		Util.showDLog("restoreDVBSsatellites()=" + mDvbsSatSnapShotId);
		if (mDvbsSatSnapShotId != -1) {
			int result = MtkTvDvbsConfigBase.restoreSatlSnapshot(mDvbsSatSnapShotId);
			Util.showDLog("restoreDVBSsatellites(),result:" + result);
		}
	}

	public static void freeBachUpDVBSsatellites() {
		Util.showDLog("freeBachUpDVBSsatellites()=" + mDvbsSatSnapShotId);
		if (mDvbsSatSnapShotId != -1) {
			MtkTvDvbsConfigBase.freeSatlSnapshot(mDvbsSatSnapShotId);
			mDvbsSatSnapShotId = -1;
		}
	}


//	public boolean changeChannelByFreq(int frequency) {
//		Util.showDLog("changeChannelByFreq(),frequency:" + frequency);
//		int length = CommonIntegration.getInstance().getChannelLength();
//		List<MtkTvChannelInfoBase> channelBaseList = CommonIntegration
//				.getInstance().getChannelList(0, 0, length, MtkTvChCommonBase.SB_VNET_ALL);
//
//		for (int i = 0; i < channelBaseList.size(); i++) {
//			if (channelBaseList.get(i).getFrequency() <= frequency+2500000 && channelBaseList.get(i).getFrequency() >= frequency-250000) {
//				CommonIntegration.getInstance().selectChannelById(channelBaseList.get(i).getChannelId());
//				Util.showDLog("changeChannelByFreq()," + channelBaseList.get(i).getFrequency());
//				return true;
//			} else {
//				Util.showDLog("changeChannelByFreq(),channelBaseList.freq: " + channelBaseList.get(i).getFrequency());
//			}
//		}
//		return false;
//	}



    public boolean isSourceType3D() {
        MtkTvAppTVBase apptv = new MtkTvAppTVBase();
        int type = apptv.GetVideoSrcTag3DType(CommonIntegration.getInstance().getCurrentFocus());
        MtkLog.d(TAG, "isSourceType3D type:"+type);
        switch (type) {
            case VSH_SRC_TAG3D_TTDO:
            case VSH_SRC_TAG3D_NOT_SUPPORT:

                return false;
            case VSH_SRC_TAG3D_2D:
            case VSH_SRC_TAG3D_MVC:
            case VSH_SRC_TAG3D_FP:
            case VSH_SRC_TAG3D_FS:
            case VSH_SRC_TAG3D_TB:
            case VSH_SRC_TAG3D_SBS:
            case VSH_SRC_TAG3D_REALD:
            case VSH_SRC_TAG3D_SENSIO:
            case VSH_SRC_TAG3D_LA:
                return true;
            default:
                return false;
        }
    }


    //check last input source name is vga
    public boolean isLastInputSourceVGA(){
    	if(lastInputSourceName.equalsIgnoreCase(MtkTvInputSource.INPUT_TYPE_VGA)){
    		lastInputSourceName = "";//reset to aviod
    		return true;
    	}
    	return false;
    }

    public boolean GetDivXPlusSupport(){
       return mMtkTvMultiMediaBase.GetDivXPlusSupport();
    }

    public String getDrmRegistrationCode() {
	     return	mMtkTvMultiMediaBase.GetDrmRegistrationCode();
	}
	public String setDrmDeactivation(){
		 return	mMtkTvMultiMediaBase.SetDrmDeactivation();
	}
	public long getDrmUiHelpInfo(){
		 return	mMtkTvMultiMediaBase.GetDrmUiHelpInfo();
	}

	/**
	 * clean SA region booked events
	 */
	/*
	public void cleanLocalData() {
		if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_SA) {
			new Thread(new Runnable(){
				@Override
				public void run() {
					DBMgrProgramList.getInstance(mContext).getWriteableDB();
					DBMgrProgramList.getInstance(mContext).deleteAllPrograms();
					DBMgrProgramList.getInstance(mContext).closeDB();
					DataReader.getInstance(mContext).cleanMStypeDB();
				}
			}).start();
	    } else if (MarketRegionInfo.getCurrentMarketRegion() == MarketRegionInfo.REGION_EU) {
	    	new Thread(new Runnable(){
				@Override
				public void run() {
					DataReader.getInstance(mContext).cleanMStypeDB();
				}
			}).start();
	    }
	}
	*/


	/**
	 * save biss key info to bsl table
	 * @param item
	 */
	/*
	public void saveBisskeyInfo(Action item){
		if(item != null && item.getmParentGroup() != null){
			int[] header = new int[]{-1,-1,-1,-1};
			List<Action> list = item.getmParentGroup();
			for(int i=0;i<list.size();i++){
				if(!list.get(i).getmItemID().equals(MenuConfigManager.BISS_KEY_CW_KEY)){
					header[i] = list.get(i).mInitValue;
				}else{
					String[] opts = list.get(i).getmOptionValue();
					String cwkey = opts[3];
				}
			}
		}
	}
	*/
	/**
	 * check bissKey is exist
	 * @param item
	 * @return
	 */
	public boolean checkBissKeyExist(Action item){
		return false ;
	}

}
