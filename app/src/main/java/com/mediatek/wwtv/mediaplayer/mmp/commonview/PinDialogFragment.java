/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.OverScroller;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;
import com.mediatek.twoworlds.tv.common.MtkTvConfigTypeBase;
import com.mediatek.twoworlds.tv.MtkTvConfig;
import com.mediatek.twoworlds.tv.MtkTvPWDDialog;

public class PinDialogFragment extends Fragment {
	private static final String TAG = "PinDialogFragment";
	private static boolean DEBUG = true;

	private Context context;

	/**
	 * PIN code dialog for unlock channel
	 */
	public static final int PIN_DIALOG_TYPE_UNLOCK_CHANNEL = 0;

	/**
	 * PIN code dialog for unlock content. Only difference between
	 * {@code PIN_DIALOG_TYPE_UNLOCK_CHANNEL} is it's title.
	 */
	public static final int PIN_DIALOG_TYPE_UNLOCK_PROGRAM = 1;

	/**
	 * PIN code dialog for change parental control settings
	 */
	public static final int PIN_DIALOG_TYPE_ENTER_PIN = 2;

	/**
	 * PIN code dialog for set new PIN
	 */
	public static final int PIN_DIALOG_TYPE_NEW_PIN = 3;

	// PIN code dialog for checking old PIN. This is intenal only.
	private static final int PIN_DIALOG_TYPE_OLD_PIN = 4;

	private static final int PIN_DIALOG_RESULT_SUCCESS = 0;
	private static final int PIN_DIALOG_RESULT_FAIL = 1;

	private static final int MAX_WRONG_PIN_COUNT = 5;
	private static final int DISABLE_PIN_DURATION_MILLIS = 60 * 1000; // 1
																		// minute

	public interface ResultListener {
		void done(String pinCode);
		void cancel();
	}

	private static final int NUMBER_PICKERS_RES_ID[] = { R.id.first,
			R.id.second, R.id.third, R.id.fourth };

	private ResultListener mListener;
	private int mRetCode;

	private PinNumberPicker[] mPickers;
	private String mPrevPin;
	private int mWrongPinCount;
	private final Handler mHandler = new Handler();

	public boolean isPinCorrect(String pin) {
		return false;
	}

	public boolean isPinSet() {
		return false;
	}

	static boolean isPinCode = false;

	public void setPinType(boolean isPin) {
		isPinCode = isPin;
	}

	// public PinDialogFragment(int type, ResultListener listener, Context
	// context) {
	// mListener = listener;
	// mRetCode = PIN_DIALOG_RESULT_FAIL;
	// this.context = context;
	// PinNumberPicker.loadResources(context);
	// }

	public PinDialogFragment() {
		super();
	}

	public void setResultListener(ResultListener listener) {
		this.mListener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		MtkLog.d(TAG, "onCreateView");
		final View v = inflater.inflate(R.layout.ci_pin_code_fragment,
				container, false);
		PinNumberPicker.loadResources((Context) this.getActivity());
		mPickers = new PinNumberPicker[NUMBER_PICKERS_RES_ID.length];
		for (int i = 0; i < NUMBER_PICKERS_RES_ID.length; i++) {
			mPickers[i] = (PinNumberPicker) v
					.findViewById(NUMBER_PICKERS_RES_ID[i]);
			mPickers[i].setValueRange(0, 9);
			mPickers[i].setPinDialogFragment(this);
			mPickers[i].updateFocus();
		}
		for (int i = 0; i < NUMBER_PICKERS_RES_ID.length - 1; i++) {
			mPickers[i].setNextNumberPicker(mPickers[i + 1]);
		}
		mPickers[0].requestFocus();
		return v;
	}

	private void done(String pin) {
//		resetPinInput();
		if (mListener != null) {
			mListener.done(pin);
		}
	}
	private void cancel() {
		if (mListener != null) {
			mListener.cancel();
		}
	}


