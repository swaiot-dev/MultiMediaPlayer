
package com.mediatek.wwtv.mediaplayer.setting.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.mediatek.twoworlds.tv.ITVRemoteService;
import com.mediatek.twoworlds.tv.MtkTvAppTV;
import com.mediatek.twoworlds.tv.MtkTvBroadcast;
import com.mediatek.twoworlds.tv.MtkTvCDTChLogoBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvHighLevel;
import com.mediatek.twoworlds.tv.MtkTvInputSource;
import com.mediatek.twoworlds.tv.MtkTvScanDvbsBase;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.MtkTvDvbsConfigBase;
import com.mediatek.twoworlds.tv.TVCommonUtil;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvChCommonBase;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

import com.mediatek.twoworlds.tv.model.MtkTvFavoritelistInfoBase;
import com.mediatek.twoworlds.tv.model.MtkTvISDBChannelInfo;
import com.mediatek.twoworlds.tv.model.MtkTvDvbsConfigInfoBase;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.util.MtkLog;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

public class CommonIntegration {

    private static final String TAG = "CommonIntegration";

    private static CommonIntegration instanceNavIntegration = null;

    private static MtkTvBroadcast chBroadCast = null;
    private static MtkTvUtil instanceMtkTvUtil = null;
    private static MtkTvHighLevel instanceMtkTvHighLevel;
    private MtkTvCDTChLogoBase mMtkTvCDTChLogoBase;
    private MtkTvAppTV mMtkTvAppTV;
    private MtkTvDvbsConfigBase mMtkTvDvbsConfigBase;

    public static final String TV_FOCUS_WIN_MAIN = "main";
    public static final String TV_FOCUS_WIN_SUB = "sub";

    public static final int TV_NORMAL_MODE = 0;
    public static final int TV_PIP_MODE = 1;
    public static final int TV_POP_MODE = 2;

    public static final int SUPPORT_THIRD_PIP_MODE = 1;

    public static final int NOT_SUPPORT_THIRD_PIP_MODE = 0;

    public static final String ZOOM_CHANGE_BEFORE = MtkTvAppTV.SYS_BEFORE_ZOOM_MODE_CHG;
    public static final String ZOOM_CHANGE_AFTER = MtkTvAppTV.SYS_AFTER_ZOOM_MODE_CHG;
    public static final String SCREEN_MODE_CHANGE_BEFORE = MtkTvAppTV.SYS_BEFORE_ASPECT_RATIO_CHG;
    public static final String SCREEN_MODE_CHANGE_AFTER = MtkTvAppTV.SYS_AFTER_ASPECT_RATIO_CHG;


    public static final int CH_UP_DOWN_MASK =  MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE | MtkTvChCommonBase.SB_VNET_FAKE;
    public static final int CH_UP_DOWN_VAL =  MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE ;
    public static final int CH_LIST_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;
    public static final int CH_LIST_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE ;


    public static final int CH_LIST_RADIO_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
    public static final int CH_LIST_RADIO_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE;
    public static final int CH_LIST_ANALOG_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
    public static final int CH_LIST_ANALOG_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
    public static final int CH_LIST_DIGITAL_MASK = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE | MtkTvChCommonBase.SB_VNET_RADIO_SERVICE | MtkTvChCommonBase.SB_VNET_ANALOG_SERVICE;
    public static final int CH_LIST_DIGITAL_VAL = MtkTvChCommonBase.SB_VNET_ACTIVE ;

    public static final int SVCTX_NTFY_CODE_VIDEO_ONLY_SVC = 20; //video only
    public static final int SVCTX_NTFY_CODE_VIDEO_FMT_UPDATE = 37;// video update


    private static final int PAGE_COUNT = 7;

    private static final String LAST_CHANNEL_ID = MtkTvConfigType.CFG_NAV_AIR_LAST_CH;
    private static final String CUR_CHANNEL_ID = MtkTvConfigType.CFG_NAV_AIR_CRNT_CH;
    private static final String TV_FOCUS_WIN = MtkTvConfigType.CFG_PIP_POP_TV_FOCUS_WIN;

    private static final String TV_MODE = MtkTvConfigType.CFG_PIP_POP_TV_MODE;

