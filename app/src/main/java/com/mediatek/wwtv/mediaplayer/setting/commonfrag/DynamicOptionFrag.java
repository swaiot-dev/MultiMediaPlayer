package com.mediatek.wwtv.mediaplayer.setting.commonfrag;

import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuDataHelper;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;

/**
 * this fragment is for 3D_MODE
 * because VIDEO_3D_MODE 's option is dynamically
 * @author sin_biaoqinggao
 *
 */
public class DynamicOptionFrag extends Fragment{
	public static final String TAG = "DynamicOptionFrag";
	 /**
     * Object listening for adapter events.
     */
    public interface ResultListener {
        void onCommitResult(List<String> result);
    }
    private Action mAction;
    private Context mContext;
    private ResultListener mResultListener;
    private ViewGroup mRootView;
    private TextView mNameView;
    private TextView mValueView;
    private TVContent mTvContent;
	private MenuConfigManager mConfigManager;
	private MenuDataHelper mDataHelper;
	private String mId;

	public void setAction(Action action){
		mAction = action;
		mId = mAction.mItemID;
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup)inflater.inflate(R.layout.dynamic_option_frag, null);
		mNameView = (TextView)mRootView.findViewById(R.id.option_title);
		mValueView = (TextView)mRootView.findViewById(R.id.option_value);
		bindData();
		return mRootView;
	}

	private void bindData(){
	    mTvContent = TVContent.getInstance(getActivity());
	    mConfigManager = MenuConfigManager.getInstance(getActivity());
	    mDataHelper = MenuDataHelper.getInstance(getActivity());
	    mNameView.setText(mAction.getTitle());
	    mValueView.setText(mAction.mOptionValue[mAction.mInitValue]);
	}

	public void onKeyLeft() {
		switchValuePrevious();
	}

	public void onKeyRight() {
		switchValueNext();
	}

	private void switchValueNext() {
		if (MenuConfigManager.VIDEO_3D_MODE.equals(mId)) {
			int nextIndex = (mAction.mInitValue + 1) % 11;
			MtkLog.d(TAG, " biaoqinggao: before idx ==" + nextIndex);
			mTvContent.setConfigValue(MenuConfigManager.VIDEO_3D_MODE,
					nextIndex);
			nextIndex = mTvContent
					.getConfigValue(MenuConfigManager.VIDEO_3D_MODE);
			MtkLog.d(TAG, " biaoqinggao:after idx" + nextIndex);
			mAction.mInitValue = nextIndex;

			switch (mAction.mInitValue) {
			case MtkTvConfigTypeBase.CFG_3D_MODE_2D_TO_3D:
			case MtkTvConfigTypeBase.CFG_3D_MODE_TOP_AND_BTM:
			case MtkTvConfigTypeBase.CFG_3D_MODE_SIDE_SIDE:
				MenuConfigManager.getInstance(mContext).toastWearGlass();
				break;
			default:
				break;
			}
			mValueView.setText(mAction.mOptionValue[mAction.mInitValue]);
			mAction.setDescription(mAction.mInitValue);
			//deal with switch child option
			mDataHelper.dealSwitchChildGroupEnable(mAction);
		}
	}

	private void switchValuePrevious() {
		if (MenuConfigManager.VIDEO_3D_MODE.equals(mId)) {
			int nextIndex = (mAction.mInitValue - 1+11) % 11;
			MtkLog.d(TAG, " biaoqinggao: before idx ==" + nextIndex);
			mTvContent.setConfigValue(MenuConfigManager.VIDEO_3D_MODE,
					nextIndex);
			nextIndex = mTvContent
					.getConfigValue(MenuConfigManager.VIDEO_3D_MODE);
			MtkLog.d(TAG, " biaoqinggao:after idx" + nextIndex);
			mAction.mInitValue = nextIndex;

			switch (mAction.mInitValue) {
			case MtkTvConfigTypeBase.CFG_3D_MODE_2D_TO_3D:
			case MtkTvConfigTypeBase.CFG_3D_MODE_TOP_AND_BTM:
			case MtkTvConfigTypeBase.CFG_3D_MODE_SIDE_SIDE:
				MenuConfigManager.getInstance(mContext).toastWearGlass();
				break;
			default:
				break;
			}
			mValueView.setText(mAction.mOptionValue[mAction.mInitValue]);
			mAction.setDescription(mAction.mInitValue);
			//deal with switch child option
			mDataHelper.dealSwitchChildGroupEnable(mAction);
		}
	}

}
