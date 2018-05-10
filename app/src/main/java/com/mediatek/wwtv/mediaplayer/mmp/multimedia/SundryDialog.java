package com.mediatek.wwtv.mediaplayer.mmp.multimedia;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.view.View;
import android.widget.TextView;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.ModelConstant;
import com.mediatek.wwtv.mediaplayer.mmpcm.CommonSet;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

/**
 * Created by DCC on 2017/5/2.
 */
public class SundryDialog extends Dialog {
    private Context mContext;
    private TextView textView;
    private int curPicMode;
    private int curScreenMode;
    private int curSoundMode;
    private int mType;
    private String[] equalizer;

    public SundryDialog(Context context, int type){
        super(context);
        this.mContext = context;
        this.mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sundry_layout);
        setWindowPosition();
        findView();
        init();
    }

    public void setWindowPosition(){
        Window window = getWindow();
        WindowManager.LayoutParams windowParams = window.getAttributes();
        window.setBackgroundDrawableResource(R.color.transparent);
        windowParams.dimAmount = 0f;
        windowParams.y = 200;
        windowParams.gravity = Gravity.CENTER_HORIZONTAL;
    }

    public void findView(){
        textView = (TextView)findViewById(R.id.sundry_shortTip_textview);
    }

    public void init(){
        if (mType == 0) {
            initPicMode();
        }
        else if (mType == 1){
            initScreenMode();
        }
        else if (mType == 2){
            initSoundMode();
        }
    }

    public void initPicMode(){
        curPicMode = LogicManager.getInstance(mContext).getCurPictureMode();
        Log.d("CCC", "onCreate: ------>" + curPicMode);
        if (curPicMode == 0) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_user));
            Log.d("CCC", "onCreate: ------>" + mContext
                    .getString(R.string.mmp_menu_user));
        } else if (curPicMode == 1) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_cinema));
        } else if (curPicMode == 2) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_sport));
        } else if (curPicMode == 3) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_vivid));
        } else if (curPicMode == 4) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_hibright));
        }
    }

    public void initScreenMode(){
        curScreenMode = LogicManager.getInstance(mContext).getCurScreenMode();
        Log.d("CCC", "onCreate: ------>" + curScreenMode);
        if (curScreenMode == CommonSet.VID_SCREEN_MODE_AUTO) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_auto));
        } else if (curScreenMode == CommonSet.VID_SCREEN_MODE_NORMAL) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_normal));
        } else if (curScreenMode == CommonSet.VID_SCREEN_MODE_PAN_SCAN) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_scan));
        } else if (curScreenMode == CommonSet.VID_SCREEN_MODE_DOT_BY_DOT) {
            setTextView(mContext
                    .getString(R.string.mmp_menu_dotbydot));
        }
        // begin => modified by yangxiong for "add  the miss screenModes for video of mmp"
        else if (curScreenMode == CommonSet.VID_SCREEN_MODE_LETTER_BOX){
            setTextView(mContext
                    .getString(R.string.mmp_menu_letterbox));
        }else if (curScreenMode == CommonSet.VID_SCREEN_MODE_NON_LINEAR_ZOOM){
            setTextView(mContext
                    .getString(R.string.mmp_menu_zoom));
        }else{
            setTextView(mContext
                    .getString(R.string.mmp_menu_auto));
        }
        // end => modified by yangxiong for "add  the miss screenModes for video of mmp"
    }

    public void initSoundMode(){
        equalizer = mContext.getResources().getStringArray(
                R.array.menu_audio_equalizer_array);
        curSoundMode = MtkTvConfig.getInstance().getConfigValue(
                MtkTvConfigType.CFG_AUD_AUD_EQUALIZER);
        if (curSoundMode == 0){
            setTextView(equalizer[0]);
        }
        else if (curSoundMode == 1){
            setTextView(equalizer[1]);
        }
        else if (curSoundMode == 2){
            setTextView(equalizer[2]);
        }
        else if (curSoundMode == 3){
            setTextView(equalizer[3]);
        }
        else if (curSoundMode == 4){
            setTextView(equalizer[4]);
        }
        else if (curSoundMode == 5){
            setTextView(equalizer[5]);
        }
        else if (curSoundMode == 6){
            setTextView(equalizer[6]);
        }
        else if (curSoundMode == 7){
            setTextView(equalizer[7]);
        }
    }

    @Override
    public void show() {
        super.show();
        startTimeOut(3000);
    }


    public void setTextView(String text){
        textView.setText(text);
        textView.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_ASSERTIVE);//add by yx for solving talkback issues
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(event.getScanCode()){
            case KeyMap.KEYCODE_MTKIR_PEFFECT:
            if (mType == 2) return true;//add by yangxiong for solving "block the peffect key if is audio"
                int picMode = LogicManager.getInstance(mContext).getCurPictureMode();
                Log.d("CCC", "onKeyDown: -------------->1--picmode--------->"+picMode);
                if (picMode == ModelConstant.PICTURE_MODEL_USER) {
                    Log.d("CCC", "onKeyDown: ----------------->");
                    LogicManager.getInstance(mContext).setPictureMode(ModelConstant.PICTURE_MODEL_CINEMA);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_cinema));
                } else if (picMode == ModelConstant.PICTURE_MODEL_CINEMA) {
                    LogicManager.getInstance(mContext).setPictureMode(ModelConstant.PICTURE_MODEL_SPORT);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_sport));
                } else if (picMode == ModelConstant.PICTURE_MODEL_SPORT) {
                    LogicManager.getInstance(mContext).setPictureMode(ModelConstant.PICTURE_MODEL_VIVID);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_vivid));
                } else if (picMode == ModelConstant.PICTURE_MODEL_VIVID) {
                    LogicManager.getInstance(mContext).setPictureMode(ModelConstant.PICTURE_MODEL_HIBRIGHT);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_hibright));
                } else if (picMode == ModelConstant.PICTURE_MODEL_HIBRIGHT) {
                    LogicManager.getInstance(mContext).setPictureMode(ModelConstant.PICTURE_MODEL_USER);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_user));
                }
                startTimeOut(3000);
                break;
            case KeyMap.KEYCODE_MTKIR_ASPECT:
             if (mType == 2) return true;//add by yangxiong for solving "block the ASPECT key if is audio"
                int screenMode = LogicManager.getInstance(mContext).getCurScreenMode( );
                // begin => modified by yangxiong for "add  the miss screenModes for video of mmp"
                 int[] modes = LogicManager.getInstance(mContext).getAvailableScreenMode( );
                Log.d("ScreenMode", "initScreenMode:  modes size:" + modes.length);
                int curIndex = -1;
                for (int i = 0; i < modes.length; i++) {
                    Log.d("ScreenMode", "modes[i]:" + modes[i]);
                    if (modes[i]<0)
                        continue;
                    if (screenMode == modes[i]) {
                        curIndex = i;
                        break;
                    }
                }
                Log.d("ScreenMode", "curIndex:" + curIndex);
                if (curIndex < modes.length-1) {
                    curIndex++;
                    Log.d("ScreenMode", "curIndex++:" + curIndex);
                    while (modes[curIndex]<0){
                        curIndex++;
                        if (curIndex == modes.length) {
                            curIndex =0;
                            break;
                        }
                    }
                }else{
                    curIndex = 0;
                }
                LogicManager.getInstance(mContext).setScreenMode(modes[curIndex]);
                Log.d("ScreenMode", "modes[curIndex]:" + modes[curIndex]);
                initScreenMode();
                    /*
                Log.d("CCC", "onKeyDown: -------------->1--screenMode--------->" + screenMode);
                if (screenMode == CommonSet.VID_SCREEN_MODE_AUTO) {
                    Log.d("CCC", "onKeyDown: ----------------->");
                    LogicManager.getInstance(mContext).setScreenMode(CommonSet.VID_SCREEN_MODE_NORMAL);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_normal));
                } else if (screenMode == CommonSet.VID_SCREEN_MODE_NORMAL) {
                    LogicManager.getInstance(mContext).setScreenMode(CommonSet.VID_SCREEN_MODE_PAN_SCAN);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_scan));
                } else if (screenMode == CommonSet.VID_SCREEN_MODE_PAN_SCAN) {
                    LogicManager.getInstance(mContext).setScreenMode(CommonSet.VID_SCREEN_MODE_DOT_BY_DOT);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_dotbydot));
                } else if (screenMode == CommonSet.VID_SCREEN_MODE_DOT_BY_DOT) {
                    LogicManager.getInstance(mContext).setScreenMode(CommonSet.VID_SCREEN_MODE_AUTO);
                    setTextView(mContext
                            .getString(R.string.mmp_menu_auto));
                }*/
                // begin => modified by yangxiong for "add  the miss screenModes for video of mmp"
                startTimeOut(3000);
                break;
            case KeyMap.KEYCODE_MTKIR_SEFFECT:
             if (mType == 0) return true;//add by yangxiong for solving "block the SEFFECT key if is picture"
                String[] soundModeList = mContext.getResources().getStringArray(
                        R.array.menu_audio_equalizer_array);
                int soundValue = MtkTvConfig.getInstance().getConfigValue(
                        MtkTvConfigType.CFG_AUD_AUD_EQUALIZER);
                Log.d("CCC", "onKeyDown--------->soundValue:------>"+soundValue);
                if (soundValue == 0){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 1);
                    setTextView(soundModeList[1]);
                }
                else if (soundValue == 1){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 2);
                    setTextView(soundModeList[2]);
                }
                else if (soundValue == 2){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 3);
                    setTextView(soundModeList[3]);
                }
                else if (soundValue == 3){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 4);
                    setTextView(soundModeList[4]);
                }
                else if (soundValue == 4){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 5);
                    setTextView(soundModeList[5]);
                }
                else if (soundValue == 5){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 6);
                    setTextView(soundModeList[6]);
                }
                else if (soundValue == 6){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 7);
                    setTextView(soundModeList[7]);
                }
                else if (soundValue == 7){
                    MtkTvConfig.getInstance().setConfigValue(MtkTvConfigType.CFG_AUD_AUD_EQUALIZER, 0);
                    setTextView(soundModeList[0]);
                }
                startTimeOut(3000);
                break;
            default:
                Log.d("CCC", "onKeyDown: -------------->2");
                dismiss();
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startTimeOut(int time){
        handler.removeMessages(0);
        handler.sendEmptyMessageDelayed(0,time);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0){
                dismiss();
            }
        }
    };

}
