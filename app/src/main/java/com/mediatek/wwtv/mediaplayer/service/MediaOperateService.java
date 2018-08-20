package com.mediatek.wwtv.mediaplayer.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.model.FilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.MusicPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.Photo4K2KPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.PhotoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.TextPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.multimedia.VideoPlayActivity;
import com.mediatek.wwtv.mediaplayer.mmp.util.LogicManager;
import com.mediatek.wwtv.mediaplayer.setting.util.IMediaAidlInterface;
import com.mediatek.wwtv.mediaplayer.util.MmpApp;
import com.mediatek.wwtv.mediaplayer.util.Util;
import com.mediatek.wwtv.util.MtkLog;

import java.lang.ref.WeakReference;
import java.util.List;

public class MediaOperateService extends Service {
    private static final String TAG = MediaOperateService.class.getSimpleName();

    public static final int VIDEO_TYPE = 0;
    public static final int PHOTO_TYPE = 1;
    public static final int MUSIC_TYPE = 2;

    private MmpApp mmpApp;
    private MultiFilesManager multiFilesManager;
    private USBFileManager usbFileManager;

    private Handler mHandler = new H(this);

    public IBinder mBinder = new IMediaAidlInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void Previous() throws RemoteException {   //上一个
            mHandler.sendEmptyMessage(H.PREVIOUS);
        }

        @Override
        public void Next() throws RemoteException {   //下一个
            mHandler.sendEmptyMessage(H.NEXT);
        }

        @Override
        public void Play() throws RemoteException {   //继续播放
            mHandler.sendEmptyMessage(H.PLAY);
        }

        @Override
        public void Pause() throws RemoteException {   //暂停播放
            mHandler.sendEmptyMessage(H.PAUSE);
        }

        @Override
        public void Stop() throws RemoteException {   //停止播放
            mHandler.sendEmptyMessage(H.STOP);
        }

        @Override
        public void StartOver() throws RemoteException {   //重来
            mHandler.sendEmptyMessage(H.STARTOVER);
        }

        @Override
        public void Rewind() throws RemoteException {   //倒带
            mHandler.sendEmptyMessage(H.REWIND);
        }

        @Override
        public void FastForward() throws RemoteException {   //快进
            mHandler.sendEmptyMessage(H.FAST_FORWARD);
        }

        @Override
        public void playResource(int type) throws RemoteException {
            Message message = mHandler.obtainMessage();
            message.what = H.PLAY_RESOURCE;
            message.arg1 = type;
            mHandler.sendMessage(message);
        }

    };

    private void fastward() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Fastward();
        } else if ("PhotoPlayActivity".equals(className)) {

        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.FastForward();
        }
    }

    private void rewind() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Rewind();
        } else if ("PhotoPlayActivity".equals(className)) {

        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Rewind();
        }
    }

    private void startOver() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.StartOver();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            //                activity.StartOver();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
