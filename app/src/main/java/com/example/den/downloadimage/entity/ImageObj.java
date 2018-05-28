package com.example.den.downloadimage.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "imageObj")
public class ImageObj implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "link")
    private String link;

    @ColumnInfo(name = "linkDevice")
    private String linkDevice;

    @ColumnInfo(name = "download")
    private boolean download;

    public ImageObj(String link, String linkDevice, boolean download) {
        this.link = link;
        this.linkDevice = linkDevice;
        this.download = download;
    }

    protected ImageObj(Parcel in) {
        id = in.readInt();
        link = in.readString();
        linkDevice = in.readString();
        download = in.readByte() != 0;
    }

    public static final Creator<ImageObj> CREATOR = new Creator<ImageObj>() {
        @Override
        public ImageObj createFromParcel(Parcel in) {
            return new ImageObj(in);
        }

        @Override
        public ImageObj[] newArray(int size) {
            return new ImageObj[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLinkDevice() {
        return linkDevice;
    }

    public void setLinkDevice(String linkDevice) {
        this.linkDevice = linkDevice;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(link);
        dest.writeString(linkDevice);
        dest.writeByte((byte) (download ? 1 : 0));
    }
}
