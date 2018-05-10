package com.mediatek.wwtv.mediaplayer.mmpcm.videoimpl;

import android.graphics.Rect;
import android.util.Log;

import com.mediatek.twoworlds.tv.MtkTvUtil;
import com.mediatek.twoworlds.tv.model.MtkTvRectangle;
import com.mediatek.twoworlds.tv.model.MtkTvRegionCapability;

import com.mediatek.wwtv.util.MtkLog;

public class VideoComset{
	private static final String TAG = "VideoComset";
	private int MaxZoomType;
	private int curZoomType;
	private static final String SOURCE_MAIN = "main";
	private final MtkTvUtil mtkTvUtil;

	public VideoComset() {
		mtkTvUtil = MtkTvUtil.getInstance();
		MaxZoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
		curZoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
		calMaxZoom();
	}

	/**
	 * get current zoom type
	 */
	public int getCurZoomType() {
		return curZoomType;
	}

	/**
	 * get zoom types
	 */
	public int[] getZoomTypes() {
		return null;
	}

	/**
	 * set zoom type
	 */
	public void setZoomType(int type) {
		if (type < VideoConst.VOUT_ZOOM_TYPE_1X
				|| type > VideoConst.VOUT_ZOOM_TYPE_512X) {
			MtkLog.d(TAG,"Comset.setZoomType"
					+ "This zoom type is not supported !!!");
			curZoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
		}
		curZoomType = type;
	}

	/**
	 * get speed
	 *
	 * @return
	 */
	public int getSpeed() {
		return 0;
	}

	/**
	 * set picture detail message
	 */
	public void setPictureDetail() {

	}

	/**
	 * change video zoom
	 *
	 * @param videoZoomType
	 *            the type of zoom
	 */
	public int videoZoom(int videoZoomType) {
		Rect orgVidRect = new Rect();
		Rect orgDspRect = new Rect();
		Rect adjVidRect = new Rect();
		Rect adjDspRect = new Rect();
		float fdivisor = (float) 0.0;
		int maxZoom;
		int ret = -1;
		if (mtkTvUtil == null) {
			MtkLog.d(TAG,"mtkTvUtil manager null!!!");
			return -1;
		}
		if (calMaxZoom() == -1) {
		  return -1;
		}
		maxZoom = MaxZoomType;
		MtkLog.d(TAG,"video" + "getMaxZoom():" + maxZoom);
		if (maxZoom < 0) {
			maxZoom = VideoConst.VOUT_ZOOM_TYPE_1X;
		}
		if (maxZoom < videoZoomType) {
			MtkLog.d(TAG,"video" + "maxZoom < videoZoomType");
			return -1;
		}
		if (videoZoomType < VideoConst.VOUT_ZOOM_TYPE_1X
				|| videoZoomType > VideoConst.VOUT_ZOOM_TYPE_512X) {
			MtkLog.d(TAG,"video zoom type is not supported!");
			return -1;
		}
		orgVidRect = getScreenOutputVideoRect();
		orgDspRect = getScreenOutputRect();
		if (null != orgVidRect) {
			MtkLog.d(TAG,"video" + "orgVidRect.x = " + orgVidRect.left);
			MtkLog.d(TAG,"video" + "orgVidRect.y = " + orgVidRect.top);
			MtkLog.d(TAG,"video" + "orgVidRect.w = " + orgVidRect.right);
			MtkLog.d(TAG,"video" + "orgVidRect.h = " + orgVidRect.bottom);
		}
		if (null != orgDspRect) {
			MtkLog.d(TAG,"video" + "orgDspRect.x = " + orgDspRect.left);
			MtkLog.d(TAG,"video" + "orgDspRect.y = " + orgDspRect.top);
			MtkLog.d(TAG,"video" + "orgDspRect.w = " + orgDspRect.right);
			MtkLog.d(TAG,"video" + "orgDspRect.h = " + orgDspRect.bottom);
		}

		adjDspRect.left = adjDspRect.top = 0;
		adjDspRect.right = VideoConst.VOUT_REGION_MAX_WIDTH;
		adjDspRect.bottom = VideoConst.VOUT_REGION_MAX_HEIGTH;
		if (0 == videoZoomType % 2) {
			fdivisor = (float) ((1 << (videoZoomType / 2)) * 1.000);
		} else {
			fdivisor = (float) ((1 << (videoZoomType / 2)) * 1.414);
		}
		adjVidRect.left = (int) (VideoConst.VOUT_REGION_MAX_WIDTH / 2 - VideoConst.VOUT_REGION_MAX_WIDTH
				/ fdivisor / 2);
		adjVidRect.right = (int) (VideoConst.VOUT_REGION_MAX_WIDTH / 2 + VideoConst.VOUT_REGION_MAX_WIDTH
				/ fdivisor / 2);
		adjVidRect.top = (int) (VideoConst.VOUT_REGION_MAX_HEIGTH / 2 - VideoConst.VOUT_REGION_MAX_HEIGTH
				/ fdivisor / 2);
		adjVidRect.bottom = (int) (VideoConst.VOUT_REGION_MAX_HEIGTH / 2 + VideoConst.VOUT_REGION_MAX_HEIGTH
				/ fdivisor / 2);
		Log.d(TAG, "videoZoom :" + Log.getStackTraceString(new Throwable()));
		ret = setScreenOutputVideoRect(adjVidRect);
		MtkLog.d(TAG,"setScreenOutputVideoRect ret = " + ret);
		if (ret == -1) {
		  return -1;
		}
		ret = setScreenOutputRect(adjDspRect);
		MtkLog.d(TAG,"setScreenOutputRect ret = " + ret);
		if (ret == -1) {
		  return -1;
		}
		curZoomType = videoZoomType;
		return ret;
	}

