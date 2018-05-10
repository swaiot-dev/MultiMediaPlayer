package com.mediatek.wwtv.mediaplayer.util;

public class MmpConst {



	public static final int	NONE 		= 0;
	public static final int REPEATE_ONE = 1;
	public static final int REPEATE_ALL = 2;
	public static final int VIDEO 		= 0;
	public static final int PHOTO 		= 1;
	public static final int AUDIO 	 	= 2;
	public static final int TXT  		= 3;
	public static final int DMR 		= 4;

	public static final String AllMedia  =  "allmedia";


	//from menuconfigmanager
    public static final String DLNA = "SETUP_dlna_mmp";
    public static final String MY_NET_PLACE = "SETUP_net_place_mmp";

    //from MtkFilesBaseListActivity
    public static final int MODE_NORMAL = 1;
	public static final int MODE_RECURSIVE = 2;
	public static final String INTENT_NAME_PATH = "Path";
	public static final String INTENT_NAME_SELECTION = "Position";
	public static final String INTENT_NAME_COPYED_FILES = "CopyedFiles";

	//from 4k2k
	public static final String PLAY_MODE = "PlayMode";
	public static final int NORMAL_MODE = 0;

	//from flim
	public static final String MEDIA_PATH = "media_path";
	public static final String TO_3DBROWSE = "To3DBrowse";
	public static final String FROM_PLAYTYPE="FromMusicPlay";
	public static final String RECURSER_MODE = "RecurserMode";
	public static final String CURRENT_BROWSE_STATUS ="CurrentBrowseStatus";

	public enum BrowseStatus {
		FROM_GRID_STATUS,
		FROM_LIST_STATUS,
		FROM_PLAY_STATUS,
		NORMAL_STATUS,
	}
	public static final String MMP_VIDEO = "Video";
	public static int  MMP_VIDEO_INT = 0;
	public static final String MMP_PHOTO = "Photo";
	public static int  MMP_PHOTO_INT = 1;
	public static final String MMP_AUDIO = "Audio";
	public static int  MMP_AUDIO_INT = 2;
	public static final String MMP_TEXT = "Text";
	public static int  MMP_TEXT_INT = 3;
	public static final String MMP_DMR = "DMR";
	public static int  MMP_DMR_INT = 4;
	public static final String MMP_SETTING = "SETTING";
	public static int  MMP_SETTING_INT = 5;


	//Activity intent
	public static final String INTENT_TEXT = "android.mtk.intent.action.mediaplayer.test";
	public static final String INTENT_MUSIC = "android.mtk.intent.action.mediaplayer.music";
	public static final String INTENT_VIDEO = "android.mtk.intent.action.mediaplayer.video";
	public static final String INTENT_PHOTO = "android.mtk.intent.action.mediaplayer.photo";
	public static final String INTENT_3DPHOTO = "android.mtk.intent.action.mediaplayer.3dphoto";
	public static final String INTENT_4K2KPHOTO = "android.mtk.intent.action.mediaplayer.4k2kphoto";
	public static final String INTENT_FILELIST = "android.mtk.intent.action.mediaplayer.list";
	public static final String INTENT_FILEGRID = "android.mtk.intent.action.mediaplayer.grid";
	public static final String INTENT_FILEBROWSE = "android.mtk.intent.action.mediaplayer.filebrowse";
	public static final String INTENT_SETTING = "android.mtk.intent.action.mediaplayer.setting";
    public static final String INTENT_AUDIO_SETTING = "android.mtk.intent.action.mediaplayer.audio.setting";
	public static final String INTENT_NETSETTING = "android.mtk.intent.action.mediaplayer.netsetting";
	public static final String INTENT_DMR = "android.mtk.intent.action.mmp.dmr";




}
