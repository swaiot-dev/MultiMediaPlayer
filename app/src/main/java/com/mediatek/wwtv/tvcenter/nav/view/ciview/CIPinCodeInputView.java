package com.mediatek.wwtv.tvcenter.nav.view.ciview;

import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

public class CIPinCodeInputView extends View {

	Context mContext;
	private int mCurrentSelectedPosition = -1;
	private Paint mPaint;
	private int mTextSize = 30;
	boolean flag = true;
	private float mWidth;
	private float mHeight;
	private float x;
	private float y;
	private String mText;
	final int PADDING = 2;
	final int XOFFUNIT = 8;

	public CIPinCodeInputView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setText(String text) {
		mText = text;
		this.postInvalidate();
	}

	public String getText() {
		return mText;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		String textStr = mText;
		MtkLog.d("CIDIALOGEDIT", "onDraw===" + mCurrentSelectedPosition + ","
				+ textStr);
		if (!TextUtils.isEmpty(textStr)) {
			mPaint = new Paint();
			mWidth = this.getWidth();
			mHeight = this.getHeight();
			x = (mWidth - mPaint.measureText(textStr)) / 2;
			y = (mHeight + mPaint.measureText(textStr, 0, 1) * 1.2f) / 2;

			mPaint.setTextSize(mTextSize);
			mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
			mPaint.setAntiAlias(true);
			FontMetrics fm = new FontMetrics();
			mPaint.getFontMetrics(fm);
			mPaint.setColor(Color.BLACK);
			mPaint.setStyle(Paint.Style.FILL);

			// drawText just because set gravity in xml so x coordinate is the
			// text's center
			// so if text length is 4 we minus UNIT*3 else minus UNIT*POS
			if (textStr.length() == 4) {
				canvas.drawText(textStr, x - XOFFUNIT * 3, y, mPaint);
			} else {
				canvas.drawText(textStr, x - XOFFUNIT
						* mCurrentSelectedPosition, y, mPaint);
			}

			if (mCurrentSelectedPosition != -1) {
				mPaint.setColor(Color.YELLOW);
				if (mCurrentSelectedPosition == 0) {

					// when test is full and left/right to set position to 0
					// minus x offset again
					if (textStr.length() == 4) {// full
						x = x - XOFFUNIT * 3;
					}
					MtkLog.d("CIDIALOGEDIT", "pos0--x===" + x + ",y==" + y);
					String selStr = textStr.substring(mCurrentSelectedPosition,
							1);
					float offset = mPaint.measureText(selStr);
					canvas.drawRect(x, fm.top + y - PADDING, x + offset,
							fm.bottom + y + PADDING, mPaint);
					mPaint.setColor(Color.GRAY);
					canvas.drawText(selStr, (int) x, (int) y, mPaint);
				} else {
					float offset = mPaint.measureText(textStr.substring(0, 1));
					x = x
							+ mPaint.measureText(textStr.substring(0,
									mCurrentSelectedPosition));
					// drawText just because set gravity in xml so x coordinate
					// is the text's center
					// so if text length is 4 we minus UNIT*3 else minus
					// UNIT*POS
					if (textStr.length() == 4) {// full
						x = x - XOFFUNIT * 3;
					} else {
						x = x - XOFFUNIT * mCurrentSelectedPosition;
					}
					MtkLog.d("CIDIALOGEDIT", "x===" + x + ",y==" + y);
					canvas.drawRect(x, fm.top + y - PADDING, x + offset,
							fm.bottom + y + PADDING, mPaint);
					mPaint.setColor(Color.GRAY);
					canvas.drawText(textStr.substring(mCurrentSelectedPosition,
							mCurrentSelectedPosition + 1), (int) x, (int) y,
							mPaint);

				}
			}
		}

	}

	public void setCursorPos(int keyCode) {
		if (TextUtils.isEmpty(this.getText())) {
			return;
		}
		if (keyCode == KeyMap.KEYCODE_DPAD_LEFT) {
			MtkLog.d("CIDIALOGEDIT", "left cursor index:"
					+ mCurrentSelectedPosition);
			if (mCurrentSelectedPosition != 0) {
				--mCurrentSelectedPosition;
			}
			this.postInvalidate();
		} else {
			MtkLog.d("CIDIALOGEDIT", "right cursor index:"
					+ mCurrentSelectedPosition);
			if (mCurrentSelectedPosition != this.getText().length() - 1) {
				++mCurrentSelectedPosition;
			}
			this.postInvalidate();
		}
	}

	public void setCurrentSelectedPosition(int pos) {
		mCurrentSelectedPosition = pos;
	}

	public int getCurrentSelectedPosition() {
		return mCurrentSelectedPosition;
	}

}
