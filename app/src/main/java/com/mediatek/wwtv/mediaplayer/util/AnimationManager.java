package com.mediatek.wwtv.mediaplayer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

import com.mediatek.wwtv.mediaplayer.mmp.carousel.Rotate3D;
import com.mediatek.wwtv.util.MtkLog;

public class AnimationManager {
	private static final String TAG = AnimationManager.class.getSimpleName();

	private static int bannerDuration = 400;

	private static AnimationManager mAnimationManager;

	private static final String animationConfigFilePath = "/etc/animation.conf";

	public static final int TYPE_SIMPLE_BANNER_ENTER = 1;
	public static final int TYPE_SIMPLE_BANNER_EXIT = 2;
	public static final int TYPE_BASIC_BANNER_ENTER = 3;
	public static final int TYPE_BASIC_BANNER_EXIT = 4;
	public static final int TYPE_DETAIL_BANNER_ENTER = 5;
	public static final int TYPE_DETAIL_BANNER_EXIT = 6;
	public static final int TYPE_SIMPLE_BANNER_ENTER_0 = 7;

	private static boolean isAnimation = true;
	private boolean isAnimationRunning = false;

	public static synchronized AnimationManager getInstance() {
		if (mAnimationManager == null) {
			mAnimationManager = new AnimationManager();
		}
		return mAnimationManager;
	}

	public AnimationManager() {
		//initAnimationConfig();
	}

	private void initAnimationConfig(){
		File file = new File(animationConfigFilePath);
		if(file.exists()){
			MtkLog.d(TAG, "file exist" );
			BufferedReader reader = null;
			try {

				reader = new BufferedReader(new FileReader(file));

				String tempString;

				if ((tempString = reader.readLine()) != null) {
					MtkLog.d(TAG, "string = " + tempString);
					if (tempString.contains("1")) {
						isAnimation = true;
					} else {
						isAnimation = false;
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally{
				if(null != reader){
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}else{
			MtkLog.d(TAG, "file not exist" );

			isAnimation = false;
		}
		MtkLog.d(TAG, "animation is " + isAnimation);
	}

	public boolean getIsAnimation(){
		return isAnimation;
	}





	public Rotate3D activityAnimation;
	public void startActivityEnterAnimation(Context context,
			View layout, AnimationListener animationListener) {
		MtkLog.d(TAG, "activityAnimation>>111>>" + activityAnimation + ">>" + ((Activity)context).isFinishing());
//		if(null != activityAnimation){
//			return;
//		}
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int mCenterX = dm.widthPixels / 2;
		int mCenterY = dm.heightPixels / 2;

		activityAnimation = new Rotate3D(90, 0, mCenterX, mCenterY);
		activityAnimation.setFillAfter(true);
		activityAnimation.setDuration(500);
		activityAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "startActivityEnterAnimation onAnimationStart");
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "startActivityEnterAnimation onAnimationEnd");
				activityAnimation = null;
			}
		});

		layout.startAnimation(activityAnimation);
	}

