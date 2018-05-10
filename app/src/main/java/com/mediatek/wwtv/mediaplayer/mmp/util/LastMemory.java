
package com.mediatek.wwtv.mediaplayer.mmp.util;

import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;

import android.content.Context;
import android.content.SharedPreferences;

import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;
import com.mediatek.mmp.util.DivxDisplayInfo;
import com.mediatek.mmp.util.DivxLastMemoryFilePosition;

public class LastMemory {

  public static final String TAG = "LastMemory";
  public static final String LASTMEMORYID = "LastMemory";
  public static final String LASTMEMORY_ID = "LastMemoryId";
  public static final int LASTMEMORY_OFF = 0;
  public static final int LASTMEMORY_TIME = 1;
  public static final int LASTMEMORY_POSITION = 2;

  public static final String DIVX_COUNT = "divx_count";
  public static final String DIVX_LAST = "divx_last";
  public static final String DIVX_SORT_LIST = "divx_sort_list";
  public static final String DIVX_PLAYID = "playid";
  public static final String DIVX_UI8_CHIP_ID = "ui8_clip_id";
  public static final String DIVX_UI4_TITLE_IDX = "ui4_title_idx";
  public static final String DIVX_UI4_PLAYLIST_IDX = "ui4_playlist_idx";
  public static final String DIVX_UI4_CHAP_IDX = "ui4_chap_idx";
  public static final String DIVX_UI2_AUD_IDX = "ui2_aud_idx";
  public static final String DIVX_UI2_SUB_IDX = "ui2_sub_idx";
  public static final String DIVX_UI8_AUD_PTS_INFO = "ui8_aud_pts_info";
  public static final String DIVX_UI8_AUD_FRAME_POSITION = "ui8_aud_frame_position";
  public static final String DIVX_UI8_I_PTS_INFO = "ui8_i_pts_info";
  public static final String DIVX_UI8_PTS_INFO = "ui8_pts_info";
  public static final String DIVX_UI8_I_FRAME_POSITION = "ui8_i_frame_position";
  public static final String DIVX_UI8_FRAME_POSITION = "ui8_frame_position";
  public static final String DIVX_I4_TEMPORAL_REFERENCE = "i4_temporal_reference";
  public static final String DIVX_UI2_DECODING_ORDER = "ui2_decoding_order";
  public static final String DIVX_UI8_STC = "ui8_stc";
  public static final String DIVX_UI8_FRAME_POSITION_DISP = "ui8_frame_position_disp";
  public static final String DIVX_UI4_TIMESTAP = "ui4_timestap";
  public static final String LAST_MEMORY_TIME = "ui8_frame_time";

  private static boolean isLastMemorySupport(Context context) {
    if (MultiFilesManager.isSourceLocal(context)) {
      return true;
    } else {
      return false;
    }
  }

  public static void saveLastMemory(Context context) {

    MtkLog.i(TAG, "saveLastMemory");
    // if (!DivxUtil.isDivxSupport(context)) {
    // return;
    // }
    if (!isLastMemorySupport(context)) {
      return;
    }
    if (LogicManager.getInstance(context).getVideoDuration() <= 0
        && !isLastMemoryPosition(context)) {
      return;
    }
    long time = 0;
    if (!isLastMemoryPosition(context)) {
      long duration = LogicManager.getInstance(context).getVideoDuration();
      if (duration <= 0) {
        return;
      } else {
        time = LogicManager.getInstance(context).getVideoProgress();
        MtkLog.i(TAG, "time:" + time + "--duration:" + duration);
        if (time >= duration && time <= 0) {
          return;
        }
      }
    }
    // int[] lists = getList(context);
    SharedPreferences sp = context.getSharedPreferences(DIVX_LAST, 0);
    int count = sp.getInt(DIVX_COUNT, 0);
    long id = getDivxLastMemoryFileID(context);

    long haveId = -1;
    int i = 0;
    /** this to check id is exsisted or not **/
    int[] list = getListCount(sp, count);
    for (; i < count; i++) {
      haveId = sp.getLong(DIVX_PLAYID + list[i], -2);
      if (haveId == id) {
        break;
      }
    }

    int current = 0;
    MtkLog.i(TAG, "saveCurrentPlayInfo id:" + id + "---haveId:" + haveId
        + "--count:" + count);
    if (haveId == id) {
      // if contained ,replace old info and move to first
      current = i;
    } else {
      if (count == 5) {
        // if full,replace the oldest one;
        current = 4;
      } else {
        // if not full,continue to add
        count++;
        current = count - 1;
      }
    }
    writeLastMemory(context, sp, count, id, current, time);
  }

