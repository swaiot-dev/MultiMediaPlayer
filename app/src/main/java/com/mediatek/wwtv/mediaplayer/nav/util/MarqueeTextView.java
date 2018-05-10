package com.mediatek.wwtv.mediaplayer.nav.util;


import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import android.content.res.AssetManager;
import android.graphics.Typeface;

/**
 * Created by Calow on 2016/12/13.
 */

public class MarqueeTextView extends TextView {
    public MarqueeTextView(Context context) {
        this(context, null);
		init(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
		init(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setFocusable(true);
        setFocusableInTouchMode(true);

        setSingleLine();
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
		init(context);
    }

	private void init(Context context){
		AssetManager mgr = context.getAssets();
			Typeface font = Typeface.createFromAsset (mgr,"fonts/RobotoCondensedRegular.ttf");
			setTypeface(font);
	}
	
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean focused) {
        if (focused) {
            super.onWindowFocusChanged(focused);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }
}
