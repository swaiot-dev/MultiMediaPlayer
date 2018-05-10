package com.mediatek.wwtv.mediaplayer.tvchannel;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author SKY205711
 * Date   2018/1/9
 * Description: This is ProgramFile
 */
public class ProgramFile implements Parcelable {

    public static final Creator CREATOR = new Creator() {
        @Override
        public ProgramFile createFromParcel(Parcel source) {
            return new ProgramFile(source);
        }

        @Override
        public ProgramFile[] newArray(int size) {
            return new ProgramFile[0];
        }
    };

    public static final int TYPE_FILE_VIDEO = 0;
    public static final int TYPE_FILE_AUDIO = 2;
    public static final int TYPE_FILE_PICTURE = 1;

    public static final int TYPE_PROGRAM_USB = 1;
    public static final int TYPE_PROGRAM_PVR = 2;

    //The thumbnail origin file path
    private String filePath;
    //The thumbnail own file path
    private String thumbnailPath;
    //USB or PVR
    private int programType;
    //video or audio or picture
    private int fileType;

    public ProgramFile(String filePath, String thumbnailPath, int fileType) {
        this.filePath = filePath;
        this.thumbnailPath = thumbnailPath;
        this.fileType = fileType;
    }

    private ProgramFile(Parcel in) {
        filePath = in.readString();
        thumbnailPath = in.readString();
        programType = in.readInt();
        fileType = in.readInt();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public int getProgramType() {
        return programType;
    }

    public void setProgramType(int programType) {
        this.programType = programType;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(thumbnailPath);
        dest.writeInt(programType);
        dest.writeInt(fileType);
    }

    @Override
    public String toString() {
        return "ProgramFile{" +
                "filePath='" + filePath + '\'' +
                ", thumbnailPath='" + thumbnailPath + '\'' +
                ", programType=" + programType +
                ", fileType=" + fileType +
                '}';
    }
}
