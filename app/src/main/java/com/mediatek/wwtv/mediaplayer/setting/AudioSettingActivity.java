package com.mediatek.wwtv.mediaplayer.setting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;

import com.mediatek.twoworlds.tv.common.MtkTvConfigType;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvCI;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.setting.util.TvCallbackConst;
import com.mediatek.wwtv.mediaplayer.setting.util.TvCallbackData;
import com.mediatek.twoworlds.tv.model.MtkTvChannelInfoBase;
import com.mediatek.wwtv.mediaplayer.setting.base.BaseSettingsActivity;
import com.mediatek.wwtv.mediaplayer.setting.base.SpecialOptionDealer;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.DynamicOptionFrag;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.ProgressBarFrag;
import com.mediatek.twoworlds.tv.MtkTvAppTVBase;
import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.MessageType;
import com.mediatek.wwtv.mediaplayer.setting.util.MessageType;
import com.mediatek.wwtv.mediaplayer.setting.util.TvCallbackConst;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.mediaplayer.setting.widget.view.LiveTVDialog;
import com.mediatek.wwtv.mediaplayer.setting.widget.view.MjcDemoDialog;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.util.MtkLog;

public class AudioSettingActivity extends BaseSettingsActivity implements ActionAdapter.Listener,
    SpecialOptionDealer {

  private final static String TAG = "AudioSettingActivity";
  private String ItemName;

  private Context mContext;
  private AudioManager audioManager;
  private static AudioSettingActivity mInstance = null;

  private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
        if (mCurrAction.getmDataType() == Action.DataType.TOPVIEW && mCurrAction.getmItemId().equals("Menu Audio") && mCurrAction.getmTitle().equals("Menu Audio")){
          mCurrAction = new Action("Menu Audio", "Menu Audio", Action.DataType.TOPVIEW);
          setActionsForTopView();
          updateView();
        } else if(mCurrAction.getmItemId().equals(MenuConfigManager.SRS_MODE) || mCurrAction.getmItemId().equals(MenuConfigManager.SPEAKER_MODE)){
          goBack();
        }
      }
    }
  };

  public static AudioSettingActivity getInstance() {
    return mInstance;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    mContext = this;
    mCurrAction = new Action("Menu Audio", "Menu Audio", Action.DataType.TOPVIEW);
    super.onCreate(savedInstanceState);
    mInstance = this;
    mTV.addSingleLevelCallBackListener(mSignalHandler);
    audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    mSpecialOptionDealer = this;
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction("android.intent.action.HEADSET_PLUG");
    this.registerReceiver(mReceiver, intentFilter);
  }

  @Override
  protected Object getInitialDataType() {
    return DataType.TOPVIEW;
  }

  @Override
  protected void updateView() {
    switch ((Action.DataType) mState) {
      case TOPVIEW:
      case OPTIONVIEW:
      case SWICHOPTIONVIEW:
      case EFFECTOPTIONVIEW:
      case HAVESUBCHILD:
        int vi_st_index = 0;
        if (mCurrAction.mItemID.equals(MenuConfigManager.SOUND_TRACKS)) {
          vi_st_index = this.loadSoundTrack(soundtrack);
          MtkLog.d(TAG, "updateView for soundtracks selIndex = " + vi_st_index);
        } else if (mCurrAction.mItemID.equals(MenuConfigManager.CFG_MENU_AUDIOINFO)) {
          // visually impaired detail
          vi_st_index = this.loadVisuallyImpaired(visuallyImpairedAudioInfo);
          MtkLog.d(TAG, "updateView for visually impaired selIndex = " + vi_st_index);
        }
        refreshActionList();
        setView((mCurrAction == null ? "Menu Audio" : mCurrAction.getTitle()),
            (mParentAction == null ? mContext.getResources().getString(
                R.string.menu_header_name) : mParentAction.getTitle()),
            "", R.drawable.menu_audio_icon);
        if (vi_st_index > 0
            && (mCurrAction.mItemID.equals(MenuConfigManager.SOUND_TRACKS)
            || mCurrAction.mItemID.equals(MenuConfigManager.CFG_MENU_AUDIOINFO))) {
          Handler mHandler = new Handler();
          final int index = vi_st_index;
          mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
              if (mActionFragment instanceof ActionFragment) {
                ((ActionFragment) mActionFragment).getScrollAdapterView().setSelection(index);
              }
            }

          }, 300);

        }

        break;
      case PROGRESSBAR:
        ProgressBarFrag frag = new ProgressBarFrag();
        frag.setAction(mCurrAction);
        mActionFragment = frag;
        setViewWithActionFragment((mCurrAction == null ? "Menu Audio" : mCurrAction.getTitle()),
            (mParentAction == null ? mContext.getResources().getString(
                R.string.menu_header_name) : mParentAction.getTitle()),
            "", R.drawable.menu_audio_icon);
        break;
      case LASTVIEW:
        break;
      default:
        break;
    }

  }

  @Override
  protected void setActionsForTopView() {
    loadDataAudio(mCurrAction);
    mActions.addAll(mCurrAction.mSubChildGroup);
  }

  @Override
  protected void setProperty(boolean enable) {

  }

  protected void setViewWithActionFragment(String title, String breadcrumb, String description,
      int iconResId) {
    mContentFragment = ContentFragment.newInstance(title, breadcrumb, description, iconResId,
        getResources().getColor(R.color.icon_background));
    setContentAndActionFragments(mContentFragment, mActionFragment);
  }

  @Override
  public void onActionClicked(Action action) {
    if (action.mDataType == DataType.SAVEDATA) {
      String mId = action.mItemID;
      if (mId.startsWith(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING)) {
        String[] spls = mId.split("_");
        if (spls != null && spls.length == 2) {
          int index = Integer.parseInt(spls[1]);
          MtkLog.d(TAG, "set CFG_MENU_AUDIOINFO_GET_STRING" + index);
          mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_SELECT, index);
        } else
          MtkLog.e(TAG, "mID is not correct:" + mId);
      } else if (mId.startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)) {
        String[] spls = mId.split("_");
        if (spls != null && spls.length == 2) {
          int index = Integer.parseInt(spls[1]);
          MtkLog.d(TAG, "set SOUNDTRACKS_SET_SELECT" + index);
          mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_SELECT, index);
        } else
          MtkLog.e(TAG, "mID is not correct:" + mId);
      }
    }
    super.onActionClicked(action);
  }

  /**
   * may need to handle special item like visually-impaired or power-on-channel
   */
  private void handleSpecialItem() {

  }

  Action audioVisuallyImpaired;
  Action soundtrack;
  Action visuallyImpairedAudioInfo;
  Action visuallyvolume;

  /**
   * load data for Audio
   */
  public void loadDataAudio(Action mAudio) {
    mAudio.mSubChildGroup = new ArrayList<Action>();
    String[] SRS_Mode = mContext.getResources().getStringArray(
        R.array.menu_audio_srs_mode_array);
    String[] equalizer = mContext.getResources().getStringArray(
        R.array.menu_audio_equalizer_array);
    String[] Speaker_Mode = mContext.getResources().getStringArray(
        R.array.menu_audio_speaker_mode_array);
    String[] SPDIF_Mode;
    if (mTV.isCNRegion()) {
      SPDIF_Mode = mContext.getResources().getStringArray(
          R.array.menu_audio_spdif_mode_us_array);
    } else {
      SPDIF_Mode = mContext.getResources().getStringArray(
          R.array.menu_audio_spdif_mode_us_array);
      int spdifTypeOption = 0;
      spdifTypeOption = mConfigManager.getMax(MenuConfigManager.SPDIF_MODE)
                            - mConfigManager.getMin(MenuConfigManager.SPDIF_MODE);
      MtkLog.d(TAG,"spdifTypeOption=="+spdifTypeOption);
      if(spdifTypeOption == 5) {
          SPDIF_Mode = mContext.getResources().getStringArray(
              R.array.menu_audio_spdif_mode_us_array_ms12b);
      }
    }
    String[] AVC_Mode = mContext.getResources().getStringArray(
        R.array.menu_audio_avc_mode_array);

    String[] TYPE_Mode;
    if (Util.mIsUseEXOPlayer) {
        TYPE_Mode = mContext.getResources().getStringArray(
            R.array.menu_audio_type_no_ad_typ_array);
    } else {
        TYPE_Mode = mContext.getResources().getStringArray(
            R.array.menu_audio_type_array);
    }
    String[] mVisuallySpeaker = mContext.getResources().getStringArray(
        R.array.menu_audio_visually_speaker_array);
    String[] mVisuallyHeadphone = mContext.getResources().getStringArray(
        R.array.menu_audio_visually_headphone_array);

    ItemName = MenuConfigManager.BALANCE;
    Action audioBalance = new Action(ItemName,
        mContext.getString(R.string.menu_audio_balance),
        mConfigManager.getMin(ItemName),
        mConfigManager.getMax(ItemName),
        mConfigManager.getDefault(ItemName), null,
        MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);

    ItemName = MenuConfigManager.BASS;
    Action audioBass = new Action(ItemName,
        mContext.getString(R.string.menu_audio_bass),
        mConfigManager.getMin(ItemName),
        mConfigManager.getMax(ItemName),
        mConfigManager.getDefault(ItemName), null,
        MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);

    ItemName = MenuConfigManager.TREBLE;
    Action audioTreble = new Action(ItemName,
        mContext.getString(R.string.menu_audio_treble),
        mConfigManager.getMin(ItemName),
        mConfigManager.getMax(ItemName),
        mConfigManager.getDefault(ItemName), null,
        MenuConfigManager.STEP_VALUE, Action.DataType.PROGRESSBAR);

    ItemName = MenuConfigManager.SRS_MODE;
    int initValue;
    if (audioManager == null) {
      audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
    if (audioManager.isWiredHeadsetOn()){
      initValue = 0;
    } else {
      initValue = mConfigManager.getDefault(ItemName)
              - mConfigManager.getMin(ItemName);
    }
    Action audioSoundSur = new Action(ItemName,
        mContext.getString(R.string.menu_audio_srs_mode),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE, initValue, SRS_Mode,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

    ItemName = MenuConfigManager.EQUALIZE;
    Action audioEqualizer = new Action(ItemName,
        mContext.getString(R.string.menu_audio_equalize),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), equalizer,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

    ItemName = MenuConfigManager.SPEAKER_MODE;
    Action audioSpeaker = new Action(ItemName,
        mContext.getString(R.string.menu_audio_speaker_mode),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), Speaker_Mode,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

    ItemName = MenuConfigManager.SPDIF_MODE;
    Action audioSpdifType = new Action(ItemName,
        mContext.getString(R.string.menu_audio_spdif_mode),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), SPDIF_Mode,
        MenuConfigManager.STEP_VALUE, Action.DataType.SWICHOPTIONVIEW);

    ItemName = MenuConfigManager.TYPE;
    Action audioType = new Action(ItemName,
        mContext.getString(R.string.menu_audio_type),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), TYPE_Mode,
        MenuConfigManager.STEP_VALUE, Action.DataType.SWICHOPTIONVIEW);

    ItemName = MenuConfigManager.VISUALLY_IMPAIRED;
    audioVisuallyImpaired = new Action(ItemName,
        mContext.getString(R.string.menu_audio_visually_impaired),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE, null,
        MenuConfigManager.STEP_VALUE, Action.DataType.HAVESUBCHILD);

    audioType.mEffectGroup = new ArrayList<Action>();
    audioType.mEffectGroup.add(audioVisuallyImpaired);
    audioType.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
    // normal can not use
    audioType.mSwitchHashMap.put(0, new Boolean[] {
        false
    });
    // hearing impaired can not use
    audioType.mSwitchHashMap.put(1, new Boolean[] {
        false
    });
    // visually impaired available
    if (mTV.isCNRegion()) {
      audioType.mSwitchHashMap.put(2, new Boolean[] {
          true
      });
    } else {
      audioType.mSwitchHashMap.put(2, new Boolean[] {
          mTV.isConfigEnabled(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)
      });
    }

    // when parent initial data is visually,available of down
    if (mConfigManager.getDefault(MenuConfigManager.TYPE) == 2) {
      if (mTV.isConfigEnabled(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
        audioVisuallyImpaired.setEnabled(true);
      } else {
        audioVisuallyImpaired.setEnabled(false);
      }
    } else {
      audioVisuallyImpaired.setEnabled(false);
    }
    audioVisuallyImpaired.mSubChildGroup = new ArrayList<Action>();

    // VisuallySpeaker
    ItemName = MenuConfigManager.VISUALLY_SPEAKER;
    audioVisuallyImpaired.mSubChildGroup.add(new Action(ItemName,
        mContext.getString(R.string.menu_audio_visually_speaker),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), mVisuallySpeaker,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW));

    // VisuallyHeadphone
    ItemName = MenuConfigManager.VISUALLY_HEADPHONE;
    audioVisuallyImpaired.mSubChildGroup.add(new Action(ItemName,
        mContext.getString(R.string.menu_audio_visually_headphone),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), mVisuallyHeadphone,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW));

    // VisuallyVolume
    ItemName = MenuConfigManager.VISUALLY_VOLUME;
    visuallyvolume = new Action(ItemName,
        mContext.getString(R.string.menu_audio_visually_volume),
        mConfigManager.getMin(ItemName), mConfigManager
            .getMax(ItemName), mConfigManager.getDefault(ItemName),
        null, MenuConfigManager.STEP_VALUE,
        Action.DataType.PROGRESSBAR);
    audioVisuallyImpaired.mSubChildGroup.add(visuallyvolume);
    if (mConfigManager.getDefault(MenuConfigManager.VISUALLY_SPEAKER) == 0
        && mConfigManager.getDefault(MenuConfigManager.VISUALLY_HEADPHONE) == 0) {
      visuallyvolume.setEnabled(false);
    } else {
      visuallyvolume.setEnabled(true);
    }
    audioVisuallyImpaired.setmParentGroup(mAudio.mSubChildGroup);
    if (mTV.isEURegion()) {
      // VISUALLY_PAN_FADE 
/*  
      ItemName = MenuConfigManager.VISUALLY_PAN_FADE;
      audioVisuallyImpaired.mSubChildGroup
          .add(new Action(ItemName,
              mContext.getString(R.string.menu_audio_visually_pan_and_fade),
              mConfigManager.getMin(ItemName), mConfigManager
                  .getMax(ItemName), mConfigManager
                  .getDefault(ItemName),
              mContext.getResources().getStringArray(
                  R.array.menu_audio_visually_speaker_array),
              MenuConfigManager.STEP_VALUE,
              Action.DataType.OPTIONVIEW));
      audioVisuallyImpaired.setmParentGroup(mAudio.mSubChildGroup);
*/
      // VISUALLY_IMPAIRED_AUDIO
      ItemName = MenuConfigManager.CFG_MENU_AUDIOINFO;
      visuallyImpairedAudioInfo = new Action(ItemName,
          mContext.getString(R.string.menu_audio_visually_impaired_audio),
          mConfigManager.getMin(ItemName), mConfigManager
              .getMax(ItemName), mConfigManager
              .getDefault(ItemName), mVisuallyHeadphone,
          MenuConfigManager.STEP_VALUE,
          Action.DataType.HAVESUBCHILD);
      visuallyImpairedAudioInfo.mSubChildGroup = new ArrayList<Action>();
//      audioVisuallyImpaired.mSubChildGroup
//          .add(visuallyImpairedAudioInfo);
//      visuallyImpairedAudioInfo.setmParentGroup(audioVisuallyImpaired.mSubChildGroup);
//      audioVisuallyImpaired.setmParentGroup(mAudio.mSubChildGroup);
      // loadVisuallyImpaired(visuallyImpairedAudioInfo);
    }
    ItemName = MenuConfigManager.AVCMODE;
    Action audioAutoVoice = new Action(ItemName,
        mContext.getString(R.string.menu_audio_avc_mode),
        MenuConfigManager.INVALID_VALUE,
        MenuConfigManager.INVALID_VALUE,
        mConfigManager.getDefault(ItemName)
            - mConfigManager.getMin(ItemName), AVC_Mode,
        MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);

    if(!"1".equals(SystemProperties.get("ro.mtk.dts.ss2.support"))) {
        mAudio.mSubChildGroup.add(audioBalance);
        mAudio.mSubChildGroup.add(audioBass);
        mAudio.mSubChildGroup.add(audioTreble);
        mAudio.mSubChildGroup.add(audioSoundSur);
        mAudio.mSubChildGroup.add(audioEqualizer);
        mAudio.mSubChildGroup.add(audioAutoVoice);
    }

    mAudio.mSubChildGroup.add(audioSpeaker);
    mAudio.mSubChildGroup.add(audioSpdifType);
    audioSpdifType.mEffectGroup = new ArrayList<Action>();
    if (mTV.isEURegion() || mTV.isCNRegion()||mTV.isSARegion()) {
      ItemName = MenuConfigManager.SPDIF_DELAY;
      Action audioSpdifDelay = new Action(ItemName,
          mContext.getString(R.string.menu_audio_spdif_delay), 0,
          250, mConfigManager.getDefault(ItemName), null, 10,
          Action.DataType.PROGRESSBAR);

      audioSpdifType.mEffectGroup.add(audioSpdifDelay);
      if (mTV.isEURegion()) {
        if (mConfigManager.getDefault(MenuConfigManager.SPDIF_MODE) == 2) {
          audioSpdifDelay.setEnabled(true);
        } else {
          audioSpdifDelay.setEnabled(false);
        }
      } else if (mTV.isCNRegion()) {
        if (mConfigManager.getDefault(MenuConfigManager.SPDIF_MODE) == 0) {
          audioSpdifDelay.setEnabled(false);
        } else {
          audioSpdifDelay.setEnabled(true);
        }
        //Begin==>add by xiechangtao for fix SA issue 76352
      }else if(mTV.isSARegion()){
        audioSpdifDelay.setEnabled(true);
      }
      mAudio.mSubChildGroup.add(audioSpdifDelay);
    }
    audioSpdifType.mSwitchHashMap = new HashMap<Integer, Boolean[]>();
    if (mTV.isCNRegion()) {
      audioSpdifType.mSwitchHashMap.put(0, new Boolean[] {
          false
      });
      audioSpdifType.mSwitchHashMap.put(1, new Boolean[] {
          true
      });
      audioSpdifType.mSwitchHashMap.put(2, new Boolean[] {
          true
      });
      audioSpdifType.mSwitchHashMap.put(3, new Boolean[] {
          true
      });
    } else {
      audioSpdifType.mSwitchHashMap.put(0, new Boolean[] {
          false
      });
      audioSpdifType.mSwitchHashMap.put(1, new Boolean[] {
          false
      });
      audioSpdifType.mSwitchHashMap.put(2, new Boolean[] {
          true
      });
    }
    //if (mTV.isCurrentSourceTv()) {
    if (! mTV.isSARegion()) {
      if (mTV.isConfigVisible(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
        if (!mTV.isCNRegion()) {
          //mAudio.mSubChildGroup.add(audioType);//modified by yx for block audioType
          // add audioVisuallyImpaired
          //mAudio.mSubChildGroup.add(audioVisuallyImpaired);//modified by yx for block audioType
        }
      }
    }
    // for EU
    //if (mTV.isUSRegion() || mTV.isEURegion()) {
      String[] downmixModeArr = mContext.getResources().getStringArray(
          R.array.menu_audio_downmixmode_array);
      if (mTV.isEURegion()) {
        downmixModeArr = mContext.getResources().getStringArray(
            R.array.menu_audio_downmixmode_eu_array);
      }
      /*ItemName = MenuConfigManager.DOWNMIX_MODE;
      Action DownmixMode = new Action(ItemName,
          mContext.getString(R.string.menu_audio_downmix_mode),
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE,
          mConfigManager.getDefault(ItemName), downmixModeArr,
          MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
      mAudio.mSubChildGroup.add(DownmixMode);*/ //by dcc
    //}
    // for EU
    /*if (mTV.isEURegion()) {
      ItemName = MenuConfigManager.SOUND_TRACKS;
      soundtrack = new Action(ItemName,
          mContext.getString(R.string.menu_audio_sound_tracks),
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE, null,
          MenuConfigManager.STEP_VALUE,
          Action.DataType.HAVESUBCHILD);
      mAudio.mSubChildGroup.add(soundtrack);
      soundtrack.mSubChildGroup = new ArrayList<Action>();
      boolean enable = mTV.isConfigEnabled(MenuConfigManager.SOUNDTRACKS_GET_ENABLE);
      soundtrack.setEnabled(enable);
      soundtrack.setmParentGroup(mAudio.mSubChildGroup);

      String[] bbemodearr = mContext.getResources().getStringArray(
          R.array.menu_audio_bbe_mode_array);
      ItemName = MenuConfigManager.CFG_AUD_AUD_BBE_MODE;
      Action bbemode = new Action(ItemName,
          mContext.getString(R.string.menu_audio_bbe_mode),
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE,
          mConfigManager.getDefault(ItemName), bbemodearr,
          MenuConfigManager.STEP_VALUE, Action.DataType.OPTIONVIEW);
      // mAudioDataGroup.add(bbemode); no need to add BBEmode in Audio

      // loadSoundTrack(soundtrack);
    }*/

    if (audioManager == null) {
      audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }
	if (audioManager.isWiredHeadsetOn()){
      audioSpeaker.setEnabled(false);
    }else{
      audioSpeaker.setEnabled(true);
    }
    if (audioManager.isHdmiSystemAudioSupported()) {
      MtkLog.e(TAG, "isHdmiSystemAudioSupported==true");
      audioBalance.setEnabled(false);
      audioBass.setEnabled(false);
      audioTreble.setEnabled(false);
      audioSoundSur.setEnabled(false);
      audioEqualizer.setEnabled(false);
      audioAutoVoice.setEnabled(false);
    } else {
      MtkLog.e(TAG, "isHdmiSystemAudioSupported==false");
      audioBalance.setEnabled(true);
      audioBass.setEnabled(true);
      audioTreble.setEnabled(true);
      if (audioManager.isWiredHeadsetOn()){
        audioSoundSur.setEnabled(false);
      } else {
      audioSoundSur.setEnabled(true);
      }
      audioEqualizer.setEnabled(true);
      audioAutoVoice.setEnabled(true);
    }
  }

  /**
   * load soundTrack data
   *
   * @param soundtrack
   */
  public int loadSoundTrack(Action soundtrack) {
    soundtrack.mSubChildGroup.clear();
    mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_INIT, 0);
    // Intent intent = new Intent(mContext,ChannelInfoActivity.class);
    // TransItem trans = new TransItem(MenuConfigManager.SOUNDTRACKS_GET_STRING,
    // "",MenuConfigManager.INVALID_VALUE,MenuConfigManager.INVALID_VALUE
    // ,MenuConfigManager.INVALID_VALUE);
    // intent.putExtra("TransItem", trans);
    // intent.putExtra("ActionID", soundtrack.mItemID);
    // soundtrack.setmIntent(intent);

    int soundListsize = mTV
        .getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_TOTAL);
    for (int i = 0; i < soundListsize; i++) {
      String ItemName = MenuConfigManager.SOUNDTRACKS_GET_STRING + "_" + i;
      String soundString = mTV.getConfigString(ItemName);
      android.util.Log.d("MenuAudioActivity", "ABC soundString:" + soundString);
      String[] itemValueStrings = new String[3];
      itemValueStrings[0] = "" + (i + 1);
      if (soundString != null) {
        String[] temp = soundString.split("\\+");
        if (temp.length >= 2) {
          itemValueStrings[1] = temp[0];
          itemValueStrings[2] = temp[1];
          if ((MtkTvConfig.getInstance().getConfigValue(MtkTvConfigType.CFG_MISC_CH_LST_TYPE) > 0)
              &&
              ("qaa".equalsIgnoreCase(temp[0]) || "und".equalsIgnoreCase(temp[0]))) {
            itemValueStrings[1] = MtkTvCI.getInstance(0).getProfileISO639LangCode();

            if (temp[1].startsWith("QAA")) {
              itemValueStrings[2] = itemValueStrings[2].replace("QAA",
                  itemValueStrings[1].toUpperCase());
            }

            if (temp[1].startsWith("UND")) {
              itemValueStrings[2] = itemValueStrings[2].replace("UND",
                  itemValueStrings[1].toUpperCase());
            }

            soundString = itemValueStrings[1] + "+" + itemValueStrings[2];
            android.util.Log.d("MenuAudioActivity", "ABC soundString:" + soundString);
          }
        } else if (temp.length == 1) {
          itemValueStrings[1] = temp[0];
          itemValueStrings[2] = "";
        } else {
          itemValueStrings[1] = "";
          itemValueStrings[2] = "";
        }

      }
      Action soundtrackItem = new Action(ItemName, "" + (i + 1),
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE,
          0, new String[] {
              soundString
          },
          MenuConfigManager.STEP_VALUE,
          Action.DataType.SAVEDATA);
      soundtrack.mSubChildGroup.add(soundtrackItem);
    }
    int stIndex = mTV.getConfigValue(MenuConfigManager.SOUNDTRACKS_GET_CURRENT);
    return stIndex;
  }

  public int loadVisuallyImpaired(Action visuallyImpairedAudioInfo) {
    // mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_INIT, 0);
    //
    // Intent intent = new Intent(mContext,ChannelInfoActivity.class);
    // TransItem trans = new TransItem(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING,
    // "",MenuConfigManager.INVALID_VALUE,MenuConfigManager.INVALID_VALUE
    // ,MenuConfigManager.INVALID_VALUE);
    // intent.putExtra("TransItem", trans);
    // intent.putExtra("ActionID", visuallyImpairedAudioInfo.mItemID);
    // visuallyImpairedAudioInfo.setmIntent(intent);
    // return soundListsize > 0 ? true:false;
    visuallyImpairedAudioInfo.mSubChildGroup.clear();
    mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_INIT, 0);
    int soundListsize = mTV
        .getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_TOTAL);
    for (int i = 0; i < soundListsize; i++) {
      String ItemName = MenuConfigManager.CFG_MENU_AUDIOINFO_GET_STRING + "_" + i;
      String soundString = mTV.getConfigString(ItemName);
      MtkLog.d("MENUAudioActivity", "VisuallyImpaired:" + soundString);
      String[] itemValueStrings = new String[3];
      if (soundString != null) {
        itemValueStrings[0] = soundString;
        itemValueStrings[1] = "";
        itemValueStrings[2] = "";
      } else {
        itemValueStrings[0] = "";
        itemValueStrings[1] = "";
        itemValueStrings[2] = "";
      }
      Action soundtrackItem = new Action(ItemName, "" + (i + 1),
          MenuConfigManager.INVALID_VALUE,
          MenuConfigManager.INVALID_VALUE,
          0, new String[] {
              soundString
          },
          MenuConfigManager.STEP_VALUE,
          Action.DataType.SAVEDATA);
      visuallyImpairedAudioInfo.mSubChildGroup.add(soundtrackItem);
    }
    int viIndex = mTV.getConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_GET_CURRENT);
    return viIndex;
  }

  @Override
  protected void onDestroy() {
    mTV.removeSingleLevelCallBackListener(mSignalHandler);
    this.unregisterReceiver(mReceiver);
    super.onDestroy();
  }

  private final Handler mSignalHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      if (msg.what == TvCallbackConst.MSG_CB_SVCTX_NOTIFY) {
        TvCallbackData backData = (TvCallbackData) msg.obj;
        MtkLog.d("MenuAudioActivity", "msg.what:" + msg.what + "backParam1 ==" + backData.param1);
        switch (backData.param1) {
          case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOSS:
            if (mTV.isEURegion() && audioVisuallyImpaired != null) {
              audioVisuallyImpaired.setEnabled(false);
            }
            break;
          case SettingsUtil.SVCTX_NTFY_CODE_SIGNAL_LOCKED:
            if (mTV.isEURegion() && audioVisuallyImpaired != null) {
              audioVisuallyImpaired.setEnabled(true);
            }
            break;
        }
      }
    }
  };

  final int MSG_REFRESH_VI = 0x76;
  final int MSG_REFRESH_VI_TIMES = 5;
  int requestTimes = 0;

  private final Handler mRefreshVIHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      super.handleMessage(msg);
      Action viAction = (Action) msg.obj;
      if (msg.what == MSG_REFRESH_VI) {
        int tv = mConfigManager.getDefault(MenuConfigManager.TYPE);
        MtkLog.d(TAG, "VImsg.what:" + msg.what + "viAction.mItemID=" + viAction.mItemID + ",tv=="
            + tv);
        if (viAction.mItemID.equals(MenuConfigManager.TYPE)
            && mConfigManager.getDefault(MenuConfigManager.TYPE) == 2) {
          if (mTV.isConfigEnabled(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
            MtkLog.d(TAG, "refresh listview don't send message again");
            audioVisuallyImpaired.setEnabled(true);
            refreshListView();
            requestTimes = 0;
            mRefreshVIHandler.removeMessages(MSG_REFRESH_VI);
          } else {
            if (requestTimes < MSG_REFRESH_VI_TIMES) {
              requestTimes++;
              MtkLog.d(TAG, "try " + requestTimes + " times***");
              Message msg2 = new Message();
              msg2.what = MSG_REFRESH_VI;
              msg2.obj = viAction;
              mRefreshVIHandler.sendMessageDelayed(msg2, 300);
            } else {
              if (requestTimes >= MSG_REFRESH_VI_TIMES) {
                MtkLog.d(TAG, "try 5 times !! don't try" + ",requsetTimes==" + requestTimes);
                requestTimes = 0;
                mRefreshVIHandler.removeMessages(MSG_REFRESH_VI);
              }
            }
          }
        }
      }
    }
  };

  @Override
  public void specialOptionClick(Action currAction) {
    // when parent initial data is visually,available of down
    if (currAction.mItemID.equals(MenuConfigManager.TYPE)
        && mConfigManager.getDefault(MenuConfigManager.TYPE) == 2) {
      if (!mTV.isConfigEnabled(MenuConfigManager.CFG_MENU_AUDIO_AD_TYP)) {
        MtkLog.d(TAG, "specialOptionClick ->send msg");
        Message msg = new Message();
        msg.what = MSG_REFRESH_VI;
        msg.obj = currAction;
        mRefreshVIHandler.sendMessage(msg);
      }
    }
  }

  @Override
  public void goBack() {
    MtkLog.d(TAG, "goBack id:" + mCurrAction.mItemID);
    if (mCurrAction.mItemID.equals(MenuConfigManager.SOUND_TRACKS)) {
      MtkLog.d(TAG, "goBack set deinit for soundtracks");
      mTV.setConfigValue(MenuConfigManager.SOUNDTRACKS_SET_DEINIT, 0);
    } else if (mCurrAction.mItemID.equals(MenuConfigManager.CFG_MENU_AUDIOINFO)) {
      MtkLog.d(TAG, "goBack set deinit for visually impaired");
      mTV.setConfigValue(MenuConfigManager.CFG_MENU_AUDIOINFO_SET_DEINIT, 0);
    } else if (mCurrAction.mItemID.equals(MenuConfigManager.VISUALLY_SPEAKER)
        || mCurrAction.mItemID.equals(MenuConfigManager.VISUALLY_HEADPHONE)) {
      if (mTV.getConfigValue(MenuConfigManager.VISUALLY_SPEAKER) == 0
          && mTV.getConfigValue(MenuConfigManager.VISUALLY_HEADPHONE) == 0) {
        if (visuallyvolume != null) {
          visuallyvolume.setEnabled(false);
        }
      } else {
        if (visuallyvolume != null) {
          visuallyvolume.setEnabled(true);
        }
      }
    }
    super.goBack();
  }

}
