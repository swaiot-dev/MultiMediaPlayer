
package com.mediatek.wwtv.mediaplayer.mmp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import android.content.Context;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.lrcObject;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;

import com.mediatek.mmp.util.DivxTitleInfo;
import com.mediatek.mmp.util.DivxChapInfo;
import com.mediatek.mmp.util.DivxPlayListInfo;
import com.mediatek.mmp.util.DivxPositionInfo;

public class GetDataImp {
  private static final String TAG = "GetDataImp";
  // private static final Random RNG = new Random();
  int[] rawArray = new int[4];
  private static Vector<lrcObject> lrc_map;

  // Added by Dan for fix bug DTV00389330
  private boolean mIsInLrcOffsetMenu = false;
  private boolean mIsInEncodingMenu;

  private static GetDataImp imp = new GetDataImp();

  private GetDataImp() {
  };

  public static GetDataImp getInstance() {
    return imp;
  }

  public List<File> getChildList(String path) {
    List<File> mFileList = new ArrayList<File>();
    File file = new File(path);
    MtkLog.d(TAG, file.getPath());
    if (file == null || !file.exists()) {
      return mFileList;
    }
    File[] files = file.listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        mFileList.add(files[i]);
      }
    }
    return mFileList;
  }

  public List<File> getParentList(String path) {
    List<File> mFileList = new ArrayList<File>();
    File file = new File(path);
    if (file == null || !file.exists()) {
      return mFileList;
    }
    File parentfile = file.getParentFile();
    if (parentfile != null) {
      mFileList = getChildList(parentfile.getPath());
    }
    return mFileList;
  }

  /*
   * public int[] randArr() { int[] rawArray = new int[8]; for (int i = 0; i < rawArray.length; i++)
   * { rawArray[i] = RNG.nextInt(7); } return rawArray; }
   */

  public ArrayList<MenuFatherObject> getComMenu(Context context,
      int contentresid, int enableresid, int hasnextresid) {
    // Added by Dan for fix bug DTV00389330
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();

    String[] menucontent = context.getResources().getStringArray(
        contentresid);
    int[] menuenable = context.getResources().getIntArray(enableresid);
    int[] menuhasnext = context.getResources().getIntArray(hasnextresid);
    for (int i = 0; i < menuenable.length; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = menucontent[i];
      object.enable = menuenable[i] == 1 ? true : false;
      object.hasnext = menuhasnext[i] == 1 ? true : false;
      MtkLog.d(TAG, "object.hasnext" + object.hasnext + " menuhasnext[i]"
          + menuhasnext[i]);
      if (object.content.equals(context.getString(R.string.mmp_divx_title))
          || object.content.equals(context.getString(R.string.mmp_divx_edition))
          || object.content.equals(context.getString(R.string.mmp_divx_chapter))) {
        if (!DivxUtil.checkDivxEnable(context, object.content)) {
          object.enable = false;
          object.hasnext = false;
        }
      }
      menuList.add(object);
    }
    return menuList;
  }

  public ArrayList<MenuFatherObject> getChildList(Context mContext,
      String content) {
    // Added by Dan for fix bug DTV00389330
    mIsInLrcOffsetMenu = false;
    mIsInEncodingMenu = false;
    ArrayList<MenuFatherObject> mList = new ArrayList<MenuFatherObject>();
    int contentresid = R.array.mmp_menu_sortlist_photoortext;
    int enableresid = R.array.mmp_menu_sortlist_enable;
    MtkLog.d(TAG, "content:" + content);
    if (content.equals(mContext.getString(R.string.mmp_menu_sort))) {

      enableresid = R.array.mmp_menu_sortlist_enable;
      int type = MultiFilesManager.getInstance(mContext).getContentType();
      switch (type) {
        case MultiMediaConstant.PHOTO:
          contentresid = R.array.mmp_menu_sortlist_video;
          break;
        case MultiMediaConstant.TEXT: {
          contentresid = R.array.mmp_menu_sortlist_photoortext;
          break;
        }
        case MultiMediaConstant.AUDIO: {
//          if (MultiFilesManager.getInstance(mContext).getCurrentSourceType()
//              == MultiFilesManager.SOURCE_DLNA) {
            contentresid = R.array.mmp_menu_sortlist_audio_dlna;
//          } else {
//            contentresid = R.array.mmp_menu_sortlist_audio;
//          }
          break;
        }
        case MultiMediaConstant.VIDEO: {
          contentresid = R.array.mmp_menu_sortlist_video;
          break;
        }

        default:
          break;
      }

    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_mediatype))) {
      contentresid = R.array.mmp_menu_mediatypelist;
//    if (Util.isUseExoPlayer()) {
//      enableresid = R.array.mmp_menu_mediatypelist_enable_exo;
//    } else {
        enableresid = R.array.mmp_menu_mediatypelist_enable;
//    }
    } else if (content.equals(mContext.getString(R.string.mmp_menu_thumb))) {
      contentresid = R.array.mmp_menu_thumblist;
      enableresid = R.array.mmp_menu_thumblist_enable;

    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_photoframe))) {
      contentresid = R.array.mmp_menu_photoframelist;
      enableresid = R.array.mmp_menu_photoframelist_enable;
      mList = getChildComMenu(mContext, contentresid, enableresid);
      String name = MultiFilesManager.getInstance(mContext)
          .getCurDevName();
      if (null != name) {
        MenuFatherObject object = new MenuFatherObject();
        object.content = name;
        object.enable = true;
        mList.add(object);
      }
      return mList;

    } else if (content.equals(mContext.getString(R.string.mmp_menu_repeat))) {
      contentresid = R.array.mmp_menu_repeatlist;
      enableresid = R.array.mmp_menu_repeatlist_enable;
    } else if (content
        .equals(mContext.getString(R.string.mmp_menu_shuffle))) {
      contentresid = R.array.mmp_menu_shufflelist;
      enableresid = R.array.mmp_menu_shufflelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_duration))) {
      contentresid = R.array.mmp_menu_durationlist;
      enableresid = R.array.mmp_menu_durationlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_effect))) {
      contentresid = R.array.mmp_menu_effectlist;
      enableresid = R.array.mmp_menu_effectlist_enable;
    } else if (content
        .equals(mContext.getString(R.string.mmp_menu_display))) {
      contentresid = R.array.mmp_menu_displaylinelist;
      enableresid = R.array.mmp_menu_displaylinelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_timeoffset))) {
      contentresid = R.array.mmp_menu_timeoffsetlist;
      enableresid = R.array.mmp_menu_timeoffsetlist_enable;
      // Added by Dan for fix bug DTV00389330
      mIsInLrcOffsetMenu = true;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_encoding))) {
      contentresid = R.array.mmp_menu_encodinglist;
      enableresid = R.array.mmp_menu_encodinglist_enable;
      mIsInEncodingMenu = true;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_picturemode))) {
      contentresid = R.array.mmp_menu_picturemodelist;
      enableresid = R.array.mmp_menu_picturemodelist_enable;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_ts_program))) {
      String[] programs = LogicManager.getInstance(mContext)
          .getTSVideoProgramList();
      for (int i = 0; i < programs.length; i++) {
        MenuFatherObject object = new MenuFatherObject();
        object.content = programs[i];
        object.enable = true;
        mList.add(object);
      }
      return mList;
    } else if (content.equals(mContext
        .getString(R.string.mmp_menu_screenmode))) {
      // TODO screen mode
       //begin == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
      LogicManager.curMenuListType = LogicManager.MENULIST_SCREEN_MODE;
      //end == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
      int[] modes = LogicManager.getInstance(mContext)
          .getAvailableScreenMode();
      String[] strModes = mContext.getResources().getStringArray(
          R.array.mmp_menu_screenmodelist);
      for (int i = 0, length = modes.length; i < length; i++) {
        if (modes[i] >= 0) {
          MenuFatherObject object = new MenuFatherObject();
          object.content = strModes[i];
          object.enable = true;
          mList.add(object);
        }
      }
      return mList;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_size))) {
      contentresid = R.array.mmp_menu_sizelist;
      enableresid = R.array.mmp_menu_sizelist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_style))) {
      contentresid = R.array.mmp_menu_stylelist;
      enableresid = R.array.mmp_menu_stylelist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_color))) {
      contentresid = R.array.mmp_menu_colorlist;
      enableresid = R.array.mmp_menu_colorlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_menu_zoom))) {
      contentresid = R.array.mmp_menu_zoomlist;
      enableresid = R.array.mmp_menu_zoomlist_enable;
    } else if (content.equals(mContext.getString(R.string.mmp_last_memory))) {

      contentresid = R.array.mmp_lastmemory_array;
      enableresid = R.array.mmp_lastmemory_array_enable;
      MtkLog.i(TAG, "content: Last Memory contentresid:" + contentresid + " enableresid:"
          + enableresid);
    } else if (content.equals(mContext.getString(R.string.mmp_subtitle_encoding))) {
      //begin == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
      LogicManager.curMenuListType = LogicManager.MENULIST_SUBTTILE_ENCODING;
      //end == >modified by yangxiong for  solving the auto option is invalid if luanguage is not english;
      contentresid = R.array.mmp_subtitle_encoding_array;
      enableresid = R.array.mmp_subtitle_encoding_array_enable;
    }else {
      if (content.equals(mContext.getString(R.string.mmp_divx_title))
          || content.equals(mContext.getString(R.string.mmp_divx_edition))
          || content.equals(mContext.getString(R.string.mmp_divx_chapter))) {
        mList = getChildComMenu(mContext, content);
        return mList;
      }
    }
    mList = getChildComMenu(mContext, contentresid, enableresid);

    return mList;
  }

  private ArrayList<MenuFatherObject> getChildComMenu(Context context, String content) {
    ArrayList<MenuFatherObject> menuList;
    int size = 0;
    if (content.equals(context.getString(R.string.mmp_divx_title))) {
      size = DivxUtil.getDivxTitleNum(context);
      // LogicManager.getInstance(context).getDivxTitleNum();
    } else if (content.equals(context.getString(R.string.mmp_divx_edition))) {
      // MtkLog.i(TAG,"getDivxPositionInfo");
      // DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
      // MtkLog.i(TAG,"getDivxPositionInfo u4TitleIdx:"+positioninfo.u4TitleIdx);
      // DivxTitleInfo titleinfo =
      // LogicManager.getInstance(context).getDivxTitleInfo(positioninfo.u4TitleIdx);
      // MtkLog.i(TAG,"getDivxPositionInfo titleinfo:"+titleinfo);
      // if(titleinfo == null){
      // size = titleinfo.u4PlaylistNum;
      // }
      // MtkLog.i(TAG,"playlistNum:"+titleinfo.u4PlaylistNum);
      size = DivxUtil.getDivxEditionNum(context);
    } else {
      DivxPositionInfo positioninfo = LogicManager.getInstance(context).getDivxPositionInfo();
      DivxPlayListInfo playlistinfo = LogicManager.getInstance(context).getDivxPlayListInfo(
          positioninfo.u4TitleIdx, positioninfo.u4PlaylistIdx);
      size = playlistinfo.u4ChapterNum;
      MtkLog.i(TAG, "u4ChapterNum:" + playlistinfo.u4ChapterNum);
    }
    menuList = createChildList(content, size);
    return menuList;
  }

  private ArrayList<MenuFatherObject> createChildList(String content, int size) {

    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();
    for (int i = 0; i < size; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = content + "_" + String.valueOf(i + 1);
      object.enable = true;
      menuList.add(object);
    }
    return menuList;
  }

  private ArrayList<MenuFatherObject> getChildComMenu(Context context,
      int contentresid, int enableresid) {
    ArrayList<MenuFatherObject> menuList = new ArrayList<MenuFatherObject>();

    String[] menucontent = context.getResources().getStringArray(
        contentresid);
    int[] menuenable = context.getResources().getIntArray(enableresid);
    for (int i = 0; i < menucontent.length; i++) {
      MenuFatherObject object = new MenuFatherObject();
      object.content = menucontent[i];
      object.enable = menuenable[i] == 1 ? true : false;
      menuList.add(object);
    }
    return menuList;
  }

  public Vector<lrcObject> read(String file, Context context) {
    lrc_map = new Vector<lrcObject>();
    Vector<lrcObject> lrc_read = new Vector<lrcObject>();
    String data;
    try {
      // File saveFile = new File(file);
      // FileInputStream stream = new FileInputStream(saveFile);
      InputStreamReader stream = new InputStreamReader(context
          .getResources().getAssets().open(file), "GB2312");
      // context.getResources().getAssets().open(file);
      BufferedReader br = new BufferedReader(stream);
      int i = 0;
      while ((data = br.readLine()) != null) {
        data = data.replace("[", "");
        data = data.replace("]", "@");

        String splitdata[] = data.split("@");
        String lrcContenet = splitdata[splitdata.length - 1];
        for (int j = 0; j < splitdata.length - 1; j++) {
          String tmpstr = splitdata[j];

          tmpstr = tmpstr.replace(":", ".");
          tmpstr = tmpstr.replace(".", "@");
          String timedata[] = tmpstr.split("@");

          Long m = Long.parseLong(timedata[0]); //
          Long s = Long.parseLong(timedata[1]); //
          Long ms = Long.parseLong(timedata[2]); //
          int currTime = (int) ((m * 60 + s) * 1000 + ms * 10);
          lrcObject item1 = new lrcObject();

          item1.begintime = currTime;
          item1.lrc = lrcContenet;
          // lrc_read.put(currTime, item1);//
          lrc_read.add(item1);
          i++;
        }

      }
      stream.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    lrc_map.clear();
    // data = "";
    lrcObject oldval = null;
    int i = 0;
    StringBuffer sb = new StringBuffer();
    for (int j = 0; j < lrc_read.size(); j++) {
      lrcObject val = lrc_read.get(i);
      if (oldval == null)
        oldval = val;
      else {
        lrcObject item1 = new lrcObject();
        item1 = oldval;
        item1.timeline = val.begintime - oldval.begintime;
        lrc_map.add(item1);
        sb.append(String.format("[%04d]-[%04d]-%s\n", item1.begintime,
            item1.timeline, item1.lrc));
        i++;
        oldval = val;
      }
    }
    // data = sb.toString();
    return lrc_map;
  }

  // Added by Dan for fix bug DTV00389330
  public boolean isInLrcOffsetMenu() {
    return mIsInLrcOffsetMenu;
  }

  public boolean isInEncodingMenu() {
    return mIsInEncodingMenu;
  }
}
