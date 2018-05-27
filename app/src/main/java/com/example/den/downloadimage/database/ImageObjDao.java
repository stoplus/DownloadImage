package com.example.den.downloadimage.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.den.downloadimage.entity.ImageObj;

import java.util.List;

import io.reactivex.Flowable;

@Dao
public interface ImageObjDao {
    @Query("SELECT * FROM imageObj ORDER BY id")
    Flowable<List<ImageObj>> all();

    @Query("SELECT * FROM imageObj WHERE download = 1 ORDER BY id")
    Flowable<ImageObj[]> allDownloaded();

    @Query("SELECT * FROM imageObj WHERE download = 0 ORDER BY id")
    Flowable<ImageObj[]> allNotDownloaded();

    @Query("SELECT COUNT(*) from imageObj")
    int count();

    @Insert
    void insert(ImageObj... imageObjs);

    @Update
    void update(ImageObj imageObj);

    @Delete
    void delete(ImageObj... imageObj);
}