//                            activity.StartOver();
        }
    }

    private void stop() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Stop();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            activity.finish();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Stop();
        }
    }

    private void pause() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Pause();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            activity.Pause();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Pause();
        }
    }

    private void play() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Play();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            activity.Play();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Play();
        }
    }

    private void next() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Next();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            activity.Next();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Next();
        }
    }

    private void previous() {
        getClassNames();
        if ("MusicPlayActivity".equals(className)) {
            MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
            activity.Previous();
        } else if ("PhotoPlayActivity".equals(className)) {
            PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
            activity.Previous();
        } else if ("VideoPlayActivity".equals(className)) {
            VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
            activity.Previous();
        }
    }

    //add by y.wan for play resource for AIDL start 2018/6/29
    /**
     * play media by type
     *
     * @param type
     */
    private void playResourceByType(int type) {
        if (mmpApp == null) {
            mmpApp = ((MmpApp) getApplication());
        }
        if (usbFileManager == null) {
            usbFileManager = USBFileManager.getInstance();
        }

        getClassNames();

        if (type == VIDEO_TYPE) {
            startVideo(type);
            Log.d(TAG, "playResource: " + "video type");
        } else if (type == MUSIC_TYPE) {
            startMusic(type);
            Log.d(TAG, "playResource: " + "music type");
        } else if (type == PHOTO_TYPE) {
            startPhoto(type);
            Log.d(TAG, "playResource: " + "photo type");
        } else {
            Log.d(TAG, "playResource: " + "type not find");
        }
    }

    private void startVideo(int type) {
        if (!"VideoPlayActivity".equals(className)) {
            if (usbFileManager.getVideoLists().size() > 0) {
                if ("MusicPlayActivity".equals(className)) {
                    MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
                    activity.Stop();
                } else if ("PhotoPlayActivity".equals(className)) {
                    PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
                    if (!activity.isFinishing())
                        activity.finish();
                }
                startPlayActivity(usbFileManager.getVideoLists().get(0).getAbsolutePath(), type);
            }
        }
    }

    private void startMusic(int type) {
        if (!"MusicPlayActivity".equals(className)) {
            if (usbFileManager.getMusicLists().size() > 0) {
                if ("VideoPlayActivity".equals(className)) {
                    VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
                    activity.Stop();
                } else if ("PhotoPlayActivity".equals(className)) {
                    PhotoPlayActivity activity = (PhotoPlayActivity) MmpApp.getActivity();
                    if (!activity.isFinishing())
                        activity.finish();
                }
                startPlayActivity(usbFileManager.getMusicLists().get(0).getAbsolutePath(), type);
            }
        }
    }

    private void startPhoto(int type) {
        if (!"PhotoPlayActivity".equals(className)) {
            if (usbFileManager.getPhotoLists().size() > 0) {
                if ("MusicPlayActivity".equals(className)) {
                    MusicPlayActivity activity = (MusicPlayActivity) MmpApp.getActivity();
                    activity.Stop();
                } else if ("VideoPlayActivity".equals(className)) {
                    VideoPlayActivity activity = (VideoPlayActivity) MmpApp.getActivity();
                    activity.Stop();
                }
                startPlayActivity(usbFileManager.getPhotoLists().get(0).getAbsolutePath(), type);
            }
        }
    }

    private void startPlayActivity(String path, int contentType) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("path", path);

        if (multiFilesManager == null) {
            multiFilesManager = MultiFilesManager.getInstance(getApplicationContext());
        }

        multiFilesManager.setCurrentSourceType(MultiFilesManager.SOURCE_LOCAL);
        //multiFilesManager.closePlayer();
        MtkLog.d(TAG, "playFile,mIsEnterPip==" + Util.mIsEnterPip);

        if (contentType == FilesManager.CONTENT_VIDEO
                || contentType == FilesManager.CONTENT_AUDIO
                || contentType == FilesManager.CONTENT_PHOTO
                || contentType == FilesManager.CONTENT_THRDPHOTO) {
            exitPIP();
        }

        if (contentType == FilesManager.CONTENT_VIDEO || Util.mIsEnterPip) {
            LogicManager.getInstance(getApplicationContext()).finishVideo();
            if (VideoPlayActivity.getInstance() != null) {
                VideoPlayActivity.getInstance().finish();
            }
        }
        MtkLog.d(TAG, "playFile contentType:" + contentType);
        if (contentType == FilesManager.CONTENT_PHOTO) {
            MtkLog.i(TAG, "FilesManager.CONTENT_PHOTO");
            multiFilesManager.getPlayList(usbFileManager.getPhotoLists(), 0, FilesManager.CONTENT_PHOTO,
                    MultiFilesManager.SOURCE_LOCAL);
            Util.reset3D(this);
            if (Util.PHOTO_4K2K_ON) {
                MtkLog.i(TAG, "4k2k on");
                intent.setClass(this, Photo4K2KPlayActivity.class);
            } else {
                MtkLog.i(TAG, "4k2k off");
                intent.setClass(this, PhotoPlayActivity.class);
            }
            bundle.putInt("PlayMode", 0);
        } else if (contentType == FilesManager.CONTENT_AUDIO) {
            MtkLog.i(TAG, "FilesManager.CONTENT_AUDIO");
            multiFilesManager.getPlayList(usbFileManager.getMusicLists(), 0, FilesManager.CONTENT_AUDIO,
                    MultiFilesManager.SOURCE_LOCAL);
            intent.setClass(this, MusicPlayActivity.class);
            intent.putExtras(bundle);

        } else if (contentType == FilesManager.CONTENT_VIDEO) {
            multiFilesManager.getPlayList(usbFileManager.getVideoLists(), 0, FilesManager.CONTENT_VIDEO,
                    MultiFilesManager.SOURCE_LOCAL);
            intent.setClass(this, VideoPlayActivity.class);
        } else if (contentType == FilesManager.CONTENT_TEXT) {
            intent.setClass(this, TextPlayActivity.class);
        }

        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

    protected void exitPIP() {
        //noral case, should close other android PIP.
        String package_name = this.getApplication().getPackageName();
        MtkLog.d(TAG, "send broadcast exit pip in file base package_name:" + package_name);
        if (package_name == null || package_name.equals("")) {
            package_name = "com.mediatek.wwtv.mediaplayer";
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_RESOURCE_GRANTED);
        intent.putExtra(Intent.EXTRA_PACKAGES,
                new String[]{package_name});
        intent.putExtra(Intent.EXTRA_MEDIA_RESOURCE_TYPE,
                Intent.EXTRA_MEDIA_RESOURCE_TYPE_VIDEO_CODEC);
        this.sendBroadcastAsUser(intent,
                new UserHandle(ActivityManager.getCurrentUser()),
                android.Manifest.permission.RECEIVE_MEDIA_RESOURCE_USAGE);
    }