  public static long getDivxLastMemoryFileID(Context context) {
    return LogicManager.getInstance(context).getDivxLastMemoryFileID();
  }

  public static void recoveryLastMemoryInfo(Context context, int type) {
    if (!isLastMemorySupport(context)) {
      return;
    }

    int value = SaveValue.getInstance(context).readValue(LASTMEMORY_ID);
    if (type != value) {
      return;
    }
    LastMemory.setLastMemory(context);
  }

  private static int[] getListCount(SharedPreferences sp, int count) {
    if (count <= 0) {
      return null;
    }
    String sortslist = sp.getString(DIVX_SORT_LIST, "0-1-2-3-4");
    // get int array from string
    String[] strs = sortslist.split("-");
    int[] sortlist = new int[count];
    int i = 0;
    for (; i < count; i++) {
      sortlist[i] = Integer.parseInt(strs[i]);
    }

    return sortlist;
  }

  public static int getLastMemortyType(Context context) {
    int type = SaveValue.getInstance(context).readValue(LASTMEMORY_ID);
    MtkLog.i(TAG, "type:" + type);
    return type;
  }

  private static boolean isLastMemoryPosition(Context context) {
    return LastMemory.getLastMemortyType(context) == LastMemory.LASTMEMORY_POSITION;
  }

  private static void writeLastMemory(Context context, SharedPreferences sp,
      int count, long id, int current, long time) {
    MtkLog.d(TAG, "writeLastMemory --current" + current + "count:" + count + " time:" + time
        + "  isLastMemoryPosition(context):" + isLastMemoryPosition(context));
    // TODO Auto-generated method stub
    DivxLastMemoryFilePosition position = getDivxLastMemoryFilePosition(context);
//    if (isLastMemoryPosition(context)) {
      MtkLog.d(TAG, "writeLastMemory position :" + position);
      if (position == null) {
        return;
      }
      int pos = getIndex(sp, current);
      String sort = getSortString(sp, pos, false);
      MtkLog.d(TAG, "writePrefrence ~pos =" + pos + "--current" + current + "count:" + count
          + " sortString:" + sort);

      writePrefrence(sp, position, pos, id, count, sort, context);
      MtkLog.d(TAG, "BEFORE");
      dumpDivxPositionInfo(position, context);
      MtkLog.d(TAG, "AFTER");
//    } else {
//      dumpDivxPositionInfo(position, context);
//      int pos = getIndex(sp, current);
//      String sort = getSortString(sp, pos, false);
//      writePrefrenceTime(sp, pos, id, count, sort, time);
//    }

  }

  private static void writePrefrenceTime(SharedPreferences sp, int pos, long id, int count,
      String sort, long time) {
    SharedPreferences.Editor edit = sp.edit();
    edit.putString(DIVX_SORT_LIST, sort);
    edit.putInt(DIVX_COUNT, count);
    edit.putLong(DIVX_PLAYID + pos, id);
    edit.putLong(LAST_MEMORY_TIME + pos, time);
    edit.commit();
  }

  private static void writePrefrence(SharedPreferences sp, DivxLastMemoryFilePosition position,
      int pos, long id, int count, String sort, Context context) {
    SharedPreferences.Editor edit = sp.edit();
    edit.putString(DIVX_SORT_LIST, sort);
    edit.putInt(DIVX_COUNT, count);
    edit.putLong(DIVX_PLAYID + pos, id);
    edit.putLong(DIVX_UI8_CHIP_ID + pos, position.ui8_clip_id);
    edit.putInt(DIVX_UI4_TITLE_IDX + pos, position.ui4_title_idx);
    edit.putInt(DIVX_UI4_PLAYLIST_IDX + pos, position.ui4_playlist_idx);
    edit.putInt(DIVX_UI4_CHAP_IDX + pos, position.ui4_chap_idx);
    if (isLastMemoryPosition(context)) {
      edit.putInt(DIVX_UI2_AUD_IDX + pos, position.ui2_aud_idx);
      edit.putInt(DIVX_UI2_SUB_IDX + pos, position.ui2_sub_idx);
    } else {
      edit.putInt(DIVX_UI2_AUD_IDX + pos, 0);
      edit.putInt(DIVX_UI2_SUB_IDX + pos, -1);
    }
    edit.putLong(DIVX_UI8_AUD_PTS_INFO + pos, position.ui8_aud_pts_info);
    edit.putLong(DIVX_UI8_AUD_FRAME_POSITION + pos,
        position.ui8_aud_frame_position);
    edit.putLong(DIVX_UI8_I_PTS_INFO + pos, position.ui8_i_pts_info);
    edit.putLong(DIVX_UI8_PTS_INFO + pos, position.ui8_pts_info);
    edit.putLong(DIVX_UI8_I_FRAME_POSITION + pos,
        position.ui8_i_frame_position);
    edit.putLong(DIVX_UI8_FRAME_POSITION + pos, position.ui8_frame_position);
    edit.putInt(DIVX_I4_TEMPORAL_REFERENCE + pos,
        position.i4_temporal_reference);
    edit.putInt(DIVX_UI2_DECODING_ORDER + pos, position.ui2_decoding_order);
    edit.putLong(DIVX_UI8_STC + pos, position.ui8_stc);
    edit.putLong(DIVX_UI8_FRAME_POSITION_DISP + pos,
        position.ui8_frame_position_disp);
    edit.putInt(DIVX_UI4_TIMESTAP + pos, position.ui4_timestap);
    edit.commit();
  }

