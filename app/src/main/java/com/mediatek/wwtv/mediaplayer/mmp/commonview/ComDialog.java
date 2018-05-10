
package com.mediatek.wwtv.mediaplayer.mmp.commonview;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.AccessibilityDelegate;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.os.Handler;

import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmp.commonview.ListAdapterMenu.MyOffsetListener;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MediaPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.MenuFatherObject;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.ScreenConstant;

public class ComDialog extends Dialog implements OnItemSelectedListener {

  private static final String TAG = "ComDialog";

  private List<MenuFatherObject> mDataList = new ArrayList<MenuFatherObject>();

  private Context mContext;

  protected ListView vListView;
  protected Handler mHandler;
  protected static final int MSG_DISMISS_MENU = 1;
  protected static final int HIDE_DELAYTIME = 10000;

  private int mStyle;

  public int selectPosition;

  protected View selectView;
  protected View oldSelectView;
  protected String mSelectShow;
  private ListAdapterMenu mAdapterMenu;

  private ListView.OnItemClickListener mListener;

  private int menuWidth = 200;

  private int menuHight = 250;

  private int marginY = 50;

  private int marginX = 0;

  protected int mMediaType;
  private final int PAGE_SIZE = ListAdapterMenu.PAGE_SIZE;

  public int mSelection = 0;

  public void setMediaType(int type) {
    mMediaType = type;
  }

  public int getMediaType() {
    return mMediaType;
  }

  public void setBackGround() {
    //SKY luojie modify 20180103 for UI begin
/*
    if (mMediaType == MultiMediaConstant.VIDEO) {
      this.findViewById(R.id.menu_layout).setBackgroundResource(
          R.drawable.popmenu_bg);
    } else {
      this.findViewById(R.id.menu_layout).setBackgroundResource(
          R.drawable.popmenu_bg);
    }
*/
    //SKY luojie modify 20180103 for UI end
  }

  public int getCount() {
    if (null == mDataList) {
      return 0;
    }
    return mDataList.size();
  }

  public int getLastEnableIndex() {
    if (null == mDataList) {
      return 0;
    }
    int count = mDataList.size() - 1;
    for (int i = count; i >= 0; i--) {
      if (mDataList.get(i).enable) {
        return i;
      }
    }
    return 0;
  }

  public boolean isContain(String menuContent) {
    for (MenuFatherObject menuItem : mDataList) {
      if (menuItem.content.equals(menuContent)) {
        return true;
      }
    }
    return false;
  }

  public ComDialog(Context context, int theme) {
    super(context, theme);
  }

  @SuppressWarnings("unchecked")
  public ComDialog(Context context, List<?> list, int style,
      ListView.OnItemClickListener listener) {
    this(context, R.style.dialog);
    mDataList = (List<MenuFatherObject>) list;
    mContext = context;
    mStyle = style;
    mListener = listener;
  }

  private AccessibilityDelegate mAccDelegate = new AccessibilityDelegate() {
      public void sendAccessibilityEvent(View host, int eventType) {
          host.sendAccessibilityEventInternal(eventType);
          MtkLog.d(TAG, "sendAccessibilityEvent." + eventType + "," + host);
      }

    public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child,
          AccessibilityEvent event) {
          MtkLog.d(TAG, "onRequestSendAccessibilityEvent." + host + "," + child + "," + event);
          do {
              if (vListView != host) {
                  MtkLog.d(TAG, ":" + vListView + "," + host);
                  break;
              }

              List<CharSequence> texts = event.getText();
              if (texts == null) {
                  MtkLog.d(TAG, ":" + texts);
                  break;
              }

              //confirm which item is focus
              if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {//move focus
                  int index = findSelectItem(texts.get(0).toString());
                  if (index >= 0) {
                      int selectionIndex = index % PAGE_SIZE;
                      vListView.setSelection(selectionIndex);
                      selectPosition = index;
                  }

                  if (null != mHandler){
                      mHandler.removeMessages(MSG_DISMISS_MENU);
                      mHandler.sendEmptyMessageDelayed(MSG_DISMISS_MENU, HIDE_DELAYTIME);
                  }
              }

          } while(false);

          return host.onRequestSendAccessibilityEventInternal(child, event);
      }

