package com.mediatek.wwtv.mediaplayer.setting.util;

/**
 * 
 * @author sin_biaoqinggao
 *
 */
public class RatingConst {
	
	/*tv rating domain*/
	public static final String RATING_DOMAIN = "com.android.tv";
	
	/*us tv rating system*/
	public static final String RATING_SYS_US_TV = "US_TV";
	/*dvb tv rating system*/
	public static final String RATING_SYS_DVB_TV = "DVB";
	/*au tv rating system*/
	public static final String RATING_SYS_AU_TV = "AU_TV";
	/*us movie rating system*/
	public static final String RATING_SYS_US_MV = "US_MV";
	
	/*us canadian english language rating system*/
	public static final String RATING_SYS_US_CA_EN_TV = "CA_EN_TV";
	
	/*us canadian french language rating system*/
	public static final String RATING_SYS_US_CA_FR_TV = "CA_TV";
	
	/*us tv rating*/
	public static final String RATING_US_TV_Y = "US_TV_Y";
	public static final String RATING_US_TV_Y7 = "US_TV_Y7";
	public static final String RATING_US_TV_G = "US_TV_G";
	public static final String RATING_US_TV_PG = "US_TV_PG";
	public static final String RATING_US_TV_14 = "US_TV_14";
	public static final String RATING_US_TV_MA = "US_TV_MA";
	
	/*us tv sub rating */
	public static final String SUB_RATING_US_TV_A = "US_TV_A";//a custom sub rating
	public static final String SUB_RATING_US_TV_D = "US_TV_D";
	public static final String SUB_RATING_US_TV_L = "US_TV_L";
	public static final String SUB_RATING_US_TV_S = "US_TV_S";
	public static final String SUB_RATING_US_TV_V = "US_TV_V";
	public static final String SUB_RATING_US_TV_FV = "US_TV_FV";
	
	/*us tv sub rating array for every rating */
	public static final String[] US_TV_Y_SUB_RATINGS = {SUB_RATING_US_TV_A};
	public static final String[] US_TV_Y7_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_FV};
	public static final String[] US_TV_G_SUB_RATINGS = {SUB_RATING_US_TV_A};
	public static final String[] US_TV_PG_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	public static final String[] US_TV_14_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	public static final String[] US_TV_MA_SUB_RATINGS = {SUB_RATING_US_TV_A,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	
	/*us tv sub rating array for every rating without US_TV_A*/
	public static final String[] US_TV_Y_SUB_RATINGS_N_A = {};
	public static final String[] US_TV_Y7_SUB_RATINGS_N_A = {SUB_RATING_US_TV_FV};
	public static final String[] US_TV_G_SUB_RATINGS_N_A = {};
	public static final String[] US_TV_PG_SUB_RATINGS_N_A = {SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	public static final String[] US_TV_14_SUB_RATINGS_N_A = {SUB_RATING_US_TV_D,SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	public static final String[] US_TV_MA_SUB_RATINGS_N_A = {SUB_RATING_US_TV_L,SUB_RATING_US_TV_S,SUB_RATING_US_TV_V};
	
	/********************************australia tv rating****************************************/
	/*au tv rating*/
	public static final String RATING_AU_TV_P = "AU_TV_P";
	public static final String RATING_AU_TV_C = "AU_TV_C";
	public static final String RATING_AU_TV_G = "AU_TV_G";
	public static final String RATING_AU_TV_PG = "AU_TV_PG";
	public static final String RATING_AU_TV_M = "AU_TV_M";
	public static final String RATING_AU_TV_MA = "AU_TV_MA";
	public static final String RATING_AU_TV_AV = "AU_TV_AV";
	public static final String RATING_AU_TV_R = "AU_TV_R";
	//custom type
	public static final String RATING_AU_TV_ALL = "ALL";
	
	/************US Movie rating*************/
	public static final String RATING_US_MV_G = "US_MV_G";
	public static final String RATING_US_MV_PG = "US_MV_PG";
	public static final String RATING_US_MV_PG13 = "US_MV_PG13";
	public static final String RATING_US_MV_R = "US_MV_R";
	public static final String RATING_US_MV_NC17 = "US_MV_NC17";
	public static final String RATING_US_MV_X = "US_MV_X";
	
	/************US Canadian english rating*************/
	public static final String RATING_US_CA_EN_TV_EXEMPT = "CA_EN_TV_EXEMPT";
	public static final String RATING_US_CA_EN_TV_C = "CA_EN_TV_C";
	public static final String RATING_US_CA_EN_TV_C8 = "CA_EN_TV_C8";
	public static final String RATING_US_CA_EN_TV_G = "CA_EN_TV_G";
	public static final String RATING_US_CA_EN_TV_PG = "CA_EN_TV_PG";
	public static final String RATING_US_CA_EN_TV_14 = "CA_EN_TV_14";
	public static final String RATING_US_CA_EN_TV_18 = "CA_EN_TV_18";
	
	/*************US Canadian french rating***************/
	public static final String RATING_US_CA_FR_TV_E = "CA_FR_TV_E";
	public static final String RATING_US_CA_FR_TV_G = "CA_FR_TV_G";
	public static final String RATING_US_CA_FR_TV_8 = "CA_FR_TV_8";
	public static final String RATING_US_CA_FR_TV_13 = "CA_FR_TV_13";
	public static final String RATING_US_CA_FR_TV_16 = "CA_FR_TV_16";
	public static final String RATING_US_CA_FR_TV_18 = "CA_FR_TV_18";
	
}
