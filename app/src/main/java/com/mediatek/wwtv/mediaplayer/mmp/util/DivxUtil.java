
package com.mediatek.wwtv.mediaplayer.mmp.util;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.setting.util.TVContent;
import com.mediatek.wwtv.mediaplayer.util.MarketRegionInfo;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
import com.mediatek.wwtv.mediaplayer.mmpcm.mmcimpl.Const;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;
import com.mediatek.mmp.util.DivxDisplayInfo;
import com.mediatek.mmp.util.DivxLastMemoryFilePosition;

import android.os.SystemProperties;

public class DivxUtil {
  public static String TAG = "DivxUtil";

  public static final int DIGITAL = 12;
  public static final int NEXT = 13;
  public static final int PRE = 14;

  public static final int TITLE_TYPE = 1;
  public static final int CHAPTER_TYPE = 2;
  public static final int EDITION_TYPE = 3;

  public static final int TITLE_NAME = 1;
  public static final int TITLE_RATE = 2;

  public static final int EDITION_NAME = 3;
  public static final int EDITION_RATE = 4;

  public static final int CHAPTER_NAME = 5;

  public static final int AUDIO_TRACK_FORMAT = 6;
  public static final int AUDIO_TRACK_CHANNEL_CONF = 7;
  public static final int AUDIO_TRACK_LANGUAGE = 8;
  public static final int AUDIO_TRACK_NAME = 9;

  public static final int SUBTITLE_TRACK_LANGUAGE = 10;
  public static final int SUBTITLE_TRACK_NAME = 11;

  /*
   * ifeq "$(DIVX_PLUS_SUPPORT)" "true" PRODUCT_PROPERTY_OVERRIDES +=ro.mtk.system.divx.existed=1
   * else PRODUCT_PROPERTY_OVERRIDES +=ro.mtk.system.divx.existed=0 endif
   */

  public static final String MTK_DIVX_SUPPORT = "ro.mtk.system.divx.existed";// "mtk_divx_support";

  /*
   * public static final String DIVX_LAST="divx_last"; public static final String
   * DIVX_COUNT="divx_count"; public static final String DIVX_UI8_CHIP_ID="ui8_clip_id"; public
   * static final String DIVX_UI4_TITLE_IDX="ui4_title_idx"; public static final String
   * DIVX_UI4_PLAYLIST_IDX="ui4_playlist_idx"; public static final String
   * DIVX_UI4_CHAP_IDX="ui4_chap_idx"; public static final String DIVX_UI2_AUD_IDX="ui2_aud_idx";
   * public static final String DIVX_UI2_SUB_IDX="ui2_sub_idx"; public static final String
   * DIVX_UI8_AUD_PTS_INFO="ui8_aud_pts_info"; public static final String
   * DIVX_UI8_AUD_FRAME_POSITION="ui8_aud_frame_position"; public static final String
   * DIVX_UI8_I_PTS_INFO="ui8_i_pts_info"; public static final String
   * DIVX_UI8_PTS_INFO="ui8_pts_info"; public static final String
   * DIVX_UI8_I_FRAME_POSITION="ui8_i_frame_position"; public static final String
   * DIVX_UI8_FRAME_POSITION="ui8_frame_position"; public static final String
   * DIVX_I4_TEMPORAL_REFERENCE="i4_temporal_reference"; public static final String
   * DIVX_UI2_DECODING_ORDER="ui2_decoding_order"; public static final String
   * DIVX_UI8_STC="ui8_stc"; public static final String
   * DIVX_UI8_FRAME_POSITION_DISP="ui8_frame_position_disp"; public static final String
   * DIVX_UI4_TIMESTAP="ui4_timestap";
   */

  private static Map<String, DivxInfo> mDivxInfo = new HashMap();