    private static final String THIRD_PIP_MODE = MtkTvConfigType.CFG_PIP_POP_ANDROID_POP_MODE;
    /**svl id value, 1:T, 2:C, 3:General S, 4: Prefer S, 5:T(CI), 6:C(CI), 7:S(CI)*/
    public static final int DB_AIR_SVLID = 1;
    public static final int DB_CAB_SVLID = 2;
    public static final int DB_SAT_SVLID = 3;
    public static final int DB_SAT_PRF_SVLID = 4;
    public static final int DB_CI_PLUS_SVLID_AIR = 5;
    public static final int DB_CI_PLUS_SVLID_CAB = 6;
    public static final int DB_CI_PLUS_SVLID_SAT = 7;
    /**tuner mode value, 0:T, 1:C, 2:S, 3:used by set general S in wizard and menu*/
    public static final int DB_AIR_OPTID = 0;
    public static final int DB_CAB_OPTID = 1;
    public static final int DB_SAT_OPTID = 2;
    public static final int DB_GENERAL_SAT_OPTID = 3;//maybe used by set tuner mode, but should not be used by get tuner mode

    private boolean iCurrentInputSourceHasSignal;
    private boolean iCurrentTVHasSignal;
    private boolean isVideoScrambled;
    private boolean showFAVListFullToastDealy = false;

    private boolean doPIPPOPAction = false;

    private static int mCurrentTvMode = -1;




    public boolean isShowFAVListFullToastDealy() {
		return showFAVListFullToastDealy;
	}

	public void setShowFAVListFullToastDealy(boolean showFAVListFullToastDealy) {
		this.showFAVListFullToastDealy = showFAVListFullToastDealy;
	}

	public boolean isDoPIPPOPAction() {
		return doPIPPOPAction;
	}

	public void setDoPIPPOPAction(boolean doPIPPOPAction) {
		this.doPIPPOPAction = doPIPPOPAction;
	}


	private static Context mContext;
	/*
	 * Destroy initilize context
	 */
	public void setContext(Context context){
		mContext = context;
	}

	public boolean isContextInit() {
		return mContext != null;
	}

    private CommonIntegration() {
        chBroadCast = MtkTvBroadcast.getInstance();
        instanceMtkTvUtil = MtkTvUtil.getInstance();
        instanceMtkTvHighLevel = new MtkTvHighLevel();

        mMtkTvCDTChLogoBase = new MtkTvCDTChLogoBase();
	mMtkTvAppTV = MtkTvAppTV.getInstance();
	mMtkTvDvbsConfigBase = new MtkTvDvbsConfigBase();
    }

    public static CommonIntegration getInstanceWithContext(Context context) {
        if (null == instanceNavIntegration) {
            instanceNavIntegration = new CommonIntegration();
        }
        if (mContext == null) {
        	mContext = context;
        }
        return instanceNavIntegration;
    }

    public static CommonIntegration getInstance() {
        if (null == instanceNavIntegration) {
            instanceNavIntegration = new CommonIntegration();
        }

        return instanceNavIntegration;
    }

    /**
     * this method is used to remove the object
     */
    protected static void remove() {
        instanceNavIntegration = null;

        chBroadCast = null;
        instanceMtkTvUtil = null;
        instanceMtkTvHighLevel = null;
    }

    /**
     * get MtkTvBroadcast instance
     *
     * @return
     */
    public static MtkTvBroadcast getInstanceMtkTvBroadcast() {
        if (null == chBroadCast) {
            chBroadCast = MtkTvBroadcast.getInstance();
        }
        return chBroadCast;
    }

    public static MtkTvUtil getInstanceMtkTvUtil() {
        if (null == instanceMtkTvUtil) {
            instanceMtkTvUtil = MtkTvUtil.getInstance();
        }
        return instanceMtkTvUtil;
    }

    public static MtkTvHighLevel getInstanceMtkTvHighLevel() {
        if (null == instanceMtkTvHighLevel) {
            instanceMtkTvHighLevel = new MtkTvHighLevel();
        }
        return instanceMtkTvHighLevel;
    }