      private int findSelectItem(String text) {
          if(mDataList == null) {
              return -1;
          }

          for(int i = 0; i < mDataList.size(); i++) {
              if(mDataList.get(i).equals(text)) {
                  return i;
              }
          }

          return -1;
      }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mmp_menulistview);
    setBackGround();
    vListView = (ListView) findViewById(R.id.mmp_menulistview_list);
    mAdapterMenu = new ListAdapterMenu(mContext, mDataList, mStyle);
    setListener();
    if (mSelection >= PAGE_SIZE) {
      int offset = mSelection - PAGE_SIZE + 1;
      mSelection = PAGE_SIZE - 1;
      mAdapterMenu.setOffset(offset);
      // int i = mSelection/PAGE_SIZE;
      // mAdapterMenu.setOffset(i*PAGE_SIZE);
      // mSelection = mSelection -i*PAGE_SIZE;
    }
    vListView.setAdapter(mAdapterMenu);
    vListView.setAccessibilityDelegate(mAccDelegate);

    setDialogParams();
    LinearLayout m = (LinearLayout) findViewById(R.id.menu_layout);
    //SKY 20180103 luojie modify for UI bengin
    //m.getBackground().setAlpha(220);
    //SKY 20180103 luojie modify for UI end
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    int keyCode = event.getKeyCode();
    switch (keyCode) {
      case KeyMap.KEYCODE_VOLUME_UP:
      case KeyMap.KEYCODE_VOLUME_DOWN:
        return true;
      default:
        break;
    }

    return super.dispatchKeyEvent(event);
  }

  public void initItem(int position, String content) {
    if (position < mDataList.size() - 1) {
      mDataList.get(position).content = content;
    }
    mAdapterMenu.initList(mDataList);
    mAdapterMenu.notifyDataSetChanged();
  }

  public void addItem(int location, MenuFatherObject obj) {
    mDataList.add(location, obj);
    if (null != mAdapterMenu) {
      mAdapterMenu.notifyDataSetChanged();
    }
  }

  public void setItem(int position, String content) {
    if (position < mDataList.size() - 1) {
      mDataList.get(position).content = content;
    }
    if (null != mAdapterMenu) {
      mAdapterMenu.notifyDataSetChanged();
    }
  }

  public void updateListView() {
    if (null != mAdapterMenu) {
      mAdapterMenu.notifyDataSetChanged();
    }
  }

  public void setItemEnableState(String content, boolean enabled) {
    if (mDataList != null) {
      for (int i = 0; i < mDataList.size(); i++) {
        if (mDataList.get(i).equals(content)) {
          mDataList.get(i).enable = enabled;
          break;
        }
      }
    }
  }

  public int getItemIndex(String content) {
    if (mDataList != null) {
      for (int i = 0; i < mDataList.size(); i++) {
        if (mDataList.get(i).equals(content)) {
          return i;
        }
      }
    }

    return -1;
  }

  // Added by Dan for fix bug DTV00384878
  public void setItemEnabled(int position, boolean enabled) {
    if (position >= 0 && position < mDataList.size()) {
      mDataList.get(position).enable = enabled;
    }
  }

  public void initList(List<MenuFatherObject> list) {
    mDataList = list;
    if (null != mAdapterMenu) {
      mAdapterMenu.initList(mDataList);
      mAdapterMenu.notifyDataSetChanged();
    }
  }

  private void setListener() {
    mAdapterMenu.setMyOffsetListener(new MyOffsetListener() {
      @Override
      public void offset(int offset) {
        selectPosition = vListView.getSelectedItemPosition() + offset;
      }
    });
    vListView.setOnItemSelectedListener(this);
    vListView.setOnItemClickListener(mListener);
  }

  private void setDialogParams() {
    WindowManager m = getWindow().getWindowManager();
    Display display = m.getDefaultDisplay();
    Window window = getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();

    menuWidth = (int) (ScreenConstant.SCREEN_WIDTH * 0.15);
    menuHight = (menuWidth * 4 / 3 + (int) (ScreenConstant.SCREEN_HEIGHT * 0.010));
    marginY = (ScreenConstant.SCREEN_HEIGHT * 3 / 8)
        - (int) (ScreenConstant.SCREEN_HEIGHT * 0.16) - menuHight / 2;
    marginX = (int) (ScreenConstant.SCREEN_WIDTH * 0.05);
	//fix by tjs for change menu position;yi ji caidan
    lp.gravity = Gravity.LEFT|Gravity.TOP;
    lp.width = 479;
    lp.height = 625;

    MtkLog.i(TAG,
        "----------------------- display.getWidth:"
            + ScreenConstant.SCREEN_WIDTH);
     
    lp.x = 61;//ScreenConstant.SCREEN_WIDTH / 2 - ((3 * menuWidth) / 2) - marginX;
    lp.y = 248;//marginY;
	//fix by tjs for change menu position;yi ji caidan
    window.setAttributes(lp);

    // setWindowPosition();
  }

  public void setWindowPosition() {
    WindowManager m = getWindow().getWindowManager();
    Display display = m.getDefaultDisplay();
    Window window = getWindow();
    WindowManager.LayoutParams lp = window.getAttributes();
	//fix by tjs for change menu position;er ji caidan
    lp.x = 544;//ScreenConstant.SCREEN_WIDTH / 2 - menuWidth / 2 - marginX + 1;
    lp.y = 248;//marginY;
	//fix by tjs for change menu position;er ji caidan
    window.setAttributes(lp);
  }

  @Override
  public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
      long arg3) {
    selectPosition = arg2 + mAdapterMenu.getOffset();
    TextView tvTextView = (TextView) arg1
        .findViewById(R.id.mmp_menulist_tv);
	//fix by tjs for change menu focus textview color
    tvTextView.setTextColor(Color.WHITE);
	//fix by tjs for change menu focus textview color
    if (selectView != null && selectView != arg1) {
      selectView.findViewById(R.id.mmp_menulist_img).setVisibility(
          View.INVISIBLE);
		  //fix by tjs for change menu select textview color
      ((TextView) selectView.findViewById(R.id.mmp_menulist_tv))
          .setTextColor(0xffa9a9a9);
		   //fix by tjs for change menu select textview color
    }
    selectView = arg1;
    mSelectShow = tvTextView.getText().toString();
  }

  @Override
  public void onNothingSelected(AdapterView<?> arg0) {
  }

  public void setSelectShowText(String selectShow) {
    mSelectShow = selectShow;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    int position = vListView.getSelectedItemPosition();
    int offset = mAdapterMenu.getOffset();
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_CENTER:
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_RIGHT:
      case KeyMap.KEYCODE_MTKIR_YELLOW:{

        if (selectView != null) {

          if (mDataList.get(position).hasnext) {
            MtkLog.i(
                TAG,
                "position:" + position + "--offset:" + offset + "--hasNext:"
                    + mDataList.get(position).hasnext);
            selectView.findViewById(R.id.mmp_menulist_img).setVisibility(
                View.VISIBLE);
          }
          ((TextView) selectView.findViewById(R.id.mmp_menulist_tv))
              .setTextColor(Color.GREEN);
        }
        break;
      }
      case KeyMap.KEYCODE_MTKIR_RED:
        if (!Util.isTTSEnabled(mContext)){
            break;
        }
        if (position == mAdapterMenu.getCount() - 1) {
          int lastenabled = getLastEnableIndex();
          if (selectPosition >= lastenabled) {
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
          } else {
              mAdapterMenu.setOffset(offset + mAdapterMenu.getCount());
              mAdapterMenu.notifyDataSetChanged();
              vListView.setSelection(0);
          }
        }
        break;

      case KeyMap.KEYCODE_MTKIR_GREEN:
        if (!Util.isTTSEnabled(mContext)){
            break;
        }
        if (selectPosition == PAGE_SIZE){
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(PAGE_SIZE - 1);
            break;
        } else if (selectPosition == 0 && mAdapterMenu.getCount() > PAGE_SIZE) {
          mAdapterMenu.setOffset(PAGE_SIZE);
          mAdapterMenu.notifyDataSetChanged();
          vListView.setSelection(mAdapterMenu.getCount() - 1);
        }
        break;

      case KeyEvent.KEYCODE_DPAD_DOWN:
        MtkLog.i("dialog", "----position:" + position + " offset:" + offset);
        if (position == PAGE_SIZE - 1) {
          int lastenabled = getLastEnableIndex();
//          if (MultiFilesManager.getInstance(mContext).getCurrentSourceType()
//                == MultiFilesManager.SOURCE_DLNA
//              && MultiFilesManager.getInstance(mContext).getContentType()
//                != MultiFilesManager.CONTENT_AUDIO) {
//            lastenabled = getCount() - 3;
//          }
          if (mSelectShow.equals(mDataList.get(lastenabled).content)) {
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
          } else {
            if ((offset + PAGE_SIZE) >= getCount()) {
              mAdapterMenu.setOffset(0);
              mAdapterMenu.notifyDataSetChanged();
              vListView.setSelection(0);
            } else {
              mAdapterMenu.setOffset(offset + 1);
              mAdapterMenu.notifyDataSetChanged();
              vListView.setSelection(PAGE_SIZE - 1);
            }
          }

        } else {
          int lastenabled = getLastEnableIndex();
          MtkLog.i(TAG, "getCount:" + getCount());
//          if (MultiFilesManager.getInstance(mContext).getCurrentSourceType()
//                == MultiFilesManager.SOURCE_DLNA
//              && MultiFilesManager.getInstance(mContext).getContentType()
//                != MultiFilesManager.CONTENT_AUDIO) {
//            lastenabled = getCount() - 3;
//          }
		//Begin==>Modified by yangxiong for solving "IndexOutOfBoundsException for android-M"
          if (mDataList != null && mDataList.size( ) > 0
              && mSelectShow.equals(mDataList.get(lastenabled).content)) {
		//End==>Modified by yangxiong for solving "IndexOutOfBoundsException for android-M"
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
            break;
          }

          if (position == (getCount() - 1 - offset)) {
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
            break;
          }
          for (int i = 0; i < getCount() - 1; i++) {
            if (mSelectShow.equals(mDataList.get(i).content)) {
              if (i == getCount() - 1) {
                mAdapterMenu.setOffset(0);
                mAdapterMenu.notifyDataSetChanged();
                vListView.setSelection(0);
                break;
              } else {
                MtkLog.i("keke", i + "---" + offset + "------2------"
                    + (mAdapterMenu.getCount() - 1));
                while (i <= (getCount() - 2) && !mDataList.get(i + 1).enable) {
                  i++;
                  if (i == getCount() - 2) {
                    break;
                  }
                }

                offset = i + 2 - mAdapterMenu.getCount();
                mAdapterMenu.setOffset(offset);
                mAdapterMenu.notifyDataSetChanged();
                vListView.setSelection(mAdapterMenu.getCount() - 1);
              }
            }
          }

        }
        break;
      case KeyEvent.KEYCODE_DPAD_UP:
        MtkLog.i(TAG, "KEYCODE_DPAD_UP position:" + position + "--offset:" + offset);

        boolean isEnable = false;
        int index = 0;
        if (position > 0) {
          // keep offset ,down postion to 0 for enabled item
          index = position - 1;
          while (index >= 0) {
            if (mDataList.get(index + offset).enable) {
              isEnable = true;
              break;
            }
            index--;
          }

          if (isEnable == true) {
            vListView.setSelection(index);
            return true;
          }
        }

        if (offset > 0) {
          offset--;
          position++;
          mAdapterMenu.setOffset(offset);
          mAdapterMenu.notifyDataSetChanged();
          vListView.setSelection(position);
          return true;
        }
        int size = mDataList.size() - 1;
        index = size;
        while (index >= 0) {
          if (mDataList.get(index).enable) {
            isEnable = true;
            break;
          }
          index--;
        }
        MtkLog.i(TAG, "size:" + size + "--index:" + index);
        if (isEnable) {
          int icount = 1;
          while ((size + 1 - icount * PAGE_SIZE) > index) {
            icount++;
          }
          offset = size + 1 - icount * PAGE_SIZE;
          if (offset < 0) {
            offset = 0;
          }
          mAdapterMenu.setOffset(offset);
          mAdapterMenu.notifyDataSetChanged();
          vListView.setSelection(index - offset);
        } else {
          MtkLog.i(TAG, "up error");
        }
        // if(isEnable == true){
        // mAdapterMenu.setOffset(index);
        // mAdapterMenu.notifyDataSetChanged();
        // vListView.setSelection(0);
        // }else{
        // index = mDataList.size()-1;
        // while(index >=0){
        // if(mDataList.get(index).enable){
        // isEnable = true;
        // break;
        // }
        // index--;
        // }
        // if(isEnable){
        // int icount = 0;
        // while(index > PAGE_SIZE){
        // index = index - PAGE_SIZE;
        // icount++;
        // }
        // mAdapterMenu.setOffset(icount*PAGE_SIZE);
        // mAdapterMenu.notifyDataSetChanged();
        // vListView.setSelection(index);
        // }
        // }

        /*
         * if (position == 0) { if (offset > 0) { while(mDataList.get(offset-1).enable){
         * if(offset==1){ break; } offset--; } offset=offset-1; mAdapterMenu.setOffset(offset);
         * mAdapterMenu.notifyDataSetChanged(); vListView.setSelection(0); } else { if (getCount() >
         * PAGE_SIZE) { offset = getCount() - PAGE_SIZE; mAdapterMenu.setOffset(offset);
         * mAdapterMenu.notifyDataSetChanged(); int temp = PAGE_SIZE-1; for(;temp>0;temp--){
         * if(mDataList.get(temp+offset).enable){ break; } } vListView.setSelection(temp); } else {
         * vListView.setSelection(getCount() - 1); } } } else { for (int i = (offset + position -
         * 1); i > 0; i--) { if (mDataList.get(i).enable && i > offset) { vListView.setSelection(i -
         * offset); break; } else if (mDataList.get(i).enable && i <= offset) { offset--;
         * mAdapterMenu.setOffset(offset); mAdapterMenu.notifyDataSetChanged();
         * vListView.setSelection(0); break; } } }
         */
        break;
      case KeyMap.KEYCODE_MTKIR_CHUP:
        onCHKeyDown(KeyMap.KEYCODE_MTKIR_CHUP);
        break;
      case KeyMap.KEYCODE_MTKIR_CHDN:
        onCHKeyDown(KeyMap.KEYCODE_MTKIR_CHDN);
        break;
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        // dismiss();
        // Util.exitMmpActivity(mContext);
        break;
      case KeyMap.KEYCODE_MTKIR_NEXT:
      case KeyMap.KEYCODE_MTKIR_PREVIOUS:
      case KeyMap.KEYCODE_MTKIR_PLAYPAUSE:
        if (null != mContext && mContext instanceof MediaPlayActivity) {
          ((MediaPlayActivity) mContext).onKeyDown(keyCode, event);
          return true;
        }
      default:
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  public void init() {
    if (selectView != null){
    selectView.findViewById(R.id.mmp_menulist_img).setVisibility(
        View.INVISIBLE);
    ((TextView) selectView.findViewById(R.id.mmp_menulist_tv))
        .setTextColor(Color.WHITE);
     }
  }

  // add by keke fix DTV00384939
  private void onCHKeyDown(int keycode) {
    int position = vListView.getSelectedItemPosition();
    int offset = mAdapterMenu.getOffset();

    if (keycode == KeyMap.KEYCODE_MTKIR_CHUP) {

      if (position == 0) {

        if (getCount() > PAGE_SIZE && offset != PAGE_SIZE) {

          if (offset == 0) {
            offset = PAGE_SIZE;
            mAdapterMenu.setOffset(offset);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
          } else {
            mAdapterMenu.setOffset(0);
            mAdapterMenu.notifyDataSetChanged();
            vListView.setSelection(0);
          }

        } else if (offset == PAGE_SIZE) {
          mAdapterMenu.setOffset(0);
          mAdapterMenu.notifyDataSetChanged();
          vListView.setSelection(0);
        }

      } else {
        for (int j = offset; j < getCount() - 1; j++) {
          if (mDataList.get(j).enable) {
            if ((j - offset) < position) {
              vListView.setSelection(j - offset);
              return;
            } else {
              break;
            }

          }
        }
        mAdapterMenu.setOffset(0);
        mAdapterMenu.notifyDataSetChanged();
        vListView.setSelection(0);
      }

    } else if (keycode == KeyMap.KEYCODE_MTKIR_CHDN) {

      if (position == (PAGE_SIZE - 1)) {
        if (getCount() > PAGE_SIZE) {
          offset = PAGE_SIZE;
          mAdapterMenu.setOffset(offset);
          mAdapterMenu.notifyDataSetChanged();
          vListView.setSelection(getCount() - offset - 1);
        }
      } else {
        if (position == (getCount() - offset - 1)) {
          MtkLog.i("keke", "--1---" + (getCount() - offset - 1));
          offset = 0;
          mAdapterMenu.setOffset(offset);
          mAdapterMenu.notifyDataSetChanged();
          position = mAdapterMenu.getCount() - 1;
          while (!mDataList.get(position).enable) {
            position--;
            if (position <= 0) {
              break;
            }
          }
          MtkLog.i("keke", "---2--" + position);
          vListView.setSelection(position);
        } else {
          if (getCount() > PAGE_SIZE) {
            int mcount = offset + mAdapterMenu.getCount() - 1;
            while (!mDataList.get(mcount).enable) {
              mcount--;
            }
            if (mcount == position) {
              offset = PAGE_SIZE;
              mAdapterMenu.setOffset(offset);
              mAdapterMenu.notifyDataSetChanged();
              vListView.setSelection(getCount() - offset - 1);
            } else {
              vListView.setSelection(mcount - offset);
            }
          } else {
            vListView.setSelection(getCount() - 1);
          }
        }
      }
    }

  }
}
