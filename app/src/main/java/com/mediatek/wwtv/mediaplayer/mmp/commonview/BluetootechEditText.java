package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import java.lang.reflect.Field;

public class BluetootechEditText extends EditText {
    public BluetootechEditText(Context context) {
        super(context);
    }

    public BluetootechEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BluetootechEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public BluetootechEditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Class<? extends KeyEvent> eventClass = event.getClass();
        try {
            Field mDeviceId = eventClass.getDeclaredField("mDeviceId");
            mDeviceId.setAccessible(true);
            mDeviceId.set(event, 2);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        event.setSource(0x301);
        return super.dispatchKeyEvent(event);
    }
}