	public void startActivityReEnterAnimation(Context context,
			View layout, AnimationListener animationListener) {
		if(null != activityAnimation){
			return;
		}

		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int mCenterX = dm.widthPixels / 2;
		int mCenterY = dm.heightPixels / 2;

		activityAnimation = new Rotate3D(-90, 0, mCenterX, mCenterY);
		activityAnimation.setFillAfter(true);
		activityAnimation.setDuration(500);
		activityAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "REEnterAnimation:onAnimationStart");
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "REEnterAnimation:onAnimationEnd");
				activityAnimation = null;
			}
		});
		layout.clearAnimation();
		layout.startAnimation(activityAnimation);
	}

	public void setActivityAnimationNull(){
	    activityAnimation = null;
	}
	public void startActivityEndAnimation(final Context context, View layout,
			AnimationListener animationListener) {
		MtkLog.d(TAG, "startActivityEndAnimation activityAnimation>>>>" + activityAnimation + ">>" + ((Activity)context).isFinishing()
				+ ">>>" + isAnimationRunning);
		if(((Activity)context).isFinishing() || isAnimationRunning){
//			activityAnimation = null;
			return;
		}
		DisplayMetrics dm = new DisplayMetrics();
		dm = context.getResources().getDisplayMetrics();
		int mCenterX = dm.widthPixels / 2;
		int mCenterY = dm.heightPixels / 2;

		activityAnimation = new Rotate3D(0, -90, mCenterX, mCenterY);
		activityAnimation.setFillAfter(true);
		activityAnimation.setDuration(500);

		activityAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "startActivityEndAnimation onAnimationStart");
			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				// TODO Auto-generated method stub
				MtkLog.d(TAG, "startActivityEndAnimation onAnimationEnd");
				isAnimationRunning = false;
				((Activity)context).finish();
				activityAnimation = null;
			}
		});
		MtkLog.d(TAG, "began......"+System.currentTimeMillis()+",can run=="+!isAnimationRunning);
		if(!isAnimationRunning){
			isAnimationRunning = true;
			new Handler().postDelayed(new Runnable(){
				@Override
				public void run() {
//					layout.clearAnimation();
					MtkLog.d(TAG, "endanimation......"+System.currentTimeMillis() + ">>>" + isAnimationRunning + ">>" + ((Activity)context).isFinishing());
					if (isAnimationRunning) {
						isAnimationRunning = false;
					}
					if(!((Activity)context).isFinishing()) {
						((Activity)context).finish();
					}
					activityAnimation = null ;
				}
			}, activityAnimation.getDuration() * 2);
			layout.clearAnimation();
			layout.startAnimation(activityAnimation);
		}
	}

	public void startAnimation(int type, View layout,
			AnimatorListenerAdapter listener) {

		ObjectAnimator bounceAnim = null;

		layout.clearAnimation();

		switch (type) {
		case TYPE_SIMPLE_BANNER_ENTER:
			bounceAnim = ObjectAnimator.ofFloat(layout, "x",
					ScreenConstant.SCREEN_WIDTH, layout.getLeft()).setDuration(
					bannerDuration);
			break;
		case TYPE_SIMPLE_BANNER_ENTER_0:
			bounceAnim = ObjectAnimator.ofFloat(layout, "x",
					ScreenConstant.SCREEN_WIDTH, layout.getLeft()).setDuration(
					0);
			break;
		case TYPE_SIMPLE_BANNER_EXIT:
			bounceAnim = ObjectAnimator.ofFloat(layout, "x", layout.getLeft(),
					ScreenConstant.SCREEN_WIDTH).setDuration(bannerDuration);
			break;
		case TYPE_BASIC_BANNER_ENTER:
			bounceAnim = ObjectAnimator.ofFloat(layout, "x",
					0 - layout.getWidth(), layout.getLeft()).setDuration(
					bannerDuration);
			break;
		case TYPE_BASIC_BANNER_EXIT:
			bounceAnim = ObjectAnimator.ofFloat(layout, "x", layout.getLeft(),
					0 - layout.getWidth()).setDuration(bannerDuration);
			break;
		case TYPE_DETAIL_BANNER_ENTER:
			bounceAnim = ObjectAnimator.ofFloat(layout, "y",
					0 - layout.getHeight(), layout.getTop()).setDuration(
					bannerDuration);
			break;
		case TYPE_DETAIL_BANNER_EXIT:
			bounceAnim = ObjectAnimator.ofFloat(layout, "y", layout.getTop(),
					0 - layout.getHeight()).setDuration(bannerDuration);
			break;
		}

		if (null != bounceAnim && null != listener)
			bounceAnim.addListener(listener);

		if (null != bounceAnim)
			bounceAnim.start();
	}

	public void adjustVolEnterAnimation(View layout) {

		ValueAnimator bounceAnim = ObjectAnimator.ofFloat(layout, "y",
				ScreenConstant.SCREEN_HEIGHT + layout.getHeight(),
				layout.getTop()).setDuration(bannerDuration);
		// bounceAnim.setInterpolator(new BounceInterpolator());
		bounceAnim.start();
	}

	public void adjustVolExitAnimation(View layout,
			AnimatorListenerAdapter listener) {
		ValueAnimator bounceAnim = ObjectAnimator.ofFloat(layout, "y",
				layout.getTop(),
				ScreenConstant.SCREEN_HEIGHT + layout.getHeight()).setDuration(
				bannerDuration);
		bounceAnim.addListener(listener);
		// bounceAnim.setInterpolator(new BounceInterpolator());
		bounceAnim.start();
	}

	public void channelListEnterAnimation(View layout) {
		final ObjectAnimator animator1 = ObjectAnimator.ofFloat(layout,
				"scaleX", 0f, 1f);
		animator1.setDuration(500);

		final ObjectAnimator animator2 = ObjectAnimator.ofFloat(layout,
				"scaleY", 0f, 1f);
		animator2.setDuration(500);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animator1, animator2);
		set.start();
	}

	public void channelListExitAnimation(View layout,
			AnimatorListenerAdapter listener) {
		final ObjectAnimator animator1 = ObjectAnimator.ofFloat(layout,
				"scaleX", 1f, 0f);
		animator1.setDuration(500);

		final ObjectAnimator animator2 = ObjectAnimator.ofFloat(layout,
				"scaleY", 1f, 0f);
		animator2.setDuration(500);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(animator1, animator2);
		set.addListener(listener);
		set.start();
	}

	public static final String FACTORY_TV_RANGE_SCAN_DIG = "tuner_range_scan_dig";
	/**
	 * some item 's page redirect nendn't animation
	 * if use animation the page 'll fresh
	 * @param itemId
	 */
	public boolean isItemAllowAnimation(String itemId){
		if(itemId != null && itemId.equals(FACTORY_TV_RANGE_SCAN_DIG)){
			return false;
		}
		return true;
	}

}