	private String getPinInput() {
		MtkLog.d(TAG, "getPinInput");
		String result = "";
		int i = 0;
		try {
			for (PinNumberPicker pnp : mPickers) {
				// pnp.updateText();
				result += pnp.getValue();
				MtkLog.d(TAG, "pnp[" + i++ + "]:" + pnp.getValue());
			}
		} catch (IllegalStateException e) {
			result = "";
		}
		MtkLog.d(TAG, "result:" + result);
		return result;
	}

	@Override
	public void onResume() {
		super.onResume();
		MtkLog.d(TAG, "onResume");
	}

	@Override
	public void onStart() {
		super.onStart();
		MtkLog.d(TAG, "onStart");
	}

	public void resetPinInput() {
		for (PinNumberPicker pnp : mPickers) {
			pnp.setValueRange(0, 9);
		}
		mPickers[0].requestFocus();
	}

	public static final class PinNumberPicker extends FrameLayout {
		private static final int NUMBER_VIEWS_RES_ID[] = {
				R.id.previous2_number, R.id.previous_number,
				R.id.current_number, R.id.next_number, R.id.next2_number };
		private static final int CURRENT_NUMBER_VIEW_INDEX = 2;

		private static Animator sFocusedNumberEnterAnimator;
		private static Animator sFocusedNumberExitAnimator;
		private static Animator sAdjacentNumberEnterAnimator;
		private static Animator sAdjacentNumberExitAnimator;

		private static float sAlphaForFocusedNumber;
		private static float sAlphaForAdjacentNumber;

		private int mMinValue;
		private int mMaxValue;
		private int mCurrentValue;
		private int mNextValue;
		private int mNumberViewHeight;
		private PinDialogFragment mDialog;
		private PinNumberPicker mNextNumberPicker;
		private boolean mCancelAnimation;

		private final View mNumberViewHolder;
		private final View mBackgroundView;
		private final TextView[] mNumberViews;
		private final OverScroller mScroller;

		public PinNumberPicker(Context context) {
			this(context, null);
		}

		public PinNumberPicker(Context context, AttributeSet attrs) {
			this(context, attrs, 0);
		}

		public PinNumberPicker(Context context, AttributeSet attrs,
				int defStyleAttr) {
			this(context, attrs, defStyleAttr, 0);
		}

		public PinNumberPicker(Context context, AttributeSet attrs,
				int defStyleAttr, int defStyleRes) {
			super(context, attrs, defStyleAttr, defStyleRes);
			View view = inflate(context, R.layout.ci_fragment_number_picker,
					this);
			mNumberViewHolder = view.findViewById(R.id.number_view_holder);
			mBackgroundView = view.findViewById(R.id.focused_background);
			mNumberViews = new TextView[NUMBER_VIEWS_RES_ID.length];
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				mNumberViews[i] = (TextView) view
						.findViewById(NUMBER_VIEWS_RES_ID[i]);
			}
			Resources resources = context.getResources();
			mNumberViewHeight = resources
					.getDimensionPixelOffset(R.dimen.pin_number_picker_text_view_height);

			mScroller = new OverScroller(context);

			mNumberViewHolder
					.setOnFocusChangeListener(new OnFocusChangeListener() {
						@Override
						public void onFocusChange(View v, boolean hasFocus) {
							updateFocus();
						}
					});

