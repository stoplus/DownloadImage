package com.example.den.downloadimage.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "imageObj")
public class ImageObj {
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
}