  public static DivxLastMemoryFilePosition getDivxLastMemoryFilePosition(Context context) {
    return LogicManager.getInstance(context).getDivxLastMemoryFilePosition();
  }

  private static int getIndex(SharedPreferences sp, int current) {
    // TODO Auto-generated method stub
    if (current >= 5 || current < 0) {
      return -1;
    }

    String sortslist = sp.getString(DIVX_SORT_LIST, "0-1-2-3-4");
    // get int array from string
    String[] strs = sortslist.split("-");

    int pos = Integer.parseInt(strs[current]);
    return pos;

  }

  private static String getSortString(SharedPreferences sp, int pos,
      boolean takeoff) {
    // TODO Auto-generated method stub
    String sortslist = sp.getString(DIVX_SORT_LIST, "0-1-2-3-4");

    // get int array from string
    String[] strs = sortslist.split("-");
    int[] sortlist = new int[strs.length];
    int i = 0;
    for (; i < strs.length; i++) {
      sortlist[i] = Integer.parseInt(strs[i]);
    }

    // get the index of pos parameter
    i = 0;
    int index = 0;
    for (; i < sortlist.length; i++) {
      if (pos == sortlist[i]) {
        index = i;
        break;
      }
    }
    // the values in front of index move forward one step
    i = index;
    int tmp = sortlist[index];
    if (takeoff) {
      for (; i < sortlist.length - 1; i++) {
        sortlist[i] = sortlist[i + 1];
      }
      // move to first one
      sortlist[i] = tmp;
    } else {
      for (; i > 0; i--) {
        sortlist[i] = sortlist[i - 1];
      }
      // move to first one
      sortlist[i] = tmp;
    }

    // put new sortlist to preference
    String str = "";
    i = 0;
    for (; i < sortlist.length - 1; i++) {
      str += sortlist[i] + "-";
    }
    str += sortlist[sortlist.length - 1];
    return str;
  }

  public static void setLastMemory(Context context) {

    SharedPreferences sp = context.getSharedPreferences(DIVX_LAST, 0);
    int count = sp.getInt(DIVX_COUNT, 0);
    if (count == 0) {
      MtkLog.i(TAG, "count ==:" + count);
      return;
    }
    long id = getDivxLastMemoryFileID(context);
    long haveId = -1;
    int i = 0;
    int list[] = getListCount(sp, count);
    for (; i < count; i++) {
      MtkLog.i(TAG, "list[" + i + "]:--" + list[i]);
      haveId = sp.getLong(DIVX_PLAYID + list[i], -2);
      if (haveId == id) {
        break;
      }
    }
    MtkLog.i(TAG, "setLastMemory haveId:" + haveId + "----id:" + id + "--count:" + count + " pos:"
        + i);
    if (haveId == id) {
      if (isLastMemoryPosition(context)) {
        recoverPosition(sp, context, list, i);
      } else {
        recoverPosition(sp, context, list, i);
//        recoverTime(sp, context, list, i);
      }
      // clean lastmemory , else it will take effect,after playdone this time
      writeOverride(sp, count - 1, list[i]);
    }
  }

  private static void recoverPosition(SharedPreferences sp, Context context, int[] list, int i) {

    DivxLastMemoryFilePosition position = getMemoryInfo(context, sp, list[i]);
    dumpDivxPositionInfo(position, context);
    if (position != null) {
      // recover lastmemory
      setDivxLastMemoryFilePosition(context, position);
    }
  }

  public static int setDivxLastMemoryFilePosition(Context context,
      DivxLastMemoryFilePosition info) {
    return LogicManager.getInstance(context).setDivxLastMemoryFilePosition(info);
  }

