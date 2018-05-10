package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.util.KeyMap;

public class MoveModeDialog extends DialogFragment implements DialogInterface.OnKeyListener {

	private static final String TAG = "MoveModeDialog";

	private OnMovePicListener mMovePicListener;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = new Dialog(getActivity(), R.style.Theme_ToastDialog);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // 设置Content前设定
		dialog.setContentView(R.layout.move_mode_layout);
		dialog.setCanceledOnTouchOutside(true); // 外部点击取消
		dialog.setOnKeyListener(this);

        final Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        window.setAttributes(layoutParams);

		return dialog;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		Log.i(TAG, "onKey: keycode = " + keyCode);
		int action = event.getAction();

		if (KeyEvent.ACTION_DOWN == action) {
			switch (keyCode) {
				case KeyMap.KEYCODE_BACK:
					setShowsDialog(false);
					mMovePicListener.movePic(KeyEvent.KEYCODE_BACK);
					dismiss();
					break;
				case KeyEvent.KEYCODE_DPAD_CENTER:
					mMovePicListener.movePic(KeyEvent.KEYCODE_DPAD_CENTER);
					break;
				case KeyEvent.KEYCODE_DPAD_UP:
					mMovePicListener.movePic(KeyEvent.KEYCODE_DPAD_UP);
					break;
				case KeyEvent.KEYCODE_DPAD_DOWN:
					mMovePicListener.movePic(KeyEvent.KEYCODE_DPAD_DOWN);
					break;
				case KeyEvent.KEYCODE_DPAD_LEFT:
					mMovePicListener.movePic(KeyEvent.KEYCODE_DPAD_LEFT);
					break;
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					mMovePicListener.movePic(KeyEvent.KEYCODE_DPAD_RIGHT);
					break;
			}
			return true;
		}
		return false;
	}

	public interface OnMovePicListener {
		void movePic(int action);
	}

	public void setOnMovePicListener(OnMovePicListener listener) {
		this.mMovePicListener = listener;
	}
}