	private int calMaxZoom() {
		int maxZoom = VideoConst.VOUT_ZOOM_TYPE_1X;
		float fdivisor = (float) 0.0;
		int videoWidth = 0;
		int videoHeight = 0;
		if (mtkTvUtil == null) {
			MtkLog.d(TAG,"mtkTvUtil manager null!!!");
			return -1;
		}
		MtkTvRegionCapability capability = mtkTvUtil.getVideoSrcRegionCapability(SOURCE_MAIN);
		if (null != capability) {
			MtkLog.d(TAG,"video" + "capability.width_min:"
					+ capability.getWidthMin());
			MtkLog.d(TAG,"video" + "capability.height_min:"
					+ capability.getHeightMin());
		}

		for (maxZoom = VideoConst.VOUT_ZOOM_TYPE_1X; maxZoom <= VideoConst.VOUT_ZOOM_TYPE_512X; maxZoom++) {
			if (0 == maxZoom % 2) {
				fdivisor = (float) ((1 << (maxZoom / 2)) * 1.000);
			} else {
				fdivisor = (float) ((1 << (maxZoom / 2)) * 1.414);
			}

			videoWidth = (int) (VideoConst.VOUT_REGION_MAX_WIDTH / fdivisor);
			videoHeight = (int) (VideoConst.VOUT_REGION_MAX_HEIGTH / fdivisor);

			if (null != capability) {
				int mWidthmin = capability.getWidthMin();
				int mHeightmin = capability.getHeightMin();
				if ((mWidthmin > videoWidth) || (mHeightmin > videoHeight)) {
					MaxZoomType = maxZoom - 1;
					return 1;
				}
			}
		}

		MaxZoomType = VideoConst.VOUT_ZOOM_TYPE_512X;
		return 1;
	}

	/**
	 * get max zoom
	 */
	public int getMaxZoom() {
		MtkLog.d(TAG,"getMaxZoom MaxZoomType =" +MaxZoomType);
		return MaxZoomType;
	}

	private Rect getScreenOutputVideoRect(){
		MtkTvRectangle mSrcRectangleRectF = mtkTvUtil.getScreenSourceRect(SOURCE_MAIN);
		Rect r = new Rect((int) (mSrcRectangleRectF.getX()* 10000.0f),
						(int) (mSrcRectangleRectF.getY() * 10000.0f),
						(int) ((mSrcRectangleRectF.getW()+mSrcRectangleRectF.getX())* 10000.0f),
						(int) ((mSrcRectangleRectF.getH() + mSrcRectangleRectF.getY())* 10000.0f));
		return r;
	}

	private Rect getScreenOutputRect(){
	  	MtkTvRectangle mScreenRectangle = mtkTvUtil.getScreenOutputDispRect(SOURCE_MAIN);
		Rect r = new Rect((int) (mScreenRectangle.getX()* 10000.0f),
				(int) (mScreenRectangle.getY() * 10000.0f),
				(int) ((mScreenRectangle.getW() + mScreenRectangle.getX())* 10000.0f),
				(int) ((mScreenRectangle.getH() + mScreenRectangle.getY())* 10000.0f));
		return r;
	}

	private int setScreenOutputVideoRect(Rect rect){
		MtkTvRectangle  srcRect = new MtkTvRectangle(rect.left / 10000.0f,
					rect.top / 10000.0f, ((float) rect.right - (float) rect.left )/ 10000.0f,
					((float) rect.bottom - (float) rect.top )/ 10000.0f);
		return mtkTvUtil.setScreenSourceRect(SOURCE_MAIN,srcRect);
	}

	private int setScreenOutputRect(Rect rect){
		MtkTvRectangle  screenRect = new MtkTvRectangle(rect.left / 10000.0f,
					rect.top / 10000.0f, ((float) rect.right - (float) rect.left )/ 10000.0f,
					((float) rect.bottom - (float) rect.top )/ 10000.0f);
		return mtkTvUtil.setScreenOutputDispRect(SOURCE_MAIN,screenRect);
	}

	/**
	 * reset video zoom
	 *
	 * @return
	 */
	public int videoZoomReset() {
		Rect outRect = new Rect(0, 0, VideoConst.VOUT_REGION_MAX_WIDTH,
				VideoConst.VOUT_REGION_MAX_HEIGTH);
		if (mtkTvUtil == null) {
			MtkLog.d(TAG,"mtkTvUtil manager null!!!");
			return -1;
		}
		MtkLog.d(TAG,"zoom reset!!!");
		curZoomType = VideoConst.VOUT_ZOOM_TYPE_1X;
		return setScreenOutputRect(outRect);
	}

}