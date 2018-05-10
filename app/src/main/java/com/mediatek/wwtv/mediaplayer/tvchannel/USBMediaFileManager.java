package com.mediatek.wwtv.mediaplayer.tvchannel;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.mediatek.wwtv.mediaplayer.mmp.model.FileAdapter;
import com.mediatek.wwtv.mediaplayer.mmp.model.MultiFilesManager;
import com.mediatek.wwtv.mediaplayer.mmp.util.MultiMediaConstant;
import com.mediatek.wwtv.mediaplayer.mmp.util.SkyThumbnailUtils;
import com.mediatek.wwtv.mediaplayer.R;
import com.mediatek.wwtv.mediaplayer.mmpcm.fileimpl.FileConst;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Author SKY205711 luojie
 * Date   2018/1/2
 * Description: This is USBMediaFileManager
 */
public class USBMediaFileManager {

    private static final String TAG = "USBMediaFileManager";
    public static int mThumbnailWidth = 640;
    public static int mThumbnailHeight = 360;

    private static final String THUMBNAIL_SAVE_APP_DIR = "MediaPlay";
    private static final String THUMBNAIL_SAVE_SD_DIR = "Pictures";

    public static String getHashKey(String str) {
        String cacheKey;
        try {
            final MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());
            cacheKey = bytesToHexString(mDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(str.hashCode());
        }
        return cacheKey;
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * Save the usb media thumbnail to sdcard and return the thumbnail's origin file path.
     *
     * @param usbPath
     * @param context
     * @param filesManager
     * @return
     */
    public static ProgramFile getUSBCardImageFilePath(String usbPath, Context context, MultiFilesManager filesManager) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String thumbnailPath = sdPath + File.separator + THUMBNAIL_SAVE_SD_DIR
                + File.separator + THUMBNAIL_SAVE_APP_DIR;
        ProgramFile programFile = USBMediaFileManager.getProgramThumbnail(usbPath, thumbnailPath,
                context, filesManager, true);
        if(programFile != null) {
            programFile.setProgramType(ProgramFile.TYPE_PROGRAM_USB);
        }
        return programFile;
    }

