package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.util.MtkLog;

/*
 * adapter for CI list
 */

public class CIListAdapter extends BaseAdapter {
	private static final String TAG = "CIListAdapter";
	String[] mData; // data
	Context mContext;
	ViewHolder mViewHolder;

	public CIListAdapter(Context context) {
		mContext = context;
	}

	public CIListAdapter(Context context, String[] data) {
		mContext = context;
		mData = data;
	}

	public int getCount() {
		if (mData == null) {
			return 0;
		}
		return mData.length;
	}

	public Object getItem(int position) {
		return mData == null ? null : mData[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		MtkLog.d(TAG, "getView-->"+position);
		if (convertView == null) {
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.menu_ci_main_item, null);

			// ViewGroup.LayoutParams params = convertView.getLayoutParams();
			// if (params == null) {
			// params = new AbsListView.LayoutParams(
			// LayoutParams.MATCH_PARENT, 38);
			// } else {
			// params.height = 38;
			// }
			// convertView.setLayoutParams(params);
			mViewHolder.mTextView = (TextView) convertView
					.findViewById(R.id.menu_ci_main_item);
			convertView.setTag(mViewHolder);
		} else {
			mViewHolder = (ViewHolder) convertView.getTag();
		}
		if (mData != null && mData.length > 0) {
			if (mData[position] != null && mData[position].length()> 0) {
				MtkLog.d(TAG, "setText:"+mData[position].trim());
				mViewHolder.mTextView.setText(mData[position].trim());
			}
		}
		return convertView;
	}

	class ViewHolder {
		TextView mTextView;
	}

	public String[] getCIData() {
		return mData;
	}

	public void setCIGroup(String[] data) {
		MtkLog.d(TAG, "setCIGroup");
		this.mData = data;
	}

}
