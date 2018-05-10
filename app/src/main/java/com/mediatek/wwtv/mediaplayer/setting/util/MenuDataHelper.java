package com.mediatek.wwtv.mediaplayer.setting.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.wwtv.util.MtkLog;

public class MenuDataHelper {

	public static final String TAG ="MenuDataHelper";
	protected MenuConfigManager mConfigManager;
	Context mContext;
	protected static MenuDataHelper mSelf ;

	private MenuDataHelper(Context context) {
		this.mContext = context;
		mConfigManager = MenuConfigManager.getInstance(context);
//		saveV = SaveValue.getInstance(context);
//		mTV = TVContent.getInstance(mContext);
	}

	/**
	 * string to show in UI for video related options
	 */
	public static MenuDataHelper getInstance(Context context) {
		if (mSelf == null) {
			mSelf = new MenuDataHelper(context);
		}
		return mSelf;
	}

	/**
	 * get positions of data item which are effected by parent data item
	 *
	 * @return the position of data item
	 */
	public int[] getEffectGroupInitValues(Action mEffectParentAction) {
		int mEffectGroupInitValues[];
		if (mEffectParentAction != null
				&& mEffectParentAction.getmDataType() == Action.DataType.EFFECTOPTIONVIEW) {
			mEffectGroupInitValues = new int[mEffectParentAction.getmEffectGroup().size()];
			int i = 0;
			for (Action childAction : mEffectParentAction.getmEffectGroup()) {
				mEffectGroupInitValues[i] = mConfigManager
						.getDefault(childAction.getmItemId());
				i++;
			}
			return mEffectGroupInitValues;
		} else {
			throw new IllegalArgumentException(
					"type of mEffectParentAction is not DataType.EFFECTOPTIONVIEW or mEffectParentAction is null");
		}
	}

	/**
	 * deal with switchview
	 * @param mainAction
	 */
	public void dealSwitchChildGroupEnable(Action mainAction){
		List<Action> mChildGroup = mainAction.mEffectGroup;
		if (MenuConfigManager.VIDEO_3D_MODE.equals(mainAction.mItemID)) {
			ArrayList<Boolean> m3DConfigList = MenuConfigManager.getInstance(
					mContext).get3DConfig();
			mainAction.setEnabled(m3DConfigList.get(0)) ;
			MtkLog.d("OptionView", "mDataItem:"+mainAction.mItemID+"childItem.isEnable:"+mainAction.isEnabled());
			int i = 1;
			for (Action childItem : mChildGroup) {
				childItem.setEnabled(m3DConfigList.get(i)) ;
				MtkLog.d("OptionView", "childItem:"+childItem+"childItem.isEnable:"+childItem.isEnabled());
				i++;
			}
			return ;
		} else if (mainAction.mItemID.equals(MenuConfigManager.EFFECT)){//for mjc effect
            if (null != mainAction.mParent && null != mainAction.mParent.mSubChildGroup){
                List<Action> subChildGroup = mainAction.getmParent().getmSubChildGroup();
                if (subChildGroup.get(0).mInitValue == 0) {
                    subChildGroup.get(1).setEnabled(false);
                    subChildGroup.get(1).mInitValue = 0;
                    subChildGroup.get(1).setDescription(0);
//                    subChildGroup.get(2).setEnabled(false);
                } else {
                	subChildGroup.get(1).setEnabled(true);
//                	subChildGroup.get(2).setEnabled(true);
                }
            }
		}
		/*

		Map<Integer,Boolean[]>mHashMap = mainAction.getmSwitchHashMap();
		Boolean[] isEnables = mHashMap.get(mainAction.mInitValue);
		if(isEnables !=null){
			MtkLog.d("SwitchOptionView", "isEnables[0]==:"+isEnables[0]);
		}
		int i = 0;
		for (Action childItem : mChildGroup) {
			if(isEnables != null){
				childItem.setEnabled(isEnables[i++]);
				MtkLog.d(TAG, "childItem.isEnbaled:"+childItem.mItemID +"isEnable:"+childItem.isEnabled());
				//special deal PowerOnCh
				if(childItem.isEnabled() && childItem.mItemID.equals("PowerOnCh")){
					if (CommonIntegration.getInstance().getChannelLength() <= 0){
						childItem.setEnabled(false);
					}
				}

				if (MenuConfigManager.TYPE.equals(mainAction.mItemID) && mainAction.mInitValue == 2) {
				    MtkLog.d("SwitchOptionView", "SwitchOptionView mItemID:"+childItem.mItemID +"isEnable:"+childItem.isEnabled()+"mInitValue:"+mainAction.mInitValue);
				    if(TVContent.getInstance(mContext).isEURegion()){
                        if(TVContent.getInstance(mContext).iCurrentInputSourceHasSignal()){
                            childItem.setEnabled(true);
                        }else {
                            childItem.setEnabled(false);
                        }
                    }
				}
			}
		}*/
	}

	/**
	 * load tune dialog info
	 *
	 * @param flag
	 *            :load different type of dataIt
	 */
	public void loadTuneDiagInfo(Action diagAction,boolean flag) {
		/*
		if (flag) {

		} else {
			List<String> displayName = new ArrayList<String>();
			List<String> displayValue = new ArrayList<String>();
			if (CommonIntegration.getInstance().isCurrentSourceTv()
					 && !CommonIntegration.getInstance().isCurrentSourceDTV()) {
				MtkLog.d(TAG, "loadTuneDiagInfo show atv =="+CommonIntegration.getInstance().isCurrentSourceATV());
				MtkTvUtil.getInstance().tunerFacQuery(true, displayName,
						displayValue);
			} else {
				MtkLog.d(TAG, "loadTuneDiagInfo not show");
				MtkTvUtil.getInstance().tunerFacQuery(false, displayName,
						displayValue);
			}
			String ItemName = MenuConfigManager.FACTORY_TV_TUNER_DIAGNOSTIC_NOINFO;
			for (int i = 0; i < displayName.size(); i++) {
				String[] display = { displayValue.get(i) };
				Action fvEventForm = new Action(ItemName,
						displayName.get(i), MenuConfigManager.INVALID_VALUE,
						MenuConfigManager.INVALID_VALUE, 0, display,
						MenuConfigManager.STEP_VALUE,
						Action.DataType.FACTORYOPTIONVIEW);
				fvEventForm.hasRealChild = false;
				diagAction.mSubChildGroup.add(fvEventForm);
			}
            if (displayName.size() == 0) {
                String[] nullArray = mContext.getResources().getStringArray(
                        R.array.menu_setup_null_array);
                Action fvEventForm = new Action(ItemName, mContext
                        .getString(R.string.menu_factory_TV_tunerdiag_noinfo),
                        MenuConfigManager.INVALID_VALUE,
                        MenuConfigManager.INVALID_VALUE, 0, nullArray,
                        MenuConfigManager.STEP_VALUE,
                        Action.DataType.FACTORYOPTIONVIEW);
                fvEventForm.hasRealChild = false;
                diagAction.mSubChildGroup.add(fvEventForm);
            }
		}*/

	}

}
