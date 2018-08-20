package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import android.content.DialogInterface;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.util.Util;


/**
 * Created by sniuniu on 2018/3/26.
 */

public class PhotoInfoDialog extends InfoDialog implements View.OnClickListener {

	private ImageView mRorate_iv;
	private ImageView mZoomIn_iv;
	private ImageView mZoomOut_iv;
	private TextView mPhotoTitle_tv;
	private TextView mPhotoFormat_tv;
	private TextView mPhotoResolution_tv;

	private OnZoomChangeListener mZoomChangeListener;

    @Override
    protected int setupContentView() {
        return R.layout.photo_info_layout;
    }

    @Override
    public void initView(View view) {
        mPhotoTitle_tv = view.findViewById(R.id.photo_title_text_view);
        mPhotoFormat_tv = view.findViewById(R.id.photo_format_text_view);
        mPhotoResolution_tv = view.findViewById(R.id.photo_resolution_text_view);

        mRorate_iv = view.findViewById(R.id.photo_rorate_image_view);
        mZoomIn_iv = view.findViewById(R.id.photo_zoom_in_image_view);
        mZoomOut_iv = view.findViewById(R.id.photo_zoom_out_image_view);

        mRorate_iv.setFocusable(true);
        mZoomIn_iv.setFocusable(true);
        mZoomOut_iv.setFocusable(true);
    }

    @Override
    protected void initData() {

        LogicManager logicManager = LogicManager.getInstance();
        String photoName = logicManager.getPhotoName();
        mPhotoTitle_tv.setText(photoName);

        String format = photoName.substring(photoName.lastIndexOf(".") + 1);
        mPhotoFormat_tv.setText(format);

        mPhotoResolution_tv.setText(logicManager.getResolution());
    }

    @Override
    public void initEvent() {
        mRorate_iv.setOnClickListener(this);
        mZoomIn_iv.setOnClickListener(this);
        mZoomOut_iv.setOnClickListener(this);

        mRorate_iv.setOnFocusChangeListener(mOnFocusChangeListener);
        mZoomIn_iv.setOnFocusChangeListener(mOnFocusChangeListener);
        mZoomOut_iv.setOnFocusChangeListener(mOnFocusChangeListener);

        mRorate_iv.requestFocus();
    }

    @Override
    protected DialogInterface.OnKeyListener getKeyListener() {
        return null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.photo_rorate_image_view:
                RorateModeDialog rorateModeDialog = new RorateModeDialog();
                rorateModeDialog.show(getFragmentManager(), "rorate_mode");
                if (Util.PHOTO_4K2K_ON) {
                    ((Photo4K2KPlayActivity) getActivity()).rotate();
                    ((Photo4K2KPlayActivity) getActivity()).pausePhoto();
                } else {
                    ((PhotoPlayActivity) getActivity()).rotate();
                    ((PhotoPlayActivity) getActivity()).pausePhoto();
                }
//				dismiss();
                break;
            case R.id.photo_zoom_in_image_view:
                if (Util.PHOTO_4K2K_ON) {
                    mZoomChangeListener.zoomChange(Photo4K2KPlayActivity.ZOOMIN);
                    ((Photo4K2KPlayActivity) getActivity()).pausePhoto();
                } else {
                    mZoomChangeListener.zoomChange(PhotoPlayActivity.ZOOMIN);
                    ((PhotoPlayActivity) getActivity()).pausePhoto();
                }
//				dismiss();
                break;
            case R.id.photo_zoom_out_image_view:
                if (Util.PHOTO_4K2K_ON) {
                    mZoomChangeListener.zoomChange(Photo4K2KPlayActivity.ZOOMOUT);
                    ((Photo4K2KPlayActivity) getActivity()).pausePhoto();
                } else {
                    mZoomChangeListener.zoomChange(PhotoPlayActivity.ZOOMOUT);
                    ((PhotoPlayActivity) getActivity()).pausePhoto();
                }
//				dismiss();
                break;
        }
    }

    private View.OnFocusChangeListener mOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if (hasFocus) {
                ViewCompat.animate(view).scaleX(1.17f).scaleY(1.17f).translationZ(1).start();
            } else {
                ViewCompat.animate(view).scaleX(1.0f).scaleY(1.0f).translationZ(0).start();
            }
        }
    };

	public interface OnZoomChangeListener {
		void zoomChange(int type);
	}

	public void setOnZoomChangeListener(OnZoomChangeListener zoomChangeListener) {
		this.mZoomChangeListener = zoomChangeListener;
	}
}
