package com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.LocalFileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.util.Lists;
import com.mediatek.wwtv.mediaplayer.mmpcm.MetaData;

public class UsbFileOperater {

    private static final String TAG = "UsbFileOperater";
    static private UsbFileOperater usbFileManager = null;

    private int copyedLen;
    private boolean abortCopy = false;
    private List<File> del = new ArrayList<File>();
    private OnUsbCopyProgressListener mOnUsbCopyProgressListener;

    //SKY luojie 20180207 add for can cancel List Recursive files begin
    private boolean cancelListRecursive = false;
    //SKY luojie 20180207 add for can cancel List Recursive files end

    private UsbFileOperater() {
        del.clear();
    }

    public static UsbFileOperater getInstance() {
        if (usbFileManager == null) {
            synchronized (UsbFileOperater.class) {
                if (usbFileManager == null) {
                    usbFileManager = new UsbFileOperater();
                }
            }
        }
        return usbFileManager;
    }

    private class ImageFilter implements FilenameFilter {
        public boolean isImage(String file) {
            for (String s : FileConst.photoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isImage(fname));
        }
    }

    private class ThrdPhotoFilter implements FilenameFilter {
        public boolean isImage(String file) {
            for (String s : FileConst.thrdPhotoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isImage(fname));
        }
    }
    private class VideoFilter implements FilenameFilter {
        public boolean isVideo(String file) {
            for (String s : FileConst.videoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isVideo(fname));
        }
    }
    private class AllFilter implements FilenameFilter {
        public boolean isAccept(String file) {
            for (String s : FileConst.videoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            for (String s : FileConst.audioSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            for (String s : FileConst.photoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isAccept(fname));
        }
    }

    private class AudioFilter implements FilenameFilter {
        public boolean isAudio(String file) {
            for (String s : FileConst.audioSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isAudio(fname));
        }
    }

    private class TextFilter implements FilenameFilter {
        public boolean isText(String file) {
            for (String s : FileConst.textSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }
            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isText(fname));
        }
    }

    public class DirFilter implements FilenameFilter {

        public boolean accept(File dir, String filename) {
            return dir.isDirectory();
        }

    }

    /*
     * add
     */
    public class IsoFilter implements FilenameFilter {

        public boolean isIso(String file) {

            if (file.toLowerCase().endsWith("iso")) {
                return true;
            }

            return false;
        }

        public boolean accept(File dir, String fname) {
            return (isIso(fname));
        }
    }

    public class IsoVideoFilter implements FilenameFilter {

        public boolean isIsoVideo(String file) {
            for (String s : FileConst.isovideoSuffix) {
                if (file.toLowerCase().endsWith(s)) {
                    return true;
                }
            }

            return false;

        }

        public boolean accept(File dir, String filename) {
            return (isIsoVideo(filename));
        }

    }

    public List<File> getDirectory(File dir) {
        List<File> mfiles = new ArrayList<File>();

        File[] files = dir.listFiles();

        if (files == null) {
            return null;
        }

        for (File f : files) {
            if (f.isDirectory() && (f.isHidden() == false)) {
                mfiles.add(f);
            }
        }

        return mfiles;
    }

    public List<MtkFile> listDirectory(File dir, int sortType) {
        List<MtkFile> mfile = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new DirFilter());

        if (files == null) {
            return null;
        }

        if( sortType == FileConst.SORT_NAME || sortType == FileConst.SORT_DATE){
            sortFiles(files, sortType);
        }else{
            //default FileConst.SORT_NAME
            sortFiles(files, FileConst.SORT_NAME);

        }

        for (File f : files) {
            if (f.isDirectory() && (f.isHidden() == false)) {
                mfile.add(new MtkFile(f));
            }
        }

        return mfile;
    }

