
package com.mediatek.wwtv.mediaplayer.util;

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

  private GetCurrentTask(Context context) {
    mContext = context;
  }

  public static GetCurrentTask getInstance(Context context) {
    if (curTask == null) {
      curTask = new GetCurrentTask(context);
    }
    return curTask;
  }

  public static GetCurrentTask getInstance() {
    return curTask;
  }

  private ComponentName getCurRunningCN() {
    ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
    return cn;
  }

  // public List<RunningTaskInfo> getRunningTasks(){
  // ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
  // List<RunningTaskInfo> mTaskList =am.getRunningTasks(12);
  // return mTaskList ;
  // }

  public String getCurRunningPackageName() {
    String className = null;// getCurRunningCN().getClassName();
    ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
    ActivityManager.RunningAppProcessInfo topAPP = null;
    if (appList != null && appList.size() > 0) {
      for (int i = 0; i < appList.size(); i++) {
        topAPP = appList.get(i);
        if (topAPP != null
            && topAPP.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            && topAPP.processState == ActivityManager.START_TASK_TO_FRONT) {
          className = topAPP.processName;
          MtkLog.d(TAG, "Current Running Activity xx Name: " + className);
          break;
        }
      }
    }
    MtkLog.d(TAG, "Current Running Activity Name: " + className);
    return className;
  }

  // public String getCurRunningPKG(){
  // String packageName = getCurRunningCN().getPackageName();
  // MtkLog.d(TAG," Current Running Package Name: " + packageName);
  // return packageName;
  // }

  public String getCurRunningClassName() {
    String className = getCurRunningCN().getClassName();
    MtkLog.d(TAG, "Current Running Activity Name: " + className);
    return className;
  }

  public boolean isCurTaskTKUI() {
    return getCurRunningCN().getPackageName().equals("com.mediatek.wwtv.tvcenter");
  }

  public boolean isCurActivtyGridActivity() {
    return getCurRunningClassName().contains(
        "com.mediatek.wwtv.mediaplayer.mmp.multimedia.MtkFilesGridActivity");
  }

  public boolean isCurActivityTkuiMainActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.nav.TurnkeyUiMainActivity");
  }

  public boolean isCurMediaActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.mmp.MeidaMainActivity");
  }

  public boolean isCurMenuMainActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.setting.SettingActivity");
  }

  public boolean isCurOADActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.oad.NavOADActivity");
  }

  public boolean isCurWizardActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.wizard.SetupWizardActivity");
  }

  public boolean isCurEPGActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.epg.us.EPGUsActivity")
        || getCurRunningClassName().equals("com.mediatek.ui.epg.eu.EPGEuActivity")
        || getCurRunningClassName().equals("com.mediatek.ui.epg.sa.EPGSaActivity");
  }

  public boolean isCurEUEPGActivity() {
    return getCurRunningClassName().equals("com.mediatek.ui.epg.eu.EPGEuActivity");
  }

}
