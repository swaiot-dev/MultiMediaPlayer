
package com.mediatek.wwtv.mediaplayer.mmp;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MusicPlayDmrActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayDmrActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayDmrActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayDmrActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.mediaplayer.mmp.util.DmrHelper.iStartListener;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;
//import com.mediatek.wwtv.mediaplayer.mmp.util.DmrUtil;
//import com.mediatek.wwtv.mediaplayer.mmp.util.SaveValue;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.util.KeyMap;
import com.mediatek.wwtv.mediaplayer.util.MmpConst;
import com.mediatek.wwtv.util.MtkLog;
import com.mediatek.wwtv.mediaplayer.util.SaveValue;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mediatek.wwtv.mediaplayer.mmp.MediaMainActivity;
import com.mediatek.wwtv.mediaplayer.R;

public class DmrActivity extends Activity {

  private DmrNetWorkRecevier mDmrNetWorkRecevier = null;

  private TextView mDmrInfo;
  // private View mDmrView;
  private boolean mIsListMode;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dmr_main);
    MmpApp des = (MmpApp) this.getApplication();
    des.add(this);
    Intent intent = getIntent();
    mIsListMode = intent.getBooleanExtra("FILE_LIST", false);
    SaveValue pref = SaveValue.getInstance(this);
    boolean smbAvailable = pref.readValue(MmpConst.MY_NET_PLACE) == 0 ? false
        : true;
    boolean dlnaAvailable = pref.readValue(MmpConst.DLNA) == 0 ? false
        : true;
    MtkLog.d(TAG, "Dlna Available : " + dlnaAvailable
        + "Samba Available : " + smbAvailable);
    MultiFilesManager.getInstance(this, smbAvailable, dlnaAvailable);
    mDmrNetWorkRecevier = new DmrNetWorkRecevier();
    IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
    registerReceiver(mDmrNetWorkRecevier, intentFilter);
    initView();
    // dmr_2014 when open mmp,open dmr;
    DmrHelper.setStartListener(mListener);
    DmrHelper.openDmr(getApplicationContext());
    if (DmrHelper.isNetworkConnect(getApplicationContext())) {
      mDmrInfo.setText(getString(R.string.mmp_dmr_started));
      // mDmrView.setBackgroundResource(R.drawable.dmr_suc);
      DmrHelper.init(getApplicationContext());
    } else {
      // mDmrView.setBackgroundResource(R.drawable.dmr_fail);
      mDmrInfo.setText(getString(R.string.mmp_dmr_network_failed));
    }
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onCreate");
    } else {
      Util.LogLife(TAG, "onCreate");
    }
  }

  private static final String NAME = "/3rd_rw/dlna/xml/dmr.xml";

  private void initView() {
    // TODO Auto-generated method stub
    mDmrInfo = (TextView) findViewById(R.id.dmr_info);
    mDmrInfo.setText(getString(R.string.mmp_dmr_starting));
    // mDmrView = findViewById(R.id.dmr_layout);

    // try{
    // File file = new File(NAME);
    // Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
    // Element root = doc.getDocumentElement();
    // root.getElementsByTagName("friendlyName").item(0)
    //.setTextContent(DmrHelper.getDeviceName(getApplicationContext()));
    // TransformerFactory tf = TransformerFactory.newInstance();
    //
    // Transformer tr = tf.newTransformer();
    // tr.setOutputProperty("encoding","utf-8");
    // tr.setOutputProperty("indent", "yes");
    // DOMSource source = new DOMSource();
    // source.setNode(root);
    // StreamResult result = new StreamResult();
    // result.setOutputStream(new FileOutputStream(file));
    // tr.transform(source, result);
    //
    // }catch(Exception e){
    // e.printStackTrace();
    // }

  }

  boolean isHandled = false;

  private synchronized void handleBack() {
    if (isHandled == false) {
      isHandled = true;
      if (MultiFilesManager.hasInstance() && !mIsListMode) {
        MultiFilesManager.getInstance(this).destroy();
      }
      DmrHelper.closeDmr(getApplicationContext());
      unregisterReceiver(mDmrNetWorkRecevier);
    }
  }

  @Override
  protected void onDestroy() {
    // TODO Auto-generated method stub
    if (isHandled == false) {
      handleBack();
    }
    super.onDestroy();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // TODO Auto-generated method stub
    // temp solution
    if (keyCode == KeyEvent.KEYCODE_FUNCTION) {
      keyCode = event.getScanCode();
    }
    Log.i(TAG, "keyCode:" + keyCode);
    switch (keyCode) {
      case KeyMap.KEYCODE_MTKIR_ANGLE:
        // Util.exitMmpActivity(getApplicationContext());
        break;
      case KeyMap.KEYCODE_BACK:
        if (isHandled == false) {
          handleBack();
        }
        break;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  protected void onPause() {
    // TODO Auto-generated method stub
    super.onPause();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onPause");
    } else {
      Util.LogLife(TAG, "onPause");
    }
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onResume");
    } else {
      Util.LogLife(TAG, "onResume");
    }
  }

  @Override
  protected void onStop() {
    // TODO Auto-generated method stub
    super.onStop();
    Util.onStop(getApplicationContext());
    if (MediaMainActivity.mIsDlnaAutoTest) {
      Log.d(TAG, "onStop");
    } else {
      Util.LogLife(TAG, "onStop");
    }
  }

  class DmrNetWorkRecevier extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
      ConnectivityManager connectivityManager = (ConnectivityManager)
          context.getSystemService(Context.CONNECTIVITY_SERVICE);
      if (null != connectivityManager) {
        NetworkInfo[] netWorkInfos = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < netWorkInfos.length; i++) {
          State state = netWorkInfos[i].getState();
          if (NetworkInfo.State.CONNECTED == state) {
            MtkLog.e(TAG, "network ok.....");
            // open dmr
            DmrHelper.openDmr(context);
            mDmrInfo.setText(getString(R.string.mmp_dmr_started));
            return;
          }
        }
      }
      if (MediaMainActivity.mIsDlnaAutoTest) {
        Log.d(TAG, "network error..... no network available");
      } else {
        MtkLog.e(TAG, "network error.....no network available");
      }
      DmrHelper.closeDmr(context);
      mDmrInfo.setText(getString(R.string.mmp_dmr_network_failed));
    }
  }

  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Log.i(TAG, "msg.what:" + msg.what);
      switch (msg.what) {
        case DmrHelper.DLNA_DMR_UNVAILD:
          Toast.makeText(getApplicationContext(), getString(R.string.mmp_dmr_url_failed),
              Toast.LENGTH_SHORT).show();
          break;
      }
    }
  };
  private final iStartListener mListener = new iStartListener() {

    @Override
    public void notifyStartActivity(int type) {
      // TODO Auto-generated method stub
      Log.d(TAG, "notifyStartActivity type:" + type);
      switch (type) {
        case DmrHelper.DLNA_DMR_AUDIO:
          startMusicActivity(false);
          break;
        case DmrHelper.DLNA_DMR_VIDEO:
          startVideoActivity(false);
          break;
        case DmrHelper.DLNA_DMR_PHOTO:
          startPhotoActivity(false);
          break;
        case DmrHelper.DLNA_DMR_UNVAILD:
          DmrHelper.handleStop();
          mHandler.sendEmptyMessage(DmrHelper.DLNA_DMR_UNVAILD);
          break;
      }

    }

  };

  private void startMusicActivity(boolean fromlist) {
    // Intent intent = new Intent("android.intent.action.mmp.music");
    Intent intent = new Intent();
    intent.setClass(this, MusicPlayDmrActivity.class);
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    // if(fromlist == false){
    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // }
    startActivity(intent);
  }

  private void startVideoActivity(boolean fromlist) {
    // Intent intent = new Intent("android.intent.action.mmp.video");
    Intent intent = new Intent();
    intent.setClass(this, VideoPlayDmrActivity.class);
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    // if(fromlist == false){
    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // }
    startActivity(intent);
  }

  private void startPhotoActivity(boolean fromlist) {
    Intent intent = new Intent();
    if (Util.PHOTO_4K2K_ON) {
      intent.setClass(this, Photo4K2KPlayDmrActivity.class);
      // intent.setClass(this, Photo4K2KPlayActivity.class);
    } else {
      intent.setClass(this, PhotoPlayDmrActivity.class);
    }
    Bundle bundle = new Bundle();
    intent.putExtra(DmrHelper.DMRSOURCE, true);
    intent.putExtras(bundle);
    // if(fromlist == false){
    // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // }
    startActivity(intent);
  }

  private static String TAG = "DmrActivity";

}
