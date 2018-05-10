/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.mediaplayer.setting.base;

import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.DynamicOptionFrag;
import com.mediatek.wwtv.mediaplayer.setting.commonfrag.ProgressBarFrag;
import com.mediatek.wwtv.mediaplayer.setting.util.CommonIntegration;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuDataHelper;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.setting.util.SettingsUtil;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionAdapter;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ActionFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.ContentFragment;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.DialogActivity;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl.Thumbnail;
import com.mediatek.wwtv.mediaplayer.mmpcm.audioimpl.CorverPic;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class BaseSettingsActivity extends DialogActivity {
	private final String TAG ="BaseSettings";
    protected Object mState;
    protected Stack<Object> mStateStack = new Stack<Object>();
    protected Stack<Action> mActionLevelStack = new Stack<Action>();
    protected Resources mResources;
    protected Fragment mContentFragment;
    protected Fragment mActionFragment;
    protected ArrayList<Action> mActions;
    protected Action mCurrAction ;
    protected Action mParentAction ;
    protected MenuDataHelper mDataHelper;
    protected MenuConfigManager mConfigManager;
    protected TVContent mTV;
    protected CommonIntegration mIntegration;
    protected SpecialOptionDealer mSpecialOptionDealer;
    //below is for exo volume
    private SharedPreferences mSharedPreferences;
    private Editor mEditor;
    private static final String EXO_PLAYER_PRESERENCE = "exo_player";
    private static final String IS_EXO_ON = "is_exo_on";

    /**
     * This method initializes the parameter and sets the initial state. <br/>
     * Activities extending {@link BaseSettingsActivity} should initialize their
     * own local variables, if any, before calling {@link #onCreate}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mResources = getResources();
        mActions = new ArrayList<Action>();
        super.onCreate(savedInstanceState);
        mDataHelper= MenuDataHelper.getInstance(this);
        mConfigManager = MenuConfigManager.getInstance(this);
        mTV = TVContent.getInstance(this);
        mIntegration = CommonIntegration.getInstanceWithContext(this);
        setState(getInitialDataType(), true);
        if (mSharedPreferences == null || mEditor == null) {
          try {
            this.createPackageContext(getApplicationContext().getPackageName(), Context.CONTEXT_IGNORE_SECURITY);
            mSharedPreferences = getSharedPreferences(EXO_PLAYER_PRESERENCE,Context.MODE_WORLD_WRITEABLE);
            mEditor = mSharedPreferences.edit();
          } catch (Exception e) {
            MtkLog.d(TAG, "createPackageContext Exception mSharedPreferences>>" + mSharedPreferences);
          }
        }
    }

    protected abstract Object getInitialDataType();

    protected void setState(Object state, boolean updateStateStack) {
        if (updateStateStack && mState != null) {
            mStateStack.push(mState);
        }
        mState = state;

        updateView();
    }

    protected void setView(int titleResId, int breadcrumbResId, int descResId, int iconResId) {
        String title = titleResId != 0 ? mResources.getString(titleResId) : null;
        String breadcrumb = breadcrumbResId != 0 ? mResources.getString(breadcrumbResId) : null;
        String description = descResId != 0 ? mResources.getString(descResId) : null;
        setView(title, breadcrumb, description, iconResId);
    }

    protected void setView(String title, String breadcrumb, String description, int iconResId) {
        mContentFragment = ContentFragment.newInstance(title, breadcrumb, description, iconResId,
                getResources().getColor(R.color.icon_background));
        mActionFragment = ActionFragment.newInstance(mActions);
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    /**
     * Set the view.
     *
     * @param uri Uri of icon resource.
     */
    protected void setView(String title, String breadcrumb, String description, Uri uri) {
        mContentFragment = ContentFragment.newInstance(title, breadcrumb, null, uri,
                getResources().getColor(R.color.icon_background));
        mActionFragment = ActionFragment.newInstance(mActions);
        setContentAndActionFragments(mContentFragment, mActionFragment);
    }

    protected void setView(int titleResId, String breadcrumb, int descResId, Uri uri) {
        String title = titleResId != 0 ? mResources.getString(titleResId) : null;
        String description = descResId != 0 ? mResources.getString(descResId) : null;
        setView(title, breadcrumb, description, uri);
    }

    /**
     * This method is called by {@link #setState}, and updates the layout based
     * on the current state
     */
    protected abstract void updateView();

    /**
     * This method is called to update the contents of mActions to reflect the
     * list of actions for the current state.
     */
    protected void refreshActionList(){
    	mActions.clear();
    	switch((Action.DataType)mState){
    	case TOPVIEW:
    		setActionsForTopView();
    		break;
    	case OPTIONVIEW:
    	case SWICHOPTIONVIEW:
    	case EFFECTOPTIONVIEW:
    		MtkLog.d("BaseSettings", "mCurrAction.mOptionValue.length=="+mCurrAction.mOptionValue.length);
    		for(int i=0 ;i<mCurrAction.mOptionValue.length;i++){
    			String newId = mCurrAction.mItemID+SettingsUtil.OPTIONSPLITER+i;
    			MtkLog.d("BaseSettings", "mCurrAction.mOptionValue[:"+i+"]:"+mCurrAction.mOptionValue[i]);
    			Action option = new Action(newId,mCurrAction.mOptionValue[i],
    					MenuConfigManager.INVALID_VALUE,
    					MenuConfigManager.INVALID_VALUE,
    					MenuConfigManager.INVALID_VALUE, null,
    					MenuConfigManager.STEP_VALUE, Action.DataType.LASTVIEW);
    			if(i == mCurrAction.mInitValue){
    				option.setmChecked(true);
    			}
    			mActions.add(option);
    		}
    		break;
    	case HAVESUBCHILD:
    		MtkLog.d("BaseSettings", "mCurrAction.mSubChild.length=="+mCurrAction.mSubChildGroup.size());
    		mActions.addAll(mCurrAction.mSubChildGroup);
    		break;
    	case PROGRESSBAR:
    	case POSITIONVIEW:
    		break;
    	case LASTVIEW:
    		break;
    	default:
    		break;
    	}
    }

    /**
     * generate item for top view
     */
    protected abstract void setActionsForTopView();

    @Override
	public void onActionClicked(Action action) {
    	MtkLog.i(TAG,"onActionClicked:"+action.mDataType+"--mCurrAction.mDataType:"+mCurrAction.mDataType);
    	if(action.mDataType != Action.DataType.LASTVIEW
    			&&action.mDataType != Action.DataType.SCANROOTVIEW
    			&&action.mDataType != Action.DataType.DIALOGPOP
    			&&action.hasRealChild){
			if (action.mItemID.startsWith(MenuConfigManager.SOUNDTRACKS_GET_STRING)){
                goBack();
            } else {
        		mParentAction = mCurrAction;
        		mCurrAction = action;
        		mActionLevelStack.push(mParentAction);
        		MtkLog.d("BaseSettings", "action.mDataType=="+action.mItemID+","+action.mDataType);
        		setState(action.mDataType,true);
            }
    	}else{//this is for lastView
    		if(mCurrAction.mDataType == Action.DataType.OPTIONVIEW
    			||mCurrAction.mDataType == Action.DataType.EFFECTOPTIONVIEW
    			||mCurrAction.mDataType == Action.DataType.SWICHOPTIONVIEW){
    			String[] idValue = SettingsUtil.getRealIdAndValue(action.mItemID);
    			if(idValue != null){
    				try{
    					MtkLog.d("BaseSettings", "idValue[0]=="+idValue[0]+","+"idValue[1]:"+idValue[1]);
    					int value =Integer.parseInt(idValue[1]);
    					mCurrAction.mInitValue = value;
    					mCurrAction.setDescription(value);
    					int preExoValue = mConfigManager
    					    .getValueFromPrefer(MenuConfigManager.EXO_PLAYER_SWITCHER);
    					mConfigManager.setValue(idValue[0], value, mCurrAction);
    					int newExoValue = mConfigManager
    					    .getValueFromPrefer(MenuConfigManager.EXO_PLAYER_SWITCHER);
    					if ((preExoValue == 0  && newExoValue == 1)
    					    || (preExoValue == 1  && newExoValue == 0)) {
    					  LogicManager.getInstance(getApplicationContext()).stopAudio();
    					  LogicManager.getInstance(getApplicationContext()).clearAudio();
    					  if (Util.mIsEnterPip && VideoPlayActivity.getInstance() != null) {
    					    VideoPlayActivity.getInstance().finish();
    					  }
    					}
              boolean isExo = false;
              if (newExoValue == 1) {
                isExo = true;
              }
              if (mEditor != null && mSharedPreferences != null) {
                mEditor.putBoolean(IS_EXO_ON, isExo);
                mEditor.commit();
                Thumbnail thumbnail = Thumbnail.getInstance();
                thumbnail.resetHandlerSource();
                CorverPic vCorver = CorverPic.getInstance();
                vCorver.resetSrcType();
                if (isExo){
                    mTV.setConfigValue(MenuConfigManager.EXO_PLAYER_SWITCH_NATIVE, 1);
                    Util.reset3D(this);
                } else {
                    mTV.setConfigValue(MenuConfigManager.EXO_PLAYER_SWITCH_NATIVE, 0);
                }
                MtkLog.d(TAG, "is_exo>>newExoValue>" + newExoValue + "  get:"
                    + mSharedPreferences.getBoolean(IS_EXO_ON, false));
              }

                        if (idValue[0].equals(MenuConfigManager.NOTIFY_SWITCH) && 1 == value){//for set notification switch of dolby vision
                            MtkLog.d(TAG,"show dolby vision logo");
                            Util.showDoViToast(this);
                        }
    				}catch(Exception e){
    					e.printStackTrace();
    				}
    				if(mSpecialOptionDealer != null){
    					mSpecialOptionDealer.specialOptionClick(mCurrAction);
    				}
    			}
    			if(mCurrAction.mDataType == Action.DataType.EFFECTOPTIONVIEW){
    				dealEffectOptionValues(mCurrAction);
    			}else if(mCurrAction.mDataType == Action.DataType.SWICHOPTIONVIEW){
    				dealSwitchChildItemEnable(mCurrAction);
    			}
    			//option type view clicked call goBack
    			goBack();
    		}
    	}
		super.onActionClicked(action);
	}

    /**
     * This method is used to set boolean properties
     *
     * @param enable whether or not to enable the property
     */
    protected abstract void setProperty(boolean enable);

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	MtkLog.d(TAG, "onKeyDown");
		 if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (this.mActionFragment instanceof ProgressBarFrag) {
				MtkLog.d("yangxiong", "onKeyDowntrue");
                return true;
            }
        }
        if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (event.getRepeatCount() == 0){
            goBack();
            return true;
			}
            return false;
        }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT
        		||keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
        	if(this.mActionFragment instanceof ProgressBarFrag){
        		MtkLog.d(TAG, "I can do progress");
        		ProgressBarFrag frag = (ProgressBarFrag)mActionFragment;
        		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
        			frag.onKeyLeft();
        		}else{
        			frag.onKeyRight();
        		}
        		return true;
        	}else if(this.mActionFragment instanceof DynamicOptionFrag){
        		MtkLog.d(TAG, "I can do dynamic option");
        		DynamicOptionFrag frag = (DynamicOptionFrag)mActionFragment;
        		if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
        			frag.onKeyLeft();
        		}else{
        			frag.onKeyRight();
        		}
        		return true;
        	}
        	else{
        		MtkLog.d(TAG, "I can do not");
        	}
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void goBack(){
        if (mState.equals(getInitialDataType())) {
            finish();
        } else if (getPrevState() != null) {
            mState = mStateStack.pop();
            mCurrAction = mActionLevelStack.pop();
            MtkLog.d(TAG, "curr action is== "+mCurrAction.mDataType);
            mParentAction = getPrevAction();
            MtkLog.d(TAG, "back prevaction is null== "+(mParentAction == null));
            // Using the synchronous version of popBackStack so that we can get
            // the updated
            // instance of the action Fragment on the following line.
            getFragmentManager().popBackStackImmediate();
            mActionFragment = getActionFragment();
            // Update Current State Actions
            if ((mActionFragment != null) && (mActionFragment instanceof ActionFragment)) {
            	MtkLog.d(TAG, "action frag");
            	ActionFragment actFrag = (ActionFragment) mActionFragment;
                refreshActionList();
                ((ActionAdapter) actFrag.getAdapter()).setActions(mActions);
            }else{
            	MtkLog.d(TAG, "which frag");
            }
        }
    }



    protected Object getPrevState() {
        return mStateStack.isEmpty() ? null : mStateStack.peek();
    }

    protected Action getPrevAction() {
        return mActionLevelStack.isEmpty() ? null : mActionLevelStack.peek();
    }

    public void dealEffectOptionValues(Action mainAction){
		List<Action> mEeffectGroup = mainAction.getmEffectGroup();
		int i = 0;
		int initValues[] = mDataHelper.getEffectGroupInitValues(mainAction);
		if ((mActionFragment != null) && (mActionFragment instanceof ActionFragment)) {
			ActionFragment actFrag = (ActionFragment) mActionFragment;
			for (; i < mEeffectGroup.size(); i++) {
				Action effectChildData = mEeffectGroup.get(i);
				MtkLog.d(TAG, "itemId,initValues[i]=="+effectChildData.mItemID+","+initValues[i]);
				effectChildData.setmInitValue(initValues[i]);
				effectChildData.setDescription(initValues[i]);
			}
			((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
		}
	}
    public void dealSwitchChildItemEnable(Action mainAction){
    	mDataHelper.dealSwitchChildGroupEnable(mainAction);
    	if ((mActionFragment != null) && (mActionFragment instanceof ActionFragment)) {
			ActionFragment actFrag = (ActionFragment) mActionFragment;
			((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
    	}
    }

    protected void refreshListView() {
        if ((mActionFragment != null) && (mActionFragment instanceof ActionFragment)) {
            MtkLog.d(TAG, "loadFinished setDatas....");
            ActionFragment actFrag = (ActionFragment) mActionFragment;
            ((ActionAdapter) actFrag.getAdapter()).notifyDataSetChanged();
        }
    }
}
