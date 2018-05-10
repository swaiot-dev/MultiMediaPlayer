package com.mediatek.wwtv.mediaplayer.setting.commonfrag;

import java.util.List;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.util.MenuConfigManager;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action;
import com.mediatek.wwtv.mediaplayer.setting.widget.detailui.Action.DataType;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

public class ProgressBarFrag extends Fragment{
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
    private ProgressBar mProgressView;
    private SeekBar mSeekBarView;
    private TextView mValueView;

    private int mPostion;
	private int mOffset;
	private TVContent mTvContent;
	private MenuConfigManager mConfigManager;
	private String mId;
	boolean isPositionView = false ;
	/*
	 * when press LEFT or RIGHT, the decrease or increase value every time
	 * default value is 1
	 */
	private int mStepValue;
	private int listPosition;

	public void setAction(Action action){
		mAction = action;
		if(action.mDataType == DataType.POSITIONVIEW){
			isPositionView = true;
		}
	}


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	mRootView = (ViewGroup)inflater.inflate(R.layout.progress_frag, null);
    	mProgressView =(ProgressBar) mRootView.findViewById(R.id.progress_view);
    	mSeekBarView = (SeekBar)mRootView.findViewById(R.id.seekbar_view);
    	mValueView = (TextView) mRootView.findViewById(R.id.progress_value);
    	if(isPositionView){
    		mSeekBarView.setVisibility(View.VISIBLE);
    		mProgressView.setVisibility(View.GONE);
    	}
    	bindData();
    	return mRootView;
    }

    private void bindData(){
    	mTvContent = TVContent.getInstance(getActivity());
    	mConfigManager = MenuConfigManager.getInstance(getActivity());
    	mId = mAction.mItemID;
    	mOffset = -mAction.getmStartValue();
    	if(!isPositionView){
    		mProgressView.setMax(mAction.getmEndValue()
    				- mAction.getmStartValue());
    	}else{
    		mSeekBarView.setMax(mAction.getmEndValue()
    				- mAction.getmStartValue());
    	}

	mPostion = mAction.getmInitValue() + mOffset;
	mStepValue = mAction.getmStepValue();
	setProgressAndValue(mPostion,false);
    }

    public void setValue(int mInitValue) {
	mPostion = mInitValue + mOffset;
	mAction.mInitValue = mInitValue;
	showValue(mAction.mInitValue);
	mConfigManager.setActionValue(mAction);
   }

	public void showValue(int value) {
		mValueView.setText("" + value);
		if (MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_LEVEL.equals(mAction.mItemID)
				|| MenuConfigManager.TV_SINGLE_SCAN_SIGNAL_QUALITY.equals(mAction.mItemID)
				|| MenuConfigManager.DVBS_SIGNAL_QULITY.equals(mAction.mItemID)
				|| MenuConfigManager.DVBS_SIGNAL_LEVEL.equals(mAction.mItemID)) {
			if (mTvContent.isEURegion()) {
				mValueView.setText(value + "%");
			} else {
				mValueView.setText("" + value);
			}
		}
		mAction.mInitValue = value;
		mPostion = value + mOffset;
		if(!isPositionView){
			mProgressView.setProgress(mPostion);
    	}else{
    		mSeekBarView.setProgress(mPostion);
    	}

		mAction.setDescription(value);
	}


	private void setProgressAndValue(int postion ,boolean fromUser){
//		mProgressView.setProgress(mPostion);
		if(!fromUser){
			showValue(mAction.mInitValue);
		}else{
			showValue(mAction.mInitValue);
		}
	}

	public void onKeyLeft() {
		switchValuePrevious();
	}

	public void onKeyRight() {
		switchValueNext();
	}

    private void switchValuePrevious() {
		resetColorTempUser();
		MtkLog.d("ProgressView", "switchValuePrevious mPostion:" + mPostion);
		if (mPostion > 0) {
			MtkLog.d("ProgressView", "switchValuePrevious mAction.mInitValue >>" + mAction.mInitValue + " >>  " + mAction.mItemID
					+ "   " + MenuConfigManager.VPOSITION);
			if (mAction.mItemID.equals(MenuConfigManager.VPOSITION)) {
				int ret = setVPositionPrevious(mAction.mInitValue);
				setProgressAndValue(ret,true);
			}else{
				mPostion = mPostion - mStepValue;
				mAction.mInitValue = mPostion - mOffset;
				MtkLog.d("ProgressFrag", "mAction.mInitValue=="+mAction.mInitValue);
				setProgressAndValue(mAction.mInitValue,true);
				mConfigManager.setActionValue(mAction);
			}

		}
	}

	private void switchValueNext() {
		resetColorTempUser();
		int max = -1;
		if(!isPositionView){
			max = mProgressView.getMax();
		}else{
			max = mSeekBarView.getMax();
		}
		MtkLog.d("switchValueNext","getMax():" + max
				+ "----mPostion:" + mPostion);
		if (mPostion < max) {

			if (mAction.mItemID.equals(MenuConfigManager.VPOSITION)) {
				int ret = setVPositionNext(mAction.mInitValue);
				setProgressAndValue(ret,true);
			}else{
				mPostion = mPostion + mStepValue;
				mAction.mInitValue = mPostion - mOffset;
				MtkLog.d("ProgressFrag", "mAction.mInitValue=="+mAction.mInitValue);
				setProgressAndValue(mAction.mInitValue,true);
				mConfigManager.setActionValue(mAction);
			}

		}
	}

    private void resetColorTempUser() {
		if (mAction.mItemID.equals(MenuConfigManager.COLOR_G_R)
				|| mAction.mItemID.equals(MenuConfigManager.COLOR_G_G)
				|| mAction.mItemID.equals(MenuConfigManager.COLOR_G_B)) {
			if (mAction.mParentGroup.size() < 6) {// factory mode have six
													// data item
				mConfigManager.setValue(MenuConfigManager.COLOR_TEMPERATURE, 0,
						mAction);
			}
		}
	}

	public void removeCallback(){
	}

	private int setVPositionNext(int value){
		value ++;
		do{
			mConfigManager.setActionValue(mAction);
			int newValue = MenuConfigManager.getInstance(getActivity()).getDefault(MenuConfigManager.VPOSITION);
			if(newValue >= value){
				value = newValue;
				MtkLog.d("Next","biaoqing Next newValue=="+newValue+",bigger value=="+value);
				break;
			}else{
				value++;
				if(value >=100){
					break;
				}
			}
		}while(true);
		return value;
	}


	private int setVPositionPrevious(int value){
		value--;
		do{
			mConfigManager.setActionValue(mAction);
			int newValue = MenuConfigManager.getInstance(getActivity()).getDefault(MenuConfigManager.VPOSITION);
			if(newValue <= value){
				value = newValue;
				MtkLog.d("Previous","biaoqing newValue=="+newValue);
				break;
			}else{
				value--;
				if(value >100){
					break;
				}
			}
		}while(true);
		return value;
	}


}
