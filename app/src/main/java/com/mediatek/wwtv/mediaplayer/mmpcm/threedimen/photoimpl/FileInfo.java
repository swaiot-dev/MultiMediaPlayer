package com.mediatek.wwtv.mediaplayer.mmpcm.threedimen.photoimpl;

import com.mediatek.wwtv.mediaplayer.mmpcm.Info;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;

public class FileInfo extends Info{
	static private FileInfo mInfo;
	public static FileInfo getInstance() {
		if (mInfo == null) {
			synchronized (FileInfo.class) {
				if (mInfo == null) {
					mInfo = new FileInfo();
				}
			}
		}
		return mInfo;
	}
	
	public MetaData getMetaDataInfo(){
		return null;
	}
	
}
