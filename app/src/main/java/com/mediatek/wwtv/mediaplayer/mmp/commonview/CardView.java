package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;

/**
 * Author SKY205711
 * Date   2017/12/12
 * Description: This is CardView
 */
public class CardView extends LinearLayout {
    private ImageView mImageView;
    private TextView mTextView;

    public CardView(Context context) {
        super(context);
        buildImageCardView();
    }

    public CardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        buildImageCardView();
    }

    public CardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        buildImageCardView();
    }

    private void buildImageCardView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.mmp_item_file, this);

        mImageView =(ImageView) findViewById(R.id.img_preview);
        mTextView = (TextView) findViewById(R.id.text_title);
    }

    public void setImageDrawble(Drawable drawble) {
        if(mImageView != null && drawble != null) {
            mImageView.setImageDrawable(drawble);
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        if(mImageView != null && bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
    }

    public void setTextTitle(String title) {
        if(mTextView != null && title != null) {
            mTextView.setText(title);
        }
    }

    public ImageView getImageView() {
        return mImageView;
    }

    public TextView getTextView() {
        return mTextView;
    }
}
