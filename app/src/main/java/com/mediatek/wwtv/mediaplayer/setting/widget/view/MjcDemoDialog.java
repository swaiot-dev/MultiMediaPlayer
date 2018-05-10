package com.mediatek.wwtv.mediaplayer.setting.widget.view;

import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.setting.util.MarketRegionInfo;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.wwtv.mediaplayer.R;

public class MjcDemoDialog extends Dialog{

	private Context mContext;
	private TextView vLeft;
	private TextView vRight;
	TVContent mTV;
	CommonIntegration mCommonIntegration;


	private void initData() {
		final MenuConfigManager mg = MenuConfigManager.getInstance(mContext);
		mTV = TVContent.getInstance(mContext);
		mCommonIntegration = CommonIntegration.getInstance();
		int value =mg.getDefault(MenuConfigManager.DEMO_PARTITION);
		switch (value) {
            case MtkTvConfigType.CFG_MJC_DEMO_RIGHT:
                vRight.setVisibility(View.VISIBLE);
                vLeft.setVisibility(View.VISIBLE);
                vLeft.setText(mContext.getString(R.string.menu_video_mjc_demo_off));
                vRight.setText(mContext.getString(R.string.menu_video_mjc_demo_on));
                break;
            case MtkTvConfigType.CFG_MJC_DEMO_LEFT:
                vRight.setVisibility(View.VISIBLE);
                vLeft.setVisibility(View.VISIBLE);
                vLeft.setText(mContext.getString(R.string.menu_video_mjc_demo_on));
                vRight.setText(mContext.getString(R.string.menu_video_mjc_demo_off));
                break;
            case MtkTvConfigType.CFG_MJC_DEMO_OFF:
                vRight.setVisibility(View.GONE);
                vLeft.setVisibility(View.GONE);
                break;
            default:
                break;
        }
		// set demo_partition right
		mg.setValue(MenuConfigManager.CFG_VIDEO_VID_MJC_DEMO_STATUS, 1,null);
	}

	public MjcDemoDialog(Context context) {
		super(context, R.style.Theme_ActivityDialog);
		mContext = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_mjc_demo);
		vLeft = (TextView) findViewById(R.id.mjc_left);
		vRight = (TextView) findViewById(R.id.mjc_right);
		initData();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			mTV.setConfigValue(MenuConfigManager.CFG_VIDEO_VID_MJC_DEMO_STATUS, 0);
			dismiss();
			return true;

		case KeyMap.KEYCODE_MTKIR_CHDN:
			dismiss();
			return true;

		case KeyMap.KEYCODE_MTKIR_CHUP:
			dismiss();
			return true;

		case KeyMap.KEYCODE_MTKIR_PRECH:
            dismiss();
			return true;

		// mute
		/*temp code for biaoqing
		case KeyMap.KEYCODE_MTKIR_MUTE:
            MuteView.getInstance(mContext).switchMute();
			return true;
		case KeyMap.KEYCODE_VOLUME_DOWN:
		case KeyMap.KEYCODE_VOLUME_UP:
			if(!MuteView.getInstance(mContext).cancelMute()){
                int volume = 0;
                if(MarketRegionInfo.isFunctionSupport(MarketRegionInfo.F_VOLUME_SYNC)){
                    volume = ((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamVolume(AudioManager.STREAM_MUSIC);
                  }
                  int max = ((AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE)).getStreamMaxVolume(AudioManager.STREAM_MUSIC);

                  if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                      if(volume < max){
                          volume ++;
                      } else{
                          return true;
                      }
                  } else{
                      if(volume > 0){
                          volume --;
                      } else{
                          return true;
                      }
                  }
                  AdjustVolumeView.setVolume(mContext, volume);
              }
			return true;
		*/
		default:
			mTV.setConfigValue(MenuConfigManager.CFG_VIDEO_VID_MJC_DEMO_STATUS, 0);
			dismiss();
			return false;
		}
	}



}