//add by y.wan for play resource for AIDL end 2018/6/29

    String className;

    public void getClassNames() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        className = runningTasks.get(0).topActivity.getShortClassName();
        int index = className.lastIndexOf(".");
        if (-1 != index && (++index) <= className.length()) {
            className = className.substring(index);
        }
//        String[] results = className.split(".");
//        className = results[results.length - 1];
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: MyServices");
        return mBinder;

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: MyServices");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: MyServices");
        return super.onStartCommand(intent, flags, startId);
    }

    private static class H extends Handler {

        public static final int PLAY = 0x101;
        public static final int PAUSE = 0x102;
        public static final int STOP = 0x103;
        public static final int FAST_FORWARD = 0x104;
        public static final int REWIND = 0x105;
        public static final int NEXT = 0x106;
        public static final int PREVIOUS = 0x107;
        public static final int STARTOVER = 0x108;
        public static final int PLAY_RESOURCE = 0x109;

        private WeakReference<MediaOperateService> mWeakReference;

        public H(MediaOperateService myService) {
            mWeakReference = new WeakReference<>(myService);
        }

        @Override
        public void handleMessage(Message msg) {
            MediaOperateService mService = mWeakReference.get();
            if (null == mService) {
                return;
            }
            switch (msg.what) {
                case PLAY:
                    mService.play();
                    break;
                case PAUSE:
                    mService.pause();
                    break;
                case STOP:
                    mService.stop();
                    break;
                case FAST_FORWARD:
                    mService.fastward();
                    break;
                case REWIND:
                    mService.rewind();
                    break;
                case NEXT:
                    mService.next();
                    break;
                case PREVIOUS:
                    mService.previous();
                    break;
                case STARTOVER:
                    mService.startOver();
                    break;
                case PLAY_RESOURCE:
                    mService.playResourceByType(msg.arg1);
                    break;
            }
            super.handleMessage(msg);
        }
    }
}
