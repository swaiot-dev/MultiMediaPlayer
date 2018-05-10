package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.mediatek.wwtv.mediaplayer.R;

public class VisualizerView extends View {

    private int mStartColor;
    private int mEndColor;
    private int mCount;

    private static final float proportion = 0.6f;

    private Paint mPaint;
    private Paint otherPaint;
    private boolean isRepeatDraw = true;

    public VisualizerView(Context context) {
        super(context);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public VisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VisualizerView);
        mStartColor = ta.getColor(R.styleable.VisualizerView_startColor,
                context.getResources().getColor(R.color.colorDefaultStart));
        mEndColor = ta.getColor(R.styleable.VisualizerView_endColor,
                context.getResources().getColor(R.color.colorDefaultEnd));
        mCount = ta.getColor(R.styleable.VisualizerView_count, 11);
        ta.recycle();
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(1f);
        mPaint.setColor(Color.BLACK);

        otherPaint = new Paint();
        otherPaint.setAntiAlias(true);
        otherPaint.setStrokeWidth(1f);
        otherPaint.setColor(Color.TRANSPARENT);
        otherPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        float availlableItemWidth = (width * proportion) / mCount;
        float blackItemWidth = (width * (1 - proportion)) / (mCount + 1);

        //        canvas.drawColor(Color.CYAN);

        int count = mCount;
        for (int i = 0; i < count; i++) {
            float left = availlableItemWidth * i + blackItemWidth * (i + 1);
            float right = availlableItemWidth * (i + 1) + blackItemWidth * (i + 1);
            LinearGradient lg = new LinearGradient(left, 0,
                    right, height, mEndColor, mStartColor, Shader.TileMode.REPEAT);
            mPaint.setShader(lg);
            canvas.drawRect(left, 0, right, height, mPaint);
            float randomItemHeight = (float) (Math.random() * height);
            canvas.drawRect(left, 0, right, randomItemHeight, otherPaint);
        }

        if (isRepeatDraw) {
            postInvalidateDelayed(500);
        }

    }

    public boolean isDraw(){
        return isRepeatDraw;
    }

    public void start() {
        isRepeatDraw = true;
        postInvalidate();
    }

    public void stop() {
        isRepeatDraw = false;
    }
}