    public MtkFile[] listPhoto(File dir) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new ImageFilter());
        if(files != null) {
            for (File f : files) {
                if (f.isHidden() == false) {
                    mfiles.add(new MtkFile(f));
                }
            }
        }
        return mfiles.toArray(new MtkFile[mfiles.size()] );
    }

    public MtkFile[] listThrdPhoto(File dir) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new ThrdPhotoFilter());
        if(files != null) {
            for (File f : files) {
                if (f.isHidden() == false) {
                    mfiles.add(new MtkFile(f));
                }
            }
        }
        return mfiles.toArray(new MtkFile[mfiles.size()] );
    }


    public MtkFile[] listAudio(File dir) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new AudioFilter());
        if(files != null){
            for (File f : files) {
                if (f.isHidden() == false) {
                    mfiles.add(new MtkFile(f));
                }
            }
        }
        return mfiles.toArray(new MtkFile[mfiles.size()] );
    }

    public MtkFile[] listVideo(File dir) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new VideoFilter());
        if(files != null) {
            for (File f : files) {
                if (f.isHidden() == false) {
                    mfiles.add(new MtkFile(f));
                }
            }
        }
        return mfiles.toArray(new MtkFile[mfiles.size()] );
    }

    public MtkFile[] listText(File dir) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new TextFilter());
        if(files != null) {
            for (File f : files) {
                if (f.isHidden() == false) {
                    mfiles.add(new MtkFile(f));
                }
            }
        }
        return mfiles.toArray(new MtkFile[mfiles.size()] );
    }

    public MtkFile[] listIsoVideo(File dir) {
        List<MtkFile> mFiles = new ArrayList<MtkFile>();

        File[] files = dir.listFiles(new IsoVideoFilter());
        if(files != null) {
            for (File f : files) {
                if (f.isHidden() == false) {
                    mFiles.add(new MtkFile(f));
                }
            }
        }
        return mFiles.toArray(new MtkFile[mFiles.size()]);
    }

    private File[] listFile(File dir, int file_filter) {
        if (file_filter == FileConst.MMP_FF_VIDEO) {
            return dir.listFiles(new VideoFilter());
        } else if (file_filter == FileConst.MMP_FF_PHOTO) {
            return dir.listFiles(new ImageFilter());
        } else if (file_filter == FileConst.MMP_FF_AUDIO) {
            return dir.listFiles(new AudioFilter());
        } else if (file_filter == FileConst.MMP_FF_TEXT) {
            return dir.listFiles(new TextFilter());
        }
//        else if (file_filter == FileConst.MMP_FF_ISO) {   // add
//            return dir.listFiles(new IsoFilter());
//        }
        else if (file_filter == FileConst.MMP_FF_ISOVIDEO) {   // add
            return dir.listFiles(new IsoVideoFilter());
        } else if (file_filter == FileConst.MMP_FF_THRDPHOTO){
            return dir.listFiles(new ThrdPhotoFilter());
        }
        return null;
    }
    private File[] listFile(File dir) {

        return dir.listFiles(new AllFilter());

    }

    public MtkFile[] getFile(File dir, int fileType, int sortType) {
        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        if (fileType == FileConst.MMP_FF_VIDEO) {
            File[] files = listVideo(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        } else if (fileType == FileConst.MMP_FF_PHOTO) {
            File[] files = listPhoto(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        } else if (fileType == FileConst.MMP_FF_AUDIO) {
            File[] files = listAudio(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        } else if (fileType == FileConst.MMP_FF_TEXT) {
            File[] files = listText(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        } else if (fileType == FileConst.MMP_FF_ISOVIDEO) {   //add
            File[] files = listIsoVideo(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        }     else if (fileType == FileConst.MMP_FF_THRDPHOTO) {   //add by lei
            File[] files = listThrdPhoto(dir);
            sortFiles(files, sortType);
            for (File f : files) {
                mfiles.add(new MtkFile(f));
            }
        }

        if (mfiles.size() == 0) {
            return null;
        } else {
            return mfiles.toArray(new MtkFile[mfiles.size()]);
        }
    }

    public List<MtkFile> listFilterFiles(int file_filter, File dir, int sortType) {
        List<MtkFile> fileDir = new ArrayList<MtkFile>();
        fileDir = listDirectory(dir, sortType);
        if(fileDir != null){
            Log.d(TAG,"list dir  size :" + fileDir.size());
        }else{
            Log.d(TAG,"list dir  == null" );
        }

        File[] files = listFile(dir, file_filter);

        if (files == null && fileDir == null) {
            return null;
        }
        if(files!=null){
            Log.d(TAG,"listFile files size :"+ files.length);

            sortFiles(files, sortType);

            List<MtkFile> mfiles = new ArrayList<MtkFile>();
            for (File f : files) {
                MtkFile mf = new MtkFile(f);
                if (mf.isHidden() == false){
                    if (file_filter == FileConst.MMP_FF_VIDEO) {
                        if (mf.isVideoFile() && mf.isFile()) {
                            mfiles.add(new VideoFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_PHOTO) {
                        if (mf.isPhotoFile() && mf.isFile()) {
                            mfiles.add(new PhotoFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_AUDIO) {
                        if (mf.isAudioFile() && mf.isFile()) {
                            mfiles.add(new AudioFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_TEXT) {
                        if (mf.isTextFile() && mf.isFile()) {
                            mfiles.add(new TextFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_ISO) {  //add
                        if (mf.isIsoFile() && mf.isFile()) {
                            mfiles.add(new IsoFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_ISOVIDEO) {  //add
                        if (mf.isIsoVideoFile() && mf.isFile()) {
                            mfiles.add(new IsoVideoFile(mf));
                        }
                    } else if (file_filter == FileConst.MMP_FF_THRDPHOTO){ //add by lei
                        if (mf.isThrdPhotoFile() && mf.isFile()){
                            mfiles.add(new ThrDPhotoFile(mf));
                        }
                    }
                    else {
                        mfiles.add(mf);
                    }
                }
            }
            if(fileDir != null){
                return mergeList(fileDir, mfiles);
            }else{
                return mfiles;
            }
        }else{
            Log.d(TAG,"list files  == null" );
            if(fileDir != null){
                return fileDir;
            }else{
                return null;
            }
        }
    }
    public List<MtkFile> listFilterFiles(File dir, int sortType) {
        Log.d(TAG,"listFilterFiles dir = "+ dir.getPath() +"sortType = "+ sortType);
        List<MtkFile> fileDir = new ArrayList<MtkFile>();
        fileDir = listDirectory(dir, sortType);
        Log.d(TAG,"list dir  size :" + fileDir.size());
        /*for(File f : fileDir){
            Log.d(TAG,"listFilterFiles sort dirs  :" + f.getName());


        }*/

        File[] files = listFile(dir);

        if (files == null || fileDir == null) {
            return null;
        }
        Log.d(TAG,"listFile files size :"+ files.length);

        sortFiles(files, sortType);

        List<MtkFile> mVideofiles = new ArrayList<MtkFile>();
        List<MtkFile> mAudiofiles = new ArrayList<MtkFile>();
        List<MtkFile> mPhotofiles = new ArrayList<MtkFile>();
        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            MtkFile mf = new MtkFile(f);
            if (mf.isHidden() == false){
                if (mf.isVideoFile() && mf.isFile()) {
                    mVideofiles.add(new VideoFile(mf));
                }

                if (mf.isPhotoFile() && mf.isFile()) {
                    mPhotofiles.add(new PhotoFile(mf));
                }

                if (mf.isAudioFile() && mf.isFile()) {
                    mAudiofiles.add(new AudioFile(mf));
                }
            }

        }

        mfiles.addAll(mVideofiles);
        mfiles.addAll(mPhotofiles);
        mfiles.addAll(mAudiofiles);

        return mfiles;
    }

    private List<MtkFile> mergeList(List<MtkFile> listDir,
                                    List<MtkFile> listFile) {
        List<MtkFile> mList = new ArrayList<MtkFile>();
        int i;

        for (i = 0; i < listDir.size(); i++) {
            mList.add(listDir.get(i));
        }

        for (i = 0; i < listFile.size(); i++) {
            mList.add(listFile.get(i));
        }

        return mList;
    }

    public List<MtkFile> listAllFiles(File dir, int sortType) {

        List<MtkFile> fileDir = new ArrayList<MtkFile>();
        fileDir = listDirectory(dir, sortType);

        List<MtkFile> file = new ArrayList<MtkFile>();

        File[] files = dir.listFiles();

        if (files == null || fileDir == null) {
            return null;
        }

        for (File f : files) {
            MtkFile mf = new MtkFile(f);
            if (mf.isDirectory() == false && (mf.isHidden() == false)) {
                file.add(mf);
            }
        }

        return mergeList(fileDir, file);
    }

    private void getAudioFiles(Collection<File> af, File d) {
        Collections.addAll(af, listAudio(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getAudioFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursiveAudio(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();

        getAudioFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            mfiles.add(new AudioFile(new MtkFile(f)));
        }

        return mfiles;
    }

    // add by sky luojie begin
    public List<MtkFile> listRecursiveAudio(File dir, int sortType, int maxAudioCount) {
        Collection<File> allFile = new ArrayList<File>();

        getAudioFiles(allFile, dir, maxAudioCount);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            mfiles.add(new AudioFile(new MtkFile(f)));
        }

        return mfiles;
    }

    public void listRecursiveAudio(List<FileAdapter> af, File d) {
        if(af == null) return;
        if(cancelListRecursive) {
            Log.d(TAG, "listRecursiveAudio cancelListRecursive 1");
            return;
        }

        MtkFile[] files = listAudio(d);
        if(files != null && files.length > 0) {
            for (File f : files) {
                af.add(newWrapFile(new AudioFile(new MtkFile(f))));
            }
        }

        List<File> mDirs = getDirectory(d);
        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                if(cancelListRecursive) {
                    Log.d(TAG, "listRecursiveAudio cancelListRecursive 2");
                    return;
                }
                listRecursiveAudio(af, f);
            }
        }
    }

    private void getAudioFiles(Collection<File> af, File d, int maxFileCount) {
        if(af == null) return;
        if(af.size() >= maxFileCount) return;
        MtkFile[] files = listAudio(d);
        int needAdd = maxFileCount - af.size();
        if(needAdd >= files.length) {
            Collections.addAll(af, files);
        } else {
            for (int i = 1; i <= needAdd; i++) {
                af.add(files[i]);
            }
        }

        if(af.size() < maxFileCount) {
            List<File> mDirs = getDirectory(d);

            if (mDirs != null && mDirs.size() > 0) {
                for (File f : mDirs) {
                    getAudioFiles(af, f, maxFileCount);
                }
            }
        }
    }

    public List<MtkFile> listRecursivePhoto(File dir, int sortType, int maxPhotoCount) {
        Collection<File> allFile = new ArrayList<File>();
        getPhotoFiles(allFile, dir, maxPhotoCount);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        MtkFile mf = null;
        for (File f : files) {
            mf = new MtkFile(f);
            mfiles.add(new PhotoFile(mf));
        }

        return mfiles;
    }

    public void listRecursivePhoto(List<FileAdapter> af, File d) {
        if(af == null) return;
        if(cancelListRecursive) {
            Log.d(TAG, "listRecursivePhoto cancelListRecursive 1");
            return;
        }

        MtkFile[] files = listPhoto(d);
        if(files != null && files.length > 0) {
            for (File f : files) {
                af.add(newWrapFile(new PhotoFile(new MtkFile(f))));
            }
        }

        List<File> mDirs = getDirectory(d);
        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                if(cancelListRecursive) {
                    Log.d(TAG, "listRecursivePhoto cancelListRecursive 2");
                    return;
                }
                listRecursivePhoto(af, f);
            }
        }
    }

    private void getPhotoFiles(Collection<File> af, File d, int maxFileCount) {
        if(af == null) return;
        if(af.size() >= maxFileCount) return;
        MtkFile[] files = listPhoto(d);
        int needAdd = maxFileCount - af.size();
        if(needAdd >= files.length) {
            Collections.addAll(af, files);
        } else {
            for (int i = 1; i <= needAdd; i++) {
                af.add(files[i]);
            }
        }

        if(af.size() < maxFileCount) {
            List<File> mDirs = getDirectory(d);

            if (mDirs != null && mDirs.size() > 0) {
                for (File f : mDirs) {
                    getPhotoFiles(af, f, maxFileCount);
                }
            }
        }
    }

    /**
     * Get the
     * @param af
     * @param maxFileCount
     * @param d
     */
    private void getVideoFiles(Collection<File> af, int maxFileCount, File d, boolean exceptPVR) {
        if(af == null) return;
        if(af.size() >= maxFileCount) return;

        if(d == null) return;
        if(exceptPVR && d.getName().equalsIgnoreCase("pvr")
                && d.isDirectory()) {
            return;
        }
        MtkFile[] files = listVideo(d);
        int needAdd = maxFileCount - af.size();
        if(needAdd >= files.length) {
            Collections.addAll(af, files);
        } else {
            for (int i = 1; i <= needAdd; i++) {
                af.add(files[i]);
            }
        }

        if(af.size() < maxFileCount) {
            List<File> mDirs = getDirectory(d);

            if (mDirs != null && mDirs.size() > 0) {
                for (File f : mDirs) {
                    getVideoFiles(af, maxFileCount, f, exceptPVR);
                }
            }
        }
    }

    public List<MtkFile> listRecursiveVideo(File dir, int sortType, int maxVideoCount, boolean exceptPVR) {
        Collection<File> allFile = new ArrayList<File>();
        getVideoFiles(allFile, maxVideoCount, dir, exceptPVR);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            if(f.isDirectory()){
                Log.i(TAG,"isDirectory");
            }else{
                mfiles.add(new VideoFile(new MtkFile(f)));
            }
        }

        return mfiles;
    }

    public void listRecursiveVideo(List<FileAdapter> af, File d) {
        if(af == null) return;
        if(cancelListRecursive) {
            Log.d(TAG, "listRecursiveVideo cancelListRecursive 1");
            return;
        }

        MtkFile[] files = listVideo(d);
        if(files != null && files.length > 0) {
            for (File f : files) {
                af.add(newWrapFile(new VideoFile(new MtkFile(f))));
            }
        }

        List<File> mDirs = getDirectory(d);
        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                if(cancelListRecursive) {
                    Log.d(TAG, "listRecursiveVideo cancelListRecursive 2");
                    return;
                }
                listRecursiveVideo(af, f);
            }
        }
    }

    protected List<LocalFileAdapter> wrapFiles(List<MtkFile> originalFiles) {
        List<LocalFileAdapter> wrapedFiles = Lists.newArrayList();
        if (originalFiles != null) {
            for (MtkFile file : originalFiles) {
                LocalFileAdapter wrapedFile = newWrapFile(file);
                if (null != wrapedFile) {
                    wrapedFiles.add(wrapedFile);
                }
            }
        }
        return wrapedFiles;
    }

    protected LocalFileAdapter newWrapFile(MtkFile originalFile) {
        return new LocalFileAdapter(originalFile);
    }
    // add by sky luojie end


    private void getAllFiles(Collection<File> af, File d) {
        Collections.addAll(af, listVideo(d));
        Collections.addAll(af, listAudio(d));
        Collections.addAll(af, listPhoto(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getAllFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursive(File dir, int sortType,boolean isALL) {
        Collection<File> allFile = new ArrayList<File>();

        getAllFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        List<MtkFile> mVideofiles = new ArrayList<MtkFile>();
        List<MtkFile> mAudiofiles = new ArrayList<MtkFile>();
        List<MtkFile> mPhotofiles = new ArrayList<MtkFile>();
        for (File f : files) {
            MtkFile mf = new MtkFile(f);
            if(mf.isAudioFile()){
                mAudiofiles.add(new AudioFile(mf));
                continue;
            }
            if(mf.isVideoFile()){
                mVideofiles.add(new VideoFile(mf));
                continue;
            }
            if(mf.isPhotoFile()){
                mPhotofiles.add(new PhotoFile(mf));
                continue;
            }
        }
        mfiles.addAll(mVideofiles);
        mfiles.addAll(mAudiofiles);
        mfiles.addAll(mPhotofiles);

        return mfiles;
    }

    private void getVideoFiles(Collection<File> af, File d) {
        Collections.addAll(af, listVideo(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getVideoFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursiveVideo(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getVideoFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            if(f.isDirectory()){
                Log.i(TAG,"isDirectory");
            }else{
                mfiles.add(new VideoFile(new MtkFile(f)));
            }
        }

        return mfiles;
    }

    public List<VideoFile> recursiveVideo(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getVideoFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<VideoFile> mfiles = new ArrayList<VideoFile>();
        for (File f : files) {
            if(f.isDirectory()){
                Log.i(TAG,"isDirectory");
            }else{
                mfiles.add(new VideoFile(new MtkFile(f)));
            }
        }

        return mfiles;
    }

    private void getTextFiles(Collection<File> af, File d) {
        Collections.addAll(af, listText(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getTextFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursiveText(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getTextFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        for (File f : files) {
            mfiles.add(new TextFile(new MtkFile(f)));
        }

        return mfiles;
    }

    private void getPhotoFiles(Collection<File> af, File d) {
        Collections.addAll(af, listPhoto(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getPhotoFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursivePhoto(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getPhotoFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        MtkFile mf = null;
        for (File f : files) {
            mf = new MtkFile(f);
            mfiles.add(new PhotoFile(mf));
        }

        return mfiles;
    }

    private void getThrdPhotoFiles(Collection<File> af, File d) {
        Collections.addAll(af, listThrdPhoto(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getThrdPhotoFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursiveThrdPhoto(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getThrdPhotoFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();
        MtkFile mf = null;
        for (File f : files) {
            mf = new MtkFile(f);
            mfiles.add(new ThrDPhotoFile(mf));
        }

        return mfiles;
    }

    private void getIsoVideoFiles(Collection<File> af, File d) {
        Collections.addAll(af, listIsoVideo(d));

        List<File> mDirs = getDirectory(d);

        if (mDirs != null && mDirs.size() > 0) {
            for (File f : mDirs) {
                getIsoVideoFiles(af, f);
            }
        }
    }

    public List<MtkFile> listRecursiveSsif(File dir, int sortType) {
        Collection<File> allFile = new ArrayList<File>();
        getIsoVideoFiles(allFile, dir);

        File[] files = new File[allFile.size()];
        sortFiles(allFile.toArray(files), sortType);

        List<MtkFile> mfiles = new ArrayList<MtkFile>();

        for (File f : files) {
            mfiles.add(new IsoVideoFile(new MtkFile(f)));
        }

        return mfiles;
    }

    static public MtkFile[] shuffleFiles(MtkFile f[]) {
        List<MtkFile> list = new ArrayList<MtkFile>(Arrays.asList(f));
        Collections.shuffle(list);
        return list.toArray(new MtkFile[list.size()]);
    }

    static public void sortFiles(File[] mf, int mode) {
        switch (mode) {
            case FileConst.SORT_NAME:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        return object1.getName().compareTo(object2.getName());
                    }
                });
                break;
            case FileConst.SORT_DATE:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        if (object1.lastModified() < object2.lastModified()) {
                            return -1;
                        } else if (object1.lastModified() == object2.lastModified()) {
                            return 0;
                        } else
                            return 1;
                    }
                });
                break;
            case FileConst.SORT_TYPE:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        if ( object1.getName().lastIndexOf('.') == -1 )
                            return -1;
                        else if ( object2.getName().lastIndexOf('.') == -1 )
                            return 1;
                        String obj1 = object1.getName().substring(object1.getName().lastIndexOf('.')).toLowerCase();
                        String obj2 = object2.getName().substring(object2.getName().lastIndexOf('.')).toLowerCase();
                        return obj1.compareTo(obj2);
                    }
                });
                break;
            case FileConst.SORT_GENRE:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        if( object1.isDirectory())
                            return -1;
                        else if(object2.isDirectory())
                            return 1;
                        AudioFile obj1 = new AudioFile(new MtkFile(object1));
                        AudioFile obj2 = new AudioFile(new MtkFile(object2));
                        MetaData mMetaData1 = obj1.getMetaDataInfo();
                        MetaData mMetaData2 = obj2.getMetaDataInfo();
                        if( mMetaData1 == null )
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        String info1 = mMetaData1.getGenre();
                        String info2 = mMetaData2.getGenre();
                        if( info1 == null )
                            return -1;
                        else if( info2 == null )
                            return 1;
                        return info1.compareTo(info2);
                    }
                });
                break;
            case FileConst.SORT_ARTIST:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        if( object1.isDirectory())
                            return -1;
                        else if(object2.isDirectory())
                            return 1;
                        AudioFile obj1 = new AudioFile(new MtkFile(object1));
                        AudioFile obj2 = new AudioFile(new MtkFile(object2));
                        MetaData mMetaData1 = obj1.getMetaDataInfo();
                        MetaData mMetaData2 = obj2.getMetaDataInfo();
                        if( mMetaData1 == null )
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        String info1 = mMetaData1.getArtist();
                        String info2 = mMetaData2.getArtist();
                        if( info1 == null )
                            return -1;
                        else if( info2 == null )
                            return 1;
                        return info1.compareTo(info2);
                    }
                });
                break;
            case FileConst.SORT_ALBUM:
                Arrays.sort(mf, new Comparator<File>() {
                    public int compare(File object1, File object2) {
                        if( object1.isDirectory())
                            return -1;
                        else if(object2.isDirectory())
                            return 1;
                        AudioFile obj1 = new AudioFile(new MtkFile(object1));
                        AudioFile obj2 = new AudioFile(new MtkFile(object2));
                        MetaData mMetaData1 = obj1.getMetaDataInfo();
                        MetaData mMetaData2 = obj2.getMetaDataInfo();
                        if( mMetaData1 == null )
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        String info1 = mMetaData1.getAlbum();
                        String info2 = mMetaData2.getAlbum();
                        if( info1 == null )
                            return -1;
                        else if( info2 == null )
                            return 1;
                        return info1.compareTo(info2);
                    }
                });
                break;

            default:
                break;
        }
    }

    static public void sortAudioFiles(AudioFile[] af, int mode) {
        switch (mode) {
            case FileConst.SORT_GENRE:
                Arrays.sort(af, new Comparator<AudioFile>() {
                    public int compare(AudioFile object1, AudioFile object2) {
                        MetaData mMetaData1 = object1.getMetaDataInfo();
                        MetaData mMetaData2 = object2.getMetaDataInfo();
                        if( mMetaData1 == null || mMetaData1.getGenre() == null)
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        return mMetaData1.getGenre().compareTo(mMetaData2.getGenre());
                    }
                });
                break;
            case FileConst.SORT_ARTIST:
                Arrays.sort(af, new Comparator<AudioFile>() {
                    public int compare(AudioFile object1, AudioFile object2) {
                        MetaData mMetaData1 = object1.getMetaDataInfo();
                        MetaData mMetaData2 = object2.getMetaDataInfo();
                        if( mMetaData1 == null || mMetaData1.getArtist() == null)
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        return mMetaData1.getArtist().compareTo(mMetaData2.getArtist());
                    }
                });
                break;
            case FileConst.SORT_ALBUM:
                Arrays.sort(af, new Comparator<AudioFile>() {
                    public int compare(AudioFile object1, AudioFile object2) {
                        MetaData mMetaData1 = object1.getMetaDataInfo();
                        MetaData mMetaData2 = object2.getMetaDataInfo();
                        if( mMetaData1 == null || mMetaData1.getAlbum() == null)
                            return -1;
                        else if( mMetaData2 == null )
                            return 1;
                        return mMetaData1.getAlbum().compareTo(mMetaData2.getAlbum());
                    }
                });
                break;

            default:
                break;
        }
    }

    /**
     *
     * @param abort
     */
    public void setCopyAbort(boolean abort) {
        abortCopy = abort;
    }

    /**
     *
     * @author MTK94044
     *
     */
    public interface OnUsbCopyProgressListener {
        void onSetProgress(long len);
    }

    /**
     *
     * @param listener
     */
    public void setOnUsbCopyProgressListener(OnUsbCopyProgressListener listener) {
        mOnUsbCopyProgressListener = listener;
    }

    /**
     *
     * @param src
     * @param dst
     * @return
     * @throws IOException
     */
    public boolean copyFile2Dir(MtkFile src, MtkFile dst) throws IOException {
        File dstFile;

        if (false == dst.isDirectory() && true == src.isDirectory()) {
            throw new IOException(
                    "src should be a regular file and dst should be a diretory!");
        } else {
            dstFile = new File(new File(dst.getPath()), src.getName());
        }

        InputStream in = null;
        OutputStream out = null;
        try {

            in = new FileInputStream(new File(src.getPath()));

            out = new FileOutputStream(dstFile);

            if(in == null || out == null)
                return false;
            // Transfer bytes from in to out
            copyedLen = 0;

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                if (abortCopy == true) {
                    break;
                }

                out.write(buf, 0, len);

                copyedLen += len;

                if (mOnUsbCopyProgressListener != null) {
                    mOnUsbCopyProgressListener.onSetProgress(copyedLen);
                }
            }
            if (abortCopy == true) {
                abortCopy = false;
                dstFile.delete();
            }

        } catch (Exception e) {
            // TODO: handle exception
            return false;
        } finally {
            try{
                if(in != null)
                    in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                if(out != null)
                    out.close();
            }
        }

        return true;
    }

    /**
     *
     * @param f
     * @return
     * @throws IOException
     */
    public boolean addFileToDeleteList(MtkFile f) throws IOException {
        if (f == null) {
            throw new NullPointerException("delete file is null!");
        }

        return del.add(new File(f.getPath()));
    }

    /**
     *
     */
    public void deleteFiles() {
        Iterator<File> it = del.iterator();
        while (it.hasNext()) {
            it.next().delete();
            it.remove();
        }
    }

    //SKY luojie 20180207 add for can cancel List Recursive files begin
    public boolean isCancelListRecursive() {
        return cancelListRecursive;
    }

    public void setCancelListRecursive(boolean cancelListRecursive) {
        this.cancelListRecursive = cancelListRecursive;
    }
    //SKY luojie 20180207 add for can cancel List Recursive files end
}