    /**
     * current TV mode is PIP or POP mode or not
     * @return
     */
    public boolean isPipOrPopState(){
        if(mCurrentTvMode != TV_NORMAL_MODE){
            mCurrentTvMode = getInstanceMtkTvHighLevel().getCurrentTvMode();
        }
        else{
            return false;
        }

        if (TV_PIP_MODE == mCurrentTvMode || TV_POP_MODE == mCurrentTvMode) {
            return true;
        }

        return false;
    }

    /**
     * current TV mode is PIP state or not
     * @return
     */
    public boolean isPIPState() {
        if(mCurrentTvMode != TV_NORMAL_MODE){
            mCurrentTvMode = instanceMtkTvHighLevel.getCurrentTvMode();
        }
        else{
            return false;
        }

        if (TV_PIP_MODE == mCurrentTvMode) {
            return true;
        }
        return false;
    }

    /**
     * current TV mode is POP state or not
     * @return
     */
    public boolean isPOPState() {
        if(mCurrentTvMode != TV_NORMAL_MODE){
            mCurrentTvMode = instanceMtkTvHighLevel.getCurrentTvMode();
        }
        else{
            return false;
        }

        if (TV_POP_MODE == mCurrentTvMode) {
            return true;
        }
        return false;
    }

    /**
     * current TV mode is normal state
     * @return
     */
    public boolean isTVNormalState() {
//        if(TimeShiftManager.getInstance().pvrIsRunning()){
//            return false;
//        }

        if(mCurrentTvMode != TV_NORMAL_MODE){
            mCurrentTvMode = instanceMtkTvHighLevel.getCurrentTvMode();
        }

        if (TV_NORMAL_MODE == mCurrentTvMode) {
            return true;
        }

        return false;
    }

    /**
     * if current source is dtv or not
     *
     * @return
     */






    /**
     * select channel with channe1 number This API is used to select a channel
     * by the whole channel number.
     *
     * @param [in] majorNo The major part of the channel number.
     * @param [in] minorNo The minor part of the channel number. {-1 for no
     *        minorNo}
     *
     *        <pre>
     *             Generally, the range of major and minor channel number depends on the region, county and broadcast type.
     *
     *             EU => Doesn't support minor channel No.
     *             NAFTA=>Air:      1 <= majorNo <= 99
     *                                     1 <= minorNo <= 65535
     *                          Cable:  0 <= majorNo <= 999
     *                                     0 <= minorNo <= 65535
     *             LATIN =>Doesn't support minor channel No.
     *
     * </pre>
     * @return This API will return 0 (OK), others (Fail).
     */

    /**
     * select channel with channe1 number
     *
     * @param num
     * @return true (OK) false (Error)
     */



