package com.mediatek.wwtv.tvcenter.util;

import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import com.mediatek.wwtv.util.MtkLog;

public class GetCurrentTask {

	private static final String TAG = "GetCurrentTask";

	private Context mContext = null;
	private static GetCurrentTask curTask = null;

	private GetCurrentTask(Context context){
		mContext = context;
	}

	public static GetCurrentTask getInstance(Context context){
		if(curTask == null){
			curTask = new GetCurrentTask(context);
		}
		return curTask;
	}

	public static GetCurrentTask getInstance(){
		return curTask;
	}

	private  ComponentName getCurRunningCN(){
		ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
		ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
		return cn;
	}

	public List<RunningTaskInfo> getRunningTasks(){
		ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
	    List<RunningTaskInfo>   mTaskList =am.getRunningTasks(12);
	    return mTaskList ;
	}

	public String getCurRunningClass(){
		String className = getCurRunningCN().getClassName();
		MtkLog.d(TAG,"Current Running Activity Name: " + className);
		return className;
	}

	public String getCurRunningPKG(){
		String packageName = getCurRunningCN().getPackageName();
		MtkLog.d(TAG," Current Running Package Name: " + packageName);
		return packageName;
	}

	public boolean isCurTaskTKUI() {
		return getCurRunningPKG().equals("com.mediatek.wwtv.tvcenter");
	}

	public boolean isCurActivtyMeidaMainActivity() {
		return getCurRunningClass().contains("com.mediatek.wwtv.tvcenter.mmp.");
	}

	public boolean isCurActivityTkuiMainActivity() {
		return getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.nav.TurnkeyUiMainActivity");
	}
	public boolean isCurMediaActivity(){
		return false;
	}

	public boolean isCurOADActivity(){
		return getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.oad.NavOADActivity");
	}

	public boolean isCurWizardActivity(){
		return false;
	}
	public boolean isCurEPGActivity(){
		return getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.epg.us.EPGUsActivity")
				|| getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity")
				|| getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.epg.sa.EPGSaActivity");
	}

	public boolean isCurEUEPGActivity(){
		return getCurRunningClass().equals("com.mediatek.wwtv.tvcenter.epg.eu.EPGEuActivity");
	}

}