  private static void recoverTime(SharedPreferences sp, Context context, int[] list, int i) {
    int time = (int) getMemoryTime(context, sp, list[i]);
    MtkLog.i(TAG, "time:" + time);
    DivxLastMemoryFilePosition positon = LogicManager.getInstance(context)
        .getDivxLastMemoryFilePosition();
    dumpDivxPositionInfo(positon, context);
    positon.ui4_timestap = time;
    setDivxLastMemoryFilePosition(context, positon);
//    LogicManager.getInstance(context).seekLastMemory(time);
  }

  private static void writeOverride(SharedPreferences sp, int count, int i) {
    // TODO Auto-generated method stub
    String sort = getSortString(sp, i, true);
    SharedPreferences.Editor edit = sp.edit();
    edit.putInt(DIVX_COUNT, count);
    edit.putString(DIVX_SORT_LIST, sort);
    edit.commit();

  }

  public static DivxLastMemoryFilePosition getMemoryInfo(Context context, SharedPreferences sp,
      int position) {
    MtkLog.d(TAG, "getMemoryInfo ~position = " + position);

    DivxLastMemoryFilePosition file = new DivxLastMemoryFilePosition(
        sp.getLong(DIVX_UI8_CHIP_ID + position, -1),
        sp.getInt(DIVX_UI4_TITLE_IDX + position, -1),
        sp.getInt(DIVX_UI4_PLAYLIST_IDX + position, -1),
        sp.getInt(DIVX_UI4_CHAP_IDX + position, -1),
        sp.getInt(DIVX_UI2_AUD_IDX + position, -1),
        sp.getInt(DIVX_UI2_SUB_IDX + position, -1),
        sp.getLong(DIVX_UI8_AUD_PTS_INFO + position, -1),
        sp.getLong(DIVX_UI8_AUD_FRAME_POSITION + position, -1),
        sp.getLong(DIVX_UI8_I_PTS_INFO + position, -1),
        sp.getLong(DIVX_UI8_PTS_INFO + position, -1),
        sp.getLong(DIVX_UI8_I_FRAME_POSITION + position, -1),
        sp.getLong(DIVX_UI8_FRAME_POSITION + position, -1),
        sp.getInt(DIVX_I4_TEMPORAL_REFERENCE + position, -1),
        sp.getInt(DIVX_UI2_DECODING_ORDER + position, -1),
        sp.getLong(DIVX_UI8_STC + position, -1),
        sp.getLong(DIVX_UI8_FRAME_POSITION_DISP + position, -1),
        sp.getInt(DIVX_UI4_TIMESTAP + position, -1)
        );
    // dumpDivxPositionInfo(file,context);
    return file;

  }

  public static long getMemoryTime(Context context, SharedPreferences sp, int position) {
    MtkLog.d(TAG, "getMemoryTime ~position = " + position);
    long time = sp.getLong(LAST_MEMORY_TIME + position, -1);
    return time;
  }

  public static void clearLastMemory(Context context) {
    SharedPreferences sp = context.getSharedPreferences(DIVX_LAST, 0);
    SharedPreferences.Editor ed = sp.edit();
    ed.putInt(DIVX_COUNT, 0);
    ed.commit();
  }

  private static void dumpDivxPositionInfo(DivxLastMemoryFilePosition pos, Context context) {
    // new Exception().printStackTrace();
    String result = "dumpDivxPositionInfo :\n";
    long id = LogicManager.getInstance(context).getDivxLastMemoryFileID();

    MtkLog.d(TAG, "dumpDivxPositionInfo : id = " + id + " [ui8_clip_id=" + pos.ui8_clip_id
        + ", ui4_title_idx="
        + pos.ui4_title_idx + ", ui4_playlist_idx=" + pos.ui4_playlist_idx
        + ", ui4_chap_idx=" + pos.ui4_chap_idx + ", ui2_aud_idx="
        + pos.ui2_aud_idx + ", ui2_sub_idx=" + pos.ui2_sub_idx
        + ", ui8_aud_pts_info=" + pos.ui8_aud_pts_info
        + ", ui8_aud_frame_position=" + pos.ui8_aud_frame_position
        + ", ui8_i_pts_info=" + pos.ui8_i_pts_info + ", ui8_pts_info="
        + pos.ui8_pts_info + ", ui8_i_frame_position="
        + pos.ui8_i_frame_position + ", ui8_frame_position="
        + pos.ui8_frame_position + ", i4_temporal_reference="
        + pos.i4_temporal_reference + ", ui2_decoding_order="
        + pos.ui2_decoding_order + ", ui8_stc=" + pos.ui8_stc
        + ", ui8_frame_position_disp=" + pos.ui8_frame_position_disp
        + ", ui4_timestap=" + pos.ui4_timestap + "]");

  }
}