	/**
	 * same with getSvl() function
	 * svlID 1:air, 2:cable, 3:general sat, 4:prefer sat, 5:CAM-air, 6:CAM-cable, 7:CAM-sat
	 * @return
	 */
    public int getSvlFromACFG() {
    	int svlId = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.SVL_ID);
    	return svlId;
    }

	/**
	 * same with getSvlFromACFG() function
	 * svlID 1:air, 2:cable, 3:general sat, 4:prefer sat, 5:CAM-air, 6:CAM-cable, 7:CAM-sat
	 * @return
	 */
    public int getSvl() {
        int svl = -1;
        int tunerMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
        boolean flag = MtkTvConfig.getInstance().isConfigVisible(MenuConfigManager.CHANNEL_LIST_TYPE) == MtkTvConfigType.CFGR_VISIBLE ? true : false;
        boolean hasCAM = false;
        if (flag) {
        	int value = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.CHANNEL_LIST_TYPE);
        	if (value > 0) {
        		hasCAM = true;
        	}
        	MtkLog.d(TAG, "getSvl>>>>" + value);
        }
        switch (tunerMode) {
            case DB_AIR_OPTID://T
            	if (hasCAM) {
            		svl = DB_CI_PLUS_SVLID_AIR;
            	} else {
            		svl = DB_AIR_SVLID;
            	}
                break;
            case DB_CAB_OPTID://C
            	if (hasCAM) {
            		svl = DB_CI_PLUS_SVLID_CAB;
            	} else {
            		svl = DB_CAB_SVLID;
            	}
                break;
            case DB_SAT_OPTID://S
            case DB_GENERAL_SAT_OPTID://maybe has no this value when get tuner mode
            	if (hasCAM) {
            		svl = DB_CI_PLUS_SVLID_SAT;
            	} else {
            		int prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
            		MtkLog.d(TAG, "getSvl tunerMode,prefer =" +prefer);
            		if (prefer!=0) {
            			svl = DB_SAT_PRF_SVLID;
            		} else {
            			svl = DB_SAT_SVLID;
            		}
            	}
                break;
            default://default is T
            	if (hasCAM) {
            		svl = DB_CI_PLUS_SVLID_AIR;
            	} else {
            		svl = DB_AIR_SVLID;
            	}
                break;
        }
        MtkLog.d(TAG, "getSvl tunerMode =" + tunerMode + " svl =" + svl);
        return svl;
    }

    /**
     * tuner mode value, 0:T, 1:C, 2:S
     * */
    public int getTunerMode() {
    	int tunerMode = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE);
    	return tunerMode;
    }

    public boolean isPreferSatMode() {
    	int prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
    	return prefer != 0 && getTunerMode() >= DB_SAT_OPTID;
    }

    public boolean isGeneralSatMode() {
    	int prefer = MtkTvConfig.getInstance().getConfigValue(MenuConfigManager.TUNER_MODE_PREFER_SAT);
    	return prefer == 0 && getTunerMode() >= DB_SAT_OPTID;
	}

    public int getChUpDownFilter() {
        int filter = 0;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
            case MarketRegionInfo.REGION_US:
            case MarketRegionInfo.REGION_SA:
                filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_VISIBLE;// MtkTvChCommonBase.SB_VNET_ACTIVE
                                                                                              // |
                                                                                              // MtkTvChCommonBase.SB_VNET_FAKE;
                break;
        }

        MtkLog.d(TAG, "getChUpDownFilter filter =" + filter);
        return filter;
    }

    public int getChListFilter() {
        int filter = 0;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
            case MarketRegionInfo.REGION_US:
            case MarketRegionInfo.REGION_SA:
                filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;// |MtkTvChCommonBase.SB_VNET_VISIBLE;
                break;
        }

        MtkLog.d(TAG, "getChListFilter filter =" + filter);
        return filter;
    }

	public int getChUpDownFilterEPG() {
        int filter = 0;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
            case MarketRegionInfo.REGION_SA:
            	filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;// |MtkTvChCommonBase.SB_VNET_VISIBLE;
            	break;
            case MarketRegionInfo.REGION_US:
                filter = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_VISIBLE;// MtkTvChCommonBase.SB_VNET_ACTIVE
                                                                                              // |
                                                                                              // MtkTvChCommonBase.SB_VNET_FAKE;
                break;
        }

        MtkLog.d(TAG, "getChUpDownFilter filter =" + filter);
        return filter;
    }

	public int getChListFilterEPG() {
        int filter = 0;
        switch (MarketRegionInfo.getCurrentMarketRegion()) {
            case MarketRegionInfo.REGION_CN:
            case MarketRegionInfo.REGION_EU:
            case MarketRegionInfo.REGION_SA:
            	filter = MtkTvChCommonBase.SB_VNET_ACTIVE | MtkTvChCommonBase.SB_VNET_FAKE;// |MtkTvChCommonBase.SB_VNET_VISIBLE;
            	break;
            case MarketRegionInfo.REGION_US:
                filter = MtkTvChCommonBase.SB_VNET_EPG | MtkTvChCommonBase.SB_VNET_FAKE;// |MtkTvChCommonBase.SB_VNET_VISIBLE;
                break;
        }
		MtkLog.d(TAG,"getChListFilter filter ="+filter);
		return filter;
	}

	/**
	 * selected channel up by satellite
	 * true(OK),false(error)
	 */

	/**
	 * selected channel up
	 * true(OK),false(error)
	 */


    /**
     * selected channel up
     * true(OK),false(error)
     */



    /**
     * selected channel down true(OK),false(error)

    /**
     * This API allows the user to set some specified channels to the database
     * BY MASS.
     *
     * @param [in] channelOperator CHLST_OPERATOR_ADD, CHLST_OPERATOR_MOD,
     *        CHLST_OPERATOR_DEL
     * @param [in] channelsToSet A list of Channels which the user wants to set
     *        (CHLST_OPERATOR_ADD/CHLST_OPERATOR_MOD/CHLST_OPERATOR_DEL) to the
     *        database.
     *
     *        <pre>
     *            There is at least one channel in 'channelsToSet' and these channels must be in the same database.
     *                   E.g. If 'channelOperator' == CHLST_OPERATOR_ADD, then the channel(s) in  'channelsToSet' will be appended to the database.
     *                         If 'channelOperator' == CHLST_OPERATOR_MOD, then the channel(s) in  'channelsToSet' will be updated to the database.
     *                         If 'channelOperator' == CHLST_OPERATOR_DEL, then the channel(s) in  'channelsToSet' will be deleted from the database.
     * </pre>
     * @return CHLST_RET_OK, sucessful ; CHLST_RET_FAIL, failed.
     * @see com.mediatek.twoworlds.tv.model.MtkTvChannelInfo.
     */


    /*
     * chlist filter default :MtkTvChannelList.CHLST_FLT_ACTIVE |
     * MtkTvChannelList.CHLST_FLT_VISIBLE chId: ch cursor prevCount: prev chId
     * channel count,except chId itself. nextCount: next chId channel count, if
     * chId exist contain itself ,or not contain. note: default filter fake
     * channel;
     */











    // just for test skip SA minorNo channel.

    public static final String WW_SKIP_MINOR = "ww.sa.skip";

    public static int getProperty(String proName) {
        String line = null;
        Process ifc = null;
        int result = 0;
        try {
            ifc = Runtime.getRuntime().exec("getprop " + proName);
            BufferedReader bis = new BufferedReader(new InputStreamReader(ifc.getInputStream()));
            line = bis.readLine();
            MtkLog.w(TAG, "line =" + line);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ifc.destroy();
        }

        MtkLog.w(TAG, proName + "line =" + line);
        if (line != null && line.length() > 0) {
            try {
                result = Integer.valueOf(line);
            } catch (Exception ex) {

            }
        }

        return result;

    }














    /*
     * only for ch+/- to get up or down channelInfo. US/EU:loop get first not
     * skip channel SA option (true): loop get first not skip majorNo channel.
     * tip: major/minor only for SA (ISDB) DTV Channel, true(UP),false(Down);
     */


    /*
     * for set the opacity of OSD
     */
    public boolean setOpacity(int opacity) {
        boolean bool = false;

        try {
            ITVRemoteService mService = TVCommonUtil.getTVRemoteService();
            bool = mService.setOpacity(opacity);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return bool;
    }

    /**
     * get current focus output, has "main" or "sub"
     *
     * @return
     */
    public String getCurrentFocus() {
        String focusWin = "";
        int result = MtkTvConfig.getInstance().getConfigValue(TV_FOCUS_WIN);
        if (0 == result) {
            focusWin = "main";
        } else if (1 == result) {
            focusWin = "sub";
        }
        return focusWin;
    }

	/**
	 * set current TV mode
	 *
	 * @param tvMode
	 *            :TV_NORMAL_MODE,TV_PIP_MODE,TV_POP_MODE
	 */
	public void setCurrentTVMode(int tvMode) {
		MtkTvConfig.getInstance().setConfigValue(TV_MODE, tvMode);
	}

	/**
	 *
	 * @param TVMode is 0== not support  1==support
	 */
    public void setSupportThirdPIPMode(int supportMode) {

        MtkTvConfig.getInstance().setConfigValue(THIRD_PIP_MODE, supportMode);

    }




    public boolean isCurrentInputSourceHasSignal() {
        return iCurrentInputSourceHasSignal;
    }

    public void setCurrentInputSourceHasSignal(boolean iCurrentInputSourceHasSignal) {
        this.iCurrentInputSourceHasSignal = iCurrentInputSourceHasSignal;
    }

    public boolean isCurrentTVHasSignal() {
        return iCurrentTVHasSignal;
    }

    public void setCurrentTVHasSignal(boolean iCurrentTVHasSignal) {
        this.iCurrentTVHasSignal = iCurrentTVHasSignal;
    }

    public boolean isVideoScrambled() {
        return isVideoScrambled;
    }

    public void setVideoScrambled(boolean isVideoScrambled) {
        this.isVideoScrambled = isVideoScrambled;
    }


	/*
	public void showFavFullMsg() {
		showCommonInfo(
				MyApplication.getAppContext(),
				"Full");
	}

	public void showCommonInfo(Activity activity, String info) {
	    mPopup = new NavCommonInfoBar(activity, info,
				NavCommonInfoBar.WARRNING_INFO);
		mPopup.show();
	}


	public void closeFavFullMsg(){
		if(mPopup!=null && mPopup.isShowing()){
			mPopup.dismiss();
		}
	}
	*/

	/**
	 * if the current source has signal or not
	 * @return
	 */
	public boolean isCurrentSourceHasSignal(){
		if(chBroadCast.isSignalLoss()){
			return false;
		}else{
			return true;
		}
	}


    /**
     * get current tv mode,0-->normal,1-->pip,2-->pop;
     *
     * @return
     */
    public int getCurrentTVState() {
        if(mCurrentTvMode != TV_NORMAL_MODE){
            mCurrentTvMode = instanceMtkTvHighLevel.getCurrentTvMode();
        }

        return mCurrentTvMode;
    }

    public void recordCurrentTvState(int value){
        if(value < TV_NORMAL_MODE || value > TV_POP_MODE){
            return ;
        }

        mCurrentTvMode = value;
    }

	/**
	 * when Ginga mode, ap change output with zoom or screen mode and so,need
	 * call this api
	 *
	 * @param changeState
	 */
	public void updateOutputChangeState(String changeState) {
		mMtkTvAppTV.updatedSysStatus(getCurrentFocus(), changeState);
	}

	/**
	 * get the satellite count
	 * @return
	 */
	public int getSatelliteCount() {
		int count = 0;
		count = mMtkTvDvbsConfigBase.getSatlNumRecs(getSvl());
		return count;
	}

	/**
	 * get satellite chanel list
	 * @param count
	 * @return
	 */


	/**
	 * get satellite names
	 * @param tempResultList
	 * @return
	 */
	public String[] getSatelliteNames(List<MtkTvDvbsConfigInfoBase> tempResultList) {
		if (tempResultList == null) {
			return new String[0];
		}
		int size = tempResultList.size();
		String []names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = tempResultList.get(i).getSatName();
		}
		return names;
	}

	/**
	 * get a default satellite channel
	 * @param name
	 * @return
	 */
	public MtkTvDvbsConfigInfoBase getDefaultSatellite(String name) {
		MtkTvDvbsConfigInfoBase tempSatellite = new MtkTvDvbsConfigInfoBase();
		tempSatellite.setSatlRecId(0);
		tempSatellite.setSatName(name);
		return tempSatellite;
	}


	/**
	 * get the available String from the illegal String of the TV_API
	 * @param illegalString
	 * @return
	 */
	public String getAvailableString(String illegalString) {
		String resultString = "";
		if (null != illegalString && !("").equals(illegalString)) {
			byte[] illegalByte = illegalString.getBytes();
			int j = 0;
			byte[] availableByte = new byte[illegalByte.length];
			for (byte mByte : illegalByte) {
				if (((mByte & 0xff) >= 32 && (mByte & 0xff) != 127)
						|| ((mByte & 0xff) == 10) || ((mByte & 0xff) == 13)) {
					availableByte[j] = mByte;
					j++;
				}
			}
			if (((availableByte[availableByte.length - 1] & 0xff) == 10)
					|| ((availableByte[availableByte.length - 1] & 0xff) == 13))  {
					j--;
			}
			if (null != availableByte) {
				resultString = new String(availableByte, 0, j);
			}
		}
		return resultString;
	}

	private static Object mObject = new Object();
	public Object getCurChInfo(){
		return mObject;
	}

}
