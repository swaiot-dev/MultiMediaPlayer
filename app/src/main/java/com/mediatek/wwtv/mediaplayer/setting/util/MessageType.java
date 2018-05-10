package com.mediatek.wwtv.mediaplayer.setting.util;

public final class MessageType {
	public static final int WAIT_DELAY_MESSAGE = 100;
	public static final int MESSAGE1 = 101;
	public static final int MESSAGE2 = 102;
	public static final int MENU_AUTO_EXIT_MESSAGE = 103;
	public static final int MESSAGE_LR = 104;
	public static final int MESSAGE_UPDATE_POWOFF_TIMER = 106;
	public static final int MESSAGE_NOTLR = 105;
	public static final int RF_DELAY_MESSAGE = 107;
	public static final int EDITCHANNEL_NEED_CI = 111;
	public static final int FROM_MENUMAIN_TO_TK = 112;
	
	public static final int MESSAGE_CHANNEL_CHANGED = 113;
	
		public static final int MESSAGE_SETOPACITY = 114;
		public static final int MESSAGE_AUTOADJUST = 115;
		public static final int MESSAGE_AUTOCOLOR = 116;
		public static final int MESSAGE_REFSH_TUNEDIALOG = 117;
		public static final int MESSAGE_REFSH_RECORDING_SETTING = 118;
		public static final int MESSAGE_REFSH_SYSTEM_INFO = 119;
	public static final int MESSAGE_DVBT_THD_FULLSCAN = 120;
	public static final int MESSAGE_REFRESH_SIGNAL_QUALITY_LEVEL = 121;
	public static final int MESSAGE_AFTER_SCAN_SELECT_CHANNEL = 122;
	public static final int MESSAGE_INACTIVE_CHANNELS = 123;
		
	public static final long delayMillis1 = 9000000;
	public static final long delayMillis2 = 60000;
	public static final long delayMillis3 = 30000;
	public static final long delayMillis4 = 5000;
	public static final long delayMillis5 = 2000;
	public static final long delayMillis6 = 1000;
	public static final long delayMillis10 = 10000;
	public static final long delayForTKToMenu = 6000;

	// for Nav
	public static final int NAV_NUMKEY_CHANGE_CHANNEL = 200;
	public static final int NAV_SOURCE_LISTVIEW_DIMISS = 201;
	public static final int NAV_ADUST_VOLUME_DIMISS = 202;
	public static final int NAV_SHORTTIP_TEXTVIEW_DIMISS = 203;
	public static final int NAV_ZOOMVIEW_DIMISS = 204;
	public static final int NAV_SHOW_SPECIALVIEW = 205;
	public static final int NAV_CHANGE_IS_FIRST=218;

	//for show no TTX
	public static final int SHOW_NO_TTX=1001;
	// Lock ,sgnal,scann chennel
	public static final int NAV_CURRENT_SOURCE_LOCKED = 206;
	public static final int NAV_CURRENT_SOURCE_NO_SIGNAL = 207;
	public static final int NAV_SCANN_CHANNEL = 208;
	public static final int NAV_CURRENT_CHANNEL_NO_SIGNAL = 209;
	public static final int NAV_CURRENT_CHANNEL_LOCKED = 210;
	public static final int NAV_TV_SOURCE_NO_SIGNAL = 211;
	public static final int NAV_CHEECK_LOCKED_SIGNAL_HASCHANNEL_STATUS =214;
	public static final int NAV_SHOW_CURRENT_CHANNEL_INFO =215;
	// BannerView
	public static final int NAV_BANNERVIEW_DIMISS = 212;
	// InputPwdView
	public static final int NAV_INPUTPWDVIEW_DIMISS = 213;
	//info
	public static final int NAV_SHOW_CHANNEL_INFO =216;
	public static final int NAV_SHOW_CHANNEL_INFO_DIMISS =217;
	
	//for tv scan completed
	public static final int MENU_TV_SCAN_COMPLETED =300;
	public static final int MENU_TV_RF_SCAN_REFRESH =301;
	public static final int MENU_TV_RF_SCAN_REFRESH_GETLEVEL =302;
	public static final int MENU_DVBT_RF_SCAN_TUNESIGNAL = 310;
	public static final int FORM_TK_TO_MENUMAIN = 219;
	public static final int FBM_MODE_SWITCH_OK = 3333;

	//for warm boot
	public static final int VIDEO_BLUE_MUTE_REVERT = 400;
	//for pipsize delay
	public static final int MESSAGE_FOR_PIPSIZE_DELAY = 401;
	public static final long FOR_PIPSIZE_DELAY_TIME = 100;
}
