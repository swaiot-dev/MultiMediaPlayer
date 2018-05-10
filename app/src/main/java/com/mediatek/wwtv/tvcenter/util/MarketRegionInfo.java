package com.mediatek.wwtv.tvcenter.util;

import android.util.SparseBooleanArray;
import android.os.SystemProperties;
import com.mediatek.wwtv.util.MtkLog;

public final class MarketRegionInfo {
    private static final String TAG = "MarketRegion";

    /* MarketRegion Info */
    public static final int REGION_CN       = 0;
    public static final int REGION_US       = 1;
    public static final int REGION_SA       = 2;
    public static final int REGION_EU       = 3;

    /* Functions Info */
    public static final int F_EAS           = 0;
    public static final int F_EWS           = 1;
    public static final int F_GINGA         = 2;
    public static final int F_MHP           = 3;
    public static final int F_DVBS          = 4;
    public static final int F_HBBTV         = 5;
    public static final int F_TTX           = 6;
    public static final int F_SUBTITLE      = 7;
    public static final int F_CI            = 8;
    public static final int F_MHEG5         = 9;
    public static final int F_OAD           = 10;
    public static final int F_AV_RECORD_LIB = 11;
    public static final int F_VOLUME_SYNC   = 12;
    public static final int F_TIF_SUPPORT   = 13;
    public static final int F_VSS_SUPPORT   = 14;
    public static final int F_TIF_CEC       = 15;
    public static final int F_OCEANIA       = 16;
    public static final int F_TV_CAPTION    = 17;//google TvView setCaptionEnabled
    public static final int F_TIF_BANNER    = 18;
    public static final int F_TIF_PWD       = 19;
    public static final int F_MODULES_WITH_TIF = 20;
    public static final int F_TIF_RATING    = 21;
    public static final int F_TIF_SUBTITLE  = 22;
    public static final int F_DIVX_SUPPORT   = 23;
    public static final int F_DISEQC12_IMPROVE = 24;

    public static final int F_MULTI_VIEW_SUPPORT = 26;

    public static final int F_ALLOW_TIMESYNC = 25;
    public static final int F_NEW_APP = 27;
    public static final int F_TIF_RATING_SA  = 28;

    private static SparseBooleanArray sba = null;
    private static int region = REGION_CN;

    private MarketRegionInfo(){
    }

    static{
        init();
    }