			mNumberViewHolder.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						switch (keyCode) {

						case KeyEvent.KEYCODE_DPAD_UP:
						case KeyEvent.KEYCODE_DPAD_DOWN: {
							if (!mScroller.isFinished() || mCancelAnimation) {
								endScrollAnimation();
							}
							if (mScroller.isFinished() || mCancelAnimation) {
								mCancelAnimation = false;
								if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
									mNextValue = adjustValueInValidRange(mCurrentValue + 1);
									startScrollAnimation(true);
									mScroller
											.startScroll(
													0,
													0,
													0,
													mNumberViewHeight,
													getResources()
															.getInteger(
																	R.integer.pin_number_scroll_duration));
								} else {
									mNextValue = adjustValueInValidRange(mCurrentValue - 1);
									startScrollAnimation(false);
									mScroller
											.startScroll(
													0,
													0,
													0,
													-mNumberViewHeight,
													getResources()
															.getInteger(
																	R.integer.pin_number_scroll_duration));
								}
								updateText();
								invalidate();
							}
							return true;
						}
						}
					} else if (event.getAction() == KeyEvent.ACTION_UP) {
						switch (keyCode) {
						case KeyEvent.KEYCODE_DPAD_UP:
						case KeyEvent.KEYCODE_DPAD_DOWN: {
							mCancelAnimation = true;
							return true;
						}
						}
					}
					return false;
				}
			});
			mNumberViewHolder.setScrollY(mNumberViewHeight);
		}

		static void loadResources(Context context) {
			if (sFocusedNumberEnterAnimator == null) {
				TypedValue outValue = new TypedValue();
				context.getResources().getValue(
						R.dimen.pin_alpha_for_focused_number, outValue,
						true);
				sAlphaForFocusedNumber = outValue.getFloat();
				context.getResources().getValue(
						R.dimen.pin_alpha_for_adjacent_number, outValue,
						true);
				sAlphaForAdjacentNumber = outValue.getFloat();

				sFocusedNumberEnterAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_focused_number_enter);
				sFocusedNumberExitAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_focused_number_exit);
				sAdjacentNumberEnterAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_adjacent_number_enter);
				sAdjacentNumberExitAnimator = AnimatorInflater.loadAnimator(
						context, R.animator.pin_adjacent_number_exit);
			}
		}

		@Override
		public void computeScroll() {
			super.computeScroll();
			if (mScroller.computeScrollOffset()) {
				mNumberViewHolder.setScrollY(mScroller.getCurrY()
						+ mNumberViewHeight);
				updateText();
				invalidate();
			} else if (mCurrentValue != mNextValue) {
				mCurrentValue = mNextValue;
			}
		}

		@Override
		public boolean dispatchKeyEvent(KeyEvent event) {
			MtkLog.d(TAG, "dispatchKeyEvent,keyCode:" + event.getKeyCode());
			if (event.getAction() == KeyEvent.ACTION_UP) {
				int keyCode = event.getKeyCode();
				if (keyCode >= KeyEvent.KEYCODE_0
						&& keyCode <= KeyEvent.KEYCODE_9) {
					setNextValue(keyCode - KeyEvent.KEYCODE_0);
					if (mNextNumberPicker == null) {
						if (mScroller.isFinished() || mCancelAnimation) {
							mCancelAnimation = false;
							startScrollAnimation(true);
							mScroller.startScroll(
											0,
											0,
											0,
											0,
											getResources().getInteger(R.integer.pin_number_scroll_duration));
							mCurrentValue = mNextValue;
							updateText();
							invalidate();
							endScrollAnimation();
						}
						return true;
					}
				}else if(keyCode== KeyMap.KEYCODE_BACK){
					mDialog.cancel();
					return true;
				}else if (keyCode != KeyEvent.KEYCODE_DPAD_CENTER
						&& keyCode != KeyEvent.KEYCODE_ENTER) {
					return super.dispatchKeyEvent(event);
				}
				if (mNextNumberPicker == null) {
					String pin = mDialog.getPinInput();
					MtkLog.d(TAG, "getPinInput:" + pin);
					if (!TextUtils.isEmpty(pin)) {
						mDialog.done(pin);
					}
				} else {
					mNextNumberPicker.requestFocus();
				}
				return true;
			}
			return super.dispatchKeyEvent(event);
		}

		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			mNumberViewHolder.setFocusable(enabled);
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				mNumberViews[i].setEnabled(enabled);
			}
		}

		void startScrollAnimation(boolean scrollUp) {
			if (scrollUp) {
				sAdjacentNumberExitAnimator.setTarget(mNumberViews[1]);
				sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
				sFocusedNumberEnterAnimator.setTarget(mNumberViews[3]);
				sAdjacentNumberEnterAnimator.setTarget(mNumberViews[4]);
			} else {
				sAdjacentNumberEnterAnimator.setTarget(mNumberViews[0]);
				sFocusedNumberEnterAnimator.setTarget(mNumberViews[1]);
				sFocusedNumberExitAnimator.setTarget(mNumberViews[2]);
				sAdjacentNumberExitAnimator.setTarget(mNumberViews[3]);
			}
			sAdjacentNumberExitAnimator.start();
			sFocusedNumberExitAnimator.start();
			sFocusedNumberEnterAnimator.start();
			sAdjacentNumberEnterAnimator.start();
		}

		void endScrollAnimation() {
			sAdjacentNumberExitAnimator.end();
			sFocusedNumberExitAnimator.end();
			sFocusedNumberEnterAnimator.end();
			sAdjacentNumberEnterAnimator.end();
			mCurrentValue = mNextValue;
			mNumberViews[1].setAlpha(sAlphaForAdjacentNumber);
			mNumberViews[2].setAlpha(sAlphaForFocusedNumber);
			mNumberViews[3].setAlpha(sAlphaForAdjacentNumber);
		}

		void setValueRange(int min, int max) {
			if (min > max) {
				throw new IllegalArgumentException(
						"The min value should be greater than or equal to the max value");
			}
			mMinValue = min;
			mMaxValue = max;
			mNextValue = mCurrentValue = mMinValue - 1;
			clearText();
			mNumberViews[CURRENT_NUMBER_VIEW_INDEX].setText("-");
		}

		void setPinDialogFragment(PinDialogFragment dlg) {
			mDialog = dlg;
		}

		void setNextNumberPicker(PinNumberPicker picker) {
			mNextNumberPicker = picker;
		}

		int getValue() {
			if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
				throw new IllegalStateException("Value is not set");
			}
			return mCurrentValue;
		}

		// Will take effect when the focus is updated.
		void setNextValue(int value) {
			MtkLog.d(TAG, "setNextValue:" + value);
			if (value < mMinValue || value > mMaxValue) {
				throw new IllegalStateException("Value is not set");
			}
			mNextValue = adjustValueInValidRange(value);
		}

		void updateFocus() {
			MtkLog.d(TAG, "update focus");
			endScrollAnimation();
			if (mNumberViewHolder.isFocused()) {
				mBackgroundView.setVisibility(View.VISIBLE);
				updateText();
			} else {
				mBackgroundView.setVisibility(View.GONE);
				if (!mScroller.isFinished()) {
					mCurrentValue = mNextValue;
					mScroller.abortAnimation();
				}
				clearText();
				mNumberViewHolder.setScrollY(mNumberViewHeight);
			}
		}

		private void clearText() {
			for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
				if (i != CURRENT_NUMBER_VIEW_INDEX) {
					mNumberViews[i].setText("");
				} else if (mCurrentValue >= mMinValue
						&& mCurrentValue <= mMaxValue) {
					mNumberViews[i].setText(String.valueOf(mCurrentValue));
				}
			}
		}

		private void updateText() {
			if (mNumberViewHolder.isFocused()) {
				if (mCurrentValue < mMinValue || mCurrentValue > mMaxValue) {
					mNextValue = mCurrentValue = mMinValue;
				}
				int value = adjustValueInValidRange(mCurrentValue
						- CURRENT_NUMBER_VIEW_INDEX);
				for (int i = 0; i < NUMBER_VIEWS_RES_ID.length; ++i) {
					mNumberViews[i].setText(String
							.valueOf(adjustValueInValidRange(value)));
					value = adjustValueInValidRange(value + 1);
				}
			}
		}

		private int adjustValueInValidRange(int value) {
			MtkLog.d(TAG, "adjustValueInValidRange:" + value);
			int interval = mMaxValue - mMinValue + 1;
			if (value < mMinValue - interval || value > mMaxValue + interval) {
				throw new IllegalArgumentException("The value( " + value
						+ ") is too small or too big to adjust");
			}
			int retValue = (value < mMinValue) ? value + interval
					: (value > mMaxValue) ? value - interval : value;
			MtkLog.d(TAG, "return value:" + retValue);
			return retValue;
		}
	}
}