  /*
   * get Title num
   */
  public static int getDivxTitleNum(Context context) {
    int size = 0;
    int length = LogicManager.getInstance(context).getDivxTitleNum();
    MtkLog.i(TAG, "length:" + length);
    for (int i = 0; i < length; i++) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(i);

      if (titleinfo != null && titleinfo.fgHidden == 0) {
        MtkLog.i(TAG, "i:" + i + "titleinfo != null");
        size++;
      } else {
        if (titleinfo == null) {
          MtkLog.i(TAG, "i:" + i + " titleinfo == null");
        } else {
          MtkLog.i(TAG, "i:" + i + "titleinfo.fgHidden :" + titleinfo.fgHidden);
        }
      }
    }
    return size;
  }

  /*
   * according to current index,to get next index
   */
  public static int getDivxTitleIndex(Context context, int index) {
    int titleindex = index;
    int length = LogicManager.getInstance(context).getDivxTitleNum();
    int order = 0;
    int i = 0;
    for (; i < length; i++) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(i);
      if (titleinfo != null && titleinfo.fgHidden == 0) {
        // if(order == titleindex){
        // break;
        // }
        order++;
        if (order == titleindex) {
          break;
        }
      }
    }
    titleindex = i;
    if (titleindex >= length) {
      titleindex = -1;
    }

    MtkLog.i(TAG, "index:" + index + " titleindex:" + titleindex);
    return titleindex;

  }

  /*
   * according to current index,to get next index
   */
  public static int getDivNextTitleIndex(Context context, int index) {
    int titleindex = index;
    int length = LogicManager.getInstance(context).getDivxTitleNum();
    int order = 0;
    int i = 0;
    for (; i < length; i++) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(i);
      if (titleinfo != null && titleinfo.fgHidden == 0) {
        if (order == titleindex) {
          break;
        }
        order++;
      }
    }
    titleindex = i;
    if (titleindex >= length || titleindex == 0) {
      titleindex = -1;
    }

    MtkLog.i(TAG, "index:" + index + " titleindex:" + titleindex + " length:" + length);
    return titleindex;

  }

  /*
   * according to current index,to get next index
   */
  public static int getDivxNextTitleIndex(Context context, int index) {
    int length = LogicManager.getInstance(context).getDivxTitleNum();
    int i = index + 1;
    for (; i < length; i++) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(i);
      if (titleinfo != null && titleinfo.fgHidden == 0) {
        break;
      } else {
        MtkLog.i(TAG, "titleinfo is null or titleinfo is hidden:" + titleinfo);
      }
    }
    if (i >= length) {
      i = -1;
    }

    MtkLog.i(TAG, "index:" + index + " i:" + i + " length:" + length);
    return i;

  }

  public static int getDivxEditionNum(Context context) {
    int size = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(
          positioninfo.u4TitleIdx);
      int num = titleinfo.u4PlaylistNum;
      MtkLog.i(TAG, "titleinfo.u4PlaylistNum:" + num);
      DivxPlayListInfo listinfo = null;
      for (int i = 0; i < num; i++) {
        listinfo = LogicManager.getInstance(context)
            .getDivxPlayListInfo(positioninfo.u4TitleIdx, i);
        if (listinfo != null && listinfo.fgHidden == 0) {
          size++;
        }
      }
    }
    MtkLog.i(TAG, "getDivxEditionNum:" + size);
    return size;
  }

  public static int getDivxEditionIndex(Context context, int index) {
    int editionindex = index;
    int length = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(
          positioninfo.u4TitleIdx);
      if (titleinfo != null) {
        length = titleinfo.u4PlaylistNum;
      }
    }
    int order = 0;
    int i = 0;
    for (; i < length; i++) {
      DivxPlayListInfo playlistinfo = LogicManager.getInstance(context).getDivxPlayListInfo(
          positioninfo.u4TitleIdx, i);
      ;
      if (playlistinfo != null && playlistinfo.fgHidden == 0) {
        order++;
        if (order == editionindex) {
          break;
        }
      }
    }
    editionindex = i;
    MtkLog.i(TAG, "index:" + index + " editionindex:" + editionindex);
    return editionindex;
  }

  public static int getDivxChapterNum(Context context) {
    int size = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      DivxPlayListInfo listinfo = LogicManager.getInstance(context).getDivxPlayListInfo(
          positioninfo.u4TitleIdx, positioninfo.u4PlaylistIdx);
      int num = listinfo.u4ChapterNum;
      MtkLog.i(TAG, "listinfo.u4ChapterNum:" + num);
      DivxChapInfo chapterinfo = null;
      for (int i = 0; i < num; i++) {
        chapterinfo = LogicManager.getInstance(context).getDivxChapInfo(positioninfo.u4TitleIdx,
            positioninfo.u4PlaylistIdx, i);
        if (chapterinfo != null && chapterinfo.fgHidden == 0) {
          size++;
        }
      }
    }
    MtkLog.i(TAG, "getDivxChapterNum:" + size);
    return size;
  }

  public static int getDivxChapterIndex(Context context, int index) {
    int chapterindex = index;
    int length = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      DivxPlayListInfo playlistinfo = LogicManager.getInstance(context).getDivxPlayListInfo(
          positioninfo.u4TitleIdx, positioninfo.u4PlaylistIdx);
      if (playlistinfo != null) {
        length = playlistinfo.u4ChapterNum;
      }
    }
    int order = 0;
    int i = 0;
    for (; i < length; i++) {
      DivxChapInfo chapterinfo = LogicManager.getInstance(context).getDivxChapInfo(
          positioninfo.u4TitleIdx, positioninfo.u4PlaylistIdx, i);
      ;
      if (chapterinfo != null && chapterinfo.fgHidden == 0) {
        order++;
        if (order == chapterindex) {
          break;
        }
      }
    }
    chapterindex = i;
    MtkLog.i(TAG, "index:" + index + " chapterindex:" + chapterindex);
    return chapterindex;
  }

  public static boolean checkDivxEnable(Context context, String content) {
    boolean isEnabled = true;
    if (content.equals(context.getString(R.string.mmp_divx_title))) {
      isEnabled = checkTitleDivxEnable(context);
    } else if (content.equals(context.getString(R.string.mmp_divx_edition))) {
      isEnabled = checkEditionDivxEnable(context);
    } else {
      isEnabled = checkChapterDivxEnable(context);
    }
    return isEnabled;
  }

  public static boolean checkTitleDivxEnable(Context context) {
    long fd = getDivxLastMemoryFileID(context);
    String id = String.valueOf(fd);
    DivxInfo info = mDivxInfo.get(id);
    if (info == null) {
      info = new DivxInfo();
      mDivxInfo.put(id, info);
      int num = getDivxTitleNum(context);
      info.setTNum(num);
    }

    return info.tEnable();
  }

  public static boolean checkEditionDivxEnable(Context context) {
    long fd = getDivxLastMemoryFileID(context);
    String id = String.valueOf(fd);
    DivxInfo info = mDivxInfo.get(id);
    if (info != null) {
      if (info.tEnable()) {
        if (info.getENum() < 0) {
          int num = getDivxEditionNum(context);
          info.setENum(num);
        }
      } else {
        return false;
      }
      return info.eEnable();
    } else {
      Log.i(TAG, "checkEditionDivxEnable id:" + id
          + "--info == null");
      return false;
    }
  }

  public static boolean checkChapterDivxEnable(Context context) {
    long fd = getDivxLastMemoryFileID(context);
    String id = String.valueOf(fd);
    DivxInfo info = mDivxInfo.get(id);
    if (info != null) {
      if (info.eEnable()) {
        if (info.getCNum() < 0) {
          int num = getDivxChapterNum(context);
          info.setCNum(num);
        }
      } else {
        return false;
      }
      return info.cEnable();
    } else {
      Log.i(TAG, "checkEditionDivxEnable id:" + id
          + "--info == null");
      return false;
    }
  }

  public static int getTitleFocus(Context context) {
    int index = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      index = positioninfo.u4TitleIdx;
    }
    MtkLog.i(TAG, "getTitleFocus index:" + index + "  positioninfo:" + positioninfo);
    int size = 0;
    for (int i = 0; i < index; i++) {
      DivxTitleInfo titleinfo = LogicManager.getInstance(context).getDivxTitleInfo(i);

      if (titleinfo != null && titleinfo.fgHidden == 0) {
        MtkLog.i(TAG, "i:" + i + "titleinfo != null");
        size++;
      } else {
        if (titleinfo == null) {
          MtkLog.i(TAG, "i:" + i + " titleinfo == null");
        } else {
          MtkLog.i(TAG, "i:" + i + "titleinfo.fgHidden :" + titleinfo.fgHidden);
        }
      }
    }
    return size;
  }

  public static int getEditionFocus(Context context) {
    int index = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      index = positioninfo.u4PlaylistIdx;
    }
    int size = 0;
    for (int i = 0; i < index; i++) {
      DivxPlayListInfo listinfo = LogicManager.getInstance(context).getDivxPlayListInfo(
          positioninfo.u4TitleIdx, i);

      if (listinfo != null && listinfo.fgHidden == 0) {
        MtkLog.i(TAG, "i:" + i + "titleinfo != null");
        size++;
      } else {
        if (listinfo == null) {
          MtkLog.i(TAG, "i:" + i + " titleinfo == null");
        } else {
          MtkLog.i(TAG, "i:" + i + "titleinfo.fgHidden :" + listinfo.fgHidden);
        }
      }
    }
    return size;
  }

  public static int getChapterFocus(Context context) {
    int index = 0;
    DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
    if (positioninfo != null) {
      index = positioninfo.u4ChapIdx;
    }
    int size = 0;
    for (int i = 0; i < index; i++) {
      DivxChapInfo chapterinfo = LogicManager.getInstance(context).getDivxChapInfo(
          positioninfo.u4TitleIdx, positioninfo.u4PlaylistIdx, i);
      if (chapterinfo != null && chapterinfo.fgHidden == 0) {
        size++;
      }
    }
    return size;
  }

  public static DivxPositionInfo getDivxPositionInfo(Context context) {
    return LogicManager.getInstance(context).getDivxPositionInfo();

  }

  public static DivxDisplayInfo getDivxDisplayInfo(Context context, int type) {
    DivxPositionInfo positioninfo = getDivxPositionInfo(context);
    if (positioninfo != null) {
      int track = -1;
      if (SUBTITLE_TRACK_LANGUAGE == type || SUBTITLE_TRACK_NAME == type) {
        track = LogicManager.getInstance(context).getSubtitleIndex();
      } else if (AUDIO_TRACK_FORMAT == type
          || AUDIO_TRACK_CHANNEL_CONF == type
          || AUDIO_TRACK_LANGUAGE == type
          || AUDIO_TRACK_NAME == type) {
        track = LogicManager.getInstance(context).getAudioTrackIndex();
      }
      MtkLog.d(TAG, "getDivxDisplayInfo type:" + type + "  track:" + track);
      return LogicManager.getInstance(context).getDivxDisplayInfo(type, positioninfo.u4TitleIdx,
          positioninfo.u4PlaylistIdx, positioninfo.u4ChapIdx, track);
    } else {
      return null;
    }
  }

  public static void saveCurrentPlayInfo(Context context) {
    MtkLog.i(TAG, "saveCurrentPlayInfo");
    if (!isDivxSupport(context)) {
      return;
    }
    // int[] lists = getList(context);
    SharedPreferences sp = context.getSharedPreferences(DIVX_LAST, 0);
    int count = sp.getInt(DIVX_COUNT, 0);
    long id = getDivxLastMemoryFileID(context);

    /*
     * if(count < 5){ long haveId = -1; for(int i = 0;i <count;i++){ haveId =
     * sp.getLong(DIVX_PLAYID+i, -2); if(haveId == id){ return; } } if(haveId != id){
     * writePrefrence(context,sp,count+1,id); } }else{ writePrefrence(context,sp,1,id); }
     */

    // if haved id
    // replae the one ,and move to first
    // if not contain
    // if sount == 5
    // replay latest one
    // else add one to file

    long haveId = -1;
    int i = 0;
    /**this to check id is exsisted or not**/
    int[] list = getListCount(sp, count);
    for (; i < count; i++) {
      haveId = sp.getLong(DIVX_PLAYID + list[i], -2);
      if (haveId == id) {
        break;
      }
    }

    int current = 0;
    MtkLog.i(TAG, "saveCurrentPlayInfo id:" + id + "---haveId:" + haveId + "--count:" + count);
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

    writePrefrence(context, sp, count, id, current);

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

  public static final String DIVX_SORT_LIST = "divx_sort_list";
  public static final String DIVX_LAST = "divx_last";
  public static final String DIVX_COUNT = "divx_count";
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
  public static final String DIVX_REPLACE_ID = "ui4_replace_id";

  /*
   * count:memory size pos: this mark the position saved;
   */
  private static void writePrefrence(Context context, SharedPreferences sp, int count, long id,
      int current) {
    // TODO Auto-generated method stub
    DivxLastMemoryFilePosition position = getDivxLastMemoryFilePosition(context);
    if (position == null) {
      return;
    }

    int pos = getIndex(sp, current);
    String sort = getSortString(sp, pos, false);
    MtkLog.d(TAG, "writePrefrence ~pos =" + pos + "--current" + current + "count:" + count
        + " sortString:" + sort);
    dumpDivxPositionInfo(position, context);
    SharedPreferences.Editor edit = sp.edit();
    edit.putString(DIVX_SORT_LIST, sort);
    edit.putInt(DIVX_COUNT, count);
    edit.putLong(DIVX_PLAYID + pos, id);
    edit.putLong(DIVX_UI8_CHIP_ID + pos, position.ui8_clip_id);
    edit.putInt(DIVX_UI4_TITLE_IDX + pos, position.ui4_title_idx);
    edit.putInt(DIVX_UI4_PLAYLIST_IDX + pos, position.ui4_playlist_idx);
    edit.putInt(DIVX_UI4_CHAP_IDX + pos, position.ui4_chap_idx);
    edit.putInt(DIVX_UI2_AUD_IDX + pos, position.ui2_aud_idx);
    edit.putInt(DIVX_UI2_SUB_IDX + pos, position.ui2_sub_idx);
    edit.putLong(DIVX_UI8_AUD_PTS_INFO + pos, position.ui8_aud_pts_info);
    edit.putLong(DIVX_UI8_AUD_FRAME_POSITION + pos, position.ui8_aud_frame_position);
    edit.putLong(DIVX_UI8_I_PTS_INFO + pos, position.ui8_i_pts_info);
    edit.putLong(DIVX_UI8_PTS_INFO + pos, position.ui8_pts_info);
    edit.putLong(DIVX_UI8_I_FRAME_POSITION + pos, position.ui8_i_frame_position);
    edit.putLong(DIVX_UI8_FRAME_POSITION + pos, position.ui8_frame_position);
    edit.putInt(DIVX_I4_TEMPORAL_REFERENCE + pos, position.i4_temporal_reference);
    edit.putInt(DIVX_UI2_DECODING_ORDER + pos, position.ui2_decoding_order);
    edit.putLong(DIVX_UI8_STC + pos, position.ui8_stc);
    edit.putLong(DIVX_UI8_FRAME_POSITION_DISP + pos, position.ui8_frame_position_disp);
    edit.putInt(DIVX_UI4_TIMESTAP + pos, position.ui4_timestap);
    edit.commit();

    // writeSort(context,current,false);

  }

  private static void dumpDivxPositionInfo(DivxLastMemoryFilePosition pos, Context context) {
    String result = "dumpDivxPositionInfo :\n";
    long id = LogicManager.getInstance(context).getDivxLastMemoryFileID();

    MtkLog.d(TAG, "dumpDivxPositionInfo :\n id = " + id + " [ui8_clip_id=" + pos.ui8_clip_id
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

  /*
   * position:mark the position to get the momeory
   */

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

  private static String getSortString(SharedPreferences sp, int pos, boolean takeoff) {
    // TODO Auto-generated method stub
    // SharedPreferences sp = context.getSharedPreferences(DIVX_SORT, 0);
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
    // SharedPreferences.Editor edit = sp.edit();
    // edit.putString(DIVX_SORT_LIST, str);
    // edit.commit();

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
    dumpDivxPositionInfo(file, context);
    return file;

  }

  /*
   * called to set last_memory position
   */
  public static void setCurrentPlayInfo(Context context) {

    SharedPreferences sp = context.getSharedPreferences(DIVX_LAST, 0);
    int count = sp.getInt(DIVX_COUNT, 0);
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
    MtkLog.i(TAG, "setCurrentPlayInfo haveId:" + haveId + "----id:" + id + "--count:" + count
        + " pos:" + i);
    if (haveId == id) {
      DivxLastMemoryFilePosition position = getMemoryInfo(context, sp, list[i]);
      if (position != null) {
        // recover lastmemory
        setDivxLastMemoryFilePosition(context, position);
      }
      // clean lastmemory , else it will take effect,after playdone this time
      writeOverride(sp, count - 1, list[i]);
    }
  }

  private static int[] getList(SharedPreferences sp) {
    // TODO Auto-generated method stub
    // SharedPreferences sp = context.getSharedPreferences(DIVX_SORT, 0);
    String sortslist = sp.getString(DIVX_SORT_LIST, "0-1-2-3-4");

    // get int array from string
    String[] strs = sortslist.split("-");
    int[] sortlist = new int[strs.length];
    int i = 0;
    for (; i < strs.length; i++) {
      sortlist[i] = Integer.parseInt(strs[i]);
    }
    return sortlist;
  }

  private static void writeOverride(SharedPreferences sp, int count, int i) {
    // TODO Auto-generated method stub
    String sort = getSortString(sp, i, true);
    SharedPreferences.Editor edit = sp.edit();
    edit.putInt(DIVX_COUNT, count);
    edit.putString(DIVX_SORT_LIST, sort);
    edit.commit();

  }

  public static DivxLastMemoryFilePosition getDivxLastMemoryFilePosition(Context context) {
    return LogicManager.getInstance(context).getDivxLastMemoryFilePosition();
  }

  public static long getDivxLastMemoryFileID(Context context) {
    return LogicManager.getInstance(context).getDivxLastMemoryFileID();
  }

  public static int setDivxLastMemoryFilePosition(Context context,
      DivxLastMemoryFilePosition info) {
    return LogicManager.getInstance(context).setDivxLastMemoryFilePosition(info);
  }

  public static boolean isDivxFormatFile(Context context) {
    boolean isDivxFmt = false;

    String path = "";
    if (LogicManager.getInstance(context) != null) {
      path = LogicManager.getInstance(context).getCurrentFileName(Const.FILTER_VIDEO);
      if (LogicManager.getInstance(context).getMediaType() == FileConst.MEDIA_TYPE_MKV) {
        isDivxFmt = true;
      }
    }
    MtkLog.i(TAG, "isDivxFormatFile path:" + path + "  isDivxFmt:" + isDivxFmt);
//    if (path != null) {
//      String fmt = "";
//      int index = path.lastIndexOf(".");
//      if (index >= 0 && index < path.length()) {
//        fmt = path.substring(index + 1);
//        MtkLog.i(TAG, "isDivxFormatFile fmt:" + fmt);
//        for (int i = 0; i < sptFormat.length; i++) {
//          if (fmt.equalsIgnoreCase(sptFormat[i])) {
//            isDivxFmt = true;
//          }
//        }
//      }
//    }

    return isDivxFmt;
  }

  public static String[] sptFormat = {
      "mkv"
  };

  /*
   * when tile one play end,check title_two exsit
   */
  public static int isThereMoreTitleVideo(Context context) {
    int index = -1;
    DivxPositionInfo positioninfo = getDivxPositionInfo(context);
    if (positioninfo != null) {
      // check positioninfo.u4TitleIdx position in title list
      // select next position to play
      index = getDivxNextTitleIndex(context, positioninfo.u4TitleIdx);
    }
    return index;
  }

  private static boolean isDivxSupport = false;

  public static void checkDivxSupport(Context context) {
    isDivxSupport = TVContent.getInstance(context).GetDivXPlusSupport();
  }

  public synchronized static boolean isDivxSupport(Context context) {
    boolean support = false;
    if (MultiFilesManager.getInstance(context).getCurrentSourceType()
        == MultiFilesManager.SOURCE_LOCAL) {
      support = isDivxSupport;
    }

    MtkLog.i(TAG, "isDivxSupport:" + support);

    return support;
  }

  private static boolean mChapterChange = false;

  public static void setChapterChanged(boolean change) {
    mChapterChange = change;
  }

  public static boolean getChapterChanged() {
    return mChapterChange;
  }

  public static boolean isAviAndPlusSupport(Context context) {
    boolean isSupport = false;
    if (LogicManager.getInstance(context) != null) {
      if (LogicManager.getInstance(context).getMediaType() == FileConst.MEDIA_TYPE_AVI) {
        isSupport = true;
      }
    }
//    String path = "";
//    if (LogicManager.getInstance(context) != null) {
//      path = LogicManager.getInstance(context).getCurrentFileName(Const.FILTER_VIDEO);
//    }
//    MtkLog.i(TAG, "isDivxFormatFile path:" + path);
//    if (path != null) {
//      String fmt = "";
//      int index = path.lastIndexOf(".");
//      if (index >= 0 && index < path.length()) {
//        fmt = path.substring(index + 1);
//        MtkLog.i(TAG, "isDivxAviFile fmt:" + fmt);
//        if (fmt.equalsIgnoreCase("avi")) {
//          isSupport = true;
//        }
//
//      }
//    }

    if (!isDivxSupport) {
      isSupport = false;
    }

    return isSupport;
  }

}