    /**
     * Save the pvr media thumbnail to sdcard and return the thumbnail's origin file path.
     *
     * @param pvrPath
     * @param context
     * @param filesManager
     * @return
     */
    public static ProgramFile getPVRCardImageFilePath(String pvrPath, Context context, MultiFilesManager filesManager) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String thumbnailPath = sdPath + File.separator + THUMBNAIL_SAVE_SD_DIR
                + File.separator + THUMBNAIL_SAVE_APP_DIR;
        ProgramFile programFile = USBMediaFileManager.getProgramThumbnail(pvrPath, thumbnailPath,
                context, filesManager, false);
        if(programFile != null) {
            programFile.setProgramType(ProgramFile.TYPE_PROGRAM_PVR);
        }
        return programFile;
    }

    /**
     *
     * @param usbPath
     * @param thumbnailSavePath
     * @param context
     * @param filesManager
     * @return return the thumbnail picture origin file path
     */
    private static ProgramFile getProgramThumbnail(String usbPath, String thumbnailSavePath,
                                                Context context, MultiFilesManager filesManager, boolean isUSBProgram) {
        if(thumbnailSavePath == null) return null;
        File thumbnailPath = new File(thumbnailSavePath);
        if(!thumbnailPath.exists()) {
            thumbnailPath.mkdirs();
        }
        boolean success = false;

        Resources res = context.getResources();
        mThumbnailWidth = (int) res.getDimension(R.dimen.tvblock_desktop_imgwidth);
        mThumbnailHeight = (int) res.getDimension(R.dimen.tvblock_desktop_imgheight);

        String thumbnailName = null;
        List<FileAdapter> movies = filesManager.listRecursiveFiles(usbPath, MultiMediaConstant.VIDEO,2, isUSBProgram);
        if(movies != null && movies.size() > 0) {
            Log.d(TAG, "getUSBProgramThumbnail movies.get(0):" + movies.get(0).getAbsolutePath());
            Bitmap bitmap = null;
            String tempFilePath = null;
            for(FileAdapter file : movies) {
                bitmap = SkyThumbnailUtils.getVideoThumbnailMINI_KIND(file.getAbsolutePath(), mThumbnailWidth, mThumbnailHeight);
                //bitmap = SkyThumbnailUtils.getVideoThumbnailMINI_KIND(file.getAbsolutePath());
                if(bitmap == null) {
                    bitmap = file.getThumbnail(mThumbnailWidth, mThumbnailHeight, true);
                }
                if(bitmap != null) {
                    thumbnailName = getHashKey(file.getAbsolutePath()) + ".png";
                    tempFilePath = file.getAbsolutePath();
                    break;
                }
            }
            if(bitmap != null) {
                success = SkyThumbnailUtils.saveBitmap(bitmap, thumbnailSavePath + File.separator + thumbnailName);
                if(success) {
                    return new ProgramFile(tempFilePath, thumbnailSavePath + File.separator
                            + thumbnailName, ProgramFile.TYPE_FILE_VIDEO);
                }
            }
            return new ProgramFile(movies.get(0).getAbsolutePath(), null, ProgramFile.TYPE_FILE_VIDEO);
        }

        if(!isUSBProgram) return null;

        if(movies == null || movies.size() < 1) {
            Log.d(TAG, "getUSBProgramThumbnail movies.size() == 0");
            List<FileAdapter> pictures = filesManager.listRecursiveFiles(usbPath, MultiMediaConstant.PHOTO, 2, isUSBProgram);
            if(pictures != null && pictures.size() > 0) {
                Log.d(TAG, "getUSBProgramThumbnail pictures.get(0):" + pictures.get(0).getAbsolutePath());
                Bitmap bitmap = null;
                String tempFilePath = null;
                for(FileAdapter file : pictures) {
                    bitmap = file.getThumbnail(mThumbnailWidth, mThumbnailHeight, true);
                    if(bitmap == null) {
                        bitmap = SkyThumbnailUtils.getPictureThumbnail(file.getAbsolutePath(), mThumbnailWidth, mThumbnailHeight);
                    }
                    if(bitmap != null) {
                        thumbnailName = getHashKey(file.getAbsolutePath()) + ".png";
                        tempFilePath = file.getAbsolutePath();
                        break;
                    }
                }
                if(bitmap != null) {
                    success = SkyThumbnailUtils.saveBitmap(bitmap, thumbnailSavePath + File.separator + thumbnailName);
                    if(success) {
                        return new ProgramFile(tempFilePath, thumbnailSavePath + File.separator
                                + thumbnailName, ProgramFile.TYPE_FILE_PICTURE);
                    }
                }
                return new ProgramFile(tempFilePath, null, ProgramFile.TYPE_FILE_PICTURE);
            }

            if(pictures == null || pictures.size() < 1) {
                Log.d(TAG, "getUSBProgramThumbnail pictures.size() == 0");
                List<FileAdapter> musics = filesManager.listRecursiveFiles(usbPath, MultiMediaConstant.AUDIO, 2, isUSBProgram);
                if(musics != null && musics.size() > 0) {
                    Log.d(TAG, "getUSBProgramThumbnail musics.get(0):" + musics.get(0).getAbsolutePath());
                    Bitmap bitmap = null;
                    String tempFilePath = null;
                    for(FileAdapter file : musics) {
                        bitmap = file.getThumbnail(mThumbnailWidth, mThumbnailHeight, true);
                        if(bitmap != null) {
                            thumbnailName = getHashKey(file.getAbsolutePath()) + ".png";
                            tempFilePath = file.getAbsolutePath();
                            break;
                        }
                    }
                    if(bitmap != null) {
                        success = SkyThumbnailUtils.saveBitmap(bitmap, thumbnailSavePath + File.separator + thumbnailName);
                        if(success) {
                            return new ProgramFile(tempFilePath, thumbnailSavePath
                                    + File.separator + thumbnailName, ProgramFile.TYPE_FILE_AUDIO);
                        }
                    }
                    return new ProgramFile(tempFilePath, null, ProgramFile.TYPE_FILE_AUDIO);
                }
            }
        }
        return null;
    }

    /**
     * Delete external storage file: Pictures/MediaPlay/
     */
    public static void deleteThumbnail() {
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String thumbnailPath = sdPath + File.separator + THUMBNAIL_SAVE_SD_DIR + File.separator + THUMBNAIL_SAVE_APP_DIR;
        File dir = new File(thumbnailPath);
        if(dir.exists()) {
            File[] files = dir.listFiles();
            if(files != null) {
                for(File f : files) {
                    f.delete();
                }
            }
        }
    }

    public static boolean isVideo(String file) {
        for (String s : FileConst.videoSuffix) {
            if (file.toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudio(String file) {
        for (String s : FileConst.audioSuffix) {
            if (file.toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPicture(String file) {
        for (String s : FileConst.photoSuffix) {
            if (file.toLowerCase().endsWith(s)) {
                return true;
            }
        }
        return false;
    }
}
