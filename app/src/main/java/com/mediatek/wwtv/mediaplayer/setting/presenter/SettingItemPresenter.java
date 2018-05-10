package com.mediatek.wwtv.mediaplayer.setting.presenter;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.setting.widget.view.CircleImageView;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingItemPresenter extends Presenter{
	
	private static class SettingItemViewHolder extends ViewHolder {
        public final ImageView mIconView;
//        public final ImageView mCircleView;
//        public final CircleImageView mCircleView;
//        public final RelativeLayout mLayout;
        public final TextView mTitleView;
//        public final TextView mDescriptionView;

        SettingItemViewHolder(View v) {
            super(v);
            mIconView = (ImageView) v.findViewById(R.id.icon);
//            mCircleView = (CircleImageView) v.findViewById(R.id.circle);
//            mLayout = (RelativeLayout)v.findViewById(R.id.item_layout);
            mTitleView = (TextView) v.findViewById(R.id.title);
//            mDescriptionView = (TextView) v.findViewById(R.id.description);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.browse_item, parent, false);
        return new SettingItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        if (item instanceof SettingItem && viewHolder instanceof SettingItemViewHolder) {
            final SettingItem menuItem = (SettingItem) item;
            SettingItemViewHolder mItemViewHolder = (SettingItemViewHolder) viewHolder;

//            prepareTextView(mItemViewHolder.mTitleView, menuItem.getmTitle());
           

//            Resources res = mItemViewHolder.mTitleView.getContext().getResources();
//            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams)
//            		mItemViewHolder.mTitleView.getLayoutParams();
//            boolean hasDescription = false;
//            if (hasDescription) {
//                lp.bottomMargin = (int) res.getDimension(R.dimen.browse_item_title_marginBottom);
//                mItemViewHolder.mTitleView.setSingleLine(true);
//                mItemViewHolder.mTitleView.setLines(1);
//            } else {
//                lp.bottomMargin = (int) res.getDimension(R.dimen.browse_item_description_marginBottom);
//                mItemViewHolder.mTitleView.setSingleLine(false);
//                mItemViewHolder.mTitleView.setLines(2);
//            }
//            mItemViewHolder.mTitleView.setLayoutParams(lp);

//            viewHolder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if (v != null && menuItem.getIntent() != null) {
//                        ((Activity) v.getContext()).startActivity(menuItem.getIntent());
//                    }
//                }
//            });

            prepareImageView(mItemViewHolder, menuItem.getmIcon(),menuItem.isCircle(),menuItem.getmTitle());
        }
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        if (viewHolder instanceof SettingItemViewHolder) {
        	SettingItemViewHolder menuItemViewHolder = (SettingItemViewHolder) viewHolder;
            menuItemViewHolder.mIconView.setImageBitmap(null);
//            menuItemViewHolder.mCircleView.setImageBitmap(null);
        }
    }

    private void prepareImageView(final SettingItemViewHolder menuItemViewHolder,Drawable d,boolean isCircle,String description) {
//        menuItemViewHolder.mIconView.setVisibility(View.INVISIBLE);
//    	Log.i("prepareImageView","isCircle:"+isCircle);
//    	if(isCircle){
//    		menuItemViewHolder.mIconView.setVisibility(View.GONE);
//      		menuItemViewHolder.mCircleView.setVisibility(View.VISIBLE);
//	        LayoutParams lp = menuItemViewHolder.mCircleView.getLayoutParams();
//	        menuItemViewHolder.mCircleView.setImageDrawable(d);
//	        fadeIn(menuItemViewHolder.mCircleView);
//    	}else{
//    		menuItemViewHolder.mCircleView.setVisibility(View.GONE);
    		menuItemViewHolder.mIconView.setVisibility(View.VISIBLE);
	        LayoutParams lp = menuItemViewHolder.mIconView.getLayoutParams();
    		menuItemViewHolder.mIconView.setImageDrawable(d);//.setBackgroundDrawable(d);
            menuItemViewHolder.mIconView.setContentDescription(description);
            //SKY luojie 20180110 modify for UI begin
            menuItemViewHolder.mTitleView.setText(description);
            //SKY luojie 20180110 modify for UI end
	        //fadeIn(menuItemViewHolder.mIconView);
//    	}
    }

    private void fadeIn(View v) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(v, "alpha", 1f);
        alphaAnimator.setDuration(v.getContext().getResources().getInteger(
                android.R.integer.config_shortAnimTime));
        alphaAnimator.start();
    }

    private boolean prepareTextView(TextView textView, String title) {
        boolean hasTextView = !TextUtils.isEmpty(title);
        if (hasTextView) {
            textView.setText(title);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
        return hasTextView;
    }
}
