// ChannelManager.aidl
package com.mediatek.wwtv.mediaplayer.tvchannel;

import com.mediatek.wwtv.mediaplayer.tvchannel.ProgramFile;
import com.mediatek.wwtv.mediaplayer.tvchannel.AddCallBack;
import java.util.List;

interface ChannelManager {

    void addPrograms(in List<ProgramFile> programs);

    void registerCallback(AddCallBack cb);

    void unregisterCallback(AddCallBack cb);
}