    private static boolean init(){
        if(sba == null){
            sba = new SparseBooleanArray();
        }
        else{
            sba.clear();
        }

        String marketregion = SystemProperties.get("ro.mtk.system.marketregion");
        MtkLog.d(TAG, "marketregion>>>" + marketregion);
        if(marketregion != null && marketregion.length() > 0){
            if(marketregion.equals("us")){
                region = REGION_US;

                //F_EAS
                sba.append(F_EAS, true);
            }
            else if(marketregion.equals("eu")){
                region = REGION_EU;

                //F_MHP
                sba.append(F_MHP, (1 == SystemProperties.getInt("ro.mtk.system.mhp.existed", 0)) ? true : false);
                //F_DVBS
                sba.append(F_DVBS, (1 == SystemProperties.getInt("ro.mtk.system.dvbs.existed", 0)) ? true : false);
                //F_HBBTV
                sba.append(F_HBBTV, (1 == SystemProperties.getInt("ro.mtk.system.hbbtv.existed", 0)) ? true : false);
                //F_TTX
                sba.append(F_TTX, (1 == SystemProperties.getInt("ro.mtk.system.ttx.existed", 0)) ? true : false);
                //F_SUBTITLE
                sba.append(F_SUBTITLE, (1 == SystemProperties.getInt("ro.mtk.system.subtitle.existed", 0)) ? true : false);
                //F_CI
                sba.append(F_CI, (1 == SystemProperties.getInt("ro.mtk.system.ci.existed", 0)) ? true : false);
                //F_MHEG5
                sba.append(F_MHEG5, (1 == SystemProperties.getInt("ro.mtk.system.mheg5.existed", 0)) ? true : false);
                //F_OAD
                sba.append(F_OAD, (1 == SystemProperties.getInt("ro.mtk.system.oad.existed", 0)) ? true : false);
                //F_OCEANIA
                sba.append(F_OCEANIA, (1 == SystemProperties.getInt("ro.mtk.system.eu.oceania", 0)) ? true : false);
                //F_DISEQC 1.2
                sba.append(F_DISEQC12_IMPROVE, (1 == SystemProperties.getInt("ro.mtk.system.diseqcip.existed", 0)) ? true : false);
                //allow time sync
                sba.append(F_ALLOW_TIMESYNC, (1 == SystemProperties.getInt("ro.mtk.system.timesync.existed", 0)) ? true : false);
            }
            else if(marketregion.equals("sa")){
                region = REGION_SA;

                //F_GINGA
                sba.append(F_GINGA, (1 == SystemProperties.getInt("ro.mtk.system.ginga.existed", 0)) ? true : false);
                //F_EWS
                sba.append(F_EWS, true);
            } else if (marketregion.equals("cn")) {
            	region = REGION_CN;
            }

            //F_AV_RECORD_LIB
            sba.append(F_AV_RECORD_LIB, (1 == SystemProperties.getInt("ro.mtk.system.av_record_lib", 0)) ? true : false);

            MtkLog.d(TAG, "MarketRegion info:" + marketregion + "," + region);
        }
        else{
            MtkLog.d(TAG, "can't get MarketRegion~");
        }

        sba.append(F_VOLUME_SYNC, (1 == SystemProperties.getInt("ro.mtk.system.audiosync", 0)) ? true : false);

        sba.append(F_TIF_SUPPORT, true);

        sba.append(F_NEW_APP, true);

        if(sba.get(F_TIF_SUPPORT, false)){
            sba.append(F_TIF_CEC, (1 == SystemProperties.getInt("ro.mtk.system.cec.hal", 0)) ? true : false);
        }
        else{
            sba.append(F_TIF_CEC, false);
        }

        if (sba.get(F_TIF_SUPPORT,false)) {
			sba.append(F_TIF_BANNER, false);
		}else {
			sba.append(F_TIF_BANNER, false);
		}

        if (sba.get(F_TIF_SUPPORT, false)) {
			sba.append(F_TIF_PWD, true);
		}else {
			sba.append(F_TIF_PWD, false);
		}

        if (sba.get(F_TIF_SUPPORT, false)) {
			sba.append(F_TIF_SUBTITLE, false);
		}else {
			sba.append(F_TIF_SUBTITLE, false);
		}
        sba.append(F_VSS_SUPPORT, (1 == SystemProperties.getInt("ro.mtk.system.vss.existed",0))? true : false);
        sba.append(F_DIVX_SUPPORT, (1 == SystemProperties.getInt("ro.mtk.system.divx.existed",0))? true : false);
        //set caption default
        if (sba.get(F_TIF_SUPPORT,false)) {
        	if(region == REGION_US || region == REGION_SA){
				sba.append(F_TV_CAPTION, true);
			}else{
				sba.append(F_TV_CAPTION, false);
			}
		}else {
			sba.append(F_TV_CAPTION, false);
		}

		if (sba.get(F_TIF_SUPPORT, false)) {
			sba.append(F_MODULES_WITH_TIF, true);
		}else {
			sba.append(F_MODULES_WITH_TIF, false);
		}
		//TIF_RATING is rejected need not use
        if (sba.get(F_TIF_SUPPORT,false)) {
        	if(region == REGION_US || region == REGION_EU){//support us and eu no aus
        		sba.append(F_TIF_RATING, true);
        	}else{
        		sba.append(F_TIF_RATING, false);
        	}
        	if(region == REGION_SA){
        		sba.append(F_TIF_RATING_SA, true);
        	}
		}else {
			sba.append(F_TIF_RATING, false);
		}

        sba.append(F_MULTI_VIEW_SUPPORT, true);

        MtkLog.d(TAG, "functions array size:" + sba.size());
        return true;
    }

    /**
     * this method is used to get current Market Region info
     *
     * @return
     *      the MarketRegion info, refer REGION_CN,REGION_US,REGION_SA,REGION_EU
     */
    public static int getCurrentMarketRegion(){
        return region;
    }

    /**
     * this method is used to check whether this function is support or not
     *
     * @param function
     *          refer F_EAS,F_EWS,F_GINGA,...
     * @return
     *          true,support
     *          false, not support
     */
    public static boolean isFunctionSupport(int function){
        return sba.get(function, false);
    }
}
